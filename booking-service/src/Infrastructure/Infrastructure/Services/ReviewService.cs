using Application.Constants;
using Application.DTOs;
using Application.Interfaces;
using AutoMapper;
using Infrashtructure.Data;
using Infrashtructure.Validators;
using Microsoft.EntityFrameworkCore;
using Domain.Entities;

namespace Infrashtructure.Services
{
    public class ReviewService : IReviewService
    {
        private readonly BookingDbContext _context;
        private readonly IMapper _mapper;

        public ReviewService(BookingDbContext context, IMapper mapper)
        {
            _context = context;
            _mapper = mapper;
        }

        public async Task<ApiResponseDto<PagedResponseDto<ReviewDto>>> GetReviewsAsync(ReviewPaginationRequestDto request)
        {
            try
            {
                var query = _context.Reviews.Where(r => r.DeletedAt == null).AsQueryable();
                if(!string.IsNullOrEmpty(request.Search))
                {
                    var searchItem = request.Search.ToLower();
                    query = query.Where(r => r.Comment.ToLower().Contains(searchItem));
                }
                if(request.RoomId.HasValue)
                {
                    query = query.Where(r => r.RoomId == request.RoomId.Value);
                }
                if(request.AccountId.HasValue)
                {
                    query = query.Where(r => r.AccountId == request.AccountId.Value);
                }
                if(request.Rating.HasValue)
                {
                    query = query.Where(r => r.Rating == request.Rating.Value);
                }

                var total = await query.CountAsync();
                var totalPages = (int)Math.Ceiling(total / (double)request.PageSize);

                var reviews = await query
                    .Skip((request.PageNumber - 1) * request.PageSize)
                    .Take(request.PageSize)
                    .ToListAsync();

                var reviewDtos = _mapper.Map<List<ReviewDto>>(reviews);
                var pagedResponse = new PagedResponseDto<ReviewDto>
                {
                    Items = reviewDtos,
                    Paging =
                    {
                        Total = total,
                        PageNumber = request.PageNumber,
                        PageSize = request.PageSize,
                        TotalPages = totalPages,
                        HasPrevious = request.PageNumber > 1,
                        HasNext = request.PageNumber < totalPages
                    }
                };

                return new ApiResponseDto<PagedResponseDto<ReviewDto>>
                {
                    IsSuccess = true,
                    Data = pagedResponse,
                    Message = "Reviews retrieved successfully",
                    StatusCode = ApplicationStatusCode.Success
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<PagedResponseDto<ReviewDto>>
                {
                    IsSuccess = false,
                    Message = $"An error occurred: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }

        public async Task<ApiResponseDto<ReviewDto>> GetReviewByIdAsync(Guid id)
        {
            try
            {
                var review = await _context.Reviews.FirstOrDefaultAsync(r => r.Id == id && r.DeletedAt == null);
                if (review == null)
                {
                    return new ApiResponseDto<ReviewDto>
                    {
                        IsSuccess = false,
                        Message = "Review not found",
                        StatusCode = ApplicationStatusCode.NotFound
                    };
                }
                return new ApiResponseDto<ReviewDto>
                {
                    IsSuccess = true,
                    Data = _mapper.Map<ReviewDto>(review),
                    Message = "Review retrieved successfully",
                    StatusCode = ApplicationStatusCode.Success
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<ReviewDto>
                {
                    IsSuccess = false,
                    Message = $"An error occurred: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }

        public async Task<ApiResponseDto<ReviewDto>> CreateReviewAsync(CreateReviewDto createReviewDto)
        {
            try
            {
                var roomIdValidation = ReviewValidator.ValidateRoomId(createReviewDto.RoomId);
                if (!roomIdValidation.IsValid)
                {
                    return new ApiResponseDto<ReviewDto>
                    {
                        IsSuccess = false,
                        Message = roomIdValidation.ErrorMessage,
                        StatusCode = ApplicationStatusCode.BadRequest
                    };
                }
                var accountIdValidation = ReviewValidator.ValidateAccountId(createReviewDto.AccountId);
                if (!accountIdValidation.IsValid)
                {
                    return new ApiResponseDto<ReviewDto>
                    {
                        IsSuccess = false,
                        Message = accountIdValidation.ErrorMessage,
                        StatusCode = ApplicationStatusCode.BadRequest
                    };
                }
                var reviewValidation = ReviewValidator.ValidateReview(createReviewDto.Rating);
                if (!reviewValidation.IsValid)
                {
                    return new ApiResponseDto<ReviewDto>
                    {
                        IsSuccess = false,
                        Message = reviewValidation.ErrorMessage,
                        StatusCode = ApplicationStatusCode.BadRequest
                    };
                }
                var review = _mapper.Map<Review>(createReviewDto);
                await _context.Reviews.AddAsync(review);
                await _context.SaveChangesAsync();

                return new ApiResponseDto<ReviewDto>
                {
                    IsSuccess = true,
                    Data = _mapper.Map<ReviewDto>(review),
                    Message = "Review created successfully",
                    StatusCode = ApplicationStatusCode.Created
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<ReviewDto>
                {
                    IsSuccess = false,
                    Message = $"An error occurred: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }

        public async Task<ApiResponseDto<ReviewDto>> UpdateReviewAsync(Guid id, UpdateReviewDto updateReviewDto)
        {
            try
            {
                var ratingValidation = ReviewValidator.ValidateReview(updateReviewDto.Rating);
                if (!ratingValidation.IsValid)
                {
                    return new ApiResponseDto<ReviewDto>
                    {
                        IsSuccess = false,
                        Message = ratingValidation.ErrorMessage,
                        StatusCode = ApplicationStatusCode.BadRequest
                    };
                }
                var review = await _context.Reviews.FirstOrDefaultAsync(r => r.Id == id && r.DeletedAt == null);
                if (review == null)
                {
                    return new ApiResponseDto<ReviewDto>
                    {
                        IsSuccess = false,
                        Message = "Review not found",
                        StatusCode = ApplicationStatusCode.NotFound
                    };
                }
                review.Comment = updateReviewDto.Comment;
                review.Rating = updateReviewDto.Rating;
                _context.Reviews.Update(review);
                await _context.SaveChangesAsync();

                return new ApiResponseDto<ReviewDto>
                {
                    IsSuccess = true,
                    Data = _mapper.Map<ReviewDto>(review),
                    Message = "Review updated successfully",
                    StatusCode = ApplicationStatusCode.Success
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<ReviewDto>
                {
                    IsSuccess = false,
                    Message = $"An error occurred: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }

        public async Task<ApiResponseDto<bool>> DeleteReviewAsync(Guid id)
        {
            try
            {
                var review = await _context.Reviews.FirstOrDefaultAsync(r => r.Id == id && r.DeletedAt == null);
                if (review == null)
                {
                    return new ApiResponseDto<bool>
                    {
                        IsSuccess = false,
                        Data = false,
                        Message = "Review not found",
                        StatusCode = ApplicationStatusCode.NotFound
                    };
                }
                review.DeletedAt = DateTime.UtcNow;
                _context.Reviews.Update(review);
                await _context.SaveChangesAsync();

                return new ApiResponseDto<bool>
                {
                    IsSuccess = true,
                    Data = true,
                    Message = "Review deleted successfully",
                    StatusCode = ApplicationStatusCode.Success
                };
            }
            catch (Exception ex)
            {
                return new ApiResponseDto<bool>
                {
                    IsSuccess = false,
                    Data = false,
                    Message = $"An error occurred: {ex.Message}",
                    StatusCode = ApplicationStatusCode.InternalServerError
                };
            }
        }
    }
}
