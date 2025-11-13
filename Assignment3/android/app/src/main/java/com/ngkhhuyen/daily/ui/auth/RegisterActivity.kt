// android/app/src/main/java/com/yourname/dailybean/ui/auth/RegisterActivity.kt
package com.ngkhhuyen.daily.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ngkhhuyen.daily.R
import com.ngkhhuyen.daily.data.models.RegisterRequest
import com.ngkhhuyen.daily.databinding.ActivityRegisterBinding
import com.ngkhhuyen.daily.ui.home.HomeActivity
import com.ngkhhuyen.daily.utils.PreferenceManager
import com.ngkhhuyen.daily.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var preferenceManager: PreferenceManager
    private val registerVM: RegisterViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        observeViewModel()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }

        binding.tvSignInLink.setOnClickListener {
            finish()
        }
    }

    private fun validateAndRegister() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Clear previous errors
        binding.tilUsername.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        // Validation
        var isValid = true

        if (username.isEmpty()) {
            binding.tilUsername.error = getString(R.string.error_username_required)
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_not_match)
            isValid = false
        }

        if (isValid) {
            performRegister(username, email, password)
        }
    }

    private fun performRegister(username: String, email: String, password: String) {
        registerVM.register(RegisterRequest(username, email, password))

        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        // TODO: Week 4 - Replace with actual API call
        // For now, simulate registration with delay
        binding.root.postDelayed({
            // Simulate successful registration
            preferenceManager.saveUserData(
                userId = 1,
                email = email,
                username = username,
                token = "fake_token_for_testing"
            )

            Toast.makeText(this, getString(R.string.success_register), Toast.LENGTH_SHORT).show()

            // Navigate to Home
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, 1500)
    }

    private fun observeViewModel() {
        registerVM.registerState.observe(this) { responseBody ->
            println(responseBody)
        }
    }
}