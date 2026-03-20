import { useEffect, useState, useCallback } from 'react'
import { api } from '../api/client'
import { useAdminSse } from '../hooks/useAdminSse'
import TableDetailModal from '../components/TableDetailModal'
import type { TableSummary, Order } from '../types'

export default function DashboardPage() {
  const [tables, setTables] = useState<TableSummary[]>([])
  const [orders, setOrders] = useState<Record<number, Order[]>>({})
  const [selected, setSelected] = useState<TableSummary | null>(null)
  const [newOrderTableIds, setNewOrderTableIds] = useState<Set<number>>(new Set())

  const loadTables = useCallback(async () => {
    const res = await api.get('/api/admin/tables')
    setTables(res.data)
  }, [])

  const loadOrders = useCallback(async (tableId: number) => {
    const res = await api.get(`/api/admin/tables/${tableId}/orders`)
    setOrders(prev => ({ ...prev, [tableId]: res.data }))
  }, [])

  useEffect(() => {
    loadTables()
  }, [loadTables])

  useEffect(() => {
    tables.forEach(t => { if (t.sessionId) loadOrders(t.id) })
  }, [tables, loadOrders])

  useAdminSse({
    onNewOrder: (order) => {
      setOrders(prev => ({ ...prev, [order.tableId]: [order, ...(prev[order.tableId] ?? [])] }))
      setNewOrderTableIds(prev => new Set(prev).add(order.tableId))
      setTimeout(() => setNewOrderTableIds(prev => { const s = new Set(prev); s.delete(order.tableId); return s }), 3000)
    },
    onStatusChanged: (order) => {
      setOrders(prev => ({
        ...prev,
        [order.tableId]: (prev[order.tableId] ?? []).map(o => o.id === order.id ? order : o),
      }))
    },
    onOrderDeleted: ({ orderId }) => {
      setOrders(prev => {
        const next = { ...prev }
        for (const tid in next) next[tid] = next[tid].filter(o => o.id !== orderId)
        return next
      })
    },
    onTableReset: () => { loadTables() },
  })

  return (
    <div>
      <h2 style={{ marginBottom: 20, fontSize: 18 }}>주문 대시보드</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 16 }}>
        {tables.map(table => {
          const tableOrders = orders[table.id] ?? []
          const total = tableOrders.reduce((s, o) => s + o.totalAmount, 0)
          const isNew = newOrderTableIds.has(table.id)
          const hasActive = !!table.sessionId

          return (
            <div key={table.id} className="card"
              style={{
                cursor: hasActive ? 'pointer' : 'default',
                border: isNew ? '2px solid #ff6b35' : '2px solid transparent',
                transition: 'border 0.3s',
                opacity: hasActive ? 1 : 0.5,
              }}
              onClick={() => hasActive && setSelected(table)}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                <span style={{ fontWeight: 700, fontSize: 16 }}>{table.tableNumber}번</span>
                <span style={{
                  fontSize: 11, padding: '2px 8px', borderRadius: 10,
                  background: hasActive ? '#e8f5e9' : '#f5f5f5',
                  color: hasActive ? '#2e7d32' : '#999',
                }}>
                  {hasActive ? '사용 중' : '비어있음'}
                </span>
              </div>
              {hasActive && (
                <>
                  <p style={{ fontSize: 13, color: '#666' }}>주문 {tableOrders.length}건</p>
                  <p style={{ fontSize: 14, fontWeight: 700, color: '#1976d2', marginTop: 4 }}>
                    {total.toLocaleString()}원
                  </p>
                  {isNew && <p style={{ fontSize: 12, color: '#ff6b35', marginTop: 4 }}>🔔 신규 주문!</p>}
                </>
              )}
            </div>
          )
        })}
      </div>

      {selected && (
        <TableDetailModal
          table={selected}
          orders={orders[selected.id] ?? []}
          onClose={() => setSelected(null)}
          onOrdersChange={() => loadOrders(selected.id)}
          onComplete={() => { loadTables(); setSelected(null) }}
        />
      )}
    </div>
  )
}
