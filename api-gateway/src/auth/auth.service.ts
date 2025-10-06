import { Injectable, UnauthorizedException } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';
import { Inject } from '@nestjs/common';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';

interface AccountService {
  login(data: LoginDto): Promise<any>;
  register(data: RegisterDto): Promise<any>;
  refreshToken(data: { refreshToken: string }): Promise<any>;
}

@Injectable()
export class AuthService {
  private accountService: AccountService;

  constructor(@Inject('ACCOUNT_SERVICE') private client: ClientGrpc) {}

  onModuleInit() {
    this.accountService =
      this.client.getService<AccountService>('AccountService');
  }

  async login(loginDto: LoginDto) {
    try {
      return await this.accountService.login(loginDto).toPromise();
    } catch (error) {
      throw new UnauthorizedException('Invalid credentials');
    }
  }

  async register(registerDto: RegisterDto) {
    try {
      return await this.accountService.register(registerDto).toPromise();
    } catch (error) {
      throw new UnauthorizedException('Registration failed');
    }
  }

  async refreshToken(refreshToken: string) {
    try {
      return await this.accountService
        .refreshToken({ refreshToken })
        .toPromise();
    } catch (error) {
      throw new UnauthorizedException('Invalid refresh token');
    }
  }
}
