import { Controller, Get, UseGuards, Request } from '@nestjs/common';
import { AccountService } from './account.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';

@Controller('account')
@UseGuards(JwtAuthGuard)
export class AccountController {
  constructor(private readonly accountService: AccountService) {}

  @Get('profile')
  async getProfile(@Request() req) {
    return this.accountService.getProfile(req.user.userId);
  }
}
