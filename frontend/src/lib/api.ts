const ACCESS_TOKEN_KEY = 'macromind_access_token'
const REFRESH_TOKEN_KEY = 'macromind_refresh_token'

export function getToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function storeTokens(accessToken: string, refreshToken: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

export function clearTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

// Keep setToken/clearToken for backwards compat with AuthContext
export function setToken(t: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, t)
}

export function clearToken(): void {
  clearTokens()
}

let refreshing: Promise<boolean> | null = null

async function doRefresh(): Promise<boolean> {
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)
  if (!refreshToken) return false

  try {
    const res = await fetch('/api/v1/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    })
    if (!res.ok) return false
    const data: { accessToken: string; refreshToken: string } = await res.json()
    storeTokens(data.accessToken, data.refreshToken)
    return true
  } catch {
    return false
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken()

  const res = await fetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers ?? {}),
    },
  })

  if (res.status === 401) {
    if (!refreshing) {
      refreshing = doRefresh().finally(() => { refreshing = null })
    }
    const ok = await refreshing
    if (!ok) {
      clearTokens()
      window.location.href = '/login'
      throw new Error('401: Unauthorized')
    }

    const newToken = getToken()
    const retry = await fetch(path, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(newToken ? { Authorization: `Bearer ${newToken}` } : {}),
        ...(options.headers ?? {}),
      },
    })

    if (retry.status === 401) {
      clearTokens()
      window.location.href = '/login'
      throw new Error('401: Unauthorized')
    }

    if (!retry.ok) {
      const text = await retry.text().catch(() => retry.statusText)
      throw new Error(`${retry.status}: ${text}`)
    }

    if (retry.status === 204) return undefined as T
    return retry.json() as Promise<T>
  }

  if (!res.ok) {
    const text = await res.text().catch(() => res.statusText)
    throw new Error(`${res.status}: ${text}`)
  }

  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: body !== undefined ? JSON.stringify(body) : undefined }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PUT', body: body !== undefined ? JSON.stringify(body) : undefined }),
  patch: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'PATCH', body: body !== undefined ? JSON.stringify(body) : undefined }),
  delete: <T = void>(path: string) => request<T>(path, { method: 'DELETE' }),
}
