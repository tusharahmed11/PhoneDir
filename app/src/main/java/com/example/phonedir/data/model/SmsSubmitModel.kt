package com.example.phonedir.data.model

data class SmsSubmitModel(
    val ownNumber: String?,
    val type: String,
    val callList: List<MessageLogModel>
)
