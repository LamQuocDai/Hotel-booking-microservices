namespace Application.DTOs;

public class RoomDto
{
    public Guid Id { get; set; }
    public string Name { get; set; }
    public Guid TypeRoomId { get; set; }
    public Guid LocationId { get; set; }
    public DateTime CreatedAt { get; set; }
}

public class CreateRoomDto
{
    public string Name { get; set; }
    public Guid TypeRoomId { get; set; }
    public Guid LocationId { get; set; }
}

public class UpdateRoomDto
{
    public string Name { get; set; }
    public Guid TypeRoomId { get; set; }
    public Guid LocationId { get; set; }
}

public class RoomPaginationRequestDto : BasePaginationRequestDto
{
    public Guid? TypeRoomId { get; set; }
    public Guid? LocationId { get; set; }
}