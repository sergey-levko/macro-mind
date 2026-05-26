import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { useToast } from '../components/Toast'
import type { UserResponse, GoalType } from '../lib/types'

const GOAL_OPTIONS: { value: GoalType; label: string }[] = [
  { value: 'LOSE_WEIGHT', label: 'Lose Weight' },
  { value: 'MAINTAIN_WEIGHT', label: 'Maintain Weight' },
  { value: 'GAIN_MUSCLE', label: 'Gain Muscle' },
]

export default function Profile() {
  const { showToast } = useToast()
  const [profile, setProfile] = useState<UserResponse | null>(null)
  const [form, setForm] = useState({
    name: '',
    age: '',
    weightKg: '',
    heightCm: '',
    goalType: 'MAINTAIN_WEIGHT' as GoalType,
  })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    api.get<UserResponse>('/api/v1/users/me').then(u => {
      setProfile(u)
      setForm({
        name: u.name,
        age: String(u.age ?? ''),
        weightKg: String(u.weightKg ?? ''),
        heightCm: String(u.heightCm ?? ''),
        goalType: u.goalType,
      })
    })
  }, [])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    try {
      const updated = await api.put<UserResponse>('/api/v1/users/me', {
        name: form.name,
        age: Number(form.age),
        weightKg: Number(form.weightKg),
        heightCm: Number(form.heightCm),
        goalType: form.goalType,
      })
      setProfile(updated)
      showToast('Profile updated', 'success')
    } catch {
      showToast('Failed to update profile, please try again')
    } finally {
      setSaving(false)
    }
  }

  const inputCls = 'w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-teal-500'

  return (
    <div className="p-8 max-w-lg">
      <h1 className="text-2xl font-bold text-white mb-6">Profile</h1>
      <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs text-gray-400 mb-1">Email</label>
            <input
              type="email"
              className={`${inputCls} opacity-50 cursor-not-allowed`}
              value={profile?.email ?? ''}
              readOnly
              disabled
            />
          </div>
          <div>
            <label className="block text-xs text-gray-400 mb-1">Name</label>
            <input
              type="text"
              className={inputCls}
              value={form.name}
              onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
              required
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs text-gray-400 mb-1">Age</label>
              <input
                type="number"
                className={inputCls}
                value={form.age}
                onChange={e => setForm(f => ({ ...f, age: e.target.value }))}
                required
                min={1}
              />
            </div>
            <div>
              <label className="block text-xs text-gray-400 mb-1">Weight (kg)</label>
              <input
                type="number"
                className={inputCls}
                value={form.weightKg}
                onChange={e => setForm(f => ({ ...f, weightKg: e.target.value }))}
                required
                min={1}
                step="0.1"
              />
            </div>
            <div>
              <label className="block text-xs text-gray-400 mb-1">Height (cm)</label>
              <input
                type="number"
                className={inputCls}
                value={form.heightCm}
                onChange={e => setForm(f => ({ ...f, heightCm: e.target.value }))}
                required
                min={1}
                step="0.1"
              />
            </div>
            <div>
              <label className="block text-xs text-gray-400 mb-1">Goal</label>
              <select
                className={inputCls}
                value={form.goalType}
                onChange={e => setForm(f => ({ ...f, goalType: e.target.value as GoalType }))}
              >
                {GOAL_OPTIONS.map(o => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>
          </div>
          <button
            type="submit"
            disabled={saving}
            className="w-full py-2 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 text-white text-sm font-semibold rounded-lg transition-colors mt-2"
          >
            {saving ? 'Saving…' : 'Save'}
          </button>
        </form>
      </div>
    </div>
  )
}
