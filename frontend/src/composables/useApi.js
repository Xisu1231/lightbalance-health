const API_BASE = (import.meta.env.VITE_API_BASE || '').replace(/\/$/, '');

async function request(path, options = {}) {
  const target = API_BASE ? `${API_BASE}${path}` : path;
  const response = await fetch(target, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  });

  if (!response.ok) {
    let detail = `Request failed: ${response.status}`;
    try {
      const contentType = response.headers.get('content-type') || '';
      if (contentType.includes('application/json')) {
        const payload = await response.json();
        detail = payload.message || payload.error || JSON.stringify(payload);
      } else {
        const text = await response.text();
        if (text) {
          detail = text;
        }
      }
    } catch {
      // ignore parse errors and fall back to status-based message
    }
    throw new Error(detail);
  }

  return response.json();
}

export function useApi() {
  return {
    get: (path) => request(path),
    post: (path, body) =>
      request(path, {
        method: 'POST',
        body: body ? JSON.stringify(body) : undefined,
      }),
  };
}
