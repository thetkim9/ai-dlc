import { useEffect, useState } from 'react'
import { api } from '../api/client'
import type { TableSummary, Order } from '../types'

export default function TableManagePage() {
  const [tables, setTables] = useState<TableSummary[]>([])
  const [setupForm, setSetupForm] = useState<Record<number, string>>({})
  const [history, setHistory] = useState<Record<number, Order[]>>({})
  const [openHistory, setOpenHistory] = useState<number | null>(null)
  const [msg, setMsg] = useState('')

  useEffect(() => {
    api.get('/api/admin/tables').then(res => setTables(res.data))
  }, [])

  async function handleSetup(tableId: number) {
    const password = setupForm[tableId]
    if (!password) return
    try {
      await api.post(`/api/admin/tables/${tableId}/setup`, { password })
      setMsg(`테이블 설정 완료`)
      api.get('/api/admin/tables').then(res => setTables(res.data))
    } catch {
      setMsg('설정 실패')
    }
  }

  async function loadHistory(tableId: number) {
    const res = await api.get(`/api/admin/tables/${tableId}/history`)
    setHistory(prev => ({ ...prev, [tableId]: res.data }))
    setOpenHistory(tableId)
  }

  return (
    <div>
      <h2 style={{ marginBottom: 20, fontSize: 18 }}>테이블 관리</h2>
      {msg && <p style={{ color: '#1976d2', marginBottom: 12, fontSize: 13 }}>{msg}</p>}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        {tables.map(table => (
          <div key={table.id} className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
              <span style={{ fontWeight: 700, minWidth: 60 }}>{table.tableNumber}번 테이블</span>
              <span style={{
                fontSize: 12, padding: '2px 8px', borderRadius: 10,
                background: table.sessionId ? '#e8f5e9' : '#f5f5f5',
                color: table.sessionId ? '#2e7d32' : '#999',
              }}>
                {table.sessionId ? '사용 중' : '비어있음'}
              </span>
              <div style={{ display: 'flex', gap: 8, marginLeft: 'auto', alignItems: 'center' }}>
                <input
                  placeholder="새 비밀번호"
                  type="password"
                  style={{ width: 120 }}
                  value={setupForm[table.id] ?? ''}
                  onChange={e => setSetupForm(f => ({ ...f, [table.id]: e.target.value }))}
                />
                <button className="btn-primary" onClick={() => handleSetup(table.id)}>
                  초기 설정
                </button>
                <button className="btn-secondary" onClick={() => loadHistory(table.id)}>
                  이력 조회
                </button>
              </div>
            </div>

            {openHistory === table.id && (
              <div style={{ marginTop: 12, borderTop: '1px solid #eee', paddingTop: 12 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                  <span style={{ fontSize: 13, fontWeight: 600 }}>과거 주문 이력</span>
                  <button className="btn-secondary" style={{ fontSize: 11 }} onClick={() => setOpenHistory(null)}>닫기</button>
                </div>
                {(history[table.id] ?? []).length === 0 ? (
                  <p style={{ fontSize: 13, color: '#aaa' }}>이력 없음</p>
                ) : (
                  (history[table.id] ?? []).map(order => (
                    <div key={order.id} style={{ fontSize: 13, padding: '6px 0', borderBottom: '1px solid #f5f5f5' }}>
                      <span style={{ color: '#888', marginRight: 8 }}>
                        {new Date(order.orderedAt).toLocaleDateString('ko-KR')}
                      </span>
                      {order.items.map(i => `${i.menuName}×${i.quantity}`).join(', ')}
                      <span style={{ float: 'right', fontWeight: 600 }}>{order.totalAmount.toLocaleString()}원</span>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
