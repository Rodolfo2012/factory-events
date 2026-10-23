import { useState } from 'react';
import { EVENT_TYPES } from '../constants.js';
import { ApiError, createEvent } from '../services/api.js';
import { normalizeTimestamp, toLocalIsoSeconds } from '../utils/datetime.js';

function initialForm() {
  return {
    eventId: '',
    eventType: EVENT_TYPES[0],
    description: '',
    timestamp: toLocalIsoSeconds().slice(0, 16)
  };
}

export default function EventForm({ onSuccess, onError }) {
  const [form, setForm] = useState(initialForm);
  const [submitting, setSubmitting] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const cleanId = form.eventId.replace(/^"+|"+$/g, '').trim();
    const cleanDescription = form.description.replace(/^"+|"+$/g, '').trim();

    if (!cleanId) {
      onError(new ApiError('eventId é obrigatório', 400, ['eventId é obrigatório']));
      return;
    }

    if (!cleanDescription) {
      onError(new ApiError('description é obrigatório', 400, ['description é obrigatório']));
      return;
    }

    setSubmitting(true);
    try {
      const created = await createEvent({
        ...form,
        eventId: cleanId,
        description: cleanDescription,
        timestamp: normalizeTimestamp(form.timestamp)
      });
      setForm(initialForm());
      onSuccess(created);
    } catch (err) {
      onError(err);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="card form" onSubmit={handleSubmit} noValidate aria-label="Cadastro de evento">
      <h2>Novo evento</h2>

      <label htmlFor="eventId">ID do evento</label>
      <input
        id="eventId"
        name="eventId"
        value={form.eventId}
        onChange={handleChange}
        placeholder="EVT-1001"
        maxLength={100}
        required
      />

      <label htmlFor="eventType">Tipo</label>
      <select id="eventType" name="eventType" value={form.eventType} onChange={handleChange}>
        {EVENT_TYPES.map((type) => (
          <option key={type} value={type}>
            {type}
          </option>
        ))}
      </select>

      <label htmlFor="timestamp">Data/hora</label>
      <input
        id="timestamp"
        name="timestamp"
        type="datetime-local"
        step="1"
        value={form.timestamp}
        onChange={handleChange}
        required
      />

      <label htmlFor="description">Descrição</label>
      <textarea
        id="description"
        name="description"
        value={form.description}
        onChange={handleChange}
        placeholder="Production line started"
        maxLength={500}
        rows={4}
        required
      />

      <button type="submit" disabled={submitting}>
        {submitting ? 'Enviando...' : 'Registrar evento'}
      </button>
    </form>
  );
}
