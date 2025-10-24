using Infrashtructure.Constants;

namespace Infrashtructure.Validators;

public static class LocationValidator
{
    public static LocationValidationResult ValidateName(string name)
    {
        if (string.IsNullOrEmpty(name))
        {
            return new LocationValidationResult
            {
                IsValid = false,
                ErrorMessage = "Name is required."
            };
        }
        if (name.Length < LocationValidationConstants.MinNameLength || name.Length > LocationValidationConstants.MaxNameLength)
        {
            return new LocationValidationResult
            {
                IsValid = false,
                ErrorMessage = $"Name must be between {LocationValidationConstants.MinNameLength} and {LocationValidationConstants.MaxNameLength} characters."
            };
        }
        return new LocationValidationResult { IsValid = true };
    }

    public static LocationValidationResult ValidateAddress(string address)
    {
        if (string.IsNullOrEmpty(address))
        {
            return new LocationValidationResult
            {
                IsValid = false,
                ErrorMessage = "Address is required."
            };
        }

        if (address.Length < LocationValidationConstants.MinAddressLength ||
            address.Length > LocationValidationConstants.MaxAddressLength)
        {
            return new LocationValidationResult
            {
                IsValid = false,
                ErrorMessage =
                    $"Address must be between {LocationValidationConstants.MinAddressLength} and {LocationValidationConstants.MaxAddressLength} characters."
            };
        }

        return new LocationValidationResult { IsValid = true };
    }
}

public class LocationValidationResult
{
    public bool IsValid { get; set; }
    public string ErrorMessage { get; set; } = string.Empty;
}