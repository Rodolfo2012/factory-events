import { renderHook, waitFor, act } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useEvents } from './useEvents.js';
import { listEvents } from '../services/api.js';

vi.mock('../services/api.js', () => ({ listEvents: vi.fn() }));

describe('useEvents', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('carrega eventos ao montar', async () => {
    listEvents.mockResolvedValue([{ eventId: 'EVT-1' }]);

    const { result } = renderHook(() => useEvents());
    expect(result.current.loading).toBe(true);

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.events).toEqual([{ eventId: 'EVT-1' }]);
    expect(result.current.error).toBeNull();
  });

  it('expõe a mensagem de erro quando a API falha', async () => {
    listEvents.mockRejectedValue(new Error('Servidor indisponível'));

    const { result } = renderHook(() => useEvents());

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.error).toBe('Servidor indisponível');
    expect(result.current.events).toEqual([]);
  });

  it('refresh recarrega os dados', async () => {
    listEvents.mockResolvedValueOnce([]).mockResolvedValueOnce([{ eventId: 'EVT-2' }]);

    const { result } = renderHook(() => useEvents());
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => {
      await result.current.refresh();
    });

    expect(result.current.events).toEqual([{ eventId: 'EVT-2' }]);
    expect(listEvents).toHaveBeenCalledTimes(2);
  });
});
