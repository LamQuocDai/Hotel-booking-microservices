using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;


namespace Domain.Entities;

public class Image
{
    [Key]
    public Guid Id { get; set; }
    [Required]
    public string OriginFilename { get; set; } = string.Empty;
    [Required]
    public string Filename { get; set; } = string.Empty;
    [Required]
    public int Filesize { get; set; }
    [Required]
    public string FileType { get; set; } = string.Empty;
    [Required]
    public string FullPath { get; set; } = string.Empty;
    [Required]
    public Guid RoomId { get; set; }
    [ForeignKey(nameof(RoomId))]
    public Room Room { get; set; } = default!;
    [Required]
    public DateTime CreatedAt { get; set; } = DateTime.Now;
    public DateTime? DeletedAt { get; set; }
}