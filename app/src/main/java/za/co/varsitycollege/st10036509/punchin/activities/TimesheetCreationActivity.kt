package za.co.varsitycollege.st10036509.punchin.activities
//
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetCreationBinding
import za.co.varsitycollege.st10036509.punchin.models.TimesheetModel
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import java.util.Calendar
import java.util.Date
import android.app.TimePickerDialog
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.ProjectsModel
import com.google.firebase.Timestamp
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityCameraBinding



class TimesheetCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimesheetCreationBinding //binds the ActivityTimesheetCreation
    private lateinit var intentHandler: IntentHandler
    private lateinit var timesheetModel: TimesheetModel
    private var currentUser: FirebaseUser? = null
    private var startDate: Date? = null
    private var timesheetStartTime: Date? = null
    private var timesheetEndTime: Date? = null
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
        fetchUserProjects { projectData ->
            val adapter = ArrayAdapter<Pair<String, String>>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                projectData
            )
            binding.projectDropdown.adapter = adapter

            binding.projectDropdown.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedProject = projectData[position]
                        val selectedProjectId = selectedProject.second
                        // Now you have the selected project ID, you can use it as needed
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing if nothing is selected
                    }
                }
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
                val selectedProject = binding.projectDropdown.selectedItem as? Pair<String, String>

                // Check if a project is selected and retrieve its ID
                val projectId = selectedProject?.second ?: ""

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

    // Function to fetch project names and IDs for the current user
    fun fetchUserProjects(completion: (List<Pair<String, String>>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        currentUser = authModel.getCurrentUser()

        // Check if user is logged in
        currentUser?.let { user ->
            // Query Firestore for projects created by the current user
            db.collection("projects")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    val projectData = mutableListOf<Pair<String, String>>()
                    for (document in documents) {
                        val projectName = document.getString("projectName")
                        val projectId = document.id // Assuming the document ID is the project ID
                        projectName?.let {
                            projectData.add(it to projectId)
                        }
                    }
                    completion(projectData)
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
