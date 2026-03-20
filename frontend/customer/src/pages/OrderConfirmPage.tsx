import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import { useCart } from '../hooks/useCart'

export default function OrderConfirmPage() {
  const navigate = useNavigate()
  const { items, totalAmount, clearCart } = useCart()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  if (items.length === 0) {
    navigate('/')
    return null
  }

  async function handleOrder() {
    setLoading(true)
    setError('')
    try {
      await api.post('/api/orders', {
        items: items.map(i => ({ menuId: i.menuId, quantity: i.quantity })),
      })
      clearCart()
      navigate('/orders')
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: string } } })?.response?.data?.error
      setError(msg || '주문에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ padding: 16 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
        <button className="btn-secondary" onClick={() => navigate(-1)}>← 뒤로</button>
        <h2 style={{ fontSize: 18, fontWeight: 700 }}>주문 확인</h2>
      </div>

      <div style={{ background: '#f9f9f9', borderRadius: 12, padding: 16, marginBottom: 16 }}>
        {items.map(item => (
          <div key={item.menuId} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #eee' }}>
            <span>{item.name} × {item.quantity}</span>
            <span style={{ fontWeight: 600 }}>{(item.price * item.quantity).toLocaleString()}원</span>
          </div>
        ))}
        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 12, fontWeight: 700, fontSize: 16 }}>
          <span>합계</span>
          <span style={{ color: '#ff6b35' }}>{totalAmount.toLocaleString()}원</span>
        </div>
      </div>

      {error && <p style={{ color: 'red', fontSize: 13, marginBottom: 12 }}>{error}</p>}

      <button
        className="btn-primary"
        style={{ width: '100%', padding: 14, fontSize: 15 }}
        onClick={handleOrder}
        disabled={loading}
      >
        {loading ? '주문 중...' : '주문 확정'}
      </button>
    </div>
  )
}
