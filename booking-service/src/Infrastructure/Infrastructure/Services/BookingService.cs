using Application.Constants;
using Application.DTOs;
using Application.Interfaces;
using AutoMapper;
using Infrashtructure.Data;
using Microsoft.EntityFrameworkCore;
using Infrashtructure.Validators;
using Domain.Entities;
using Domain.Enums;

namespace Infrashtructure.Services
{
    public class BookingService : IBookingService
    {
        private readonly BookingDbContext _context;
        private readonly IMapper _mapper;
        public BookingService(BookingDbContext context, IMapper mapper)
        {
            _context = context;
            _mapper = mapper;
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
                // check room existence
                if (!await _context.Rooms.AnyAsync(r => r.Id == createBookingDto.RoomId && r.DeletedAt == null))
                {
                    return new ApiResponseDto<BookingDto>
                    {
                        IsSuccess = false,
                        Message = "Room does not exist.",
                        StatusCode = ApplicationStatusCode.BadRequest
                    };
                }
                // Set default status
                var booking = _mapper.Map<Booking>(createBookingDto);
                booking.Status = BookingStatus.Holding;
                _context.Bookings.Add(booking);
                await _context.SaveChangesAsync();

                return new ApiResponseDto<BookingDto>
                {
                    IsSuccess = true,
                    Data = _mapper.Map<BookingDto>(booking),
                    Message = "Booking created successfully.",
                    StatusCode = ApplicationStatusCode.Created,
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

        public async Task<ApiResponseDto<BookingDto>> UpdateBookingAsync(Guid bookingId, UpdateBookingDto updateBookingDto)
        {
            try
            {
                var dateValidation = BookingValidator.ValidateBookingDates(updateBookingDto.CheckInTime, updateBookingDto.CheckOutTime);
                if(dateValidation.IsValid) {
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
    }
}
