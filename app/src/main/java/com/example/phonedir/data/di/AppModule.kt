package com.example.phonedir.data.di

import com.example.phonedir.MainRepository
import com.example.phonedir.api.AppApi
import com.example.phonedir.network.RetrofitHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideMainRepository() : MainRepository{
        val appApi =
            RetrofitHelper.createService(AppApi::class.java, "", true)
        return MainRepository(appApi)
    }
}