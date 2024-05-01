package za.co.varsitycollege.st10036509.punchin.activities

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetCreationBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.TimesheetModel
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler

class TimesheetCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimesheetCreationBinding //binds the ActivityTimesheetCreation
    private lateinit var intentHandler: IntentHandler
    private lateinit var timesheetModel: TimesheetModel
    private var currentUser: FirebaseUser? = null

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

        //Initialize firebase auth
        val auth = FirebaseAuth.getInstance()

        //Initialize TimesheetModel
        timesheetModel = TimesheetModel("", "", "", "", "", "", "")

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

            //follow appropriate steps once button is pressed
            tbStopwatchTimer.setOnClickListener { timerStopwatchSwap() }
            btnReturn.setOnClickListener { returnToView() }
            btnAdd.setOnClickListener { createTimesheet() }
        }
    }

    /**
     * Method that will swap between our time and stopwatch pages
     */
    private fun timerStopwatchSwap() {

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

        val timesheetNameET = findViewById<EditText>(R.id.et_Timesheet_Name)
        val timesheetProjectNameET = findViewById<EditText>(R.id.et_Timesheet_ProjectName)
        val timesheetStartDateET = findViewById<EditText>(R.id.et_Timesheet_StartDate)
        val timesheetStartTimeET = findViewById<EditText>(R.id.et_Timesheet_Start_Time)
        val timesheetEndTimeET = findViewById<EditText>(R.id.et_Timesheet_End_Time)
        val timesheetDescriptionET = findViewById<EditText>(R.id.et_Timesheet_Description)
        //photo

        val timesheetName = timesheetNameET.text.toString().trim()
        val timesheetProjectName = timesheetProjectNameET.text.toString().trim()
        val timesheetStartDate = timesheetStartDateET.text.toString().trim()
        val timesheetStartTime = timesheetStartTimeET.text.toString().trim()
        val timesheetEndTime = timesheetEndTimeET.text.toString().trim()
        val timesheetDescription = timesheetDescriptionET.text.toString().trim()
        //val timesheetPhoto = ivTimesheetImage

        val projectUID = timesheetProjectName

        // Check if the current user is not null
        currentUser?.let { user ->
            // Get user id
            val timesheetUID = user.uid

            // Set timesheet data using the setData method of TimesheetModel
            timesheetModel.setData(
                timesheetUID,
                timesheetName,
                projectUID,
                timesheetStartDate,
                timesheetStartTime,
                timesheetEndTime,
                timesheetDescription
            )

            // Call the method to write project data to Firestore
            timesheetModel.writeDataToFirestore()

            clearInputFields()

        }

    }

    private fun clearInputFields() {

        val timesheetNameET = findViewById<EditText>(R.id.et_Timesheet_Name)
        val timesheetProjectNameET = findViewById<EditText>(R.id.et_Timesheet_ProjectName)
        val timesheetStartDateET = findViewById<EditText>(R.id.et_Timesheet_StartDate)
        val timesheetStartTimeET = findViewById<EditText>(R.id.et_Timesheet_Start_Time)
        val timesheetEndTimeET = findViewById<EditText>(R.id.et_Timesheet_End_Time)
        val timesheetDescriptionET = findViewById<EditText>(R.id.et_Timesheet_Description)

        timesheetNameET.setText("")
        timesheetProjectNameET.setText("")
        timesheetStartDateET.setText("")
        timesheetStartTimeET.setText("")
        timesheetEndTimeET.setText("")
        timesheetDescriptionET.setText("")
    }

}
