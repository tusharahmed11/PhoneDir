package com.example.phonedir.data.model

data class LoginRequestModel(
    val email: String,
    val password: String,
    val phoneNumber: String
)