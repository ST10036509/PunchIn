package za.co.varsitycollege.st10036509.punchin.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetCreationBinding
import za.co.varsitycollege.st10036509.punchin.models.TimesheetModel
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import java.util.Calendar
import java.util.Date
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Button
import android.widget.Toast
import com.google.firebase.Timestamp


class TimesheetCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimesheetCreationBinding //binds the ActivityTimesheetCreation
    private lateinit var intentHandler: IntentHandler
    private lateinit var timesheetModel: TimesheetModel
    private var currentUser: FirebaseUser? = null
    private var startDate: Date? = null
    private var timesheetStartTime: Date? = null
    private var timesheetEndTime: Date? = null

    //strings to use
    private companion object {
        const val MSG_NULL = ""
        const val MSG_DATABASE_ADD_ERROR = "Failed to add timesheet data to database"
        const val MSG_UNEXPECTED_ERROR = "Unexpected Error Occurred"
        const val MSG_INVALID_CREDENTIALS = "No account found matches these credentials"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimesheetCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timesheetModel = TimesheetModel("", "", "", null, null, null, "")

        //Initialize firebase auth
        val auth = FirebaseAuth.getInstance()


        val startTimePickerButton: Button = binding.btnTimeStart
        val endTimePickerButton: Button = binding.btnTimeEnd
        startTimePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    timesheetStartTime =  Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                },
                hour,
                minute,
                false
            )

            timePickerDialog.show()
        }

        endTimePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    timesheetEndTime = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                },
                hour,
                minute,
                false
            )

            timePickerDialog.show()
        }

        // Get current user
        currentUser = auth.currentUser

        intentHandler = IntentHandler(this@TimesheetCreationActivity)
        //setup listeners for ui controls
        setupListeners()

    }

    /**
     * Method to setup listeners for UI controls
     */
    private fun setupListeners() {
        //apply binding to all lines within method
        binding.apply {

            btnReturn.setOnClickListener { returnToView() }
            btnAdd.setOnClickListener { createTimesheet() }
            btnDatePicker.setOnClickListener {
                showDatePicker { selectedDate ->
                    // Save the selected date to timesheetStartDate
                    startDate = selectedDate
                }
            }
        }
    }

    private fun showDatePicker(callback: (Date?) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                // Handle the selected date
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                callback(selectedDate.time)
            },
            year,
            month,
            day
        )

        // Show the Date Picker dialog
        datePickerDialog.show()
    }

    /**
     * Method to return to the View page and cancel adding a new timesheet
     */
    private fun returnToView() {

    }

    /**
     * Method to add new timesheet once fully entered
     */
    private fun createTimesheet() {
        // Ensure both start and end times are selected before proceeding
        if (timesheetStartTime != null && timesheetEndTime != null) {
            // Ensure timesheetStartDate is not null before proceeding
            startDate?.let { timesheetStartDate ->
                val timesheetName = binding.etTimesheetName.text.toString().trim()
                val timesheetProjectName = binding.etTimesheetProjectName.text.toString().trim()
                // edit this so that it gets the actual projectId
                val projectId = timesheetProjectName
                val timesheetDescription = binding.etTimesheetDescription.text.toString().trim()

                // Check if the current user is not null
                currentUser?.let { user ->
                    // Get user id
                    val userId = user.uid

                    // Set timesheet data using the setData method of TimesheetModel
                    timesheetModel.setData(
                        userId,
                        timesheetName,
                        projectId,
                        timesheetStartDate,
                        timesheetStartTime,
                        timesheetEndTime,
                        timesheetDescription
                    )

                    // Call the method to write project data to Firestore
                    timesheetModel.writeDataToFirestore()

                }

                clearInputFields()
            }
        } else {
            // Inform the user that both start and end times need to be selected
            Toast.makeText(this, "Please select both start and end times", Toast.LENGTH_SHORT).show()
        }
    }


    private fun clearInputFields() {

        val timesheetNameET = findViewById<EditText>(R.id.et_Timesheet_Name)
        val timesheetProjectNameET = findViewById<EditText>(R.id.et_Timesheet_ProjectName)
        val timesheetDescriptionET = findViewById<EditText>(R.id.et_Timesheet_Description)

        timesheetNameET.setText("")
        timesheetProjectNameET.setText("")
        timesheetDescriptionET.setText("")
    }

}
