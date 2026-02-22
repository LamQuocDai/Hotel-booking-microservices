# Booking System Enhancement - Implementation Summary

## Overview
Enhanced the hotel booking system with overlap prevention, availability checking, and distributed locking using Redis to prevent race conditions in concurrent booking scenarios.

## Changes Made

### 1. **Repository Layer** (New)

#### `IBookingRepository.cs` (Application Layer)
- **IsRoomAvailableAsync**: Checks if a room is available for a date range
  - Only considers `Holding` (Pending) and `Confirmed` bookings as blocking
  - Uses efficient LINQ that translates to optimized SQL queries
  - Overlap detection logic: `new.CheckIn < existing.CheckOut AND new.CheckOut > existing.CheckIn`

- **GetOverlappingBookingsAsync**: Retrieves all overlapping bookings for availability calendar

#### `BookingRepository.cs` (Infrastructure Layer)
- Implements efficient database queries using EF Core
- Leverages the composite index for optimal performance
- Filters by `DeletedAt == null` and booking status

### 2. **Redis Distributed Lock**

#### `RedisLockHelper.cs`
Provides distributed locking mechanism to prevent race conditions:

**Key Features:**
- **AcquireLockAsync**: Uses Redis `SET key value NX PX milliseconds`
  - `NX`: Only set if key doesn't exist (atomic check-and-set)
  - `PX`: Expiry time in milliseconds (default 10 seconds)
  
- **ReleaseLockAsync**: Uses Lua script for atomic release
  ```lua
  if redis.call('get', KEYS[1]) == ARGV[1] then
      return redis.call('del', KEYS[1])
  else
      return 0
  end
  ```
  This ensures only the lock owner can release it, preventing accidental releases.

- **ExecuteWithLockAsync**: Helper method with automatic lock management

**Lock Key Format:** `lock:room:{roomId}`

### 3. **Database Index Optimization**

#### Updated `BookingDbContext.cs`
Added composite index for efficient availability queries:
```csharp
entity.HasIndex(e => new { e.RoomId, e.CheckInTime, e.CheckOutTime })
    .HasDatabaseName("IX_Booking_Room_CheckInOut");
```

This index dramatically improves performance of overlap detection queries by allowing the database to quickly locate relevant bookings.

### 4. **Enhanced Booking Service**

#### Updated `BookingService.cs`

**Modified CreateBookingAsync Method:**
Implements a robust booking creation flow:

1. **Validate Input**: Check date validity (CheckIn < CheckOut)
2. **Verify Room Exists**: Ensure room is not deleted
3. **Acquire Redis Lock**: Prevent concurrent bookings for same room
   ```csharp
   var lockKey = $"lock:room:{createBookingDto.RoomId}";
   ```
4. **Start Database Transaction**: Ensure ACID properties
5. **Recheck Availability**: Double-check pattern inside transaction
6. **Insert Booking**: Create with default status `Holding`
7. **Commit Transaction**: Finalize the booking
8. **Release Lock**: Always release using Lua script (in finally block)

**Concurrency Protection Flow:**
```
Request 1                    Request 2
    |                            |
    v                            v
Validate Input              Validate Input
    |                            |
    v                            v
Acquire Lock (SUCCESS)      Acquire Lock (WAIT/FAIL)
    |                            |
    v                            X
Start Transaction           Returns Conflict Error
    |
    v
Check Availability
    |
    v
Insert Booking
    |
    v
Commit Transaction
    |
    v
Release Lock
```

**New GetRoomAvailabilityAsync Method:**
Generates daily availability calendar:

1. Validates date range
2. Verifies room exists
3. Fetches all overlapping bookings
4. Generates per-day availability status
5. Returns array of `{ date: "yyyy-MM-dd", available: bool }`

**Fixed UpdateBookingAsync:**
- Corrected inverted validation logic (bug fix)

### 5. **API Endpoints**

#### New Availability Endpoint
```
GET /api/rooms/{roomId}/availability?start=2026-02-23&end=2026-02-28
```

