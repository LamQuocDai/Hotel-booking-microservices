using Application.DTOs;
using Application.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers;

[ApiController]
[Route("api/type-rooms")]
public class TypeRoomController : ControllerBase
{
    private readonly ITypeRoomService _typeRoomService;

    public TypeRoomController(ITypeRoomService typeRoomService)
    {
        _typeRoomService = typeRoomService;
    }
    
    [HttpGet]   
    public async Task<ActionResult<ApiResponseDto<PagedResponseDto<TypeRoomDto>>>> GetTypeRooms([FromQuery] TypeRoomPaginationRequestDto request)
    {
        var response = await _typeRoomService.GetTypeRoomsAsync(request);
        return StatusCode(response.StatusCode, response);
    }
    
    [HttpGet("{id}")]
    public async Task<ActionResult<ApiResponseDto<TypeRoomDto>>> GetTypeRoomById(Guid id)
    {
        var response = await _typeRoomService.GetTypeRoomByIdAsync(id);
        return StatusCode(response.StatusCode, response);
    }

    [HttpPost]
    public async Task<ActionResult<ApiResponseDto<TypeRoomDto>>> CreateTypeRoom(
        [FromBody] CreateTypeRoomDto createTypeRoomDto)
    {
        var response = await _typeRoomService.CreateTypeRoomAsync(createTypeRoomDto);
        return StatusCode(response.StatusCode, response);
    }

    [HttpPatch("{id}")]
    public async Task<ActionResult<ApiResponseDto<TypeRoomDto>>> UpdateTypeRoom(
        Guid id,
        [FromBody] UpdateTypeRoomDto updateTypeRoomDto)
    {
        var response = await _typeRoomService.UpdateTypeRoomAsync(id, updateTypeRoomDto);
        return StatusCode(response.StatusCode, response);
    }

    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiResponseDto<bool>>> DeleteTypeRoom(Guid id)
    {
        var response = await _typeRoomService.DeleteTypeRoomAsync(id);
        return StatusCode(response.StatusCode, response);
    }
}