using Application.DTOs;

namespace Application.Interfaces;

public interface ILocationService
{
    Task<ApiResponseDto<PagedResponseDto<LocationDto>>> GetAllLocationsAsync(LocationPaginationRequestDto request);
}