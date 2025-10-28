namespace Application.DTOs;

public class RoomDto
{
    public Guid Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public Guid TypeRoomId { get; set; }
    public string TypeRoomName { get; set; } = string.Empty;
    public Guid LocationId { get; set; }
    public string LocationName { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; }
}

public class CreateRoomDto
{
    public string Name { get; set; } = string.Empty;
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