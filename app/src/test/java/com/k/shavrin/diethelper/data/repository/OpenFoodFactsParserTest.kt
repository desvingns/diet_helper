package com.k.shavrin.diethelper.data.repository

import android.app.Application
import com.k.shavrin.diethelper.domain.model.matchesQuery
import com.k.shavrin.diethelper.domain.repository.FoodMarket
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class OpenFoodFactsParserTest {

    @Test
    fun `brand search uses full text and keeps country and store filters`() {
        assertEquals("Milbona AND countries_tags:\"en:serbia\" AND stores:\"lidl\"",
            OpenFoodFactsRepository.textSearchQuery("Milbona", FoodMarket.SERBIA, true))
        assertEquals("mleko AND countries_tags:\"en:serbia\"",
            OpenFoodFactsRepository.textSearchQuery("молоко", FoodMarket.SERBIA, false))
    }
    private fun product() = JSONObject("""{
        "code":"1234567890123", "product_name":"Mleko", "product_name_ru":"Молоко", "brands":"Pilos",
        "stores":"Lidl", "countries_tags":["en:serbia"],
        "nutriments":{"energy-kcal_100g":46,"proteins_100g":3.2,"fat_100g":1.5,"carbohydrates_100g":4.8}
    }""")

    @Test
    fun `keeps Russian name barcode aliases and attribution`() {
        val parsed = OpenFoodFactsRepository.parseProduct(product())!!
        assertEquals("Молоко · Pilos", parsed.name)
        assertEquals(46f, parsed.caloriesPer100g, 0.001f)
        assertTrue(parsed.aliases.contains("Lidl"))
        assertEquals("https://world.openfoodfacts.org/product/1234567890123", parsed.sourceUrl)
    }

    @Test
    fun `modern search array fields remain readable and Serbian names get Russian search aliases`() {
        val item = product()
        item.remove("product_name_ru")
        item.put("brands", JSONArray("[\"Pilos\"]"))
        item.put("stores", JSONArray("[\"Lidl\"]"))
        val parsed = OpenFoodFactsRepository.parseProduct(item)!!
        assertEquals("Mleko · Pilos", parsed.name)
        assertTrue(parsed.matchesQuery("молоко"))
        assertTrue(parsed.matchesQuery("ПИЛОС"))
        assertTrue(parsed.aliases.contains("Лидл"))
    }

    @Test
    fun `missing macros are excluded instead of converted to misleading zeros`() {
        val item = product()
        item.getJSONObject("nutriments").remove("proteins_100g")
        assertNull(OpenFoodFactsRepository.parseProduct(item))
    }

    @Test
    fun `kJ energy is converted and impossible nutrition is rejected`() {
        val item = product()
        val nutrition = item.getJSONObject("nutriments")
        nutrition.remove("energy-kcal_100g")
        nutrition.put("energy-kj_100g", 192.464)
        assertEquals(46f, OpenFoodFactsRepository.parseProduct(item)!!.caloriesPer100g, 0.001f)
        nutrition.put("fat_100g", -1)
        assertNull(OpenFoodFactsRepository.parseProduct(item))
    }
}
