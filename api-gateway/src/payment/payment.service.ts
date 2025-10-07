import { Injectable, Inject } from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { Observable, firstValueFrom } from 'rxjs';
import { CreatePaymentDto } from './dto/create-payment.dto';

interface IPaymentService {
  createPayment(data: any): Observable<any>;
  getPromotions(): Observable<any>;
  getMyPayments(data: { userId: string }): Observable<any>;
  getPayment(data: { id: string }): Observable<any>;
}

@Injectable()
export class PaymentService {
  private paymentService: IPaymentService;

  constructor(@Inject('PAYMENT_SERVICE') private client: ClientGrpc) {}

  onModuleInit() {
    this.paymentService =
      this.client.getService<IPaymentService>('PaymentService');
  }

  async createPayment(createPaymentDto: CreatePaymentDto, userId: string) {
    return await firstValueFrom(
      this.paymentService.createPayment({
        ...createPaymentDto,
        userId,
      }),
    );
  }

  async getPromotions() {
    return await firstValueFrom(this.paymentService.getPromotions());
  }

  async getMyPayments(userId: string) {
    return await firstValueFrom(this.paymentService.getMyPayments({ userId }));
  }

  async getPayment(id: string) {
    return await firstValueFrom(this.paymentService.getPayment({ id }));
  }
}
