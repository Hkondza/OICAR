import axios from './axiosInstance'

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

export async function getRoomsByProperty(propertyId: number): Promise<RoomResponse[]> {
    const res = await axios.get<RoomResponse[]>(`/rooms/property/${propertyId}`)
    return res.data
}

export async function createRoom(propertyId: number, data: RoomRequest): Promise<RoomResponse> {
    const res = await axios.post<RoomResponse>(`/rooms/property/${propertyId}`, data)
    return res.data
}

export async function updateRoom(id: number, data: RoomRequest): Promise<RoomResponse> {
    const res = await axios.put<RoomResponse>(`/rooms/${id}`, data)
    return res.data
}

export async function deleteRoom(id: number): Promise<void> {
    await axios.delete(`/rooms/${id}`)
}

export async function uploadRoomImages(id: number, files: File[]): Promise<string[]> {
    const formData = new FormData()
    files.forEach(file => formData.append('files', file))
    const res = await axios.post<string[]>(`/images/room/${id}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
    return res.data
}

export async function deleteRoomImage(id: number, imageUrl: string): Promise<void> {
    await axios.delete(`/images/room/${id}`, { params: { imageUrl } })
}