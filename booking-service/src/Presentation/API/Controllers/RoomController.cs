using Application.DTOs;
using Application.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers;

[ApiController]
[Route("api/rooms")]
public class RoomController : ControllerBase
{
    private readonly IRoomService _roomService;
    public RoomController(IRoomService roomService)
    {
        _roomService = roomService;
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
}