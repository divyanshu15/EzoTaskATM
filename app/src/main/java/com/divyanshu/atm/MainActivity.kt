package com.divyanshu.atm

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import android.content.SharedPreferences
import java.lang.Integer.min

class MainActivity : AppCompatActivity() {
    private lateinit var transactionRecordsTextView: TextView
    private var transactionRecords = ""
    private lateinit var dialog: Dialog

    private lateinit var count2000: TextView
    private lateinit var count500: TextView
    private lateinit var count200: TextView
    private lateinit var count100: TextView
    private lateinit var countTotal: TextView

    private lateinit var depositButton: Button
    private lateinit var withdrawButton: Button

    private lateinit var amount2000: EditText
    private lateinit var amount500: EditText
    private lateinit var amount200: EditText
    private lateinit var amount100: EditText
    private lateinit var withdrawAmount: EditText

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        transactionRecordsTextView = findViewById(R.id.transactionRecords)

        count2000 = findViewById(R.id.count2000)
        count500 = findViewById(R.id.count500)
        count200 = findViewById(R.id.count200)
        count100 = findViewById(R.id.count100)
        countTotal = findViewById(R.id.counttotal)

        depositButton = findViewById(R.id.depositButton)
        withdrawButton = findViewById(R.id.withdrawButton)

        sharedPreferences = getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)

        depositButton.setOnClickListener {
            openCustomDialog()
        }

        withdrawButton.setOnClickListener {
            openWithdrawDialog()
        }

        restoreCountsFromSharedPreferences()
        updateTotalCount()
        restoreTransactionRecordsFromSharedPreferences()
    }

    private fun openCustomDialog() {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.deposit_dialog)

        val saveButton: Button = dialog.findViewById(R.id.btnSave)
        val cancelButton: Button = dialog.findViewById(R.id.btnCancel)
        amount2000 = dialog.findViewById(R.id.amount2000)
        amount500 = dialog.findViewById(R.id.amount500)
        amount200 = dialog.findViewById(R.id.amount200)
        amount100 = dialog.findViewById(R.id.amount100)

        saveButton.setOnClickListener {
            updateCounts()
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openWithdrawDialog() {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.withdraw_dialog)

        val withdrawButton: Button = dialog.findViewById(R.id.btnWithdraw)
        val cancelTransactionButton: Button = dialog.findViewById(R.id.btnCancelTransaction)
        withdrawAmount = dialog.findViewById(R.id.etWithdrawAmount)

        withdrawButton.setOnClickListener {
            withdrawAmount()
        }

        cancelTransactionButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateCounts() {
        val count2000Value = count2000.text.toString().toIntOrNull() ?: 0
        val count500Value = count500.text.toString().toIntOrNull() ?: 0
        val count200Value = count200.text.toString().toIntOrNull() ?: 0
        val count100Value = count100.text.toString().toIntOrNull() ?: 0

        val amount2000Value = amount2000.text.toString().toIntOrNull() ?: 0
        val amount500Value = amount500.text.toString().toIntOrNull() ?: 0
        val amount200Value = amount200.text.toString().toIntOrNull() ?: 0
        val amount100Value = amount100.text.toString().toIntOrNull() ?: 0

        count2000.text = (count2000Value + amount2000Value).toString()
        count500.text = (count500Value + amount500Value).toString()
        count200.text = (count200Value + amount200Value).toString()
        count100.text = (count100Value + amount100Value).toString()

        updateTotalCount()
        recordTransaction("Deposit: 2000 x $amount2000Value, 500 x $amount500Value, 200 x $amount200Value, 100 x $amount100Value")
    }

    private fun updateTotalCount() {
        val count2000Value = count2000.text.toString().toIntOrNull() ?: 0
        val count500Value = count500.text.toString().toIntOrNull() ?: 0
        val count200Value = count200.text.toString().toIntOrNull() ?: 0
        val count100Value = count100.text.toString().toIntOrNull() ?: 0

        val total =
            (count2000Value * 2000) + (count500Value * 500) + (count200Value * 200) + (count100Value * 100)
        countTotal.text = total.toString()
    }

    private fun withdrawAmount() {
        val withdrawAmountValue = withdrawAmount.text.toString().toInt()

        val count2000Value = count2000.text.toString().toInt()
        val count500Value = count500.text.toString().toInt()
        val count200Value = count200.text.toString().toInt()
        val count100Value = count100.text.toString().toInt()

        val availableBalance =
            (count2000Value * 2000) + (count500Value * 500) + (count200Value * 200) + (count100Value * 100)

        if (withdrawAmountValue <= availableBalance) {
            val remaining2000 = min(withdrawAmountValue / 2000, count2000Value)
            val remainingAmountAfter2000 = withdrawAmountValue - (remaining2000 * 2000)

            val remaining500 = min(remainingAmountAfter2000 / 500, count500Value)
            val remainingAmountAfter500 = remainingAmountAfter2000 - (remaining500 * 500)

            val remaining200 = min(remainingAmountAfter500 / 200, count200Value)
            val remainingAmountAfter200 = remainingAmountAfter500 - (remaining200 * 200)

            val remaining100 = min(remainingAmountAfter200 / 100, count100Value)

            val totalWithdrawnAmount =
                (remaining2000 * 2000) + (remaining500 * 500) + (remaining200 * 200) + (remaining100 * 100)

            if (totalWithdrawnAmount == withdrawAmountValue) {
                count2000.text = (count2000Value - remaining2000).toString()
                count500.text = (count500Value - remaining500).toString()
                count200.text = (count200Value - remaining200).toString()
                count100.text = (count100Value - remaining100).toString()

                updateTotalCount()
                recordTransaction("Withdraw: $withdrawAmountValue")
            } else {
                Toast.makeText(
                    this,
                    "Cannot withdraw the exact amount. Please enter a different amount.",
                    Toast.LENGTH_SHORT
                ).show()
                recordTransaction("Failed Transaction (cannot withdraw exact amount)")
            }
        } else {
            Toast.makeText(this, "Insufficient balance. Please enter a lower amount.", Toast.LENGTH_SHORT).show()
            recordTransaction("Failed Transaction (insufficient balance)")
        }

        dialog.dismiss()
    }


    private fun recordTransaction(transactionResult: String) {
        val timestamp =
            SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z", Locale.getDefault()).format(Date())
        transactionRecords += "$timestamp\n $transactionResult\n"
        transactionRecordsTextView.text = transactionRecords
        saveTransactionRecordsToSharedPreferences()
    }

    private fun saveCountsToSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.putString("count2000", count2000.text.toString())
        editor.putString("count500", count500.text.toString())
        editor.putString("count200", count200.text.toString())
        editor.putString("count100", count100.text.toString())
        editor.apply()
    }

    private fun restoreCountsFromSharedPreferences() {
        count2000.text = sharedPreferences.getString("count2000", "0")
        count500.text = sharedPreferences.getString("count500", "0")
        count200.text = sharedPreferences.getString("count200", "0")
        count100.text = sharedPreferences.getString("count100", "0")
    }

    private fun saveTransactionRecordsToSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.putString("transactionRecords", transactionRecords)
        editor.apply()
    }

    private fun restoreTransactionRecordsFromSharedPreferences() {
        transactionRecords = sharedPreferences.getString("transactionRecords", "") ?: ""
        transactionRecordsTextView.text = transactionRecords
    }

    override fun onPause() {
        super.onPause()
        saveCountsToSharedPreferences()
        saveTransactionRecordsToSharedPreferences()
    }
}