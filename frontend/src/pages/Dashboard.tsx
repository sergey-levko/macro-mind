import { useEffect, useState, useCallback } from 'react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts'
import { api } from '../lib/api'
import { useToast } from '../components/Toast'
import type { SummaryCard, WeeklySummary, NutritionalGoal } from '../lib/types'

// ─── Summary Card ────────────────────────────────────────────────────────────

interface MacroBarProps {
  label: string
  value: number
  target: number | null
  pct: number | null
  unit?: string
  color: string
}

function MacroBar({ label, value, target, pct, unit = 'g', color }: MacroBarProps) {
  const displayPct = pct ?? 0
  const overTarget = displayPct > 100
  const barColor = overTarget ? '#f87171' : color
  const width = Math.min(displayPct, 200) / 2 // map 0–200% → 0–100% width

  return (
    <div>
      <div className="flex justify-between text-sm mb-1">
        <span className="text-gray-300 font-medium">{label}</span>
        <span className="text-gray-400">
          {label === 'Calories' ? `${value} / ${target ?? '—'} kcal` : `${value} / ${target ?? '—'} ${unit}`}
          {pct !== null && <span className={`ml-2 font-semibold ${overTarget ? 'text-red-400' : 'text-teal-400'}`}>{pct}%</span>}
        </span>
      </div>
      <div className="h-2 bg-gray-800 rounded-full overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-500"
          style={{ width: `${target !== null ? width : 0}%`, backgroundColor: barColor }}
        />
      </div>
    </div>
  )
}

// ─── Goal Form ────────────────────────────────────────────────────────────────

interface GoalFormProps {
  existing: NutritionalGoal | null
  onSaved: () => void
}

