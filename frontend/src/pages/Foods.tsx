import { useEffect, useState, useRef } from 'react'
import { api } from '../lib/api'
import type { Food } from '../lib/types'

interface EditForm {
  name: string
  calories100g: string
  proteinG: string
  carbsG: string
  fatG: string
}

function foodToForm(food: Food): EditForm {
  return {
    name: food.name,
    calories100g: String(food.calories100g ?? ''),
    proteinG: String(food.proteinG ?? ''),
    carbsG: String(food.carbsG ?? ''),
    fatG: String(food.fatG ?? ''),
  }
}

interface FoodRowProps {
  food: Food
  onUpdated: (food: Food) => void
  onDeleted: (id: string) => void
}

function FoodRow({ food, onUpdated, onDeleted }: FoodRowProps) {
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState<EditForm>(() => foodToForm(food))
  const [saving, setSaving] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  function startEdit() {
    setForm(foodToForm(food))
    setDeleteError(null)
    setEditing(true)
  }

  async function saveEdit() {
    setSaving(true)
    try {
      const updated = await api.put<Food>(`/api/v1/foods/${food.id}`, {
        name: form.name.trim(),
        calories100g: Number(form.calories100g),
        proteinG: Number(form.proteinG),
        carbsG: Number(form.carbsG),
        fatG: Number(form.fatG),
      })
      onUpdated(updated)
      setEditing(false)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete() {
    if (!confirm(`Delete "${food.name}"?`)) return
    setDeleting(true)
    setDeleteError(null)
    try {
      await api.delete(`/api/v1/foods/${food.id}`)
      onDeleted(food.id)
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err)
      if (msg.startsWith('409:')) {
        setDeleteError('This food is used in meal logs and cannot be deleted.')
      } else {
        setDeleteError('Failed to delete food.')
      }
    } finally {
      setDeleting(false)
    }
  }

  const inputCls = 'w-full px-2 py-1 bg-gray-700 border border-gray-600 rounded text-xs text-white focus:outline-none focus:ring-1 focus:ring-teal-500'

  return (
    <div className="bg-gray-800 rounded-xl p-4 space-y-3">
      {editing ? (
        <>
          <div className="grid grid-cols-5 gap-2">
            <div className="col-span-5">
              <label className="block text-xs text-gray-500 mb-0.5">Name</label>
              <input
                className={inputCls}
                value={form.name}
                onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
              />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-0.5">kcal/100g</label>
              <input type="number" min={0} className={inputCls} value={form.calories100g}
                onChange={e => setForm(f => ({ ...f, calories100g: e.target.value }))} />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-0.5">Protein (g)</label>
              <input type="number" min={0} className={inputCls} value={form.proteinG}
                onChange={e => setForm(f => ({ ...f, proteinG: e.target.value }))} />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-0.5">Carbs (g)</label>
              <input type="number" min={0} className={inputCls} value={form.carbsG}
                onChange={e => setForm(f => ({ ...f, carbsG: e.target.value }))} />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-0.5">Fat (g)</label>
              <input type="number" min={0} className={inputCls} value={form.fatG}
                onChange={e => setForm(f => ({ ...f, fatG: e.target.value }))} />
            </div>
          </div>
          <div className="flex gap-2">
            <button
              onClick={saveEdit}
              disabled={saving || !form.name.trim()}
              className="px-3 py-1.5 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 text-white text-xs rounded-lg transition-colors"
            >
              {saving ? 'Saving…' : 'Save'}
            </button>
            <button
              onClick={() => setEditing(false)}
              className="px-3 py-1.5 text-gray-400 hover:text-gray-200 text-xs transition-colors"
            >
              Cancel
            </button>
          </div>
        </>
      ) : (
        <div className="flex items-center justify-between gap-4">
          <div className="flex-1 min-w-0">
            <span className="text-sm font-medium text-white truncate block">{food.name}</span>
            <span className="text-xs text-gray-500">
              {food.calories100g ?? '—'} kcal · P {food.proteinG ?? '—'}g · C {food.carbsG ?? '—'}g · F {food.fatG ?? '—'}g
            </span>
          </div>
          <div className="flex items-center gap-2 shrink-0">
            <button
              onClick={startEdit}
              className="px-3 py-1.5 bg-gray-700 hover:bg-gray-600 text-gray-300 text-xs rounded-lg transition-colors"
            >
              Edit
            </button>
            <button
              onClick={handleDelete}
              disabled={deleting}
              className="px-3 py-1.5 text-gray-500 hover:text-red-400 text-xs transition-colors disabled:opacity-50"
            >
              {deleting ? '…' : 'Delete'}
            </button>
          </div>
        </div>
      )}
      {deleteError && (
        <p className="text-xs text-red-400">{deleteError}</p>
      )}
    </div>
  )
}

interface FoodsPage {
  content: Food[]
  page: number
  totalPages: number
  totalElements: number
}

export default function Foods() {
  const [foods, setFoods] = useState<Food[]>([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const debounceRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined)

  async function loadFoods(search: string, pageNum: number) {
    setLoading(true)
    try {
      const params = new URLSearchParams({ page: String(pageNum), size: '20' })
      if (search.trim()) params.set('search', search.trim())
      const data = await api.get<FoodsPage>(`/api/v1/foods?${params}`)
      setFoods(data.content)
      setPage(data.page)
      setTotalPages(data.totalPages)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadFoods('', 0)
  }, [])

  function handleSearch(value: string) {
    setQuery(value)
    clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => loadFoods(value, 0), 300)
  }

  function handleUpdated(updated: Food) {
    setFoods(prev => prev.map(f => f.id === updated.id ? updated : f))
  }

  function handleDeleted(id: string) {
    setFoods(prev => prev.filter(f => f.id !== id))
  }

  function handlePrev() {
    const next = page - 1
    setPage(next)
    loadFoods(query, next)
  }

  function handleNext() {
    const next = page + 1
    setPage(next)
    loadFoods(query, next)
  }

  return (
    <div className="p-8 space-y-6 max-w-4xl">
      <div className="flex items-center gap-4">
        <h1 className="text-2xl font-bold text-white flex-1">Foods</h1>
      </div>

      <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6 space-y-4">
        <input
          type="text"
          placeholder="Search foods…"
          value={query}
          onChange={e => handleSearch(e.target.value)}
          className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-1 focus:ring-teal-500"
        />

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <span className="text-gray-400 text-sm">Loading…</span>
          </div>
        ) : foods.length === 0 ? (
          <p className="text-sm text-gray-500 py-4 text-center">
            {query ? 'No foods match your search.' : 'No foods yet. Add foods via the Meal Log.'}
          </p>
        ) : (
          <div className="space-y-2">
            {foods.map(food => (
              <FoodRow
                key={food.id}
                food={food}
                onUpdated={handleUpdated}
                onDeleted={handleDeleted}
              />
            ))}
          </div>
        )}

        {!loading && totalPages > 1 && (
          <div className="flex items-center justify-between pt-2">
            <button
              onClick={handlePrev}
              disabled={page === 0}
              className="px-3 py-1.5 bg-gray-800 hover:bg-gray-700 disabled:opacity-40 disabled:cursor-not-allowed text-gray-300 text-sm rounded-lg transition-colors"
            >
              ← Previous
            </button>
            <span className="text-xs text-gray-500">Page {page + 1} of {totalPages}</span>
            <button
              onClick={handleNext}
              disabled={page >= totalPages - 1}
              className="px-3 py-1.5 bg-gray-800 hover:bg-gray-700 disabled:opacity-40 disabled:cursor-not-allowed text-gray-300 text-sm rounded-lg transition-colors"
            >
              Next →
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
