package com.k.shavrin.diethelper.domain.model

/** A reviewable template. Working loads are supplied locally for each week, never inferred from old records. */
object ReturnToTrainingPlan {
    fun create(): WorkoutPlan = WorkoutPlan(
        "Возвращение · 6 недель", (1..6).flatMap { week -> (1..5).map { day -> createDay(week, day) } },
        WorkoutProgram.RETURN
    )

    fun weekDescription(week: Int): String = when (week) {
        1 -> "Вход: RPE до 6. Повторяем комфортные нагрузки, собираем реакцию на следующий день."
        2 -> "Закрепление: RPE до 6. После отчёта выбираем повтор или небольшую прибавку."
        3 -> "Развитие: RPE до 7. Меняем вес или объём; прибавка не обязательна."
        4 -> "Лёгкая волна: меньше подходов, 90% выбранных рабочих весов недели. RPE до 6."
        5 -> "Развитие: RPE до 7. Рабочие веса выбираем заново после отчёта."
        else -> "Контроль: RPE до 7–8 при хорошей переносимости. Проверка максимума не требуется."
    }

    private fun createDay(week: Int, day: Int): WorkoutDay {
        val lightWeek = week == 4
        val sets = if (lightWeek) 2 else 3
        val wave = if (lightWeek) 0.9 else 1.0
        val exercises = when (day) {
            1 -> listOf(lift(1, "Приседания со штангой", "squat", 3, sets, wave),
                lift(2, "Жим лёжа · лёгкий", "bench", 5, 2, 0.85 * wave))
            2 -> listOf(lift(2, "Жим лёжа", "bench", 5, sets, wave))
            3 -> listOf(lift(3, "Становая тяга сумо", "deadlift", 3, sets, wave),
                lift(2, "Жим лёжа · лёгкий", "bench", 6, 2, 0.9 * wave))
            4 -> listOf(WorkoutExercise(4, "Подтягивания", "Лёгкая", "", 1.0, 2.5, 1.0,
                listOf(WorkoutGroup(4, 2, 1.0)), TrainingLoadBasis.BODYWEIGHT))
            else -> listOf(lift(1, "Приседания со штангой · лёгкие", "squat", 3, sets, 0.85 * wave))
        }
        val title = listOf("Присед + лёгкий жим", "Жим и верх тела", "Сумо + лёгкий жим",
            "Короткий верх · необязательный", "Лёгкий присед и ноги")[day - 1]
        val accessory = when (day) {
            1 -> "По желанию: сгибание ног 2×10–12 с запасом."
            2 -> "Тяга с упором грудью 2×8–12 с запасом. Вес подбери отдельно и запиши как дополнительный подход."
            3 -> "Первая тяга: подбирай вес с лёгкой разминки; прекрати движение при необычных ощущениях."
            4 -> "30–45 минут достаточно. Подтягивания с запасом, при необходимости замени на тягу блока. " +
                "Можно добавить руки 2×10–15 и спокойную ходьбу. День можно пропустить с заметкой без отработки объёма."
            else -> "По желанию: сгибание ног 2×10–12. Субботу и воскресенье делаем разными по нагрузке."
        }
        return WorkoutDay((week - 1) * 5 + day - 1, week, day,
            "${weekDescription(week)}\n$accessory\n" +
                "В таблице только рабочие подходы; разминка отдельно. Отдых в базе 2–4 минуты. " +
                "Пример ритма: вт/ср/пт/сб/вс, выходные утром; дни можно сдвигать, сохраняя восстановление. " +
                "Не добавляй весь прежний комплекс разминки как обязательную нагрузку. " +
                "Фронтальный присед и наклоны со штангой в этот шаблон не включены. " +
                "При повторных необычных ощущениях останови упражнение и пересмотри нагрузку.",
            exercises, WorkoutProgram.RETURN, title)
    }

    private fun lift(id: Int, name: String, base: String, reps: Int, sets: Int, percent: Double) =
        WorkoutExercise(id, name, if (percent < 0.95) "Лёгкая" else "Основная", base, 1.0, 2.5, 1.0,
            listOf(WorkoutGroup(reps, sets, percent)))
}
