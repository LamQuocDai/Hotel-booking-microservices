import { Injectable, Inject } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';
import { CreateBookingDto } from './dto/create-booking.dto';

interface BookingService {
  getRooms(): Promise<any>;
  getRoom(data: { id: string }): Promise<any>;
  createBooking(data: any): Promise<any>;
  getMyBookings(data: { userId: string }): Promise<any>;
}

@Injectable()
export class BookingService {
  private bookingService: BookingService;

  constructor(@Inject('BOOKING_SERVICE') private client: ClientGrpc) {}

  onModuleInit() {
    this.bookingService =
      this.client.getService<BookingService>('BookingService');
  }

  async getRooms() {
    return await this.bookingService.getRooms().toPromise();
  }

  async getRoom(id: string) {
    return await this.bookingService.getRoom({ id }).toPromise();
  }

  async createBooking(createBookingDto: CreateBookingDto, userId: string) {
    return await this.bookingService
      .createBooking({
        ...createBookingDto,
        userId,
      })
      .toPromise();
  }

  async getMyBookings(userId: string) {
    return await this.bookingService.getMyBookings({ userId }).toPromise();
  }
}
