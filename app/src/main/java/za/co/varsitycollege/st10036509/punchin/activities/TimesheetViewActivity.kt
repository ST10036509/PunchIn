package za.co.varsitycollege.st10036509.punchin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.widget.Button
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.util.Log
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.activities.TimesheetCreationActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetViewBinding
import za.co.varsitycollege.st10036509.punchin.models.TimesheetModel
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TimesheetViewActivity : AppCompatActivity() {
    private lateinit var binding: za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetViewBinding
    private val firestoreInstance = FirestoreConnection.getDatabaseInstance()
    private lateinit var intentHandler: IntentHandler
    private var currentDate = LocalDate.now()

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

        binding.fabAddTimesheet.setOnClickListener{
            intentHandler.openActivityIntent(TimesheetCreationActivity::class.java)
        }

        // Display the current week initially
        updateWeekDisplay()
        //retrieveDataFromFirestore()
        retrieveDataFromFirestore()?.let { displayData(it) }
    }


    private fun navPreviousWeek() {
        this.currentDate = currentDate.minusWeeks(1)
        updateWeekDisplay()
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
    }

    // Function to update the current date and display the week



    private fun updateWeekDisplay() {
        // Calculate the start date of the week
        val startDate = currentDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val startDay = startDate.dayOfMonth

        // Calculate the end date of the week
        val endDate = startDate.plusDays(6)

        // Calculate the end date of the current week
        val endOfWeek = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))

        // Update UI with the new week time window using data binding or findViewById
        val displayEndDate = if (endOfWeek.isBefore(endDate)) endOfWeek else endDate

        // Update UI with the new week time window using data binding or findViewById
        binding.tvWeeklySelector.text = "${startDay} - ${formatDate(displayEndDate)}"
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
        binding.tvWeeklySelector.text= "${startDay} - ${formatDate(endDate)}"
    }

    private fun retrieveDataFromFirestore(): TimesheetModel? {
        var timesheetModel: TimesheetModel? = null
        // Reference to the project document in Firestore
        val timesheetRef = firestoreInstance.collection("timesheets").document("adminTimesheetTest")

        // Retrieve data from Firestore
        timesheetRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Parse and populate the TextViews with data
                    timesheetModel = TimesheetModel(
                        userId = documentSnapshot.getString("userUid") ?:"",
                        timesheetName = documentSnapshot.getString("timesheetName") ?:"",
                        projectId = documentSnapshot.getString("projectId") ?:"",
                        timesheetStartDate = documentSnapshot.getDate("timesheetStartDate"),
                        timesheetStartTime = documentSnapshot.getDate("timesheetStartTime"),
                        timesheetEndTime = documentSnapshot.getDate("timesheetEndTime"),
                        timesheetDescription = documentSnapshot.getString("timesheetDescription") ?:""
                    )
                }
            }
            .addOnFailureListener { exception ->
                // Log any errors that occur while retrieving data
                Log.e("ProjectDetailsActivity", "Error retrieving project data: $exception")
            }
        return timesheetModel
    }

    //leonards code
    private fun displayData(timesheetModel: TimesheetModel){
        binding.tvStartTime.text = timesheetModel.timesheetStartTime.toString()
        binding.tvEndTime.text = timesheetModel.timesheetEndTime.toString()
        binding.tvTimesheetDescription.text = timesheetModel.timesheetDescription.toString()
    }
}

