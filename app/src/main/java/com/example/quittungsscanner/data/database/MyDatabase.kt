package com.example.quittungsscanner.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class MyDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(vararg users: UserEntity)
    @Update
    fun updateUser(vararg users: UserEntity)
    @Delete
    fun deleteUser(vararg users: UserEntity)
    @Query("SELECT * FROM userentity")
    fun flowLoadAllUsers(): Flow<List<UserEntity>>
}

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val name: String,
    val age: Int,
    val authorized: Boolean
)
