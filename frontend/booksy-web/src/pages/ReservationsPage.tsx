import { useState, useEffect, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { clearSession, getSession } from '../api/auth'
import {
  getMyReservations,
  getIncomingReservations,
  createReservation,
  updateReservationStatus,
  deleteReservation,
  ReservationResponse,
} from '../api/reservations'
import { getAllProperties, PropertyResponse } from '../api/properties'
import { getRoomsByProperty, RoomResponse } from '../api/rooms'

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'Pending',
  APPROVED: 'Approved',
  DENIED: 'Denied',
  CANCELLED: 'Cancelled',
}

const STATUS_CLASS: Record<string, string> = {
  PENDING: 'badge-pending',
  APPROVED: 'badge-accepted',
  DENIED: 'badge-denied',
  CANCELLED: 'badge-denied',
}

export default function ReservationsPage() {
  const navigate = useNavigate()
  const session = getSession()
  const initials = (session?.email?.[0] ?? '?').toUpperCase()
  const isOwner = session?.role === 'OWNER' || session?.role === 'ADMIN'

  const [activeTab, setActiveTab] = useState<'my' | 'incoming'>('my')
  const [myReservations, setMyReservations] = useState<ReservationResponse[]>([])
  const [incomingReservations, setIncomingReservations] = useState<ReservationResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [actionId, setActionId] = useState<number | null>(null)

  const [showModal, setShowModal] = useState(false)
  const [properties, setProperties] = useState<PropertyResponse[]>([])
  const [selectedPropertyId, setSelectedPropertyId] = useState('')
  const [rooms, setRooms] = useState<RoomResponse[]>([])
  const [roomsLoading, setRoomsLoading] = useState(false)
  const [form, setForm] = useState({ roomId: '', checkIn: '', checkOut: '' })
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  useEffect(() => {
    const fetches: Promise<void>[] = [
      getMyReservations().then(setMyReservations),
    ]
    if (isOwner) {
      fetches.push(getIncomingReservations().then(setIncomingReservations))
    }
    Promise.all(fetches).finally(() => setLoading(false))
  }, [isOwner])

  function openModal() {
    setShowModal(true)
    setFormError('')
    setSelectedPropertyId('')
    setRooms([])
    setForm({ roomId: '', checkIn: '', checkOut: '' })
    getAllProperties().then(setProperties)
  }

  async function handlePropertyChange(e: React.ChangeEvent<HTMLSelectElement>) {
    const propId = e.target.value
    setSelectedPropertyId(propId)
    setForm(prev => ({ ...prev, roomId: '' }))
    setRooms([])
    if (!propId) return
    setRoomsLoading(true)
    try {
      const data = await getRoomsByProperty(Number(propId))
      setRooms(data)
    } finally {
      setRoomsLoading(false)
    }
  }

  async function handleBook(e: FormEvent) {
    e.preventDefault()
    setFormError('')
    if (new Date(form.checkIn) >= new Date(form.checkOut)) {
      setFormError('Check-in must be before check-out.')
      return
    }
    setSubmitting(true)
    try {
      const created = await createReservation({
        roomId: Number(form.roomId),
        checkIn: form.checkIn,
        checkOut: form.checkOut,
      })
      setMyReservations(prev => [created, ...prev])
      setShowModal(false)
    } catch {
      setFormError('Failed to create reservation. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleCancel(id: number) {
    setActionId(id)
    try {
      const updated = await updateReservationStatus(id, 'CANCELLED')
      setMyReservations(prev => prev.map(r => r.id === id ? updated : r))
    } finally {
      setActionId(null)
    }
  }

  async function handleDelete(id: number) {
    setActionId(id)
    try {
      await deleteReservation(id)
      setMyReservations(prev => prev.filter(r => r.id !== id))
    } finally {
      setActionId(null)
    }
  }

  async function handleApprove(id: number) {
    setActionId(id)
    try {
      const updated = await updateReservationStatus(id, 'APPROVED')
      setIncomingReservations(prev => prev.map(r => r.id === id ? updated : r))
    } finally {
      setActionId(null)
    }
  }

  async function handleDeny(id: number) {
    setActionId(id)
    try {
      const updated = await updateReservationStatus(id, 'DENIED')
      setIncomingReservations(prev => prev.map(r => r.id === id ? updated : r))
    } finally {
      setActionId(null)
    }
  }

  function handleLogout() {
    clearSession()
    navigate('/login')
  }

  const displayed = activeTab === 'my' ? myReservations : incomingReservations

  return (
    <div className="dash-shell">
      <nav className="dash-nav">
        <div className="dash-nav-logo">
          <span className="dash-nav-logo-dot" />
          Booksy
        </div>
        <div className="dash-nav-right">
          <button className="btn-ghost" onClick={() => navigate('/dashboard')}>Dashboard</button>
          {isOwner && (
            <button className="btn-ghost" onClick={() => navigate('/properties')}>My Properties</button>
          )}
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
            <h1 className="page-title">Reservations</h1>
            <p className="page-subtitle">View and manage your bookings.</p>
          </div>
          {activeTab === 'my' && (
            <button className="btn-primary btn-add" onClick={openModal}>
              + New Booking
            </button>
          )}
        </div>

        {isOwner && (
          <div className="res-tabs">
            <button
              className={`res-tab ${activeTab === 'my' ? 'res-tab-active' : ''}`}
              onClick={() => setActiveTab('my')}
            >
              My Bookings
            </button>
            <button
              className={`res-tab ${activeTab === 'incoming' ? 'res-tab-active' : ''}`}
              onClick={() => setActiveTab('incoming')}
            >
              Incoming Reservations
            </button>
          </div>
        )}

        {loading ? (
          <p className="empty-state">Loading...</p>
        ) : displayed.length === 0 ? (
          <div className="empty-state">
            <p>No reservations found.</p>
            {activeTab === 'my' && <p>Click <strong>New Booking</strong> to make a reservation.</p>}
          </div>
        ) : (
          <div className="admin-list">
            {displayed.map(r => (
              <div key={r.id} className="admin-card">
                <div className="admin-card-info">
                  <div className="admin-card-name">
                    {r.roomName} — {r.propertyName}
                    <span className={`status-badge ${STATUS_CLASS[r.status] ?? ''}`}>
                      {STATUS_LABELS[r.status] ?? r.status}
                    </span>
                  </div>
                  <div className="admin-card-meta">{r.city}</div>
                  <div className="admin-card-meta">
                    {r.checkIn} → {r.checkOut} &nbsp;·&nbsp; €{r.totalPrice}
                  </div>
                  {activeTab === 'incoming' && (
                    <div className="admin-card-owner">Guest: {r.guestEmail}</div>
                  )}
                </div>
                <div className="admin-card-actions">
                  {activeTab === 'my' && r.status === 'PENDING' && (
                    <button
                      className="btn-deny"
                      onClick={() => handleCancel(r.id)}
                      disabled={actionId === r.id}
                    >
                      {actionId === r.id ? '…' : 'Cancel'}
                    </button>
                  )}
                  {activeTab === 'my' && r.status === 'CANCELLED' && (
                    <button
                      className="btn-delete"
                      onClick={() => handleDelete(r.id)}
                      disabled={actionId === r.id}
                    >
                      {actionId === r.id ? '…' : 'Delete'}
                    </button>
                  )}
                  {activeTab === 'incoming' && r.status === 'PENDING' && (
                    <>
                      <button
                        className="btn-approve"
                        onClick={() => handleApprove(r.id)}
                        disabled={actionId === r.id}
                      >
                        {actionId === r.id ? '…' : 'Approve'}
                      </button>
                      <button
                        className="btn-deny"
                        onClick={() => handleDeny(r.id)}
                        disabled={actionId === r.id}
                      >
                        {actionId === r.id ? '…' : 'Deny'}
                      </button>
                    </>
                  )}
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
              <h2 className="modal-title">New Booking</h2>
              <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
            </div>
            <p className="modal-subtitle">Select a property and room, then choose your dates.</p>

            <form onSubmit={handleBook}>
              <div className="field">
                <label className="label">Property</label>
                <select
                  className="input select"
                  value={selectedPropertyId}
                  onChange={handlePropertyChange}
                  required
                >
                  <option value="">— Select a property —</option>
                  {properties.map(p => (
                    <option key={p.id} value={p.id}>{p.name} · {p.city}</option>
                  ))}
                </select>
              </div>

              <div className="field">
                <label className="label">Room</label>
                <select
                  className="input select"
                  value={form.roomId}
                  onChange={e => setForm(prev => ({ ...prev, roomId: e.target.value }))}
                  required
                  disabled={!selectedPropertyId || roomsLoading}
                >
                  <option value="">
                    {!selectedPropertyId
                      ? '— Select a property first —'
                      : roomsLoading
                      ? 'Loading rooms…'
                      : rooms.length === 0
                      ? 'No rooms available'
                      : '— Select a room —'}
                  </option>
                  {rooms.map(r => (
                    <option key={r.id} value={r.id}>
                      {r.name} · {r.capacity} guests · €{r.pricePerNight}/night
                    </option>
                  ))}
                </select>
              </div>

              <div className="field field-row">
                <div>
                  <label className="label">Check-in</label>
                  <input
                    className="input"
                    name="checkIn"
                    type="date"
                    value={form.checkIn}
                    onChange={e => setForm(prev => ({ ...prev, checkIn: e.target.value }))}
                    required
                  />
                </div>
                <div>
                  <label className="label">Check-out</label>
                  <input
                    className="input"
                    name="checkOut"
                    type="date"
                    value={form.checkOut}
                    onChange={e => setForm(prev => ({ ...prev, checkOut: e.target.value }))}
                    required
                  />
                </div>
              </div>

              {formError && <div className="error-msg">{formError}</div>}

              <div className="modal-actions">
                <button type="button" className="btn-ghost" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary btn-modal-submit" disabled={submitting}>
                  {submitting ? 'Booking…' : 'Book'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
