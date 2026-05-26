import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import type { GoalType } from '../lib/types'

const GOAL_LABELS: Record<GoalType, string> = {
  LOSE_WEIGHT: 'Lose Weight',
  MAINTAIN_WEIGHT: 'Maintain Weight',
  GAIN_MUSCLE: 'Gain Muscle',
}

export default function Onboarding() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleGenerate() {
    setError(null)
    setLoading(true)
    try {
      const suggested = await api.post<{
        caloriesTarget: number
        proteinG: number
        carbsG: number
        fatG: number
      }>('/api/v1/nutritional-goals/generate')
      await api.put('/api/v1/nutritional-goals', {
        caloriesTarget: suggested.caloriesTarget,
        proteinG: suggested.proteinG,
        carbsG: suggested.carbsG,
        fatG: suggested.fatG,
      })
      navigate('/dashboard', { replace: true })
    } catch {
      setError('Something went wrong generating your goals. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  function handleSkip() {
    navigate('/dashboard', { replace: true })
  }

  const goalLabel = user?.goalType ? GOAL_LABELS[user.goalType] : null

  return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center px-4">
      <div className="w-full max-w-md text-center">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-white">
            Macro<span className="text-teal-400">Mind</span>
          </h1>
          <p className="text-gray-400 mt-2">
            Welcome{user?.name ? `, ${user.name}` : ''}!
          </p>
        </div>

        <div className="bg-gray-900 rounded-2xl p-8 border border-gray-800 space-y-6">
          <div>
            <div className="w-14 h-14 bg-teal-600/20 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-7 h-7 text-teal-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <h2 className="text-xl font-semibold text-white">One last step</h2>
            <p className="text-gray-400 mt-2 text-sm leading-relaxed">
              Let us calculate your personalised daily macro targets using AI.
              {goalLabel && (
                <span className="block mt-1 text-gray-500">
                  Goal: <span className="text-teal-400">{goalLabel}</span>
                </span>
              )}
            </p>
          </div>

          {error && (
            <p className="text-sm text-red-400 bg-red-900/20 rounded-lg px-3 py-2">{error}</p>
          )}

          <div className="space-y-3">
            <button
              onClick={handleGenerate}
              disabled={loading}
              className="w-full py-2.5 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-lg transition-colors flex items-center justify-center gap-2"
            >
              {loading ? (
                <>
                  <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                  </svg>
                  Calculating your macros…
                </>
              ) : (
                'Generate my macros'
              )}
            </button>

            <button
              onClick={handleSkip}
              disabled={loading}
              className="w-full py-2 text-sm text-gray-500 hover:text-gray-300 transition-colors disabled:opacity-50"
            >
              Skip for now
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
