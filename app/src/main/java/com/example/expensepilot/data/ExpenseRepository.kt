package com.example.expensepilot.data

class ExpenseRepository(private val dao: ExpenseDao) {
    suspend fun save(expense: ExpenseEntity) = dao.insert(expense)

    suspend fun allExpenses(): List<ExpenseEntity> = dao.getAll()

    suspend fun monthlyExpenses(start: Long, end: Long): List<ExpenseEntity> = dao.getBetween(start, end)
}
