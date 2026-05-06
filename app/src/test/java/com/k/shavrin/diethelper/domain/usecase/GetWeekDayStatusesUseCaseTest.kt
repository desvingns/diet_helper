package com.k.shavrin.diethelper.domain.usecase

import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.data.FakeGoalsRepository
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.DayStatus
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.usecase.foodentry.GetWeekDayStatusesUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GetWeekDayStatusesUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val foodRepo = FakeFoodEntryRepository()
    private val goalsRepo = FakeGoalsRepository(
        DailyGoals(
            calories = 2000f,
            proteinMin = 80f, proteinMax = 120f,
            fatMin = 50f, fatMax = 70f,
            carbsMin = 200f, carbsMax = 300f
        )
    )
    private val useCase = GetWeekDayStatusesUseCase(foodRepo, goalsRepo)

    // Monday 2025-04-28
    private val monday = LocalDate.of(2025, 4, 28)

    private val product = Product(
        id = 1, name = "Тест",
        caloriesPer100g = 100f,
        proteinPer100g = 10f,
        fatPer100g = 6f,
        carbsPer100g = 25f
    )

    @Test
    fun `returns 7 days starting from Monday of the given date's week`() = runTest {
        useCase(monday).test {
            val statuses = awaitItem()
            assertEquals(7, statuses.size)
            assertEquals(DayOfWeek.MONDAY, statuses.first().first.dayOfWeek)
            assertEquals(DayOfWeek.SUNDAY, statuses.last().first.dayOfWeek)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `future days have FUTURE status`() = runTest {
        // Monday 2025-04-28 is in the past; use a date far in the future
        val futureMonday = LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY)
        useCase(futureMonday).test {
            val statuses = awaitItem()
            statuses.forEach { (_, status) ->
                assertEquals(DayStatus.FUTURE, status)
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `day with no entries gets GRAY_LOGGED status for past date`() = runTest {
        useCase(monday).test {
            val statuses = awaitItem()
            // Monday 2025-04-28 is in the past, no entries → 0 calories < 30% of 2000 → GRAY_LOGGED
            val mondayStatus = statuses.first { it.first == monday }.second
            assertEquals(DayStatus.GRAY_LOGGED, mondayStatus)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `day with green macros gets GREEN status`() = runTest {
        // product: cal=100, prot=10, fat=6, carbs=25
        // multiplier = 10 → cal=1000 (< 2000), prot=100 ∈ [80,120], fat=60 ∈ [50,70], carbs=250 ∈ [200,300]
        foodRepo.seed(
            listOf(
                FoodEntry(
                    productId = 1, product = product, date = monday,
                    mealType = MealType.BREAKFAST, multiplier = 10f
                )
            )
        )
        useCase(monday).test {
            val statuses = awaitItem()
            val mondayStatus = statuses.first { it.first == monday }.second
            assertEquals(DayStatus.GREEN, mondayStatus)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `day with exceeded calories gets RED status`() = runTest {
        // multiplier = 30 → cal = 3000 > 2000 * 1.25 = 2500 → RED
        foodRepo.seed(
            listOf(
                FoodEntry(
                    productId = 1, product = product, date = monday,
                    mealType = MealType.BREAKFAST, multiplier = 30f
                )
            )
        )
        useCase(monday).test {
            val statuses = awaitItem()
            val mondayStatus = statuses.first { it.first == monday }.second
            assertEquals(DayStatus.RED, mondayStatus)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── computeDayStatus companion tests ──────────────────────────────────────

    private val goals = DailyGoals(
        calories = 2000f,
        proteinMin = 80f, proteinMax = 120f,
        fatMin = 50f, fatMax = 70f,
        carbsMin = 200f, carbsMax = 300f
    )

    private fun entry(multiplier: Float, date: LocalDate = monday) = FoodEntry(
        productId = 1, product = product, date = date,
        mealType = MealType.BREAKFAST, multiplier = multiplier
    )

    @Test
    fun `computeDayStatus returns FUTURE for a date after today`() {
        val tomorrow = LocalDate.now().plusDays(1)
        val status = GetWeekDayStatusesUseCase.computeDayStatus(tomorrow, emptyList(), goals)
        assertEquals(DayStatus.FUTURE, status)
    }

    @Test
    fun `computeDayStatus returns GRAY_LOGGED when calories below 30 percent threshold`() {
        // threshold = 2000 * 0.30 = 600; product cal=100, multiplier=5 → 500 < 600
        val status = GetWeekDayStatusesUseCase.computeDayStatus(monday, listOf(entry(5f)), goals)
        assertEquals(DayStatus.GRAY_LOGGED, status)
    }

    @Test
    fun `computeDayStatus returns GREEN when calories and all macros are within goal ranges`() {
        // multiplier=10: cal=1000, prot=100∈[80,120], fat=60∈[50,70], carbs=250∈[200,300]
        val status = GetWeekDayStatusesUseCase.computeDayStatus(monday, listOf(entry(10f)), goals)
        assertEquals(DayStatus.GREEN, status)
    }

    @Test
    fun `computeDayStatus returns YELLOW when calories under 125 percent and macros in relaxed range`() {
        // Use a product that hits yellow: calories just under 125%, macros ±25% of bounds
        // product: cal=100, prot=10, fat=6, carbs=25 per multiplier=1
        // multiplier=22: cal=2200 < 2000*1.25=2500 ✓
        // prot=220 > proteinMax*1.25=150 ✗ — doesn't qualify
        // Need a product that gives yellow: calories in (goal, 1.25*goal), macros in relaxed window
        // Use a product with cal=150, prot=5, fat=3, carbs=15 per mult=1
        // multiplier=14: cal=2100 < 2500 ✓; prot=70 ∈ [60,150] ✓; fat=42 ∈ [37.5,87.5] ✓; carbs=210 ∈ [150,375] ✓
        // but cal=2100 > goal=2000, so isGreen=false
        val yellowProduct = Product(
            id = 2, name = "Жёлтый",
            caloriesPer100g = 150f,
            proteinPer100g = 5f,
            fatPer100g = 3f,
            carbsPer100g = 15f
        )
        val yellowEntry = FoodEntry(
            productId = 2, product = yellowProduct, date = monday,
            mealType = MealType.BREAKFAST, multiplier = 14f
        )
        // cal=2100, prot=70∈[60,150], fat=42∈[37.5,87.5], carbs=210∈[150,375] → YELLOW
        val status = GetWeekDayStatusesUseCase.computeDayStatus(monday, listOf(yellowEntry), goals)
        assertEquals(DayStatus.YELLOW, status)
    }

    @Test
    fun `computeDayStatus returns RED when calories exceed 125 percent of goal`() {
        // multiplier=30: cal=3000 > 2500 → RED
        val status = GetWeekDayStatusesUseCase.computeDayStatus(monday, listOf(entry(30f)), goals)
        assertEquals(DayStatus.RED, status)
    }

    @Test
    fun `computeDayStatus returns RED when macros are out of relaxed range even if calories moderate`() {
        // multiplier=20: cal=2000 = goal, NOT < goal → isGreen=false
        // prot=200 > proteinMax*1.25=150 → isYellow=false → RED
        val status = GetWeekDayStatusesUseCase.computeDayStatus(monday, listOf(entry(20f)), goals)
        assertEquals(DayStatus.RED, status)
    }

    @Test
    fun `computeDayStatus returns GRAY_LOGGED for today when no entries`() {
        val today = LocalDate.now()
        val status = GetWeekDayStatusesUseCase.computeDayStatus(today, emptyList(), goals)
        assertEquals(DayStatus.GRAY_LOGGED, status)
    }
}
