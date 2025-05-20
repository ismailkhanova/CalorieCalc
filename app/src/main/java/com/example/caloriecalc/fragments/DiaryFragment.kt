package com.example.caloriecalc.fragments

import android.graphics.Color
import android.graphics.Insets.add
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriecalc.adapters.DaysAdapter
import com.example.caloriecalc.R
import com.example.caloriecalc.adapters.MealAdapter
import com.example.caloriecalc.data.CalendarDay
import com.example.caloriecalc.data.DiaryViewModel
import com.example.caloriecalc.data.Meal
import com.example.caloriecalc.data.Product
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

class DiaryFragment : Fragment() {
    private lateinit var viewModel: DiaryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var daysAdapter: DaysAdapter
    private lateinit var recyclerViewMeals: RecyclerView
    private lateinit var mealAdapter: MealAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var totalCaloriesTextView: TextView
    private lateinit var totalProteinTextView: TextView
    private lateinit var totalFatsTextView: TextView
    private lateinit var totalCarbsTextView: TextView
    private var selectedDate: LocalDate = LocalDate.now()


    private val cachedWeek = mutableMapOf<LocalDate, Map<String, List<Product>>>()
    private val meals = mutableListOf( // Добавляем meals в поле класса, чтобы к нему можно было обращаться
        Meal("Завтрак"),
        Meal("Обед"),
        Meal("Ужин"),
        Meal("Перекус")
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(DiaryViewModel::class.java)
        recyclerView = view.findViewById(R.id.recyclerViewDays)
        recyclerViewMeals = view.findViewById(R.id.recyclerViewMeals)
        progressBar = view.findViewById(R.id.progressBar)

        totalCaloriesTextView = view.findViewById(R.id.text_total_calories)
        totalProteinTextView = view.findViewById(R.id.text_total_protein)
        totalFatsTextView = view.findViewById(R.id.text_total_fats)
        totalCarbsTextView = view.findViewById(R.id.text_total_carbs)



        view.findViewById<ImageView>(R.id.btn_calendar).setOnClickListener {
            showDatePicker()
        }

        setupRecyclerView()
        setupMealsRecyclerView()
        loadCurrentWeek()

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Можно также блокировать UI во время загрузки
            recyclerView.isEnabled = !isLoading
            recyclerViewMeals.isEnabled = !isLoading
        }

        viewModel.foodData.observe(viewLifecycleOwner) { foodData ->
            val selectedDate = viewModel.selectedDate.value ?: LocalDate.now()

            // Обновляем отображение приёмов пищи
            val mealsData = viewModel.getFoodForDate(selectedDate)
            updateMealsForDate(selectedDate, mealsData)

            // Обновляем информацию о нутриентах, если известна цель по калориям
            viewModel.goalCalories.value?.let { goalCalories ->
                updateNutrientUI(goalCalories, selectedDate)
            }
        }


        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            daysAdapter.currentSelectedDate = date

