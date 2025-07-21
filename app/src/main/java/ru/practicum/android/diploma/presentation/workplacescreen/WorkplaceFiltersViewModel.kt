package ru.practicum.android.diploma.presentation.workplacescreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.practicum.android.diploma.domain.filters.repository.FiltersParametersInteractor
import ru.practicum.android.diploma.domain.models.filters.FilterParameters

class WorkplaceFiltersViewModel(private val interactor: FiltersParametersInteractor) : ViewModel() {

    private val _tempCountry = MutableLiveData<String?>()
    val getTempCountry: LiveData<String?> = _tempCountry

    private val _tempRegion = MutableLiveData<String?>()
    val getTempRegion: LiveData<String?> = _tempRegion

    private val _paramsSelectedState = MutableLiveData<FilterParameters>()
    val getSelectedParams: LiveData<FilterParameters> = _paramsSelectedState

    private var tempSelectedCountry: String? = null
    private var tempSelectedRegion: String? = null

    fun setTempCountrySelection(name: String?) {
        tempSelectedCountry = name
        _tempCountry.postValue(name)
    }
    // для назначения временного значения
    fun setTempRegionSelection(name: String?) {
        tempSelectedRegion = name
        _tempRegion.postValue(name)
    }
    // метод на сохранение параметров
    fun saveSelection() {
        interactor.selectCountry(tempSelectedCountry)
    // interactor.selectRegion(tempSelectedRegion)
        _tempCountry.postValue(null)
    }

    fun loadParameters() {
        _paramsSelectedState.value = interactor.getFiltersParameters()
    }

    fun clearCountry() {
        interactor.selectCountry(null)
        _tempCountry.postValue(null)
        loadParameters()
    }

    fun clearRegion() {
        interactor.selectRegion(null)
        _tempRegion.postValue(null)
        loadParameters()
    }
}
