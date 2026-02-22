using Application.Interfaces;
using Domain.Entities;
using Domain.Enums;
using Infrashtructure.Data;
using Microsoft.EntityFrameworkCore;

namespace Infrashtructure.Repositories
{
    public class BookingRepository : IBookingRepository
    {
        private readonly BookingDbContext _context;

        public BookingRepository(BookingDbContext context)
        {
            _context = context;
        }

        /// <summary>
        /// Checks if a room is available by verifying no overlapping bookings exist.
        /// Only considers Pending (Holding) and Confirmed bookings as blocking.
        /// 
        /// Overlap logic:
        /// - new.CheckIn < existing.CheckOut AND new.CheckOut > existing.CheckIn
        /// 
        /// This translates to an efficient SQL query with proper indexing.
        /// </summary>
        public async Task<bool> IsRoomAvailableAsync(Guid roomId, DateTime checkIn, DateTime checkOut)
        {
            // Efficient LINQ query that translates to SQL
            // Uses composite index on (RoomId, CheckInTime, CheckOutTime)
            var hasOverlap = await _context.Bookings
                .AnyAsync(b => 
                    b.RoomId == roomId 
                    && b.DeletedAt == null
                    && (b.Status == BookingStatus.Holding || b.Status == BookingStatus.Confirmed)
                    && b.CheckInTime < checkOut 
                    && b.CheckOutTime > checkIn);

            return !hasOverlap;
        }

        /// <summary>
        /// Gets all overlapping bookings for availability calendar generation.
        /// </summary>
        public async Task<List<Booking>> GetOverlappingBookingsAsync(Guid roomId, DateTime checkIn, DateTime checkOut)
        {
            return await _context.Bookings
                .Where(b => 
                    b.RoomId == roomId 
                    && b.DeletedAt == null
                    && (b.Status == BookingStatus.Holding || b.Status == BookingStatus.Confirmed)
                    && b.CheckInTime < checkOut 
                    && b.CheckOutTime > checkIn)
                .OrderBy(b => b.CheckInTime)
                .ToListAsync();
        }
    }
}
