namespace Application.DTOs;

public class TypeRoomDto
{
    public Guid Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public double PricePerHour { get; set; }
    public DateTime CreatedAt { get; set; }
}

public class CreateTypeRoomDto
{
    public string Name { get; set; } = string.Empty;
    public double PricePerHour { get; set; }
}

public class UpdateTypeRoomDto
{
    public string Name { get; set; } = string.Empty;
    public double PricePerHour { get; set; }
}

public class TypeRoomPaginationRequestDto : BasePaginationRequestDto
{
}