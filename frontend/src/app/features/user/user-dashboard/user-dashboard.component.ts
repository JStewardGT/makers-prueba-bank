import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { LoanService } from '../../../core/services/loan.service';
import { CurrencyMaskDirective } from '../../../shared/directives/currency-mask.directive';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    CurrencyMaskDirective,
    CurrencyFormatPipe
  ],
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.scss']
})
export class UserDashboardComponent implements OnInit, OnDestroy {
  authService = inject(AuthService);
  loanService = inject(LoanService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);

  private sseSub?: Subscription;
  submitting = false;

  loanForm: FormGroup = this.fb.group({
    amount: [null, [Validators.required, Validators.min(100), Validators.max(100000000)]],
    termMonths: [12, [Validators.required, Validators.min(1), Validators.max(72)]]
  });

  ngOnInit(): void {
    this.loanService.loadMyLoans().subscribe();

    // Suscripción al stream reactivo SSE de WebFlux para actualizaciones en tiempo real
    this.sseSub = this.loanService.connectSseStream().subscribe({
      next: (event) => {
        if (event.userId === this.authService.currentUser()?.id) {
          this.snackBar.open(
            `Actualización: Préstamo #${event.loanId} ahora está ${event.status}`,
            'OK',
            { duration: 4000 }
          );
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.sseSub?.unsubscribe();
  }

  onSubmit(): void {
    if (this.loanForm.invalid) {
      this.loanForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const { amount, termMonths } = this.loanForm.value;

    this.loanService.requestLoan({ amount, termMonths }).subscribe({
      next: (newLoan) => {
        this.submitting = false;
        this.snackBar.open(
          `¡Solicitud de préstamo #${newLoan.id} enviada exitosamente!`,
          'Cerrar',
          { duration: 4000 }
        );
        this.loanForm.reset({ amount: null, termMonths: 12 });
      },
      error: (err) => {
        this.submitting = false;
        const msg = err.error?.detail || 'Error al procesar la solicitud';
        this.snackBar.open(msg, 'Cerrar', { duration: 4000 });
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
