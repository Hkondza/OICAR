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
    imageUrls: string[]
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

export async function uploadRoomImages(id: number, files: File[]): Promise<string[]> {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    const res = await api.post<string[]>(`/images/room/${id}`, formData, {
        headers: {
            ...authHeaders(),
            'Content-Type': 'multipart/form-data'
        }
    })
    return res.data
}

export async function deleteRoomImage(id: number, imageUrl: string): Promise<void> {
    await api.delete(`/images/room/${id}`, {
        headers: authHeaders(),
        params: { imageUrl }
    })
}