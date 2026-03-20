import type { Menu } from '../types'

interface Props {
  menu: Menu
  quantity: number
  onAdd: (menu: Menu) => void
  onRemove: (menuId: number) => void
}

export default function MenuCard({ menu, quantity, onAdd, onRemove }: Props) {
  const BASE = import.meta.env.VITE_API_BASE_URL || ''

  return (
    <div style={{
      display: 'flex',
      gap: 12,
      padding: '14px 16px',
      borderBottom: '1px solid #f0f0f0',
      opacity: menu.available ? 1 : 0.4,
    }}>
      {menu.imageUrl && (
        <img
          src={`${BASE}${menu.imageUrl}`}
          alt={menu.name}
          style={{ width: 72, height: 72, borderRadius: 8, objectFit: 'cover', flexShrink: 0 }}
        />
      )}
      <div style={{ flex: 1 }}>
        <p style={{ fontWeight: 600, fontSize: 15 }}>{menu.name}</p>
        {menu.description && (
          <p style={{ fontSize: 12, color: '#888', marginTop: 2 }}>{menu.description}</p>
        )}
        <p style={{ fontSize: 14, color: '#ff6b35', fontWeight: 600, marginTop: 4 }}>
          {menu.price.toLocaleString()}원
        </p>
      </div>
      {menu.available && (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexShrink: 0 }}>
          {quantity > 0 ? (
            <>
              <button
                onClick={() => onRemove(menu.id)}
                style={{ width: 28, height: 28, borderRadius: '50%', background: '#f0f0f0', fontWeight: 700 }}
              >-</button>
              <span style={{ minWidth: 16, textAlign: 'center', fontWeight: 600 }}>{quantity}</span>
              <button
                onClick={() => onAdd(menu)}
                style={{ width: 28, height: 28, borderRadius: '50%', background: '#ff6b35', color: '#fff', fontWeight: 700 }}
              >+</button>
            </>
          ) : (
            <button
              onClick={() => onAdd(menu)}
              style={{ width: 28, height: 28, borderRadius: '50%', background: '#ff6b35', color: '#fff', fontWeight: 700 }}
            >+</button>
          )}
        </div>
      )}
    </div>
  )
}
