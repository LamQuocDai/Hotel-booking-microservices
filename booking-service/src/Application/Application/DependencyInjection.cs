using Application.Mappings;
using Microsoft.Extensions.DependencyInjection;

namespace Application;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        // AutoMapper Configuration
        services.AddAutoMapper(typeof(MappingProfiles));

        return services;
    }
}