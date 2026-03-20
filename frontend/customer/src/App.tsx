import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { getSession } from './api/client'
import SetupPage from './pages/SetupPage'
import MenuPage from './pages/MenuPage'
import OrderConfirmPage from './pages/OrderConfirmPage'
import OrderHistoryPage from './pages/OrderHistoryPage'

function RequireSession({ children }: { children: React.ReactNode }) {
  const session = getSession()
  if (!session?.token) return <Navigate to="/setup" replace />
  return <>{children}</>
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/setup" element={<SetupPage />} />
        <Route path="/" element={<RequireSession><MenuPage /></RequireSession>} />
        <Route path="/confirm" element={<RequireSession><OrderConfirmPage /></RequireSession>} />
        <Route path="/orders" element={<RequireSession><OrderHistoryPage /></RequireSession>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
