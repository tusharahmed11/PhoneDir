package com.example.phonedir.data.model

data class CallLogModel(
    val id: String,
    val phoneNumber: String,
    val contactName: String,
    val callType: String,
    val callDate: String,
    val callTime: String,
    val callDateInLong: Long,
    val callDuration: String,
)
