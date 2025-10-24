using Application.DTOs;

namespace Application.Interfaces;

public interface ITypeRoomService
{
    Task<ApiResponseDto<PagedResponseDto<TypeRoomDto>>> GetTypeRoomsAsync(TypeRoomPaginationRequestDto request);
    Task<ApiResponseDto<TypeRoomDto>> GetTypeRoomByIdAsync(Guid id);
    Task<ApiResponseDto<TypeRoomDto>> CreateTypeRoomAsync(CreateTypeRoomDto createTypeRoomDto);
    Task<ApiResponseDto<TypeRoomDto>> UpdateTypeRoomAsync(Guid id, UpdateTypeRoomDto updateTypeRoomDto);
    Task<ApiResponseDto<bool>> DeleteTypeRoomAsync(Guid id);
}