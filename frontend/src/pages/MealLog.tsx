import { useEffect, useState, useCallback, useRef } from 'react'
import { DayPicker } from 'react-day-picker'
import { api } from '../lib/api'
import type { MealLog, MealLogSummary, MealType, Food, MealItemResponse, UsdaFoodResult } from '../lib/types'

const MEAL_TYPES: MealType[] = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK']
const MEAL_LABELS: Record<MealType, string> = {
  BREAKFAST: 'Breakfast',
  LUNCH: 'Lunch',
  DINNER: 'Dinner',
  SNACK: 'Snack',
}

function todayIso(): string {
  return new Date().toISOString().split('T')[0]
}

function shiftDay(iso: string, delta: number): string {
  const d = new Date(iso + 'T12:00:00')
  d.setDate(d.getDate() + delta)
  return d.toISOString().split('T')[0]
}

function formatDateLabel(iso: string): string {
  const today = todayIso()
  const yesterday = shiftDay(today, -1)
  const d = new Date(iso + 'T12:00:00')
  const dateStr = d.toLocaleDateString('en-US', { month: 'long', day: 'numeric' })
  if (iso === today) return `Today, ${dateStr}`
  if (iso === yesterday) return `Yesterday, ${dateStr}`
  return d.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })
}

// ─── Food Item Form ───────────────────────────────────────────────────────────

interface FoodItemFormProps {
  logId: string
  onAdded: () => void
}

interface CreateFoodFormProps {
  name: string
  onCreated: (food: Food) => void
  onCancel: () => void
}

function CreateFoodForm({ name, onCreated, onCancel }: CreateFoodFormProps) {
  const [form, setForm] = useState({ name, calories100g: '', proteinG: '', carbsG: '', fatG: '' })
  const [saving, setSaving] = useState(false)

  async function handleCreate() {
    setSaving(true)
    try {
      const food = await api.post<Food>('/api/v1/foods', {
        name: form.name.trim(),
        calories100g: Number(form.calories100g),
        proteinG: Number(form.proteinG),
        carbsG: Number(form.carbsG),
        fatG: Number(form.fatG),
      })
      onCreated(food)
    } finally {
      setSaving(false)
    }
  }

  const inputCls = 'w-full px-2 py-1 bg-gray-700 border border-gray-600 rounded text-xs text-white focus:outline-none focus:ring-1 focus:ring-teal-500'

  return (
    <div className="mt-2 p-3 bg-gray-750 border border-gray-600 rounded-lg space-y-2">
      <p className="text-xs font-medium text-teal-400">New food</p>
      <input className={inputCls} placeholder="Name" value={form.name}
        onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required />
      <div className="grid grid-cols-2 gap-2">
        <div>
          <label className="block text-xs text-gray-500 mb-0.5">Calories / 100g</label>
          <input type="number" className={inputCls} placeholder="0" min={0} value={form.calories100g}
            onChange={e => setForm(f => ({ ...f, calories100g: e.target.value }))} required />
        </div>
        <div>
          <label className="block text-xs text-gray-500 mb-0.5">Protein (g)</label>
          <input type="number" className={inputCls} placeholder="0" min={0} value={form.proteinG}
            onChange={e => setForm(f => ({ ...f, proteinG: e.target.value }))} required />
        </div>
        <div>
          <label className="block text-xs text-gray-500 mb-0.5">Carbs (g)</label>
          <input type="number" className={inputCls} placeholder="0" min={0} value={form.carbsG}
            onChange={e => setForm(f => ({ ...f, carbsG: e.target.value }))} required />
        </div>
        <div>
          <label className="block text-xs text-gray-500 mb-0.5">Fat (g)</label>
          <input type="number" className={inputCls} placeholder="0" min={0} value={form.fatG}
            onChange={e => setForm(f => ({ ...f, fatG: e.target.value }))} required />
        </div>
      </div>
      <div className="flex gap-2">
        <button type="button" disabled={saving} onClick={handleCreate}
          className="flex-1 py-1 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 text-white text-xs rounded transition-colors">
          {saving ? 'Creating…' : 'Create & select'}
        </button>
        <button type="button" onClick={onCancel}
          className="px-3 py-1 text-gray-400 hover:text-gray-200 text-xs">
          Cancel
        </button>
      </div>
    </div>
  )
}

