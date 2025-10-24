using System.ComponentModel.DataAnnotations;

namespace Domain.Entities;
public class Location
{
    [Key]
    public Guid Id { get; set; }
    [Required]
    [MaxLength(100), MinLength(3)]
    public string Name { get; set; }
    [Required]
    [MaxLength(200), MinLength(10)]
    public string Address { get; set; }
    public ICollection<Room> Rooms { get; set; } = new HashSet<Room>();
    [Required]
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? DeletedAt { get; set; } 
}