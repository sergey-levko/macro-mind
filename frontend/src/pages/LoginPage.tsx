import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import type { GoalType, AuthResponse } from '../lib/types'

const GOAL_LABELS: Record<GoalType, string> = {
  LOSE_WEIGHT: 'Lose Weight',
  MAINTAIN_WEIGHT: 'Maintain Weight',
  GAIN_MUSCLE: 'Gain Muscle',
}

type Mode = 'login' | 'register'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [mode, setMode] = useState<Mode>('login')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [loginForm, setLoginForm] = useState({ email: '', password: '' })
  const [registerForm, setRegisterForm] = useState({
    name: '',
    email: '',
    password: '',
    age: '',
    weightKg: '',
    heightCm: '',
    goalType: '' as GoalType | '',
  })

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: loginForm.email, password: loginForm.password }),
      })
      if (!res.ok) {
        setError(res.status === 401 ? 'Invalid email or password.' : 'Login failed. Please try again.')
        return
      }
      const data: AuthResponse = await res.json()
      login(data.accessToken, data.refreshToken, data.user)
      navigate('/dashboard', { replace: true })
    } catch {
      setError('Network error — is the backend running?')
    } finally {
      setLoading(false)
    }
  }

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    if (!registerForm.goalType) { setError('Please select a goal.'); return }
    setLoading(true)
    try {
      const res = await fetch('/api/v1/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: registerForm.name.trim(),
          email: registerForm.email.trim(),
          password: registerForm.password,
          age: Number(registerForm.age),
          weightKg: Number(registerForm.weightKg),
          heightCm: Number(registerForm.heightCm),
          goalType: registerForm.goalType,
        }),
      })
      if (!res.ok) {
        const text = await res.text().catch(() => res.statusText)
        setError(res.status === 409 ? 'Email already in use.' : `Registration failed: ${text}`)
        return
      }
      const data: AuthResponse = await res.json()
      login(data.accessToken, data.refreshToken, data.user)
      navigate('/dashboard', { replace: true })
    } catch {
      setError('Network error — is the backend running?')
    } finally {
      setLoading(false)
    }
  }

  const inputCls =
    'w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500 text-sm'

  return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-white">
            Macro<span className="text-teal-400">Mind</span>
          </h1>
          <p className="text-gray-400 mt-2">
            {mode === 'login' ? 'Sign in to your account' : 'Create your profile to get started'}
          </p>
        </div>

        <div className="bg-gray-900 rounded-2xl p-8 border border-gray-800">
          <div className="flex gap-2 mb-6">
            <button
              type="button"
              onClick={() => { setMode('login'); setError('') }}
              className={`flex-1 py-2 rounded-lg text-sm font-medium transition-colors ${
                mode === 'login' ? 'bg-teal-600 text-white' : 'text-gray-400 hover:text-white'
              }`}
            >
              Sign In
            </button>
            <button
              type="button"
              onClick={() => { setMode('register'); setError('') }}
              className={`flex-1 py-2 rounded-lg text-sm font-medium transition-colors ${
                mode === 'register' ? 'bg-teal-600 text-white' : 'text-gray-400 hover:text-white'
              }`}
            >
              Register
            </button>
          </div>

          {mode === 'login' ? (
            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Email</label>
                <input
                  type="email"
                  className={inputCls}
                  placeholder="you@example.com"
                  value={loginForm.email}
                  onChange={e => setLoginForm(f => ({ ...f, email: e.target.value }))}
                  required
                  autoFocus
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Password</label>
                <input
                  type="password"
                  className={inputCls}
                  placeholder="••••••••"
                  value={loginForm.password}
                  onChange={e => setLoginForm(f => ({ ...f, password: e.target.value }))}
                  required
                />
              </div>
              {error && (
                <p className="text-sm text-red-400 bg-red-900/20 rounded-lg px-3 py-2">{error}</p>
              )}
              <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-lg transition-colors"
              >
                {loading ? 'Signing in…' : 'Sign In'}
              </button>
            </form>
          ) : (
            <form onSubmit={handleRegister} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Full Name</label>
                <input
                  type="text"
                  className={inputCls}
                  placeholder="Jane Smith"
                  value={registerForm.name}
                  onChange={e => setRegisterForm(f => ({ ...f, name: e.target.value }))}
                  required
                  autoFocus
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Email</label>
                <input
                  type="email"
                  className={inputCls}
                  placeholder="jane@example.com"
                  value={registerForm.email}
                  onChange={e => setRegisterForm(f => ({ ...f, email: e.target.value }))}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Password <span className="text-gray-500 font-normal">(min 8 chars)</span>
                </label>
                <input
                  type="password"
                  className={inputCls}
                  placeholder="••••••••"
                  value={registerForm.password}
                  onChange={e => setRegisterForm(f => ({ ...f, password: e.target.value }))}
                  required
                  minLength={8}
                />
              </div>
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Age</label>
                  <input
                    type="number"
                    className={inputCls}
                    placeholder="30"
                    value={registerForm.age}
                    onChange={e => setRegisterForm(f => ({ ...f, age: e.target.value }))}
                    required
                    min={1}
                    max={120}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Weight (kg)</label>
                  <input
                    type="number"
                    className={inputCls}
                    placeholder="70"
                    step="0.1"
                    value={registerForm.weightKg}
                    onChange={e => setRegisterForm(f => ({ ...f, weightKg: e.target.value }))}
                    required
                    min={20}
                    max={500}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">Height (cm)</label>
                  <input
                    type="number"
                    className={inputCls}
                    placeholder="175"
                    value={registerForm.heightCm}
                    onChange={e => setRegisterForm(f => ({ ...f, heightCm: e.target.value }))}
                    required
                    min={50}
                    max={300}
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Goal</label>
                <select
                  className={inputCls}
                  value={registerForm.goalType}
                  onChange={e => setRegisterForm(f => ({ ...f, goalType: e.target.value as GoalType }))}
                >
                  <option value="">Select a goal</option>
                  {(Object.keys(GOAL_LABELS) as GoalType[]).map(g => (
                    <option key={g} value={g}>{GOAL_LABELS[g]}</option>
                  ))}
                </select>
              </div>
              {error && (
                <p className="text-sm text-red-400 bg-red-900/20 rounded-lg px-3 py-2">{error}</p>
              )}
              <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-lg transition-colors"
              >
                {loading ? 'Creating account…' : 'Get Started'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}
