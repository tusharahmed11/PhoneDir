package com.example.phonedir.data.di

import android.content.Context
import com.example.phonedir.MainRepository
import com.example.phonedir.api.AppApi
import com.example.phonedir.network.RetrofitHelper
import com.example.phonedir.utils.BaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BaseDatabase {
        return BaseDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideMainRepository(db: BaseDatabase) : MainRepository{
        val appApi =
            RetrofitHelper.createService(AppApi::class.java, "", true)

        return MainRepository(appApi,db.onUserDao)
    }
}