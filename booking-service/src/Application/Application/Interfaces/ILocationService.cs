using Application.DTOs;

namespace Application.Interfaces;

public interface ILocationService
{
    Task<ApiResponseDto<PagedResponseDto<LocationDto>>> GetAllLocationsAsync(LocationPaginationRequestDto request);
    Task<ApiResponseDto<LocationDto>> GetLocationByIdAsync(Guid id);
    Task<ApiResponseDto<LocationDto>> CreateLocationAsync(CreateLocationDto createLocationDto);
    Task<ApiResponseDto<LocationDto>> UpdateLocationAsync(Guid id, UpdateLocationDto updateLocationDto);
    Task<ApiResponseDto<bool>> DeleteLocationAsync(Guid id);
}