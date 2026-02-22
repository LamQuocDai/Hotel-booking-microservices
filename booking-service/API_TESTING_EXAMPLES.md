# API Testing Examples

## 1. Check Room Availability (Before Booking)

```http
GET /api/rooms/3fa85f64-5717-4562-b3fc-2c963f66afa6/availability?start=2026-02-23T00:00:00&end=2026-02-28T00:00:00
Accept: application/json
```

**Expected Response (200 OK):**
```json
{
  "isSuccess": true,
  "data": [
    { "date": "2026-02-23", "available": true },
    { "date": "2026-02-24", "available": true },
    { "date": "2026-02-25", "available": true },
    { "date": "2026-02-26", "available": true },
    { "date": "2026-02-27", "available": true }
  ],
  "message": "Availability retrieved successfully.",
  "statusCode": 200
}
```

## 2. Create First Booking (Should Succeed)

```http
POST /api/bookings
Content-Type: application/json
Accept: application/json

{
  "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "checkInTime": "2026-02-23T14:00:00",
  "checkOutTime": "2026-02-25T11:00:00",
  "accountId": "7fa85f64-5717-4562-b3fc-2c963f66afa9"
}
```

**Expected Response (201 Created):**
```json
{
  "isSuccess": true,
  "data": {
    "id": "b2a85f64-5717-4562-b3fc-2c963f66afa1",
    "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "roomName": "Deluxe Suite",
    "checkInTime": "2026-02-23T14:00:00",
    "checkOutTime": "2026-02-25T11:00:00",
    "accountId": "7fa85f64-5717-4562-b3fc-2c963f66afa9",
    "status": 1,
    "createdAt": "2025-01-15T10:30:00"
  },
  "message": "Booking created successfully.",
  "statusCode": 201
}
```

## 3. Try Overlapping Booking (Should Fail)

### Scenario A: Exact Same Dates
```http
POST /api/bookings
Content-Type: application/json

{
  "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "checkInTime": "2026-02-23T14:00:00",
  "checkOutTime": "2026-02-25T11:00:00",
  "accountId": "8fa85f64-5717-4562-b3fc-2c963f66afb1"
}
```

**Expected Response (409 Conflict):**
```json
{
  "isSuccess": false,
  "data": null,
  "message": "Room is not available for the selected dates.",
  "statusCode": 409
}
```

### Scenario B: Partial Overlap (Start During Existing)
```http
POST /api/bookings
Content-Type: application/json

{
  "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "checkInTime": "2026-02-24T10:00:00",
  "checkOutTime": "2026-02-26T10:00:00",
  "accountId": "8fa85f64-5717-4562-b3fc-2c963f66afb1"
}
```

**Expected Response (409 Conflict):**
```json
{
  "isSuccess": false,
  "data": null,
  "message": "Room is not available for the selected dates.",
  "statusCode": 409
}
```

### Scenario C: Partial Overlap (End During Existing)
```http
POST /api/bookings
Content-Type: application/json

{
  "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "checkInTime": "2026-02-22T14:00:00",
  "checkOutTime": "2026-02-24T11:00:00",
  "accountId": "8fa85f64-5717-4562-b3fc-2c963f66afb1"
}
```

**Expected Response (409 Conflict):**
```json
{
  "isSuccess": false,
  "data": null,
  "message": "Room is not available for the selected dates.",
  "statusCode": 409
}
```

### Scenario D: Encompasses Existing Booking
```http
POST /api/bookings
Content-Type: application/json

{
  "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "checkInTime": "2026-02-22T14:00:00",
  "checkOutTime": "2026-02-26T11:00:00",
  "accountId": "8fa85f64-5717-4562-b3fc-2c963f66afb1"
}
```

**Expected Response (409 Conflict):**
```json
{
  "isSuccess": false,
  "data": null,
  "message": "Room is not available for the selected dates.",
  "statusCode": 409
}
```

## 4. Non-Overlapping Booking (Should Succeed)

### Before Existing Booking (CheckOut = Existing CheckIn)
```http
POST /api/bookings
Content-Type: application/json

{
  "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "checkInTime": "2026-02-21T14:00:00",
  "checkOutTime": "2026-02-23T14:00:00",
  "accountId": "8fa85f64-5717-4562-b3fc-2c963f66afb1"
}
```

**Expected Response (201 Created):**
```json
{
  "isSuccess": true,
  "data": {
    "id": "c3a85f64-5717-4562-b3fc-2c963f66afa2",
    "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "checkInTime": "2026-02-21T14:00:00",
    "checkOutTime": "2026-02-23T14:00:00",
    "status": 1
  },
  "message": "Booking created successfully.",
  "statusCode": 201
}
```

