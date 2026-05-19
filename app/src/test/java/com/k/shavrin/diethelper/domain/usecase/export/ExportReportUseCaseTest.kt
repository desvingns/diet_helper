package com.k.shavrin.diethelper.domain.usecase.export

import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.data.FakeGoalsRepository
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.model.ExportConfig
import com.k.shavrin.diethelper.domain.model.ExportMode
import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.model.MealType
import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.model.ReportData
import com.k.shavrin.diethelper.domain.repository.ReportRenderer
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ExportReportUseCaseTest {

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
    private val renderer = FakeReportRenderer()
    private val useCase = ExportReportUseCase(foodRepo, goalsRepo, renderer)

    private val day1: LocalDate = LocalDate.of(2025, 5, 1)
    private val day2: LocalDate = LocalDate.of(2025, 5, 2)
    private val day3: LocalDate = LocalDate.of(2025, 5, 3)

    // ── Test products (per 100g) ───────────────────────────────────────────
    // 100 cal / 10 prot / 5 fat / 8 carbs
    private val productA = Product(
        id = 1, name = "Гречка",
        caloriesPer100g = 100f, proteinPer100g = 10f, fatPer100g = 5f, carbsPer100g = 8f
    )

    // 200 cal / 20 prot / 8 fat / 12 carbs
    private val productB = Product(
        id = 2, name = "Курица",
        caloriesPer100g = 200f, proteinPer100g = 20f, fatPer100g = 8f, carbsPer100g = 12f
    )

    // ── (a) Aggregation across 3 days, with empty middle day ───────────────

    @Test
    fun `aggregates daily summaries across 3 days with empty middle day`() = runTest {
        // day1: productA × 1 (100 cal) + productB × 1 (200 cal) → 300 cal
        // day2: empty
        // day3: productA × 2 (200 cal) → 200 cal
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = productA, date = day1, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 2, product = productB, date = day1, mealType = MealType.LUNCH, multiplier = 1f),
                FoodEntry(productId = 1, product = productA, date = day3, mealType = MealType.DINNER, multiplier = 2f)
            )
        )

        useCase(ExportConfig(from = day1, to = day3, mode = ExportMode.DETAILED, includeStats = false))

        val data = renderer.lastData!!
        assertEquals(3, data.days.size)
        assertEquals(day1, data.days[0].date)
        assertEquals(day2, data.days[1].date)
        assertEquals(day3, data.days[2].date)
        assertEquals(300f, data.days[0].summary.totalCalories, 0.001f)
        assertEquals(0f, data.days[1].summary.totalCalories, 0.001f)
        assertEquals(200f, data.days[2].summary.totalCalories, 0.001f)
        assertEquals(30f, data.days[0].summary.totalProtein, 0.001f)
        assertEquals(20f, data.days[2].summary.totalProtein, 0.001f)
    }

    // ── (b) DETAILED vs SUMMARY_ONLY entriesByMeal ─────────────────────────

    @Test
    fun `DETAILED mode populates entriesByMeal grouped by MealType`() = runTest {
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = productA, date = day1, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 2, product = productB, date = day1, mealType = MealType.BREAKFAST, multiplier = 0.5f),
                FoodEntry(productId = 1, product = productA, date = day1, mealType = MealType.DINNER, multiplier = 1f)
            )
        )

        useCase(ExportConfig(from = day1, to = day1, mode = ExportMode.DETAILED, includeStats = false))

        val byMeal = renderer.lastData!!.days[0].entriesByMeal
        assertEquals(2, byMeal[MealType.BREAKFAST]?.size)
        assertEquals(1, byMeal[MealType.DINNER]?.size)
        assertNull("LUNCH should not be present (no entries)", byMeal[MealType.LUNCH])
        // Verify per-entry math: productB × 0.5 → cal=100, prot=10, fat=4, carbs=6
        val secondBreakfast = byMeal[MealType.BREAKFAST]!![1]
        assertEquals("Курица", secondBreakfast.productName)
        assertEquals(50, secondBreakfast.grams)
        assertEquals(100f, secondBreakfast.calories, 0.001f)
        assertEquals(10f, secondBreakfast.protein, 0.001f)
        assertEquals(4f, secondBreakfast.fat, 0.001f)
        assertEquals(6f, secondBreakfast.carbs, 0.001f)
    }

    @Test
    fun `SUMMARY_ONLY mode leaves entriesByMeal empty for every day`() = runTest {
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = productA, date = day1, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = productA, date = day2, mealType = MealType.LUNCH, multiplier = 1f),
                FoodEntry(productId = 1, product = productA, date = day3, mealType = MealType.DINNER, multiplier = 1f)
            )
        )

        useCase(ExportConfig(from = day1, to = day3, mode = ExportMode.SUMMARY_ONLY, includeStats = false))

        val data = renderer.lastData!!
        assertEquals(3, data.days.size)
        for (day in data.days) {
            assertTrue("Expected empty entriesByMeal for $day", day.entriesByMeal.isEmpty())
        }
        // But the summary itself should still be present
        assertEquals(100f, data.days[0].summary.totalCalories, 0.001f)
    }

    // ── (c) includeStats true / false ──────────────────────────────────────

    @Test
    fun `includeStats true produces non-null stats`() = runTest {
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = productA, date = day1, mealType = MealType.BREAKFAST, multiplier = 1f)
            )
        )

        useCase(ExportConfig(from = day1, to = day3, mode = ExportMode.DETAILED, includeStats = true))

        assertNotNull(renderer.lastData!!.stats)
    }

    @Test
    fun `includeStats false produces null stats`() = runTest {
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = productA, date = day1, mealType = MealType.BREAKFAST, multiplier = 1f)
            )
        )

        useCase(ExportConfig(from = day1, to = day3, mode = ExportMode.DETAILED, includeStats = false))

        assertNull(renderer.lastData!!.stats)
    }

    // ── (d) Adherence %: goal 2000 kcal, tolerance 0.9–1.1 ─────────────────

    @Test
    fun `adherence counts only days within 0_9 to 1_1 of goal`() = runTest {
        // Build products carefully:
        //   day1 → 1850 kcal (within 1800..2200)         hit
        //   day2 → 2200 kcal (== upper bound)            hit
        //   day3 → 1900 kcal (within 1800..2200)         hit
        // Then add day4: 1500 kcal (below 1800)          miss
        //      day5: 2300 kcal (above 2200)              miss
        val pCal = Product(
            id = 100, name = "Стандарт",
            caloriesPer100g = 100f, proteinPer100g = 1f, fatPer100g = 1f, carbsPer100g = 1f
        )
        val day4 = day3.plusDays(1)
        val day5 = day3.plusDays(2)
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 100, product = pCal, date = day1, mealType = MealType.BREAKFAST, multiplier = 18.5f),
                FoodEntry(productId = 100, product = pCal, date = day2, mealType = MealType.BREAKFAST, multiplier = 22f),
                FoodEntry(productId = 100, product = pCal, date = day3, mealType = MealType.BREAKFAST, multiplier = 19f),
                FoodEntry(productId = 100, product = pCal, date = day4, mealType = MealType.BREAKFAST, multiplier = 15f),
                FoodEntry(productId = 100, product = pCal, date = day5, mealType = MealType.BREAKFAST, multiplier = 23f)
            )
        )

        useCase(ExportConfig(from = day1, to = day5, mode = ExportMode.DETAILED, includeStats = true))

        val stats = renderer.lastData!!.stats!!
        assertEquals(5, stats.daysCount)
        // 3 of 5 hit → 60%
        assertEquals(60f, stats.daysHitCaloriePercent, 0.001f)
    }

    // ── (e) Averages skip zero-calorie days ────────────────────────────────

    @Test
    fun `averages skip zero-calorie days`() = runTest {
        // day1: 100 cal / 10 prot
        // day2: 0
        // day3: 300 cal / 30 prot
        // sum/non-empty = (100 + 300) / 2 = 200 cal
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = productA, date = day1, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = productA, date = day3, mealType = MealType.BREAKFAST, multiplier = 3f)
            )
        )

        useCase(ExportConfig(from = day1, to = day3, mode = ExportMode.DETAILED, includeStats = true))

        val stats = renderer.lastData!!.stats!!
        assertEquals(3, stats.daysCount)
        assertEquals(2, stats.nonEmptyDaysCount)
        assertEquals(200f, stats.averageCalories, 0.001f)
        assertEquals(20f, stats.averageProtein, 0.001f)
        // productA: fat=5/100g; day1 → 5, day3 → 15; avg = 10
        assertEquals(10f, stats.averageFat, 0.001f)
        // carbs=8; day1 → 8, day3 → 24; avg = 16
        assertEquals(16f, stats.averageCarbs, 0.001f)
    }

    // ── (f) Range with from > to is swapped ────────────────────────────────

    @Test
    fun `range with from after to is swapped`() = runTest {
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = productA, date = day1, mealType = MealType.BREAKFAST, multiplier = 1f),
                FoodEntry(productId = 1, product = productA, date = day3, mealType = MealType.BREAKFAST, multiplier = 2f)
            )
        )

        // from > to (day3 → day1): should be swapped to day1..day3
        useCase(ExportConfig(from = day3, to = day1, mode = ExportMode.DETAILED, includeStats = false))

        val data = renderer.lastData!!
        // Days list covers the same dates (ascending)
        assertEquals(3, data.days.size)
        assertEquals(day1, data.days.first().date)
        assertEquals(day3, data.days.last().date)
        // File-name range (config.from/to) also matches the ordered dates
        assertEquals(day1, data.config.from)
        assertEquals(day3, data.config.to)
    }

    // ── (g) Empty range: full date span with zero summaries ────────────────

    @Test
    fun `empty range produces full date span with zero summaries`() = runTest {
        // No seeded entries
        useCase(ExportConfig(from = day1, to = day3, mode = ExportMode.DETAILED, includeStats = true))

        val data = renderer.lastData!!
        assertEquals(3, data.days.size)
        for (day in data.days) {
            assertEquals(0f, day.summary.totalCalories, 0f)
            assertEquals(0f, day.summary.totalProtein, 0f)
            assertEquals(0f, day.summary.totalFat, 0f)
            assertEquals(0f, day.summary.totalCarbs, 0f)
            assertTrue(day.entriesByMeal.isEmpty())
        }
        // Stats requested → zero averages and 0% adherence
        val stats = data.stats!!
        assertEquals(3, stats.daysCount)
        assertEquals(0, stats.nonEmptyDaysCount)
        assertEquals(0f, stats.averageCalories, 0f)
        assertEquals(0f, stats.averageProtein, 0f)
        assertEquals(0f, stats.averageFat, 0f)
        assertEquals(0f, stats.averageCarbs, 0f)
        assertEquals(0f, stats.daysHitCaloriePercent, 0f)
    }

    // ── (h) Use case returns renderer's string path unchanged ──────────────

    @Test
    fun `returns path from renderer unchanged`() = runTest {
        renderer.pathToReturn = "/tmp/fake_report.pdf"

        val result = useCase(
            ExportConfig(from = day1, to = day3, mode = ExportMode.DETAILED, includeStats = false)
        )

        assertEquals("/tmp/fake_report.pdf", result)
    }

    // ── extra: single-day range works ───────────────────────────────────────

    @Test
    fun `single-day range produces a single day`() = runTest {
        foodRepo.seed(
            listOf(
                FoodEntry(productId = 1, product = productA, date = day1, mealType = MealType.BREAKFAST, multiplier = 1f)
            )
        )

        useCase(ExportConfig(from = day1, to = day1, mode = ExportMode.DETAILED, includeStats = false))

        val data = renderer.lastData!!
        assertEquals(1, data.days.size)
        assertEquals(day1, data.days[0].date)
        assertEquals(100f, data.days[0].summary.totalCalories, 0.001f)
    }

    // ── Fake renderer captures last input ──────────────────────────────────

    private class FakeReportRenderer : ReportRenderer {
        var lastData: ReportData? = null
        var pathToReturn: String = "/tmp/fake_report.pdf"

        override suspend fun render(data: ReportData): String {
            lastData = data
            return pathToReturn
        }
    }
}
