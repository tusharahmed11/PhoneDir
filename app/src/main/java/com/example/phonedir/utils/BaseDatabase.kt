package com.example.phonedir.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.phonedir.constants.DatabaseConstants
import com.example.phonedir.dao.UserDao
import com.example.phonedir.data.entities.UserEntity

@Database(
    entities = [
        UserEntity::class
    ],
    version = DatabaseConstants.DATABASE_VERSION,
)
abstract class BaseDatabase : RoomDatabase() {
    abstract val onUserDao: UserDao

    companion object {
        private var INSTANCE: BaseDatabase? = null

        fun getDatabase(ctx: Context): BaseDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        ctx, BaseDatabase::class.java, DatabaseConstants.DATABASE_NAME
                    ).build()
                }
            }

            return INSTANCE!!
        }
    }
}