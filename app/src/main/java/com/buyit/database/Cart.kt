package com.buyit.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "cart")
@TypeConverters(Converters::class)
data class Cart(
    @PrimaryKey(autoGenerate = true) val orderId: Long = 0,
    var itemName: String? = "item",
    var itemPrice: Double? = 0.00,
    var itemQuantity: Int? = 0,
    var date: Long? = 0,
    var history: Boolean = false
)
