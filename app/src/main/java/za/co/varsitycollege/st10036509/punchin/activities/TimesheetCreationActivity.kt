package za.co.varsitycollege.st10036509.punchin.activities
//
import android.app.DatePickerDialog
import android.app.ProgressDialog
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
import android.graphics.Bitmap
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isEmpty
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler
import za.co.varsitycollege.st10036509.punchin.utils.ValidationHandler
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Locale

class TimesheetCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimesheetCreationBinding //binds the ActivityTimesheetCreation
    private lateinit var intentHandler: IntentHandler
    private lateinit var timesheetModel: TimesheetModel
    private lateinit var validationHandler: ValidationHandler
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private lateinit var loadingDialogHandler: LoadDialogHandler//setup an intent handler for navigating pages
    private lateinit var toaster: ToastHandler
    private var currentUser: FirebaseUser? = null
    private var startDate: Date? = null
    private var timesheetStartTime: Date? = null
    private var timesheetEndTime: Date? = null
    private var authModel = AuthenticationModel()
    private lateinit var timesheetPhotoString : String


    //strings to use
    private companion object {
        const val MSG_NULL = ""
        const val MSG_DATABASE_ADD_ERROR = "Failed to add timesheet data to database"
        const val MSG_UNEXPECTED_ERROR = "Unexpected Error Occurred"
        const val MSG_NO_PROJECT_SELECTED = "Please select a project first"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimesheetCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        loadingDialogHandler = LoadDialogHandler(this@TimesheetCreationActivity, progressDialog)//initialise the loading dialog
        //initialise the validation handler
        validationHandler = ValidationHandler()
        // Initialize FirebaseAuth
        toaster = ToastHandler(this@TimesheetCreationActivity)

        val capturedPhoto = intent.getParcelableExtra<Bitmap>("capturedPhoto")
        val imageView: ImageView = binding.ivTimesheetImage
        capturedPhoto?.let {
            imageView.setImageBitmap(it)

            // Convert Bitmap to byte array
            val byteArrayOutputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // Convert byte array to Base64 string manually
            val base64String = StringBuilder()
            for (byte in byteArray) {
                base64String.append(String.format("%02X", byte))
            }
            timesheetPhotoString = base64String.toString()


        }

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
                        toaster.showToast(MSG_NO_PROJECT_SELECTED)
                    }
                }
        }

        timesheetModel = TimesheetModel("", "", "", null, null, null, "", null)

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
                    timesheetStartTime = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time

                    // Update button text
                    val formattedTime =
                        timesheetStartTime?.let { it1 ->
                            SimpleDateFormat(
                                "HH:mm",
                                Locale.getDefault()
                            ).format(it1)
                        }
                    startTimePickerButton.text = formattedTime
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

                    // Update button text
                    val formattedTime =
                        timesheetEndTime?.let { it1 ->
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                it1
                            )
                        }
                    endTimePickerButton.text = formattedTime
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
            btnAdd.setOnClickListener { createTimesheet(timesheetPhotoString) }
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

                // Update the TextView with the selected date
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                binding.textView.text = formattedDate
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
    private fun createTimesheet(timesheetPhotoString : String) {
        // Ensure both start and end times are selected before proceeding
        if (timesheetStartTime != null && timesheetEndTime != null) {
            // Ensure timesheetStartDate is not null before proceeding
            startDate?.let { timesheetStartDate ->
                val timesheetName = binding.etTimesheetName.text.toString().trim()
                val timesheetDescription = binding.etTimesheetDescription.text.toString().trim()

                // Check if project name and description are not empty
                if (timesheetName.isEmpty() || timesheetDescription.isEmpty()) {
                    if (timesheetName.isEmpty()) {
                        Toast.makeText(this, "Please include a project name", Toast.LENGTH_SHORT).show()
                    }
                    if (timesheetDescription.isEmpty()) {
                        Toast.makeText(this, "Please include a project description", Toast.LENGTH_SHORT).show()
                    }
                    // Return without proceeding further
                    return
                }

                val selectedProject = binding.projectDropdown.selectedItem as? Pair<String, String>
                if (binding.projectDropdown.isEmpty()) {
                    Toast.makeText(this, "Please create a project first", Toast.LENGTH_SHORT).show()
                    return
                }
                // Check if a project is selected and retrieve its ID
                val projectId = selectedProject?.second ?: ""

                // Check if the current user is not null
                currentUser?.let { user ->
                    // Get user id
                    val userId = user.uid
                    timesheetModel.setData(
                        userId,
                        timesheetName,
                        projectId,
                        timesheetStartDate,
                        timesheetStartTime!!,
                        timesheetEndTime!!,
                        timesheetDescription,
                        timesheetPhotoString
                    )
                    // Call the method to write project data to Firestore
                    timesheetModel.writeDataToFirestore()
                }
                clearInputFields()
            }
        } else {
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
