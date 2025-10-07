import { Injectable, Inject } from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { Observable, firstValueFrom } from 'rxjs';
import { CreateBookingDto } from './dto/create-booking.dto';

interface IBookingService {
  getRooms(): Observable<any>;
  getRoom(data: { id: string }): Observable<any>;
  createBooking(data: any): Observable<any>;
  getMyBookings(data: { userId: string }): Observable<any>;
}

@Injectable()
export class BookingService {
  private bookingService: IBookingService;

  constructor(@Inject('BOOKING_SERVICE') private client: ClientGrpc) {}

  onModuleInit() {
    this.bookingService =
      this.client.getService<IBookingService>('BookingService');
  }

  async getRooms() {
    return await firstValueFrom(this.bookingService.getRooms());
  }

  async getRoom(id: string) {
    return await firstValueFrom(this.bookingService.getRoom({ id }));
  }

  async createBooking(createBookingDto: CreateBookingDto, userId: string) {
    return await firstValueFrom(
      this.bookingService.createBooking({
        ...createBookingDto,
        userId,
      }),
    );
  }

  async getMyBookings(userId: string) {
    return await firstValueFrom(this.bookingService.getMyBookings({ userId }));
  }
}
