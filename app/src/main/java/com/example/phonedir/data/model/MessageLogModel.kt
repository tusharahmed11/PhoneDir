package com.example.phonedir.data.model

data class MessageLogModel(
    val phoneNumber: String,
    val contactName: String,
    val messageDate: String,
    val messageTime: String,
    val messageDateInLong: Long,
    val message: String
)
