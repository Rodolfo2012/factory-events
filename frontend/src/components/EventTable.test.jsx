import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import EventTable from './EventTable.jsx';

const sample = {
  eventId: 'EVT-1',
  eventType: 'QUALITY',
  description: 'Defeito detectado',
  timestamp: '2026-05-20T10:00:00',
  receivedAt: '2026-05-20T13:00:00Z'
};

describe('EventTable', () => {
  it('mostra estado vazio', () => {
    render(<EventTable events={[]} loading={false} error={null} onRefresh={() => {}} />);
    expect(screen.getByText('Nenhum evento registrado')).toBeInTheDocument();
  });

  it('mostra carregando', () => {
    render(<EventTable events={[]} loading error={null} onRefresh={() => {}} />);
    expect(screen.getByText('Carregando...')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Atualizando...' })).toBeDisabled();
  });

  it('renderiza as linhas e o badge do tipo', () => {
    render(<EventTable events={[sample]} loading={false} error={null} onRefresh={() => {}} />);
    expect(screen.getByText('EVT-1')).toBeInTheDocument();
    expect(screen.getByText('QUALITY')).toHaveClass('badge-quality');
  });

  it('exibe erro e dispara onRefresh', async () => {
    const onRefresh = vi.fn();
    render(<EventTable events={[]} loading={false} error="Falha de rede" onRefresh={onRefresh} />);
    expect(screen.getByText('Falha de rede')).toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'Atualizar' }));
    expect(onRefresh).toHaveBeenCalledTimes(1);
  });
});
