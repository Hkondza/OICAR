import { useState, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { register, saveSession } from '../api/auth'

export default function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'GUEST',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setLoading(true)
    try {
      const auth = await register({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
        role: form.role,
      })
      saveSession(auth)
      navigate('/dashboard')
    } catch {
      setError('Registration failed. Email may already be in use.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-shell">
      <aside className="auth-brand">
        <div className="brand-logo">
          <span className="brand-logo-dot" />
          Booksy
        </div>
        <div className="brand-tagline">
          <h2>Join thousands of travelers and hosts.</h2>
          <p>Create an account in seconds. Book your next adventure or list your property and start earning today.</p>
        </div>
        <div className="brand-footer">© 2026 Booksy. Team 16.</div>
      </aside>

      <main className="auth-form-side">
        <div className="auth-card">
          <h1 className="auth-title">Create your account</h1>
          <p className="auth-subtitle">Start your journey with Booksy.</p>

          <form onSubmit={handleSubmit}>
            <div className="field">
              <div className="field-row">
                <div>
                  <label className="label">First name</label>
                  <input
                    className="input"
                    name="firstName"
                    placeholder="John"
                    value={form.firstName}
                    onChange={handleChange}
                    required
                    autoFocus
                  />
                </div>
                <div>
                  <label className="label">Last name</label>
                  <input
                    className="input"
                    name="lastName"
                    placeholder="Doe"
                    value={form.lastName}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>
            </div>

            <div className="field">
              <label className="label">Email</label>
              <input
                className="input"
                name="email"
                type="email"
                placeholder="you@example.com"
                value={form.email}
                onChange={handleChange}
                required
              />
            </div>

            <div className="field">
              <div className="field-row">
                <div>
                  <label className="label">Password</label>
                  <input
                    className="input"
                    name="password"
                    type="password"
                    placeholder="••••••••"
                    value={form.password}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div>
                  <label className="label">Confirm</label>
                  <input
                    className="input"
                    name="confirmPassword"
                    type="password"
                    placeholder="••••••••"
                    value={form.confirmPassword}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>
            </div>

            <div className="field">
              <label className="label">I want to</label>
              <select className="select" name="role" value={form.role} onChange={handleChange}>
                <option value="GUEST">Book properties as a Guest</option>
                <option value="HOST">List my property as a Host</option>
              </select>
            </div>

            {error && <div className="error-msg">{error}</div>}

            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Creating account…' : 'Create account'}
            </button>
          </form>

          <p className="auth-footer">
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </main>
    </div>
  )
}
