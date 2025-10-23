namespace Application.Constants;

public static class PaginationRequestContants
{
    public const int MaxPageSize = 100;
    public const int MinPageNumber = 1;
    public const int DefaultPageSize = 10;
    public const int MaxPageNumber = int.MaxValue;
    public const string DefaultSortBy = "CreatedAt";
    public const string DefaultSortDirection = "desc";
}