### After Existing Booking (CheckIn = Existing CheckOut)
```http
POST /api/bookings
Content-Type: application/json

{
  "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "checkInTime": "2026-02-25T11:00:00",
  "checkOutTime": "2026-02-27T11:00:00",
  "accountId": "9fa85f64-5717-4562-b3fc-2c963f66afc1"
}
```

**Expected Response (201 Created):**
```json
{
  "isSuccess": true,
  "data": {
    "id": "d4a85f64-5717-4562-b3fc-2c963f66afa3",
    "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "checkInTime": "2026-02-25T11:00:00",
    "checkOutTime": "2026-02-27T11:00:00",
    "status": 1
  },
  "message": "Booking created successfully.",
  "statusCode": 201
}
```

## 5. Check Availability After Bookings

```http
GET /api/rooms/3fa85f64-5717-4562-b3fc-2c963f66afa6/availability?start=2026-02-20T00:00:00&end=2026-02-28T00:00:00
```

**Expected Response (200 OK):**
```json
{
  "isSuccess": true,
  "data": [
    { "date": "2026-02-20", "available": true },
    { "date": "2026-02-21", "available": false },
    { "date": "2026-02-22", "available": false },
    { "date": "2026-02-23", "available": false },
    { "date": "2026-02-24", "available": false },
    { "date": "2026-02-25", "available": false },
    { "date": "2026-02-26", "available": false },
    { "date": "2026-02-27", "available": true }
  ],
  "message": "Availability retrieved successfully.",
  "statusCode": 200
}
```

## 6. Invalid Requests (Error Handling)

### Invalid Date Range (CheckIn >= CheckOut)
```http
POST /api/bookings
Content-Type: application/json

{
  "roomId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "checkInTime": "2026-02-25T14:00:00",
  "checkOutTime": "2026-02-23T11:00:00",
  "accountId": "7fa85f64-5717-4562-b3fc-2c963f66afa9"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "isSuccess": false,
  "data": null,
  "message": "Check-out time must be after check-in time.",
  "statusCode": 400
}
```

### Non-Existent Room
```http
POST /api/bookings
Content-Type: application/json

{
  "roomId": "00000000-0000-0000-0000-000000000000",
  "checkInTime": "2026-02-23T14:00:00",
  "checkOutTime": "2026-02-25T11:00:00",
  "accountId": "7fa85f64-5717-4562-b3fc-2c963f66afa9"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "isSuccess": false,
  "data": null,
  "message": "Room does not exist.",
  "statusCode": 400
}
```

## 7. Concurrent Booking Test (Load Testing)

Use this as a template for concurrent testing tools (JMeter, k6, etc.):

```javascript
// k6 script example
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 10, // 10 concurrent users
  duration: '10s',
};

export default function () {
  const url = 'http://localhost:5000/api/bookings';
  const payload = JSON.stringify({
    roomId: '3fa85f64-5717-4562-b3fc-2c963f66afa6',
    checkInTime: '2026-03-01T14:00:00',
    checkOutTime: '2026-03-03T11:00:00',
    accountId: '7fa85f64-5717-4562-b3fc-2c963f66afa9'
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);
  
  // Only ONE should succeed (201), others should get 409 Conflict
  check(res, {
    'status is 201 or 409': (r) => r.status === 201 || r.status === 409,
  });
}
```

**Expected Behavior:**
- First request: 201 Created
- All subsequent concurrent requests: 409 Conflict
- No double bookings should occur

## 8. Get All Bookings

```http
GET /api/bookings?pageNumber=1&pageSize=10
```

### Filter by Room
```http
GET /api/bookings?roomId=3fa85f64-5717-4562-b3fc-2c963f66afa6&pageNumber=1&pageSize=10
```

### Filter by Date Range
```http
GET /api/bookings?checkInTime=2026-02-20&checkOutTime=2026-02-28&pageNumber=1&pageSize=10
```

### Filter by Status
```http
GET /api/bookings?status=1&pageNumber=1&pageSize=10
```

## Notes

- All date-times should be in ISO 8601 format
- CheckOutDate is exclusive (guest checks out on this date)
- CheckInDate is inclusive (guest checks in on this date)
- Booking status: 1=Holding, 2=Confirmed, 3=Cancelled, 4=Completed
- Only Holding (1) and Confirmed (2) bookings block availability
