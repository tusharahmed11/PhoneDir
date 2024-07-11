package com.example.phonedir.data.model

data class CallSubmitModel(
    val ownNumber: String?,
    val type: String,
    val callList: List<CallLogModel>
)
