package com.example.expensepilot.ui

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensepilot.data.AppDatabase
import com.example.expensepilot.data.ExpenseEntity
import com.example.expensepilot.data.ExpenseRepository
import com.example.expensepilot.databinding.ActivityMainBinding
import com.example.expensepilot.util.ExportUtils
import com.example.expensepilot.util.UpiDetails
import com.example.expensepilot.util.UpiUtils
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: ExpenseRepository
    private val adapter = ExpenseAdapter()
    private var upiDetails: UpiDetails? = null

    private val categories = mutableListOf(
        "Groceries", "Food", "Transport", "Rent", "Utilities", "Medical", "Shopping",
        "Entertainment", "Education", "Travel", "Bills", "Personal Care", "Gifts", "Others"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ExpenseRepository(AppDatabase.getInstance(this).expenseDao())
        setupUi()
        refreshExpenses()
    }

    private fun setupUi() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.categoryInput.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        )

        binding.scanQrButton.setOnClickListener { startQrScanner() }
        binding.payButton.setOnClickListener { initiateUpiFlow() }
        binding.addManualExpenseButton.setOnClickListener { saveManualExpense() }

        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == com.example.expensepilot.R.id.action_export) {
                exportCsv()
                true
            } else {
                false
            }
        }
    }

    private fun startQrScanner() {
        IntentIntegrator(this).apply {
            setPrompt("Scan merchant UPI QR")
            setBeepEnabled(true)
            setOrientationLocked(true)
            initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                toast("QR scan cancelled")
                return
            }
            upiDetails = UpiUtils.parseUpiQr(result.contents)
            if (upiDetails?.upiId == null) {
                toast("Could not parse UPI details from QR")
            } else {
                toast("UPI ID detected: ${upiDetails?.upiId}")
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initiateUpiFlow() {
        val details = upiDetails
        val amount = binding.amountInput.text?.toString()?.toDoubleOrNull()
        val category = binding.categoryInput.text?.toString()?.trim().orEmpty()
        val note = binding.noteInput.text?.toString()?.trim().orEmpty()

        if (details?.upiId.isNullOrBlank()) {
            toast("Scan a UPI QR first")
            return
        }
        if (amount == null || amount <= 0.0) {
            toast("Enter valid amount")
            return
        }
        if (category.isBlank()) {
            toast("Enter/select category")
            return
        }
        if (!categories.contains(category)) {
            categories.add(category)
            binding.categoryInput.setAdapter(
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
            )
        }

        val upiUri = UpiUtils.buildUpiUri(
            details!!.upiId!!,
            details.payeeName ?: "Merchant",
            amount,
            note,
            category
        )

        val paymentIntent = Intent(Intent.ACTION_VIEW, upiUri)
        val apps = packageManager.queryIntentActivities(paymentIntent, 0)
        if (apps.isEmpty()) {
            toast("No UPI apps installed")
            return
        }
        showAppPicker(apps, paymentIntent, amount, category, note, details)
    }

    private fun showAppPicker(
        apps: List<ResolveInfo>,
        paymentIntent: Intent,
        amount: Double,
        category: String,
        note: String,
        details: UpiDetails
    ) {
        val appNames = apps.map { it.loadLabel(packageManager).toString() }
        AlertDialog.Builder(this)
            .setTitle("Choose UPI app")
            .setItems(appNames.toTypedArray()) { _, which ->
                val chosen = apps[which]
                val appName = appNames[which]
                paymentIntent.setPackage(chosen.activityInfo.packageName)
                startActivity(paymentIntent)
                saveExpense(amount, category, note, appName, details)
            }
            .show()
    }

    private fun saveManualExpense() {
        val amount = binding.amountInput.text?.toString()?.toDoubleOrNull()
        val category = binding.categoryInput.text?.toString()?.trim().orEmpty()
        val note = binding.noteInput.text?.toString()?.trim().orEmpty()

        if (amount == null || amount <= 0) {
            toast("Enter valid amount")
            return
        }
        if (category.isBlank()) {
            toast("Enter category")
            return
        }
        if (!categories.contains(category)) {
            categories.add(category)
            binding.categoryInput.setAdapter(
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
            )
        }
        saveExpense(amount, category, note, null, upiDetails)
    }

    private fun saveExpense(
        amount: Double,
        category: String,
        note: String,
        appName: String?,
        upiDetails: UpiDetails?
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            repository.save(
                ExpenseEntity(
                    amount = amount,
                    category = category,
                    note = note,
                    upiApp = appName,
                    upiPayee = upiDetails?.payeeName,
                    upiId = upiDetails?.upiId
                )
            )
            withContext(Dispatchers.Main) {
                toast("Expense saved")
                refreshExpenses()
            }
        }
    }

    private fun refreshExpenses() {
        lifecycleScope.launch(Dispatchers.IO) {
            val all = repository.allExpenses()
            val now = Calendar.getInstance()
            now.set(Calendar.DAY_OF_MONTH, 1)
            now.set(Calendar.HOUR_OF_DAY, 0)
            now.set(Calendar.MINUTE, 0)
            now.set(Calendar.SECOND, 0)
            val start = now.timeInMillis
            val end = System.currentTimeMillis()
            val monthly = repository.monthlyExpenses(start, end)
            val total = monthly.sumOf { it.amount }
            val grouped = monthly.groupBy { it.category }
                .mapValues { (_, items) -> items.sumOf { it.amount } }
                .entries
                .sortedByDescending { it.value }
                .joinToString("\n") { "${it.key}: ₹${"%.2f".format(it.value)}" }

            withContext(Dispatchers.Main) {
                adapter.submitList(all)
                binding.summaryText.text = "This month: ₹${"%.2f".format(total)}"
                binding.categoryBreakdown.text = if (grouped.isBlank()) "No expenses yet" else grouped
            }
        }
    }

    private fun exportCsv() {
        lifecycleScope.launch(Dispatchers.IO) {
            val all = repository.allExpenses()
            val file = ExportUtils.exportCsv(this@MainActivity, all)
            withContext(Dispatchers.Main) {
                shareFile(file)
            }
        }
    }

    private fun shareFile(file: File) {
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share expenses CSV"))
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
