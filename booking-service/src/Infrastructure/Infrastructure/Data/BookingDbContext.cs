using Microsoft.EntityFrameworkCore;
using Domain.Entities;

namespace Infrashtructure.Data;

public class BookingDbContext : DbContext
{
    public BookingDbContext(DbContextOptions<BookingDbContext> options) : base(options)
    {
    }
    
    public DbSet<Location> Locations { get; set; }
    public DbSet<TypeRoom> TypeRooms { get; set; }
    public DbSet<Room> Rooms { get; set; }
    public DbSet<Image> Images { get; set; }
    public DbSet<Booking> Bookings { get; set; }
    public DbSet<Review> Reviews { get; set; }
}