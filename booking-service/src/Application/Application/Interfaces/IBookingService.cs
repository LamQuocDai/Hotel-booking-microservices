using Application.DTOs;

namespace Application.Interfaces
{
    public interface IBookingService
    {
        Task<ApiResponseDto<PagedResponseDto<BookingDto>>> GetBookingsAsync(BookingPaginationRequestDto request);
        Task<ApiResponseDto<BookingDto>> GetBookingByIdAsync(Guid id);
        Task<ApiResponseDto<BookingDto>> CreateBookingAsync(CreateBookingDto createBookingDto);
        Task<ApiResponseDto<BookingDto>> UpdateBookingAsync(Guid id, UpdateBookingDto updateBookingDto);
        Task<ApiResponseDto<bool>> DeleteBookingAsync(Guid id);
    }
}
