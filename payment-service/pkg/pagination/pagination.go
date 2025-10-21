package pagination

import (
	"math"
	"strconv"

	"github.com/gin-gonic/gin"
)

// PaginationQuery represents common pagination parameters
type PaginationQuery struct {
	PageNumber    int    `form:"pageNumber" json:"pageNumber" example:"1"`
	PageSize      int    `form:"pageSize" json:"pageSize" example:"10"`
	Search        string `form:"search" json:"search" example:"search term"`
	SortBy        string `form:"sortBy" json:"sortBy" example:"created_at"`
	SortDirection string `form:"sortDirection" json:"sortDirection" example:"desc"`
}

// PaginationInfo represents pagination metadata
type PaginationInfo struct {
	Total       int64 `json:"total" example:"100"`
	PageNumber  int   `json:"pageNumber" example:"1"`
	PageSize    int   `json:"pageSize" example:"10"`
	TotalPages  int   `json:"totalPages" example:"10"`
	HasNext     bool  `json:"hasNext" example:"true"`
	HasPrevious bool  `json:"hasPrevious" example:"false"`
}

// PaginatedResponse represents a paginated API response
type PaginatedResponse struct {
	Items      interface{}    `json:"items"`
	Pagination PaginationInfo `json:"pagination"`
}

// NewPaginationQuery creates a new pagination query with default values
func NewPaginationQuery() *PaginationQuery {
	return &PaginationQuery{
		PageNumber:    1,
		PageSize:      10,
		Search:        "",
		SortBy:        "created_at",
		SortDirection: "desc",
	}
}

// ParsePaginationQuery parses pagination parameters from gin context
func ParsePaginationQuery(c *gin.Context) *PaginationQuery {
	query := NewPaginationQuery()

	if pageNumber := c.Query("pageNumber"); pageNumber != "" {
		if num, err := strconv.Atoi(pageNumber); err == nil && num > 0 {
			query.PageNumber = num
		}
	}

	if pageSize := c.Query("pageSize"); pageSize != "" {
		if size, err := strconv.Atoi(pageSize); err == nil && size > 0 && size <= 100 {
			query.PageSize = size
		}
	}

	query.Search = c.Query("search")
	
	if sortBy := c.Query("sortBy"); sortBy != "" {
		query.SortBy = sortBy
	}

	if sortDirection := c.Query("sortDirection"); sortDirection != "" {
		if sortDirection == "asc" || sortDirection == "desc" {
			query.SortDirection = sortDirection
		}
	}

	return query
}

// GetOffset calculates the MongoDB skip value
func (p *PaginationQuery) GetOffset() int {
	return (p.PageNumber - 1) * p.PageSize
}

// GetLimit returns the page size
func (p *PaginationQuery) GetLimit() int {
	return p.PageSize
}

// NewPaginationInfo creates pagination info from total count and query
func NewPaginationInfo(total int64, query *PaginationQuery) PaginationInfo {
	totalPages := int(math.Ceil(float64(total) / float64(query.PageSize)))
	
	return PaginationInfo{
		Total:       total,
		PageNumber:  query.PageNumber,
		PageSize:    query.PageSize,
		TotalPages:  totalPages,
		HasNext:     query.PageNumber < totalPages,
		HasPrevious: query.PageNumber > 1,
	}
}

// NewPaginatedResponse creates a new paginated response
func NewPaginatedResponse(items interface{}, total int64, query *PaginationQuery) *PaginatedResponse {
	return &PaginatedResponse{
		Items:      items,
		Pagination: NewPaginationInfo(total, query),
	}
}