package com.example.phonedir.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.phonedir.constants.DatabaseConstants

@Entity(tableName = DatabaseConstants.TBL_USERS)
data class UserEntity(
    @PrimaryKey
    var id: Int = 0,
    var userId: Int,
    var name: String,
    var email: String,
    var userName: String,
    var phoneNumber: String,
    var accessToken: String,
    var firstTimeCALLStatus: Int,
    var firstTimeSMSStatus: Int
)