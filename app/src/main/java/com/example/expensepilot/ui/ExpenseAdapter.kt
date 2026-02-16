package com.example.expensepilot.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensepilot.data.ExpenseEntity
import com.example.expensepilot.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val expenses = mutableListOf<ExpenseEntity>()
    private val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    fun submitList(items: List<ExpenseEntity>) {
        expenses.clear()
        expenses.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun getItemCount(): Int = expenses.size

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExpenseEntity) {
            binding.title.text = "${item.category} • ₹${"%.2f".format(item.amount)}"
            binding.subtitle.text = "${sdf.format(Date(item.timestamp))} | ${item.note.ifBlank { "No note" }}"
            binding.extra.text = listOfNotNull(item.upiApp, item.upiPayee).joinToString(" • ").ifBlank { "Manual" }
        }
    }
}
