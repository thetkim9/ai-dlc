import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, saveSession } from '../api/client'

export default function SetupPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ storeCode: '', tableNumber: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.post('/api/auth/table/login', {
        storeCode: form.storeCode,
        tableNumber: Number(form.tableNumber),
        password: form.password,
      })
      saveSession({
        storeCode: form.storeCode,
        tableNumber: Number(form.tableNumber),
        password: form.password,
        token: res.data.token,
        sessionId: res.data.sessionId,
        tableId: res.data.tableId,
      })
      navigate('/')
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: string } } })?.response?.data?.error
      setError(msg || '로그인에 실패했습니다. 관리자에게 문의하세요.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ padding: 32 }}>
      <h2 style={{ marginBottom: 24, fontSize: 20 }}>테이블 설정</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <input
          placeholder="매장 코드"
          value={form.storeCode}
          onChange={e => setForm(f => ({ ...f, storeCode: e.target.value }))}
          required
        />
        <input
          placeholder="테이블 번호"
          type="number"
          value={form.tableNumber}
          onChange={e => setForm(f => ({ ...f, tableNumber: e.target.value }))}
          required
        />
        <input
          placeholder="비밀번호"
          type="password"
          value={form.password}
          onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
          required
        />
        {error && <p style={{ color: 'red', fontSize: 13 }}>{error}</p>}
        <button type="submit" className="btn-primary" disabled={loading} style={{ padding: '12px', marginTop: 8 }}>
          {loading ? '연결 중...' : '시작하기'}
        </button>
      </form>
    </div>
  )
}
