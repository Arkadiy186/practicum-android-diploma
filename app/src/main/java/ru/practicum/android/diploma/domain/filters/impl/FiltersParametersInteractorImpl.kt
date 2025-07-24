package ru.practicum.android.diploma.domain.filters.impl

import ru.practicum.android.diploma.domain.filters.repository.FilterParametersRepository
import ru.practicum.android.diploma.domain.filters.repository.FiltersParametersInteractor
import ru.practicum.android.diploma.domain.models.filters.FilterParameters

class FiltersParametersInteractorImpl(
    private val repository: FilterParametersRepository
) : FiltersParametersInteractor {

    override fun selectCountry(countryName: String?, countryId: String?) {
        repository.selectCountry(countryName, countryId)
    }

    override fun getSelectedCountryId(): String? {
        return repository.getFiltersParameters().countryId
    }

    override fun selectRegion(regionName: String?, regionId: String?, countryName: String?) {
        val currentParams = repository.getFiltersParameters()

        if (!countryName.isNullOrBlank() && currentParams.countryName.isNullOrBlank()) {
            repository.selectCountry(countryName, null)
        }

        repository.selectRegion(regionName, regionId)
    }

    override fun selectIndustry(industryId: String?, industryName: String?) {
        repository.selectIndustry(industryId, industryName)
    }

    override fun defineSalary(value: String?) {
        repository.defineSalary(value)
    }

    override fun toggleWithoutSalary(enabled: Boolean) {
        repository.toggleWithoutSalary(enabled)
    }

    override fun getFiltersParameters(): FilterParameters {
        return repository.getFiltersParameters()
    }

    override fun clearFilters() {
        repository.clearFilters()
    }

    override fun hasActiveFilters(): Boolean {
        val filters = repository.getFiltersParameters()
        return !filters.countryName.isNullOrBlank() ||
            !filters.regionName.isNullOrBlank() ||
            !filters.industryName.isNullOrBlank() ||
            !filters.salary.isNullOrBlank() ||
            filters.checkboxWithoutSalary == true
    }
}
