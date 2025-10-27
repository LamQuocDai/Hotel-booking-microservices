using Application.Interfaces;
using Infrashtructure.Data;
using Infrashtructure.Services;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace Infrashtructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        services.AddDbContext<BookingDbContext>(options =>
        {
            var connectionString = configuration.GetConnectionString("DefaultConnection");
            if(string.IsNullOrEmpty(connectionString))
            {
                throw new InvalidOperationException("Connection string 'DefaultConnection' not found.");
            }
            options.UseNpgsql(connectionString);
        });

        services.AddScoped<ILocationService, LocationService>();
        services.AddScoped<ITypeRoomService, TypeRoomService>();

        return services;
    }
}