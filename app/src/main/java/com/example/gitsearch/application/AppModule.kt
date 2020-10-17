package com.example.gitsearch.application

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent


@Module
@InstallIn(ApplicationComponent::class)
object AppModule {
    @Module
    @InstallIn(ApplicationComponent::class)
    interface Bindings {
        @Binds
        fun context(app: Application): Context
    }



    @Provides
    fun preferences(app: Application): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(
        app
    )



}