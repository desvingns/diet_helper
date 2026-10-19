package com.k.shavrin.diethelper.domain.model

/** Search aliases, not a substitute for the manufacturer's product label. */
object FoodVocabulary {
    private val words = linkedMapOf(
        "mleko" to "молоко", "milk" to "молоко", "milch" to "молоко",
        "jogurt" to "йогурт", "yogurt" to "йогурт", "yoghurt" to "йогурт",
        "kefir" to "кефир", "skyr" to "скир", "quark" to "творог", "pavlaka" to "сметана",
        "mozzarella" to "моцарелла", "mocarela" to "моцарелла", "sir" to "сыр", "cheese" to "сыр",
        "piletina" to "курица", "pileca" to "курица", "chicken" to "курица", "poulet" to "курица",
        "jaja" to "яйца", "hleb" to "хлеб", "bread" to "хлеб", "tunjevina" to "тунец", "tuna" to "тунец",
        "pirinac" to "рис", "basmati" to "рис басмати", "pasta" to "макароны", "ketchup" to "кетчуп",
        "protein" to "протеин белковый", "wraps" to "тортильи лаваш", "butter" to "сливочное масло",
        "ice cream" to "мороженое", "cokolada" to "шоколад", "chocolate" to "шоколад",
        "oats" to "овсянка", "ovsene" to "овсянка", "pecenica" to "печеница копчёная свинина"
    )

    fun russianAliases(name: String): String {
        val normalized = normalizeProductQuery(name)
        return words.filterKeys { normalized.contains(it) }.values.distinct().joinToString(" ")
    }

    fun serbianQuery(query: String): String = mapOf(
        "молоко" to "mleko", "йогурт" to "jogurt", "сыр" to "sir", "курица" to "piletina",
        "яйца" to "jaja", "хлеб" to "hleb", "тунец" to "tunjevina", "рис" to "pirinac",
        "сметана" to "pavlaka", "кефир" to "kefir", "моцарелла" to "mozzarella", "протеин" to "protein"
    )[query.lowercase().trim()] ?: query
}
