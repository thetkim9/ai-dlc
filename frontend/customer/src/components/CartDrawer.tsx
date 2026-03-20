import { useNavigate } from 'react-router-dom'
import type { CartItem } from '../types'

interface Props {
  items: CartItem[]
  totalAmount: number
  totalCount: number
  onUpdateQuantity: (menuId: number, qty: number) => void
}

export default function CartDrawer({ items, totalAmount, totalCount, onUpdateQuantity }: Props) {
  const navigate = useNavigate()

  if (totalCount === 0) return null

  return (
    <div style={{
      position: 'fixed',
      bottom: 0,
      left: '50%',
      transform: 'translateX(-50%)',
      width: '100%',
      maxWidth: 480,
      background: '#fff',
      borderTop: '1px solid #eee',
      padding: '12px 16px',
      boxShadow: '0 -2px 8px rgba(0,0,0,0.08)',
      zIndex: 100,
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
        <span style={{ fontSize: 13, color: '#666' }}>장바구니 {totalCount}개</span>
        <span style={{ fontWeight: 700, color: '#ff6b35' }}>{totalAmount.toLocaleString()}원</span>
      </div>
      <div style={{ display: 'flex', gap: 8, marginBottom: 8, maxHeight: 120, overflowY: 'auto' }}>
        {items.map(item => (
          <div key={item.menuId} style={{
            display: 'flex', alignItems: 'center', gap: 6,
            background: '#f9f9f9', borderRadius: 8, padding: '4px 8px', flexShrink: 0,
          }}>
            <span style={{ fontSize: 13 }}>{item.name}</span>
            <button onClick={() => onUpdateQuantity(item.menuId, item.quantity - 1)}
              style={{ width: 20, height: 20, borderRadius: '50%', background: '#eee', fontSize: 12 }}>-</button>
            <span style={{ fontSize: 13, fontWeight: 600 }}>{item.quantity}</span>
            <button onClick={() => onUpdateQuantity(item.menuId, item.quantity + 1)}
              style={{ width: 20, height: 20, borderRadius: '50%', background: '#ff6b35', color: '#fff', fontSize: 12 }}>+</button>
          </div>
        ))}
      </div>
      <button
        className="btn-primary"
        style={{ width: '100%', padding: 14, fontSize: 15 }}
        onClick={() => navigate('/confirm')}
      >
        주문하기
      </button>
    </div>
  )
}
