package com.example.phonedir.data.model

data class LoginResponseModel(
    val accessToken: String,
    val user: UserDataModel
)