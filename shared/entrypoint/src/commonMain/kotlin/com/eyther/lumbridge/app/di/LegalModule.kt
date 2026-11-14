package com.eyther.lumbridge.app.di

import com.eyther.lumbridge.features.legal.data.local.datasource.CommonLegalDocumentsDataSource
import com.eyther.lumbridge.features.legal.domain.datasource.LegalDocumentsDataSource
import com.eyther.lumbridge.features.legal.domain.usecase.GetLegalDocumentsUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

fun legalModule(): Module = module {
    single<LegalDocumentsDataSource> { CommonLegalDocumentsDataSource() }
    single { GetLegalDocumentsUseCase(legalDocumentsDataSource = get()) }
}
