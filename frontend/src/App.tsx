import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { ErrorBoundary } from 'react-error-boundary'
import { AuthProvider } from './context/AuthContext'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import { ToastProvider } from './components/Toast'
import RouteErrorFallback from './components/RouteErrorFallback'
import RootErrorFallback from './components/RootErrorFallback'
import LoginPage from './pages/LoginPage'
import Dashboard from './pages/Dashboard'
import MealLog from './pages/MealLog'
import Profile from './pages/Profile'
import Foods from './pages/Foods'
import Coach from './pages/Coach'
import Onboarding from './pages/Onboarding'
import Settings from './pages/Settings'

function RouteBoundary({ children }: { children: React.ReactNode }) {
  const location = useLocation()
  return (
    <ErrorBoundary FallbackComponent={RouteErrorFallback} resetKeys={[location.pathname]}>
      {children}
    </ErrorBoundary>
  )
}

export default function App() {
  return (
    <ErrorBoundary FallbackComponent={RootErrorFallback}>
      <BrowserRouter>
        <AuthProvider>
          <ToastProvider>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route
                path="/onboarding"
                element={
                  <ProtectedRoute>
                    <RouteBoundary>
                      <Onboarding />
                    </RouteBoundary>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/"
                element={
                  <ProtectedRoute>
                    <Layout />
                  </ProtectedRoute>
                }
              >
                <Route index element={<Navigate to="/dashboard" replace />} />
                <Route path="dashboard" element={<RouteBoundary><Dashboard /></RouteBoundary>} />
                <Route path="meal-log" element={<RouteBoundary><MealLog /></RouteBoundary>} />
                <Route path="foods" element={<RouteBoundary><Foods /></RouteBoundary>} />
                <Route path="coach" element={<RouteBoundary><Coach /></RouteBoundary>} />
                <Route path="profile" element={<RouteBoundary><Profile /></RouteBoundary>} />
                <Route path="settings" element={<RouteBoundary><Settings /></RouteBoundary>} />
              </Route>
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </ToastProvider>
        </AuthProvider>
      </BrowserRouter>
    </ErrorBoundary>
  )
}
