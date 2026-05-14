import axios from 'axios'
import { getSession } from './auth'

export interface RoomRequest {
  name: string
  capacity: number
  pricePerNight: number
  availableFrom: string
  availableTo: string
}

export interface RoomResponse {
  id: number
  name: string
  capacity: number
  pricePerNight: number
  availableFrom: string
  availableTo: string
  propertyId: number
  propertyName: string
  city: string
}

function authHeaders() {
  const session = getSession()
  return { Authorization: `Bearer ${session?.token}` }
}

const api = axios.create({ baseURL: '/api' })

export async function getRoomsByProperty(propertyId: number): Promise<RoomResponse[]> {
  const res = await api.get<RoomResponse[]>(`/rooms/property/${propertyId}`, { headers: authHeaders() })
  return res.data
}

export async function createRoom(propertyId: number, data: RoomRequest): Promise<RoomResponse> {
  const res = await api.post<RoomResponse>(`/rooms/property/${propertyId}`, data, { headers: authHeaders() })
  return res.data
}

export async function updateRoom(id: number, data: RoomRequest): Promise<RoomResponse> {
  const res = await api.put<RoomResponse>(`/rooms/${id}`, data, { headers: authHeaders() })
  return res.data
}

export async function deleteRoom(id: number): Promise<void> {
  await api.delete(`/rooms/${id}`, { headers: authHeaders() })
}
