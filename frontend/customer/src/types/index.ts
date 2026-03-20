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

export interface CartItem {
  menuId: number
  name: string
  price: number
  quantity: number
}

export interface OrderItem {
  menuId: number
  menuName: string
  quantity: number
  unitPrice: number
}

export interface Order {
  id: number
  status: 'PENDING' | 'PREPARING' | 'COMPLETED'
  totalAmount: number
  orderedAt: string
  items: OrderItem[]
}

export interface TableSession {
  storeCode: string
  tableNumber: number
  password: string
  token: string
  sessionId: number
  tableId: number
}

export const ORDER_STATUS_LABEL: Record<Order['status'], string> = {
  PENDING: '접수 대기',
  PREPARING: '준비 중',
  COMPLETED: '완료',
}
