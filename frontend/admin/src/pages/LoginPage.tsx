import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, saveToken } from '../api/client'

export default function LoginPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ storeCode: '', username: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/api/auth/admin/login', form)
      saveToken(res.data.token)
      navigate('/')
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: string } } })?.response?.data?.error
      setError(msg || '로그인에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
      <div className="card" style={{ width: 360, padding: 32 }}>
        <h2 style={{ marginBottom: 24, fontSize: 20, textAlign: 'center' }}>관리자 로그인</h2>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <input placeholder="매장 코드" value={form.storeCode}
            onChange={e => setForm(f => ({ ...f, storeCode: e.target.value }))} required />
          <input placeholder="아이디" value={form.username}
            onChange={e => setForm(f => ({ ...f, username: e.target.value }))} required />
          <input placeholder="비밀번호" type="password" value={form.password}
            onChange={e => setForm(f => ({ ...f, password: e.target.value }))} required />
          {error && <p style={{ color: 'red', fontSize: 13 }}>{error}</p>}
          <button type="submit" className="btn-primary" disabled={loading} style={{ padding: 12, marginTop: 8 }}>
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>
      </div>
    </div>
  )
}
