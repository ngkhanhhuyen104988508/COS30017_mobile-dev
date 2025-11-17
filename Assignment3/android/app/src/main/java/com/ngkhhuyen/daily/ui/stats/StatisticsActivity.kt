package com.ngkhhuyen.daily.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.ngkhhuyen.daily.R
import com.ngkhhuyen.daily.data.remote.RetrofitClient
import com.ngkhhuyen.daily.data.repository.StatsRepository
import com.ngkhhuyen.daily.databinding.ActivityStatisticsBinding
import com.ngkhhuyen.daily.viewmodel.StatsViewModel
import com.ngkhhuyen.daily.viewmodel.StatsViewModelFactory

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var viewModel: StatsViewModel
    private var currentPeriod = "7d"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel
        val repository = StatsRepository(RetrofitClient.apiService)
        val factory = StatsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[StatsViewModel::class.java]

        setupToolbar()
        setupPeriodButtons()
        observeData()

        // Load initial data
        viewModel.loadStats(currentPeriod)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupPeriodButtons() {
        binding.btn7Days.setOnClickListener {
            currentPeriod = "7d"
            updatePeriodButtons()
            viewModel.loadStats(currentPeriod)
        }

        binding.btn30Days.setOnClickListener {
            currentPeriod = "30d"
            updatePeriodButtons()
            viewModel.loadStats(currentPeriod)
        }

        binding.btnAll.setOnClickListener {
            currentPeriod = "all"
            updatePeriodButtons()
            viewModel.loadStats(currentPeriod)
        }

        updatePeriodButtons()
    }

    private fun updatePeriodButtons() {
        // Reset all buttons
        binding.btn7Days.strokeWidth = 0
        binding.btn30Days.strokeWidth = 0
        binding.btnAll.strokeWidth = 0

        // Highlight selected
        val selectedButton = when (currentPeriod) {
            "7d" -> binding.btn7Days
            "30d" -> binding.btn30Days
            else -> binding.btnAll
        }
        selectedButton.strokeWidth = 4
        selectedButton.strokeColor = getColorStateList(R.color.primary)
    }

    private fun observeData() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.statsData.observe(this) { stats ->
            updateSummaryCards(stats)
            updatePieChart(stats)
            updateLineChart(stats)
        }
    }

    private fun updateSummaryCards(stats: com.ngkhhuyen.daily.data.models.StatsData) {
        binding.tvTotalCount.text = stats.totalEntries.toString()

        // Find most frequent mood
        val mostFrequent = stats.distribution.maxByOrNull { it.count }
        if (mostFrequent != null) {
            val emoji = when (mostFrequent.moodType) {
                "happy" -> "😊"
                "sad" -> "😢"
                "angry" -> "😠"
                "calm" -> "😌"
                "anxious" -> "😰"
                else -> "😊"
            }
            binding.tvMostFrequent.text = emoji
        }
    }

    private fun updatePieChart(stats: com.ngkhhuyen.daily.data.models.StatsData) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        stats.distribution.forEach { dist ->
            entries.add(PieEntry(dist.count.toFloat(), dist.moodType.capitalize()))

            // Add color based on mood type
            val color = when (dist.moodType) {
                "happy" -> Color.parseColor("#FFD54F")
                "sad" -> Color.parseColor("#64B5F6")
                "angry" -> Color.parseColor("#EF5350")
                "calm" -> Color.parseColor("#81C784")
                "anxious" -> Color.parseColor("#BA68C8")
                else -> ColorTemplate.MATERIAL_COLORS[0]
            }
            colors.add(color)
        }

        if (entries.isEmpty()) {
            binding.pieChart.visibility = View.GONE
            return
        }

        binding.pieChart.visibility = View.VISIBLE
        val dataSet = PieDataSet(entries, "Mood Distribution")
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        })

        binding.pieChart.apply {
            this.data = data
            description.isEnabled = false
            legend.isEnabled = true
            setDrawEntryLabels(true)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            animateY(1000)
            invalidate()
        }
    }

    private fun updateLineChart(stats: com.ngkhhuyen.daily.data.models.StatsData) {
        if (stats.trend.isEmpty()) {
            binding.lineChart.visibility = View.GONE
            return
        }

        binding.lineChart.visibility = View.VISIBLE

        // Group by date and sum counts
        val dateMap = mutableMapOf<String, Float>()
        stats.trend.forEach { trend ->
            val currentCount = dateMap[trend.entryDate] ?: 0f
            dateMap[trend.entryDate] = currentCount + trend.count.toFloat()
        }

        val entries = dateMap.entries
            .sortedBy { it.key }
            .mapIndexed { index, entry ->
                Entry(index.toFloat(), entry.value)
            }

        val dataSet = LineDataSet(entries, "Daily Mood Entries")
        dataSet.apply {
            color = getColor(R.color.primary)
            setCircleColor(getColor(R.color.primary))
            lineWidth = 2f
            circleRadius = 4f
            setDrawFilled(true)
            fillColor = getColor(R.color.primary)
            fillAlpha = 50
            valueTextSize = 10f
        }

        val data = LineData(dataSet)

        binding.lineChart.apply {
            this.data = data
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(true)
            axisRight.isEnabled = false
            animateX(1000)
            invalidate()
        }
    }
}