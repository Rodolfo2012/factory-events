import { describe, expect, it } from 'vitest';
import { formatDateTime, normalizeTimestamp, toLocalIsoSeconds } from './datetime.js';

describe('datetime utils', () => {
  it('formatDateTime retorna "-" para vazio', () => {
    expect(formatDateTime(null)).toBe('-');
  });

  it('formatDateTime devolve o valor original quando inválido', () => {
    expect(formatDateTime('não-é-data')).toBe('não-é-data');
  });

  it('formatDateTime formata datas válidas em pt-BR', () => {
    expect(formatDateTime('2026-05-20T10:00:00')).toMatch(/20\/05\/2026/);
  });

  it('toLocalIsoSeconds gera yyyy-MM-ddTHH:mm:ss', () => {
    expect(toLocalIsoSeconds(new Date(2026, 0, 5, 3, 4, 5))).toBe('2026-01-05T03:04:05');
  });

  it('normalizeTimestamp acrescenta segundos quando faltam', () => {
    expect(normalizeTimestamp('2026-05-20T10:00')).toBe('2026-05-20T10:00:00');
    expect(normalizeTimestamp('2026-05-20T10:00:30')).toBe('2026-05-20T10:00:30');
    expect(normalizeTimestamp('')).toBe('');
  });
});
