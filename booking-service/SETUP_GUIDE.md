# Quick Setup Guide

## Prerequisites
- PostgreSQL database
- Redis server

## 1. Update Configuration

Add Redis connection string to `appsettings.json`:

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Host=localhost;Database=hotel_booking;Username=postgres;Password=your_password",
    "Redis": "localhost:6379,abortConnect=false"
  }
}
```

## 2. Run Database Migration

The composite index needs to be added to the database:

**Option A - Visual Studio Package Manager Console:**
```powershell
Add-Migration AddBookingCompositeIndex -Context BookingDbContext
Update-Database -Context BookingDbContext
```

**Option B - .NET CLI:**
```bash
cd src/Infrastructure/Infrastructure
dotnet ef migrations add AddBookingCompositeIndex --context BookingDbContext
dotnet ef database update --context BookingDbContext
```

## 3. Install Redis (if not already installed)

### Windows:
Download from: https://github.com/microsoftarchive/redis/releases

Or use Docker:
```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

### Linux/macOS:
```bash
# Using Docker
docker run -d -p 6379:6379 --name redis redis:latest

# Or using package manager
# Ubuntu/Debian
sudo apt-get install redis-server

# macOS
brew install redis
brew services start redis
```

## 4. Verify Redis Connection

Test Redis is running:
```bash
redis-cli ping
# Should return: PONG
```

## 5. Run the Application

```bash
cd src/Presentation/API
dotnet run
```

## 6. Test the Endpoints

### Create a Booking:
```http
POST https://localhost:5001/api/bookings
Content-Type: application/json

{
  "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "checkInTime": "2026-02-23T14:00:00",
  "checkOutTime": "2026-02-25T11:00:00",
  "accountId": "7fa85f64-5717-4562-b3fc-2c963f66afa9"
}
```

### Check Availability:
```http
GET https://localhost:5001/api/rooms/3fa85f64-5717-4562-b3fc-2c963f66afa6/availability?start=2026-02-23&end=2026-02-28
```

## Troubleshooting

### Redis Connection Fails
- Verify Redis is running: `redis-cli ping`
- Check connection string in appsettings.json
- Ensure port 6379 is not blocked by firewall

### Migration Fails
- Ensure connection string is correct
- Verify PostgreSQL is running
- Check if database exists

### Build Errors
- Restore NuGet packages: `dotnet restore`
- Clean and rebuild: `dotnet clean && dotnet build`

## Development Tips

### Monitor Redis Locks
```bash
# Connect to Redis CLI
redis-cli

# Monitor all commands
MONITOR

# Check for lock keys
KEYS lock:room:*

# Check lock value
GET lock:room:{roomId}
```

### Test Concurrent Bookings
Use tools like:
- Apache JMeter
- Postman (Collection Runner)
- k6 load testing

Create multiple concurrent requests to the same room to verify locking works correctly.
