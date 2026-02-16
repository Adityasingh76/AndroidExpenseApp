package com.example.expensepilot.util

import android.content.Context
import com.example.expensepilot.data.ExpenseEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {
    fun exportCsv(context: Context, expenses: List<ExpenseEntity>): File {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val file = File(context.getExternalFilesDir(null), "expenses_export.csv")
        val content = buildString {
            appendLine("Date,Amount,Category,Note,UPI App,Payee,UPI ID")
            expenses.forEach {
                appendLine(
                    "${sdf.format(Date(it.timestamp))},${it.amount},${it.category},${it.note},${it.upiApp.orEmpty()},${it.upiPayee.orEmpty()},${it.upiId.orEmpty()}"
                )
            }
        }
        file.writeText(content)
        return file
    }
}
