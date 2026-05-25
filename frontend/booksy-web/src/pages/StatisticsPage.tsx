import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { clearSession, getSession } from '../api/auth'
import {
  getGuestStats,
  getOwnerStats,
  getAdminStats,
  GuestStatsResponse,
  OwnerStatsResponse,
  AdminStatsResponse,
  MonthlyEntry,
} from '../api/statistics'

export default function StatisticsPage() {
  const navigate = useNavigate()
  const session = getSession()
  const role = session?.role ?? 'GUEST'

  const [guestStats, setGuestStats] = useState<GuestStatsResponse | null>(null)
  const [ownerStats, setOwnerStats] = useState<OwnerStatsResponse | null>(null)
  const [adminStats, setAdminStats] = useState<AdminStatsResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    async function load() {
      const errors: string[] = []

      // Guest stats — every role has this
      await getGuestStats()
        .then(setGuestStats)
        .catch(() => errors.push('guest'))

      // Owner stats — only for OWNER role
      if (role === 'OWNER') {
        await getOwnerStats()
          .then(setOwnerStats)
          .catch(() => errors.push('owner'))
      }

      // Admin stats — only for ADMIN role
      if (role === 'ADMIN') {
        await getAdminStats()
          .then(setAdminStats)
          .catch(() => errors.push('admin'))
      }

      if (errors.length > 0) {
        setError(`Could not load some statistics (${errors.join(', ')}). The backend may need a restart.`)
      }

      setLoading(false)
    }
    load()
  }, [role])

  function handleLogout() {
    clearSession()
    navigate('/login')
  }

  const initials = (session?.email?.[0] ?? '?').toUpperCase()

  return (
    <div className="dash-shell">
      <nav className="dash-nav">
        <div className="dash-nav-logo">
          <span className="dash-nav-logo-dot" />
          Booksy
        </div>
        <div className="dash-nav-right">
          <Link to="/dashboard" className="btn-ghost" style={{ textDecoration: 'none' }}>← Dashboard</Link>
          <div className="user-chip">
            <div className="user-avatar">{initials}</div>
            <span className="user-email">{session?.email}</span>
          </div>
          <button className="btn-ghost" onClick={handleLogout}>Logout</button>
        </div>
      </nav>

      <main className="dash-main">
        <div className="page-header" style={{ marginBottom: 32 }}>
          <div>
            <h1 className="page-title">Statistics & Analytics</h1>
            <p className="page-subtitle">Overview of your activity on Booksy</p>
          </div>
        </div>

        {loading && (
          <div className="stats-loading">
            <div className="stats-spinner" />
            <p>Loading statistics…</p>
          </div>
        )}

        {!loading && error && (
          <div className="error-msg" style={{ marginBottom: 24 }}>{error}</div>
        )}

        {!loading && (
          <>
            {/* ─── ADMIN SECTION ─────────────────────────────────────────── */}
            {role === 'ADMIN' && adminStats && (
              <section className="stats-section">
                <p className="section-title">Platform Overview</p>
                <div className="stat-grid" style={{ marginBottom: 24 }}>
                  <KpiCard icon="👥" iconClass="icon-blue" label="Total Users" value={adminStats.totalUsers} />
                  <KpiCard icon="🏠" iconClass="icon-pink" label="Total Properties" value={adminStats.totalProperties} />
                  <KpiCard icon="🗓" iconClass="icon-green" label="Total Reservations" value={adminStats.totalReservations} />
                  <KpiCard icon="💰" iconClass="icon-yellow" label="Platform Revenue" value={`€${adminStats.platformRevenue.toFixed(2)}`} />
                </div>

                <div className="stats-row">
                  <div className="stats-card-wide">
                    <p className="stats-card-title">Users by Role</p>
                    <BarGroup items={[
                      { label: 'Guests', value: adminStats.totalGuests, max: adminStats.totalUsers, color: '#6366f1' },
                      { label: 'Owners', value: adminStats.totalOwners, max: adminStats.totalUsers, color: '#8b5cf6' },
                    ]} />
                  </div>

                  <div className="stats-card-wide">
                    <p className="stats-card-title">Properties by Status</p>
                    <BarGroup items={[
                      { label: 'Accepted', value: adminStats.acceptedProperties, max: adminStats.totalProperties || 1, color: '#10b981' },
                      { label: 'Pending', value: adminStats.pendingProperties, max: adminStats.totalProperties || 1, color: '#f59e0b' },
                      { label: 'Denied', value: adminStats.deniedProperties, max: adminStats.totalProperties || 1, color: '#ef4444' },
                    ]} />
                  </div>

                  <div className="stats-card-wide">
                    <p className="stats-card-title">Reservations by Status</p>
                    <BarGroup items={[
                      { label: 'Accepted', value: adminStats.acceptedReservations, max: adminStats.totalReservations || 1, color: '#10b981' },
                      { label: 'Pending', value: adminStats.pendingReservations, max: adminStats.totalReservations || 1, color: '#f59e0b' },
                    ]} />
                  </div>
                </div>

                {adminStats.monthlyReservations.length > 0 && (
                  <div className="stats-card-full" style={{ marginTop: 24 }}>
                    <p className="stats-card-title">Monthly Reservations (Platform)</p>
                    <MonthlyChart data={adminStats.monthlyReservations} valueKey="count" color="#6366f1" />
                  </div>
                )}
              </section>
            )}

            {/* ─── OWNER SECTION ─────────────────────────────────────────── */}
            {role === 'OWNER' && ownerStats && (
              <section className="stats-section">
                <p className="section-title">Property & Revenue Overview</p>
                <div className="stat-grid" style={{ marginBottom: 24 }}>
                  <KpiCard icon="🏠" iconClass="icon-pink" label="Your Properties" value={ownerStats.totalProperties} />
                  <KpiCard icon="🛏" iconClass="icon-blue" label="Total Rooms" value={ownerStats.totalRooms} />
                  <KpiCard icon="🗓" iconClass="icon-green" label="Incoming Reservations" value={ownerStats.totalReservations} />
                  <KpiCard icon="💰" iconClass="icon-yellow" label="Total Revenue" value={`€${ownerStats.totalRevenue.toFixed(2)}`} />
                </div>

                <div className="stats-row">
                  <div className="stats-card-wide">
                    <p className="stats-card-title">Incoming Reservations by Status</p>
                    <BarGroup items={[
                      { label: 'Accepted', value: ownerStats.acceptedCount, max: ownerStats.totalReservations || 1, color: '#10b981' },
                      { label: 'Pending', value: ownerStats.pendingCount, max: ownerStats.totalReservations || 1, color: '#f59e0b' },
                      { label: 'Denied', value: ownerStats.deniedCount, max: ownerStats.totalReservations || 1, color: '#ef4444' },
                      { label: 'Cancelled', value: ownerStats.cancelledCount, max: ownerStats.totalReservations || 1, color: '#94a3b8' },
                    ]} />
                  </div>
                </div>

                {ownerStats.monthlyRevenue.length > 0 && (
                  <div className="stats-card-full" style={{ marginTop: 24 }}>
                    <p className="stats-card-title">Monthly Revenue</p>
                    <MonthlyChart data={ownerStats.monthlyRevenue} valueKey="amount" color="#10b981" prefix="€" />
                  </div>
                )}
              </section>
            )}

            {/* ─── GUEST SECTION ─────────────────────────────────────────── */}
            {guestStats && (
              <section className="stats-section">
                <p className="section-title">{role !== 'GUEST' ? 'Your Guest Activity' : 'My Booking Summary'}</p>
                <div className="stat-grid" style={{ marginBottom: 24 }}>
                  <KpiCard icon="🗓" iconClass="icon-blue" label="Total Bookings" value={guestStats.totalReservations} />
                  <KpiCard icon="✅" iconClass="icon-green" label="Accepted" value={guestStats.acceptedCount} />
                  <KpiCard icon="⏳" iconClass="icon-yellow" label="Pending" value={guestStats.pendingCount} />
                  <KpiCard icon="💸" iconClass="icon-pink" label="Total Spent" value={`€${guestStats.totalSpent.toFixed(2)}`} />
                </div>

                <div className="stats-row">
                  <div className="stats-card-wide">
                    <p className="stats-card-title">Reservations by Status</p>
                    <BarGroup items={[
                      { label: 'Accepted', value: guestStats.acceptedCount, max: guestStats.totalReservations || 1, color: '#10b981' },
                      { label: 'Pending', value: guestStats.pendingCount, max: guestStats.totalReservations || 1, color: '#f59e0b' },
                      { label: 'Cancelled', value: guestStats.cancelledCount, max: guestStats.totalReservations || 1, color: '#94a3b8' },
                    ]} />
                  </div>
                </div>

                {guestStats.monthlyReservations.length > 0 && (
                  <div className="stats-card-full" style={{ marginTop: 24 }}>
                    <p className="stats-card-title">Monthly Bookings</p>
                    <MonthlyChart data={guestStats.monthlyReservations} valueKey="count" color="#6366f1" />
                  </div>
                )}

                {guestStats.totalReservations === 0 && (
                  <div className="empty-state">
                    No booking data yet.<br />
                    <Link to="/reservations" style={{ color: '#6366f1', fontWeight: 600 }}>Make your first reservation →</Link>
                  </div>
                )}
              </section>
            )}
          </>
        )}
      </main>
    </div>
  )
}

