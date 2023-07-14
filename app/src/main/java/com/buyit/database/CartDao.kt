package com.buyit.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart")
    fun getAllOrders(): Flow<List<Cart>>

    @Query("SELECT * FROM cart WHERE orderId IN (:userIds)")
    fun loadAllByIds(userIds: LongArray): Flow<List<Cart>>

    @Query("SELECT * FROM cart WHERE orderId LIKE :orderId LIMIT 1")
    fun findById(orderId: Long): Flow<Cart>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrders(vararg cart: Cart)

    @Update
    suspend fun updateOrders(vararg cart: Cart)

    @Query("DELETE FROM cart WHERE orderId = :key")
    suspend fun deleteOrder(key: Long)

    @Query("DELETE FROM cart")
    suspend fun deleteAll()
}