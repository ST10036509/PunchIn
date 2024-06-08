package za.co.varsitycollege.st10036509.punchin.activities
//
import android.annotation.SuppressLint
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
import android.util.Log
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
import za.co.varsitycollege.st10036509.punchin.utils.NavbarViewBindingHelper
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
    private lateinit var navbarHelper: NavbarViewBindingHelper//create a NavBarViewBindingsHelper class object
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private lateinit var loadingDialogHandler: LoadDialogHandler//setup an intent handler for navigating pages
    private lateinit var toaster: ToastHandler
    private var currentUser: FirebaseUser? = null
    private var startDate: Date? = null
    private var timesheetStartTime: Date? = null
    private var timesheetEndTime: Date? = null
    private var authModel = AuthenticationModel()
    private var timesheetPhotoString : String? = null


    //strings to use
    private companion object {
        const val MSG_NULL = ""
        const val MSG_DATABASE_ADD_ERROR = "Failed to add timesheet data to database"
        const val MSG_UNEXPECTED_ERROR = "Unexpected Error Occurred"
        const val MSG_NO_PROJECT_SELECTED = "Please select a project first"

    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimesheetCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialize an instance of the NavBarHelper and pass in the current context and binding
        navbarHelper = NavbarViewBindingHelper(this@TimesheetCreationActivity, binding)
        intentHandler = IntentHandler(this@TimesheetCreationActivity)
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
            // Extract only project names for the dropdown
            val projectNames = projectData.map { it.first }
            val projectIds = projectData.map { it.second }

            // Create an ArrayAdapter with project names
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                projectNames
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
                        val selectedProject = projectData[position].second
                        // Now you have the selected project ID, you can use it as needed

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        toaster.showToast(MSG_NO_PROJECT_SELECTED)
                    }
                }
        }

        timesheetModel = TimesheetModel("", "", "", null, null, null, "", null, false)

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
                    val selectedEndTime = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time

                    // Check if end time is earlier than start time
                    if (timesheetStartTime != null && selectedEndTime.before(timesheetStartTime)) {
                        // Show toast indicating end time cannot be earlier than start time
                        Toast.makeText(
                            this@TimesheetCreationActivity,
                            "End time cannot be earlier than start time",
                            Toast.LENGTH_LONG
                        ).show()
                        // Clear the saved value for end time
                        endTimePickerButton.text = "00:00"
                        timesheetEndTime = null
                    } else {
                        // Update button text if end time is valid
                        val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedEndTime)
                        endTimePickerButton.text = formattedTime
                        timesheetEndTime = selectedEndTime
                    }
                },
                hour,
                minute,
                false
            )

            timePickerDialog.show()
        }

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

        loadingDialogHandler.showLoadingDialog("Loading...")
        intentHandler.openActivityIntent(TimesheetViewActivity::class.java)

    }

    /**
     * Method to add new timesheet once fully entered
     */
    private fun createTimesheet(timesheetPhotoString: String?) {
        // Check if timesheetPhotoString is empty
        if (timesheetPhotoString != null) {
            if (timesheetPhotoString.isEmpty()) {
                Toast.makeText(this, "Please add a photo", Toast.LENGTH_SHORT).show()
                // Return without proceeding further
                return
            }
        }

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

                // Check if project description is not more than 32 characters
                if (timesheetDescription.length > 32) {
                    Toast.makeText(this, "Project description must be 32 characters or less", Toast.LENGTH_SHORT).show()
                    // Return without proceeding further
                    return
                }

                val selectedProject = binding.projectDropdown.selectedItem.toString()
                if (binding.projectDropdown.isEmpty()) {
                    Toast.makeText(this, "Please create a project first", Toast.LENGTH_SHORT).show()
                    return
                }

                // Fetch user projects and get project ID
                fetchUserProjects { projectData ->
                    val projectId = projectData.firstOrNull { it.first == selectedProject }?.second
                    if (projectId != null) {
                        Log.d("CreateTimesheet", "Selected Project: $selectedProject, Project ID: $projectId")
                        // Check if the current user is not null
                        currentUser?.let { user ->
                            // Get user id
                            loadingDialogHandler.showLoadingDialog("Loading...")
                            val userId = user.uid
                            Log.d("CreateTimesheet", "User ID: $userId")
                            timesheetModel.setData(
                                userId,
                                timesheetName,
                                projectId,
                                timesheetStartDate,
                                timesheetStartTime!!,
                                timesheetEndTime!!,
                                timesheetDescription,
                                timesheetPhotoString!!,
                                false
                            )
                            // Call the method to write project data to Firestore
                            timesheetModel.writeDataToFirestore()
                        }

                        clearInputFields()
                        loadingDialogHandler.dismissLoadingDialog()
                        loadingDialogHandler.showLoadingDialog("Loading...")
                        intentHandler.openActivityIntent(TimesheetViewActivity::class.java)
                    } else {
                        Log.e("CreateTimesheet", "Project ID not found for selected project: $selectedProject")
                        Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show()
                    }
                }
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
                    Log.e("FetchUserProjects", "Error fetching user projects", exception)
                    // Handle failure
                    completion(emptyList()) // Return empty list on failure
                }
        } ?: run {
            // User is not logged in
            Log.e("FetchUserProjects", "User not logged in")
            completion(emptyList()) // Return empty list if user is not logged in
        }
    }

}