function GoalForm({ existing, onSaved }: GoalFormProps) {
  const { showToast } = useToast()
  const [open, setOpen] = useState(!existing)
  const [form, setForm] = useState({
    caloriesTarget: existing?.caloriesTarget?.toString() ?? '',
    proteinG: existing?.proteinG?.toString() ?? '',
    carbsG: existing?.carbsG?.toString() ?? '',
    fatG: existing?.fatG?.toString() ?? '',
  })
  const [saving, setSaving] = useState(false)
  const [generating, setGenerating] = useState(false)

  useEffect(() => {
    if (existing) {
      setForm({
        caloriesTarget: existing.caloriesTarget.toString(),
        proteinG: existing.proteinG.toString(),
        carbsG: existing.carbsG.toString(),
        fatG: existing.fatG.toString(),
      })
    }
  }, [existing])

  async function handleGenerate() {
    setGenerating(true)
    try {
      const s = await api.post<{ caloriesTarget: number; proteinG: number; carbsG: number; fatG: number }>(
        '/api/v1/nutritional-goals/generate'
      )
      setForm({
        caloriesTarget: String(s.caloriesTarget),
        proteinG: String(s.proteinG),
        carbsG: String(s.carbsG),
        fatG: String(s.fatG),
      })
    } catch {
      showToast('Generation failed, please try again')
    } finally {
      setGenerating(false)
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    try {
      await api.put('/api/v1/nutritional-goals', {
        caloriesTarget: Number(form.caloriesTarget),
        proteinG: Number(form.proteinG),
        carbsG: Number(form.carbsG),
        fatG: Number(form.fatG),
      })
      setOpen(false)
      onSaved()
    } catch {
      showToast('Failed to save goal, please try again')
    } finally {
      setSaving(false)
    }
  }

  const inputCls = 'w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-teal-500'

  return (
    <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-white">Nutritional Goal</h2>
        <button
          onClick={() => setOpen(o => !o)}
          className="text-sm text-teal-400 hover:text-teal-300"
        >
          {open ? 'Cancel' : 'Edit'}
        </button>
      </div>
      {!open && existing && (
        <div className="grid grid-cols-4 gap-4 text-center">
          {[
            { label: 'Calories', value: existing.caloriesTarget, unit: 'kcal' },
            { label: 'Protein', value: existing.proteinG, unit: 'g' },
            { label: 'Carbs', value: existing.carbsG, unit: 'g' },
            { label: 'Fat', value: existing.fatG, unit: 'g' },
          ].map(m => (
            <div key={m.label}>
              <p className="text-2xl font-bold text-white">{m.value}</p>
              <p className="text-xs text-gray-400">{m.label} / {m.unit}</p>
            </div>
          ))}
        </div>
      )}
      {open && (
        <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs text-gray-400 mb-1">Calories (kcal)</label>
            <input type="number" className={inputCls} value={form.caloriesTarget}
              onChange={e => setForm(f => ({ ...f, caloriesTarget: e.target.value }))} required min={1} />
          </div>
          <div>
            <label className="block text-xs text-gray-400 mb-1">Protein (g)</label>
            <input type="number" className={inputCls} value={form.proteinG}
              onChange={e => setForm(f => ({ ...f, proteinG: e.target.value }))} required min={0} />
          </div>
          <div>
            <label className="block text-xs text-gray-400 mb-1">Carbs (g)</label>
            <input type="number" className={inputCls} value={form.carbsG}
              onChange={e => setForm(f => ({ ...f, carbsG: e.target.value }))} required min={0} />
          </div>
          <div>
            <label className="block text-xs text-gray-400 mb-1">Fat (g)</label>
            <input type="number" className={inputCls} value={form.fatG}
              onChange={e => setForm(f => ({ ...f, fatG: e.target.value }))} required min={0} />
          </div>
          <div className="col-span-2 flex flex-col gap-2">
            <button
              type="button"
              onClick={handleGenerate}
              disabled={generating}
              className="w-full py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white text-sm font-semibold rounded-lg transition-colors"
            >
              {generating ? 'Generating…' : 'Generate with AI'}
            </button>
            <button
              type="submit"
              disabled={saving}
              className="w-full py-2 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 text-white text-sm font-semibold rounded-lg transition-colors"
            >
              {saving ? 'Saving…' : 'Save Goal'}
            </button>
          </div>
        </form>
      )}
    </div>
  )
}

// ─── Weekly Chart ─────────────────────────────────────────────────────────────

function getMonday(d = new Date()): string {
  const date = new Date(d)
  const day = date.getDay()
  const diff = (day === 0 ? -6 : 1) - day
  date.setDate(date.getDate() + diff)
  return date.toISOString().split('T')[0]
}

interface WeeklyChartProps {
  data: WeeklySummary
}

function WeeklyChart({ data }: WeeklyChartProps) {
  const chartData = data.days.map(d => ({
    day: new Date(d.date + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'short' }),
    calories: Math.round(Number(d.totals.caloriesKcal)),
  }))
  const target = data.weeklyTargets ? Number(data.weeklyTargets.caloriesTarget) / 7 : null

  return (
    <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
      <h2 className="text-lg font-semibold text-white mb-4">This Week</h2>
      <ResponsiveContainer width="100%" height={180}>
        <BarChart data={chartData} barSize={24}>
          <XAxis dataKey="day" tick={{ fill: '#9ca3af', fontSize: 12 }} axisLine={false} tickLine={false} />
          <YAxis hide />
          <Tooltip
            contentStyle={{ backgroundColor: '#1f2937', border: '1px solid #374151', borderRadius: 8 }}
            labelStyle={{ color: '#e5e7eb' }}
            formatter={(v: number) => [`${v} kcal`, 'Calories']}
          />
          <Bar dataKey="calories" radius={[4, 4, 0, 0]}>
            {chartData.map((entry, i) => (
              <Cell key={i} fill={entry.calories > 0 ? '#14b8a6' : '#374151'} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
      {target !== null && (
        <p className="text-xs text-gray-500 mt-2">Daily target: {Math.round(target)} kcal</p>
      )}
    </div>
  )
}

// ─── Dashboard Page ──────────────────────────────────────────────────────────

export default function Dashboard() {
  const [summary, setSummary] = useState<SummaryCard | null>(null)
  const [weekly, setWeekly] = useState<WeeklySummary | null>(null)
  const [goal, setGoal] = useState<NutritionalGoal | null | undefined>(undefined)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [s, w, g] = await Promise.allSettled([
        api.get<SummaryCard>('/api/v1/dashboard/summary'),
        api.get<WeeklySummary>(`/api/v1/dashboard/weekly?weekStart=${getMonday()}`),
        api.get<NutritionalGoal>('/api/v1/nutritional-goals'),
      ])
      if (s.status === 'fulfilled') setSummary(s.value)
      if (w.status === 'fulfilled') setWeekly(w.value)
      setGoal(g.status === 'fulfilled' ? g.value : null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  if (loading) {
    return (
      <div className="p-8 flex items-center justify-center min-h-64">
        <div className="text-gray-400">Loading…</div>
      </div>
    )
  }

  const totals = summary?.totals
  const targets = summary?.targets
  const pcts = summary?.percentages

  return (
    <div className="p-8 space-y-6 max-w-4xl">
      <h1 className="text-2xl font-bold text-white">Dashboard</h1>

      {/* Summary card */}
      <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6 space-y-4">
        <h2 className="text-lg font-semibold text-white">Today's Intake</h2>
        {!targets && (
          <p className="text-sm text-amber-400">No nutritional goal set — set one below to see progress.</p>
        )}
        <MacroBar
          label="Calories"
          value={Math.round(Number(totals?.caloriesKcal ?? 0))}
          target={targets ? Math.round(Number(targets.caloriesTarget)) : null}
          pct={pcts?.caloriesPct ?? null}
          unit="kcal"
          color="#14b8a6"
        />
        <MacroBar
          label="Protein"
          value={Math.round(Number(totals?.proteinG ?? 0))}
          target={targets ? Math.round(Number(targets.proteinG)) : null}
          pct={pcts?.proteinPct ?? null}
          color="#818cf8"
        />
        <MacroBar
          label="Carbs"
          value={Math.round(Number(totals?.carbsG ?? 0))}
          target={targets ? Math.round(Number(targets.carbsG)) : null}
          pct={pcts?.carbsPct ?? null}
          color="#fb923c"
        />
        <MacroBar
          label="Fat"
          value={Math.round(Number(totals?.fatG ?? 0))}
          target={targets ? Math.round(Number(targets.fatG)) : null}
          pct={pcts?.fatPct ?? null}
          color="#facc15"
        />
      </div>

      {/* Weekly chart */}
      {weekly && <WeeklyChart data={weekly} />}

      {/* Goal form */}
      {goal !== undefined && <GoalForm existing={goal} onSaved={load} />}
    </div>
  )
}
