import { useEffect, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import { useOrderSse } from '../hooks/useOrderSse'
import type { Order } from '../types'
import { ORDER_STATUS_LABEL } from '../types'

const STATUS_COLOR: Record<Order['status'], string> = {
  PENDING: '#ff9800',
  PREPARING: '#2196f3',
  COMPLETED: '#4caf50',
}

export default function OrderHistoryPage() {
  const navigate = useNavigate()
  const [orders, setOrders] = useState<Order[]>([])

  useEffect(() => {
    api.get('/api/orders/session').then(res => setOrders(res.data))
  }, [])

  const handleSseUpdate = useCallback((updated: Order) => {
    setOrders(prev =>
      prev.map(o => o.id === updated.id ? updated : o)
    )
  }, [])

  useOrderSse(handleSseUpdate)

  return (
    <div style={{ padding: 16 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
        <button className="btn-secondary" onClick={() => navigate('/')}>← 메뉴로</button>
        <h2 style={{ fontSize: 18, fontWeight: 700 }}>주문 내역</h2>
      </div>

      {orders.length === 0 ? (
        <p style={{ textAlign: 'center', color: '#aaa', marginTop: 60 }}>주문 내역이 없습니다.</p>
      ) : (
        orders.map(order => (
          <div key={order.id} style={{ background: '#f9f9f9', borderRadius: 12, padding: 16, marginBottom: 12 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
              <span style={{ fontSize: 13, color: '#888' }}>
                {new Date(order.orderedAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
              </span>
              <span style={{
                fontSize: 12, fontWeight: 600, padding: '3px 10px', borderRadius: 12,
                background: STATUS_COLOR[order.status] + '22',
                color: STATUS_COLOR[order.status],
              }}>
                {ORDER_STATUS_LABEL[order.status]}
              </span>
            </div>
            {order.items.map((item, i) => (
              <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 14, padding: '4px 0' }}>
                <span>{item.menuName} × {item.quantity}</span>
                <span>{(item.unitPrice * item.quantity).toLocaleString()}원</span>
              </div>
            ))}
            <div style={{ borderTop: '1px solid #eee', marginTop: 8, paddingTop: 8, display: 'flex', justifyContent: 'flex-end' }}>
              <span style={{ fontWeight: 700, color: '#ff6b35' }}>{order.totalAmount.toLocaleString()}원</span>
            </div>
          </div>
        ))
      )}
    </div>
  )
}
