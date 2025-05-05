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
    fun provideDatabase(@ApplicationContext context: Context): BudgetTrackerDatabase {
        val ioDispatcherExecutor = Dispatchers.IO.asExecutor()
        return Room.databaseBuilder(
            context,
            BudgetTrackerDatabase::class.java,
            "budget_tracker_database"
        )
            .setQueryExecutor(ioDispatcherExecutor)
            .setTransactionExecutor(ioDispatcherExecutor)
            .build()
    }

    @Provides
    fun provideProductDao(database: BudgetTrackerDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideReceiptDao(database: BudgetTrackerDatabase): ReceiptDao {
        return database.receiptDao()
    }
}