import { Injectable, Inject } from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { Observable, firstValueFrom } from 'rxjs';

interface IAccountService {
  getProfile(data: { userId: string }): Observable<any>;
}

@Injectable()
export class AccountService {
  private accountService: IAccountService;

  constructor(@Inject('ACCOUNT_SERVICE') private client: ClientGrpc) {}

  onModuleInit() {
    this.accountService =
      this.client.getService<IAccountService>('AccountService');
  }

  async getProfile(userId: string) {
    return await firstValueFrom(this.accountService.getProfile({ userId }));
  }
}
