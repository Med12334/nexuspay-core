import { Component, ChangeDetectionStrategy, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NexusPayApiService } from './services/nexuspay-api.service';
import { Account, LegacyLedgerRecord } from './models/nexuspay.models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="enterprise-shell">
      <header class="topbar">
        <h1>NEXUSPAY // Enterprise Kafka & IBM DB2 Modernization Control Tower</h1>
        <span class="badge">LIVE STREAMING: ACTIVE</span>
      </header>

      <section class="grid-layout">
        <!-- Panel 1: Initiate SEPA Instant Transfer (Kafka Saga Producer) -->
        <div class="card">
          <h2>Initiate SEPA Transfer (Kafka Saga)</h2>
          <form (ngSubmit)="submitTransfer()">
            <label>Source IBAN (PostgreSQL)</label>
            <input [(ngModel)]="transferForm.sourceIban" name="sourceIban" placeholder="IT60X0542811101000000123456" required />

            <label>Target IBAN (PostgreSQL)</label>
            <input [(ngModel)]="transferForm.targetIban" name="targetIban" placeholder="IT60X0542811101000000654321" required />

            <label>Amount (EUR) — >10,000 triggers Kafka AML Fraud Alert</label>
            <input type="number" [(ngModel)]="transferForm.amount" name="amount" required />

            <button type="submit">Dispatch to Kafka (tx-initiated)</button>
          </form>
        </div>

        <!-- Panel 2: Live Kafka Fraud Detection Stream -->
        <div class="card alert-panel">
          <h2>Real-Time Kafka Fraud Alerts (AML Engine)</h2>
          <ul>
            <li *ngFor="let alert of fraudAlerts$ | async">
              <strong>[{{ alert.status }}]</strong> {{ alert.amount | number:'1.2-2' }} {{ alert.currency }}
              ({{ alert.sourceIban }} → {{ alert.targetIban }}) — Reason: {{ alert.riskReason }}
            </li>
          </ul>
        </div>
      </section>

      <!-- Panel 3: IBM DB2 Legacy Mainframe Audit Trail -->
      <section class="card full-width">
        <div class="panel-header">
          <h2>IBM DB2 Mainframe Core Ledger (LEGACY_CORE_LEDGER)</h2>
          <div>
            <input [(ngModel)]="ibanFilter" placeholder="Filter by IBAN..." />
            <button (click)="refreshDb2Audit()">Query IBM DB2</button>
          </div>
        </div>
        <table>
          <thead>
            <tr>
              <th>DB2 Seq ID</th>
              <th>TX Reference UUID</th>
              <th>Source IBAN</th>
              <th>Target IBAN</th>
              <th>Amount</th>
              <th>Batch ID</th>
              <th>Audit Status</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let row of db2LedgerRecords()">
              <td>{{ row.ledgerSeqId }}</td>
              <td>{{ row.txReferenceCode }}</td>
              <td>{{ row.sourceIban }}</td>
              <td>{{ row.targetIban }}</td>
              <td>{{ row.amount | number:'1.2-2' }} {{ row.currency }}</td>
              <td>{{ row.mainframeBatchId }}</td>
              <td><span class="status-ok">{{ row.auditStatus }}</span></td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  `
})
export class AppComponent implements OnInit {
  private readonly api = inject(NexusPayApiService);

  readonly sagaStream$ = this.api.liveSagaStream$;
  readonly fraudAlerts$ = this.api.liveFraudAlerts$;

  readonly accounts = signal<Account[]>([]);
  readonly db2LedgerRecords = signal<LegacyLedgerRecord[]>([]);

  ibanFilter = '';
  transferForm = {
    sourceIban: 'IT60X0542811101000000123456',
    targetIban: 'IT60X0542811101000000654321',
    amount: 2500,
    currency: 'EUR'
  };

  ngOnInit(): void {
    this.refreshDb2Audit();
  }

  submitTransfer(): void {
    this.api.initiateTransfer(this.transferForm).subscribe(() => {
      setTimeout(() => this.refreshDb2Audit(), 1500);
    });
  }

  refreshDb2Audit(): void {
    this.api.queryDb2MainframeLedger(this.ibanFilter).subscribe(res => {
      this.db2LedgerRecords.set(res.content);
    });
  }
}
