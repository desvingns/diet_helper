package com.k.shavrin.diethelper.domain.model

enum class WorkoutProgram(val id: String, val title: String) {
    SRC7("src7", "СРЦ7"),
    RETURN("return", "Возвращение");

    companion object {
        fun fromId(id: String?) = entries.firstOrNull { it.id == id } ?: SRC7
    }
}
