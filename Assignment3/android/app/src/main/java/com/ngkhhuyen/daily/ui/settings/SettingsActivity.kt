package com.ngkhhuyen.daily.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ngkhhuyen.daily.R
import com.ngkhhuyen.daily.data.local.AppDatabase
import com.ngkhhuyen.daily.data.remote.RetrofitClient
import com.ngkhhuyen.daily.data.repository.AuthRepository
import com.ngkhhuyen.daily.data.repository.StatsRepository
import com.ngkhhuyen.daily.databinding.ActivitySettingsBinding
import com.ngkhhuyen.daily.ui.auth.LoginActivity
import com.ngkhhuyen.daily.utils.PreferenceManager
import com.ngkhhuyen.daily.viewmodel.SettingsViewModel
import com.ngkhhuyen.daily.viewmodel.SettingsViewModelFactory

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupToolbar()
        setupObservers()
        setupClickListeners()
    }

    private fun setupViewModel() {
        val preferenceManager = PreferenceManager(this)
        val authRepository = AuthRepository(RetrofitClient.apiService, preferenceManager)
        val statsRepository = StatsRepository(RetrofitClient.apiService)

        val factory = SettingsViewModelFactory(authRepository, statsRepository, preferenceManager)
        viewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        // Load all info when screen is created
        viewModel.loadAllInfo()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }

    private fun setupObservers() {
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observe user info
        viewModel.userName.observe(this) { name ->
            binding.tvUserName.text = name
        }

        viewModel.userId.observe(this) { id ->
            binding.tvUserId.text = "#$id"
        }

        // Observe stats
        viewModel.recordedDays.observe(this) { days ->
            binding.tvRecordedDays.text = days.toString()
        }

        viewModel.photoCount.observe(this) { count ->
            binding.tvPhotoCount.text = count.toString()
        }

        // Observe premium status
        viewModel.isPremiumUser.observe(this) { isPremium ->
            if (isPremium) {
                binding.tvPremiumStatus.text = "Subscribed"
                binding.chipPremiumStatus.visibility = View.VISIBLE
            } else {
                binding.tvPremiumStatus.text = "Free"
                binding.chipPremiumStatus.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        // Account section
        binding.cardAccount.setOnClickListener {
            // Navigate to Account Details
            Toast.makeText(this, "Account details - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Theme Calendar
        binding.cardThemeCalendar.setOnClickListener {
            showThemeDialog()
        }

        // Photo Gallery
        binding.cardPhotoGallery.setOnClickListener {
            Toast.makeText(this, "Photo Gallery - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Premium Pass
        binding.cardPremium.setOnClickListener {
            showPremiumDialog()
        }

        // Notification button
        binding.btnNotifications.setOnClickListener {
            Toast.makeText(this, "Notifications - Coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Settings button
        binding.btnSettings.setOnClickListener {
            showMoreSettingsDialog()
        }

        // Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    finish()
                    true
                }
                R.id.nav_history -> {
                    finish()
                    true
                }
                R.id.nav_statistics -> {
                    finish()
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_settings
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Light", "Dark", "Auto")
        val currentTheme = viewModel.themeCalendarSetting.value ?: "Light"
        val selectedIndex = themes.indexOf(currentTheme)

        AlertDialog.Builder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, selectedIndex) { dialog, which ->
                val selectedTheme = themes[which]
                viewModel.saveThemeCalendarSetting(selectedTheme)
                Toast.makeText(this, "Theme changed to $selectedTheme", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPremiumDialog() {
        AlertDialog.Builder(this)
            .setTitle("Premium Pass")
            .setMessage("Upgrade to Premium to unlock:\n\n" +
                    "✓ Unlimited photo storage\n" +
                    "✓ Advanced statistics\n" +
                    "✓ Custom themes\n" +
                    "✓ Export data\n" +
                    "✓ Ad-free experience")
            .setPositiveButton("Upgrade") { _, _ ->
                Toast.makeText(this, "Premium upgrade - Coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Maybe Later", null)
            .show()
    }

    private fun showMoreSettingsDialog() {
        val options = arrayOf(
            "Change Password",
            "Export Data",
            "Privacy Policy",
            "Terms of Service",
            "About",
            "Logout"
        )

        AlertDialog.Builder(this)
            .setTitle("More Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showChangePasswordDialog()
                    1 -> exportData()
                    2 -> showPrivacyPolicy()
                    3 -> showTermsOfService()
                    4 -> showAboutDialog()
                    5 -> showLogoutConfirmation()
                }
            }
            .show()
    }

    private fun showChangePasswordDialog() {
        // TODO: Implement change password dialog
        Toast.makeText(this, "Change Password - Coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun exportData() {
        // TODO: Implement data export
        Toast.makeText(this, "Export Data - Coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showPrivacyPolicy() {
        Toast.makeText(this, "Privacy Policy - Coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showTermsOfService() {
        Toast.makeText(this, "Terms of Service - Coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About DailyBean")
            .setMessage("DailyBean v1.0.0\n\n" +
                    "A simple and beautiful mood journaling app.\n\n" +
                    "Made with ❤️")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                viewModel.logout()
                navigateToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}