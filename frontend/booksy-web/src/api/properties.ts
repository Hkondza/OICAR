import axios from 'axios'
import { getSession } from './auth'

export interface PropertyRequest {
  name: string
  address: string
  city: string
  country: string
}

export interface PropertyResponse {
  id: number
  name: string
  address: string
  city: string
  country: string
  status: string
  ownerEmail: string
}

function authHeaders() {
  const session = getSession()
  return { Authorization: `Bearer ${session?.token}` }
}

const api = axios.create({ baseURL: '/api' })

export async function getAllProperties(): Promise<PropertyResponse[]> {
  const res = await api.get<PropertyResponse[]>('/properties', { headers: authHeaders() })
  return res.data
}

export async function getMyProperties(): Promise<PropertyResponse[]> {
  const res = await api.get<PropertyResponse[]>('/properties/my', { headers: authHeaders() })
  return res.data
}

export async function submitProperty(data: PropertyRequest): Promise<PropertyResponse> {
  const res = await api.post<PropertyResponse>('/properties/submit', data, { headers: authHeaders() })
  return res.data
}

export async function getPendingProperties(): Promise<PropertyResponse[]> {
  const res = await api.get<PropertyResponse[]>('/properties/pending', { headers: authHeaders() })
  return res.data
}

export async function approveProperty(id: number): Promise<PropertyResponse> {
  const res = await api.patch<PropertyResponse>(`/properties/${id}/approve`, {}, { headers: authHeaders() })
  return res.data
}

export async function denyProperty(id: number): Promise<PropertyResponse> {
  const res = await api.patch<PropertyResponse>(`/properties/${id}/deny`, {}, { headers: authHeaders() })
  return res.data
}
