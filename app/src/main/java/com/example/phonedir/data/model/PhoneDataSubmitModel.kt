package com.example.phonedir.data.model

data class PhoneDataSubmitModel(
    val direction: String,
    val duration: String,
    val local_number: String,
    val remote_number: String,
    val status: String,
    val type: String
)