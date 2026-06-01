import axios from 'axios'

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  refreshToken: string
  role: string
  email: string
  id: number
}

export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  password: string
  role: string
}

let refreshTokenMemory: string | null = null
const api = axios.create({ baseURL: '/api' })

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const res = await api.post<AuthResponse>('/auth/login', data)
  return res.data
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const res = await api.post<AuthResponse>('/auth/register', data)
  return res.data
}

export async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = refreshTokenMemory

  if (!refreshToken) return null
  try {
    const res = await api.post<AuthResponse>('/auth/refresh', { refreshToken })
    saveSession(res.data)
    return res.data.token
  } catch {
    clearSession()
    return null
  }
}

export async function logout() {
const refreshToken = refreshTokenMemory
  if (refreshToken) {
    try {
      await api.post('/auth/logout', { refreshToken })
    } catch {}
  }
  clearSession()
}

export function saveSession(auth: AuthResponse) {
  localStorage.setItem('token', auth.token)
  localStorage.setItem('refreshToken', auth.refreshToken)
  localStorage.setItem('role', auth.role)
  localStorage.setItem('email', auth.email)
  refreshTokenMemory = auth.refreshToken

  if (auth.id) localStorage.setItem('id', String(auth.id))
}

export function clearSession() {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('role')
  localStorage.removeItem('email')
  localStorage.removeItem('id')
  refreshTokenMemory = null

}

export function getSession() {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  const email = localStorage.getItem('email')
  if (!token || !role || !email) return null
  if (!refreshTokenMemory) {
    refreshTokenMemory = localStorage.getItem('refreshToken')
  }
  return { token, role, email }
}