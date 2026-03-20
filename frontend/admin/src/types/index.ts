export interface Category {
  id: number
  name: string
  displayOrder: number
}

export interface Menu {
  id: number
  name: string
  price: number
  description?: string
  imageUrl?: string
  displayOrder: number
  available: boolean
  categoryId: number
}

export interface OrderItem {
  menuId: number
  menuName: string
  quantity: number
  unitPrice: number
}

export type OrderStatus = 'PENDING' | 'PREPARING' | 'COMPLETED'

export interface Order {
  id: number
  status: OrderStatus
  totalAmount: number
  orderedAt: string
  completedAt?: string
  items: OrderItem[]
  tableId: number
}

export interface TableSession {
  id: number
  status: 'ACTIVE' | 'COMPLETED'
  startedAt: string
  expiresAt: string
}

export interface TableSummary {
  id: number
  tableNumber: number
  sessionId?: number
  sessionStatus?: string
  expiresAt?: string
}

export const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING: '접수 대기',
  PREPARING: '준비 중',
  COMPLETED: '완료',
}

export const ORDER_STATUS_NEXT: Partial<Record<OrderStatus, OrderStatus>> = {
  PENDING: 'PREPARING',
  PREPARING: 'COMPLETED',
}

export const ORDER_STATUS_COLOR: Record<OrderStatus, string> = {
  PENDING: '#ff9800',
  PREPARING: '#2196f3',
  COMPLETED: '#4caf50',
}
