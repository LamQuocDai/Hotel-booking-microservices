using System.ComponentModel.DataAnnotations;
using Application.Constants;

namespace Application.DTOs;

public abstract class BasePaginationRequestDto
{
    [Display(Order=1)]
    public int PageNumber { get; set; } = PaginationRequestContants.MinPageNumber;
    [Display(Order=2)]
    public int PageSize { get; set; } = PaginationRequestContants.DefaultPageSize;
    [Display(Order=3)]
    public string? Search {get; set;}
    [Display(Order=4)]
    public string? SortBy {get; set;} = PaginationRequestContants.DefaultSortBy;
    [Display(Order=5)]
    public string? SortDirection {get; set;} = PaginationRequestContants.DefaultSortDirection;
}