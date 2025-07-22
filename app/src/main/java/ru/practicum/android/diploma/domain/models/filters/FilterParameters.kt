package ru.practicum.android.diploma.domain.models.filters

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilterParameters(
    val countryName: String? = null,
    val countryId: String? = null,
    val regionName: String? = null,
    val industryId: String? = null,
    val industryName: String? = null,
    val salary: String? = null,
    val checkboxWithoutSalary: Boolean? = null,
) : Parcelable
