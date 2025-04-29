package com.example.quittungsscanner.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyDatabase {
        val ioDispatcherExecutor = Dispatchers.IO.asExecutor()
        return Room.databaseBuilder(
            context,
            MyDatabase::class.java,
            "my-database"
        )
            .setQueryExecutor(ioDispatcherExecutor)
            .setTransactionExecutor(ioDispatcherExecutor)
            .build()
    }

    @Provides
    fun provideUserDao(database: MyDatabase): UserDao {
        return database.userDao()
    }
}