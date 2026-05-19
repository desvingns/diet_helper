package com.k.shavrin.diethelper.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Unit tests for the navigation contract — verifies the data that AppNavHost depends on
 * (route constants, parameterised route builders, BottomNavItems list) plus a structural
 * smoke-test that AppNavHost.kt references every declared route.
 *
 * Full composable rendering of [AppNavHost] is not covered here — it requires Hilt-aware
 * test setup (`@HiltAndroidTest` + emulator or Robolectric Hilt) because every screen
 * resolves its ViewModel via `hiltViewModel()`. Add a HiltAndroidTest in `androidTest/`
 * when navigation behaviour itself (back-stack, deep links, arg restoration) needs coverage.
 */
class AppNavHostTest {

    // ── Routes: constants ────────────────────────────────────────────────────

    @Test
    fun `top-level route constants are stable`() {
        assertEquals("today", Routes.TODAY)
        assertEquals("history", Routes.HISTORY)
        assertEquals("statistics", Routes.STATISTICS)
        assertEquals("weight", Routes.WEIGHT)
        assertEquals("settings", Routes.SETTINGS)
        assertEquals("export", Routes.EXPORT)
    }

    @Test
    fun `parameterised route patterns expose the expected argument names`() {
        assertTrue(Routes.PRODUCT_SEARCH.contains("{${Routes.ARG_DATE}}"))
        assertTrue(Routes.PRODUCT_SEARCH.contains("{${Routes.ARG_MEAL_TYPE}}"))
        assertTrue(Routes.ADD_PRODUCT.contains("{${Routes.ARG_NAME}}"))
        assertTrue(Routes.HISTORY_DAY.contains("{${Routes.ARG_DATE}}"))
    }

    // ── Routes: builders ─────────────────────────────────────────────────────

    @Test
    fun `productSearch builds path matching the route pattern`() {
        val built = Routes.productSearch("2026-05-19", "BREAKFAST")
        assertEquals("product_search/2026-05-19/BREAKFAST", built)
        // Same shape as the pattern, with placeholders replaced by values
        val pattern = Routes.PRODUCT_SEARCH
        assertEquals(pattern.count { it == '/' }, built.count { it == '/' })
    }

    @Test
    fun `addProduct without name argument still encodes the name query param`() {
        val built = Routes.addProduct()
        assertEquals("add_product?name=", built)
    }

    @Test
    fun `addProduct with name embeds it in the query string`() {
        val built = Routes.addProduct("Яблоко")
        assertEquals("add_product?name=Яблоко", built)
    }

    @Test
    fun `historyDay builds path matching the route pattern`() {
        val built = Routes.historyDay("2026-05-19")
        assertEquals("history_day/2026-05-19", built)
    }

    // ── BottomNavItems ───────────────────────────────────────────────────────

    @Test
    fun `BottomNavItems lists exactly five entries`() {
        assertEquals(5, BottomNavItems.size)
    }

    @Test
    fun `BottomNavItems routes match the corresponding Routes constants`() {
        assertEquals(Routes.TODAY, BottomNavItem.Today.route)
        assertEquals(Routes.HISTORY, BottomNavItem.History.route)
        assertEquals(Routes.STATISTICS, BottomNavItem.Statistics.route)
        assertEquals(Routes.WEIGHT, BottomNavItem.Weight.route)
        assertEquals(Routes.SETTINGS, BottomNavItem.Settings.route)
    }

    @Test
    fun `BottomNavItems all have a Russian label and a non-null icon`() {
        for (item in BottomNavItems) {
            assertTrue("label is blank for ${item.route}", item.label.isNotBlank())
            // Every label is Cyrillic — we ship UI in Russian
            assertTrue(
                "label '${item.label}' for ${item.route} is not Cyrillic",
                item.label.any { it in 'А'..'я' || it == 'Ё' || it == 'ё' }
            )
            assertNotNull("icon is null for ${item.route}", item.icon)
        }
    }

    @Test
    fun `BottomNavItems list order matches the canonical tab order`() {
        val order = BottomNavItems.map { it.route }
        assertEquals(
            listOf(Routes.TODAY, Routes.HISTORY, Routes.STATISTICS, Routes.WEIGHT, Routes.SETTINGS),
            order
        )
    }

    // ── Structural smoke: AppNavHost.kt references every declared route ─────

    @Test
    fun `AppNavHost source references every Routes constant in a composable call`() {
        val source = readAppNavHostSource()
        // Each top-level route appears in a composable(...) call.
        val expectedComposables = listOf(
            "composable(Routes.TODAY)",
            "composable(Routes.HISTORY)",
            "composable(Routes.STATISTICS)",
            "composable(Routes.WEIGHT)",
            "composable(Routes.SETTINGS)",
            "composable(Routes.EXPORT)"
        )
        for (snippet in expectedComposables) {
            assertTrue(
                "AppNavHost.kt is missing $snippet",
                source.contains(snippet)
            )
        }
        // Parameterised routes appear via route = Routes.X (with arguments block)
        for (paramRoute in listOf("Routes.PRODUCT_SEARCH", "Routes.ADD_PRODUCT", "Routes.HISTORY_DAY")) {
            assertTrue(
                "AppNavHost.kt is missing route = $paramRoute",
                source.contains("route = $paramRoute")
            )
        }
    }

    @Test
    fun `AppNavHost startDestination is the today route`() {
        val source = readAppNavHostSource()
        assertTrue(
            "AppNavHost.kt must declare startDestination = Routes.TODAY",
            source.contains("startDestination = Routes.TODAY")
        )
    }

    private fun readAppNavHostSource(): String {
        // Tests run from the :app module dir; AppNavHost lives at the path below.
        val path = "src/main/java/com/k/shavrin/diethelper/presentation/navigation/AppNavHost.kt"
        val file = File(path)
        assertTrue("Source not found at $path (cwd=${File(".").absolutePath})", file.exists())
        return file.readText()
    }
}
