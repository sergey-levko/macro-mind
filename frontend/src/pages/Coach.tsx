import { useEffect, useRef, useState } from 'react'
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

export default function Coach() {
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const [dailyInsights, setDailyInsights] = useState<AdviceResponse[]>([])
  const [weeklyInsights, setWeeklyInsights] = useState<AdviceResponse[]>([])
  const [insightsLoading, setInsightsLoading] = useState(true)
  const [insightsError, setInsightsError] = useState(false)
  const [insightsNeedGoal, setInsightsNeedGoal] = useState(false)

  useEffect(() => {
    async function loadInsights() {
      setInsightsLoading(true)
      setInsightsError(false)
      setInsightsNeedGoal(false)
      try {
        let [daily, weekly] = await Promise.all([
          api.get<AdviceResponse[]>('/api/v1/advice?adviceType=DAILY'),
          api.get<AdviceResponse[]>('/api/v1/advice?adviceType=WEEKLY'),
        ])

        let noGoal = false

        if (daily.length === 0) {
          try {
            const generated = await api.post<AdviceResponse>('/api/v1/advice', {
              adviceType: 'DAILY',
              periodStart: todayStr(),
            })
            daily = [generated]
          } catch (err) {
            if (String(err).includes('400:')) noGoal = true
          }
        }

        if (weekly.length === 0 && !noGoal) {
          try {
            const generated = await api.post<AdviceResponse>('/api/v1/advice', {
              adviceType: 'WEEKLY',
              periodStart: mondayStr(),
            })
            weekly = [generated]
          } catch (err) {
            if (String(err).includes('400:')) noGoal = true
          }
        }

        if (noGoal) setInsightsNeedGoal(true)
        setDailyInsights(daily)
        setWeeklyInsights(weekly)
      } catch {
        setInsightsError(true)
      } finally {
        setInsightsLoading(false)
      }
    }
    loadInsights()
  }, [])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

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

  function InsightContent({ content }: { content: string }) {
    return (
      <div className="text-sm text-gray-300">
        <ReactMarkdown remarkPlugins={[remarkGfm]} components={mdComponents}>
          {content}
        </ReactMarkdown>
      </div>
    )
  }

  return (
    <div className="p-8 max-w-6xl space-y-6">
      <h1 className="text-2xl font-bold text-white">Coach</h1>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">
        {/* Chat Panel */}
        <div className="bg-gray-900 rounded-2xl border border-gray-800 flex flex-col" style={{ height: '600px' }}>
          <div className="px-5 py-4 border-b border-gray-800">
            <h2 className="text-sm font-semibold text-white">Ask your coach</h2>
            <p className="text-xs text-gray-500 mt-0.5">Ask anything about your nutrition</p>
          </div>

          <div className="flex-1 overflow-y-auto px-5 py-4 space-y-4">
            {messages.length === 0 && (
              <p className="text-sm text-gray-500 text-center mt-8">
                Start the conversation — ask about your macros, meal ideas, or nutrition goals.
              </p>
            )}
            {messages.map((msg, i) => (
              <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div
                  className={`max-w-xs lg:max-w-sm px-4 py-2.5 rounded-2xl text-sm ${
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

        {/* Insights Panel */}
        <div className="space-y-4">
          {insightsNeedGoal && (
            <div className="bg-gray-900 rounded-2xl border border-amber-800/40 p-4">
              <p className="text-sm text-amber-400">
                Set up your nutritional goals in Profile to enable personalized insights.
              </p>
            </div>
          )}

          <div className="bg-gray-900 rounded-2xl border border-gray-800 p-5">
            <h2 className="text-sm font-semibold text-white mb-3">Daily Insights</h2>
            {insightsLoading ? (
              <p className="text-sm text-gray-500">Generating insights…</p>
            ) : insightsError ? (
              <p className="text-sm text-red-400">Could not load insights.</p>
            ) : dailyInsights.length === 0 ? (
              <p className="text-sm text-gray-500">No insights yet — log some meals to get started.</p>
            ) : (
              <div className="space-y-4">
                {dailyInsights.slice(0, 3).map(insight => (
                  <div key={insight.id} className="space-y-1">
                    <p className="text-xs text-gray-500">{insight.periodStart}</p>
                    <InsightContent content={insight.content} />
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="bg-gray-900 rounded-2xl border border-gray-800 p-5">
            <h2 className="text-sm font-semibold text-white mb-3">Weekly Insights</h2>
            {insightsLoading ? (
              <p className="text-sm text-gray-500">Generating insights…</p>
            ) : insightsError ? (
              <p className="text-sm text-red-400">Could not load insights.</p>
            ) : weeklyInsights.length === 0 ? (
              <p className="text-sm text-gray-500">No insights yet — log some meals to get started.</p>
            ) : (
              <div className="space-y-4">
                {weeklyInsights.slice(0, 2).map(insight => (
                  <div key={insight.id} className="space-y-1">
                    <p className="text-xs text-gray-500">Week of {insight.periodStart}</p>
                    <InsightContent content={insight.content} />
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
