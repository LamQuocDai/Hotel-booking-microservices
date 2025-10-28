using Application.DTOs;

namespace Application.Interfaces
{
    public interface IReviewService
    {
        Task<ApiResponseDto<PagedResponseDto<ReviewDto>>> GetReviewsAsync(ReviewPaginationRequestDto request);
        Task<ApiResponseDto<ReviewDto>> GetReviewByIdAsync(Guid id);
        Task<ApiResponseDto<ReviewDto>> CreateReviewAsync(CreateReviewDto createReviewDto);
        Task<ApiResponseDto<ReviewDto>> UpdateReviewAsync(Guid id, UpdateReviewDto updateReviewDto);
        Task<ApiResponseDto<bool>> DeleteReviewAsync(Guid id);
    }
}
