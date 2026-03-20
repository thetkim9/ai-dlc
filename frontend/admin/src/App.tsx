import { BrowserRouter, Routes, Route, Navigate, Link } from 'react-router-dom'
import { getToken, clearToken } from './api/client'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import TableManagePage from './pages/TableManagePage'
import MenuManagePage from './pages/MenuManagePage'

function RequireAuth({ children }: { children: React.ReactNode }) {
  if (!getToken()) return <Navigate to="/login" replace />
  return <>{children}</>
}

function Layout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <nav>
        <span className="logo">🍽 테이블오더</span>
        <Link to="/">대시보드</Link>
        <Link to="/tables">테이블 관리</Link>
        <Link to="/menus">메뉴 관리</Link>
        <button
          className="btn-secondary"
          style={{ marginLeft: 'auto', fontSize: 12 }}
          onClick={() => { clearToken(); window.location.href = '/login' }}
        >로그아웃</button>
      </nav>
      <div style={{ padding: 24 }}>{children}</div>
    </>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<RequireAuth><Layout><DashboardPage /></Layout></RequireAuth>} />
        <Route path="/tables" element={<RequireAuth><Layout><TableManagePage /></Layout></RequireAuth>} />
        <Route path="/menus" element={<RequireAuth><Layout><MenuManagePage /></Layout></RequireAuth>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
