package za.co.varsitycollege.st10036509.punchin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.widget.Button
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.compose.ui.text.toUpperCase
import androidx.core.content.ContextCompat
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.activities.TimesheetCreationActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetViewBinding
import za.co.varsitycollege.st10036509.punchin.models.TimesheetModel
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import java.time.LocalDate
import java.time.LocalDate.ofInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimesheetViewActivity : AppCompatActivity() {
    private lateinit var binding: za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetViewBinding
    private val firestoreInstance = FirestoreConnection.getDatabaseInstance()
    private lateinit var intentHandler: IntentHandler
    private lateinit var timesheetModel: TimesheetModel
    private var currentDate = LocalDate.now()
    private val listOfUserTimesheets = mutableListOf<TimesheetModel>()
    private lateinit var selectedDateStart: Date
    private lateinit var selectedDateEnd: Date
    private val filteredTimesheets = mutableListOf<TimesheetModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimesheetViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timesheetModel = TimesheetModel("", "", "", null, null, null, "", null)

        // Set onClickListener for the previous week button
        binding.btnPreviousWeek.setOnClickListener {
            navPreviousWeek()
        }

        // Set onClickListener for the next week button
        binding.btnNextWeek.setOnClickListener {
            navNextWeek()
        }

        binding.fabAddTimesheet.setOnClickListener {
            intentHandler.openActivityIntent(TimesheetCreationActivity::class.java)
        }

        // Display the current week initially
        updateWeekDisplay()
        //retrieveDataFromFirestore()
        //displayData()

    }

    private fun navPreviousWeek() {
        this.currentDate = currentDate.minusWeeks(1)
        updateWeekDisplay()
        searchTimesheetsForUser("sadZ5nWIjNORFcfaZipx8fTBDj32")
        filterTimesheetsByTimePeriod(selectedDateStart, selectedDateEnd)
        displayTimesheetsForWeek(filteredTimesheets)
    }

    private fun navNextWeek() {
        // Calculate the end date of the next week
        val nextWeekEndDate = currentDate.plusWeeks(1)
            .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))

        // Calculate the end date of the current week
        val currentWeekEndDate = LocalDate.now()
            .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))

        // Check if the end date of the next week is after the end of the current week
        if (nextWeekEndDate.isAfter(currentWeekEndDate)) {
            // Do not update the current date or display
            return
        }
        // Update the current date and display
        currentDate = currentDate.plusWeeks(1)
        updateWeekDisplay()
        filterTimesheetsByTimePeriod(selectedDateStart, selectedDateEnd)
        searchTimesheetsForUser("sadZ5nWIjNORFcfaZipx8fTBDj32")
        displayTimesheetsForWeek(filteredTimesheets)
    }

    // Function to update the current date and display the week


    private fun updateWeekDisplay() {
        // Calculate the start date of the week
        val createdAt =
            currentDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val startDay = createdAt.dayOfMonth

        // Calculate the end date of the week
        val endDate = createdAt.plusDays(6)

        // Calculate the end date of the current week
        val endOfWeek = LocalDate.now()
            .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))

        // Update UI with the new week time window using data binding or findViewById
        val displayEndDate = if (endOfWeek.isBefore(endDate)) endOfWeek else endDate

        // Update UI with the new week time window using data binding or findViewById
        binding.tvWeeklySelector.text = "${startDay} - ${formatDate(displayEndDate)}"
        selectedDateStart = convertToDate(createdAt)
        selectedDateEnd = convertToDate(endDate)
        //searchTimesheetsForUser("sadZ5nWIjNORFcfaZipx8fTBDj32")
    }

    fun convertToDate(localDate: LocalDate): Date {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    fun formatDate(date: LocalDate): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return date.format(dateFormatter)
    }

    private fun displayCurrentWeek(date: LocalDate?) {
        // Calculate the start date of the week
        val startDay = currentDate.minusDays(7).dayOfMonth
        // Calculate the end date of the week
        val endDate = currentDate
        // Update UI with the new week time window using data binding
        binding.tvWeeklySelector.text = "${startDay} - ${formatDate(endDate)}"
    }


    fun getHourAndMinutes(date: Date): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    private fun searchTimesheetsForUser(userId: String) {
        // Reference to the collection of timesheets in Firestore
        val timesheetsCollectionRef = firestoreInstance.collection("timesheets")
        val timesheetsQuery = timesheetsCollectionRef.whereEqualTo("userId", userId)

        // Query to search for timesheets with the specified user ID
        timesheetsQuery.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                // Retrieve data from the document
                val timesheetModel = TimesheetModel(
                    userId = document.getString("userId") ?: "",
                    timesheetName = document.getString("timesheetName") ?: "",
                    projectId = document.getString("projectId") ?: "",
                    timesheetStartDate = document.getTimestamp("timesheetStartDate")?.toDate(),
                    timesheetStartTime = document.getTimestamp("timesheetStartTime")?.toDate(),
                    timesheetEndTime = document.getTimestamp("timesheetEndTime")?.toDate(),
                    timesheetDescription = document.getString("timesheetDescription") ?: "",
                    timesheetPhoto = null
                )
            }
        }
    }

    fun filterTimesheetsByTimePeriod(startDate: Date, endDate: Date) {
        // Iterate through the list of timesheets
        for (timesheet in listOfUserTimesheets) {
            // Check if the timesheet's start date is after or equal to the specified start date
            // and if the timesheet's end date is before or equal to the specified end date
            if (timesheet.timesheetStartDate!! in startDate..endDate) {
                // Add the timesheet to the filtered list
                filteredTimesheets.add(timesheet)
            }
        }
    }

    private fun displayTimesheetsForWeek(timesheets: List<TimesheetModel>) {
        val daysOfWeek =
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        // Get a reference to the parent layout where the day linear layouts will be added
        val parentLayout = findViewById<LinearLayout>(R.id.ll_ScrollContainer)
        // Clear any existing views from the parent layout
        parentLayout.removeAllViews()

        // Iterate through each day of the week
        for (day in daysOfWeek) {
            // Create a new linear layout for the day
            val dayLayout = LinearLayout(this)
            dayLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            dayLayout.orientation = LinearLayout.HORIZONTAL

            // Add a TextView to display the day of the week
            val dayTextView = TextView(this)
            dayTextView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            dayTextView.text = day
            dayTextView.setTextColor(ContextCompat.getColor(this, R.color.indigo_900))
            dayTextView.textSize = 20f
            dayTextView.setPadding(20, 10, 20, 10)
            dayLayout.addView(dayTextView)

            val llTimesheetReport = LinearLayout(this)
            llTimesheetReport.layoutParams

            // Get the timesheets for the current day
            val timesheetsForDay = timesheets.filter {
                it.timesheetStartDate?.let { startDate ->
                    val calendar = Calendar.getInstance()
                    calendar.time = startDate
                    val dayNumberOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                    val dayOfWeek = when (dayNumberOfWeek) {
                        2 -> "MONDAY"
                        3 -> "TUESDAY"
                        4 -> "WEDNESDAY"
                        5 -> "THURSDAY"
                        6 -> "FRIDAY"
                        7 -> "SATURDAY"
                        1 -> "SUNDAY"
                        else -> "Invalid number" // Default value if the dayNumber is not recognized
                    }
                    dayOfWeek.toString() == day.toUpperCase()
                } ?: false
            }

            // Iterate through the timesheets for the current day
            for (timesheet in timesheetsForDay) {
                createLayout(timesheet, llTimesheetReport)
            }

            // Add the day layout to the parent layout
            parentLayout.addView(dayLayout)
        }
    }

    fun createLayout(timesheet: TimesheetModel, layout: LinearLayout) {
        // Create the parent LinearLayout
        // Create the LinearLayout for recent views
        val llTimesheetContainer = LinearLayout(this)
        llTimesheetContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llTimesheetContainer.orientation = LinearLayout.VERTICAL
        llTimesheetContainer.setPadding(20, 0, 20, 0)
        //llTimesheetContainer.background = resources.getDrawable(R.drawable.rectangle_wrapper_white_round_corners) // Change R.drawable.rectangle_wrapper_white_round_corners to your drawable resource
        llTimesheetContainer.elevation = 2f
        layout.addView(llTimesheetContainer)

        // Create the LinearLayout for timesheet view
        val llTimesheetView = LinearLayout(this)
        llTimesheetView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            70
        )
        llTimesheetView.orientation = LinearLayout.HORIZONTAL
        llTimesheetView.setPadding(15, 10, 15, 10)
        //llTimesheetView.background = resources.getDrawable(R.drawable.rectangle_wrapper_white_round_corners) // Change R.drawable.rectangle_wrapper_white_round_corners to your drawable resource
        llTimesheetView.elevation = 2f
        llTimesheetContainer.addView(llTimesheetView)

        // Create the LinearLayout for start and stop times
        val llStartStop = LinearLayout(this)
        llStartStop.layoutParams = LinearLayout.LayoutParams(
            75,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        llStartStop.orientation = LinearLayout.VERTICAL
        llStartStop.setPadding(10, 0, 10, 0)
        llTimesheetView.addView(llStartStop)

        // Create the TextView for start time
        val tvStartTime = TextView(this)
        tvStartTime.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        tvStartTime.setPadding(27, 10, 0, 0)
        tvStartTime.text = "16:00"
        tvStartTime.setTextColor(resources.getColor(R.color.dark_blue_900)) // Change R.color.dark_blue_900 to your color resource
        llStartStop.addView(tvStartTime)

        // Create the TextView for end time
        val tvEndTime = TextView(this)
        tvEndTime.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        tvEndTime.setPadding(27, 5, 0, 0)
        tvEndTime.setText("18:30")
        tvEndTime.setTextColor(resources.getColor(R.color.dark_blue_900)) // Change R.color.dark_blue_900 to your color resource
        llStartStop.addView(tvEndTime)

        // Create the ImageView for the divider
        val ivDivider = ImageView(this)
        ivDivider.layoutParams = LinearLayout.LayoutParams(
            1,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        ivDivider.setPadding(0, 10, 0, 10)
        ivDivider.setBackgroundColor(resources.getColor(R.color.divider_color)) // Change R.color.divider to your color resource
        llTimesheetView.addView(ivDivider)

        // Create the LinearLayout for timesheet info
        val llTimesheetInfo = LinearLayout(this)
        llTimesheetInfo.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        llTimesheetInfo.orientation = LinearLayout.VERTICAL
        llTimesheetInfo.setPadding(10, 0, 25, 0)
        llTimesheetView.addView(llTimesheetInfo)

        // Create the TextView for timesheet description
        val tvTimesheetDescription = TextView(this)
        tvTimesheetDescription.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        tvTimesheetDescription.setPadding(10, 10, 0, 0)
        tvTimesheetDescription.text = "Create Settings page in figma"
        tvTimesheetDescription.setTextColor(resources.getColor(R.color.dark_blue_900)) // Change R.color.dark_blue_900 to your color resource
        tvTimesheetDescription.textSize = 16f
        llTimesheetInfo.addView(tvTimesheetDescription)

        // Create the TextView for project name
        val tvProjectName = TextView(this)
        tvProjectName.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        tvProjectName.setPadding(15, 5, 0, 0)
        tvProjectName.text = "OPSC7311_POE"
        tvProjectName.setTextColor(resources.getColor(R.color.dark_blue_900)) // Change R.color.dark_blue_900 to your color resource
        tvProjectName.textSize = 16f
        llTimesheetInfo.addView(tvProjectName)

        // Create the ImageView for the timesheet divider
        val ivTimesheetDivider = ImageView(this)
        ivTimesheetDivider.layoutParams = LinearLayout.LayoutParams(
            30,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        ivTimesheetDivider.setPadding(10, 0, 0, 0)
        ivTimesheetDivider.setBackgroundColor(resources.getColor(R.color.dark_blue_900)) // Change R.color.dark_blue_900 to your color resource
        llTimesheetView.addView(ivTimesheetDivider)

    }
}

