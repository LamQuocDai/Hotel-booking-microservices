namespace Application.DTOs;

public class PagedResponseDto<T>
{
    public List<T> Items { get; set; } = new();
    public PagingDto Paging { get; set; } = new();
}

public class PagingDto
{
    public int Total {get; set;}
    public int PageNumber {get; set;}
    public int PageSize {get; set;}
    public int TotalPages {get; set;}
    public bool HasPrevious {get; set;}
    public bool HasNext {get; set;}
}