            // Получаем данные из кэша ViewModel
            val meals = viewModel.getMealsForDate(date)
            updateMealsForDate(date, meals)
        }

        viewModel.goalCalories.observe(viewLifecycleOwner) { goalCalories ->
            val selectedDate = viewModel.selectedDate.value ?: LocalDate.now()
            updateNutrientUI(goalCalories, selectedDate)
        }



        view?.post {
            viewModel.loadInitialWeekData()
        }

        val openSearchButton: ImageView = view.findViewById(R.id.search_button)
        openSearchButton.setOnClickListener {
            // Открываем новый фрагмент поиска с помощью NavController
            findNavController().navigate(R.id.action_diaryFragment_to_searchProductFragment)
        }

        parentFragmentManager.setFragmentResultListener("product_added", this) { _, bundle ->
            val mealName = bundle.getString("meal_name")
            val newProduct = bundle.getParcelable("new_product", Product::class.java)

            if (mealName != null && newProduct != null) {
                val selectedDate = daysAdapter.currentSelectedDate ?: LocalDate.now()
                viewModel.addProduct(selectedDate, mealName, newProduct)
            }
        }

        return view
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            // Конвертируем миллисекунды в LocalDate
            val date = Instant.ofEpochMilli(selectedDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            // Обновляем выбранную дату
            viewModel.setSelectedDate(date)

            // Загружаем данные для выбранной даты
            viewModel.loadDataForDate(date)

            // Обновляем неделю в календаре
            updateCalendarWeek(date)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun updateCalendarWeek(selectedDate: LocalDate) {
        val startDate = selectedDate.with(DayOfWeek.MONDAY)
        val endDate = startDate.plusDays(6)

        val weekDates = mutableListOf<LocalDate>()
        var currentDate = startDate
        while (currentDate <= endDate) {
            weekDates.add(currentDate)
            currentDate = currentDate.plusDays(1)
        }

        // Обновляем адаптер с новыми датами
        val updatedDays = weekDates.map { date ->
            CalendarDay(date, date.isEqual(LocalDate.now()))
        }
        daysAdapter.submitList(updatedDays)

        // Выделяем выбранную дату
        daysAdapter.currentSelectedDate = selectedDate
        daysAdapter.notifyDataSetChanged()
    }

    private fun updateMealsForDate(date: LocalDate, mealsData: Map<String, List<Product>>) {
        meals.forEach { meal ->
            meal.products.clear()
            meal.products.addAll(mealsData[meal.name] ?: emptyList())
        }
        mealAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        daysAdapter = DaysAdapter (
            onDaySelected = { selectedDay ->
                Log.d("DiaryFragment", "Day selected: ${selectedDay.date}")
            },
            onDateSelected = { date ->
                viewModel.setSelectedDate(date)
            }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = daysAdapter
        }

        val today = LocalDate.now()
        daysAdapter.currentSelectedDate = today
        viewModel.setSelectedDate(today)
    }

    private fun setupMealsRecyclerView() {
        mealAdapter = MealAdapter(
            meals,
            onAddProductClick = { selectedMeal ->
                val bundle = Bundle().apply {
                    putString("meal_name", selectedMeal.name)
                }

                // Переход к фрагменту выбора продуктов/рецептов
                findNavController().navigate(
                    R.id.action_diaryFragment_to_addToMealViewPagerFragment,
                    bundle
                )
            },
            onDeleteProductClick = { selectedMeal, product ->
                // Удаление продукта через ViewModel
                viewModel.removeProduct(selectedDate, selectedMeal.name, product)
            }
        )


    recyclerViewMeals.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewMeals.adapter = mealAdapter
        recyclerViewMeals.isNestedScrollingEnabled = true
    }

    // Метод для загрузки текущей недели
    private fun loadCurrentWeek() {
        val today = LocalDate.now()
        val currentWeek = mutableListOf<LocalDate>()

        for (i in 0..6) {
            val currentDate = today.with(DayOfWeek.MONDAY).plusDays(i.toLong())
            currentWeek.add(currentDate)
        }

        // Обновляем список дней, выделяя текущий день
        updateDays(currentWeek)
    }

    // Метод для обновления дней
    private fun updateDays(days: List<LocalDate>) {
        val today = LocalDate.now()  // Текущая дата
        val updatedDays = days.map { date ->
            val isToday = date.isEqual(today)
            CalendarDay(date, isToday)
        }
        daysAdapter.submitList(updatedDays)
    }

    private fun updateNutrientUI(goalCalories: Int, date: LocalDate) {
        val uiModel = viewModel.getNutrientUIModel(date, goalCalories)

        totalCaloriesTextView.text = uiModel.caloriesText
        totalProteinTextView.text = uiModel.proteinText
        totalFatsTextView.text = uiModel.fatsText
        totalCarbsTextView.text = uiModel.carbsText
        view?.findViewById<TextView>(R.id.caloriesInPercent)?.text = uiModel.caloriesInPercentText

        setupPieChart(
            uiModel.pieChartData.first,
            uiModel.pieChartData.second,
            uiModel.pieChartData.third
        )

        setupCaloriePieChart(
            uiModel.kcalChartData.first,
            uiModel.kcalChartData.second
        )

        view?.findViewById<TextView>(R.id.usedKcal)?.text = uiModel.usedKcalText
        view?.findViewById<TextView>(R.id.leftKcal)?.text = uiModel.leftKcalText
    }


    private fun setupPieChart(protein: Float, fats: Float, carbs: Float) {
        val pieChart = view?.findViewById<PieChart>(R.id.pieChart)

        // 1. Подготовка данных
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(protein, "Белки"))
            add(PieEntry(fats, "Жиры"))
            add(PieEntry(carbs, "Углеводы"))
        }

        // 2. Настройка внешнего вида
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.protein_color), // Синий
                ContextCompat.getColor(requireContext(), R.color.fats_color),   // Красный
                ContextCompat.getColor(requireContext(), R.color.carbs_color)  // Зеленый
            )
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }

        // 3. Форматирование значений
        val pieData = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}%"
                }
            })
        }

        // 4. Настройка диаграммы
        pieChart?.apply {
            data = pieData
            description.isEnabled = false // Скрыть описание
            isDrawHoleEnabled = false     // Кольцо в центре
            setDrawEntryLabels(false) // ← Это отключит все надписи на секторах
            setHoleColor(Color.TRANSPARENT)
            setEntryLabelColor(Color.BLACK)
            animateY(1000)

            legend.isEnabled = false
            invalidate()                 // Обновить
        }
    }

    private fun setupCaloriePieChart(consumed: Float, remaining: Float) {
        val pieChart = view?.findViewById<PieChart>(R.id.caloriePieChart)

        // 1. Подготовка данных
        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(consumed, "Употреблено"))
            add(PieEntry(remaining, "Осталось"))
        }

        // 2. Настройка внешнего вида
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.kcal_used_color),    // Например, синий
                ContextCompat.getColor(requireContext(), R.color.kcal_left_color)     // Например, серый или светлый
            )
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            sliceSpace = 2f // Расстояние между секторами
            setDrawValues(false)

        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()} ккал"
                }
            })
        }

        // 3. Настройка диаграммы
        pieChart?.apply {
            data = pieData
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 10f
            setHoleColor(Color.WHITE)
            setDrawEntryLabels(false)
            legend.isEnabled = false
            setUsePercentValues(false)
            animateY(1000)
            setTransparentCircleAlpha(0)  // Важно! Убирает полупрозрачное кольцо
            invalidate()
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel.selectedDate.value?.let { currentDate ->
            daysAdapter.currentSelectedDate = currentDate
            daysAdapter.notifyDataSetChanged()
        }
    }
}
