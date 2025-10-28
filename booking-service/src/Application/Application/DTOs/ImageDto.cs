
namespace Application.DTOs
{
    public class ImageDto
    {
        public Guid Id { get; set; }
        public string OriginFilename { get; set; } = string.Empty;
        public string Filename { get; set; } = string.Empty;
        public int Filesize { get; set; }
        public string FileType { get; set; } = string.Empty;
        public string FullPath { get; set; } = string.Empty;
        public Guid RoomId { get; set; }
        public DateTime CreatedAt { get; set; }
    }

    public class CreateImageDto
    {
        public string OriginFilename { get; set; } = string.Empty;
        public string Filename { get; set; } = string.Empty;
        public int Filesize { get; set; }
        public string FileType { get; set; } = string.Empty;
        public string FullPath { get; set; } = string.Empty;
        public Guid RoomId { get; set; }
    }

    public class UpdateImageDto
    {
        public string OriginFilename { get; set; } = string.Empty;
        public string Filename { get; set; } = string.Empty;
        public int Filesize { get; set; }
        public string FileType { get; set; } = string.Empty;
        public string FullPath { get; set; } = string.Empty;
        public Guid RoomId { get; set; }
    }


}