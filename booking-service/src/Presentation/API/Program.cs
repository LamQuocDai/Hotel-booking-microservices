using Application;
using Microsoft.EntityFrameworkCore;
// using BookingService.Data;
// using BookingService.Services;
using Serilog;
using Infrashtructure;
using StackExchange.Redis;

var builder = WebApplication.CreateBuilder(args);

// Configure Serilog
Log.Logger = new LoggerConfiguration()
    .WriteTo.Console()
    .WriteTo.File("logs/booking-service-.txt", rollingInterval: RollingInterval.Day)
    .CreateLogger();

builder.Host.UseSerilog();

// Add Infrastructure and Application services
builder.Services.AddInfrastructure(builder.Configuration);
builder.Services.AddApplication();

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Add Entity Framework
// builder.Services.AddDbContext<BookingDbContext>(options =>
//     options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

// // Add Redis
// builder.Services.AddSingleton<IConnectionMultiplexer>(sp =>
//     ConnectionMultiplexer.Connect(builder.Configuration.GetConnectionString("Redis")));

// Add gRPC
//builder.Services.AddGrpc();

// Add custom services
// builder.Services.AddScoped<IBookingService, BookingService.Services.BookingService>();
// builder.Services.AddScoped<IRedisLockService, RedisLockService>();

var app = builder.Build();

app.MapGet("/health", () => "Booking service OK");

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

// Map gRPC services
//app.MapGrpcService<BookingGrpcService>();

app.Run();