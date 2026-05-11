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

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
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
      setForm({ roomId: '', checkIn: '', checkOut: '' })
      setShowModal(false)
    } catch {
      setFormError('Failed to create reservation. Check the room ID and dates.')
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
            <button className="btn-primary btn-add" onClick={() => { setShowModal(true); setFormError('') }}>
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
            <p className="modal-subtitle">Enter the room ID and your dates.</p>

            <form onSubmit={handleBook}>
              <div className="field">
                <label className="label">Room ID</label>
                <input
                  className="input"
                  name="roomId"
                  type="number"
                  min="1"
                  placeholder="e.g. 3"
                  value={form.roomId}
                  onChange={handleChange}
                  required
                  autoFocus
                />
              </div>

              <div className="field field-row">
                <div>
                  <label className="label">Check-in</label>
                  <input
                    className="input"
                    name="checkIn"
                    type="date"
                    value={form.checkIn}
                    onChange={handleChange}
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
                    onChange={handleChange}
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
