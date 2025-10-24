using Application.DTOs;
using Application.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers;

[ApiController]
[Route("api/locations")]
public class LocationController : ControllerBase
{
    private readonly ILocationService _locationService;
    public LocationController(ILocationService locationService)
    {
        _locationService = locationService;
    }

    [HttpGet]
    public async Task<ActionResult<ApiResponseDto<List<LocationDto>>>> GetLocations([FromQuery] LocationPaginationRequestDto request)
    {
        var response = await _locationService.GetAllLocationsAsync(request);
        return StatusCode(response.StatusCode, response);
    }
    
    [HttpGet("{id}")]
    public async Task<ActionResult<ApiResponseDto<LocationDto>>> GetLocationById(Guid id)
    {
        var response = await _locationService.GetLocationByIdAsync(id);
        return StatusCode(response.StatusCode, response);
    }

    [HttpPost]
    public async Task<ActionResult<ApiResponseDto<LocationDto>>> CreateLocation(
        [FromBody] CreateLocationDto createLocationDto)
    {
        var response = await _locationService.CreateLocationAsync(createLocationDto);
        return StatusCode(response.StatusCode, response);
    }

    [HttpPatch("{id}")]
    public async Task<ActionResult<ApiResponseDto<LocationDto>>> UpdateLocation(
        Guid id, 
        [FromBody] UpdateLocationDto updateLocationDto)
    {
        var response = await _locationService.UpdateLocationAsync(id, updateLocationDto);
        return StatusCode(response.StatusCode, response);
    }

    [HttpDelete("{id}")]
    public async Task<ActionResult<ApiResponseDto<bool>>> DeleteLocation(Guid id)
    {
        var response = await _locationService.DeleteLocationAsync(id);
        return StatusCode(response.StatusCode, response);
    }
}