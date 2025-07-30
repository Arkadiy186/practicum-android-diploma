package ru.practicum.android.diploma.data.favouritevacancies.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.practicum.android.diploma.data.db.AppDatabase
import ru.practicum.android.diploma.data.db.converter.toData
import ru.practicum.android.diploma.data.db.converter.toDomain
import ru.practicum.android.diploma.data.db.entyties.FavouriteVacancy
import ru.practicum.android.diploma.domain.favouritevacancies.repository.FavouriteVacanciesDbRepository
import ru.practicum.android.diploma.domain.models.vacancies.Vacancy
import ru.practicum.android.diploma.domain.models.vacancydetails.VacancyDetails

class FavouriteVacanciesDbRepositoryImpl(private val appDataBase: AppDatabase) : FavouriteVacanciesDbRepository {
    override suspend fun insertVacancy(vacancy: VacancyDetails) {
        val currentVacancy = appDataBase.favouriteVacancyDao().getVacancyById(vacancy.id)
        if (currentVacancy == null) {
            appDataBase.favouriteVacancyDao().insertVacancy(vacancy.toData())
        }
    }

    override suspend fun deleteVacancy(vacancy: VacancyDetails) {
        val currentVacancy = appDataBase.favouriteVacancyDao().getVacancyById(vacancy.id)
        if (currentVacancy != null) {
            appDataBase.favouriteVacancyDao().deleteVacancy(vacancy.toData())
        }
    }

    override fun getFavouriteVacancies(): Flow<List<Vacancy>> {
        return appDataBase.favouriteVacancyDao().getFavouriteVacancies()
            .map { favouriteList ->
                favouriteList.map { favouriteVacancy ->
                    favouriteVacancy.toDomain()
                }
            }
    }

    override suspend fun getVacancyById(id: String): FavouriteVacancy? {
        return appDataBase.favouriteVacancyDao().getVacancyById(id)
    }
}
