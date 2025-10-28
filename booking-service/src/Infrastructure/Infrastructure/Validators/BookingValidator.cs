namespace Infrashtructure.Validators
{
    public static class BookingValidator
    {
        public static BookingValidationResult ValidateBookingDates(DateTime startDate, DateTime endDate)
        {
            if (startDate >= endDate)
            {
                return new BookingValidationResult
                {
                    IsValid = false,
                    ErrorMessage = "Start date must be earlier than end date."
                };
            }
            if (startDate < DateTime.Now)
            {
                return new BookingValidationResult
                {
                    IsValid = false,
                    ErrorMessage = "Start date cannot be in the past."
                };
            }
            return new BookingValidationResult
            {
                IsValid = true
            };
        }
    }

    public class BookingValidationResult
    {
        public bool IsValid { get; set; }
        public string ErrorMessage { get; set; } = string.Empty;
    }
}
