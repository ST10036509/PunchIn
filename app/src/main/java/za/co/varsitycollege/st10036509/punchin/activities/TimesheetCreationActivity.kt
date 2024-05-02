package za.co.varsitycollege.st10036509.punchin.activities
//
import android.app.Activity
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
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityCameraBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.ProjectsModel

class TimesheetCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimesheetCreationBinding //binds the ActivityTimesheetCreation
    private lateinit var intentHandler: IntentHandler
    private lateinit var timesheetModel: TimesheetModel
    private var currentUser: FirebaseUser? = null
    private var startDate: Date? = null
    private var timesheetStartTime: Date? = null
    private var timesheetEndTime: Date? = null
    private lateinit var projectModel: ProjectsModel
    private var authModel = AuthenticationModel()

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

        // Call fetchUserProjects function
        fetchUserProjects { projectNames ->
            // Assuming you have a dropdown box named "projectDropdown" in your UI
            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, projectNames)
            binding.projectDropdown.adapter = adapter
        }

        timesheetModel = TimesheetModel("", "", "", null, null, null, "")

        currentUser = authModel.getCurrentUser()

        val startTimePickerButton: Button = binding.btnTimeStart
        val endTimePickerButton: Button = binding.btnTimeEnd
        startTimePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    timesheetStartTime = calendar.time // Set timesheetStartTime as Date
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
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    timesheetEndTime = calendar.time // Set timesheetStartTime as Date
                },
                hour,
                minute,
                false
            )

            timePickerDialog.show()
        }



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
            ivTimesheetImage.setOnClickListener { intentHandler.openActivityIntent(CameraActivity::class.java) }
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

        intentHandler.openActivityIntent(TimesheetViewActivity::class.java)

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
                val timesheetProjectName = binding.projectDropdown.selectedItem.toString()

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
                        timesheetStartTime!!,
                        timesheetEndTime!!,
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
        val timesheetDescriptionET = findViewById<EditText>(R.id.et_Timesheet_Description)

        timesheetNameET.setText("")
        timesheetDescriptionET.setText("")
    }

    // Function to fetch project names for the current user
    fun fetchUserProjects(completion: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        currentUser = authModel.getCurrentUser()

        // Check if user is logged in
        currentUser?.let { user ->
            // Query Firestore for projects created by the current user
            db.collection("projects")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    val projectNames = mutableListOf<String>()
                    for (document in documents) {
                        // Assuming "name" is the field containing the project name
                        val projectName = document.getString("projectName")
                        projectName?.let {
                            projectNames.add(it)
                        }
                    }
                    completion(projectNames)
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    completion(emptyList()) // Return empty list on failure
                }
        } ?: run {
            // User is not logged in
            completion(emptyList()) // Return empty list if user is not logged in
        }
    }


}
