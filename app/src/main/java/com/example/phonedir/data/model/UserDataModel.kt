package com.example.phonedir.data.model

data class UserDataModel(
    val contact: String,
    val email: String,
    val id: Int,
    val name: String,
    val password: String,
    val status: Boolean,
    val username: String?,
    val verified: Boolean
)