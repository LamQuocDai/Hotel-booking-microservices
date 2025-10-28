using Domain.Enums;

namespace Application.DTOs
{
    public class BookingDto
    {
        public Guid Id { get; set; }
        public Guid RoomId { get; set; }
        public string RoomName { get; set; } = string.Empty;
        public DateTime CheckInTime { get; set; }
        public DateTime CheckOutTime { get; set; }
        public Guid AccountId { get; set; }
        public BookingStatus Status { get; set; }
        public DateTime CreatedAt { get; set; }
    }

    public class CreateBookingDto
    {
        public Guid RoomId { get; set; }
        public DateTime CheckInTime { get; set; }
        public DateTime CheckOutTime { get; set; }
        public Guid AccountId { get; set; }
    }

    public class UpdateBookingDto
    {
        public DateTime CheckInTime { get; set; }
        public DateTime CheckOutTime { get; set; }
        public BookingStatus Status { get; set; }
    }

    public class BookingPaginationRequestDto : BasePaginationRequestDto
    {
        public Guid? RoomId { get; set; }
        public Guid? AccountId { get; set; }
        public  DateTime? CheckInTime { get; set; }
        public DateTime? CheckOutTime { get; set; }
        public BookingStatus? Status { get; set; }
    }
}
