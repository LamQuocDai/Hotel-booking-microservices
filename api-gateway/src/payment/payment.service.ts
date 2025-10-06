import { Injectable, Inject } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';
import { CreatePaymentDto } from './dto/create-payment.dto';

interface PaymentService {
  createPayment(data: any): Promise<any>;
  getPromotions(): Promise<any>;
  getMyPayments(data: { userId: string }): Promise<any>;
  getPayment(data: { id: string }): Promise<any>;
}

@Injectable()
export class PaymentService {
  private paymentService: PaymentService;

  constructor(@Inject('PAYMENT_SERVICE') private client: ClientGrpc) {}

  onModuleInit() {
    this.paymentService =
      this.client.getService<PaymentService>('PaymentService');
  }

  async createPayment(createPaymentDto: CreatePaymentDto, userId: string) {
    return await this.paymentService
      .createPayment({
        ...createPaymentDto,
        userId,
      })
      .toPromise();
  }

  async getPromotions() {
    return await this.paymentService.getPromotions().toPromise();
  }

  async getMyPayments(userId: string) {
    return await this.paymentService.getMyPayments({ userId }).toPromise();
  }

  async getPayment(id: string) {
    return await this.paymentService.getPayment({ id }).toPromise();
  }
}
