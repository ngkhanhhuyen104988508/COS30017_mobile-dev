package com.ngkhhuyen.daily.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ngkhhuyen.daily.R
import com.ngkhhuyen.daily.databinding.ActivityLoginBinding
import com.ngkhhuyen.daily.ui.auth.RegisterActivity
import com.ngkhhuyen.daily.ui.home.HomeActivity
import com.ngkhhuyen.daily.utils.PreferenceManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            validateAndLogin()
        }

        binding.tvSignUpLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password - Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Clear previous errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // Validation
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_short)
            isValid = false
        }

        if (isValid) {
            performLogin(email, password)
        }
    }

    private fun performLogin(email: String, password: String) {
        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        // TODO: Week 4 - Replace with actual API call
        // For now, simulate login with delay
        binding.root.postDelayed({
            // Simulate successful login
            preferenceManager.saveUserData(
                userId = 1,
                email = email,
                username = "Test User",
                token = "fake_token_for_testing"
            )

            Toast.makeText(this, getString(R.string.success_login), Toast.LENGTH_SHORT).show()

            // Navigate to Home
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, 1500)
    }
}