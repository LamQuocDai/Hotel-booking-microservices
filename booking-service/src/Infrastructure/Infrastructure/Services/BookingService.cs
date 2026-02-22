using Application.Constants;
using Application.DTOs;
using Application.Interfaces;
using AutoMapper;
using Infrashtructure.Data;
using Microsoft.EntityFrameworkCore;
using Infrashtructure.Validators;
using Domain.Entities;
using Domain.Enums;
using Infrashtructure.Helpers;

namespace Infrashtructure.Services
{
    public class BookingService : IBookingService
    {
        private readonly BookingDbContext _context;
        private readonly IMapper _mapper;
        private readonly IBookingRepository _bookingRepository;
        private readonly RedisLockHelper _redisLock;

        public BookingService(
            BookingDbContext context, 
            IMapper mapper,
            IBookingRepository bookingRepository,
            RedisLockHelper redisLock)
        {
            _context = context;
            _mapper = mapper;
            _bookingRepository = bookingRepository;
            _redisLock = redisLock;
        }

        public async Task<ApiResponseDto<PagedResponseDto<BookingDto>>> GetBookingsAsync(BookingPaginationRequestDto request)
        {
            try
            {
                var query = _context.Bookings.Where(b => b.DeletedAt == null).AsQueryable();
                if(request.RoomId.HasValue)
                {
                    query = query.Where(b => b.RoomId == request.RoomId.Value);
                }
                if(request.AccountId.HasValue)
                {
                    query = query.Where(b => b.AccountId == request.AccountId.Value);
                }
                if(request.CheckInTime.HasValue && request.CheckOutTime.HasValue)
                {
                    query = query.Where(b => b.CheckInTime >= request.CheckInTime.Value && b.CheckOutTime <= request.CheckOutTime.Value);
                }
                if(request.Status.HasValue)
                {
                    query = query.Where(b => b.Status == request.Status.Value);
                }

                var total = await query.CountAsync();
                var totalPages = (int)Math.Ceiling(total / (double)request.PageSize);
                var bookings = await query
                    .Skip((request.PageNumber - 1) * request.PageSize)
                    .Take(request.PageSize)
                    .ToListAsync();
                var bookingDtos = _mapper.Map<List<BookingDto>>(bookings);
                var pagedResponse = new PagedResponseDto<BookingDto>
                {
                    Items = bookingDtos,
                    Paging = new PagingDto
                    {
                        Total = total,
                        PageNumber = request.PageNumber,
                        PageSize = request.PageSize,
                        TotalPages = totalPages,
                        HasPrevious = request.PageNumber > 1,
                        HasNext = request.PageNumber < totalPages,
                    }
                };

                return new ApiResponseDto<PagedResponseDto<BookingDto>>
                {
                    IsSuccess = true,
                    Data = pagedResponse,
                    Message = "Bookings retrieved successfully.",
                    StatusCode = ApplicationStatusCode.Success,
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<PagedResponseDto<BookingDto>>
                {
                    IsSuccess = false,
                    Message = $"An error occurred: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }

        public async Task<ApiResponseDto<BookingDto>> GetBookingByIdAsync(Guid bookingId)
        {
            try
            {
                var booking = await _context.Bookings
                    .FirstOrDefaultAsync(b => b.Id == bookingId && b.DeletedAt == null);
                if (booking == null)
                {
                    return new ApiResponseDto<BookingDto>
                    {
                        IsSuccess = false,
                        Message = "Booking not found.",
                        StatusCode = ApplicationStatusCode.NotFound
                    };
                }
                return new ApiResponseDto<BookingDto>
                {
                    IsSuccess = true,
                    Data = _mapper.Map<BookingDto>(booking),
                    Message = "Booking retrieved successfully.",
                    StatusCode = ApplicationStatusCode.Success,
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<BookingDto>
                {
                    IsSuccess = false,
                    Message = $"An error occurred: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }

        public async Task<ApiResponseDto<BookingDto>> CreateBookingAsync(CreateBookingDto createBookingDto)
        {
            try
            {
                // 1. Validate booking dates
                var dateValidation = BookingValidator.ValidateBookingDates(createBookingDto.CheckInTime, createBookingDto.CheckOutTime);
                if (!dateValidation.IsValid)
                {
                    return new ApiResponseDto<BookingDto>
                    {
                        IsSuccess = false,
                        Message = dateValidation.ErrorMessage,
                        StatusCode = ApplicationStatusCode.BadRequest
                    };
                }

                // 2. Check room existence
                if (!await _context.Rooms.AnyAsync(r => r.Id == createBookingDto.RoomId && r.DeletedAt == null))
                {
                    return new ApiResponseDto<BookingDto>
                    {
                        IsSuccess = false,
                        Message = "Room does not exist.",
                        StatusCode = ApplicationStatusCode.BadRequest
                    };
                }

                // 3. Acquire Redis distributed lock to prevent race conditions
                // Lock key format: lock:room:{roomId}
                var lockKey = $"lock:room:{createBookingDto.RoomId}";
                var lockValue = Guid.NewGuid().ToString();
                var lockAcquired = await _redisLock.AcquireLockAsync(lockKey, lockValue, expiryMilliseconds: 10000);

                if (!lockAcquired)
                {
                    return new ApiResponseDto<BookingDto>
                    {
                        IsSuccess = false,
                        Message = "Unable to process booking at this time. Please try again.",
                        StatusCode = ApplicationStatusCode.Conflict
                    };
                }

                try
                {
                    // 4. Start database transaction
                    await using var transaction = await _context.Database.BeginTransactionAsync();

                    try
                    {
                        // 5. Recheck availability inside transaction (double-check pattern)
                        // This ensures no booking was created between initial check and lock acquisition
                        var isAvailable = await _bookingRepository.IsRoomAvailableAsync(
                            createBookingDto.RoomId,
                            createBookingDto.CheckInTime,
                            createBookingDto.CheckOutTime);

                        if (!isAvailable)
                        {
                            return new ApiResponseDto<BookingDto>
                            {
                                IsSuccess = false,
                                Message = "Room is not available for the selected dates.",
                                StatusCode = ApplicationStatusCode.Conflict
                            };
                        }

                        // 6. Create booking with default status
                        var booking = _mapper.Map<Booking>(createBookingDto);
                        booking.Status = BookingStatus.Holding;

                        _context.Bookings.Add(booking);
                        await _context.SaveChangesAsync();

                        // 7. Commit transaction
                        await transaction.CommitAsync();

                        return new ApiResponseDto<BookingDto>
                        {
                            IsSuccess = true,
                            Data = _mapper.Map<BookingDto>(booking),
                            Message = "Booking created successfully.",
                            StatusCode = ApplicationStatusCode.Created,
                        };
                    }
                    catch
                    {
                        // Rollback on any error
                        await transaction.RollbackAsync();
                        throw;
                    }
                }
                finally
                {
                    // 8. Always release the lock (using Lua script to ensure only we release it)
                    await _redisLock.ReleaseLockAsync(lockKey, lockValue);
                }
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<BookingDto>
                {
                    IsSuccess = false,
                    Message = $"An error occurred: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }

        public async Task<ApiResponseDto<BookingDto>> UpdateBookingAsync(Guid bookingId, UpdateBookingDto updateBookingDto)
        {
            try
            {
                var dateValidation = BookingValidator.ValidateBookingDates(updateBookingDto.CheckInTime, updateBookingDto.CheckOutTime);
                if(!dateValidation.IsValid) {
                    return new ApiResponseDto<BookingDto>
                    {
                        IsSuccess = false,
                        Message = dateValidation.ErrorMessage,
                        StatusCode = ApplicationStatusCode.BadRequest
                    };
                }
                var booking = await _context.Bookings.FirstOrDefaultAsync(b => b.Id == bookingId && b.DeletedAt == null);   
                if (booking == null)
                {
                    return new ApiResponseDto<BookingDto>
                    {
                        IsSuccess = false,
                        Message = "Booking not found.",
                        StatusCode = ApplicationStatusCode.NotFound
                    };
                }
                booking.CheckInTime = updateBookingDto.CheckInTime;
                booking.CheckOutTime = updateBookingDto.CheckOutTime;
                _context.Bookings.Update(booking);
                await _context.SaveChangesAsync();

                return new ApiResponseDto<BookingDto>
                {
                    IsSuccess = true,
                    Message = "Booking updated successfully.",
                    StatusCode = ApplicationStatusCode.Success,
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<BookingDto>
                {
                    IsSuccess = false,
                    Message = $"An error occurred: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }
        public async Task<ApiResponseDto<bool>> DeleteBookingAsync(Guid bookingId)
        {
            try
            {
                var booking = await _context.Bookings.FirstOrDefaultAsync(b => b.Id == bookingId && b.DeletedAt == null);
                if (booking == null) {
                    return new ApiResponseDto<bool>
                    {
                        IsSuccess = false,
                        Message = "Booking not found.",
                        StatusCode = ApplicationStatusCode.NotFound,
                        Data = false
                    };
                }
                booking.DeletedAt = DateTime.UtcNow;
                _context.Bookings.Update(booking);
                await _context.SaveChangesAsync();

                return new ApiResponseDto<bool>
                {
                    IsSuccess = true,
                    Message = "Booking deleted successfully.",
                    StatusCode = ApplicationStatusCode.Success,
                    Data = true
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<bool>
                {
                    IsSuccess = false,
                    Message = $"An error occurred: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError,
                    Data = false
                };
            }
        }

        public async Task<ApiResponseDto<List<DailyAvailabilityDto>>> GetRoomAvailabilityAsync(Guid roomId, RoomAvailabilityRequestDto request)
        {
            try
            {
                // 1. Validate date range
                if (request.Start >= request.End)
                {
                    return new ApiResponseDto<List<DailyAvailabilityDto>>
                    {
                        IsSuccess = false,
                        Message = "Start date must be before end date.",
                        StatusCode = ApplicationStatusCode.BadRequest
                    };
                }

                // 2. Check room existence
                if (!await _context.Rooms.AnyAsync(r => r.Id == roomId && r.DeletedAt == null))
                {
                    return new ApiResponseDto<List<DailyAvailabilityDto>>
                    {
                        IsSuccess = false,
                        Message = "Room not found.",
                        StatusCode = ApplicationStatusCode.NotFound
                    };
                }

                // 3. Fetch all overlapping bookings in the date range
                var overlappingBookings = await _bookingRepository.GetOverlappingBookingsAsync(
                    roomId,
                    request.Start,
                    request.End);

                // 4. Generate daily availability
                var dailyAvailability = new List<DailyAvailabilityDto>();
                var currentDate = request.Start.Date;
                var endDate = request.End.Date;

                while (currentDate < endDate)
                {
                    var nextDate = currentDate.AddDays(1);

                    // Check if any booking overlaps with this specific day
                    // A booking overlaps with a day if:
                    // booking.CheckInTime < nextDate AND booking.CheckOutTime > currentDate
                    var isBooked = overlappingBookings.Any(b =>
                        b.CheckInTime < nextDate && b.CheckOutTime > currentDate);

                    dailyAvailability.Add(new DailyAvailabilityDto
                    {
                        Date = currentDate.ToString("yyyy-MM-dd"),
                        Available = !isBooked
                    });

                    currentDate = nextDate;
                }

                return new ApiResponseDto<List<DailyAvailabilityDto>>
                {
                    IsSuccess = true,
                    Data = dailyAvailability,
                    Message = "Availability retrieved successfully.",
                    StatusCode = ApplicationStatusCode.Success
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<List<DailyAvailabilityDto>>
                {
                    IsSuccess = false,
                    Message = $"An error occurred: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }
    }
}
