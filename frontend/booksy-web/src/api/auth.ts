import axios from 'axios'

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  role: string
  email: string
}

const api = axios.create({ baseURL: '/api' })

export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  password: string
  role: string
}

export async function login(data: LoginRequest): Promise<AuthResponse> {
  const res = await api.post<AuthResponse>('/auth/login', data)
  return res.data
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const res = await api.post<AuthResponse>('/auth/register', data)
  return res.data
}

export function saveSession(auth: AuthResponse) {
  localStorage.setItem('token', auth.token)
  localStorage.setItem('role', auth.role)
  localStorage.setItem('email', auth.email)
}

export function clearSession() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('email')
}

export function getSession(): { token: string; role: string; email: string } | null {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  const email = localStorage.getItem('email')
  if (!token || !role || !email) return null
  return { token, role, email }
}
