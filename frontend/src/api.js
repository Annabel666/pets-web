let csrfToken = "";
let sessionPromise = null;

export function loadSession() {
  sessionPromise = fetch("/api/session", { credentials: "same-origin", cache: "no-store" })
    .then((response) => response.json())
    .then((data) => {
      csrfToken = data.csrfToken || "";
      return data;
    })
    .catch((error) => {
      sessionPromise = null;
      throw error;
    });
  return sessionPromise;
}

async function ensureSession() {
  if (!csrfToken) {
    await (sessionPromise || loadSession());
  }
}

function resetSession() {
  csrfToken = "";
  sessionPromise = null;
}

async function request(url, options = {}, allowRetry = true) {
  await ensureSession();
  const headers = new Headers(options.headers || {});
  if (options.json) {
    headers.set("Content-Type", "application/json");
    headers.set("X-CSRF-Token", csrfToken);
  }
  const response = await fetch(url, {
    method: options.method || "GET",
    headers,
    body: options.json ? JSON.stringify(options.json) : undefined,
    credentials: "same-origin",
    cache: "no-store",
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    if (allowRetry && data.error === "页面已过期，请再试一次。") {
      resetSession();
      return request(url, options, false);
    }
    throw new Error(data.error || "请求没有完成。");
  }
  return data;
}

export function loadArchive() {
  return fetch("/api/archive", { credentials: "same-origin" }).then((response) => {
    if (!response.ok) {
      throw new Error("档案没有载入。");
    }
    return response.json();
  });
}

export function loadMessages(before) {
  const query = before ? `?before=${encodeURIComponent(before)}` : "";
  return fetch(`/api/messages${query}`, { credentials: "same-origin" }).then(async (response) => {
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new Error(data.error || "留言没有载入。");
    }
    return data;
  });
}

export function loadMine(before) {
  const query = before ? `?before=${encodeURIComponent(before)}` : "";
  return fetch(`/api/messages/mine${query}`, { credentials: "same-origin" }).then(async (response) => {
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new Error(data.error || "留言没有载入。");
    }
    return data;
  });
}

export function login(username, password) {
  return request("/api/login", { method: "POST", json: { username, password } });
}

export function register(username, password, confirm) {
  return request("/api/register", { method: "POST", json: { username, password, confirm } });
}

export async function logout() {
  const result = await request("/api/logout", { method: "POST", json: {} });
  resetSession();
  return result;
}

export function postMessage(content, parentId) {
  const json = { content };
  if (parentId) json.parentId = parentId;
  return request("/api/messages", { method: "POST", json });
}

export function agreeMessage(id) {
  return request(`/api/messages/${id}/agree`, { method: "POST", json: {} });
}

export function updateMessage(id, content) {
  return request(`/api/messages/${id}`, { method: "PUT", json: { content } });
}

export function deleteMessage(id) {
  return request(`/api/messages/${id}`, { method: "DELETE", json: {} });
}

export function loadListens() {
  return fetch("/api/listens", { credentials: "same-origin", cache: "no-store" }).then(async (response) => {
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new Error(data.error || "最近听过没有载入。");
    }
    return data;
  });
}

export function rememberListen(voiceId) {
  return request("/api/listens", { method: "POST", json: { voiceId } });
}
