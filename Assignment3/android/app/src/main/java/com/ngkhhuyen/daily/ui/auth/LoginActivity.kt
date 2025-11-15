package com.ngkhhuyen.daily.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ngkhhuyen.daily.R
import com.ngkhhuyen.daily.databinding.ActivityLoginBinding
import com.ngkhhuyen.daily.data.remote.RetrofitClient
import com.ngkhhuyen.daily.data.repository.AuthRepository
import com.ngkhhuyen.daily.ui.home.HomeActivity
import com.ngkhhuyen.daily.utils.PreferenceManager
import com.ngkhhuyen.daily.viewmodel.AuthViewModel
import com.ngkhhuyen.daily.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
            binding.btnLogin.isEnabled = !isLoading
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observe login success
        viewModel.loginSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.success_login), Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
        }
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
            // Call ViewModel to login via API
            viewModel.login(email, password)
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}