import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { useToast } from '../components/Toast'

interface UserSettingsResponse {
  usdaEnabled: boolean
}

export default function Settings() {
  const { showToast } = useToast()
  const [usdaEnabled, setUsdaEnabled] = useState(true)
  const [saving, setSaving] = useState(false)
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    api.get<UserSettingsResponse>('/api/v1/settings').then(s => {
      setUsdaEnabled(s.usdaEnabled)
      setLoaded(true)
    })
  }, [])

  async function handleToggle() {
    const newValue = !usdaEnabled
    const previous = usdaEnabled
    setUsdaEnabled(newValue)
    setSaving(true)
    try {
      await api.put<UserSettingsResponse>('/api/v1/settings', { usdaEnabled: newValue })
    } catch {
      setUsdaEnabled(previous)
      showToast('Failed to save settings, please try again')
    } finally {
      setSaving(false)
    }
  }

  if (!loaded) {
    return (
      <div className="p-8 text-gray-400 text-sm">Loading settings…</div>
    )
  }

  return (
    <div className="p-8 max-w-xl">
      <h1 className="text-2xl font-bold text-white mb-6">Settings</h1>
      <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
        <h2 className="text-base font-semibold text-white mb-4">Food Database</h2>
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-white">Use USDA food database</p>
            <p className="text-xs text-gray-400 mt-0.5">
              Enable searching and importing foods from USDA FoodData Central
            </p>
          </div>
          <button
            role="switch"
            aria-checked={usdaEnabled}
            onClick={handleToggle}
            disabled={saving}
            className={`relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors focus:outline-none focus:ring-2 focus:ring-teal-500 focus:ring-offset-2 focus:ring-offset-gray-900 disabled:opacity-50 disabled:cursor-not-allowed ${
              usdaEnabled ? 'bg-teal-600' : 'bg-gray-700'
            }`}
          >
            <span
              className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
                usdaEnabled ? 'translate-x-5' : 'translate-x-0'
              }`}
            />
          </button>
        </div>
      </div>
    </div>
  )
}
