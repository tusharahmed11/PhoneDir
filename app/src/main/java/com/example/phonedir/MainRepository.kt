package com.example.phonedir

import com.example.phonedir.api.AppApi
import com.example.phonedir.dao.UserDao
import javax.inject.Singleton

@Singleton
class MainRepository(
    private val appApi: AppApi,
    private val userDao: UserDao
) {
}