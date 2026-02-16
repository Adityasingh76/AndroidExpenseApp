package com.example.expensepilot.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,
    val note: String,
    val upiApp: String?,
    val upiPayee: String?,
    val upiId: String?,
    val timestamp: Long = System.currentTimeMillis()
)
