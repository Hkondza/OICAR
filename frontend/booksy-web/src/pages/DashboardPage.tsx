import { useNavigate } from 'react-router-dom'
import { clearSession, getSession } from '../api/auth'

export default function DashboardPage() {
  const navigate = useNavigate()
  const session = getSession()

  function handleLogout() {
    clearSession()
    navigate('/login')
  }

  const roleLabel = session?.role === 'ADMIN' ? 'Administrator' : session?.role === 'HOST' ? 'Host' : 'Guest'
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
          <StatCard
            icon="🗓"
            iconClass="icon-blue"
            label="My Reservations"
            value="—"
            description="View and manage your bookings"
          />
          <StatCard
            icon="🏠"
            iconClass="icon-pink"
            label="Properties"
            value="—"
            description="Browse available stays"
          />
          <StatCard
            icon="⭐"
            iconClass="icon-green"
            label="Reviews"
            value="—"
            description="Your reviews and ratings"
          />
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
