using Application.Constants;
using Application.DTOs;
using Application.Interfaces;
using AutoMapper;
using Domain.Entities;
using Infrashtructure.Data;
using Infrashtructure.Validators;
using Microsoft.EntityFrameworkCore;

namespace Infrashtructure.Services;

public class TypeRoomService : ITypeRoomService
{
    private readonly BookingDbContext _context;
    private readonly IMapper _mapper;
    public TypeRoomService(BookingDbContext context, IMapper mapper)
    {
        _context = context;
        _mapper = mapper;
    }
    public async Task<ApiResponseDto<PagedResponseDto<TypeRoomDto>>> GetTypeRoomsAsync(TypeRoomPaginationRequestDto request)
    {
        try
        {
            var query = _context.TypeRooms.Where(tr => tr.DeletedAt == null).AsQueryable();
            if (!string.IsNullOrEmpty(request.Search))
            {
                var searchItem = request.Search.ToLower();
                query = query.Where(tr => tr.Name.ToLower().Contains(searchItem));
            }
            query = (request.SortBy ?? "CreatedAt").ToLower() switch
            {
                "name" => request.SortDirection == "desc" ? query.OrderByDescending(tr => tr.Name) : query.OrderBy(tr => tr.Name),
                "priceperhour" => request.SortDirection == "desc" ? query.OrderByDescending(tr => tr.PricePerHour) : query.OrderBy(tr => tr.PricePerHour),
                _ => request.SortDirection == "desc" ? query.OrderByDescending(tr => tr.CreatedAt) : query.OrderBy(tr => tr.CreatedAt),
            };
            
            var total = await query.CountAsync();
            var totalPages = (int)Math.Ceiling(total / (double)request.PageSize);
            
            var typeRooms = await query
                .Skip((request.PageNumber - 1) * request.PageSize)
                .Take(request.PageSize)
                .ToListAsync();
            var typeRoomDtos = _mapper.Map<List<TypeRoomDto>>(typeRooms);
            var pagedResponse = new PagedResponseDto<TypeRoomDto>
            {
                Items = typeRoomDtos,
                Paging =
                {
                    Total = total,
                    PageNumber = request.PageNumber,
                    PageSize = request.PageSize,
                    TotalPages = totalPages,
                    HasPrevious = request.PageNumber > 1,
                    HasNext = request.PageNumber < totalPages,
                }
            };
            return new ApiResponseDto<PagedResponseDto<TypeRoomDto>>
            {
                IsSuccess = true,
                Data = pagedResponse,
                Message = "Type rooms retrieved successfully.",
                StatusCode = ApplicationStatusCode.Success,
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<PagedResponseDto<TypeRoomDto>>
            {
                IsSuccess = false,
                Data = null,
                Message = "An error occurred while processing your request.",
                StatusCode = ApplicationStatusCode.InternalServerError,
            };
        }
    }
    
    public async Task<ApiResponseDto<TypeRoomDto>> GetTypeRoomByIdAsync(Guid id)
    {
        try
        {
            var typeRoom = await _context.TypeRooms.FirstOrDefaultAsync(tr => tr.Id == id && tr.DeletedAt == null);
            if (typeRoom == null)
            {
                return new ApiResponseDto<TypeRoomDto>
                {
                    IsSuccess = false,
                    Data = null,
                    Message = "Type room not found.",
                    StatusCode = ApplicationStatusCode.NotFound,
                };
            }

            return new ApiResponseDto<TypeRoomDto>
            {
                IsSuccess = true,
                Data = _mapper.Map<TypeRoomDto>(typeRoom),
                Message = "Type room retrieved successfully.",
                StatusCode = ApplicationStatusCode.Success,
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<TypeRoomDto>
            {
                IsSuccess = false,
                Data = null,
                Message = "An error occurred while processing your request.",
                StatusCode = ApplicationStatusCode.InternalServerError,
            };
        }
    }
    
    public async Task<ApiResponseDto<TypeRoomDto>> CreateTypeRoomAsync(CreateTypeRoomDto createTypeRoomDto)
    {
        try
        {
            var nameValidation = TypeRoomValidator.ValidateName(createTypeRoomDto.Name);
            if (!nameValidation.IsValid)
            {
                return new ApiResponseDto<TypeRoomDto>
                {
                    IsSuccess = false,
                    Data = null,
                    Message = nameValidation.ErrorMessage,
                    StatusCode = ApplicationStatusCode.BadRequest,
                };
            }
            var priceValidation = TypeRoomValidator.ValidatePricePerHour(createTypeRoomDto.PricePerHour);
            if (!priceValidation.IsValid)
            {
                return new ApiResponseDto<TypeRoomDto>
                {                    
                    IsSuccess = false,
                    Data = null,
                    Message = priceValidation.ErrorMessage,
                    StatusCode = ApplicationStatusCode.BadRequest,
                };
            }
            var typeRoom = _mapper.Map<TypeRoom>(createTypeRoomDto);
            _context.TypeRooms.Add(typeRoom);
            await _context.SaveChangesAsync();
            
            return new ApiResponseDto<TypeRoomDto>
            {
                IsSuccess = true,
                Data = _mapper.Map<TypeRoomDto>(typeRoom),
                Message = "Type room created successfully.",
                StatusCode = ApplicationStatusCode.Success,
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<TypeRoomDto>
            {
                IsSuccess = false,
                Data = null,
                Message = "An error occurred while processing your request."+ ex.Message,
                StatusCode = ApplicationStatusCode.InternalServerError,
            };
        }
    }
    
    public async Task<ApiResponseDto<TypeRoomDto>> UpdateTypeRoomAsync(Guid id, UpdateTypeRoomDto updateTypeRoomDto)
    {
        try
        {
            var nameValidation = TypeRoomValidator.ValidateName(updateTypeRoomDto.Name);
            if (!nameValidation.IsValid)
            {
                return new ApiResponseDto<TypeRoomDto>
                {
                    IsSuccess = false,
                    Data = null,
                    Message = nameValidation.ErrorMessage,
                    StatusCode = ApplicationStatusCode.BadRequest,
                };
            }
            var priceValidation = TypeRoomValidator.ValidatePricePerHour(updateTypeRoomDto.PricePerHour);
            if (!priceValidation.IsValid)
            {
                return new ApiResponseDto<TypeRoomDto>
                {                    
                    IsSuccess = false,
                    Data = null,
                    Message = priceValidation.ErrorMessage,
                    StatusCode = ApplicationStatusCode.BadRequest,
                };
            }
            // check if type room exists
            var typeRoom = await _context.TypeRooms.FirstOrDefaultAsync(tr => tr.Id == id && tr.DeletedAt == null);
            if (typeRoom == null)
            {
                return new ApiResponseDto<TypeRoomDto>
                {
                    IsSuccess = false,
                    Data = null,
                    Message = "Type room not found.",
                    StatusCode = ApplicationStatusCode.NotFound,
                };
            }
            typeRoom.Name = updateTypeRoomDto.Name;
            typeRoom.PricePerHour = updateTypeRoomDto.PricePerHour;
            _context.TypeRooms.Update(typeRoom);
            await _context.SaveChangesAsync();
            return new ApiResponseDto<TypeRoomDto>
            {
                IsSuccess = true,
                Data = _mapper.Map<TypeRoomDto>(typeRoom),
                Message = "Type room updated successfully.",
                StatusCode = ApplicationStatusCode.Success,
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<TypeRoomDto>
            {
                IsSuccess = false,
                Data = null,
                Message = "An error occurred while processing your request.",
                StatusCode = ApplicationStatusCode.InternalServerError,
            };
        }
    }
    
    public async Task<ApiResponseDto<bool>> DeleteTypeRoomAsync(Guid id)
    {
        try
        {
            var typeRoom = await _context.TypeRooms.FirstOrDefaultAsync(tr => tr.Id == id && tr.DeletedAt == null);
            if (typeRoom == null)
            {
                return new ApiResponseDto<bool>
                {
                    IsSuccess = false,
                    Data = false,
                    Message = "Type room not found.",
                    StatusCode = ApplicationStatusCode.NotFound,
                };
            }
            typeRoom.DeletedAt = DateTime.UtcNow;
            _context.TypeRooms.Update(typeRoom);
            await _context.SaveChangesAsync();
            return new ApiResponseDto<bool>
            {
                IsSuccess = true,
                Data = true,
                Message = "Type room deleted successfully.",
                StatusCode = ApplicationStatusCode.Success,
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<bool>
            {
                IsSuccess = false,
                Data = false,
                Message = "An error occurred while processing your request.",
                StatusCode = ApplicationStatusCode.InternalServerError,
            };
        }
    }
}