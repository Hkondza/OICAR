import axios from './axiosInstance'

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
    imageUrl: string | null
}

export async function getAllProperties(): Promise<PropertyResponse[]> {
    const res = await axios.get<PropertyResponse[]>('/properties')
    return res.data
}

export async function getMyProperties(): Promise<PropertyResponse[]> {
    const res = await axios.get<PropertyResponse[]>('/properties/my')
    return res.data
}

export async function submitProperty(data: PropertyRequest): Promise<PropertyResponse> {
    const res = await axios.post<PropertyResponse>('/properties/submit', data)
    return res.data
}

export async function getPendingProperties(): Promise<PropertyResponse[]> {
    const res = await axios.get<PropertyResponse[]>('/properties/pending')
    return res.data
}

export async function approveProperty(id: number): Promise<PropertyResponse> {
    const res = await axios.patch<PropertyResponse>(`/properties/${id}/approve`, {})
    return res.data
}

export async function denyProperty(id: number): Promise<PropertyResponse> {
    const res = await axios.patch<PropertyResponse>(`/properties/${id}/deny`, {})
    return res.data
}

export async function uploadPropertyImage(id: number, file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)
    const res = await axios.post<string>(`/images/property/${id}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
    return res.data
}