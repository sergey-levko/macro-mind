import { useEffect, useState } from 'react'
import { api, getToken } from '../lib/api'
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
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirmNewPassword: '' })
  const [pwSaving, setPwSaving] = useState(false)
  const [pwError, setPwError] = useState<string | null>(null)

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

  async function handlePasswordSubmit(e: React.FormEvent) {
    e.preventDefault()
    setPwError(null)
    if (pwForm.newPassword !== pwForm.confirmNewPassword) {
      setPwError('New passwords do not match')
      return
    }
    setPwSaving(true)
    try {
      const res = await fetch('/api/v1/users/me/password', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${getToken()}`,
        },
        body: JSON.stringify({ currentPassword: pwForm.currentPassword, newPassword: pwForm.newPassword }),
      })
      if (res.status === 204) {
        showToast('Password updated', 'success')
        setPwForm({ currentPassword: '', newPassword: '', confirmNewPassword: '' })
      } else if (res.status === 401) {
        setPwError('Current password is incorrect')
      } else if (res.status === 400) {
        const data = await res.json().catch(() => null)
        setPwError(data?.message ?? 'Invalid input')
      } else {
        setPwError('Failed to update password, please try again')
      }
    } catch {
      setPwError('Failed to update password, please try again')
    } finally {
      setPwSaving(false)
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
      <h2 className="text-lg font-semibold text-white mt-8 mb-4">Change Password</h2>
      <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
        <form onSubmit={handlePasswordSubmit} className="space-y-4">
          {pwError && (
            <p className="text-sm text-red-400">{pwError}</p>
          )}
          <div>
            <label className="block text-xs text-gray-400 mb-1">Current Password</label>
            <input
              type="password"
              className={inputCls}
              value={pwForm.currentPassword}
              onChange={e => setPwForm(f => ({ ...f, currentPassword: e.target.value }))}
              required
            />
          </div>
          <div>
            <label className="block text-xs text-gray-400 mb-1">New Password</label>
            <input
              type="password"
              className={inputCls}
              value={pwForm.newPassword}
              onChange={e => setPwForm(f => ({ ...f, newPassword: e.target.value }))}
              required
              minLength={8}
            />
          </div>
          <div>
            <label className="block text-xs text-gray-400 mb-1">Confirm New Password</label>
            <input
              type="password"
              className={inputCls}
              value={pwForm.confirmNewPassword}
              onChange={e => setPwForm(f => ({ ...f, confirmNewPassword: e.target.value }))}
              required
              minLength={8}
            />
          </div>
          <button
            type="submit"
            disabled={pwSaving}
            className="w-full py-2 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 text-white text-sm font-semibold rounded-lg transition-colors mt-2"
          >
            {pwSaving ? 'Updating…' : 'Update Password'}
          </button>
        </form>
      </div>
    </div>
  )
}
