import { IsNotEmpty, IsString, IsDateString } from 'class-validator';

export class CreateBookingDto {
  @IsString()
  @IsNotEmpty()
  roomId: string;

  @IsDateString()
  @IsNotEmpty()
  checkinTime: string;

  @IsDateString()
  @IsNotEmpty()
  checkoutTime: string;
}
