import { useEffect, useRef, useCallback } from 'react'
import { getToken } from '../api/client'
import type { Order } from '../types'

interface SseCallbacks {
  onNewOrder?: (order: Order) => void
  onStatusChanged?: (order: Order) => void
  onOrderDeleted?: (data: { orderId: number }) => void
  onTableReset?: (data: { tableId: number; tableNumber: number }) => void
}

export function useAdminSse(callbacks: SseCallbacks) {
  const esRef = useRef<EventSource | null>(null)
  const cbRef = useRef(callbacks)
  cbRef.current = callbacks

  const connect = useCallback(() => {
    const token = getToken()
    if (!token) return

    const BASE = import.meta.env.VITE_API_BASE_URL || ''
    const es = new EventSource(`${BASE}/api/admin/sse/orders?token=${encodeURIComponent(token)}`)

    es.addEventListener('new-order', (e: MessageEvent) => {
      try { cbRef.current.onNewOrder?.(JSON.parse(e.data)) } catch { /* ignore */ }
    })
    es.addEventListener('order-status-changed', (e: MessageEvent) => {
      try { cbRef.current.onStatusChanged?.(JSON.parse(e.data)) } catch { /* ignore */ }
    })
    es.addEventListener('order-deleted', (e: MessageEvent) => {
      try { cbRef.current.onOrderDeleted?.(JSON.parse(e.data)) } catch { /* ignore */ }
    })
    es.addEventListener('table-reset', (e: MessageEvent) => {
      try { cbRef.current.onTableReset?.(JSON.parse(e.data)) } catch { /* ignore */ }
    })

    es.onerror = () => {
      es.close()
      setTimeout(connect, 3000)
    }

    esRef.current = es
  }, [])

  useEffect(() => {
    connect()
    return () => { esRef.current?.close() }
  }, [connect])
}
