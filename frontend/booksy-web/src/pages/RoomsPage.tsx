import { useState, useEffect, FormEvent } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'
import { clearSession, getSession } from '../api/auth'
import {
    getRoomsByProperty, createRoom, updateRoom, deleteRoom,
    uploadRoomImages, deleteRoomImage, RoomResponse
} from '../api/rooms'

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
    const [showEditModal, setShowEditModal] = useState(false)
    const [editingRoom, setEditingRoom] = useState<RoomResponse | null>(null)
    const [deletingId, setDeletingId] = useState<number | null>(null)
    const [editImageFiles, setEditImageFiles] = useState<File[]>([])
    const [editImagePreviews, setEditImagePreviews] = useState<string[]>([])

    const [form, setForm] = useState({
        name: '', capacity: '', pricePerNight: '', availableFrom: '', availableTo: '',
    })
    const [editForm, setEditForm] = useState({
        name: '', capacity: '', pricePerNight: '', availableFrom: '', availableTo: '',
    })
    const [imageFiles, setImageFiles] = useState<File[]>([])
    const [imagePreviews, setImagePreviews] = useState<string[]>([])
    const [submitting, setSubmitting] = useState(false)
    const [editSubmitting, setEditSubmitting] = useState(false)
    const [formError, setFormError] = useState('')
    const [editFormError, setEditFormError] = useState('')

    useEffect(() => {
        if (!propertyId) return
        getRoomsByProperty(Number(propertyId))
            .then(setRooms)
            .finally(() => setLoading(false))
    }, [propertyId])

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        setForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
    }

    function handleEditChange(e: React.ChangeEvent<HTMLInputElement>) {
        setEditForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
    }

    function handleImagesChange(e: React.ChangeEvent<HTMLInputElement>) {
        const files = Array.from(e.target.files ?? [])
        setImageFiles(files)
        setImagePreviews(files.map(f => URL.createObjectURL(f)))
    }

    function handleEditImagesChange(e: React.ChangeEvent<HTMLInputElement>) {
        const files = Array.from(e.target.files ?? [])
        setEditImageFiles(files)
        setEditImagePreviews(files.map(f => URL.createObjectURL(f)))
    }

    function openEditModal(room: RoomResponse) {
        setEditingRoom(room)
        setEditForm({
            name: room.name,
            capacity: String(room.capacity),
            pricePerNight: String(room.pricePerNight),
            availableFrom: room.availableFrom,
            availableTo: room.availableTo,
        })
        setEditImageFiles([])
        setEditImagePreviews([])
        setEditFormError('')
        setShowEditModal(true)
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
            if (imageFiles.length > 0) {
                const urls = await uploadRoomImages(created.id, imageFiles)
                created.imageUrls = urls
            }
            setRooms(prev => [...prev, created])
            setForm({ name: '', capacity: '', pricePerNight: '', availableFrom: '', availableTo: '' })
            setImageFiles([])
            setImagePreviews([])
            setShowModal(false)
        } catch {
            setFormError('Failed to add room. Please try again.')
        } finally {
            setSubmitting(false)
        }
    }

    async function handleEditSubmit(e: FormEvent) {
        e.preventDefault()
        setEditFormError('')
        if (new Date(editForm.availableFrom) >= new Date(editForm.availableTo)) {
            setEditFormError('Available from must be before available to.')
            return
        }
        if (!editingRoom) return
        setEditSubmitting(true)
        try {
            const updated = await updateRoom(editingRoom.id, {
                name: editForm.name,
                capacity: Number(editForm.capacity),
                pricePerNight: Number(editForm.pricePerNight),
                availableFrom: editForm.availableFrom,
                availableTo: editForm.availableTo,
            })
            let newImageUrls = editingRoom.imageUrls ?? []
            if (editImageFiles.length > 0) {
                const uploaded = await uploadRoomImages(editingRoom.id, editImageFiles)
                newImageUrls = [...newImageUrls, ...uploaded]
            }
            setRooms(prev => prev.map(r =>
                r.id === updated.id ? { ...updated, imageUrls: newImageUrls } : r
            ))
            setShowEditModal(false)
            setEditingRoom(null)
            setEditImageFiles([])
            setEditImagePreviews([])
        } catch {
            setEditFormError('Failed to update room. Please try again.')
        } finally {
            setEditSubmitting(false)
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

    async function handleDeleteImage(roomId: number, imageUrl: string) {
        try {
            await deleteRoomImage(roomId, imageUrl)
            setRooms(prev => prev.map(r =>
                r.id === roomId
                    ? { ...r, imageUrls: r.imageUrls.filter(u => u !== imageUrl) }
                    : r
            ))
            if (editingRoom?.id === roomId) {
                setEditingRoom(prev => prev
                    ? { ...prev, imageUrls: prev.imageUrls.filter(u => u !== imageUrl) }
                    : null
                )
            }
        } catch {
            alert('Failed to delete image.')
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
                                {room.imageUrls && room.imageUrls.length > 0 && (
                                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '12px' }}>
                                        {room.imageUrls.map((url, i) => (
                                            <div key={i} style={{ position: 'relative' }}>
                                                <img src={url} alt={`room-${i}`}
                                                     style={{ width: '100px', height: '75px',
                                                         objectFit: 'cover', borderRadius: '6px' }} />
                                                <button onClick={() => handleDeleteImage(room.id, url)}
                                                        style={{ position: 'absolute', top: '2px', right: '2px',
                                                            background: '#B00020', color: 'white', border: 'none',
                                                            borderRadius: '50%', width: '20px', height: '20px',
                                                            cursor: 'pointer', fontSize: '10px', lineHeight: '20px',
                                                            textAlign: 'center', padding: 0 }}>✕</button>
                                            </div>
                                        ))}
                                    </div>
                                )}
                                <div className="room-card-header">
                                    <span className="room-name">{room.name}</span>
                                    <div style={{ display: 'flex', gap: '8px' }}>
                                        <button className="btn-ghost" onClick={() => openEditModal(room)}>Edit</button>
                                        <button className="btn-delete" onClick={() => handleDelete(room.id)}
                                                disabled={deletingId === room.id}>
                                            {deletingId === room.id ? '…' : 'Delete'}
                                        </button>
                                    </div>
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

            {/* Add modal */}
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
                                <input className="input" name="name" placeholder="e.g. Deluxe Double Room"
                                       value={form.name} onChange={handleChange} required autoFocus />
                            </div>
                            <div className="field field-row">
                                <div>
                                    <label className="label">Capacity (guests)</label>
                                    <input className="input" name="capacity" type="number" min="1"
                                           placeholder="e.g. 2" value={form.capacity} onChange={handleChange} required />
                                </div>
                                <div>
                                    <label className="label">Price per night (€)</label>
                                    <input className="input" name="pricePerNight" type="number" min="0"
                                           step="0.01" placeholder="e.g. 85.00" value={form.pricePerNight}
                                           onChange={handleChange} required />
                                </div>
                            </div>
                            <div className="field field-row">
                                <div>
                                    <label className="label">Available from</label>
                                    <input className="input" name="availableFrom" type="date"
                                           value={form.availableFrom} onChange={handleChange} required />
                                </div>
                                <div>
                                    <label className="label">Available to</label>
                                    <input className="input" name="availableTo" type="date"
                                           value={form.availableTo} onChange={handleChange} required />
                                </div>
                            </div>
                            <div className="field">
                                <label className="label">Room images</label>
                                {imagePreviews.length > 0 && (
                                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '8px' }}>
                                        {imagePreviews.map((src, i) => (
                                            <img key={i} src={src} alt={`preview-${i}`}
                                                 style={{ width: '80px', height: '60px',
                                                     objectFit: 'cover', borderRadius: '6px' }} />
                                        ))}
                                    </div>
                                )}
                                <input type="file" accept="image/*" multiple
                                       onChange={handleImagesChange} className="input" />
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

            {/* Edit modal */}
            {showEditModal && editingRoom && (
                <div className="modal-overlay" onClick={() => setShowEditModal(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2 className="modal-title">Edit Room</h2>
                            <button className="modal-close" onClick={() => setShowEditModal(false)}>✕</button>
                        </div>
                        <p className="modal-subtitle">Edit <strong>{editingRoom.name}</strong>.</p>
                        <form onSubmit={handleEditSubmit}>
                            <div className="field">
                                <label className="label">Room name</label>
                                <input className="input" name="name" placeholder="e.g. Deluxe Double Room"
                                       value={editForm.name} onChange={handleEditChange} required autoFocus />
                            </div>
                            <div className="field field-row">
                                <div>
                                    <label className="label">Capacity (guests)</label>
                                    <input className="input" name="capacity" type="number" min="1"
                                           value={editForm.capacity} onChange={handleEditChange} required />
                                </div>
                                <div>
                                    <label className="label">Price per night (€)</label>
                                    <input className="input" name="pricePerNight" type="number" min="0"
                                           step="0.01" value={editForm.pricePerNight} onChange={handleEditChange} required />
                                </div>
                            </div>
                            <div className="field field-row">
                                <div>
                                    <label className="label">Available from</label>
                                    <input className="input" name="availableFrom" type="date"
                                           value={editForm.availableFrom} onChange={handleEditChange} required />
                                </div>
                                <div>
                                    <label className="label">Available to</label>
                                    <input className="input" name="availableTo" type="date"
                                           value={editForm.availableTo} onChange={handleEditChange} required />
                                </div>
                            </div>

                            {/* Postojeće slike */}
                            <div className="field">
                                <label className="label">Current images</label>
                                {editingRoom.imageUrls && editingRoom.imageUrls.length > 0 ? (
                                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '8px' }}>
                                        {editingRoom.imageUrls.map((url, i) => (
                                            <div key={i} style={{ position: 'relative' }}>
                                                <img src={url} alt={`room-${i}`}
                                                     style={{ width: '80px', height: '60px',
                                                         objectFit: 'cover', borderRadius: '6px' }} />
                                                <button type="button"
                                                        onClick={() => handleDeleteImage(editingRoom.id, url)}
                                                        style={{ position: 'absolute', top: '2px', right: '2px',
                                                            background: '#B00020', color: 'white', border: 'none',
                                                            borderRadius: '50%', width: '20px', height: '20px',
                                                            cursor: 'pointer', fontSize: '10px', lineHeight: '20px',
                                                            textAlign: 'center', padding: 0 }}>✕</button>
                                            </div>
                                        ))}
                                    </div>
                                ) : (
                                    <p style={{ fontSize: '12px', color: '#888', marginBottom: '8px' }}>No images yet.</p>
                                )}

                                {/* Dodaj nove slike */}
                                <label className="label">Add new images</label>
                                {editImagePreviews.length > 0 && (
                                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '8px' }}>
                                        {editImagePreviews.map((src, i) => (
                                            <img key={i} src={src} alt={`preview-${i}`}
                                                 style={{ width: '80px', height: '60px',
                                                     objectFit: 'cover', borderRadius: '6px' }} />
                                        ))}
                                    </div>
                                )}
                                <input type="file" accept="image/*" multiple
                                       onChange={handleEditImagesChange} className="input" />
                            </div>

                            {editFormError && <div className="error-msg">{editFormError}</div>}
                            <div className="modal-actions">
                                <button type="button" className="btn-ghost" onClick={() => setShowEditModal(false)}>Cancel</button>
                                <button type="submit" className="btn-primary btn-modal-submit" disabled={editSubmitting}>
                                    {editSubmitting ? 'Saving…' : 'Save changes'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    )
}