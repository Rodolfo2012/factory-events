import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import Alert from './Alert.jsx';

describe('Alert', () => {
  it('não renderiza nada sem título', () => {
    const { container } = render(<Alert type="error" />);
    expect(container).toBeEmptyDOMElement();
  });

  it('usa role=alert para erros e exibe detalhes', () => {
    render(<Alert type="error" title="Falha" details={['campo inválido']} onClose={() => {}} />);
    expect(screen.getByRole('alert')).toHaveTextContent('Falha');
    expect(screen.getByText('campo inválido')).toBeInTheDocument();
  });

  it('usa role=status para sucesso', () => {
    render(<Alert type="success" title="Ok" onClose={() => {}} />);
    expect(screen.getByRole('status')).toHaveTextContent('Ok');
  });

  it('chama onClose ao clicar em fechar', async () => {
    const onClose = vi.fn();
    render(<Alert type="success" title="Ok" onClose={onClose} />);
    await userEvent.click(screen.getByLabelText('Fechar mensagem'));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('chama onClose automaticamente após 4 segundos para alertas de sucesso e erro', () => {
    vi.useFakeTimers();
    const onCloseSuccess = vi.fn();
    const { unmount } = render(<Alert type="success" title="Ok" onClose={onCloseSuccess} />);

    expect(onCloseSuccess).not.toHaveBeenCalled();
    vi.advanceTimersByTime(4000);
    expect(onCloseSuccess).toHaveBeenCalledTimes(1);
    unmount();

    const onCloseError = vi.fn();
    render(<Alert type="error" title="Falha" onClose={onCloseError} />);
    expect(onCloseError).not.toHaveBeenCalled();
    vi.advanceTimersByTime(4000);
    expect(onCloseError).toHaveBeenCalledTimes(1);

    vi.useRealTimers();
  });
});
