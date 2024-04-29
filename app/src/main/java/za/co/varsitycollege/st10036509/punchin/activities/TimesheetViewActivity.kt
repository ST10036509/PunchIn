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
    private lateinit var  btnNextWeek: Button
    private lateinit var tvWeeklySelector: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimesheetViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set onClickListener for the button using data binding
        binding.btnNextWeek.setOnClickListener {
            // Get the current date
            val calendar = Calendar.getInstance()
            calendar.time = Date()

            // Calculate the start date of the previous week
            val startCalendar = calendar.clone() as Calendar
            startCalendar.add(Calendar.DAY_OF_YEAR, -7)
            val startDate = startCalendar.time

            // Calculate the end date of the previous week
            val endCalendar = calendar.clone() as Calendar
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

    private fun setupListeners() {

    binding.apply {
        //fabAddTimesheet.setOnClickListener()
        //btnPreviousWeek.setOnClickListener()
        //btnNextWeek.setOnClickListener()
    }

    }

    //private fun
}

