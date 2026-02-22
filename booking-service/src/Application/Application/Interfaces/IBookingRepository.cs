using Domain.Entities;

namespace Application.Interfaces
{
    public interface IBookingRepository
    {
        /// <summary>
        /// Checks if a room is available for booking in the specified date range.
        /// Returns true if the room is available, false if there are overlapping bookings.
        /// </summary>
        /// <param name="roomId">The ID of the room to check</param>
        /// <param name="checkIn">Check-in date (inclusive)</param>
        /// <param name="checkOut">Check-out date (exclusive)</param>
        /// <returns>True if room is available, false otherwise</returns>
        Task<bool> IsRoomAvailableAsync(Guid roomId, DateTime checkIn, DateTime checkOut);
        
        /// <summary>
        /// Gets all bookings that overlap with the specified date range for a room.
        /// Used for availability calendar generation.
        /// </summary>
        Task<List<Booking>> GetOverlappingBookingsAsync(Guid roomId, DateTime checkIn, DateTime checkOut);
    }
}
