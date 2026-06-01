import axios from 'axios'
import { refreshAccessToken, getSession, clearSession } from './auth'

const axiosInstance = axios.create({ baseURL: '/api' })

// Dodaj token na svaki request
axiosInstance.interceptors.request.use((config) => {
  const session = getSession()
  if (session?.token) {
    config.headers.Authorization = `Bearer ${session.token}`
  }
  return config
})

// Ako dobijemo 401 - pokušaj refresh
let isRefreshing = false
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: unknown) => void }> = []

function processQueue(error: unknown, token: string | null) {
  failedQueue.forEach(({ resolve, reject }) => {
    if (token) resolve(token)
    else reject(error)
  })
  failedQueue = []
}

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if ((error.response?.status === 401 || error.response?.status === 403) && !originalRequest._retry)  {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return axiosInstance(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      console.log('Attempting refresh...')

      const newToken = await refreshAccessToken()
      console.log('New token:', newToken)

      isRefreshing = false

      if (newToken) {
        processQueue(null, newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return axiosInstance(originalRequest)
      } else {
        processQueue(error, null)
        clearSession()
        window.location.href = '/login'
        return Promise.reject(error)
      }
    }

    return Promise.reject(error)
  }
)

export default axiosInstance