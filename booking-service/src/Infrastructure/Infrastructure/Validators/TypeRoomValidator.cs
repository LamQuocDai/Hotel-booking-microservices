using Infrashtructure.Constants;

namespace Infrashtructure.Validators;

public static class TypeRoomValidator
{
    public static TypeRoomValidationResult ValidateName(string name)
    {
        if (string.IsNullOrWhiteSpace(name))
        {
            return new TypeRoomValidationResult
            {
                IsValid = false,
                ErrorMessage = "Name is required."
            };
        }

        if (name.Length < TypeRoomValidationConstants.MinNameLength || name.Length > TypeRoomValidationConstants.MaxNameLength)
        {
            return new TypeRoomValidationResult
            {
                IsValid = false,
                ErrorMessage = $"Name must be between {TypeRoomValidationConstants.MinNameLength} and {TypeRoomValidationConstants.MaxNameLength} characters."
            };
        }

        return new TypeRoomValidationResult { IsValid = true };
    }
    
    public static TypeRoomValidationResult ValidatePricePerHour(double pricePerHour)
    {
        if (pricePerHour < TypeRoomValidationConstants.MinPricePerHour)
        {
            return new TypeRoomValidationResult
            {
                IsValid = false,
                ErrorMessage = $"Price per hour must be at least {TypeRoomValidationConstants.MinPricePerHour}."
            };
        }

        return new TypeRoomValidationResult { IsValid = true };
    }
}

public class TypeRoomValidationResult
{
    public bool IsValid { get; set; }
    public string ErrorMessage { get; set; } = string.Empty;
}