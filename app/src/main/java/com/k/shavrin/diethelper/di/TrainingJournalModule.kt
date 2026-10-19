package com.k.shavrin.diethelper.di

import com.k.shavrin.diethelper.data.export.TrainingJournalFileExporter
import com.k.shavrin.diethelper.data.repository.TrainingJournalRepositoryImpl
import com.k.shavrin.diethelper.domain.repository.TrainingJournalExporter
import com.k.shavrin.diethelper.domain.repository.TrainingJournalRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrainingJournalModule {
    @Binds
    @Singleton
    abstract fun bindJournal(impl: TrainingJournalRepositoryImpl): TrainingJournalRepository

    @Binds
    @Singleton
    abstract fun bindExporter(impl: TrainingJournalFileExporter): TrainingJournalExporter

    companion object {
        @Provides
        @Singleton
        fun provideTrainingClock(): Clock = Clock.systemDefaultZone()
    }
}
