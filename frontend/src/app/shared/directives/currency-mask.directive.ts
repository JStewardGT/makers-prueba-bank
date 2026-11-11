import {
  Directive,
  ElementRef,
  HostListener,
  OnInit,
  forwardRef,
  inject
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Directive({
  selector: '[appCurrencyMask]',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CurrencyMaskDirective),
      multi: true
    }
  ]
})
export class CurrencyMaskDirective implements ControlValueAccessor, OnInit {
  private el = inject(ElementRef<HTMLInputElement>);

  private onChange: (value: number | null) => void = () => {};
  private onTouched: () => void = () => {};

  ngOnInit(): void {
    this.el.nativeElement.style.textAlign = 'right';
  }

  @HostListener('input', ['$event'])
  onInput(event: Event): void {
    const input = this.el.nativeElement;
    const cleanValue = input.value.replace(/[^0-9]/g, '');

    if (!cleanValue) {
      input.value = '';
      this.onChange(null);
      return;
    }

    const numericVal = parseInt(cleanValue, 10);
    input.value = this.formatCurrency(numericVal);
    this.onChange(numericVal);
  }

  @HostListener('blur')
  onBlur(): void {
    this.onTouched();
    const input = this.el.nativeElement;
    const cleanValue = input.value.replace(/[^0-9]/g, '');
    if (cleanValue) {
      input.value = this.formatCurrency(parseInt(cleanValue, 10));
    }
  }

  writeValue(value: any): void {
    if (value !== undefined && value !== null && !isNaN(value)) {
      this.el.nativeElement.value = this.formatCurrency(Number(value));
    } else {
      this.el.nativeElement.value = '';
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.el.nativeElement.disabled = isDisabled;
  }

  private formatCurrency(value: number): string {
    return '$ ' + value.toLocaleString('en-US');
  }
}