// ─── KPI CARD ─────────────────────────────────────────────────────────────────

function KpiCard({ icon, iconClass, label, value }: {
  icon: string; iconClass: string; label: string; value: number | string
}) {
  return (
    <div className="stat-card">
      <div className={`stat-icon ${iconClass}`}>{icon}</div>
      <p className="stat-label">{label}</p>
      <p className="stat-value">{value}</p>
    </div>
  )
}

// ─── BAR GROUP (horizontal bars) ─────────────────────────────────────────────

function BarGroup({ items }: {
  items: { label: string; value: number; max: number; color: string }[]
}) {
  return (
    <div className="bar-group">
      {items.map(item => {
        const pct = item.max > 0 ? Math.round((item.value / item.max) * 100) : 0
        return (
          <div key={item.label} className="bar-row">
            <span className="bar-label">{item.label}</span>
            <div className="bar-track">
              <div
                className="bar-fill"
                style={{ width: `${pct}%`, background: item.color }}
              />
            </div>
            <span className="bar-value">{item.value}</span>
          </div>
        )
      })}
    </div>
  )
}

// ─── MONTHLY CHART (vertical bars) ───────────────────────────────────────────

function MonthlyChart({ data, valueKey, color, prefix = '' }: {
  data: MonthlyEntry[]
  valueKey: 'count' | 'amount'
  color: string
  prefix?: string
}) {
  const values = data.map(d => valueKey === 'count' ? d.count : d.amount)
  const maxVal = Math.max(...values, 1)

  return (
    <div className="monthly-chart">
      {data.map((entry, i) => {
        const val = values[i]
        const heightPct = Math.round((val / maxVal) * 100)
        const label = entry.month.slice(0, 7) // "2025-01"
        const displayVal = valueKey === 'amount'
          ? `${prefix}${Number(val).toFixed(0)}`
          : String(val)

        return (
          <div key={entry.month} className="monthly-col">
            <span className="monthly-val">{displayVal}</span>
            <div className="monthly-bar-wrap">
              <div
                className="monthly-bar"
                style={{ height: `${heightPct}%`, background: color }}
              />
            </div>
            <span className="monthly-month">{label.slice(5)}/{label.slice(2, 4)}</span>
          </div>
        )
      })}
    </div>
  )
}
