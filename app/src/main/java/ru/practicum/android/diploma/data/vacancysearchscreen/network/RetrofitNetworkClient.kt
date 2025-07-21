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
                perPage = dto.perPage
            )
            response.apply { resultCode = REQUEST_SUCCESS }
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
            response.apply { resultCode = REQUEST_SUCCESS }
        } catch (e: retrofit2.HttpException) {
            Log.e("Repository", "Error getting details vacancies", e)
            createServerErrorResponse()
        }
    }

    private suspend fun handleCountriesRequest(): Response = withContext(Dispatchers.IO) {
        try {
            val response = countryService.getCountries()
            CountriesResponseDto(response).apply {
                resultCode = REQUEST_SUCCESS
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
                resultCode = REQUEST_SUCCESS
            }
        } catch (e: retrofit2.HttpException) {
            Log.e("Repository", "Error getting details vacancies", e)
            createServerErrorResponse()
        }
    }

    private suspend fun handleRegionsRequest(dto: RegionsRequest): Response = withContext(Dispatchers.IO) {
        try {
            val response = if (dto.countryId.isBlank()) {
                val countries = countryService.getCountries()
                val allCities = mutableListOf<AreasResponseDto>()

                countries.forEach { country ->
                    val regions = countryService.getRegions(country.id).areas
                    regions.forEach { region ->
                        allCities.addAll(region.areas)
                    }
                }

                AreaWithSubareasDto(id = "", name = "All cities", areas = allCities)
            } else {
                val regions = countryService.getRegions(dto.countryId).areas
                val cities = mutableListOf<AreasResponseDto>()

                regions.forEach { region ->
                    cities.addAll(region.areas)
                }

                AreaWithSubareasDto(id = dto.countryId, name = "Cities", areas = cities)
            }

            RegionsResponseDto(response.areas).apply {
                resultCode = REQUEST_SUCCESS
            }
        } catch (e: retrofit2.HttpException) {
            Log.e("Repository", "Error getting cities list", e)
            createServerErrorResponse()
        }
    }

    private fun createServerErrorResponse() = Response().apply { resultCode = SERVER_ERROR }
    private fun createFailedResponse() = Response().apply { resultCode = REQUEST_FAILED }
    private fun createNoConnectionResponse() = Response().apply { resultCode = NO_CONNECTION }

    companion object {
        private const val NO_CONNECTION = -1
        private const val REQUEST_SUCCESS = 2
        private const val REQUEST_FAILED = 1
        private const val SERVER_ERROR = 5
    }
}
