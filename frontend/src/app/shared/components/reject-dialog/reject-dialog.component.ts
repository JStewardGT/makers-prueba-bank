import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CurrencyFormatPipe } from '../../pipes/currency-format.pipe';
import { Loan } from '../../../core/models/loan.model';

@Component({
  selector: 'app-reject-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    CurrencyFormatPipe
  ],
  template: `
    <h2 mat-dialog-title>Rechazar Solicitud de Préstamo</h2>
    <mat-dialog-content>
      <p style="margin-bottom: 12px; color: #475569;">
        ¿Estás seguro de que deseas rechazar la solicitud para
        <strong>{{ data.userEmail }}</strong> por
        <strong>{{ data.amount | currencyFormat }}</strong>?
      </p>

      <mat-form-field appearance="outline" style="width: 100%;">
        <mat-label>Motivo de rechazo (opcional)</mat-label>
        <textarea
          matInput
          rows="3"
          placeholder="Ej. Capacidad de endeudamiento insuficiente"
          [(ngModel)]="reason"
        ></textarea>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close()">Cancelar</button>
      <button class="btn-red" mat-flat-button (click)="confirmReject()">
        Confirmar Rechazo
      </button>
    </mat-dialog-actions>
  `
})
export class RejectDialogComponent {
  readonly dialogRef = inject(MatDialogRef<RejectDialogComponent>);
  readonly data = inject<Loan>(MAT_DIALOG_DATA);

  reason = '';

  confirmReject(): void {
    this.dialogRef.close({ confirmed: true, reason: this.reason.trim() });
  }
}
