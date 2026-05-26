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

function formatDateLabel(iso: string): string {
  const today = todayStr()
  const d = new Date(iso + 'T12:00:00')
  const yesterday = new Date(d)
  yesterday.setDate(d.getDate() - 1)
  const yIso = `${yesterday.getFullYear()}-${String(yesterday.getMonth() + 1).padStart(2, '0')}-${String(yesterday.getDate()).padStart(2, '0')}`
  const dateStr = d.toLocaleDateString('en-US', { month: 'long', day: 'numeric' })
  if (iso === today) return `Today, ${dateStr}`
  if (iso === yIso) return `Yesterday, ${dateStr}`
  return d.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })
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

function HistoryDatePicker({ selected, onSelect, onClear }: {
  selected: string | null
  onSelect: (iso: string) => void
  onClear: () => void
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
    <div ref={containerRef} className="relative flex items-center gap-1">
      <button
        onClick={() => setOpen(o => !o)}
        className={`px-3 py-1.5 text-sm rounded-lg border transition-colors ${
          selected
            ? 'bg-teal-600/20 border-teal-700 text-teal-300'
            : 'bg-gray-800 hover:bg-gray-700 border-gray-700 text-gray-300'
        }`}
        title="Filter by date"
      >
        {selected ? formatDateLabel(selected) : '📅 All dates'}
      </button>
      {selected && (
        <button
          onClick={onClear}
          className="text-gray-500 hover:text-gray-300 text-sm px-1 transition-colors"
          title="Clear date filter"
        >
          ✕
        </button>
      )}
      {open && (
        <div className="absolute left-0 top-full mt-1 z-50">
          <DayPicker
            mode="single"
            ISOWeek
            selected={selected ? new Date(selected + 'T12:00:00') : undefined}
            defaultMonth={selected ? new Date(selected + 'T12:00:00') : new Date()}
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
            {type === 'WEEKLY' ? `Week of ${insight.periodStart}` : insight.periodStart}
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
  const [insightPeriod, setInsightPeriod] = useState<'daily' | 'weekly' | 'history'>('daily')

  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const [dailyInsight, setDailyInsight] = useState<AdviceResponse | null>(null)
  const [weeklyInsight, setWeeklyInsight] = useState<AdviceResponse | null>(null)
  const [insightsLoading, setInsightsLoading] = useState(true)
  const [insightsError, setInsightsError] = useState(false)

  const [previewDaily, setPreviewDaily] = useState<string | null>(null)
  const [previewWeekly, setPreviewWeekly] = useState<string | null>(null)
  const [generatingDaily, setGeneratingDaily] = useState(false)
  const [generatingWeekly, setGeneratingWeekly] = useState(false)
  const [savingDaily, setSavingDaily] = useState(false)
  const [savingWeekly, setSavingWeekly] = useState(false)
  const [needGoalDaily, setNeedGoalDaily] = useState(false)
  const [needGoalWeekly, setNeedGoalWeekly] = useState(false)

  const [history, setHistory] = useState<AdviceResponse[]>([])
  const [historyLoading, setHistoryLoading] = useState(false)
  const [historyFilter, setHistoryFilter] = useState<'ALL' | 'DAILY' | 'WEEKLY'>('ALL')
  const [historyDate, setHistoryDate] = useState<string | null>(null)
  const [deletingId, setDeletingId] = useState<string | null>(null)
  const [deleteInProgress, setDeleteInProgress] = useState(false)

  useEffect(() => {
    async function loadInsights() {
      setInsightsLoading(true)
      setInsightsError(false)
      try {
        const [daily, weekly] = await Promise.all([
          api.get<AdviceResponse[]>(`/api/v1/advice?adviceType=DAILY&periodStart=${todayStr()}`),
          api.get<AdviceResponse[]>(`/api/v1/advice?adviceType=WEEKLY&periodStart=${mondayStr()}`),
        ])
        setDailyInsight(daily[0] ?? null)
        setWeeklyInsight(weekly[0] ?? null)
      } catch {
        setInsightsError(true)
      } finally {
        setInsightsLoading(false)
      }
    }
    loadInsights()
  }, [])

  useEffect(() => {
    if (insightPeriod !== 'history') return
    setHistoryLoading(true)
    const params = new URLSearchParams()
    if (historyFilter !== 'ALL') params.set('adviceType', historyFilter)
    if (historyDate) params.set('periodStart', historyDate)
    const qs = params.toString()
    api.get<AdviceResponse[]>(`/api/v1/advice${qs ? `?${qs}` : ''}`)
      .then(data => setHistory(data.sort((a, b) => b.periodStart.localeCompare(a.periodStart) || b.createdAt.localeCompare(a.createdAt))))
      .catch(() => {})
      .finally(() => setHistoryLoading(false))
  }, [insightPeriod, historyFilter, historyDate])

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
        periodStart: type === 'DAILY' ? todayStr() : mondayStr(),
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
    setSaving(true)
    try {
      const result = await api.post<AdviceResponse>('/api/v1/advice', {
        adviceType: type,
        periodStart: type === 'DAILY' ? todayStr() : mondayStr(),
      })
      setInsight(result)
      setPreview(null)
    } finally {
      setSaving(false)
    }
  }

  async function confirmDelete(id: string) {
    setDeleteInProgress(true)
    try {
      await api.delete(`/api/v1/advice/${id}`)
      setHistory(prev => prev.filter(item => item.id !== id))
    } finally {
      setDeletingId(null)
      setDeleteInProgress(false)
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

  const filterBtnCls = (active: boolean) =>
    `px-3 py-1.5 text-sm rounded-lg border transition-colors ${
      active ? 'bg-teal-600/20 border-teal-700 text-teal-300' : 'bg-gray-800 hover:bg-gray-700 border-gray-700 text-gray-400'
    }`

  const groupedHistory = history.reduce<Record<string, AdviceResponse[]>>((acc, item) => {
    ;(acc[item.periodStart] ??= []).push(item)
    return acc
  }, {})
  const historyDays = Object.keys(groupedHistory).sort((a, b) => b.localeCompare(a))

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
            <button className={pillCls(insightPeriod === 'history')} onClick={() => setInsightPeriod('history')}>History</button>
          </div>

          {insightsLoading && insightPeriod !== 'history' ? (
            <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
              <p className="text-sm text-gray-500">Loading…</p>
            </div>
          ) : insightsError && insightPeriod !== 'history' ? (
            <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
              <p className="text-sm text-red-400">Could not load insights.</p>
            </div>
          ) : insightPeriod === 'daily' ? (
            <InsightPanel
              type="DAILY"
              periodLabel="today"
              insight={dailyInsight}
              preview={previewDaily}
              generating={generatingDaily}
              saving={savingDaily}
              needGoal={needGoalDaily}
              onGenerate={() => generateInsight('DAILY')}
              onSave={() => saveInsight('DAILY')}
              onDiscard={() => setPreviewDaily(null)}
            />
          ) : insightPeriod === 'weekly' ? (
            <InsightPanel
              type="WEEKLY"
              periodLabel="this week"
              insight={weeklyInsight}
              preview={previewWeekly}
              generating={generatingWeekly}
              saving={savingWeekly}
              needGoal={needGoalWeekly}
              onGenerate={() => generateInsight('WEEKLY')}
              onSave={() => saveInsight('WEEKLY')}
              onDiscard={() => setPreviewWeekly(null)}
            />
          ) : (
            <div className="space-y-4">
              <div className="flex items-center gap-3 flex-wrap">
                <HistoryDatePicker
                  selected={historyDate}
                  onSelect={setHistoryDate}
                  onClear={() => setHistoryDate(null)}
                />
                <div className="flex gap-1">
                  <button className={filterBtnCls(historyFilter === 'ALL')} onClick={() => setHistoryFilter('ALL')}>All</button>
                  <button className={filterBtnCls(historyFilter === 'DAILY')} onClick={() => setHistoryFilter('DAILY')}>Daily</button>
                  <button className={filterBtnCls(historyFilter === 'WEEKLY')} onClick={() => setHistoryFilter('WEEKLY')}>Weekly</button>
                </div>
              </div>

              <div className="bg-gray-900 rounded-2xl border border-gray-800 p-6">
                {historyLoading ? (
                  <p className="text-sm text-gray-500">Loading history…</p>
                ) : historyDays.length === 0 ? (
                  <p className="text-sm text-gray-500">No saved insights found.</p>
                ) : (
                  <div className="space-y-8">
                    {historyDays.map(day => (
                      <div key={day}>
                        <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-4">
                          {formatDateLabel(day)}
                        </p>
                        <div className="space-y-6">
                          {groupedHistory[day].map(item => (
                            <div key={item.id} className="border-b border-gray-800 pb-6 last:border-0 last:pb-0">
                              <div className="flex items-center justify-between mb-3">
                                <span className={`inline-block text-xs px-2 py-0.5 rounded-full font-medium ${
                                  item.adviceType === 'DAILY'
                                    ? 'bg-teal-900/50 text-teal-400'
                                    : 'bg-purple-900/50 text-purple-400'
                                }`}>
                                  {item.adviceType === 'DAILY' ? 'Daily' : 'Weekly'}
                                </span>
                                {deletingId === item.id ? (
                                  <div className="flex items-center gap-2">
                                    <span className="text-xs text-gray-400">Delete?</span>
                                    <button
                                      onClick={() => confirmDelete(item.id)}
                                      disabled={deleteInProgress}
                                      className="px-2 py-1 text-xs bg-red-600 hover:bg-red-500 disabled:opacity-50 text-white rounded transition-colors"
                                    >
                                      {deleteInProgress ? '…' : 'Confirm'}
                                    </button>
                                    <button
                                      onClick={() => setDeletingId(null)}
                                      disabled={deleteInProgress}
                                      className="px-2 py-1 text-xs bg-gray-700 hover:bg-gray-600 disabled:opacity-50 text-gray-300 rounded transition-colors"
                                    >
                                      Cancel
                                    </button>
                                  </div>
                                ) : (
                                  <button
                                    onClick={() => setDeletingId(item.id)}
                                    className="text-gray-600 hover:text-red-400 transition-colors p-1"
                                    title="Delete insight"
                                  >
                                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                                      <path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd" />
                                    </svg>
                                  </button>
                                )}
                              </div>
                              <InsightContent content={item.content} />
                            </div>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
