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
    
    // snake_case naming convention
    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
    {
        if (optionsBuilder.IsConfigured)
        {
            // nuget package: EFCore.NamingConventions
            optionsBuilder.UseSnakeCaseNamingConvention();
        }
    }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);
        
        // Location entity configuration
        modelBuilder.Entity<Location>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => e.Name).IsUnique().HasDatabaseName("IX_Location_Name");
            entity.Property(e => e.Name).IsRequired();
            entity.Property(e => e.Address).IsRequired();
            entity.Property(e => e.CreatedAt).IsRequired().HasDefaultValueSql("CURRENT_TIMESTAMP");
            entity.Property(e => e.DeletedAt).HasDefaultValue(null);
        });
        // TypeRoom entity configuration
        modelBuilder.Entity<TypeRoom>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => e.Name).IsUnique().HasDatabaseName("IX_TypeRoom_Name");
            entity.Property(e => e.Name).IsRequired();
            entity.Property(e => e.PricePerHour).IsRequired();
            entity.Property(e => e.CreatedAt).IsRequired().HasDefaultValueSql("CURRENT_TIMESTAMP");
            entity.Property(e => e.DeletedAt).HasDefaultValue(null);
        });
        // Room entity configuration
        modelBuilder.Entity<Room>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => e.Name).IsUnique().HasDatabaseName("IX_Room_Name");
            entity.HasIndex(e => e.TypeRoomId).HasDatabaseName("IX_Room_TypeRoomId");
            entity.HasIndex(e => e.Location).HasDatabaseName("IX_Room_Location");
            entity.Property(e => e.Name).IsRequired();
            entity.Property(e => e.TypeRoomId).IsRequired();
            entity.Property(e => e.Location).IsRequired();
            entity.Property(e => e.CreatedAt).IsRequired().HasDefaultValueSql("CURRENT_TIMESTAMP");
            entity.Property(e => e.DeletedAt).HasDefaultValue(null);
        });
        // Image entity configuration
        modelBuilder.Entity<Image>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.OriginFilename).IsRequired();
            entity.Property(e => e.Filename).IsRequired();
            entity.Property(e => e.Filesize).IsRequired();
            entity.Property(e => e.FileType).IsRequired();
            entity.Property(e => e.FullPath).IsRequired();
            entity.Property(e => e.RoomId).IsRequired();
            entity.Property(e => e.CreatedAt).IsRequired().HasDefaultValueSql("CURRENT_TIMESTAMP");
            entity.Property(e => e.DeletedAt).HasDefaultValue(null);
        });
        // Booking entity configuration
        modelBuilder.Entity<Booking>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => e.RoomId).HasDatabaseName("IX_Booking_RoomId");
            entity.HasIndex(e => e.AccountId).HasDatabaseName("IX_Booking_AccountId");
            entity.HasIndex(e => e.Status).HasDatabaseName("IX_Booking_Status");
            entity.Property(e => e.RoomId).IsRequired();
            entity.Property(e => e.AccountId).IsRequired();
            entity.Property(e => e.CheckInTime).IsRequired();
            entity.Property(e => e.CheckOutTime).IsRequired();
            entity.Property(e => e.Status).IsRequired();
            entity.Property(e => e.CreatedAt).IsRequired().HasDefaultValueSql("CURRENT_TIMESTAMP");
            entity.Property(e => e.DeletedAt).HasDefaultValue(null);
            
            entity.ToTable(t => t.HasCheckConstraint("CK_Booking_Status", "\"status\" IN (1, 2, 3, 4)"));
        });
        // Review entity configuration
        modelBuilder.Entity<Review>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => e.RoomId).HasDatabaseName("IX_Review_RoomId");
            entity.HasIndex(e => e.AccountId).HasDatabaseName("IX_Review_AccountId");
            entity.HasIndex(e => e.Rating).HasDatabaseName("IX_Review_Rating");
            entity.Property(e => e.RoomId).IsRequired();
            entity.Property(e => e.AccountId).IsRequired();
            entity.Property(e => e.Comment).HasColumnType("text");
            entity.Property(e => e.Rating).IsRequired();
            entity.Property(e => e.CreatedAt).IsRequired().HasDefaultValueSql("CURRENT_TIMESTAMP");
            entity.Property(e => e.DeletedAt).HasDefaultValue(null);
        });
    }
}