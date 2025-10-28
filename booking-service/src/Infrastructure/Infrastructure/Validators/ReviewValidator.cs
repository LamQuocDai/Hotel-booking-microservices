
using Infrashtructure.Constants;

namespace Infrashtructure.Validators
{
    public static class ReviewValidator
    {
        public static ReviewValidationResult ValidateRoomId(Guid roomId)
        {
            if (roomId == Guid.Empty)
            {
                return new ReviewValidationResult
                {
                    IsValid = false,
                    ErrorMessage = "Room ID cannot be empty."
                };
            }
            return new ReviewValidationResult
            {
                IsValid = true
            };
        }

        public static ReviewValidationResult ValidateAccountId(Guid accountId)
        {
            if (accountId == Guid.Empty)
            {
                return new ReviewValidationResult
                {
                    IsValid = false,
                    ErrorMessage = "Account ID cannot be empty."
                };
            }
            return new ReviewValidationResult
            {
                IsValid = true
            };
        }
        public static ReviewValidationResult ValidateReview(int rating)
        {
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
