export interface Account {
  accountId: string;
  iban: string;
  ownerName: string;
  balance: number;
  currency: string;
  status: string;
  version: number;
}

export interface TransactionEvent {
  transactionId: string;
  sourceIban: string;
  targetIban: string;
  amount: number;
  currency: string;
  status: 'INITIATED' | 'VALIDATED' | 'FRAUD_FLAGGED' | 'DB2_LEDGER_BOOKED' | 'COMPLETED' | 'FAILED';
  riskReason: string;
  timestamp: string;
}

export interface LegacyLedgerRecord {
  ledgerSeqId: number;
  txReferenceCode: string;
  sourceIban: string;
  targetIban: string;
  amount: number;
  currency: string;
  bookingDate: string;
  auditStatus: string;
  mainframeBatchId: string;
}
