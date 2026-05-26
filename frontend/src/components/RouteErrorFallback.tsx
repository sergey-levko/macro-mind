import { useNavigate } from 'react-router-dom'
import type { FallbackProps } from 'react-error-boundary'

export default function RouteErrorFallback({ error, resetErrorBoundary }: FallbackProps) {
  const navigate = useNavigate()

  return (
    <div className="flex items-center justify-center min-h-[60vh] px-4">
      <div className="w-full max-w-md bg-gray-900 border border-gray-800 rounded-2xl p-8 text-center space-y-4">
        <div className="w-12 h-12 bg-red-900/30 rounded-full flex items-center justify-center mx-auto">
          <svg className="w-6 h-6 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
          </svg>
        </div>
        <h2 className="text-lg font-semibold text-white">Something went wrong</h2>
        {import.meta.env.DEV && error?.message && (
          <p className="text-xs text-red-400 bg-red-900/20 rounded-lg px-3 py-2 text-left font-mono break-all">
            {error.message}
          </p>
        )}
        <div className="flex flex-col gap-2 pt-2">
          <button
            onClick={resetErrorBoundary}
            className="w-full py-2 bg-teal-600 hover:bg-teal-500 text-white text-sm font-medium rounded-lg transition-colors"
          >
            Reload section
          </button>
          <button
            onClick={() => { resetErrorBoundary(); navigate('/dashboard') }}
            className="w-full py-2 text-sm text-gray-400 hover:text-gray-200 transition-colors"
          >
            Go to dashboard
          </button>
        </div>
      </div>
    </div>
  )
}
