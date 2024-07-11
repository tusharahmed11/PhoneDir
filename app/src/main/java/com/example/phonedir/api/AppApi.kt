package com.example.phonedir.api

import com.example.phonedir.constants.APIConstants
import com.example.phonedir.data.model.CallLogModel
import com.example.phonedir.data.model.CallSubmitModel
import com.example.phonedir.data.model.LoginRequestModel
import com.example.phonedir.data.model.LoginResponseModel
import com.example.phonedir.data.model.PhoneDataSubmitModel
import com.example.phonedir.data.model.SubmitDataList
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AppApi {

    @POST(APIConstants.SIGN_IN)
    suspend fun login(
        @Header("Content-Type") contentType: String = "application/json",
        @Body loginData: LoginRequestModel,
    ): Response<LoginResponseModel>

    @POST(APIConstants.SUBMIT_PHONE_DATA)
    suspend fun submitPhoneData(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Authorization") authorization: String,
        @Body submitDataList: CallSubmitModel,
    ): Void

}