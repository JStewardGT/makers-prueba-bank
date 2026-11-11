import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { LoanService } from '../../../core/services/loan.service';
import { Loan } from '../../../core/models/loan.model';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { RejectDialogComponent } from '../../../shared/components/reject-dialog/reject-dialog.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    CurrencyFormatPipe
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  authService = inject(AuthService);
  loanService = inject(LoanService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  private sseSub?: Subscription;
  processingId: number | null = null;

  ngOnInit(): void {
    this.loanService.loadAllLoans().subscribe();

    // Streaming reactivo SSE para reflejar en vivo nuevas solicitudes de usuarios
    this.sseSub = this.loanService.connectSseStream().subscribe({
      next: (event) => {
        // Si entra un nuevo préstamo, recargar para asegurar consistencia
        if (event.status === 'PENDING') {
          this.loanService.loadAllLoans().subscribe();
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.sseSub?.unsubscribe();
  }

  approve(loan: Loan): void {
    this.processingId = loan.id;
    this.loanService.approveLoan(loan.id).subscribe({
      next: () => {
        this.processingId = null;
        this.snackBar.open(`¡Préstamo #${loan.id} aprobado exitosamente!`, 'Cerrar', {
          duration: 3500
        });
      },
      error: (err) => {
        this.processingId = null;
        const msg = err.error?.detail || 'No se pudo aprobar el préstamo';
        this.snackBar.open(msg, 'Cerrar', { duration: 4000 });
      }
    });
  }

  openRejectDialog(loan: Loan): void {
    const dialogRef = this.dialog.open(RejectDialogComponent, {
      width: '460px',
      data: loan
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.confirmed) {
        this.reject(loan.id, result.reason);
      }
    });
  }

  private reject(id: number, reason?: string): void {
    this.processingId = id;
    this.loanService.rejectLoan(id, reason).subscribe({
      next: () => {
        this.processingId = null;
        this.snackBar.open(`Préstamo #${id} rechazado`, 'Cerrar', {
          duration: 3500
        });
      },
      error: (err) => {
        this.processingId = null;
        const msg = err.error?.detail || 'No se pudo rechazar el préstamo';
        this.snackBar.open(msg, 'Cerrar', { duration: 4000 });
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
