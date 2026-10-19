package com.k.shavrin.diethelper.domain.usecase.workout

import com.k.shavrin.diethelper.domain.model.RecoveryStatus
import com.k.shavrin.diethelper.domain.model.TrainingOutcome
import com.k.shavrin.diethelper.domain.model.TrainingSession
import javax.inject.Inject

class SuggestTrainingAdjustmentUseCase @Inject constructor() {
    operator fun invoke(session: TrainingSession?): String {
        if (session == null) return "Записывай фактические подходы и самочувствие. Волны исходного плана сохранены."
        val symptoms = session.completedSets.filter { it.symptomNote.isNotBlank() }.map { it.exerciseName }.distinct()
        return when {
            session.recovery == RecoveryStatus.ILLNESS || session.nextDayRecovery == RecoveryStatus.ILLNESS ->
                "Отмечена болезнь: пересмотри следующее занятие по состоянию. Не компенсируй пропущенный объём."
            symptoms.isNotEmpty() || session.recovery == RecoveryStatus.DISCOMFORT || session.nextDayRecovery == RecoveryStatus.DISCOMFORT ->
                "Не повышай нагрузку в движениях с необычными ощущениями. " +
                symptoms.joinToString().let { if (it.isNotEmpty()) "$it. " else "" } +
                "Обсуди снижение нагрузки или другой вариант; повторяющиеся симптомы требуют оценки."
            session.outcome == TrainingOutcome.ACTIVE ->
                "Записывай усилие и ощущения по ходу занятия. При необходимости меняй или пропускай подходы; волны плана сохранены."
            session.nextDayRecovery == RecoveryStatus.UNKNOWN ->
                "Самочувствие на следующий день ещё не отмечено. Сначала дополни журнал; оснований для автоматической прибавки нет."
            else -> workloadAdvice(session)
        }
    }

    private fun workloadAdvice(session: TrainingSession): String {
        val hard = session.completedSets.any { (it.rpe ?: 0.0) >= 8 } || (session.sessionRpe ?: 0.0) >= 8
        val missed = session.completedSets.any {
            it.exerciseName == it.plannedExerciseName && it.plannedReps != null && it.actualReps < it.plannedReps
        }
        return if (hard || missed || session.outcome == TrainingOutcome.STOPPED) {
            "Были тяжёлые подходы, недобор повторений или остановка. В затронутых движениях повтори или облегчи нагрузку; не догоняй объём."
        } else {
            "Отмечено обычное самочувствие. Можно повторить посильную нагрузку с учётом следующего дня плана. Прибавка остаётся твоим решением."
        }
    }
}
