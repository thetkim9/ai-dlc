import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

export const api = axios.create({
  baseURL: BASE_URL,
})

api.interceptors.request.use((config) => {
  const session = getSession()
  if (session?.token) {
    config.headers.Authorization = `Bearer ${session.token}`
  }
  return config
})

const SESSION_KEY = 'table_session'

export interface SessionInfo {
  storeCode: string
  tableNumber: number
  password: string
  token: string
  sessionId: number
  tableId: number
}

export function getSession(): SessionInfo | null {
  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as SessionInfo
  } catch {
    return null
  }
}

export function saveSession(info: SessionInfo) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(info))
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY)
}
