import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App.jsx';

function jsonResponse(status, body) {
  return Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body)
  });
}

describe('App', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('lista os eventos retornados pela API', async () => {
    fetch.mockReturnValueOnce(
      jsonResponse(200, [
        {
          eventId: 'EVT-1',
          eventType: 'QUALITY',
          description: 'Defeito detectado',
          timestamp: '2026-05-20T10:00:00',
          receivedAt: '2026-05-20T13:00:00Z'
        }
      ])
    );

    render(<App />);

    expect(await screen.findByText('EVT-1')).toBeInTheDocument();
    expect(screen.getByText('Defeito detectado')).toBeInTheDocument();
  });

  it('exibe mensagem de sucesso ao registrar evento', async () => {
    fetch
      .mockReturnValueOnce(jsonResponse(200, []))
      .mockReturnValueOnce(jsonResponse(201, { eventId: 'EVT-9' }))
      .mockReturnValueOnce(jsonResponse(200, []));

    render(<App />);
    await screen.findByText('Nenhum evento registrado');

    await userEvent.type(screen.getByLabelText('ID do evento'), 'EVT-9');
    await userEvent.type(screen.getByLabelText('Descrição'), 'Linha iniciada');
    await userEvent.click(screen.getByRole('button', { name: 'Registrar evento' }));

    expect(await screen.findByText('Evento EVT-9 registrado com sucesso')).toBeInTheDocument();
    const [, postCall] = fetch.mock.calls[1];
    expect(JSON.parse(postCall.body)).toMatchObject({ eventId: 'EVT-9', eventType: 'PRODUCTION' });
  });

  it('exibe mensagem de erro quando o evento é duplicado', async () => {
    fetch
      .mockReturnValueOnce(jsonResponse(200, []))
      .mockReturnValueOnce(jsonResponse(409, { message: 'Evento já processado: EVT-9' }));

    render(<App />);
    await screen.findByText('Nenhum evento registrado');

    await userEvent.type(screen.getByLabelText('ID do evento'), 'EVT-9');
    await userEvent.type(screen.getByLabelText('Descrição'), 'Linha iniciada');
    await userEvent.click(screen.getByRole('button', { name: 'Registrar evento' }));

    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('Evento já processado: EVT-9'));
  });
});
