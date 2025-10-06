import {
  Controller,
  Get,
  Post,
  Body,
  UseGuards,
  Request,
  Param,
} from '@nestjs/common';
import { BookingService } from './booking.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CreateBookingDto } from './dto/create-booking.dto';

@Controller('booking')
@UseGuards(JwtAuthGuard)
export class BookingController {
  constructor(private readonly bookingService: BookingService) {}

  @Get('rooms')
  async getRooms() {
    return this.bookingService.getRooms();
  }

  @Get('rooms/:id')
  async getRoom(@Param('id') id: string) {
    return this.bookingService.getRoom(id);
  }

  @Post('book')
  async createBooking(
    @Body() createBookingDto: CreateBookingDto,
    @Request() req,
  ) {
    return this.bookingService.createBooking(createBookingDto, req.user.userId);
  }

  @Get('my-bookings')
  async getMyBookings(@Request() req) {
    return this.bookingService.getMyBookings(req.user.userId);
  }
}
