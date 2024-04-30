package za.co.varsitycollege.st10036509.punchin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetViewBinding
import android.widget.Button
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*



class TimesheetViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimesheetViewBinding
    private lateinit var btnPreviousWeek: Button
    private lateinit var btnNextWeek: Button
    private lateinit var tvWeeklySelector: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimesheetViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set onClickListener for the previous week button
        binding.btnPreviousWeek.setOnClickListener {
            navPreviousWeek()
        }

        // Set onClickListener for the next week button
        binding.btnNextWeek.setOnClickListener {
            navNextWeek()
        }

        // Display the current week initially
        displayCurrentWeek()
    }


    private fun navPreviousWeek() {
        // Get the current date
        val calendar = Calendar.getInstance()
        calendar.time = Date()

        // Subtract 7 days from the current date
        calendar.add(Calendar.DAY_OF_YEAR, -7)

        // Display the new week
        displayWeek(calendar.time)
    }

    private fun navNextWeek() {
        // Get the current date
        val calendar = Calendar.getInstance()
        calendar.time = Date()

        // Add 7 days to the current date
        calendar.add(Calendar.DAY_OF_YEAR, 7)

        // Display the new week
        displayWeek(calendar.time)
    }

    private fun displayCurrentWeek() {
        // Display the current week
        displayWeek(Date())
    }

    private fun displayWeek(date: Date) {
        // Calculate the start date of the week
        val startCalendar = Calendar.getInstance()
        startCalendar.time = date
        startCalendar.set(Calendar.DAY_OF_WEEK, startCalendar.firstDayOfWeek)
        val startDate = startCalendar.time

        // Calculate the end date of the week
        val endCalendar = Calendar.getInstance()
        endCalendar.time = startDate
        endCalendar.add(Calendar.DAY_OF_YEAR, 6)
        val endDate = endCalendar.time

        // Format the dates as required ("7 - 14 March 2024")
        val dateFormat = SimpleDateFormat("d", Locale.getDefault())
        val startDay = dateFormat.format(startDate)
        val endDay = dateFormat.format(endDate)

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val monthYear = monthFormat.format(startDate)

        // Update UI with the new week time window using data binding
        binding.tvWeeklySelector.setText("$startDay - $endDay $monthYear")

    }
}

