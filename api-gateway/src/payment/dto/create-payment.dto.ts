import {
  IsNotEmpty,
  IsString,
  IsArray,
  IsOptional,
  IsNumber,
} from 'class-validator';

export class CreatePaymentDto {
  @IsArray()
  @IsNotEmpty()
  roomBookingIds: string[];

  @IsString()
  @IsOptional()
  promotionCode?: string;

  @IsNumber()
  @IsNotEmpty()
  total: number;

  @IsNumber()
  @IsNotEmpty()
  tax: number;

  @IsNumber()
  @IsOptional()
  discount?: number;
}
