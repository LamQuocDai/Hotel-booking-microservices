using Application.Constants;
using Application.DTOs;
using Application.Interfaces;
using AutoMapper;
using Infrashtructure.Data;

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

                return new ApiResponseDto<PagedResponseDto<BookingDto>>
                {
                    IsSuccess = true,
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
                return new ApiResponseDto<BookingDto>
                {
                    IsSuccess = true,
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
                return new ApiResponseDto<BookingDto>
                {
                    IsSuccess = true,
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
