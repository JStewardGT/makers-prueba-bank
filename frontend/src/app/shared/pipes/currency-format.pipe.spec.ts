import { CurrencyFormatPipe } from './currency-format.pipe';

describe('CurrencyFormatPipe', () => {
  const pipe = new CurrencyFormatPipe();

  it('debe instanciar el pipe correctamente', () => {
    expect(pipe).toBeTruthy();
  });

  it('debe formatear números con separador de miles y símbolo de moneda', () => {
    expect(pipe.transform(1000)).toBe('$ 1,000');
    expect(pipe.transform(2500000)).toBe('$ 2,500,000');
  });

  it('debe manejar valores nulos o indefinidos devolviendo $ 0', () => {
    expect(pipe.transform(null)).toBe('$ 0');
    expect(pipe.transform(undefined)).toBe('$ 0');
  });
});
