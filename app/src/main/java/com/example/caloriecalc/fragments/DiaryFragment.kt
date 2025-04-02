package com.example.caloriecalc.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.caloriecalc.adapters.DaysAdapter
import com.example.caloriecalc.R
import com.example.caloriecalc.adapters.MealAdapter
import com.example.caloriecalc.data.CalendarDay
import com.example.caloriecalc.data.DiaryViewModel
import com.example.caloriecalc.data.Meal
import com.example.caloriecalc.data.Product
import java.time.DayOfWeek
import java.time.LocalDate

class DiaryFragment : Fragment() {
    private  lateinit var viewModel: DiaryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var daysAdapter: DaysAdapter
    private lateinit var recyclerViewMeals: RecyclerView
    private lateinit var mealAdapter: MealAdapter
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

        setupRecyclerView()
        setupMealsRecyclerView()
        loadCurrentWeek()  // Загружаем текущую неделю

        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            if (daysAdapter.currentSelectedDate != date) {
                daysAdapter.currentSelectedDate = date
            }
            updateMealsForDate(date)
        }

        val openSearchButton: ImageView = view.findViewById(R.id.search_button)
        openSearchButton.setOnClickListener {
            // Открываем новый фрагмент поиска и закрываем текущий
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchProductFragment()) // Меняем текущий фрагмент
                .addToBackStack(null) // Добавляем в backstack для возможности вернуться назад
                .commit()
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

    private fun updateMealsForDate(date: LocalDate) {
        val mealsData = viewModel.getFoodForDate(date)
        meals.forEach { meal ->
            meal.products.clear()
            meal.products.addAll(mealsData[meal.name] ?: emptyList())
        }
        mealAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        daysAdapter = DaysAdapter { selectedDay ->
            daysAdapter.currentSelectedDate = selectedDay.date
            viewModel.setSelectedDate(selectedDay.date)
            updateMealsForDate(selectedDay.date)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = daysAdapter
        }

        val today = LocalDate.now()
        daysAdapter.currentSelectedDate = today
        viewModel.setSelectedDate(today)
    }

    private fun setupMealsRecyclerView() {
        mealAdapter = MealAdapter(meals) { selectedMeal ->
            val bundle = Bundle().apply {
                putString("meal_name", selectedMeal.name)
            }
            val searchFragment = SearchProductFragment()
            searchFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerViewMeals.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewMeals.adapter = mealAdapter
        recyclerViewMeals.isNestedScrollingEnabled = true

    }

    // 🔥 Метод для добавления продукта в нужный Meal
    private fun addProductToMeal(mealName: String, newProduct: Product) {
        val meal = meals.find { it.name == mealName }
        meal?.let {
            it.products.add(newProduct) // Добавляем продукт в приём пищи

            // 🔥 Обновляем суммарные значения калорий и БЖУ
            it.getTotalCalories()
            it.getTotalProtein()
            it.getTotalFat()
            it.getTotalCarbs()

            mealAdapter.notifyDataSetChanged() // 🔄 Обновляем RecyclerView
        }
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

    private fun loadFoodDataForDate(date: LocalDate) {
        // Логика для загрузки данных для выбранной даты
    }

    override fun onResume() {
        super.onResume()
        viewModel.selectedDate.value?.let { currentDate ->
            daysAdapter.currentSelectedDate = currentDate
            daysAdapter.notifyDataSetChanged()
        }
    }

}
