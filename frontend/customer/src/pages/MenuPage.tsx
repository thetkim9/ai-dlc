import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, getSession } from '../api/client'
import { useCart } from '../hooks/useCart'
import CategoryTabs from '../components/CategoryTabs'
import MenuCard from '../components/MenuCard'
import CartDrawer from '../components/CartDrawer'
import type { Category, Menu } from '../types'

export default function MenuPage() {
  const navigate = useNavigate()
  const session = getSession()!
  const [categories, setCategories] = useState<Category[]>([])
  const [menus, setMenus] = useState<Menu[]>([])
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null)
  const { items, addItem, removeItem, updateQuantity, totalAmount, totalCount } = useCart()

  useEffect(() => {
    api.get(`/api/categories?storeId=1`).then(res => {
      setCategories(res.data)
      if (res.data.length > 0) setSelectedCategory(res.data[0].id)
    })
  }, [])

  useEffect(() => {
    if (!selectedCategory) return
    api.get(`/api/menus?storeId=1&categoryId=${selectedCategory}`).then(res => {
      setMenus(res.data)
    })
  }, [selectedCategory])

  function getQuantity(menuId: number) {
    return items.find(i => i.menuId === menuId)?.quantity ?? 0
  }

  function handleRemove(menuId: number) {
    const item = items.find(i => i.menuId === menuId)
    if (item) updateQuantity(menuId, item.quantity - 1)
  }

  return (
    <div style={{ paddingBottom: 140 }}>
      <div style={{ padding: '16px', borderBottom: '1px solid #eee', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1 style={{ fontSize: 18, fontWeight: 700 }}>테이블 {session.tableNumber}번</h1>
        <button className="btn-secondary" style={{ fontSize: 13 }} onClick={() => navigate('/orders')}>
          주문 내역
        </button>
      </div>
      <CategoryTabs categories={categories} selected={selectedCategory} onSelect={setSelectedCategory} />
      <div>
        {menus.map(menu => (
          <MenuCard
            key={menu.id}
            menu={menu}
            quantity={getQuantity(menu.id)}
            onAdd={addItem}
            onRemove={handleRemove}
          />
        ))}
      </div>
      <CartDrawer
        items={items}
        totalAmount={totalAmount}
        totalCount={totalCount}
        onUpdateQuantity={updateQuantity}
      />
    </div>
  )
}
