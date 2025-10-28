using Application.Constants;
using Application.DTOs;
using Application.Interfaces;
using AutoMapper;
using Domain.Entities;
using Infrashtructure.Data;
using Infrashtructure.Validators;
using Microsoft.EntityFrameworkCore;

namespace Infrashtructure.Services;

public class RoomService : IRoomService
{
    private readonly BookingDbContext _context;
    private readonly IMapper _mapper;
    public RoomService(BookingDbContext context, IMapper mapper)
    {
        _context = context;
        _mapper = mapper;
    }

    public async Task<ApiResponseDto<PagedResponseDto<RoomDto>>> GetRoomsAsync(RoomPaginationRequestDto request)
    {
        try
        {
            var query = _context.Rooms.Where(r => r.DeletedAt == null).AsQueryable();
            if (!string.IsNullOrEmpty(request.Search))
            {
                var searchItem = request.Search.ToLower();
                query = query.Where(r => r.Name.ToLower().Contains(searchItem));
            }
            if (request.TypeRoomId.HasValue)
            {
                query = query.Where(r => r.TypeRoomId == request.TypeRoomId.Value);
            }
            if (request.LocationId.HasValue)
            {
                query = query.Where(r => r.LocationId == request.LocationId.Value);
            }
            // sort by name
            query = (request.SortBy ?? "CreatedAt").ToLower() switch
            {
                "name" => request.SortDirection == "desc" ? query.OrderByDescending(r => r.Name) : query.OrderBy(r => r.Name),
                _ => request.SortDirection == "desc" ? query.OrderByDescending(r => r.CreatedAt) : query.OrderBy(r => r.CreatedAt),
            };
            
            var total = await query.CountAsync();
            var totalPages = (int)Math.Ceiling(total / (double)request.PageSize);
            var rooms = await query
                .Skip((request.PageNumber - 1) * request.PageSize)
                .Take(request.PageSize)
                .ToListAsync();
            var roomDtos = _mapper.Map<List<RoomDto>>(rooms);
            var pagedResponse = new PagedResponseDto<RoomDto>
            {
                Items = roomDtos,
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
            return new ApiResponseDto<PagedResponseDto<RoomDto>>
            {
                IsSuccess = true,
                Data = pagedResponse,
                Message = "Rooms retrieved successfully.",
                StatusCode = ApplicationStatusCode.Success
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<PagedResponseDto<RoomDto>>
            {
                IsSuccess = false,
                Message = $"An error occurred while retrieving rooms: {ex.Message}",
                StatusCode = ApplicationStatusCode.InternalServerError
            };
        }
    }
    
    public async Task<ApiResponseDto<RoomDto>> GetRoomByIdAsync(Guid roomId)
    {
        try
        {
            var room = await _context.Rooms.FirstOrDefaultAsync(r => r.Id == roomId && r.DeletedAt == null);
            if (room == null)
            {
                return new ApiResponseDto<RoomDto>
                {
                    IsSuccess = false,
                    Message = "Room not found.",
                    StatusCode = ApplicationStatusCode.NotFound
                };
            }

            return new ApiResponseDto<RoomDto>
            {
                IsSuccess = true,
                Data = _mapper.Map<RoomDto>(room),
                Message = "Room retrieved successfully.",
                StatusCode = ApplicationStatusCode.Success
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<RoomDto>
            {
                IsSuccess = false,
                Message = $"An error occurred while retrieving the room: {ex.Message}",
                StatusCode = ApplicationStatusCode.InternalServerError
            };
        }
    }
    public async Task<ApiResponseDto<RoomDto>> CreateRoomAsync(CreateRoomDto createRoomDto)
    {
        try
        {
            var nameValidation = RoomValidator.ValidateRoomName(createRoomDto.Name);
            if (!nameValidation.IsValid)
            {
                return new ApiResponseDto<RoomDto>
                {
                    IsSuccess = false,
                    Message = nameValidation.ErrorMessage,
                    StatusCode = ApplicationStatusCode.BadRequest
                };
            }
            var typeRoomIdValidation = RoomValidator.ValidateTypeRoomId(createRoomDto.TypeRoomId);
            if (!typeRoomIdValidation.IsValid)
            {
                return new ApiResponseDto<RoomDto>
                {                    
                    IsSuccess = false,
                    Message = typeRoomIdValidation.ErrorMessage,
                    StatusCode = ApplicationStatusCode.BadRequest
                };
            }
            var locationIdValidation = RoomValidator.ValidateLocationId(createRoomDto.LocationId);
            if (!locationIdValidation.IsValid)
            {
                return new ApiResponseDto<RoomDto>
                {
                    IsSuccess = false,
                    Message = typeRoomIdValidation.ErrorMessage,
                    StatusCode = ApplicationStatusCode.BadRequest
                };
            }
            var room = _mapper.Map<Room>(createRoomDto);
            _context.Rooms.Add(room);
            await _context.SaveChangesAsync();
            
            return new ApiResponseDto<RoomDto>
            {
                IsSuccess = true,
                Data = _mapper.Map<RoomDto>(room),
                Message = "Room created successfully.",
                StatusCode = ApplicationStatusCode.Created
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<RoomDto>
            {
                IsSuccess = false,
                Message = $"An error occurred while creating the room: {ex.Message}",
                StatusCode = ApplicationStatusCode.InternalServerError
            };
        }
    }
    
    public async Task<ApiResponseDto<RoomDto>> UpdateRoomAsync(Guid roomId, UpdateRoomDto updateRoomDto)
    {
        try
        {
            var nameValidation = RoomValidator.ValidateRoomName(updateRoomDto.Name);
            if (!nameValidation.IsValid)
            {
                return new ApiResponseDto<RoomDto>
                {
                    IsSuccess = false,
                    Message = nameValidation.ErrorMessage,
                    StatusCode = ApplicationStatusCode.BadRequest
                };
            }
            var typeRoomIdValidation = RoomValidator.ValidateTypeRoomId(updateRoomDto.TypeRoomId);
            if (!typeRoomIdValidation.IsValid)
            {
                return new ApiResponseDto<RoomDto>
                {                    
                    IsSuccess = false,
                    Message = typeRoomIdValidation.ErrorMessage,
                    StatusCode = ApplicationStatusCode.BadRequest
                };
            }
            var locationIdValidation = RoomValidator.ValidateLocationId(updateRoomDto.LocationId);
            if (!locationIdValidation.IsValid)
            {
                return new ApiResponseDto<RoomDto>
                {
                    IsSuccess = false,
                    Message = locationIdValidation.ErrorMessage,
                    StatusCode = ApplicationStatusCode.BadRequest
                };
            }  
            var room = await _context.Rooms.FirstOrDefaultAsync(r => r.Id == roomId && r.DeletedAt == null);
            if (room == null)
            {
                return new ApiResponseDto<RoomDto>
                {
                    IsSuccess = false,
                    Message = "Room not found.",
                    StatusCode = ApplicationStatusCode.NotFound
                };
            }
            room.Name = updateRoomDto.Name;
            room.TypeRoomId = updateRoomDto.TypeRoomId;
            room.LocationId = updateRoomDto.LocationId;
            _context.Rooms.Update(room);
            await _context.SaveChangesAsync();
            return new ApiResponseDto<RoomDto>
            {
                IsSuccess = true,
                Data = _mapper.Map<RoomDto>(room),
                Message = "Room updated successfully.",
                StatusCode = ApplicationStatusCode.Success
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<RoomDto>
            {
                IsSuccess = false,
                Message = $"An error occurred while updating the room: {ex.Message}",
                StatusCode = ApplicationStatusCode.InternalServerError
            };
        }
    }
    
    public async Task<ApiResponseDto<bool>> DeleteRoomAsync(Guid roomId)
    {
        try
        {
            var room = await _context.Rooms.FirstOrDefaultAsync(r => r.Id == roomId && r.DeletedAt == null);
            if (room == null)
            {
                return new ApiResponseDto<bool>
                {
                    IsSuccess = false,
                    Message = "Room not found.",
                    StatusCode = ApplicationStatusCode.NotFound
                };
            }
            room.DeletedAt = DateTime.UtcNow;
            _context.Rooms.Update(room);
            await _context.SaveChangesAsync();
            return new ApiResponseDto<bool>
            {
                IsSuccess = true,
                Data = true,
                Message = "Room deleted successfully.",
                StatusCode = ApplicationStatusCode.Success
            };
        }
        catch (Exception ex)
        {
            return new ApiResponseDto<bool>
            {
                IsSuccess = false,
                Message = $"An error occurred while deleting the room: {ex.Message}",
                StatusCode = ApplicationStatusCode.InternalServerError
            };
        }
    }
}