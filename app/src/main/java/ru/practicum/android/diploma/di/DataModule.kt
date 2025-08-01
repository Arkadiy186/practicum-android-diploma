package ru.practicum.android.diploma.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.practicum.android.diploma.data.db.AppDatabase
import ru.practicum.android.diploma.data.models.areas.AreasApi
import ru.practicum.android.diploma.data.models.industries.IndustriesApi
import ru.practicum.android.diploma.data.models.vacancies.VacanciesApi
import ru.practicum.android.diploma.data.models.vacancydetails.VacancyDetailsApi
import ru.practicum.android.diploma.data.vacancysearchscreen.network.NetworkClient
import ru.practicum.android.diploma.data.vacancysearchscreen.network.RetrofitNetworkClient

const val FILTER_PARAMETERS_PREFERENCES = "filter_parameters"

val dataModule = module {
    // retrofit
    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("https://api.hh.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single(named("filter_parameters_prefs")) {
        androidContext()
            .getSharedPreferences(FILTER_PARAMETERS_PREFERENCES, Context.MODE_PRIVATE)
    }

    factory { Gson() }

    single<NetworkClient> {
        RetrofitNetworkClient(get(), get(), get(), get(), get())
    }

    // room
    single<AppDatabase> {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "AppDatabase.db")
            .fallbackToDestructiveMigration(false)
            .build()
    }

    // vacanciesApi
    single<VacanciesApi> {
        get<Retrofit>().create(VacanciesApi::class.java)
    }

    // vacancyDetailsApi
    single<VacancyDetailsApi> {
        get<Retrofit>().create(VacancyDetailsApi::class.java)
    }

    // areasApi
    single<AreasApi> {
        get<Retrofit>().create(AreasApi::class.java)
    }

    // industriesApi
    single<IndustriesApi> {
        get<Retrofit>().create(IndustriesApi::class.java)
    }
}
