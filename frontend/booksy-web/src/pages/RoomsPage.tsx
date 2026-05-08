import { useState, useEffect, FormEvent } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import { clearSession, getSession } from '../api/auth'
import { getRoomsByProperty, createRoom, deleteRoom, RoomResponse } from '../api/rooms'

export default function RoomsPage() {
  const navigate = useNavigate()
  const { propertyId } = useParams<{ propertyId: string }>()
  const location = useLocation()
  const propertyName = (location.state as { propertyName?: string })?.propertyName ?? 'Property'

  const session = getSession()
  const initials = (session?.email?.[0] ?? '?').toUpperCase()

  const [rooms, setRooms] = useState<RoomResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [deletingId, setDeletingId] = useState<number | null>(null)

  const [form, setForm] = useState({
    name: '',
    capacity: '',
    pricePerNight: '',
    availableFrom: '',
    availableTo: '',
  })
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  useEffect(() => {
    if (!propertyId) return
    getRoomsByProperty(Number(propertyId))
      .then(setRooms)
      .finally(() => setLoading(false))
  }, [propertyId])

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setFormError('')

    if (new Date(form.availableFrom) >= new Date(form.availableTo)) {
      setFormError('Available from must be before available to.')
      return
    }

    setSubmitting(true)
    try {
      const created = await createRoom(Number(propertyId), {
        name: form.name,
        capacity: Number(form.capacity),
        pricePerNight: Number(form.pricePerNight),
        availableFrom: form.availableFrom,
        availableTo: form.availableTo,
      })
      setRooms(prev => [...prev, created])
      setForm({ name: '', capacity: '', pricePerNight: '', availableFrom: '', availableTo: '' })
      setShowModal(false)
    } catch {
      setFormError('Failed to add room. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(id: number) {
    setDeletingId(id)
    try {
      await deleteRoom(id)
      setRooms(prev => prev.filter(r => r.id !== id))
    } finally {
      setDeletingId(null)
    }
  }

  function handleLogout() {
    clearSession()
    navigate('/login')
  }

  return (
    <div className="dash-shell">
      <nav className="dash-nav">
        <div className="dash-nav-logo">
          <span className="dash-nav-logo-dot" />
          Booksy
        </div>
        <div className="dash-nav-right">
          <button className="btn-ghost" onClick={() => navigate('/properties')}>My Properties</button>
          <div className="user-chip">
            <div className="user-avatar">{initials}</div>
            <span className="user-email">{session?.email}</span>
          </div>
          <button className="btn-ghost" onClick={handleLogout}>Logout</button>
        </div>
      </nav>

      <main className="dash-main">
        <div className="page-header">
          <div>
            <h1 className="page-title">{propertyName}</h1>
            <p className="page-subtitle">Add and manage rooms for this property.</p>
          </div>
          <button className="btn-primary btn-add" onClick={() => { setShowModal(true); setFormError('') }}>
            + Add Room
          </button>
        </div>

        {loading ? (
          <p className="empty-state">Loading...</p>
        ) : rooms.length === 0 ? (
          <div className="empty-state">
            <p>No rooms added yet.</p>
            <p>Click <strong>Add Room</strong> to get started.</p>
          </div>
        ) : (
          <div className="room-grid">
            {rooms.map(room => (
              <div key={room.id} className="room-card">
                <div className="room-card-header">
                  <span className="room-name">{room.name}</span>
                  <button
                    className="btn-delete"
                    onClick={() => handleDelete(room.id)}
                    disabled={deletingId === room.id}
                  >
                    {deletingId === room.id ? '…' : 'Delete'}
                  </button>
                </div>
                <div className="room-details">
                  <div className="room-detail">
                    <span className="room-detail-label">Capacity</span>
                    <span className="room-detail-value">{room.capacity} guests</span>
                  </div>
                  <div className="room-detail">
                    <span className="room-detail-label">Price per night</span>
                    <span className="room-detail-value">€{room.pricePerNight}</span>
                  </div>
                  <div className="room-detail">
                    <span className="room-detail-label">Available from</span>
                    <span className="room-detail-value">{room.availableFrom}</span>
                  </div>
                  <div className="room-detail">
                    <span className="room-detail-label">Available to</span>
                    <span className="room-detail-value">{room.availableTo}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">Add New Room</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <p className="modal-subtitle">Add a room to <strong>{propertyName}</strong>.</p>

            <form onSubmit={handleSubmit}>
              <div className="field">
                <label className="label">Room name</label>
                <input
                  className="input"
                  name="name"
                  placeholder="e.g. Deluxe Double Room"
                  value={form.name}
                  onChange={handleChange}
                  required
                  autoFocus
                />
              </div>

              <div className="field field-row">
                <div>
                  <label className="label">Capacity (guests)</label>
                  <input
                    className="input"
                    name="capacity"
                    type="number"
                    min="1"
                    placeholder="e.g. 2"
                    value={form.capacity}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div>
                  <label className="label">Price per night (€)</label>
                  <input
                    className="input"
                    name="pricePerNight"
                    type="number"
                    min="0"
                    step="0.01"
                    placeholder="e.g. 85.00"
                    value={form.pricePerNight}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              <div className="field field-row">
                <div>
                  <label className="label">Available from</label>
                  <input
                    className="input"
                    name="availableFrom"
                    type="date"
                    value={form.availableFrom}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div>
                  <label className="label">Available to</label>
                  <input
                    className="input"
                    name="availableTo"
                    type="date"
                    value={form.availableTo}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              {formError && <div className="error-msg">{formError}</div>}

              <div className="modal-actions">
                <button type="button" className="btn-ghost" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary btn-modal-submit" disabled={submitting}>
                  {submitting ? 'Adding…' : 'Add room'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
