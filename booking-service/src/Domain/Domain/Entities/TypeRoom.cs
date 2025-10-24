using System.ComponentModel.DataAnnotations;
namespace Domain.Entities;
public class TypeRoom
{
    [Key]
    public Guid Id { get; set; }
    [Required]
    [MaxLength(50), MinLength(3)]
    public string Name { get; set; }
    [Required]
    public double PricePerHour { get; set; }
    public ICollection<Room> Rooms { get; set; } = new HashSet<Room>();
    [Required]
    public DateTime CreatedAt { get; set; } = DateTime.Now;
    public DateTime? DeletedAt { get; set; }
}