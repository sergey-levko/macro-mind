import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useUser } from '../context/UserContext'
import type { GoalType, CreateUserRequest, UserResponse } from '../lib/types'

const GOAL_LABELS: Record<GoalType, string> = {
  LOSE_WEIGHT: 'Lose Weight',
  MAINTAIN_WEIGHT: 'Maintain Weight',
  GAIN_MUSCLE: 'Gain Muscle',
}

interface Errors {
  name?: string
  email?: string
  age?: string
  weightKg?: string
  heightCm?: string
  goalType?: string
  submit?: string
}

export default function Register() {
  const navigate = useNavigate()
  const { setUserId } = useUser()

  const [form, setForm] = useState({
    name: '',
    email: '',
    age: '',
    weightKg: '',
    heightCm: '',
    goalType: '' as GoalType | '',
  })
  const [errors, setErrors] = useState<Errors>({})
  const [loading, setLoading] = useState(false)

  function validate(): Errors {
    const e: Errors = {}
    if (!form.name.trim()) e.name = 'Name is required'
    if (!form.email.trim()) e.email = 'Email is required'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Invalid email'
    const age = Number(form.age)
    if (!form.age) e.age = 'Age is required'
    else if (age < 1 || age > 120) e.age = 'Age must be 1–120'
    const weight = Number(form.weightKg)
    if (!form.weightKg) e.weightKg = 'Weight is required'
    else if (weight < 20 || weight > 500) e.weightKg = 'Weight must be 20–500 kg'
    const height = Number(form.heightCm)
    if (!form.heightCm) e.heightCm = 'Height is required'
    else if (height < 50 || height > 300) e.heightCm = 'Height must be 50–300 cm'
    if (!form.goalType) e.goalType = 'Goal is required'
    return e
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length > 0) { setErrors(errs); return }
    setErrors({})
    setLoading(true)
    try {
      const body: CreateUserRequest = {
        name: form.name.trim(),
        email: form.email.trim(),
        age: Number(form.age),
        weightKg: Number(form.weightKg),
        heightCm: Number(form.heightCm),
        goalType: form.goalType as GoalType,
      }
      const res = await fetch('/api/v1/users', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      if (!res.ok) {
        const text = await res.text().catch(() => res.statusText)
        setErrors({ submit: `Registration failed: ${text}` })
        return
      }
      const user: UserResponse = await res.json()
      setUserId(user.id)
      navigate('/dashboard')
    } catch (err) {
      setErrors({ submit: 'Network error — is the backend running?' })
    } finally {
      setLoading(false)
    }
  }

  function field(
    id: keyof typeof form,
    label: string,
    type = 'text',
    placeholder = ''
  ) {
    return (
      <div>
        <label htmlFor={id} className="block text-sm font-medium text-gray-300 mb-1">
          {label}
        </label>
        <input
          id={id}
          type={type}
          placeholder={placeholder}
          value={form[id]}
          onChange={e => setForm(f => ({ ...f, [id]: e.target.value }))}
          className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500"
        />
        {errors[id] && <p className="mt-1 text-xs text-red-400">{errors[id]}</p>}
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-white">
            Macro<span className="text-teal-400">Mind</span>
          </h1>
          <p className="text-gray-400 mt-2">Create your profile to get started</p>
        </div>
        <form onSubmit={handleSubmit} className="bg-gray-900 rounded-2xl p-8 space-y-4 border border-gray-800">
          {field('name', 'Full Name', 'text', 'Jane Smith')}
          {field('email', 'Email', 'email', 'jane@example.com')}
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label htmlFor="age" className="block text-sm font-medium text-gray-300 mb-1">Age</label>
              <input
                id="age"
                type="number"
                placeholder="30"
                value={form.age}
                onChange={e => setForm(f => ({ ...f, age: e.target.value }))}
                className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500"
              />
              {errors.age && <p className="mt-1 text-xs text-red-400">{errors.age}</p>}
            </div>
            <div>
              <label htmlFor="weightKg" className="block text-sm font-medium text-gray-300 mb-1">Weight (kg)</label>
              <input
                id="weightKg"
                type="number"
                step="0.1"
                placeholder="70"
                value={form.weightKg}
                onChange={e => setForm(f => ({ ...f, weightKg: e.target.value }))}
                className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500"
              />
              {errors.weightKg && <p className="mt-1 text-xs text-red-400">{errors.weightKg}</p>}
            </div>
            <div>
              <label htmlFor="heightCm" className="block text-sm font-medium text-gray-300 mb-1">Height (cm)</label>
              <input
                id="heightCm"
                type="number"
                placeholder="175"
                value={form.heightCm}
                onChange={e => setForm(f => ({ ...f, heightCm: e.target.value }))}
                className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500"
              />
              {errors.heightCm && <p className="mt-1 text-xs text-red-400">{errors.heightCm}</p>}
            </div>
          </div>
          <div>
            <label htmlFor="goalType" className="block text-sm font-medium text-gray-300 mb-1">Goal</label>
            <select
              id="goalType"
              value={form.goalType}
              onChange={e => setForm(f => ({ ...f, goalType: e.target.value as GoalType }))}
              className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-teal-500"
            >
              <option value="">Select a goal</option>
              {(Object.keys(GOAL_LABELS) as GoalType[]).map(g => (
                <option key={g} value={g}>{GOAL_LABELS[g]}</option>
              ))}
            </select>
            {errors.goalType && <p className="mt-1 text-xs text-red-400">{errors.goalType}</p>}
          </div>
          {errors.submit && (
            <p className="text-sm text-red-400 bg-red-900/20 rounded-lg px-3 py-2">{errors.submit}</p>
          )}
          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-lg transition-colors"
          >
            {loading ? 'Creating account…' : 'Get Started'}
          </button>
        </form>
      </div>
    </div>
  )
}
