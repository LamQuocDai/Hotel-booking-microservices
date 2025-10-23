using Application.DTOs;
using Application.Interfaces;
using Infrashtructure.Data;
using AutoMapper;
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
    
    public async Task<ApiResponseDto<PagedResponseDto<LocationDto>>>  GetAllLocationsAsync(LocationPaginationRequestDto request) 
    {
        try
        {
            var query = _context.Locations.AsQueryable();
            
            // Check search
            if (!string.IsNullOrEmpty(request.Search))
            {
                var searchItem = request.Search.ToLower();
                query = query.Where(l => l.Name.Contains(request.Search) || l.Address.Contains(request.Search));
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
                StatusCode = 200
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<PagedResponseDto<LocationDto>>
            {
                IsSuccess = false,
                Data = null,
                Message = $"An error occurred: {ex.Message}",
                StatusCode = 500
            };
        }
    }
}