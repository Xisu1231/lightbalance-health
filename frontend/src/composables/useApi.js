const API_BASE = (import.meta.env.VITE_API_BASE || '').replace(/\/$/, '');
const TOKEN_KEY = 'lightbalance_token';

function getToken() {
  return window.localStorage.getItem(TOKEN_KEY) || '';
}

function setToken(token) {
  if (token) {
    window.localStorage.setItem(TOKEN_KEY, token);
  } else {
    window.localStorage.removeItem(TOKEN_KEY);
  }
}

function clearToken() {
  window.localStorage.removeItem(TOKEN_KEY);
}

async function request(path, options = {}) {
  const target = API_BASE ? `${API_BASE}${path}` : path;
  const token = getToken();
  const response = await fetch(target, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
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

    const error = new Error(detail);
    error.status = response.status;
    throw error;
  }

  if (response.status === 204) {
    return null;
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
    delete: (path) =>
      request(path, {
        method: 'DELETE',
      }),
    getToken,
    setToken,
    clearToken,
  };
}
