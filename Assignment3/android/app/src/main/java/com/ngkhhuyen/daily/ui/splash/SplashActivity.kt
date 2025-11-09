package com.ngkhhuyen.daily.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.ngkhhuyen.daily.databinding.ActivitySplashBinding
import com.ngkhhuyen.daily.ui.auth.LoginActivity
import com.ngkhhuyen.daily.ui.home.HomeActivity
import com.ngkhhuyen.daily.utils.PreferenceManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        // Navigate after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2000)
    }

    private fun navigateToNextScreen() {
        val intent = if (preferenceManager.isLoggedIn()) {
            // User is already logged in, go to Home
            Intent(this, HomeActivity::class.java)
        } else {
            // User not logged in, go to Login
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}