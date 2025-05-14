package com.example.quittungsscanner.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import java.util.Date


@Database(entities = [Product::class, Receipt::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BudgetTrackerDatabase: RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun productDao(): ProductDao
}

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt): Long

    @Transaction
    @Query("SELECT * FROM receipts")
    suspend fun getReceiptsWithProducts(): List<ReceiptWithProducts>

    @Query("DELETE FROM receipts WHERE id = :receiptId")
    suspend fun deleteReceiptById(receiptId: Long)

    @Update
    suspend fun updateReceipt(receipt: Receipt)
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product):Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(vararg products: Product): List<Long>

    @Query("SELECT * FROM products WHERE receiptId = :receiptId")
    suspend fun getProductsByReceipt(receiptId: Long): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Update
    suspend fun updateProduct(product: Product)
}

@Entity(
    tableName = "receipts"
)
data class Receipt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateCreated: Date,
    val storeName: String
)

@Entity(
    tableName = "products",
    foreignKeys = [ForeignKey(
        entity = Receipt::class,
        parentColumns = ["id"],
        childColumns = ["receiptId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["receiptId"])]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val receiptId: Long
)

data class ReceiptWithProducts(
    @Embedded
    val receipt: Receipt,
    @Relation(
        parentColumn = "id",
        entityColumn = "receiptId"
    )
    val products: List<Product>
)

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}