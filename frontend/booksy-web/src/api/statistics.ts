import axios from 'axios'
import { getSession } from './auth'

const api = axios.create({ baseURL: '/api' })

function authHeaders() {
  const session = getSession()
  return { Authorization: `Bearer ${session?.token}` }
}

export interface MonthlyEntry {
  month: string      // "2025-01"
  count: number
  amount: number
}

export interface GuestStatsResponse {
  totalReservations: number
  pendingCount: number
  acceptedCount: number
  cancelledCount: number
  totalSpent: number
  monthlyReservations: MonthlyEntry[]
}

export interface OwnerStatsResponse {
  totalReservations: number
  pendingCount: number
  acceptedCount: number
  deniedCount: number
  cancelledCount: number
  totalRevenue: number
  totalProperties: number
  totalRooms: number
  monthlyRevenue: MonthlyEntry[]
}

export interface AdminStatsResponse {
  totalUsers: number
  totalGuests: number
  totalOwners: number
  totalProperties: number
  pendingProperties: number
  acceptedProperties: number
  deniedProperties: number
  totalReservations: number
  pendingReservations: number
  acceptedReservations: number
  platformRevenue: number
  monthlyReservations: MonthlyEntry[]
}

export async function getGuestStats(): Promise<GuestStatsResponse> {
  const res = await api.get<GuestStatsResponse>('/statistics/guest', { headers: authHeaders() })
  return res.data
}

export async function getOwnerStats(): Promise<OwnerStatsResponse> {
  const res = await api.get<OwnerStatsResponse>('/statistics/owner', { headers: authHeaders() })
  return res.data
}

export async function getAdminStats(): Promise<AdminStatsResponse> {
  const res = await api.get<AdminStatsResponse>('/statistics/admin', { headers: authHeaders() })
  return res.data
}
