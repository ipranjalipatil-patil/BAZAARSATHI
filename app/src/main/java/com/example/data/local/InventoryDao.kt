package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InventoryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory ORDER BY productName ASC")
    fun getAllInventoryFlow(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory ORDER BY productName ASC")
    suspend fun getAllInventorySync(): List<InventoryItemEntity>

    @Query("SELECT * FROM inventory WHERE id = :id LIMIT 1")
    suspend fun getInventoryById(id: Long): InventoryItemEntity?

    @Query("SELECT * FROM inventory WHERE productName LIKE '%' || :query || '%'")
    fun searchInventory(query: String): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Query("UPDATE inventory SET quantity = MAX(0.0, quantity + :delta), updatedAt = :time WHERE id = :id")
    suspend fun adjustQuantity(id: Long, delta: Double, time: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    @Query("DELETE FROM inventory WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM inventory")
    suspend fun clearAll()
}
