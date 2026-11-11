import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { CreateLoanRequest, Loan, LoanStatusEvent, RejectLoanRequest } from '../models/loan.model';

@Injectable({
  providedIn: 'root'
})
export class LoanService {
  private http = inject(HttpClient);

  // Reactive State using Signals
  private loansSignal = signal<Loan[]>([]);
  private loadingSignal = signal<boolean>(false);
  private errorSignal = signal<string | null>(null);

  readonly loans = this.loansSignal.asReadonly();
  readonly loading = this.loadingSignal.asReadonly();
  readonly error = this.errorSignal.asReadonly();

  requestLoan(request: CreateLoanRequest): Observable<Loan> {
    this.loadingSignal.set(true);
    return this.http.post<Loan>('/api/v1/loans', request).pipe(
      tap({
        next: (newLoan) => {
          this.loansSignal.update((current) => [newLoan, ...current]);
          this.loadingSignal.set(false);
        },
        error: (err) => {
          this.errorSignal.set(err.error?.detail || 'Error al solicitar préstamo');
          this.loadingSignal.set(false);
        }
      })
    );
  }

  loadMyLoans(): Observable<Loan[]> {
    this.loadingSignal.set(true);
    return this.http.get<Loan[]>('/api/v1/loans/my-loans').pipe(
      tap({
        next: (list) => {
          this.loansSignal.set(list);
          this.loadingSignal.set(false);
        },
        error: (err) => {
          this.errorSignal.set(err.error?.detail || 'Error al cargar préstamos');
          this.loadingSignal.set(false);
        }
      })
    );
  }

  loadAllLoans(): Observable<Loan[]> {
    this.loadingSignal.set(true);
    return this.http.get<Loan[]>('/api/v1/loans').pipe(
      tap({
        next: (list) => {
          this.loansSignal.set(list);
          this.loadingSignal.set(false);
        },
        error: (err) => {
          this.errorSignal.set(err.error?.detail || 'Error al cargar lista global');
          this.loadingSignal.set(false);
        }
      })
    );
  }

  approveLoan(id: number): Observable<Loan> {
    return this.http.patch<Loan>(`/api/v1/loans/${id}/approve`, {}).pipe(
      tap((updated) => {
        this.loansSignal.update((current) =>
          current.map((l) => (l.id === id ? updated : l))
        );
      })
    );
  }

  rejectLoan(id: number, reason?: string): Observable<Loan> {
    const payload: RejectLoanRequest = { reason };
    return this.http.patch<Loan>(`/api/v1/loans/${id}/reject`, payload).pipe(
      tap((updated) => {
        this.loansSignal.update((current) =>
          current.map((l) => (l.id === id ? updated : l))
        );
      })
    );
  }

  getLoanById(id: number): Observable<Loan> {
    return this.http.get<Loan>(`/api/v1/loans/${id}`);
  }

  connectSseStream(): Observable<LoanStatusEvent> {
    return new Observable<LoanStatusEvent>((observer) => {
      const eventSource = new EventSource('/api/v1/loans/stream');

      eventSource.onmessage = (event) => {
        try {
          const data: LoanStatusEvent = JSON.parse(event.data);
          // Actualiza reactivamente el estado si el préstamo ya está en la lista
          this.loansSignal.update((current) => {
            const index = current.findIndex((l) => l.id === data.loanId);
            if (index !== -1) {
              const updatedList = [...current];
              updatedList[index] = {
                ...updatedList[index],
                status: data.status,
                rejectionReason: data.reason
              };
              return updatedList;
            }
            return current;
          });
          observer.next(data);
        } catch (e) {
          console.warn('Error parseando evento SSE:', e);
        }
      };

      eventSource.onerror = (err) => {
        console.warn('Conexión SSE cerrada o con error:', err);
      };

      return () => {
        eventSource.close();
      };
    });
  }
}
