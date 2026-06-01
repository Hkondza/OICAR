import axios from './axiosInstance'

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

export async function getMyReservations(): Promise<ReservationResponse[]> {
  const res = await axios.get<ReservationResponse[]>('/reservations/my')
  return res.data
}

export async function getIncomingReservations(): Promise<ReservationResponse[]> {
  const res = await axios.get<ReservationResponse[]>('/reservations/incoming')
  return res.data
}

export async function createReservation(data: ReservationRequest): Promise<ReservationResponse> {
  const res = await axios.post<ReservationResponse>('/reservations', data)
  return res.data
}

export async function updateReservationStatus(id: number, status: string): Promise<ReservationResponse> {
  const res = await axios.patch<ReservationResponse>(`/reservations/${id}/status`, { status })
  return res.data
}

export async function deleteReservation(id: number): Promise<void> {
  await axios.delete(`/reservations/${id}`)
}