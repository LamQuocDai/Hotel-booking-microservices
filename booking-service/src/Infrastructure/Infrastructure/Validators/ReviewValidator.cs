
using Infrashtructure.Constants;

namespace Infrashtructure.Validators
{
    public static class ReviewValidator
    {
        public static ReviewValidationResult ValidateReview(string reviewText, int rating)
        {
            if (string.IsNullOrWhiteSpace(reviewText))
            {
                return new ReviewValidationResult
                {
                    IsValid = false,
                    ErrorMessage = "Review text cannot be empty."
                };
            }
            if (rating < ReviewValidationConstants.MinRating || rating > ReviewValidationConstants.MaxRating)
            {
                return new ReviewValidationResult
                {
                    IsValid = false,
                    ErrorMessage = $"Rating must be between {ReviewValidationConstants.MinRating} and {ReviewValidationConstants.MaxRating}."
                };
            }
            return new ReviewValidationResult
            {
                IsValid = true
            };
        }
    }

    public class ReviewValidationResult
    {
        public bool IsValid { get; set; }
        public string ErrorMessage { get; set; } = string.Empty;
    }
}
