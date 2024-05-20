package com.example.phonedir.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phonedir.data.Result
import com.example.phonedir.data.entities.UserEntity
import com.example.phonedir.data.model.LoginRequestModel
import com.example.phonedir.data.model.LoginResponseModel
import com.example.phonedir.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepository: MainRepository) : ViewModel() {

    fun insertUserData(userEntity: UserEntity) = viewModelScope.launch(IO) {
        mainRepository.insertUserData(userEntity)
    }

    fun deleteAllUserData() = viewModelScope.launch(IO) {
        mainRepository.deleteAllUserData()
    }

    fun getAllUserData() = mainRepository.getUserData()

    fun loginUser(
        loginRequestModel: LoginRequestModel
    ) = viewModelScope.launch { mainRepository.loginApiCall(loginRequestModel) }

    val userData: LiveData<Result<LoginResponseModel>>
        get() = mainRepository.userInfo



}