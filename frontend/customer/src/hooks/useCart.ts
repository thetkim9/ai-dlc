import { useState, useEffect } from 'react'
import type { CartItem, Menu } from '../types'

const CART_KEY = 'cart_items'

function loadCart(): CartItem[] {
  try {
    const raw = localStorage.getItem(CART_KEY)
    return raw ? (JSON.parse(raw) as CartItem[]) : []
  } catch {
    return []
  }
}

export function useCart() {
  const [items, setItems] = useState<CartItem[]>(loadCart)

  useEffect(() => {
    localStorage.setItem(CART_KEY, JSON.stringify(items))
  }, [items])

  function addItem(menu: Menu) {
    setItems((prev) => {
      const existing = prev.find((i) => i.menuId === menu.id)
      if (existing) {
        return prev.map((i) =>
          i.menuId === menu.id ? { ...i, quantity: i.quantity + 1 } : i
        )
      }
      return [...prev, { menuId: menu.id, name: menu.name, price: menu.price, quantity: 1 }]
    })
  }

  function removeItem(menuId: number) {
    setItems((prev) => prev.filter((i) => i.menuId !== menuId))
  }

  function updateQuantity(menuId: number, quantity: number) {
    if (quantity <= 0) {
      removeItem(menuId)
      return
    }
    setItems((prev) =>
      prev.map((i) => (i.menuId === menuId ? { ...i, quantity } : i))
    )
  }

  function clearCart() {
    setItems([])
  }

  const totalAmount = items.reduce((sum, i) => sum + i.price * i.quantity, 0)
  const totalCount = items.reduce((sum, i) => sum + i.quantity, 0)

  return { items, addItem, removeItem, updateQuantity, clearCart, totalAmount, totalCount }
}
