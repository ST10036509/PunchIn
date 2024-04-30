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
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TimesheetViewActivity : AppCompatActivity() {
    private lateinit var binding: za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetViewBinding
    private val firestoreInstance = FirestoreConnection.getDatabaseInstance()
    private lateinit var intentHandler: IntentHandler
    private val currentDate = LocalDate.now()

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
        displayCurrentWeek(currentDate)
        //retrieveDataFromFirestore()
    }


    private fun navPreviousWeek() {
        //displayWeek(currentDate.minusDays(7))
    }

    private fun navNextWeek() {
        displayWeek(currentDate.plusDays(7))
    }

    private fun displayWeek(endDate: LocalDate) {
        // Display the current week
        val startDay = endDate.minusDays(7).dayOfMonth
        binding.tvWeeklySelector.text= "${startDay} - ${formatDate(endDate)}"
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

    private fun retrieveDataFromFirestore() {
        // Reference to the project document in Firestore
        val timesheetRef = firestoreInstance.collection("timesheets").document("UvZGFdEzfGM7KT4lUwy3")

        // Retrieve data from Firestore
        timesheetRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Parse and populate the TextViews with data
                    val timesheetUser = documentSnapshot.getString("timesheetUser")
                    val timesheetName = documentSnapshot.getString("timesheetName")
                    val timesheetProjectName = documentSnapshot.getString("timesheetProjectName")
                    val timesheetStartDate = documentSnapshot.getString("timesheetStartDate")
                    val timesheetStartTime = documentSnapshot.getString("timesheetStartTime")
                    val timesheetEndTime = documentSnapshot.getString("timesheetEndTime")
                    val timesheetDescription = documentSnapshot.getString("timesheetDescription")

                    // Update TextViews with retrieved data
                    val tvProjectName = findViewById<TextView>(R.id.tv_ProjectName)
                    val tvStartTime = findViewById<TextView>(R.id.tv_EndTime)
                    val tvEndTime = findViewById<TextView>(R.id.tv_colour)
                    val tvTimesheetRecentdate = findViewById<TextView>(R.id.tv_timesheet_recent_date)
                    val tvTimesheetdescription = findViewById<TextView>(R.id.tv_TimesheetDescription)

                    tvProjectName.text = timesheetProjectName
                    tvStartTime.text = timesheetStartTime
                    tvEndTime.text = timesheetEndTime
                    tvTimesheetRecentdate.text = timesheetStartDate
                    tvTimesheetdescription.text = timesheetDescription
                }
            }
            .addOnFailureListener { exception ->
                // Log any errors that occur while retrieving data
                Log.e("ProjectDetailsActivity", "Error retrieving project data: $exception")
            }
    }

}

