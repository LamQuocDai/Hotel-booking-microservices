import { Injectable, UnauthorizedException } from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { Inject } from '@nestjs/common';
import { Observable, firstValueFrom } from 'rxjs';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';

interface IAccountService {
  login(data: LoginDto): Observable<any>;
  register(data: RegisterDto): Observable<any>;
  refreshToken(data: { refreshToken: string }): Observable<any>;
}

@Injectable()
export class AuthService {
  private accountService: IAccountService;

  constructor(@Inject('ACCOUNT_SERVICE') private client: ClientGrpc) {}

  onModuleInit() {
    this.accountService =
      this.client.getService<IAccountService>('AccountService');
  }

  async login(loginDto: LoginDto) {
    try {
      return await firstValueFrom(this.accountService.login(loginDto));
    } catch (error) {
      throw new UnauthorizedException('Invalid credentials');
    }
  }

  async register(registerDto: RegisterDto) {
    try {
      return await firstValueFrom(this.accountService.register(registerDto));
    } catch (error) {
      throw new UnauthorizedException('Registration failed');
    }
  }

  async refreshToken(refreshToken: string) {
    try {
      return await firstValueFrom(
        this.accountService.refreshToken({ refreshToken }),
      );
    } catch (error) {
      throw new UnauthorizedException('Invalid refresh token');
    }
  }
}
