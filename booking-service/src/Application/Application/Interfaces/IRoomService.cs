using Application.DTOs;

namespace Application.Interfaces;

public interface IRoomService
{
    Task<ApiResponseDto<PagedResponseDto<RoomDto>>> GetRoomsAsync(RoomPaginationRequestDto paginationRequestDto);
    Task<ApiResponseDto<RoomDto>> GetRoomByIdAsync(Guid id);
    Task<ApiResponseDto<RoomDto>> CreateRoomAsync(CreateRoomDto createRoomDto);
    Task<ApiResponseDto<RoomDto>> UpdateRoomAsync(Guid id,UpdateRoomDto updateRoomDto);
    Task<ApiResponseDto<bool>> DeleteRoomAsync(Guid id);
}