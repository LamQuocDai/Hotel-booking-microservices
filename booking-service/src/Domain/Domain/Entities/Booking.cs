using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Domain.Enums;

namespace Domain.Entities;

public class Booking
{
    [Key]
    public Guid Id { get; set; }
    [Required]
    public Guid RoomId { get; set; }
    [ForeignKey(nameof(RoomId))]
    public Room Room { get; set; }
    [Required]
    public DateTime CheckInTime { get; set; }
    [Required]
    public DateTime CheckOutTime { get; set; }
    [Required]
    public Guid AccountId { get; set; }
    [Required]
    public BookingStatus Status { get; set; }
    [Required]
    public DateTime CreatedAt { get; set; } = DateTime.Now;
    public DateTime? DeletedAt { get; set; }
}