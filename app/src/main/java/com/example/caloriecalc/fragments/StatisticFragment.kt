package com.example.caloriecalc.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.caloriecalc.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.utils.ColorTemplate
import androidx.fragment.app.viewModels
import com.example.caloriecalc.data.StatisticViewModel
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StatisticFragment : Fragment() {
    private lateinit var combinedChart: CombinedChart
    private lateinit var nutrientsChart: CombinedChart // Correctly declare nutrientsChart as CombinedChart
    private val viewModel: StatisticViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_statistic, container, false)
        combinedChart = view.findViewById(R.id.barChart) // Ensure barChart is a CombinedChart in the layout
        nutrientsChart = view.findViewById(R.id.nutrientsChart) // Ensure nutrientsChart is a CombinedChart in the layout

        setupObservers()
        return view
    }

    private fun setupObservers() {
        viewModel.weeklyCalories.observe(viewLifecycleOwner) { weeklyCalories ->
            viewModel.weeklyDates.observe(viewLifecycleOwner) { weeklyDates ->
                viewModel.goalCalories.observe(viewLifecycleOwner) { goalCalories ->
                    setupBarChart(weeklyCalories, weeklyDates, goalCalories ?: 2000)
                }
            }
        }
        viewModel.weeklyNutrients.observe(viewLifecycleOwner) { weeklyNutrients ->
            viewModel.weeklyDates.observe(viewLifecycleOwner) { weeklyDates ->
                setupNutrientsChart(weeklyNutrients, weeklyDates)
            }
        }
    }

    private fun setupBarChart(weeklyCalories: List<Int>, weeklyDates: List<String>, goalCalories: Int) {
        val barEntries = weeklyCalories.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }
        val barDataSet = BarDataSet(barEntries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
        }

        val lineEntries = weeklyCalories.indices.map { index -> Entry(index.toFloat(), goalCalories.toFloat()) }
        val lineDataSet = LineDataSet(lineEntries, "").apply {
            color = ColorTemplate.COLORFUL_COLORS[0]
            setDrawCircles(false)
        }

        val barData = BarData(barDataSet)
        val lineData = LineData(lineDataSet)

        val combinedData = CombinedData().apply {
            setData(barData)
            setData(lineData)
        }

        combinedChart.data = combinedData // Use CombinedChart instead of BarChart
        combinedChart.xAxis.valueFormatter = IndexAxisValueFormatter(weeklyDates)
        combinedChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        combinedChart.xAxis.setDrawGridLines(false)
        combinedChart.xAxis.setAvoidFirstLastClipping(false)
        combinedChart.xAxis.axisMinimum = -0.5f // Reduce gap from the left edge
        combinedChart.xAxis.axisMaximum = weeklyCalories.size - 0.5f // Reduce gap from the right edge
        combinedChart.axisLeft.setDrawGridLines(true)
        combinedChart.axisRight.isEnabled = false
        combinedChart.legend.isEnabled = false
        combinedChart.description.isEnabled = false

        combinedChart.invalidate()
    }

    private fun setupNutrientsChart(weeklyNutrients: List<Triple<Double, Double, Double>>, weeklyDates: List<String>) {
        val barEntries = weeklyNutrients.mapIndexed { index, value ->
            BarEntry(
                index.toFloat(),
                floatArrayOf(value.first.toFloat(), value.second.toFloat(), value.third.toFloat())
            )
        }

        val stackedBarDataSet = BarDataSet(barEntries, "").apply {
            colors = listOf(
                ColorTemplate.COLORFUL_COLORS[0], // Proteins
                ColorTemplate.COLORFUL_COLORS[1], // Fats
                ColorTemplate.COLORFUL_COLORS[2]  // Carbs
            )
            stackLabels = arrayOf("Proteins", "Fats", "Carbs")
        }

        val barData = BarData(stackedBarDataSet).apply {
            barWidth = 0.5f
        }

        val combinedData = CombinedData().apply {
            setData(barData)
        }

        nutrientsChart.data = combinedData
        nutrientsChart.xAxis.valueFormatter = IndexAxisValueFormatter(weeklyDates)
        nutrientsChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        nutrientsChart.xAxis.setDrawGridLines(false)
        nutrientsChart.xAxis.axisMinimum = -0.5f
        nutrientsChart.xAxis.axisMaximum = weeklyNutrients.size - 0.5f
        nutrientsChart.axisLeft.setDrawGridLines(true)
        nutrientsChart.axisRight.isEnabled = false
        nutrientsChart.legend.isEnabled = true
        nutrientsChart.description.isEnabled = false

        nutrientsChart.invalidate()
    }

}