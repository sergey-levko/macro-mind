import { useEffect, useRef, useState } from 'react'
import { DayPicker } from 'react-day-picker'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { api } from '../lib/api'

interface AdviceResponse {
  id: string
  adviceType: 'DAILY' | 'WEEKLY'
  periodStart: string
  content: string
  status: 'PENDING' | 'COMPLETED' | 'FAILED'
  createdAt: string
}

interface Message {
  role: 'user' | 'assistant'
  text: string
}

function todayStr(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function mondayStr(): string {
  const d = new Date()
  const day = d.getDay()
  d.setDate(d.getDate() + (day === 0 ? -6 : 1 - day))
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function shiftDay(iso: string, delta: number): string {
  const d = new Date(iso + 'T12:00:00')
  d.setDate(d.getDate() + delta)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function mondayOf(iso: string): string {
  const d = new Date(iso + 'T12:00:00')
  const day = d.getDay()
  d.setDate(d.getDate() + (day === 0 ? -6 : 1 - day))
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatDateLabel(iso: string): string {
  const today = todayStr()
  const d = new Date(iso + 'T12:00:00')
  const yIso = shiftDay(today, -1)
  const dateStr = d.toLocaleDateString('en-US', { month: 'long', day: 'numeric' })
  if (iso === today) return `Today, ${dateStr}`
  if (iso === yIso) return `Yesterday, ${dateStr}`
  return d.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })
}

function formatWeekLabel(iso: string): string {
  const monday = new Date(iso + 'T12:00:00')
  const sunday = new Date(iso + 'T12:00:00')
  sunday.setDate(sunday.getDate() + 6)
  const startStr = monday.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
  const endOpts: Intl.DateTimeFormatOptions = monday.getMonth() !== sunday.getMonth()
    ? { month: 'short', day: 'numeric' }
    : { day: 'numeric' }
  const endStr = sunday.toLocaleDateString('en-US', endOpts)
  const range = `${startStr} – ${endStr}`
  if (iso === mondayStr()) return `This week (${range})`
  return range
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const mdComponents: Record<string, any> = {
  p: ({ children }: { children: React.ReactNode }) => <p className="mb-2 last:mb-0">{children}</p>,
  strong: ({ children }: { children: React.ReactNode }) => <strong className="font-semibold text-white">{children}</strong>,
  ul: ({ children }: { children: React.ReactNode }) => <ul className="list-disc list-inside mb-2 space-y-0.5">{children}</ul>,
  ol: ({ children }: { children: React.ReactNode }) => <ol className="list-decimal list-inside mb-2 space-y-0.5">{children}</ol>,
  li: ({ children }: { children: React.ReactNode }) => <li>{children}</li>,
  h1: ({ children }: { children: React.ReactNode }) => <h1 className="text-base font-bold text-white mb-1 mt-2">{children}</h1>,
  h2: ({ children }: { children: React.ReactNode }) => <h2 className="text-sm font-bold text-white mb-1 mt-2">{children}</h2>,
  h3: ({ children }: { children: React.ReactNode }) => <h3 className="text-sm font-semibold text-white mb-1 mt-1">{children}</h3>,
  code: ({ children }: { children: React.ReactNode }) => <code className="bg-gray-700 px-1 rounded text-xs font-mono">{children}</code>,
}

function InsightContent({ content }: { content: string }) {
  return (
    <div className="text-sm text-gray-300">
      <ReactMarkdown remarkPlugins={[remarkGfm]} components={mdComponents}>
        {content}
      </ReactMarkdown>
    </div>
  )
}

const dayPickerClassNames = {
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
  day: 'p-0 w-9',
  day_button: 'w-9 h-9 text-sm text-gray-300 hover:bg-gray-700 rounded-full transition-colors',
  today: 'text-teal-400 font-semibold',
  selected: '!bg-teal-600 !text-white rounded-full',
  disabled: 'opacity-25 cursor-not-allowed',
  outside: 'opacity-0 pointer-events-none',
}

function DayNavDatePicker({ selected, onSelect }: {
  selected: string
  onSelect: (iso: string) => void
}) {
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
        className="px-3 py-1.5 text-sm rounded-lg border bg-gray-800 hover:bg-gray-700 border-gray-700 text-gray-200 transition-colors"
        title="Pick a date"
      >
        {formatDateLabel(selected)}
      </button>
      {open && (
        <div className="absolute left-0 top-full mt-1 z-50">
          <DayPicker
            mode="single"
            ISOWeek
            selected={new Date(selected + 'T12:00:00')}
            defaultMonth={new Date(selected + 'T12:00:00')}
            onSelect={date => {
              if (date) {
                const y = date.getFullYear()
                const m = String(date.getMonth() + 1).padStart(2, '0')
                const d = String(date.getDate()).padStart(2, '0')
                onSelect(`${y}-${m}-${d}`)
                setOpen(false)
              }
            }}
            disabled={{ after: new Date() }}
            classNames={dayPickerClassNames}
          />
        </div>
      )}
    </div>
  )
}

function WeekNavDatePicker({ selected, onSelect }: {
  selected: string
  onSelect: (iso: string) => void
}) {
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
        className="px-3 py-1.5 text-sm rounded-lg border bg-gray-800 hover:bg-gray-700 border-gray-700 text-gray-200 transition-colors"
        title="Pick a week"
      >
        {formatWeekLabel(selected)}
      </button>
      {open && (
        <div className="absolute left-0 top-full mt-1 z-50">
          <DayPicker
            mode="single"
            ISOWeek
            selected={new Date(selected + 'T12:00:00')}
            defaultMonth={new Date(selected + 'T12:00:00')}
            onSelect={date => {
              if (date) {
                const y = date.getFullYear()
                const m = String(date.getMonth() + 1).padStart(2, '0')
                const d = String(date.getDate()).padStart(2, '0')
                onSelect(mondayOf(`${y}-${m}-${d}`))
                setOpen(false)
              }
            }}
            disabled={{ after: new Date() }}
            classNames={dayPickerClassNames}
          />
        </div>
      )}
    </div>
  )
}

function InsightPanel({
  type,
  periodLabel,
  insight,
  preview,
  generating,
  saving,
  needGoal,
  onGenerate,
  onSave,
  onDiscard,
}: {
  type: 'DAILY' | 'WEEKLY'
  periodLabel: string
  insight: AdviceResponse | null
  preview: string | null
  generating: boolean
  saving: boolean
  needGoal: boolean
  onGenerate: () => void
  onSave: () => void
  onDiscard: () => void
}) {
  return (
    <div className="space-y-4">
      {needGoal && (
        <div className="bg-gray-900 rounded-2xl border border-amber-800/40 p-4">
          <p className="text-sm text-amber-400">
            Set up your nutritional goals in Profile to enable personalized insights.
          </p>
        </div>
      )}

      {insight && !preview && (
        <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
          <p className="text-xs text-gray-500 mb-3">
            {type === 'WEEKLY' ? formatWeekLabel(insight.periodStart) : insight.periodStart}
          </p>
          <InsightContent content={insight.content} />
        </div>
      )}

      {!insight && !preview && !generating && (
        <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
          <p className="text-sm text-gray-500">
            No {type === 'DAILY' ? 'daily' : 'weekly'} insight for {periodLabel} yet.
          </p>
        </div>
      )}

      {generating && (
        <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
          <p className="text-sm text-gray-500">Generating insight…</p>
        </div>
      )}

      {preview && (
        <div className="bg-gray-900 rounded-2xl border border-teal-800/40 p-6 space-y-4">
          <p className="text-xs text-teal-500 font-medium">Preview — not saved yet</p>
          <InsightContent content={preview} />
          <div className="flex gap-2 pt-2">
            <button
              onClick={onSave}
              disabled={saving}
              className="px-4 py-1.5 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 text-white text-sm rounded-lg transition-colors"
            >
              {saving ? 'Saving…' : 'Save'}
            </button>
            <button
              onClick={onDiscard}
              disabled={saving}
              className="px-4 py-1.5 bg-gray-700 hover:bg-gray-600 disabled:opacity-50 text-gray-300 text-sm rounded-lg transition-colors"
            >
              Discard
            </button>
          </div>
        </div>
      )}

      {!generating && !preview && (
        <button
          onClick={onGenerate}
          className="px-4 py-2 bg-gray-800 hover:bg-gray-700 border border-gray-700 text-gray-300 text-sm rounded-lg transition-colors"
        >
          Generate new insight
        </button>
      )}
    </div>
  )
}

export default function Coach() {
  const [tab, setTab] = useState<'chat' | 'insights'>('chat')
  const [insightPeriod, setInsightPeriod] = useState<'daily' | 'weekly'>('daily')
  const [selectedDate, setSelectedDate] = useState(todayStr())
  const [selectedWeek, setSelectedWeek] = useState(mondayStr())

  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const [dailyInsight, setDailyInsight] = useState<AdviceResponse | null>(null)
  const [weeklyInsight, setWeeklyInsight] = useState<AdviceResponse | null>(null)
  const [dailyLoading, setDailyLoading] = useState(true)
  const [weeklyLoading, setWeeklyLoading] = useState(true)
  const [dailyError, setDailyError] = useState(false)
  const [weeklyError, setWeeklyError] = useState(false)

  const [previewDaily, setPreviewDaily] = useState<string | null>(null)
  const [previewWeekly, setPreviewWeekly] = useState<string | null>(null)
  const [generatingDaily, setGeneratingDaily] = useState(false)
  const [generatingWeekly, setGeneratingWeekly] = useState(false)
  const [savingDaily, setSavingDaily] = useState(false)
  const [savingWeekly, setSavingWeekly] = useState(false)
  const [needGoalDaily, setNeedGoalDaily] = useState(false)
  const [needGoalWeekly, setNeedGoalWeekly] = useState(false)

  useEffect(() => {
    async function loadDaily() {
      setDailyLoading(true)
      setDailyError(false)
      try {
        const daily = await api.get<AdviceResponse[]>(`/api/v1/advice?adviceType=DAILY&periodStart=${selectedDate}`)
        if (daily.length > 0) {
          setDailyInsight(daily[0])
        } else {
          setDailyInsight(null)
        }
        setDailyLoading(false)
      } catch {
        setDailyError(true)
        setDailyLoading(false)
      }
    }
    loadDaily()
  }, [selectedDate])

  useEffect(() => {
    setPreviewDaily(null)
    setNeedGoalDaily(false)
  }, [selectedDate])

  useEffect(() => {
    async function loadWeekly() {
      setWeeklyLoading(true)
      setWeeklyError(false)
      try {
        const weekly = await api.get<AdviceResponse[]>(`/api/v1/advice?adviceType=WEEKLY&periodStart=${selectedWeek}`)
        if (weekly.length > 0) {
          setWeeklyInsight(weekly[0])
        } else {
          setWeeklyInsight(null)
        }
        setWeeklyLoading(false)
      } catch {
        setWeeklyError(true)
        setWeeklyLoading(false)
      }
    }
    loadWeekly()
  }, [selectedWeek])

  useEffect(() => {
    setPreviewWeekly(null)
    setNeedGoalWeekly(false)
  }, [selectedWeek])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  async function generateInsight(type: 'DAILY' | 'WEEKLY') {
    const setGenerating = type === 'DAILY' ? setGeneratingDaily : setGeneratingWeekly
    const setNeedGoal = type === 'DAILY' ? setNeedGoalDaily : setNeedGoalWeekly
    const setPreview = type === 'DAILY' ? setPreviewDaily : setPreviewWeekly
    setGenerating(true)
    setNeedGoal(false)
    try {
      const result = await api.post<AdviceResponse>('/api/v1/advice', {
        adviceType: type,
        periodStart: type === 'DAILY' ? selectedDate : selectedWeek,
        preview: true,
      })
      setPreview(result.content)
    } catch (err) {
      if (String(err).includes('400:')) setNeedGoal(true)
    } finally {
      setGenerating(false)
    }
  }

  async function saveInsight(type: 'DAILY' | 'WEEKLY') {
    const setSaving = type === 'DAILY' ? setSavingDaily : setSavingWeekly
    const setPreview = type === 'DAILY' ? setPreviewDaily : setPreviewWeekly
    const setInsight = type === 'DAILY' ? setDailyInsight : setWeeklyInsight
    const content = type === 'DAILY' ? previewDaily : previewWeekly
    setSaving(true)
    try {
      const result = await api.post<AdviceResponse>('/api/v1/advice', {
        adviceType: type,
        periodStart: type === 'DAILY' ? selectedDate : selectedWeek,
        content,
      })
      setInsight(result)
      setPreview(null)
    } finally {
      setSaving(false)
    }
  }

  async function sendMessage() {
    const text = input.trim()
    if (!text || sending) return
    setInput('')
    setMessages(prev => [...prev, { role: 'user', text }])
    setSending(true)
    try {
      const { reply } = await api.post<{ reply: string }>('/api/v1/chat', { message: text })
      setMessages(prev => [...prev, { role: 'assistant', text: reply }])
    } catch {
      setMessages(prev => [...prev, { role: 'assistant', text: 'Something went wrong. Please try again.' }])
    } finally {
      setSending(false)
    }
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  const pillCls = (active: boolean) =>
    `px-4 py-2 text-sm font-medium rounded-lg transition-colors ${
      active ? 'bg-teal-600 text-white' : 'text-gray-400 hover:text-white hover:bg-gray-800'
    }`

  return (
    <div className="p-8 max-w-4xl space-y-6">
      <div className="flex items-center gap-4">
        <h1 className="text-2xl font-bold text-white flex-1">Coach</h1>
        <div className="flex gap-1 bg-gray-900 border border-gray-800 rounded-xl p-1">
          <button className={pillCls(tab === 'chat')} onClick={() => setTab('chat')}>Chat</button>
          <button className={pillCls(tab === 'insights')} onClick={() => setTab('insights')}>Insights</button>
        </div>
      </div>

      {tab === 'chat' && (
        <div className="bg-gray-900 rounded-2xl border border-gray-800 flex flex-col" style={{ height: 'calc(100vh - 220px)', minHeight: '480px' }}>
          <div className="px-5 py-4 border-b border-gray-800">
            <p className="text-xs text-gray-500">Ask anything about your nutrition — your recent meals are included as context.</p>
          </div>

          <div className="flex-1 overflow-y-auto px-5 py-4 space-y-4">
            {messages.length === 0 && (
              <p className="text-sm text-gray-500 text-center mt-12">
                Start the conversation — ask about your macros, meal ideas, or nutrition goals.
              </p>
            )}
            {messages.map((msg, i) => (
              <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div
                  className={`max-w-lg px-4 py-2.5 rounded-2xl text-sm ${
                    msg.role === 'user'
                      ? 'bg-teal-600 text-white rounded-br-sm'
                      : 'bg-gray-800 text-gray-200 rounded-bl-sm'
                  }`}
                >
                  {msg.role === 'user' ? (
                    <span className="whitespace-pre-wrap">{msg.text}</span>
                  ) : (
                    <ReactMarkdown remarkPlugins={[remarkGfm]} components={mdComponents}>
                      {msg.text}
                    </ReactMarkdown>
                  )}
                </div>
              </div>
            ))}
            {sending && (
              <div className="flex justify-start">
                <div className="bg-gray-800 text-gray-400 px-4 py-2.5 rounded-2xl rounded-bl-sm text-sm">
                  Thinking…
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className="px-5 py-4 border-t border-gray-800">
            <div className="flex gap-2 items-end">
              <textarea
                rows={2}
                value={input}
                onChange={e => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                disabled={sending}
                placeholder="Ask a question… (Enter to send)"
                className="flex-1 resize-none px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-1 focus:ring-teal-500 disabled:opacity-50"
              />
              <button
                onClick={sendMessage}
                disabled={sending || !input.trim()}
                className="px-4 py-2 bg-teal-600 hover:bg-teal-500 disabled:opacity-50 text-white text-sm rounded-lg transition-colors shrink-0"
              >
                Send
              </button>
            </div>
          </div>
        </div>
      )}

      {tab === 'insights' && (
        <div className="space-y-4">
          <div className="flex gap-1 bg-gray-800 border border-gray-700 rounded-xl p-1 w-fit">
            <button className={pillCls(insightPeriod === 'daily')} onClick={() => setInsightPeriod('daily')}>Daily</button>
            <button className={pillCls(insightPeriod === 'weekly')} onClick={() => setInsightPeriod('weekly')}>Weekly</button>
          </div>

          {insightPeriod === 'daily' ? (
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setSelectedDate(shiftDay(selectedDate, -1))}
                  className="p-2 bg-gray-800 hover:bg-gray-700 text-gray-300 rounded-lg transition-colors text-sm"
                  title="Previous day"
                >
                  ←
                </button>
                <DayNavDatePicker selected={selectedDate} onSelect={setSelectedDate} />
                <button
                  onClick={() => setSelectedDate(shiftDay(selectedDate, 1))}
                  disabled={selectedDate === todayStr()}
                  className="p-2 bg-gray-800 hover:bg-gray-700 disabled:opacity-40 disabled:cursor-not-allowed text-gray-300 rounded-lg transition-colors text-sm"
                  title="Next day"
                >
                  →
                </button>
                {selectedDate !== todayStr() && (
                  <button
                    onClick={() => setSelectedDate(todayStr())}
                    className="px-3 py-1.5 bg-gray-800 hover:bg-gray-700 border border-gray-600 text-gray-300 text-sm rounded-lg transition-colors"
                  >
                    Today
                  </button>
                )}
              </div>
              {dailyLoading ? (
                <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
                  <p className="text-sm text-gray-500">Loading…</p>
                </div>
              ) : dailyError ? (
                <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
                  <p className="text-sm text-red-400">Could not load insights.</p>
                </div>
              ) : (
                <InsightPanel
                  type="DAILY"
                  periodLabel={formatDateLabel(selectedDate).toLowerCase()}
                  insight={dailyInsight}
                  preview={previewDaily}
                  generating={generatingDaily}
                  saving={savingDaily}
                  needGoal={needGoalDaily}
                  onGenerate={() => generateInsight('DAILY')}
                  onSave={() => saveInsight('DAILY')}
                  onDiscard={() => setPreviewDaily(null)}
                />
              )}
            </div>
          ) : (
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setSelectedWeek(shiftDay(selectedWeek, -7))}
                  className="p-2 bg-gray-800 hover:bg-gray-700 text-gray-300 rounded-lg transition-colors text-sm"
                  title="Previous week"
                >
                  ←
                </button>
                <WeekNavDatePicker selected={selectedWeek} onSelect={setSelectedWeek} />
                <button
                  onClick={() => setSelectedWeek(shiftDay(selectedWeek, 7))}
                  disabled={selectedWeek === mondayStr()}
                  className="p-2 bg-gray-800 hover:bg-gray-700 disabled:opacity-40 disabled:cursor-not-allowed text-gray-300 rounded-lg transition-colors text-sm"
                  title="Next week"
                >
                  →
                </button>
                {selectedWeek !== mondayStr() && (
                  <button
                    onClick={() => setSelectedWeek(mondayStr())}
                    className="px-3 py-1.5 bg-gray-800 hover:bg-gray-700 border border-gray-600 text-gray-300 text-sm rounded-lg transition-colors"
                  >
                    This week
                  </button>
                )}
              </div>
              {weeklyLoading ? (
                <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
                  <p className="text-sm text-gray-500">Loading…</p>
                </div>
              ) : weeklyError ? (
                <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
                  <p className="text-sm text-red-400">Could not load insights.</p>
                </div>
              ) : (
                <InsightPanel
                  type="WEEKLY"
                  periodLabel={formatWeekLabel(selectedWeek).toLowerCase()}
                  insight={weeklyInsight}
                  preview={previewWeekly}
                  generating={generatingWeekly}
                  saving={savingWeekly}
                  needGoal={needGoalWeekly}
                  onGenerate={() => generateInsight('WEEKLY')}
                  onSave={() => saveInsight('WEEKLY')}
                  onDiscard={() => setPreviewWeekly(null)}
                />
              )}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
