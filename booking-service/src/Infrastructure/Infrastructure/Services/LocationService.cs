using Application.Constants;
using Application.DTOs;
using Application.Interfaces;
using Infrashtructure.Data;
using AutoMapper;
using Domain.Entities;
using Infrashtructure.Validators;
using Microsoft.EntityFrameworkCore;

namespace Infrashtructure.Services;

public class LocationService : ILocationService
{
    private readonly BookingDbContext _context;
    private readonly IMapper _mapper;
    
    public LocationService(BookingDbContext context, IMapper mapper)
    {
        _context = context;
        _mapper = mapper;
    }
    
    public async Task<ApiResponseDto<PagedResponseDto<LocationDto>>>  GetLocationsAsync(LocationPaginationRequestDto request) 
    {
        try
        {
            var query = _context.Locations.Where(l => l.DeletedAt == null).AsQueryable();
            
            // Check search
            if (!string.IsNullOrEmpty(request.Search))
            {
                var searchItem = request.Search.ToLower();
                query = query.Where(l => l.Name.ToLower().Contains(searchItem) || l.Address.ToLower().Contains(searchItem));
            }
            // sort by name
            query = (request.SortBy ?? "CreatedAt").ToLower() switch
            {
                "name" => request.SortDirection == "desc" ? query.OrderByDescending(l => l.Name) : query.OrderBy(l => l.Name),
                _ => request.SortDirection == "desc" ? query.OrderByDescending(l => l.CreatedAt) : query.OrderBy(l => l.CreatedAt),
            };
            
            var total = await query.CountAsync();
            var totalPages = (int)Math.Ceiling(total / (double)request.PageSize);
            
            var locations = await query
                .Skip((request.PageNumber - 1) * request.PageSize)
                .Take(request.PageSize)
                .ToListAsync();
            
            var locationDtos = _mapper.Map<List<LocationDto>>(locations);
            var pagedResponse = new PagedResponseDto<LocationDto>
            {
                Items = locationDtos,
                Paging =
                {
                    Total = total,
                    PageNumber = request.PageNumber,
                    PageSize = request.PageSize,
                    TotalPages = totalPages,
                    HasPrevious = request.PageNumber > 1,
                    HasNext = request.PageNumber < totalPages
                }
            };
            return new ApiResponseDto<PagedResponseDto<LocationDto>>
            {
                IsSuccess = true,
                Data = pagedResponse,
                Message = "Locations retrieved successfully",
                StatusCode = ApplicationStatusCode.Success
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<PagedResponseDto<LocationDto>>
            {
                IsSuccess = false,
                Data = null,
                Message = $"An error occurred: {ex.Message}",
                StatusCode = ApplicationStatusCode.InternalServerError
            };
        }
    }
    
    public async Task<ApiResponseDto<LocationDto>> GetLocationByIdAsync(Guid id)
    {
        try
        {
            // Find location by id and deletedAt is null
            var location = await _context.Locations
                .FirstOrDefaultAsync(l => l.Id == id && l.DeletedAt == null);
            
            if (location == null)
            {
                return new ApiResponseDto<LocationDto>
                {
                    IsSuccess = false,
                    Data = null,
                    Message = "Location not found",
                    StatusCode = ApplicationStatusCode.NotFound
                };
            }
            var locationDto = _mapper.Map<LocationDto>(location);
            return new ApiResponseDto<LocationDto>
            {
                IsSuccess = true,
                Data = locationDto,
                Message = "Location retrieved successfully",
                StatusCode = ApplicationStatusCode.Success
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<LocationDto>
            {
                IsSuccess = false,
                Data = null,
                Message = $"An error occurred: {ex.Message}",
                StatusCode = ApplicationStatusCode.InternalServerError
            };
        }
    }
    
    public async Task<ApiResponseDto<LocationDto>> CreateLocationAsync(CreateLocationDto createLocationDto)
    {
        var nameValodation = LocationValidator.ValidateName(createLocationDto.Name);
        if (!nameValodation.IsValid)
        {
            return new ApiResponseDto<LocationDto>
            {
                IsSuccess = false,
                Data = null,
                Message = nameValodation.ErrorMessage,
                StatusCode = ApplicationStatusCode.BadRequest
            };
        }
        var addressValidation = LocationValidator.ValidateAddress(createLocationDto.Address);
        if (!addressValidation.IsValid)
        {
            return new ApiResponseDto<LocationDto>()
            {
                IsSuccess = false,
                Data = null,
                Message = addressValidation.ErrorMessage,
                StatusCode = ApplicationStatusCode.BadRequest
            };
        }
        try
        {
            var location = _mapper.Map<Location>(createLocationDto);
            _context.Locations.Add(location);
            await _context.SaveChangesAsync();
            
            return new ApiResponseDto<LocationDto>
            {
                IsSuccess = true,
                Data = _mapper.Map<LocationDto>(location),
                Message = "Location created successfully",
                StatusCode = ApplicationStatusCode.Created
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<LocationDto>
            {
                IsSuccess = false,
                Data = null,
                Message = $"An error occurred: {ex.Message}",
                StatusCode = ApplicationStatusCode.InternalServerError
            };
        }
    }

    public async Task<ApiResponseDto<LocationDto>> UpdateLocationAsync(Guid id, UpdateLocationDto updateLocationDto)
    {
        var nameValodation = LocationValidator.ValidateName(updateLocationDto.Name);
        if (!nameValodation.IsValid)
        {
            return new ApiResponseDto<LocationDto>
            {
                IsSuccess = false,
                Data = null,
                Message = nameValodation.ErrorMessage,
                StatusCode = ApplicationStatusCode.BadRequest
            };
        }
        var addressValidation = LocationValidator.ValidateAddress(updateLocationDto.Address);
        if (!addressValidation.IsValid)
        {
            return new ApiResponseDto<LocationDto>()
            {
                IsSuccess = false,
                Data = null,
                Message = addressValidation.ErrorMessage,
                StatusCode = ApplicationStatusCode.BadRequest
            };
        }
        try
        {
            var location = await _context.Locations.FirstOrDefaultAsync(tr => tr.Id == id && tr.DeletedAt == null);
            if (location == null)
            {
                return new ApiResponseDto<LocationDto>
                {
                    IsSuccess = false,
                    Data = null,
                    Message = "Location not found",
                    StatusCode = ApplicationStatusCode.NotFound
                };
            }
            location.Name = updateLocationDto.Name;
            location.Address = updateLocationDto.Address;
            _context.Locations.Update(location);
            await _context.SaveChangesAsync();
            
            return new ApiResponseDto<LocationDto>
            {
                IsSuccess = true,
                Data = _mapper.Map<LocationDto>(location),
                Message = "Location updated successfully",
                StatusCode = ApplicationStatusCode.Success
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<LocationDto>
            {
                IsSuccess = false,
                Data = null,
                Message = $"An error occurred: {ex.Message}",
                StatusCode = ApplicationStatusCode.InternalServerError
            };
        }
    }
    public async Task<ApiResponseDto<bool>> DeleteLocationAsync(Guid id)
    {
        try
        {
            var location = await _context.Locations.FindAsync(id);
            if (location == null)
            {
                return new ApiResponseDto<bool>
                {
                    IsSuccess = false,
                    Data = false,
                    Message = "Location not found",
                    StatusCode = ApplicationStatusCode.NotFound
                };
            }
            location.DeletedAt = DateTime.UtcNow;
            _context.Locations.Update(location);
            await _context.SaveChangesAsync();
            
            return new ApiResponseDto<bool>
            {
                IsSuccess = true,
                Data = true,
                Message = "Location deleted successfully",
                StatusCode = ApplicationStatusCode.Success
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<bool>
            {
                IsSuccess = false,
                Data = false,
                Message = $"An error occurred: {ex.Message}",
                StatusCode = ApplicationStatusCode.InternalServerError
            };
        }
    }
}