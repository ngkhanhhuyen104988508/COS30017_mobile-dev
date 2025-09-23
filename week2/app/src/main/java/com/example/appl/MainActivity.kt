package com.example.appl

import android.os.Bundle
import android.widget.Toast
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var btnAdd: Button
    lateinit var btnSubtract: Button
    lateinit var btnMultiply: Button
    lateinit var btnDivide: Button
    lateinit var Num1: EditText
    lateinit var Num2: EditText
    lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Num1 = findViewById(R.id.Num1)
        Num2 = findViewById(R.id.Num2)
        tvResult = findViewById(R.id.tvResult)

        btnAdd = findViewById(R.id.btnAdd)
        btnSubtract = findViewById(R.id.btnSubtract)
        btnMultiply = findViewById(R.id.btnMultiply)
        btnDivide = findViewById(R.id.btnDivide)

        btnAdd.setOnClickListener(this)
        btnSubtract.setOnClickListener(this)
        btnMultiply.setOnClickListener(this)
        btnDivide.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        val num1Str = Num1.text.toString()
        val num2Str = Num2.text.toString()
        val num1Val = Num1.text.toString().toDouble()
        val num2Val = Num2.text.toString().toDouble()


        if (num1Str.isEmpty() || num2Str.isEmpty()) {
            Toast.makeText(this, "Please enter both numbers", Toast.LENGTH_SHORT).show()
            return
        }
        when (v?.id) {
            R.id.btnAdd -> { // Add
                tvResult.text = "Result: ${num1Val + num2Val}"
            }

            R.id.btnSubtract -> { // Subtract
                tvResult.text = "Result: ${num1Val - num2Val}"
            }

            R.id.btnMultiply -> { // Multiply
                tvResult.text = "Result: ${num1Val * num2Val}"
            }

            R.id.btnDivide -> { // Divide
                if (num2Val == 0.0) {
                    Toast.makeText(this, "Cannot divide", Toast.LENGTH_SHORT).show()
                } else {
                    tvResult.text = "Result: ${num1Val / num2Val}"
                }
            }
        }
    }
}