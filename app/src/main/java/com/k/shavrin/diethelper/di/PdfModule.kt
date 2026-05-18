package com.k.shavrin.diethelper.di

import com.k.shavrin.diethelper.data.pdf.PdfReportRenderer
import com.k.shavrin.diethelper.domain.repository.ReportRenderer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PdfModule {

    @Binds
    @Singleton
    abstract fun bindReportRenderer(impl: PdfReportRenderer): ReportRenderer
}
