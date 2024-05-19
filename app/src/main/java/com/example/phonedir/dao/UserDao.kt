package com.example.phonedir.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.phonedir.constants.DatabaseConstants.TBL_USERS
import com.example.phonedir.data.entities.UserEntity
import com.example.phonedir.utils.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao : BaseDao<UserEntity> {
    @Query("SELECT * FROM $TBL_USERS")
    fun getAllUser() : Flow<List<UserEntity>>
}