**Response:**
```json
{
  "isSuccess": true,
  "data": [
    { "date": "2026-02-23", "available": false },
    { "date": "2026-02-24", "available": true },
    { "date": "2026-02-25", "available": true },
    { "date": "2026-02-26", "available": false },
    { "date": "2026-02-27", "available": true }
  ],
  "message": "Availability retrieved successfully.",
  "statusCode": 200
}
```

Added to `RoomController.cs` as it's more RESTful to have room-related operations under `/api/rooms`.

### 6. **DTOs**

#### Updated `BookingDto.cs`
Added new DTOs:
- `RoomAvailabilityRequestDto`: Request parameters for availability check
- `DailyAvailabilityDto`: Daily availability response model

### 7. **Dependency Injection**

#### Updated `DependencyInjection.cs`
Registered new services:
- `IConnectionMultiplexer` (Redis) - Singleton
- `RedisLockHelper` - Scoped
- `IBookingRepository` ? `BookingRepository` - Scoped
- `IBookingService` ? `BookingService` - Scoped (was missing!)

### 8. **Package Dependencies**

#### Updated `Infrastructure.csproj`
Added:
```xml
<PackageReference Include="StackExchange.Redis" Version="2.8.16" />
```

## Booking Rules Implemented

1. **Date Validation:**
   - CheckInDate must be before CheckOutDate
   - CheckInDate is inclusive
   - CheckOutDate is exclusive

2. **Overlap Detection:**
   ```
   new.CheckInDate < existing.CheckOutDate
   AND
   new.CheckOutDate > existing.CheckInDate
   ```

3. **Blocking Statuses:**
   - Only `Holding` (Pending) and `Confirmed` bookings block availability
   - `Cancelled` and `Completed` bookings don't block

4. **Soft Deletes:**
   - Only consider bookings where `DeletedAt == null`

## Concurrency Protection Explained

### The Race Condition Problem
Without locking, two concurrent requests could both:
1. Check availability (both see room as available)
2. Create booking (both succeed)
3. Result: Double-booking!

### The Solution - Distributed Lock Pattern

#### Why Redis Lock?
- **In-Memory Speed**: Microsecond latency
- **Distributed**: Works across multiple API instances
- **Atomic Operations**: `SET NX` is atomic at Redis level
- **Auto-Expiry**: Prevents deadlocks if process crashes

#### Lock Lifecycle:
```csharp
// 1. Try to acquire lock
SET lock:room:{roomId} {uniqueValue} NX PX 10000

// 2. If successful, proceed with transaction
BEGIN TRANSACTION
  - Check availability
  - Insert booking
COMMIT TRANSACTION

// 3. Always release lock (using Lua script)
if redis.call('get', key) == value then
  return redis.call('del', key)
end
```

#### Why Lua Script for Release?
Without Lua script:
```csharp
// WRONG - Race condition between GET and DEL
var value = redis.Get(key);
if (value == myValue) {
    redis.Del(key); // Another process might have acquired the lock by now!
}
```

With Lua script:
- Executes atomically on Redis server
- No network round-trip between GET and DEL
- Guarantees only lock owner can release

### Double-Check Pattern
Even with locks, we recheck availability inside the transaction:
```csharp
// Outside transaction: Initial check (optional, for fast-fail)
var isAvailable = await _bookingRepository.IsRoomAvailableAsync(...);

// Acquire lock
await _redisLock.AcquireLockAsync(...);

// Inside transaction: Recheck (mandatory)
isAvailable = await _bookingRepository.IsRoomAvailableAsync(...);
if (!isAvailable) {
    return Conflict;
}
```

This protects against edge cases where data changed between check and lock acquisition.

## Database Migration Required

To apply the composite index, run:
```bash
cd src/Infrastructure/Infrastructure
dotnet ef migrations add AddBookingCompositeIndex --context BookingDbContext
dotnet ef database update --context BookingDbContext
```

Or in Package Manager Console:
```powershell
Add-Migration AddBookingCompositeIndex -Context BookingDbContext
Update-Database -Context BookingDbContext
```

