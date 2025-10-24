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
    }
}