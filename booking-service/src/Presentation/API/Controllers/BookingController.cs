using Microsoft.AspNetCore.Mvc;
using Application.Interfaces;
using Application.DTOs;

namespace API.Controllers
{
    [Route("api/bookings")]
    [ApiController]
    public class BookingController : ControllerBase
    {
        private readonly IBookingService _bookingService;
        public BookingController(IBookingService bookingService)
        {
            _bookingService = bookingService;
        }
        
        [HttpGet]
        public async Task<ActionResult<ApiResponseDto<PagedResponseDto<BookingDto>>>> GetBookingsAsync([FromQuery] BookingPaginationRequestDto request)
        {
            var response = await _bookingService.GetBookingsAsync(request);
            return StatusCode(response.StatusCode, response);
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<ApiResponseDto<BookingDto>>> GetBookingByIdAsync(Guid id)
        {
            var response = await _bookingService.GetBookingByIdAsync(id);
            return StatusCode(response.StatusCode, response);
        }

        [HttpPost]
        public async Task<ActionResult<ApiResponseDto<BookingDto>>> CreateBookingAsync([FromBody] CreateBookingDto createBookingDto)
        {
            var response = await _bookingService.CreateBookingAsync(createBookingDto);
            return StatusCode(response.StatusCode, response);
        }

        [HttpPatch("{id}")]
        public async Task<ActionResult<ApiResponseDto<BookingDto>>> UpdateBookingAsync(Guid id, [FromBody] UpdateBookingDto updateBookingDto)
        {
            var response = await _bookingService.UpdateBookingAsync(id, updateBookingDto);
            return StatusCode(response.StatusCode, response);
        }

        [HttpDelete("{id}")]
        public async Task<ActionResult<ApiResponseDto<bool>>> DeleteBookingAsync(Guid id)
        {
            var response = await _bookingService.DeleteBookingAsync(id);
            return StatusCode(response.StatusCode, response);
        }
    }
}
