import { useEffect, useRef, useCallback } from 'react'
import { getSession } from '../api/client'
import type { Order } from '../types'

export function useOrderSse(onUpdate: (order: Order) => void) {
  const esRef = useRef<EventSource | null>(null)
  const onUpdateRef = useRef(onUpdate)
  onUpdateRef.current = onUpdate

  const connect = useCallback(() => {
    const session = getSession()
    if (!session?.token) return

    // token을 query param으로 전달 (EventSource는 헤더 설정 불가)
    const BASE = import.meta.env.VITE_API_BASE_URL || ''
    const url = `${BASE}/api/sse/orders?token=${encodeURIComponent(session.token)}`
    const es = new EventSource(url)

    es.addEventListener('order-status-changed', (e: MessageEvent) => {
      try {
        const order = JSON.parse(e.data) as Order
        onUpdateRef.current(order)
      } catch {
        // ignore parse error
      }
    })

    es.onerror = () => {
      es.close()
      setTimeout(connect, 3000)
    }

    esRef.current = es
  }, [])

  useEffect(() => {
    connect()
    return () => {
      esRef.current?.close()
    }
  }, [connect])
}
