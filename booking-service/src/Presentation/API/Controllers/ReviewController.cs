using Microsoft.AspNetCore.Mvc;
using Application.Interfaces;
using Application.DTOs;

namespace API.Controllers
{
    [Route("api/reviews")]
    [ApiController]
    public class ReviewController : ControllerBase
    {
        private readonly IReviewService _reviewService;
        public ReviewController(IReviewService reviewService)
        {
            _reviewService = reviewService;
        }

        [HttpGet]
        public async Task<ActionResult<ApiResponseDto<PagedResponseDto<ReviewDto>>>> GetAllReviews(ReviewPaginationRequestDto request)
        {
            var response = await _reviewService.GetReviewsAsync(request);
            return StatusCode(response.StatusCode, response);
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<ApiResponseDto<ReviewDto>>> GetReviewById(Guid id)
        {
            var response = await _reviewService.GetReviewByIdAsync(id);
            return StatusCode(response.StatusCode, response);
        }

        [HttpPost]
        public async Task<ActionResult<ApiResponseDto<ReviewDto>>> CreateReview([FromBody] CreateReviewDto createReviewDto)
        {
            var response = await _reviewService.CreateReviewAsync(createReviewDto);
            return StatusCode(response.StatusCode, response);
        }

        [HttpPatch("{id}")]
        public async Task<ActionResult<ApiResponseDto<ReviewDto>>> UpdateReview(Guid id, [FromBody] UpdateReviewDto updateReviewDto)
        {
            var response = await _reviewService.UpdateReviewAsync(id, updateReviewDto);
            return StatusCode(response.StatusCode, response);
        }

        [HttpDelete("{id}")]
        public async Task<ActionResult<ApiResponseDto<bool>>> DeleteReview(Guid id)
        {
            var response = await _reviewService.DeleteReviewAsync(id);
            return StatusCode(response.StatusCode, response);
        }
    }
}