function FoodItemForm({ logId, onAdded }: FoodItemFormProps) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<Food[]>([])
  const [usdaResults, setUsdaResults] = useState<UsdaFoodResult[]>([])
  const [noResults, setNoResults] = useState(false)
  const [selected, setSelected] = useState<Food | null>(null)
  const [quantity, setQuantity] = useState('')
  const [adding, setAdding] = useState(false)
  const [showCreate, setShowCreate] = useState(false)
  const [importingFdcId, setImportingFdcId] = useState<number | null>(null)
  const debounceRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined)

  function handleQueryChange(v: string) {
    setQuery(v)
    setSelected(null)
    setShowCreate(false)
    setNoResults(false)
    setUsdaResults([])
    clearTimeout(debounceRef.current)
    if (v.length < 2) { setResults([]); return }
    debounceRef.current = setTimeout(async () => {
      try {
        const [foods, usda] = await Promise.all([
          api.get<Food[]>(`/api/v1/foods?search=${encodeURIComponent(v)}`),
          api.get<UsdaFoodResult[]>(`/api/v1/foods/usda-search?q=${encodeURIComponent(v)}`),
        ])
        setResults(foods)
        setUsdaResults(usda)
        setNoResults(foods.length === 0 && usda.length === 0)
      } catch {
        setResults([])
        setUsdaResults([])
      }
    }, 300)
  }

  async function handleImportUsda(u: UsdaFoodResult) {
    setImportingFdcId(u.fdcId)
    try {
      const food = await api.post<Food>('/api/v1/foods/import', { fdcId: u.fdcId })
      setSelected(food)
      setQuery(food.name)
      setResults([])
      setUsdaResults([])
      setNoResults(false)
      setShowCreate(false)
    } finally {
      setImportingFdcId(null)
    }
  }

  function handleFoodCreated(food: Food) {
    setSelected(food)
    setQuery(food.name)
    setResults([])
    setUsdaResults([])
    setNoResults(false)
    setShowCreate(false)
  }

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault()
    if (!selected || !quantity) return
    setAdding(true)
    try {
      await api.post(`/api/v1/meal-logs/${logId}/items`, {
        foodId: selected.id,
        quantityG: Number(quantity),
      })
      setQuery('')
      setSelected(null)
      setQuantity('')
      setResults([])
      setNoResults(false)
      setShowCreate(false)
      onAdded()
    } finally {
      setAdding(false)
    }
  }

  return (
    <form onSubmit={handleAdd} className="mt-3 pt-3 border-t border-gray-800 space-y-2">
      <div className="relative">
        <input
          type="text"
          placeholder="Search foods…"
          value={selected ? selected.name : query}
          onChange={e => handleQueryChange(e.target.value)}
          className="w-full px-3 py-1.5 bg-gray-800 border border-gray-700 rounded-lg text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-1 focus:ring-teal-500"
        />
        {(results.length > 0 || usdaResults.length > 0 || noResults) && !selected && !showCreate && (
          <ul className="absolute z-10 top-full left-0 right-0 mt-1 bg-gray-800 border border-gray-700 rounded-lg overflow-hidden shadow-lg max-h-52 overflow-y-auto">
            {results.map(f => (
              <li key={f.id}>
                <button
                  type="button"
                  onClick={() => { setSelected(f); setResults([]); setUsdaResults([]); setNoResults(false); setQuery(f.name) }}
                  className="w-full text-left px-3 py-2 text-sm text-gray-200 hover:bg-gray-700"
                >
                  {f.name}
                  <span className="text-gray-500 ml-2 text-xs">{f.calories100g} kcal/100g</span>
                </button>
              </li>
            ))}
            {usdaResults.length > 0 && (
              <>
                <li className="px-3 py-1 text-xs text-gray-500 bg-gray-900 border-t border-gray-700">
                  USDA FoodData Central
                </li>
                {usdaResults.map(u => (
                  <li key={u.fdcId}>
                    <button
                      type="button"
                      disabled={importingFdcId === u.fdcId}
                      onClick={() => handleImportUsda(u)}
                      className="w-full text-left px-3 py-2 text-sm text-gray-200 hover:bg-gray-700 flex items-center justify-between disabled:opacity-60"
                    >
                      <span>{importingFdcId === u.fdcId ? 'Importing…' : u.description}</span>
                      <span className="text-xs text-teal-400 ml-2 shrink-0">USDA</span>
                    </button>
                  </li>
                ))}
              </>
            )}
            <li className="border-t border-gray-700">
              <button
                type="button"
                onClick={() => setShowCreate(true)}
                className="w-full text-left px-3 py-2 text-xs text-teal-400 hover:bg-gray-700"
              >
                + Create "{query}" manually
              </button>
            </li>
          </ul>
        )}
      </div>
      {showCreate && (
        <CreateFoodForm
          name={query}
          onCreated={handleFoodCreated}
          onCancel={() => setShowCreate(false)}
        />
      )}
      <div className="flex gap-2">
        <input
          type="number"
          placeholder="Quantity (g)"
          value={quantity}
          onChange={e => setQuantity(e.target.value)}
          min={1}
          className="flex-1 px-3 py-1.5 bg-gray-800 border border-gray-700 rounded-lg text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-1 focus:ring-teal-500"
        />
        <button
          type="submit"
          disabled={!selected || !quantity || adding}
          className="px-4 py-1.5 bg-teal-600 hover:bg-teal-500 disabled:opacity-40 text-white text-sm rounded-lg transition-colors"
        >
          {adding ? '…' : 'Add'}
        </button>
      </div>
    </form>
  )
}

