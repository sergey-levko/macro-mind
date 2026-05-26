export type GoalType = 'LOSE_WEIGHT' | 'MAINTAIN_WEIGHT' | 'GAIN_MUSCLE'
export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK'

export interface UserResponse {
  id: string
  name: string
  email: string
  age: number
  weightKg: number
  heightCm: number
  goalType: GoalType
}

export interface CreateUserRequest {
  name: string
  email: string
  age: number
  weightKg: number
  heightCm: number
  goalType: GoalType
}

export interface NutritionalGoal {
  id: string
  userId: string
  caloriesTarget: number
  proteinG: number
  carbsG: number
  fatG: number
}

export interface MacroTotals {
  caloriesKcal: number
  proteinG: number
  carbsG: number
  fatG: number
}

export interface MacroTargets {
  caloriesTarget: number
  proteinG: number
  carbsG: number
  fatG: number
}

export interface MacroPercentages {
  caloriesPct: number
  proteinPct: number
  carbsPct: number
  fatPct: number
}

export interface SummaryCard {
  date: string
  totals: MacroTotals
  targets: MacroTargets | null
  percentages: MacroPercentages | null
}

export interface DailyEntry {
  date: string
  totals: MacroTotals
}

export interface WeeklySummary {
  weekStart: string
  days: DailyEntry[]
  weeklyTotals: MacroTotals
  weeklyTargets: MacroTargets | null
}

export interface MealItemResponse {
  itemId: string
  foodId: string
  foodName: string
  quantityG: number
  calories: number
  proteinG: number
  carbsG: number
  fatG: number
}

export interface MealLog {
  id: string
  userId: string
  mealType: MealType
  loggedAt: string
  items: MealItemResponse[]
  totals: MacroTotals
}

export interface MealLogSummary {
  id: string
  mealType: MealType
  loggedAt: string
  totals: MacroTotals
}

export interface Food {
  id: string
  name: string
  source: string
  calories100g: number
  proteinG: number
  carbsG: number
  fatG: number
}

export interface UsdaFoodResult {
  fdcId: number
  description: string
}

export interface AuthResponse {
  token: string
  user: UserResponse
}
