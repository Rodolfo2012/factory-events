import { formatDateTime } from '../utils/datetime.js';

export default function EventTable({ events, loading, error, onRefresh }) {
  return (
    <section className="card" aria-label="Eventos registrados">
      <div className="card-header">
        <h2>Eventos registrados</h2>
        <button type="button" className="secondary" onClick={onRefresh} disabled={loading}>
          {loading ? 'Atualizando...' : 'Atualizar'}
        </button>
      </div>

      {error && <p className="muted error-text">{error}</p>}

      <div className="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Tipo</th>
              <th>Descrição</th>
              <th>Data/hora</th>
              <th>Recebido em</th>
            </tr>
          </thead>
          <tbody>
            {events.length === 0 && (
              <tr>
                <td colSpan={5} className="empty">
                  {loading ? 'Carregando...' : 'Nenhum evento registrado'}
                </td>
              </tr>
            )}
            {events.map((event) => (
              <tr key={event.eventId}>
                <td>{event.eventId}</td>
                <td>
                  <span className={`badge badge-${event.eventType.toLowerCase()}`}>{event.eventType}</span>
                </td>
                <td>{event.description}</td>
                <td>{formatDateTime(event.timestamp)}</td>
                <td>{formatDateTime(event.receivedAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
