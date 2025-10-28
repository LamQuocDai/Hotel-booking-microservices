using Application.DTOs;
using Domain.Entities;
using AutoMapper;

namespace Application.Mappings;

public class MappingProfiles : Profile
{
    public MappingProfiles()   
    {
        // CreateMap<Source, Destination>();
        CreateMap<Location, LocationDto>();
        CreateMap<CreateLocationDto, Location>().ForMember(dest => dest.CreatedAt, opt => opt.MapFrom(_ => DateTime.UtcNow));
        
        // TypeRoom Mappings
        CreateMap<TypeRoom, TypeRoomDto>();
        CreateMap<CreateTypeRoomDto, TypeRoom>().ForMember(dest => dest.CreatedAt, opt => opt.MapFrom(_ => DateTime.UtcNow));
        
        // Room Mappings
        CreateMap<Room, RoomDto>();
        CreateMap<CreateRoomDto, Room>().ForMember(dest => dest.CreatedAt, opt => opt.MapFrom(_ => DateTime.UtcNow));
        
    }
}