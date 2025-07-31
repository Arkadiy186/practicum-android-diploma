package ru.practicum.android.diploma.data.filters.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.practicum.android.diploma.data.mappers.toDomain
import ru.practicum.android.diploma.data.mappers.toRegion
import ru.practicum.android.diploma.data.models.areas.country.CountriesRequest
import ru.practicum.android.diploma.data.models.areas.country.CountriesResponseDto
import ru.practicum.android.diploma.data.models.areas.regions.RegionsRequest
import ru.practicum.android.diploma.data.models.areas.regions.RegionsResponseDto
import ru.practicum.android.diploma.data.models.industries.remote.IndustryRequest
import ru.practicum.android.diploma.data.models.industries.remote.IndustryResponseDto
import ru.practicum.android.diploma.data.vacancysearchscreen.impl.ErrorType
import ru.practicum.android.diploma.data.vacancysearchscreen.network.NetworkClient
import ru.practicum.android.diploma.domain.filters.repository.FiltersRepository
import ru.practicum.android.diploma.domain.models.filters.Country
import ru.practicum.android.diploma.domain.models.filters.Industry
import ru.practicum.android.diploma.domain.models.filters.Region
import ru.practicum.android.diploma.util.Resource
import ru.practicum.android.diploma.util.ResponseType
import ru.practicum.android.diploma.util.toResponseType

class FiltersRepositoryImpl(private val networkClient: NetworkClient) : FiltersRepository {
    override fun getCountries(): Flow<Resource<List<Country>>> = flow {
        val response = networkClient.doRequest(CountriesRequest)
        when (response.resultCode.toResponseType()) {
            ResponseType.SEARCH_SUCCESS -> {
                val data = (response as CountriesResponseDto).countries
                    .filter { it.parentId == null }
                    .sortedBy { if (it.id == ResponseType.ID_OTHER_REGIONS.code.toString()) 1 else 0 }

                emit(Resource.Success(data.map { it.toDomain() }))
            }

            ResponseType.NO_CONNECTION -> emit(Resource.Error(ErrorType.NO_INTERNET))
            ResponseType.SERVER_ERROR -> emit(Resource.Error(ErrorType.SERVER_ERROR))
            else -> emit(Resource.Error(ErrorType.UNKNOWN))
        }
    }

    override fun getRegions(countryId: String): Flow<Resource<List<Region>>> = flow {
        val response = networkClient.doRequest(RegionsRequest(countryId))
        when (response.resultCode.toResponseType()) {
            ResponseType.SEARCH_SUCCESS -> {
                val data = (response as RegionsResponseDto).regions
                    .filter { it.areas.isEmpty() }
                    .sortedBy { it.name }
                emit(Resource.Success(data.map { it.toRegion() }))
            }

            ResponseType.NO_CONNECTION -> emit(Resource.Error(ErrorType.NO_INTERNET))
            ResponseType.SERVER_ERROR -> emit(Resource.Error(ErrorType.SERVER_ERROR))
            else -> emit(Resource.Error(ErrorType.UNKNOWN))
        }
    }

    override fun getIndustries(): Flow<Resource<List<Industry>>> = flow {
        val response = networkClient.doRequest(IndustryRequest)
        when (response.resultCode.toResponseType()) {
            ResponseType.SEARCH_SUCCESS -> {
                val data = (response as IndustryResponseDto).industries
                    .flatMap { it.industries }
                    .map { it.toDomain() }
                    .sortedBy { it.name }
                emit(Resource.Success(data))
            }

            ResponseType.NO_CONNECTION -> emit(Resource.Error(ErrorType.NO_INTERNET))
            ResponseType.SERVER_ERROR -> emit(Resource.Error(ErrorType.SERVER_ERROR))
            else -> emit(Resource.Error(ErrorType.UNKNOWN))
        }
    }
}
