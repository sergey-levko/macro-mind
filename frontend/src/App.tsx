import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { UserProvider, useUser } from './context/UserContext'
import Layout from './components/Layout'
import { ToastProvider } from './components/Toast'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import MealLog from './pages/MealLog'
import Profile from './pages/Profile'
import Foods from './pages/Foods'

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
        <Route path="foods" element={<Foods />} />
        <Route path="profile" element={<Profile />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <UserProvider>
        <ToastProvider>
          <AppRoutes />
        </ToastProvider>
      </UserProvider>
    </BrowserRouter>
  )
}
