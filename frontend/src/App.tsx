import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { UserProvider, useUser } from './context/UserContext'
import Layout from './components/Layout'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import MealLog from './pages/MealLog'

function AppRoutes() {
  const { userId } = useUser()

  return (
    <Routes>
      <Route path="/register" element={<Register />} />
      <Route
        path="/"
        element={userId ? <Layout /> : <Navigate to="/register" replace />}
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="meal-log" element={<MealLog />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <UserProvider>
        <AppRoutes />
      </UserProvider>
    </BrowserRouter>
  )
}
