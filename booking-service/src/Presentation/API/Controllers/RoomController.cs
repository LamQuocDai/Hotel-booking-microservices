using Application.DTOs;
using Application.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers;

[ApiController]
[Route("api/rooms")]
public class RoomController : ControllerBase
{
    private readonly IRoomService _roomService;
    private readonly IBookingService _bookingService;

    public RoomController(IRoomService roomService, IBookingService bookingService)
    {
        _roomService = roomService;
        _bookingService = bookingService;
    }
    [HttpGet]
    public async Task<ActionResult<ApiResponseDto<PagedResponseDto<RoomDto>>>> GetRoomAsync([FromQuery] RoomPaginationRequestDto paginationRequestDto)
    {
        var response = await _roomService.GetRoomsAsync(paginationRequestDto);
        return StatusCode(response.StatusCode, response);
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<ApiResponseDto<RoomDto>>> GetRoomByIdAsync(Guid id)
    {
        var response = await _roomService.GetRoomByIdAsync(id);
        return StatusCode(response.StatusCode, response);
    }

    [HttpPost]
    public async Task<ActionResult<ApiResponseDto<RoomDto>>> CreateRoomAsync([FromBody] CreateRoomDto createRoomDto)
    {
        var response = await _roomService.CreateRoomAsync(createRoomDto);
        return StatusCode(response.StatusCode, response);
    }

    [HttpPatch("{id}")]
    public async Task<ActionResult<ApiResponseDto<RoomDto>>> UpdateRoomAsync(Guid id,
        [FromBody] UpdateRoomDto updateRoomDto)
    {
        var response = await _roomService.UpdateRoomAsync(id, updateRoomDto);
        return StatusCode(response.StatusCode, response);
    }

    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiResponseDto<bool>>> DeleteRoomAsync(Guid id)
    {
        var response = await _roomService.DeleteRoomAsync(id);
        return StatusCode(response.StatusCode, response);
    }

    /// <summary>
    /// Get daily availability for a room within a date range.
    /// Returns an array of dates with availability status.
    /// </summary>
    /// <param name="id">Room ID</param>
    /// <param name="start">Start date (inclusive) in yyyy-MM-dd format</param>
    /// <param name="end">End date (exclusive) in yyyy-MM-dd format</param>
    /// <returns>Array of daily availability</returns>
    [HttpGet("{id}/availability")]
    public async Task<ActionResult<ApiResponseDto<List<DailyAvailabilityDto>>>> GetRoomAvailabilityAsync(
        Guid id,
        [FromQuery] DateTime start,
        [FromQuery] DateTime end)
    {
        var request = new RoomAvailabilityRequestDto
        {
            Start = start,
            End = end
        };

        var response = await _bookingService.GetRoomAvailabilityAsync(id, request);
        return StatusCode(response.StatusCode, response);
    }
}