export type LoanStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Loan {
  id: number;
  amount: number;
  termMonths: number;
  status: LoanStatus;
  userId: number;
  userEmail: string;
  rejectionReason?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateLoanRequest {
  amount: number;
  termMonths: number;
}

export interface RejectLoanRequest {
  reason?: string;
}

export interface LoanStatusEvent {
  loanId: number;
  userId: number;
  userEmail: string;
  amount: number;
  termMonths: number;
  status: LoanStatus;
  reason?: string | null;
  timestamp: string;
}
