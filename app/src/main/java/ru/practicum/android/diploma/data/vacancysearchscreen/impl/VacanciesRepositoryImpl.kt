package ru.practicum.android.diploma.data.vacancysearchscreen.impl

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.practicum.android.diploma.data.mappers.toDomain
import ru.practicum.android.diploma.data.models.vacancies.VacanciesRequest
import ru.practicum.android.diploma.data.models.vacancies.VacanciesResponseDto
import ru.practicum.android.diploma.data.vacancysearchscreen.network.SearchNetworkClient
import ru.practicum.android.diploma.domain.models.api.VacanciesRepository
import ru.practicum.android.diploma.domain.models.paging.VacanciesResult
import ru.practicum.android.diploma.util.Resource

class VacanciesRepositoryImpl(private val networkClient: SearchNetworkClient) : VacanciesRepository {
    private val loadedPages = mutableSetOf<Int>()

    override fun search(text: String, page: Int): Flow<Resource<VacanciesResult>> = flow {
        try {
            if (page in loadedPages) {
                emit(Resource.Success(VacanciesResult(emptyList(), page, 0, 0)))
            } else {
                val response = networkClient.doRequest(VacanciesRequest(text, page))
                when (response.resultCode) {
                    SEARCH_SUCCESS -> {
                        val vacanciesResponse = response as VacanciesResponseDto
                        loadedPages.add(page)

                        val data = vacanciesResponse.items.map { it.toDomain() }
                        val result = VacanciesResult(
                            vacancies = data,
                            page = vacanciesResponse.page,
                            pages = vacanciesResponse.pages,
                            totalFound = vacanciesResponse.found
                        )

                        emit(Resource.Success(result))
                    }
                    NO_CONNECTION -> emit(Resource.Error("No internet connection", ErrorType.NO_INTERNET))
                    SERVER_ERROR -> emit(Resource.Error("Server error", ErrorType.SERVER_ERROR))
                    else -> emit(Resource.Error("Unknown error", ErrorType.UNKNOWN))
                }
            }
        } catch (e: retrofit2.HttpException) {
            Log.e("Repository", "Search error", e)
            throw e
        }
    }

    override fun clearLoadedPages() {
        loadedPages.clear()
    }

    companion object {
        private const val NO_CONNECTION = -1
        private const val SEARCH_SUCCESS = 2
        private const val SERVER_ERROR = 5
    }
}

enum class ErrorType {
    NO_INTERNET,
    SERVER_ERROR,
    UNKNOWN
}
