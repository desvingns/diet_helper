package com.k.shavrin.diethelper.domain.model

import java.text.Normalizer
import java.util.Locale

/** SQLite NOCASE folds ASCII only; normalize Unicode and Serbian scripts in Kotlin. */
fun normalizeProductQuery(value: String): String {
    val cyrillic = "абвгдђежзијклљмнњопрстћуфхцчџш"
    val latin = listOf("a", "b", "v", "g", "d", "dj", "e", "z", "z", "i", "j", "k", "l", "lj", "m", "n", "nj", "o", "p", "r", "s", "t", "c", "u", "f", "h", "c", "c", "dz", "s")
    val normalized = Normalizer.normalize(value.lowercase(Locale.ROOT).replace('ё', 'е'), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").replace("đ", "dj")
    return buildString {
        normalized.forEach { character ->
            val index = cyrillic.indexOf(character)
            append(if (index >= 0) latin[index] else character.toString())
        }
    }
}

fun Product.matchesQuery(query: String): Boolean {
    val haystack = normalizeProductQuery("$name $aliases $barcode")
    return normalizeProductQuery(query).split(Regex("\\s+")).all { it in haystack }
}
