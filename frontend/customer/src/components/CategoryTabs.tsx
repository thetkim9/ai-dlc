import type { Category } from '../types'

interface Props {
  categories: Category[]
  selected: number | null
  onSelect: (id: number) => void
}

export default function CategoryTabs({ categories, selected, onSelect }: Props) {
  return (
    <div style={{
      display: 'flex',
      gap: 8,
      overflowX: 'auto',
      padding: '12px 16px',
      borderBottom: '1px solid #eee',
      background: '#fff',
      position: 'sticky',
      top: 0,
      zIndex: 10,
    }}>
      {categories.map(cat => (
        <button
          key={cat.id}
          onClick={() => onSelect(cat.id)}
          style={{
            whiteSpace: 'nowrap',
            padding: '6px 14px',
            borderRadius: 20,
            background: selected === cat.id ? '#ff6b35' : '#f0f0f0',
            color: selected === cat.id ? '#fff' : '#333',
            fontWeight: selected === cat.id ? 600 : 400,
            fontSize: 13,
          }}
        >
          {cat.name}
        </button>
      ))}
    </div>
  )
}
