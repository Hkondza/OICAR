import { useEffect, useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { clearSession, getSession } from '../api/auth'
import { getGuestStats, getOwnerStats, GuestStatsResponse, OwnerStatsResponse } from '../api/statistics'

export default function DashboardPage() {
  const navigate = useNavigate()
  const session = getSession()
  const role = session?.role ?? 'GUEST'

  const [guestStats, setGuestStats] = useState<GuestStatsResponse | null>(null)
  const [ownerStats, setOwnerStats] = useState<OwnerStatsResponse | null>(null)

  useEffect(() => {
    getGuestStats().then(setGuestStats).catch(() => {})
    if (role === 'OWNER') {
      getOwnerStats().then(setOwnerStats).catch(() => {})
    }
  }, [role])

  function handleLogout() {
    clearSession()
    navigate('/login')
  }

  const roleLabel = role === 'ADMIN' ? 'Administrator' : role === 'OWNER' ? 'Owner' : 'Guest'
  const initials = (session?.email?.[0] ?? '?').toUpperCase()

  return (
    <div className="dash-shell">
      <nav className="dash-nav">
        <div className="dash-nav-logo">
          <span className="dash-nav-logo-dot" />
          Booksy
        </div>
        <div className="dash-nav-right">
          <div className="user-chip">
            <div className="user-avatar">{initials}</div>
            <span className="user-email">{session?.email}</span>
          </div>
          <button className="btn-ghost" onClick={handleLogout}>Logout</button>
        </div>
      </nav>

      <main className="dash-main">
        <section className="hero-card">
          <p className="hero-greet">Welcome back 👋</p>
          <h1 className="hero-title">Hello, {session?.email?.split('@')[0]}!</h1>
          <p className="hero-subtitle">
            This is your Booksy dashboard. From here you'll manage reservations, browse properties, and keep track of your reviews.
          </p>
          <span className="role-badge">● {roleLabel}</span>
        </section>

        <p className="section-title">Overview</p>
        <div className="stat-grid">
          <Link to="/reservations" style={{ textDecoration: 'none' }}>
            <StatCard
              icon="🗓"
              iconClass="icon-blue"
              label="My Reservations"
              value={guestStats ? String(guestStats.totalReservations) : '…'}
              description="View and manage your bookings"
            />
          </Link>

          <Link to="/properties" style={{ textDecoration: 'none' }}>
            <StatCard
              icon="🏠"
              iconClass="icon-pink"
              label={role === 'GUEST' ? 'Properties' : 'My Properties'}
              value={ownerStats ? String(ownerStats.totalProperties) : '—'}
              description="Manage your listings and submissions"
            />
          </Link>

          {role === 'OWNER' && (
            <Link to="/reservations" style={{ textDecoration: 'none' }}>
              <StatCard
                icon="📥"
                iconClass="icon-green"
                label="Incoming Reservations"
                value={ownerStats ? String(ownerStats.totalReservations) : '…'}
                description="Reservations for your properties"
              />
            </Link>
          )}

          {role === 'OWNER' && (
            <StatCard
              icon="💰"
              iconClass="icon-yellow"
              label="Total Revenue"
              value={ownerStats ? `€${ownerStats.totalRevenue.toFixed(2)}` : '…'}
              description="Revenue from accepted bookings"
            />
          )}

          {role === 'GUEST' && (
            <StatCard
              icon="💸"
              iconClass="icon-yellow"
              label="Total Spent"
              value={guestStats ? `€${guestStats.totalSpent.toFixed(2)}` : '…'}
              description="Spent on accepted bookings"
            />
          )}

          <Link to="/statistics" style={{ textDecoration: 'none' }}>
            <StatCard
              icon="📊"
              iconClass="icon-purple"
              label="Analytics"
              value="→"
              description="View detailed statistics and charts"
            />
          </Link>

          {role === 'ADMIN' && (
            <Link to="/admin" style={{ textDecoration: 'none' }}>
              <StatCard
                icon="🛡"
                iconClass="icon-blue"
                label="Admin Panel"
                value="→"
                description="Approve or deny property submissions"
              />
            </Link>
          )}
        </div>
      </main>
    </div>
  )
}

function StatCard({
  icon, iconClass, label, value, description,
}: {
  icon: string; iconClass: string; label: string; value: string; description: string
}) {
  return (
    <div className="stat-card">
      <div className={`stat-icon ${iconClass}`}>{icon}</div>
      <p className="stat-label">{label}</p>
      <p className="stat-value">{value}</p>
      <p className="stat-desc">{description}</p>
    </div>
  )
}
