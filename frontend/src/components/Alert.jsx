import { useEffect } from 'react';

export default function Alert({ type, title, details = [], onClose, duration = 4000 }) {
  useEffect(() => {
    if (!duration || !onClose) return;
    const timer = setTimeout(onClose, duration);
    return () => clearTimeout(timer);
  }, [duration, onClose, title]);

  if (!title) return null;

  return (
    <div className={`alert alert-${type}`} role={type === 'error' ? 'alert' : 'status'}>
      <div className="alert-body">
        <strong>{title}</strong>
        {details.length > 0 && (
          <ul>
            {details.map((detail) => (
              <li key={detail}>{detail}</li>
            ))}
          </ul>
        )}
      </div>
      <button type="button" className="alert-close" aria-label="Fechar mensagem" onClick={onClose}>
        ×
      </button>
    </div>
  );
}
