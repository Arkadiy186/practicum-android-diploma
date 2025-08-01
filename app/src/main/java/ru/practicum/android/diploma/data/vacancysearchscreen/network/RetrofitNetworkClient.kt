package ru.practicum.android.diploma.data.vacancysearchscreen.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.practicum.android.diploma.data.models.areas.AreaWithSubareasDto
import ru.practicum.android.diploma.data.models.areas.AreasApi
import ru.practicum.android.diploma.data.models.areas.AreasResponseDto
import ru.practicum.android.diploma.data.models.areas.country.CountriesRequest
import ru.practicum.android.diploma.data.models.areas.country.CountriesResponseDto
import ru.practicum.android.diploma.data.models.areas.regions.RegionsRequest
import ru.practicum.android.diploma.data.models.areas.regions.RegionsResponseDto
import ru.practicum.android.diploma.data.models.industries.IndustriesApi
import ru.practicum.android.diploma.data.models.industries.remote.IndustryRequest
import ru.practicum.android.diploma.data.models.industries.remote.IndustryResponseDto
import ru.practicum.android.diploma.data.models.vacancies.Response
import ru.practicum.android.diploma.data.models.vacancies.VacanciesApi
import ru.practicum.android.diploma.data.models.vacancies.VacanciesRequest
import ru.practicum.android.diploma.data.models.vacancydetails.VacancyDetailsApi
import ru.practicum.android.diploma.data.models.vacancydetails.VacancyDetailsRequest
import ru.practicum.android.diploma.util.NetworkHelper.isConnected
import ru.practicum.android.diploma.util.ResponseType

class RetrofitNetworkClient(
    private val service: VacanciesApi,
    private val vacancyService: VacancyDetailsApi,
    private val countryService: AreasApi,
    private val industriesApi: IndustriesApi,
    private val context: Context
) : NetworkClient {
    override suspend fun doRequest(dto: Any): Response {
        return if (!isConnected(context)) {
            createNoConnectionResponse()
        } else {
            handleRequest(dto)
        }
    }

    private suspend fun handleRequest(dto: Any): Response {
        return when (dto) {
            is VacanciesRequest -> handleVacancyRequest(dto)
            is VacancyDetailsRequest -> handleVacancyDetailsRequest(dto)
            is CountriesRequest -> handleCountriesRequest()
            is IndustryRequest -> handleIndustriesRequest()
            is RegionsRequest -> handleRegionsRequest(dto)
            else -> createFailedResponse()
        }
    }

    private suspend fun handleVacancyRequest(dto: VacanciesRequest): Response = withContext(Dispatchers.IO) {
        try {
            val response = service.getVacancies(
                text = dto.text,
                page = dto.page,
                perPage = dto.perPage,
                filter = dto.filter,
            )
            response.apply { resultCode = ResponseType.SEARCH_SUCCESS.code }
        } catch (e: retrofit2.HttpException) {
            Log.e("Repository", "Error getting vacancies", e)
            createServerErrorResponse()
        }
    }

    private suspend fun handleVacancyDetailsRequest(
        dto: VacancyDetailsRequest
    ): Response = withContext(Dispatchers.IO) {
        try {
            val response = vacancyService.getVacancyDetails(id = dto.id)
            response.apply { resultCode = ResponseType.SEARCH_SUCCESS.code }
        } catch (e: retrofit2.HttpException) {
            Log.e("Repository", "Error getting details vacancies", e)
            createServerErrorResponse()
        }
    }

    private suspend fun handleCountriesRequest(): Response = withContext(Dispatchers.IO) {
        try {
            val response = countryService.getCountries()
            CountriesResponseDto(response).apply {
                resultCode = ResponseType.SEARCH_SUCCESS.code
            }
        } catch (e: retrofit2.HttpException) {
            Log.e("Repository", "Error getting countries list", e)
            createServerErrorResponse()
        }
    }

    private suspend fun handleIndustriesRequest(): Response = withContext(Dispatchers.IO) {
        try {
            val response = industriesApi.getIndustries()
            IndustryResponseDto(response).apply {
                resultCode = ResponseType.SEARCH_SUCCESS.code
            }
        } catch (e: retrofit2.HttpException) {
            Log.e("Repository", "Error getting details vacancies", e)
            createServerErrorResponse()
        }
    }

    private suspend fun handleRegionsRequest(dto: RegionsRequest): Response =
        withContext(Dispatchers.IO) {
            try {
                val response = if (dto.countryId.isBlank()) {
                    getAllCitiesResponse()
                } else {
                    getCitiesByCountryResponse(dto.countryId)
                }

                RegionsResponseDto(response.areas).apply {
                    resultCode = ResponseType.SEARCH_SUCCESS.code
                }
            } catch (e: retrofit2.HttpException) {
                Log.e("Repository", "Error getting cities list", e)
                createServerErrorResponse()
            }
        }

    private suspend fun getAllCitiesResponse(): AreaWithSubareasDto {
        val countries = countryService.getCountries()
        val allCities = countries.flatMap { country ->
            processRegions(countryService.getRegions(country.id).areas, country.name)
        }
        return AreaWithSubareasDto(id = "", name = "All cities", areas = allCities)
    }

    private suspend fun getCitiesByCountryResponse(countryId: String): AreaWithSubareasDto {
        val country = countryService.getCountries().firstOrNull { it.id == countryId }
        val countryName = country?.name ?: ""
        val regions = countryService.getRegions(countryId).areas
        val resultAreas = processRegions(regions, countryName)
        return AreaWithSubareasDto(id = countryId, name = "Cities", areas = resultAreas)
    }

    private fun processRegions(
        regions: List<AreasResponseDto>,
        countryName: String
    ): List<AreasResponseDto> {
        return regions.flatMap { region ->
            if (region.areas.isNotEmpty()) {
                region.areas.map { city -> city.copy(countryName = countryName) }
            } else {
                listOf(region.copy(countryName = countryName))
            }
        }
    }

    private fun createServerErrorResponse() = Response().apply { resultCode = ResponseType.SERVER_ERROR.code }
    private fun createFailedResponse() = Response().apply { resultCode = ResponseType.REQUEST_FAILED.code }
    private fun createNoConnectionResponse() = Response().apply { resultCode = ResponseType.NO_CONNECTION.code }
}
