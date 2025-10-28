

namespace Application.DTOs
{
    public class ReviewDto
    {
        public Guid Id { get; set; }
        public Guid RoomId { get; set; }
        public string RoomName { get; set; } = string.Empty;
        public Guid AccountId { get; set; }
        public string Comment { get; set; } = string.Empty;
        public int Rating { get; set; }
        public DateTime CreatedAt { get; set; }
    }

    public class CreateReviewDto
    {
        public Guid RoomId { get; set; }
        public Guid AccountId { get; set; }
        public string Comment { get; set; } = string.Empty;
        public int Rating { get; set; }
    }

    public class UpdateReviewDto
    {
        public string Comment { get; set; } = string.Empty;
        public int Rating { get; set; }
    }

    public class ReviewPaginationRequestDto : BasePaginationRequestDto
    {
        public Guid? RoomId { get; set; }
        public Guid? AccountId { get; set; }
        public int? Rating { get; set; }
    }
}
