import {
  Controller,
  Post,
  Get,
  Body,
  UseGuards,
  Request,
  Param,
} from '@nestjs/common';
import { PaymentService } from './payment.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CreatePaymentDto } from './dto/create-payment.dto';

@Controller('payment')
@UseGuards(JwtAuthGuard)
export class PaymentController {
  constructor(private readonly paymentService: PaymentService) {}

  @Post('create')
  async createPayment(
    @Body() createPaymentDto: CreatePaymentDto,
    @Request() req,
  ) {
    return this.paymentService.createPayment(createPaymentDto, req.user.userId);
  }

  @Get('promotions')
  async getPromotions() {
    return this.paymentService.getPromotions();
  }

  @Get('my-payments')
  async getMyPayments(@Request() req) {
    return this.paymentService.getMyPayments(req.user.userId);
  }

  @Get(':id')
  async getPayment(@Param('id') id: string) {
    return this.paymentService.getPayment(id);
  }
}
