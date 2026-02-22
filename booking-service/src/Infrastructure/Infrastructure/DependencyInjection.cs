using Application.Interfaces;
using Infrashtructure.Data;
using Infrashtructure.Services;
using Infrashtructure.Repositories;
using Infrashtructure.Helpers;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using StackExchange.Redis;

namespace Infrashtructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        // Database
        services.AddDbContext<BookingDbContext>(options =>
        {
            var connectionString = configuration.GetConnectionString("DefaultConnection");
            if(string.IsNullOrEmpty(connectionString))
            {
                throw new InvalidOperationException("Connection string 'DefaultConnection' not found.");
            }
            options.UseNpgsql(connectionString);
        });

        // Redis
        var redisConnection = configuration.GetConnectionString("Redis");
        if (string.IsNullOrEmpty(redisConnection))
        {
            throw new InvalidOperationException("Connection string 'Redis' not found.");
        }

        services.AddSingleton<IConnectionMultiplexer>(sp =>
        {
            var configurationOptions = ConfigurationOptions.Parse(redisConnection);
            return ConnectionMultiplexer.Connect(configurationOptions);
        });

        // Redis Lock Helper
        services.AddScoped<RedisLockHelper>();

        // Repositories
        services.AddScoped<IBookingRepository, BookingRepository>();

        // Services
        services.AddScoped<ILocationService, LocationService>();
        services.AddScoped<ITypeRoomService, TypeRoomService>();
        services.AddScoped<IRoomService, RoomService>();
        services.AddScoped<IBookingService, BookingService>();

        return services;
    }
}