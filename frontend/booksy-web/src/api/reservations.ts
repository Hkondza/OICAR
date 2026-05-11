import axios from 'axios'
import { getSession } from './auth'

export interface ReservationRequest {
  roomId: number
  checkIn: string
  checkOut: string
}

export interface ReservationResponse {
  id: number
  roomId: number
  roomName: string
  propertyName: string
  city: string
  checkIn: string
  checkOut: string
  totalPrice: number
  status: string
  createdAt: string
  guestEmail: string
}

function authHeaders() {
  const session = getSession()
  return { Authorization: `Bearer ${session?.token}` }
}

const api = axios.create({ baseURL: '/api' })

export async function getMyReservations(): Promise<ReservationResponse[]> {
  const res = await api.get<ReservationResponse[]>('/reservations/my', { headers: authHeaders() })
  return res.data
}

export async function getIncomingReservations(): Promise<ReservationResponse[]> {
  const res = await api.get<ReservationResponse[]>('/reservations/incoming', { headers: authHeaders() })
  return res.data
}

export async function createReservation(data: ReservationRequest): Promise<ReservationResponse> {
  const res = await api.post<ReservationResponse>('/reservations', data, { headers: authHeaders() })
  return res.data
}

export async function updateReservationStatus(id: number, status: string): Promise<ReservationResponse> {
  const res = await api.patch<ReservationResponse>(`/reservations/${id}/status`, { status }, { headers: authHeaders() })
  return res.data
}

export async function deleteReservation(id: number): Promise<void> {
  await api.delete(`/reservations/${id}`, { headers: authHeaders() })
}
