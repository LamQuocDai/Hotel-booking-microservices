using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Domain.Entities;

public class Room
{
    [Key]
    public Guid Id { get; set; }
    [Required]
    [MaxLength(100), MinLength(3)]
    public string Name { get; set; }
    [Required]
    public Guid TypeRoomId { get; set; }
    [Required]
    public Guid Location { get; set; }
    [Required]
    public DateTime CreatedAt { get; set; } = DateTime.Now;
    public DateTime DeletedAt { get; set; }
    [ForeignKey(nameof(TypeRoomId))]
    public TypeRoom TypeRoom { get; set; } = default!;
    [ForeignKey(nameof(Location))]
    public Location LocationRoom { get; set; } = default!;
    public ICollection<Image> Images { get; set; } = new List<Image>();
    public ICollection<Booking> Bookings { get; set; } = new List<Booking>();
    public ICollection<Review> Reviews { get; set; } = new List<Review>();
}