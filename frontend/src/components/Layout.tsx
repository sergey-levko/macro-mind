import { NavLink, Outlet } from 'react-router-dom'

export default function Layout() {
  return (
    <div className="flex min-h-screen bg-gray-950 text-white">
      <aside className="w-56 flex-shrink-0 bg-gray-900 border-r border-gray-800 flex flex-col">
        <div className="px-6 py-5 border-b border-gray-800">
          <span className="text-xl font-bold text-white">Macro</span>
          <span className="text-xl font-bold text-teal-400">Mind</span>
        </div>
        <nav className="flex flex-col gap-1 p-3 flex-1">
          <NavLink
            to="/dashboard"
            className={({ isActive }) =>
              `px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-teal-600 text-white'
                  : 'text-gray-400 hover:bg-gray-800 hover:text-white'
              }`
            }
          >
            Dashboard
          </NavLink>
          <NavLink
            to="/meal-log"
            className={({ isActive }) =>
              `px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-teal-600 text-white'
                  : 'text-gray-400 hover:bg-gray-800 hover:text-white'
              }`
            }
          >
            Meal Log
          </NavLink>
          <NavLink
            to="/profile"
            className={({ isActive }) =>
              `px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-teal-600 text-white'
                  : 'text-gray-400 hover:bg-gray-800 hover:text-white'
              }`
            }
          >
            Profile
          </NavLink>
        </nav>
      </aside>
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  )
}
