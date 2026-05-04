package com.k.shavrin.diethelper.presentation.navigation

object Routes {
    const val TODAY = "today"
    const val HISTORY = "history"
    const val WEIGHT = "weight"
    const val SETTINGS = "settings"

    const val PRODUCT_SEARCH = "product_search/{date}/{mealType}"
    const val ADD_PRODUCT = "add_product?name={name}"
    const val HISTORY_DAY = "history_day/{date}"

    const val ARG_DATE = "date"
    const val ARG_MEAL_TYPE = "mealType"
    const val ARG_NAME = "name"

    fun productSearch(date: String, mealType: String): String =
        "product_search/$date/$mealType"

    fun addProduct(name: String = ""): String = "add_product?name=$name"

    fun historyDay(date: String): String = "history_day/$date"
}
