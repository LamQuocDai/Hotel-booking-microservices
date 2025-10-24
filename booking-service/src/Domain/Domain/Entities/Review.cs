using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Domain.Entities;

public class Review
{
    [Key]
    public Guid Id { get; set; }
    [Required]
    public Guid RoomId { get; set; }
    [ForeignKey(nameof(RoomId))]
    public Room Room { get; set; }
    [Required]
    public Guid AccountId { get; set; }
    public string Comment { get; set; }
    [Range(1, 5)]
    public int Rating { get; set; }
    [Required]
    public DateTime CreatedAt { get; set; } = DateTime.Now;
    public DateTime? DeletedAt { get; set; }
}