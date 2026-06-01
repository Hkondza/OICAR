import axios from './axiosInstance'

export interface MonthlyEntry {
  month: string
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
  const res = await axios.get<GuestStatsResponse>('/statistics/guest')
  return res.data
}

export async function getOwnerStats(): Promise<OwnerStatsResponse> {
  const res = await axios.get<OwnerStatsResponse>('/statistics/owner')
  return res.data
}

export async function getAdminStats(): Promise<AdminStatsResponse> {
  const res = await axios.get<AdminStatsResponse>('/statistics/admin')
  return res.data
}