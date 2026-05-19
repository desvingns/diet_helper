package com.k.shavrin.diethelper.presentation.screen.export

import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.k.shavrin.diethelper.data.FakeFoodEntryRepository
import com.k.shavrin.diethelper.data.FakeGoalsRepository
import com.k.shavrin.diethelper.domain.model.ExportMode
import com.k.shavrin.diethelper.domain.model.ReportData
import com.k.shavrin.diethelper.domain.repository.ReportRenderer
import com.k.shavrin.diethelper.domain.usecase.export.ExportReportUseCase
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ExportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var foodRepo: FakeFoodEntryRepository
    private lateinit var goalsRepo: FakeGoalsRepository
    private lateinit var renderer: ControllableRenderer
    private lateinit var useCase: ExportReportUseCase
    private lateinit var viewModel: ExportViewModel

    @Before
    fun setUp() {
        foodRepo = FakeFoodEntryRepository()
        goalsRepo = FakeGoalsRepository()
        renderer = ControllableRenderer()
        useCase = ExportReportUseCase(foodRepo, goalsRepo, renderer)
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        viewModel = ExportViewModel(context, useCase)
    }

    // ── (a) Initial state ───────────────────────────────────────────────────

    @Test
    fun `initial state has 7-day range ending today with DETAILED mode and stats enabled`() = runTest {
        val today = LocalDate.now()
        val state = viewModel.state.value
        assertEquals(today.minusDays(6), state.from)
        assertEquals(today, state.to)
        assertEquals(ExportMode.DETAILED, state.mode)
        assertTrue(state.includeStats)
        assertFalse(state.isExporting)
        assertNull(state.errorMessage)
    }

    // ── (b) onFromChange / onToChange update state ─────────────────────────

    @Test
    fun `setFrom updates from when before to`() = runTest {
        val today = LocalDate.now()
        val newFrom = today.minusDays(10)
        viewModel.setFrom(newFrom)
        assertEquals(newFrom, viewModel.state.value.from)
        assertEquals(today, viewModel.state.value.to)
    }

    @Test
    fun `setTo updates to when after from`() = runTest {
        val today = LocalDate.now()
        val newTo = today.minusDays(1)
        viewModel.setTo(newTo)
        assertEquals(newTo, viewModel.state.value.to)
        // from unchanged
        assertEquals(today.minusDays(6), viewModel.state.value.from)
    }

    // ── (c) onModeChange / onStatsToggle update state ──────────────────────

    @Test
    fun `setMode updates mode`() = runTest {
        viewModel.setMode(ExportMode.SUMMARY_ONLY)
        assertEquals(ExportMode.SUMMARY_ONLY, viewModel.state.value.mode)
        viewModel.setMode(ExportMode.DETAILED)
        assertEquals(ExportMode.DETAILED, viewModel.state.value.mode)
    }

    @Test
    fun `setIncludeStats updates includeStats`() = runTest {
        viewModel.setIncludeStats(false)
        assertFalse(viewModel.state.value.includeStats)
        viewModel.setIncludeStats(true)
        assertTrue(viewModel.state.value.includeStats)
    }

    // ── (d) export() happy path ────────────────────────────────────────────

    @Test
    fun `exportReport on success emits Share event with non-null uri and resets isExporting`() = runTest {
        // Renderer needs to return a path to a real file that FileProvider can serve.
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val tmp = File(reportsDir, "diet_helper_test.pdf").apply {
            if (!exists()) createNewFile()
        }
        renderer.pathToReturn = tmp.absolutePath

        viewModel.events.test {
            viewModel.exportReport()
            val event = awaitItem()
            assertTrue("Expected ExportEvent.Share, got $event", event is ExportEvent.Share)
            event as ExportEvent.Share
            assertNotNull(event.uri)
            cancelAndConsumeRemainingEvents()
        }
        assertFalse(viewModel.state.value.isExporting)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `exportReport calls the use case with current state config`() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val tmp = File(reportsDir, "diet_helper_call.pdf").apply {
            if (!exists()) createNewFile()
        }
        renderer.pathToReturn = tmp.absolutePath

        viewModel.setMode(ExportMode.SUMMARY_ONLY)
        viewModel.setIncludeStats(false)

        viewModel.exportReport()
        // give the coroutine a chance to invoke the use case (UnconfinedTestDispatcher runs eagerly)
        val capturedData = renderer.lastData
        assertNotNull("Renderer was not called", capturedData)
        // The use case forwards the config (with possibly-swapped range) into ReportData.config
        assertEquals(ExportMode.SUMMARY_ONLY, capturedData!!.config.mode)
        assertFalse(capturedData.config.includeStats)
    }

    // ── (e) export() failure path ──────────────────────────────────────────

    @Test
    fun `exportReport on use case throw emits Error event and resets isExporting`() = runTest {
        renderer.shouldThrowMessage = "Boom"

        viewModel.events.test {
            viewModel.exportReport()
            val event = awaitItem()
            assertTrue("Expected ExportEvent.Error, got $event", event is ExportEvent.Error)
            event as ExportEvent.Error
            assertEquals("Boom", event.message)
            cancelAndConsumeRemainingEvents()
        }
        assertFalse(viewModel.state.value.isExporting)
        assertEquals("Boom", viewModel.state.value.errorMessage)
    }

    @Test
    fun `exportReport with null throwable message uses fallback Russian text`() = runTest {
        renderer.shouldThrowEmptyMessage = true

        viewModel.events.test {
            viewModel.exportReport()
            val event = awaitItem()
            assertTrue(event is ExportEvent.Error)
            event as ExportEvent.Error
            assertEquals("Не удалось сформировать PDF", event.message)
            cancelAndConsumeRemainingEvents()
        }
    }

    // ── extra: setFrom auto-swap when from > to ────────────────────────────

    @Test
    fun `setFrom swaps when picked date is after current to`() = runTest {
        val today = LocalDate.now()
        // current to == today; pick from = today + 5 days
        val futureFrom = today.plusDays(5)
        viewModel.setFrom(futureFrom)

        val state = viewModel.state.value
        // old to becomes new from; picked date becomes new to
        assertEquals(today, state.from)
        assertEquals(futureFrom, state.to)
    }

    @Test
    fun `setTo swaps when picked date is before current from`() = runTest {
        val today = LocalDate.now()
        // current from == today - 6 days; pick to = today - 30 days
        val earlierTo = today.minusDays(30)
        viewModel.setTo(earlierTo)

        val state = viewModel.state.value
        assertEquals(earlierTo, state.from)
        assertEquals(today.minusDays(6), state.to)
    }

    // ── Controllable renderer (Fake) ───────────────────────────────────────

    private class ControllableRenderer : ReportRenderer {
        var pathToReturn: String = "/tmp/fake_report.pdf"
        var shouldThrowMessage: String? = null
        var shouldThrowEmptyMessage: Boolean = false
        var lastData: ReportData? = null

        override suspend fun render(data: ReportData): String {
            lastData = data
            shouldThrowMessage?.let { throw IllegalStateException(it) }
            if (shouldThrowEmptyMessage) {
                // A throwable whose `.message` is null
                throw object : IllegalStateException() {}
            }
            return pathToReturn
        }
    }
}
