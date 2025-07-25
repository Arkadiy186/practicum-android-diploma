package ru.practicum.android.diploma.presentation.models.vacancies

import ru.practicum.android.diploma.domain.models.salary.Salary

data class VacancyUiModel(
    val nameVacancy: String,
    val alternateUrl: String,
    val id: String,
    val employerName: String?,
    val logo: String?,
    val salary: Salary,
    val city: String?,
    val isFavourite: Boolean = false,
    val timestamp: Long? = null
)
