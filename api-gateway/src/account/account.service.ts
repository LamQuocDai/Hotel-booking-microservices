import { Injectable, Inject } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';

interface AccountService {
  getProfile(data: { userId: string }): Promise<any>;
}

@Injectable()
export class AccountService {
  private accountService: AccountService;

  constructor(@Inject('ACCOUNT_SERVICE') private client: ClientGrpc) {}

  onModuleInit() {
    this.accountService =
      this.client.getService<AccountService>('AccountService');
  }

  async getProfile(userId: string) {
    return await this.accountService.getProfile({ userId }).toPromise();
  }
}