// ─── Meal Log Card ────────────────────────────────────────────────────────────

interface MealLogCardProps {
  log: MealLogSummary
  onDeleted: () => void
  onItemChanged: () => void
}

function MealLogCard({ log, onDeleted, onItemChanged }: MealLogCardProps) {
  const [expanded, setExpanded] = useState(false)
  const [detail, setDetail] = useState<MealLog | null>(null)
  const [loadingDetail, setLoadingDetail] = useState(false)
  const [deleting, setDeleting] = useState(false)

  async function toggleExpand() {
    if (!expanded) {
      setLoadingDetail(true)
      try {
        const d = await api.get<MealLog>(`/api/v1/meal-logs/${log.id}`)
        setDetail(d)
      } finally {
        setLoadingDetail(false)
      }
    }
    setExpanded(e => !e)
  }

  async function refreshDetail() {
    try {
      const d = await api.get<MealLog>(`/api/v1/meal-logs/${log.id}`)
      setDetail(d)
    } catch { /* ignore */ }
    onItemChanged()
  }

  async function removeItem(itemId: string) {
    await api.delete(`/api/v1/meal-logs/${log.id}/items/${itemId}`)
    refreshDetail()
  }

  async function deleteMeal() {
    if (!confirm('Delete this meal log?')) return
    setDeleting(true)
    try {
      await api.delete(`/api/v1/meal-logs/${log.id}`)
      onDeleted()
    } finally {
      setDeleting(false)
    }
  }

  const t = log.totals

  return (
    <div className="bg-gray-800 rounded-xl p-4">
      <div className="flex items-center justify-between">
        <button onClick={toggleExpand} className="flex-1 text-left">
          <div className="flex items-center gap-3">
            <span className="text-sm font-medium text-white">
              {new Date(log.loggedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
            <span className="text-xs text-gray-500">
              {Math.round(Number(t.caloriesKcal))} kcal · P {Math.round(Number(t.proteinG))}g · C {Math.round(Number(t.carbsG))}g · F {Math.round(Number(t.fatG))}g
            </span>
          </div>
        </button>
        <button
          onClick={deleteMeal}
          disabled={deleting}
          className="ml-3 text-gray-500 hover:text-red-400 text-sm transition-colors"
          title="Delete meal"
        >
          {deleting ? '…' : '✕'}
        </button>
      </div>

      {expanded && (
        <div className="mt-3">
          {loadingDetail && <p className="text-xs text-gray-500">Loading…</p>}
          {detail && (
            <>
              {detail.items.length === 0 && (
                <p className="text-xs text-gray-500 mb-2">No items yet.</p>
              )}
              {detail.items.map((item: MealItemResponse) => (
                <div key={item.itemId} className="flex items-center justify-between py-1.5 border-t border-gray-700 first:border-0">
                  <div>
                    <span className="text-sm text-gray-200">{item.foodName}</span>
                    <span className="text-xs text-gray-500 ml-2">{item.quantityG}g</span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="text-xs text-gray-400">
                      {Math.round(item.calories)} kcal
                    </span>
                    <button
                      onClick={() => removeItem(item.itemId)}
                      className="text-gray-600 hover:text-red-400 text-xs transition-colors"
                    >
                      ✕
                    </button>
                  </div>
                </div>
              ))}
              {detail.items.length > 0 && (
                <div className="mt-2 pt-2 border-t border-gray-700 flex gap-4 text-xs text-gray-500">
                  <span>Total: {Math.round(Number(detail.totals.caloriesKcal))} kcal</span>
                  <span>P {Math.round(Number(detail.totals.proteinG))}g</span>
                  <span>C {Math.round(Number(detail.totals.carbsG))}g</span>
                  <span>F {Math.round(Number(detail.totals.fatG))}g</span>
                </div>
              )}
              <FoodItemForm logId={log.id} onAdded={refreshDetail} />
            </>
          )}
        </div>
      )}
    </div>
  )
}

// ─── Meal Type Section ────────────────────────────────────────────────────────

interface MealSectionProps {
  type: MealType
  logs: MealLogSummary[]
  selectedDate: string
  onCreated: () => void
  onDeleted: () => void
  onItemChanged: () => void
}

function MealSection({ type, logs, selectedDate, onCreated, onDeleted, onItemChanged }: MealSectionProps) {
  const [creating, setCreating] = useState(false)

  async function addMeal() {
    setCreating(true)
    try {
      await api.post('/api/v1/meal-logs', {
        mealType: type,
        loggedAt: new Date(selectedDate + 'T00:00:00.000Z').toISOString(),
      })
      onCreated()
    } finally {
      setCreating(false)
    }
  }

  return (
    <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-white">{MEAL_LABELS[type]}</h2>
        <button
          onClick={addMeal}
          disabled={creating}
          className="px-3 py-1.5 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 text-white text-sm rounded-lg transition-colors"
        >
          {creating ? '…' : '+ Add meal'}
        </button>
      </div>
      {logs.length === 0 ? (
        <p className="text-sm text-gray-500">No meals logged yet.</p>
      ) : (
        <div className="space-y-2">
          {logs.map(log => (
            <MealLogCard
              key={log.id}
              log={log}
              onDeleted={onDeleted}
              onItemChanged={onItemChanged}
            />
          ))}
        </div>
      )}
    </div>
  )
}

// ─── Date Picker Popover ──────────────────────────────────────────────────────

function DatePickerPopover({ selected, onSelect }: { selected: string; onSelect: (iso: string) => void }) {
  const [open, setOpen] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!open) return
    function onMouseDown(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', onMouseDown)
    return () => document.removeEventListener('mousedown', onMouseDown)
  }, [open])

  return (
    <div ref={containerRef} className="relative">
      <button
        onClick={() => setOpen(o => !o)}
        className="px-3 py-1.5 bg-gray-800 hover:bg-gray-700 text-gray-300 text-sm rounded-lg border border-gray-700 transition-colors"
        title="Pick a date"
      >
        📅
      </button>
      {open && (
        <div className="absolute right-0 top-full mt-1 z-50">
          <DayPicker
            mode="single"
            selected={new Date(selected + 'T12:00:00')}
            defaultMonth={new Date(selected + 'T12:00:00')}
            onSelect={date => {
              if (date) {
                onSelect(date.toISOString().split('T')[0])
                setOpen(false)
              }
            }}
            disabled={{ after: new Date() }}
            classNames={{
              root: 'p-4 bg-gray-900 rounded-xl border border-gray-700 shadow-2xl select-none',
              months: 'relative',
              month_caption: 'flex justify-center items-center h-8 mb-2',
              caption_label: 'text-sm font-semibold text-white',
              nav: 'absolute top-0 flex w-full justify-between',
              button_previous: 'h-8 w-8 flex items-center justify-center text-gray-400 hover:text-white rounded transition-colors',
              button_next: 'h-8 w-8 flex items-center justify-center text-gray-400 hover:text-white rounded transition-colors',
              chevron: 'fill-current',
              month_grid: 'w-full',
              weekdays: 'flex',
              weekday: 'w-9 h-7 text-center text-xs text-gray-500 font-normal',
              weeks: 'mt-1',
              week: 'flex',
              day: 'p-0',
              day_button: 'w-9 h-9 text-sm text-gray-300 hover:bg-gray-700 rounded-full transition-colors',
              today: 'text-teal-400 font-semibold',
              selected: '!bg-teal-600 !text-white rounded-full',
              disabled: 'opacity-25 cursor-not-allowed',
              outside: 'opacity-25',
            }}
          />
        </div>
      )}
    </div>
  )
}

