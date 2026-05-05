package com.k.shavrin.diethelper.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import com.k.shavrin.diethelper.data.local.GoalsDataSource
import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * GoalsRepositoryImpl is a thin delegate to GoalsDataSource.
 * We test it with an in-memory DataStore<Preferences> so we exercise
 * the real GoalsDataSource serialisation logic without Android context.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GoalsRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dataSource: GoalsDataSource
    private lateinit var repository: GoalsRepositoryImpl

    @Before
    fun setUp() {
        dataSource = GoalsDataSource(InMemoryPreferencesDataStore())
        repository = GoalsRepositoryImpl(dataSource)
    }

    @Test
    fun `getDailyGoals returns DEFAULT when nothing saved yet`() = runTest {
        repository.getDailyGoals().test {
            val goals = awaitItem()
            assertEquals(DailyGoals.DEFAULT.calories, goals.calories, 0.001f)
            assertEquals(DailyGoals.DEFAULT.proteinMin, goals.proteinMin, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `saveGoals persists all fields and getDailyGoals reflects them`() = runTest {
        val saved = DailyGoals(
            calories = 1800f,
            proteinMin = 100f, proteinMax = 160f,
            fatMin = 50f, fatMax = 70f,
            carbsMin = 180f, carbsMax = 260f
        )
        repository.saveGoals(saved)

        repository.getDailyGoals().test {
            val goals = awaitItem()
            assertEquals(saved.calories, goals.calories, 0.001f)
            assertEquals(saved.proteinMin, goals.proteinMin, 0.001f)
            assertEquals(saved.proteinMax, goals.proteinMax, 0.001f)
            assertEquals(saved.fatMin, goals.fatMin, 0.001f)
            assertEquals(saved.fatMax, goals.fatMax, 0.001f)
            assertEquals(saved.carbsMin, goals.carbsMin, 0.001f)
            assertEquals(saved.carbsMax, goals.carbsMax, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getDailyGoals reacts to saveGoals update`() = runTest {
        repository.getDailyGoals().test {
            awaitItem() // default

            repository.saveGoals(DailyGoals.DEFAULT.copy(calories = 2200f))

            assertEquals(2200f, awaitItem().calories, 0.001f)
            cancelAndConsumeRemainingEvents()
        }
    }
}

/**
 * Pure-JVM in-memory DataStore<Preferences> — no Android context required.
 */
private class InMemoryPreferencesDataStore : DataStore<Preferences> {
    private val _data = MutableStateFlow<Preferences>(emptyPreferences())

    override val data: Flow<Preferences> = _data

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val newValue = transform(_data.value)
        _data.value = newValue
        return newValue
    }
}
