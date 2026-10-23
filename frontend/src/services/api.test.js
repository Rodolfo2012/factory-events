import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError, createEvent, listEvents } from './api.js';

function response(status, body) {
  return Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    json: () => (body === undefined ? Promise.reject(new Error('sem corpo')) : Promise.resolve(body))
  });
}

describe('api service', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('listEvents retorna o corpo da resposta', async () => {
    fetch.mockReturnValueOnce(response(200, [{ eventId: 'EVT-1' }]));
    await expect(listEvents()).resolves.toEqual([{ eventId: 'EVT-1' }]);
    expect(fetch.mock.calls[0][0]).toMatch(/\/api\/events$/);
  });

  it('createEvent envia POST com JSON', async () => {
    fetch.mockReturnValueOnce(response(201, { eventId: 'EVT-1' }));
    await createEvent({ eventId: 'EVT-1' });
    const [, options] = fetch.mock.calls[0];
    expect(options.method).toBe('POST');
    expect(JSON.parse(options.body)).toEqual({ eventId: 'EVT-1' });
  });

  it('lança ApiError com status e detalhes em respostas de erro', async () => {
    fetch.mockReturnValueOnce(response(400, { message: 'Payload inválido', details: ['eventId: obrigatório'] }));
    await expect(listEvents()).rejects.toMatchObject({
      name: 'Error',
      message: 'Payload inválido',
      status: 400,
      details: ['eventId: obrigatório']
    });
  });

  it('usa mensagem padrão quando o erro não tem corpo', async () => {
    fetch.mockReturnValueOnce(response(500));
    await expect(listEvents()).rejects.toThrow('Erro ao processar a requisição');
  });

  it('lança ApiError de conexão quando o fetch falha', async () => {
    fetch.mockRejectedValueOnce(new TypeError('network'));
    const error = await listEvents().catch((e) => e);
    expect(error).toBeInstanceOf(ApiError);
    expect(error.status).toBe(0);
  });
});
