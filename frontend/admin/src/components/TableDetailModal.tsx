import { useState } from 'react'
import { api } from '../api/client'
import type { Order, TableSummary } from '../types'
import { ORDER_STATUS_LABEL, ORDER_STATUS_NEXT, ORDER_STATUS_COLOR } from '../types'

interface Props {
  table: TableSummary
  orders: Order[]
  onClose: () => void
  onOrdersChange: () => void
  onComplete: () => void
}

export default function TableDetailModal({ table, orders, onClose, onOrdersChange, onComplete }: Props) {
  const [completing, setCompleting] = useState(false)

  async function handleStatusChange(orderId: number, status: string) {
    await api.put(`/api/admin/orders/${orderId}/status`, { status })
    onOrdersChange()
  }

  async function handleDelete(orderId: number) {
    if (!confirm('주문을 삭제하시겠습니까?')) return
    await api.delete(`/api/admin/orders/${orderId}`)
    onOrdersChange()
  }

  async function handleComplete() {
    if (!confirm('이용 완료 처리하시겠습니까? 주문 내역이 이력으로 이동됩니다.')) return
    setCompleting(true)
    try {
      await api.post(`/api/admin/tables/${table.id}/complete`)
      onComplete()
      onClose()
    } finally {
      setCompleting(false)
    }
  }

  const totalAmount = orders.reduce((s, o) => s + o.totalAmount, 0)

  return (
    <div style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
      display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200,
    }} onClick={onClose}>
      <div className="card" style={{ width: 480, maxHeight: '80vh', overflow: 'auto' }}
        onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <h3 style={{ fontSize: 16 }}>테이블 {table.tableNumber}번</h3>
          <button className="btn-secondary" onClick={onClose}>✕</button>
        </div>

        {orders.length === 0 ? (
          <p style={{ color: '#aaa', textAlign: 'center', padding: 24 }}>주문 없음</p>
        ) : (
          orders.map(order => (
            <div key={order.id} style={{ border: '1px solid #eee', borderRadius: 8, padding: 12, marginBottom: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                <span style={{
                  fontSize: 12, fontWeight: 600, padding: '2px 8px', borderRadius: 10,
                  background: ORDER_STATUS_COLOR[order.status] + '22',
                  color: ORDER_STATUS_COLOR[order.status],
                }}>
                  {ORDER_STATUS_LABEL[order.status]}
                </span>
                <span style={{ fontSize: 12, color: '#888' }}>
                  {new Date(order.orderedAt).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>
              {order.items.map((item, i) => (
                <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, padding: '2px 0' }}>
                  <span>{item.menuName} × {item.quantity}</span>
                  <span>{(item.unitPrice * item.quantity).toLocaleString()}원</span>
                </div>
              ))}
              <div style={{ display: 'flex', gap: 6, marginTop: 10 }}>
                {ORDER_STATUS_NEXT[order.status] && (
                  <button className="btn-primary" style={{ fontSize: 12 }}
                    onClick={() => handleStatusChange(order.id, ORDER_STATUS_NEXT[order.status]!)}>
                    → {ORDER_STATUS_LABEL[ORDER_STATUS_NEXT[order.status]!]}
                  </button>
                )}
                <button className="btn-danger" style={{ fontSize: 12 }}
                  onClick={() => handleDelete(order.id)}>삭제</button>
              </div>
            </div>
          ))
        )}

        <div style={{ borderTop: '1px solid #eee', paddingTop: 12, marginTop: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontWeight: 700 }}>합계: {totalAmount.toLocaleString()}원</span>
          <button className="btn-danger" onClick={handleComplete} disabled={completing}>
            {completing ? '처리 중...' : '이용 완료'}
          </button>
        </div>
      </div>
    </div>
  )
}
