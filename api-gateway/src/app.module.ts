import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { WinstonModule } from 'nest-winston';
import { AppController } from './app.controller';
import { AppService } from './app.service';
//import * as winston from 'winston';
// import { AuthModule } from './auth/auth.module';
// import { AccountModule } from './account/account.module';
// import { BookingModule } from './booking/booking.module';
// import { PaymentModule } from './payment/payment.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
    }),
    WinstonModule.forRoot({
      // level: 'info',
      // format: winston.format.combine(
      //   winston.format.timestamp(),
      //   winston.format.errors({ stack: true }),
      //   winston.format.json(),
      // ),
      // transports: [
      //   new winston.transports.Console({
      //     format: winston.format.combine(
      //       winston.format.colorize(),
      //       winston.format.simple(),
      //     ),
      //   }),
      // ],
    }),
    // AuthModule,
    // AccountModule,
    // BookingModule,
    // PaymentModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
