import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { clearSession, getSession } from '../api/auth'
import { getPendingProperties, approveProperty, denyProperty, PropertyResponse } from '../api/properties'

export default function AdminPage() {
  const navigate = useNavigate()
  const session = getSession()
  const initials = (session?.email?.[0] ?? '?').toUpperCase()

  const [properties, setProperties] = useState<PropertyResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [actionId, setActionId] = useState<number | null>(null)

  useEffect(() => {
    getPendingProperties()
      .then(setProperties)
      .finally(() => setLoading(false))
  }, [])

  async function handleApprove(id: number) {
    setActionId(id)
    try {
      await approveProperty(id)
      setProperties(prev => prev.filter(p => p.id !== id))
    } finally {
      setActionId(null)
    }
  }

  async function handleDeny(id: number) {
    setActionId(id)
    try {
      await denyProperty(id)
      setProperties(prev => prev.filter(p => p.id !== id))
    } finally {
      setActionId(null)
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
          Booksy Admin
        </div>
        <div className="dash-nav-right">
          <div className="user-chip">
            <div className="user-avatar admin-avatar">{initials}</div>
            <span className="user-email">{session?.email}</span>
          </div>
          <button className="btn-ghost" onClick={handleLogout}>Logout</button>
        </div>
      </nav>

      <main className="dash-main">
        <div className="admin-hero">
          <div>
            <h1 className="page-title">Property Approvals</h1>
            <p className="page-subtitle">Review and approve or deny property submissions from owners.</p>
          </div>
          <span className="admin-badge">Administrator</span>
        </div>

        {loading ? (
          <p className="empty-state">Loading...</p>
        ) : properties.length === 0 ? (
          <div className="empty-state">
            <p>No pending properties.</p>
            <p>All submissions have been reviewed.</p>
          </div>
        ) : (
          <>
            <p className="section-title">{properties.length} pending {properties.length === 1 ? 'submission' : 'submissions'}</p>
            <div className="admin-list">
              {properties.map(p => (
                <div key={p.id} className="admin-card">
                  <div className="admin-card-info">
                    <div className="admin-card-name">{p.name}</div>
                    <div className="admin-card-meta">{p.address}, {p.city}, {p.country}</div>
                    <div className="admin-card-owner">Submitted by: {p.ownerEmail}</div>
                  </div>
                  <div className="admin-card-actions">
                    <button
                      className="btn-approve"
                      onClick={() => handleApprove(p.id)}
                      disabled={actionId === p.id}
                    >
                      {actionId === p.id ? '…' : 'Approve'}
                    </button>
                    <button
                      className="btn-deny"
                      onClick={() => handleDeny(p.id)}
                      disabled={actionId === p.id}
                    >
                      {actionId === p.id ? '…' : 'Deny'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </main>
    </div>
  )
}
