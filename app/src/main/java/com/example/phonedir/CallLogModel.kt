package com.example.phonedir

data class CallLogModel(
    val phoneNumber: String,
    val contactName: String,
    val callType: String,
    val callDate: String,
    val callTime: String,
    val callDuration: String,
)
