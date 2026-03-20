import { useEffect, useState } from 'react'
import { api } from '../api/client'
import type { Category, Menu } from '../types'

const EMPTY_FORM = { name: '', price: '', description: '', categoryId: '', available: true }

export default function MenuManagePage() {
  const [categories, setCategories] = useState<Category[]>([])
  const [menus, setMenus] = useState<Menu[]>([])
  const [selectedCat, setSelectedCat] = useState<number | null>(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [editId, setEditId] = useState<number | null>(null)
  const [msg, setMsg] = useState('')

  const STORE_ID = 1

  useEffect(() => {
    api.get(`/api/categories?storeId=${STORE_ID}`).then(res => {
      setCategories(res.data)
      if (res.data.length > 0) setSelectedCat(res.data[0].id)
    })
  }, [])

  useEffect(() => {
    if (!selectedCat) return
    api.get(`/api/menus?storeId=${STORE_ID}&categoryId=${selectedCat}`).then(res => setMenus(res.data))
  }, [selectedCat])

  function startEdit(menu: Menu) {
    setEditId(menu.id)
    setForm({
      name: menu.name,
      price: String(menu.price),
      description: menu.description ?? '',
      categoryId: String(menu.categoryId),
      available: menu.available,
    })
    setImageFile(null)
  }

  function resetForm() {
    setEditId(null)
    setForm(EMPTY_FORM)
    setImageFile(null)
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const data = new FormData()
    const req = {
      name: form.name,
      price: Number(form.price),
      description: form.description || undefined,
      categoryId: Number(form.categoryId || selectedCat),
      available: form.available,
    }
    data.append('request', new Blob([JSON.stringify(req)], { type: 'application/json' }))
    if (imageFile) data.append('imageFile', imageFile)

    try {
      if (editId) {
        await api.put(`/api/admin/menus/${editId}`, data, { headers: { 'Content-Type': 'multipart/form-data' } })
        setMsg('메뉴 수정 완료')
      } else {
        await api.post('/api/admin/menus', data, { headers: { 'Content-Type': 'multipart/form-data' } })
        setMsg('메뉴 등록 완료')
      }
      resetForm()
      if (selectedCat) {
        const res = await api.get(`/api/menus?storeId=${STORE_ID}&categoryId=${selectedCat}`)
        setMenus(res.data)
      }
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: string } } })?.response?.data?.error
      setMsg(msg || '저장 실패')
    }
  }

  async function handleDelete(menuId: number) {
    if (!confirm('메뉴를 삭제하시겠습니까?')) return
    await api.delete(`/api/admin/menus/${menuId}`)
    setMenus(prev => prev.filter(m => m.id !== menuId))
    setMsg('삭제 완료')
  }

  const BASE = import.meta.env.VITE_API_BASE_URL || ''

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: 24 }}>
      <div>
        <h2 style={{ marginBottom: 16, fontSize: 18 }}>메뉴 관리</h2>
        <div style={{ display: 'flex', gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
          {categories.map(cat => (
            <button key={cat.id}
              onClick={() => setSelectedCat(cat.id)}
              style={{
                padding: '6px 14px', borderRadius: 20,
                background: selectedCat === cat.id ? '#1976d2' : '#f0f0f0',
                color: selectedCat === cat.id ? '#fff' : '#333',
              }}>
              {cat.name}
            </button>
          ))}
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {menus.map(menu => (
            <div key={menu.id} className="card" style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              {menu.imageUrl && (
                <img src={`${BASE}${menu.imageUrl}`} alt={menu.name}
                  style={{ width: 56, height: 56, borderRadius: 6, objectFit: 'cover' }} />
              )}
              <div style={{ flex: 1 }}>
                <p style={{ fontWeight: 600 }}>{menu.name}</p>
                <p style={{ fontSize: 13, color: '#1976d2' }}>{menu.price.toLocaleString()}원</p>
                {!menu.available && <p style={{ fontSize: 11, color: '#f44336' }}>판매 중지</p>}
              </div>
              <div style={{ display: 'flex', gap: 6 }}>
                <button className="btn-secondary" onClick={() => startEdit(menu)}>수정</button>
                <button className="btn-danger" onClick={() => handleDelete(menu.id)}>삭제</button>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="card" style={{ alignSelf: 'start' }}>
        <h3 style={{ marginBottom: 16, fontSize: 15 }}>{editId ? '메뉴 수정' : '메뉴 등록'}</h3>
        {msg && <p style={{ fontSize: 13, color: '#1976d2', marginBottom: 10 }}>{msg}</p>}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <input placeholder="메뉴명 *" value={form.name}
            onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required />
          <input placeholder="가격 *" type="number" value={form.price}
            onChange={e => setForm(f => ({ ...f, price: e.target.value }))} required />
          <input placeholder="설명" value={form.description}
            onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
          <select value={form.categoryId || selectedCat || ''}
            onChange={e => setForm(f => ({ ...f, categoryId: e.target.value }))}>
            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13 }}>
            <input type="checkbox" style={{ width: 'auto' }}
              checked={form.available}
              onChange={e => setForm(f => ({ ...f, available: e.target.checked }))} />
            판매 중
          </label>
          <input type="file" accept="image/*" style={{ fontSize: 13 }}
            onChange={e => setImageFile(e.target.files?.[0] ?? null)} />
          <div style={{ display: 'flex', gap: 8 }}>
            <button type="submit" className="btn-primary" style={{ flex: 1 }}>
              {editId ? '수정' : '등록'}
            </button>
            {editId && <button type="button" className="btn-secondary" onClick={resetForm}>취소</button>}
          </div>
        </form>
      </div>
    </div>
  )
}
