package com.example.climbingapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.RadioGroup
import android.widget.Toast
import java.util.Locale

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ClimbingScore"
        private const val KEY_SCORE = "score"
        private const val MAX_SCORE = 18
        private const val MIN_SCORE = 0
        private const val HOLDS = 9
    }

    private lateinit var scoreTextView: TextView
    private lateinit var zoneTextView: TextView
    private lateinit var climbButton: Button
    private lateinit var fallButton: Button
    private lateinit var resetButton: Button
    private lateinit var zoneOverlay: ImageView
    private var score = 0
    private var hasFallen = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d(TAG, "onCreate called")
        setContentView(R.layout.activity_main)

        val radioGroup = findViewById<RadioGroup>(R.id.languageRadioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.vietnameseRadio -> setLocale("vi")
                R.id.englishRadio -> setLocale("en")
            }
        }


        scoreTextView = findViewById(R.id.scoreTextView)
        zoneTextView = findViewById(R.id.zoneTextView)
        climbButton = findViewById(R.id.climbButton)
        fallButton = findViewById(R.id.fallButton)
        resetButton = findViewById(R.id.resetButton)
        zoneOverlay = findViewById(R.id.zoneOverlay)

        if (savedInstanceState != null) {
            score = savedInstanceState.getInt(KEY_SCORE, 0)
            hasFallen = score < getMaxScoreForHold(getHoldFromScore(score))
            Log.d(TAG, "Restored score: $score, hasFallen: $hasFallen")
        }
        updateUI()

        climbButton.setOnClickListener {
            Log.d(TAG, "Climb button clicked")
            if (hasFallen) {
                Log.d(TAG, "Cannot climb after falling")
                return@setOnClickListener
            }
            val currentHold = getHoldFromScore(score)
            if (currentHold >= HOLDS) {
                Log.d(TAG, "Already at top hold, cannot climb further")
                Toast.makeText(this, getString(R.string.toast_top), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nextHold = currentHold + 1
            val pointsToAdd = getPointsForHold(nextHold)
            val newScore = score + pointsToAdd
            if (newScore <= MAX_SCORE) {
                score = newScore
                Log.d(TAG, "Climbed to hold $nextHold, score increased by $pointsToAdd to $score")
            } else {
                Log.d(TAG, "Score would exceed max, climb ignored")
            }
            updateUI()
        }

        fallButton.setOnClickListener {
            Log.d(TAG, "Fall button clicked")
            val currentHold = getHoldFromScore(score)
            if (currentHold < 1) {
                Log.d(TAG, "Cannot fall before reaching hold 1")
                return@setOnClickListener
            }
            if (currentHold >= HOLDS) {
                Log.d(TAG, "At top hold, fall does not reduce score")
                return@setOnClickListener
            }
            if (hasFallen) {
                Log.d(TAG, "Already fallen, cannot fall again")
                return@setOnClickListener
            }

            val newScore = (score - 3).coerceAtLeast(MIN_SCORE)
            Log.d(TAG, "Fell, score reduced from $score to $newScore")
            score = newScore
            hasFallen = true
            Toast.makeText(this, getString(R.string.toast_fall, score), Toast.LENGTH_SHORT).show()
            updateUI()
        }

        resetButton.setOnClickListener {
            Log.d(TAG, "Reset button clicked")
            score = 0
            hasFallen = false
            updateUI()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setLocale(languageCode: String) {
        val currentLocale = resources.configuration.locales[0].language
        if (currentLocale == languageCode) return

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate()
    }

    private fun updateUI() {
        val pointLabel = getString(R.string.point)
        scoreTextView.text = getString(R.string.point, score)
        val currentHold = getHoldFromScore(score)
        when (currentHold) {
            in 1..3 -> {
                zoneOverlay.setBackgroundColor(Color.BLUE)
                zoneOverlay.imageAlpha = 51
                zoneOverlay.visibility = View.VISIBLE
            }
            in 4..6 -> {
                zoneOverlay.setBackgroundColor(Color.parseColor("#228B22"))
                zoneOverlay.imageAlpha = 51
                zoneOverlay.visibility = View.VISIBLE
            }
            in 7..9 -> {
                zoneOverlay.setBackgroundColor(Color.RED)
                zoneOverlay.imageAlpha = 51
                zoneOverlay.visibility = View.VISIBLE
            }
            else -> {
                zoneOverlay.visibility = View.GONE
            }
        }
        val color = getColorForHold(currentHold)
        scoreTextView.setTextColor(color)

        val zoneLabel = getString(R.string.zone)
        val zone = when (color) {
            Color.BLUE -> getString(R.string.blue)
            Color.parseColor("#228B22") -> getString(R.string.green)
            Color.RED -> getString(R.string.red)
            else -> getString(R.string.zone_start)
        }
        zoneTextView.text = getString(R.string.zone, zone)
        zoneTextView.setTextColor(color)

        Log.d(TAG, "UI updated: score=$score, hold=$currentHold, zone=$zone, color=$color")
    }

    private fun getHoldFromScore(score: Int): Int {
        return when {
            score == 0 -> 0
            score <= 3 -> score
            score <= 9 -> 3 + (score - 3) / 2
            else -> 6 + (score - 9) / 3
        }.coerceAtMost(HOLDS)
    }

    private fun getMaxScoreForHold(hold: Int): Int {
        return when {
            hold <= 0 -> 0
            hold <= 3 -> hold * 1
            hold <= 6 -> 3 + (hold - 3) * 2
            hold <= 9 -> 9 + (hold - 6) * 3
            else -> MAX_SCORE
        }
    }

    private fun getPointsForHold(hold: Int): Int {
        return when (hold) {
            in 1..3 -> 1
            in 4..6 -> 2
            in 7..9 -> 3
            else -> 0
        }
    }

    private fun getColorForHold(hold: Int): Int {
        return when (hold) {
            in 1..3 -> Color.BLUE
            in 4..6 -> Color.parseColor("#228B22")
            in 7..9 -> Color.RED
            else -> Color.WHITE
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "Saving instance state: score=$score")
        outState.putInt(KEY_SCORE, score)
    }
}



