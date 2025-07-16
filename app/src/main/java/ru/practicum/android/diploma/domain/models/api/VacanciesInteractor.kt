package ru.practicum.android.diploma.domain.models.api

import kotlinx.coroutines.flow.Flow
import ru.practicum.android.diploma.domain.models.paging.VacanciesResult
import ru.practicum.android.diploma.util.Resource

interface VacanciesInteractor {
    fun search(text: String, page: Int): Flow<Resource<VacanciesResult>>
    fun clearCache()
}