## Configuration Required

Add Redis connection string to `appsettings.json`:
```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Host=localhost;Database=hotel_booking;Username=postgres;Password=...",
    "Redis": "localhost:6379,abortConnect=false"
  }
}
```

## Performance Optimizations

1. **Composite Index**: Speeds up overlap detection queries
2. **Repository Pattern**: Separates data access logic
3. **Async/Await**: Non-blocking I/O operations
4. **Redis Locking**: Minimal latency overhead (microseconds)
5. **Transaction Scope**: Minimized to reduce lock duration

## Testing Recommendations

1. **Unit Tests:**
   - Repository overlap detection logic
   - Availability calendar generation
   - Date validation

2. **Integration Tests:**
   - Redis lock acquisition and release
   - Concurrent booking attempts
   - Transaction rollback scenarios

3. **Load Tests:**
   - Concurrent users trying to book same room
   - Lock contention under high load
   - Database query performance with index

## Security Considerations

1. **Lock Timeout**: 10-second expiry prevents indefinite locks
2. **Unique Lock Values**: Prevents accidental releases
3. **Transaction Isolation**: Prevents dirty reads
4. **Input Validation**: Checks dates and room existence

## Potential Improvements

1. **Retry Logic**: Implement exponential backoff for lock acquisition
2. **Event Sourcing**: Log all booking state changes
3. **Cache Layer**: Cache availability for popular rooms
4. **Optimistic Locking**: Use EF Core concurrency tokens
5. **Circuit Breaker**: Handle Redis failures gracefully
6. **Monitoring**: Add metrics for lock wait times and conflicts

## API Usage Examples

### Create Booking (with concurrency protection)
```http
POST /api/bookings
Content-Type: application/json

{
  "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "checkInTime": "2026-02-23T14:00:00",
  "checkOutTime": "2026-02-25T11:00:00",
  "accountId": "7fa85f64-5717-4562-b3fc-2c963f66afa9"
}
```

### Check Availability
```http
GET /api/rooms/3fa85f64-5717-4562-b3fc-2c963f66afa6/availability?start=2026-02-23&end=2026-02-28
```

### Response (Conflict - Room Not Available)
```json
{
  "isSuccess": false,
  "data": null,
  "message": "Room is not available for the selected dates.",
  "statusCode": 409
}
```

### Response (Success)
```json
{
  "isSuccess": true,
  "data": {
    "id": "...",
    "roomId": "...",
    "checkInTime": "2026-02-23T14:00:00",
    "checkOutTime": "2026-02-25T11:00:00",
    "status": 1
  },
  "message": "Booking created successfully.",
  "statusCode": 201
}
```

## Files Modified/Created

### Created:
- `src/Application/Application/Interfaces/IBookingRepository.cs`
- `src/Infrastructure/Infrastructure/Repositories/BookingRepository.cs`
- `src/Infrastructure/Infrastructure/Helpers/RedisLockHelper.cs`

### Modified:
- `src/Infrastructure/Infrastructure/Infrastructure.csproj` (added Redis package)
- `src/Infrastructure/Infrastructure/Data/BookingDbContext.cs` (added composite index)
- `src/Application/Application/DTOs/BookingDto.cs` (added availability DTOs)
- `src/Application/Application/Interfaces/IBookingService.cs` (added availability method)
- `src/Infrastructure/Infrastructure/Services/BookingService.cs` (major enhancements)
- `src/Presentation/API/Controllers/RoomController.cs` (added availability endpoint)
- `src/Infrastructure/Infrastructure/DependencyInjection.cs` (registered new services)

## Summary

The enhanced booking system now provides:
- ? Overlap prevention with efficient database queries
- ? Distributed locking to prevent race conditions
- ? Availability checking API endpoint
- ? Database index optimization
- ? Transaction safety
- ? Proper error handling
- ? RESTful API design
- ? Production-ready concurrency protection

The implementation follows clean architecture principles, maintains separation of concerns, and ensures data consistency in distributed scenarios.
