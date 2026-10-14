import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, timer, switchMap, shareReplay } from 'rxjs';
import { Account, TransactionEvent, LegacyLedgerRecord } from '../models/nexuspay.models';

@Injectable({ providedIn: 'root' })
export class NexusPayApiService {
  private readonly http = inject(HttpClient);
  private readonly gatewayUrl = 'http://localhost:8000/api/v1';

  // Reactive RxJS Stream polling live Kafka Saga states every 2 seconds
  readonly liveSagaStream$: Observable<TransactionEvent[]> = timer(0, 2000).pipe(
    switchMap(() => this.http.get<TransactionEvent[]>(`${this.gatewayUrl}/transactions`)),
    shareReplay(1)
  );

  // Reactive RxJS Stream polling live Kafka Fraud Engine alerts
  readonly liveFraudAlerts$: Observable<TransactionEvent[]> = timer(0, 3000).pipe(
    switchMap(() => this.http.get<TransactionEvent[]>(`${this.gatewayUrl}/fraud-alerts`)),
    shareReplay(1)
  );

  getAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.gatewayUrl}/accounts`);
  }

  initiateTransfer(payload: { sourceIban: string; targetIban: string; amount: number; currency: string }): Observable<TransactionEvent> {
    return this.http.post<TransactionEvent>(`${this.gatewayUrl}/transactions/initiate`, payload);
  }

  queryDb2MainframeLedger(iban?: string, page = 0, size = 20): Observable<{ content: LegacyLedgerRecord[] }> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (iban) {
      params = params.set('iban', iban);
    }
    return this.http.get<{ content: LegacyLedgerRecord[] }>(`${this.gatewayUrl}/db2-ledger/audit`, { params });
  }
}
