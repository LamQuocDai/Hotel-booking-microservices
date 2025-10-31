using Application;
using Microsoft.EntityFrameworkCore;
using Serilog;
using Infrashtructure;
using Infrashtructure.Data;
using StackExchange.Redis;
using API.Middlewares;

var builder = WebApplication.CreateBuilder(args);

// Configure Serilog
Log.Logger = new LoggerConfiguration()
    .WriteTo.Console()
    .WriteTo.File("logs/booking-service-.txt", rollingInterval: RollingInterval.Day)
    .CreateLogger();

builder.Host.UseSerilog();

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Add Infrastructure and Application services
builder.Services.AddInfrastructure(builder.Configuration);
builder.Services.AddApplication();


var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Use custom middlewares
app.UseGlobalExceptionHandling();
app.UseRequestLogging();

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

// Ensure database is created and migrated
using (var scope = app.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<BookingDbContext>();
    
    context.Database.Migrate(); // Automatically applies pending migrations
    
    Log.Information("Database migration completed successfully");
}

Log.Information("Booking API starting up...");

// Map gRPC services
//app.MapGrpcService<BookingGrpcService>();

try
{
    app.Run();
}
catch (Exception ex)
{
    Log.Fatal(ex, "Booking API failed to start");
}
finally
{
    Log.CloseAndFlush();
}