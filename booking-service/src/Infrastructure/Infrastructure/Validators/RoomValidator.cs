using Infrashtructure.Constants;

namespace Infrashtructure.Validators;

public static class RoomValidator
{
    public static RoomValidationResult ValidateRoomName(string name)
    {
        if (string.IsNullOrWhiteSpace(name))
        {
            return new RoomValidationResult
            {
                IsValid = false,
                ErrorMessage = "Room name is required."
            };
        }

        if (name.Length < RoomValidationConstants.MinRoomName || name.Length > RoomValidationConstants.MaxRoomName)
        {
            return new RoomValidationResult
            {
                IsValid = false,
                ErrorMessage = $"Room name must be between {RoomValidationConstants.MinRoomName} and {RoomValidationConstants.MaxRoomName} characters long."
            };
        }

        return new RoomValidationResult { IsValid = true };
    }

    public static RoomValidationResult ValidateTypeRoomId(Guid typeRoomId)
    {
        if (typeRoomId == Guid.Empty)
        {
            return new RoomValidationResult
            {
                IsValid = false,
                ErrorMessage = "TypeRoomId is required."
            };
        }

        return new RoomValidationResult { IsValid = true };
    }

    public static RoomValidationResult ValidateLocationId(Guid locationId)
    {
        if (locationId == Guid.Empty)
        {
            return new RoomValidationResult
            {
                IsValid = false,
                ErrorMessage = "LocationId is required."
            };
        }

        return new RoomValidationResult { IsValid = true };
    }
}

public class RoomValidationResult
{
    public bool IsValid { get; set; }
    public string ErrorMessage { get; set; } = string.Empty;
}