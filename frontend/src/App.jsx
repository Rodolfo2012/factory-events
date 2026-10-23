import { useState } from 'react';
import Alert from './components/Alert.jsx';
import EventForm from './components/EventForm.jsx';
import EventTable from './components/EventTable.jsx';
import { useEvents } from './hooks/useEvents.js';

export default function App() {
  const { events, loading, error, refresh } = useEvents();
  const [feedback, setFeedback] = useState(null);

  function handleSuccess(created) {
    setFeedback({ type: 'success', title: `Evento ${created.eventId} registrado com sucesso` });
    refresh();
  }

  function handleError(err) {
    setFeedback({ type: 'error', title: err.message, details: err.details });
  }

  return (
    <main className="container">
      <header className="header">
        <h1>Eventos da Fábrica</h1>
        <p className="muted">Registro e consulta de eventos enviados pelos equipamentos</p>
      </header>

      {feedback && <Alert {...feedback} onClose={() => setFeedback(null)} />}

      <div className="layout">
        <EventForm onSuccess={handleSuccess} onError={handleError} />
        <EventTable events={events} loading={loading} error={error} onRefresh={refresh} />
      </div>
    </main>
  );
}
