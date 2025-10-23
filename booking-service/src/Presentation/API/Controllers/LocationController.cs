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
}