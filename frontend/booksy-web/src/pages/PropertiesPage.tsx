import { useState, useEffect, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { clearSession, getSession } from '../api/auth'
import { getMyProperties, submitProperty, uploadPropertyImage, PropertyResponse } from '../api/properties'

export default function PropertiesPage() {
    const navigate = useNavigate()
    const session = getSession()
    const initials = (session?.email?.[0] ?? '?').toUpperCase()

    const [properties, setProperties] = useState<PropertyResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [showModal, setShowModal] = useState(false)

    const [form, setForm] = useState({ name: '', address: '', city: '', country: '' })
    const [imageFile, setImageFile] = useState<File | null>(null)
    const [imagePreview, setImagePreview] = useState<string | null>(null)
    const [submitting, setSubmitting] = useState(false)
    const [formError, setFormError] = useState('')
    const [successMsg, setSuccessMsg] = useState('')

    useEffect(() => {
        getMyProperties()
            .then(setProperties)
            .finally(() => setLoading(false))
    }, [])

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        setForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
    }

    function handleImageChange(e: React.ChangeEvent<HTMLInputElement>) {
        const file = e.target.files?.[0]
        if (file) {
            setImageFile(file)
            setImagePreview(URL.createObjectURL(file))
        }
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault()
        setFormError('')
        setSubmitting(true)
        try {
            const created = await submitProperty(form)

            if (imageFile) {
                const imageUrl = await uploadPropertyImage(created.id, imageFile)
                created.imageUrl = imageUrl
            }

            setProperties(prev => [created, ...prev])
            setForm({ name: '', address: '', city: '', country: '' })
            setImageFile(null)
            setImagePreview(null)
            setShowModal(false)
            setSuccessMsg('Property submitted! It will be visible once an admin approves it.')
            setTimeout(() => setSuccessMsg(''), 5000)
        } catch {
            setFormError('Failed to submit property. Please try again.')
        } finally {
            setSubmitting(false)
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
                    <button className="btn-ghost" onClick={() => navigate('/dashboard')}>Dashboard</button>
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
                        <h1 className="page-title">My Properties</h1>
                        <p className="page-subtitle">Submitted properties require admin approval before they go live.</p>
                    </div>
                    <button className="btn-primary btn-add" onClick={() => { setShowModal(true); setFormError('') }}>
                        + Add Property
                    </button>
                </div>

                {successMsg && <div className="success-msg">{successMsg}</div>}

                {loading ? (
                    <p className="empty-state">Loading...</p>
                ) : properties.length === 0 ? (
                    <div className="empty-state">
                        <p>You haven't submitted any properties yet.</p>
                        <p>Click <strong>Add Property</strong> to get started.</p>
                    </div>
                ) : (
                    <div className="property-grid">
                        {properties.map(p => (
                            <div key={p.id} className="property-card">
                                {p.imageUrl && (
                                    <img
                                        src={p.imageUrl}
                                        alt={p.name}
                                        style={{ width: '100%', height: '180px', objectFit: 'cover',
                                            borderRadius: '8px', marginBottom: '12px' }}
                                    />
                                )}
                                <div className="property-card-header">
                                    <span className="property-name">{p.name}</span>
                                    <StatusBadge status={p.status} />
                                </div>
                                <p className="property-location">{p.address}</p>
                                <p className="property-location">{p.city}, {p.country}</p>
                                {p.status === 'PENDING' && (
                                    <p className="property-notice">Awaiting admin approval — not yet visible to guests.</p>
                                )}
                                {p.status === 'DENIED' && (
                                    <p className="property-notice property-notice-denied">Your property was not approved. Contact support for more info.</p>
                                )}
                                {p.status === 'ACCEPTED' && (
                                    <button
                                        className="btn-manage-rooms"
                                        onClick={() => navigate(`/properties/${p.id}/rooms`, { state: { propertyName: p.name } })}
                                    >
                                        Manage Rooms
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </main>

            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2 className="modal-title">Add New Property</h2>
                            <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
                        </div>
                        <p className="modal-subtitle">After submission, an admin will review and approve your property.</p>

                        <form onSubmit={handleSubmit}>
                            <div className="field">
                                <label className="label">Property name</label>
                                <input
                                    className="input"
                                    name="name"
                                    placeholder="e.g. Sunset Villa"
                                    value={form.name}
                                    onChange={handleChange}
                                    required
                                    autoFocus
                                />
                            </div>
                            <div className="field">
                                <label className="label">Address</label>
                                <input
                                    className="input"
                                    name="address"
                                    placeholder="e.g. Ul. Kralja Tomislava 12"
                                    value={form.address}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                            <div className="field field-row">
                                <div>
                                    <label className="label">City</label>
                                    <input
                                        className="input"
                                        name="city"
                                        placeholder="e.g. Split"
                                        value={form.city}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="label">Country</label>
                                    <input
                                        className="input"
                                        name="country"
                                        placeholder="e.g. Croatia"
                                        value={form.country}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="field">
                                <label className="label">Property image</label>
                                {imagePreview && (
                                    <img
                                        src={imagePreview}
                                        alt="preview"
                                        style={{ width: '100%', height: '200px', objectFit: 'cover',
                                            borderRadius: '8px', marginBottom: '8px' }}
                                    />
                                )}
                                <input
                                    type="file"
                                    accept="image/*"
                                    onChange={handleImageChange}
                                    className="input"
                                />
                            </div>

                            {formError && <div className="error-msg">{formError}</div>}

                            <div className="modal-actions">
                                <button type="button" className="btn-ghost" onClick={() => setShowModal(false)}>Cancel</button>
                                <button type="submit" className="btn-primary btn-modal-submit" disabled={submitting}>
                                    {submitting ? 'Submitting…' : 'Submit for approval'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    )
}

function StatusBadge({ status }: { status: string }) {
    const map: Record<string, { label: string; cls: string }> = {
        PENDING:  { label: 'Pending approval', cls: 'badge-pending' },
        ACCEPTED: { label: 'Approved',         cls: 'badge-accepted' },
        DENIED:   { label: 'Denied',           cls: 'badge-denied' },
    }
    const { label, cls } = map[status] ?? { label: status, cls: '' }
    return <span className={`status-badge ${cls}`}>{label}</span>
}