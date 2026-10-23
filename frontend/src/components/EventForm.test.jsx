import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import EventForm from './EventForm.jsx';
import { createEvent } from '../services/api.js';

vi.mock('../services/api.js', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    createEvent: vi.fn()
  };
});

describe('EventForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  async function fill() {
    await userEvent.type(screen.getByLabelText('ID do evento'), '  EVT-1  ');
    await userEvent.type(screen.getByLabelText('Descrição'), ' Linha iniciada ');
  }

  it('envia o payload normalizado e chama onSuccess', async () => {
    const created = { eventId: 'EVT-1' };
    createEvent.mockResolvedValue(created);
    const onSuccess = vi.fn();

    render(<EventForm onSuccess={onSuccess} onError={vi.fn()} />);
    await fill();
    await userEvent.click(screen.getByRole('button', { name: 'Registrar evento' }));

    expect(createEvent).toHaveBeenCalledWith(
      expect.objectContaining({ eventId: 'EVT-1', description: 'Linha iniciada', eventType: 'PRODUCTION' })
    );
    expect(onSuccess).toHaveBeenCalledWith(created);
    expect(screen.getByLabelText('ID do evento')).toHaveValue('');
  });

  it('chama onError e mantém os dados quando a API falha', async () => {
    const error = new Error('duplicado');
    createEvent.mockRejectedValue(error);
    const onError = vi.fn();

    render(<EventForm onSuccess={vi.fn()} onError={onError} />);
    await fill();
    await userEvent.click(screen.getByRole('button', { name: 'Registrar evento' }));

    expect(onError).toHaveBeenCalledWith(error);
    expect(screen.getByLabelText('ID do evento')).toHaveValue('  EVT-1  ');
  });

  it('desabilita o botão enquanto envia', async () => {
    createEvent.mockReturnValue(new Promise(() => { }));

    render(<EventForm onSuccess={vi.fn()} onError={vi.fn()} />);
    await fill();
    await userEvent.click(screen.getByRole('button', { name: 'Registrar evento' }));

    expect(screen.getByRole('button', { name: 'Enviando...' })).toBeDisabled();
  });

  it('chama onError e impede envio quando o eventId ou descrição são vazios', async () => {
    const onError = vi.fn();
    render(<EventForm onSuccess={vi.fn()} onError={onError} />);

    await userEvent.click(screen.getByRole('button', { name: 'Registrar evento' }));
    expect(createEvent).not.toHaveBeenCalled();
    expect(onError).toHaveBeenCalledWith(expect.objectContaining({ message: 'eventId é obrigatório' }));

    await userEvent.type(screen.getByLabelText('ID do evento'), '   ""   ');
    await userEvent.click(screen.getByRole('button', { name: 'Registrar evento' }));
    expect(createEvent).not.toHaveBeenCalled();
  });
});
