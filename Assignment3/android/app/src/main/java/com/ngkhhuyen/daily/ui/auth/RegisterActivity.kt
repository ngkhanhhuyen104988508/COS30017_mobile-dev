package com.ngkhhuyen.daily.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ngkhhuyen.daily.R
import com.ngkhhuyen.daily.databinding.ActivityRegisterBinding
import com.ngkhhuyen.daily.data.remote.RetrofitClient
import com.ngkhhuyen.daily.data.repository.AuthRepository
import com.ngkhhuyen.daily.ui.home.HomeActivity
import com.ngkhhuyen.daily.utils.PreferenceManager
import com.ngkhhuyen.daily.viewmodel.AuthViewModel
import com.ngkhhuyen.daily.viewmodel.AuthViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel with Repository
        val preferenceManager = PreferenceManager(this)
        val authRepository = AuthRepository(RetrofitClient.apiService, preferenceManager)
        val factory = AuthViewModelFactory(authRepository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observe register success
        viewModel.registerSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.success_register), Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
        }
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
        } else if (username.length < 3) {
            binding.tilUsername.error = "Username must be at least 3 characters"
            isValid = false
        }

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

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_not_match)
            isValid = false
        }

        if (isValid) {
            // Call ViewModel to register via API
            viewModel.register(email, password, username)
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}