// ─── Meal Log Page ────────────────────────────────────────────────────────────

export default function MealLog() {
  const [selectedDate, setSelectedDate] = useState(todayIso)
  const [logs, setLogs] = useState<MealLogSummary[]>([])
  const [loading, setLoading] = useState(true)

  const loadLogs = useCallback(async () => {
    setLoading(true)
    try {
      const data = await api.get<MealLogSummary[]>(`/api/v1/meal-logs?date=${selectedDate}`)
      setLogs(data)
    } finally {
      setLoading(false)
    }
  }, [selectedDate])

  useEffect(() => { loadLogs() }, [loadLogs])

  const byType = (type: MealType) => logs.filter(l => l.mealType === type)
  const isToday = selectedDate === todayIso()

  return (
    <div className="p-8 space-y-6 max-w-4xl">
      <div className="flex items-center gap-4">
        <h1 className="text-2xl font-bold text-white flex-1">
          Meal Log — {formatDateLabel(selectedDate)}
        </h1>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setSelectedDate(d => shiftDay(d, -1))}
            className="px-3 py-1.5 bg-gray-800 hover:bg-gray-700 text-gray-300 text-sm rounded-lg border border-gray-700 transition-colors"
            title="Previous day"
          >
            ‹
          </button>
          <DatePickerPopover selected={selectedDate} onSelect={setSelectedDate} />
          <button
            onClick={() => setSelectedDate(d => shiftDay(d, 1))}
            disabled={isToday}
            className="px-3 py-1.5 bg-gray-800 hover:bg-gray-700 disabled:opacity-30 disabled:cursor-not-allowed text-gray-300 text-sm rounded-lg border border-gray-700 transition-colors"
            title="Next day"
          >
            ›
          </button>
        </div>
      </div>
      {loading ? (
        <div className="flex items-center justify-center min-h-64">
          <div className="text-gray-400">Loading…</div>
        </div>
      ) : (
        MEAL_TYPES.map(type => (
          <MealSection
            key={type}
            type={type}
            logs={byType(type)}
            selectedDate={selectedDate}
            onCreated={loadLogs}
            onDeleted={loadLogs}
            onItemChanged={loadLogs}
          />
        ))
      )}
    </div>
  )
}
