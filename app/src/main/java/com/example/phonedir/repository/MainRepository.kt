package com.example.phonedir.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.phonedir.api.AppApi
import com.example.phonedir.dao.UserDao
import com.example.phonedir.data.Result
import com.example.phonedir.data.entities.UserEntity
import com.example.phonedir.data.model.CallLogModel
import com.example.phonedir.data.model.LoginRequestModel
import com.example.phonedir.data.model.LoginResponseModel
import com.example.phonedir.data.model.PhoneDataSubmitModel
import com.example.phonedir.utils.SingleLiveEvent
import com.google.gson.JsonParser
import javax.inject.Singleton

@Singleton
class MainRepository(
    private val appApi: AppApi,
    private val userDao: UserDao
) {
    private val userInfoLiveData = SingleLiveEvent<Result<LoginResponseModel>>()
    private val submitLiveData = SingleLiveEvent<Result<Void>>()

    val userInfo: LiveData<Result<LoginResponseModel>>
        get() = userInfoLiveData

    val submitData: LiveData<Result<Void>>
        get() = submitLiveData

    suspend fun loginApiCall(loginRequestModel: LoginRequestModel){
        userInfoLiveData.postValue(Result.Loading("Loading"))
        try {
            val response = appApi.login(loginData = loginRequestModel)
            if (response.isSuccessful) {
                val loginResponse = response.body()!! // Unwrap successfully parsed body
                // Handle successful login (e.g., store user data)
                userInfoLiveData.postValue(Result.Success(loginResponse)) // Update UI with success
            } else {
                val errorBody = response.errorBody() ?: return // Handle empty error body
                val errorResponse = try {
                    JsonParser.parseString(errorBody.string()) // Use JsonParser for flexibility
                } catch (e: Exception) {
                    userInfoLiveData.postValue(Result.Error(Exception("Something went wrong")))
                    return // Return to avoid further processing
                }
                val message: String = try {
                    errorResponse.asJsonObject.get("message")?.asString ?: "Unknown error"
                } catch (e: IllegalStateException) {
                    "Error parsing error response: $e" // Handle potential exceptions
                }
                userInfoLiveData.postValue(Result.Error(Exception(message))) // Update UI with error message
            }
        } catch (e: Exception) {
            userInfoLiveData.postValue(Result.Error(e))
        }
    }

    suspend fun submitApiCall(phoneDataSubmitModel: List<CallLogModel>, authToken: String){
        submitLiveData.postValue(Result.Loading("Loading"))
        try {
            val response = appApi.submitPhoneData(authorization = authToken, submitDataList = phoneDataSubmitModel)
            submitLiveData.postValue(Result.Success(response))
   /*         if (response.isSuccessful) {
                val loginResponse = response.body()!! // Unwrap successfully parsed body
                // Handle successful login (e.g., store user data)
                userInfoLiveData.postValue(Result.Success(loginResponse)) // Update UI with success
            } else {
                val errorBody = response.errorBody() ?: return // Handle empty error body
                val errorResponse = try {
                    JsonParser.parseString(errorBody.string()) // Use JsonParser for flexibility
                } catch (e: Exception) {
                    userInfoLiveData.postValue(Result.Error(Exception("Something went wrong")))
                    return // Return to avoid further processing
                }
                val message: String = try {
                    errorResponse.asJsonObject.get("message")?.asString ?: "Unknown error"
                } catch (e: IllegalStateException) {
                    "Error parsing error response: $e" // Handle potential exceptions
                }
                userInfoLiveData.postValue(Result.Error(Exception(message))) // Update UI with error message
            }*/
        } catch (e: Exception) {
            submitLiveData.postValue(Result.Error(e))
        }
    }

    suspend fun insertUserData(userEntity: UserEntity){
        userDao.insert(userEntity)
    }

    suspend fun updateUserData(userEntity: UserEntity){
        userDao.update(userEntity)
    }

    suspend fun deleteAllUserData(){
        userDao.deleteAllUserData()
    }

    fun getUserData() = userDao.getAllUser()

}