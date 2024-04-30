package za.co.varsitycollege.st10036509.punchin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.type.DateTime
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetCreationBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.TimesheetModel
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler

class TimesheetCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimesheetCreationBinding //binds the ActivityTimesheetCreation
    private lateinit var authModel: AuthenticationModel
    private val firestoreInstance = FirestoreConnection.getDatabaseInstance()
    private val authInstance: FirebaseAuth by lazy { FirebaseAuth.getInstance() }//link an instance of the firebase authentication sdk
    private lateinit var intentHandler: IntentHandler

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

        intentHandler = IntentHandler(this@TimesheetCreationActivity)
        //setup listeners for ui controls
        setupListeners()

    }

    override fun onStart() {

        super.onStart()

        authModel = AuthenticationModel()
        val currentUser = authModel.getCurrentUser()

        currentUser?.reload()?.addOnCompleteListener() { task ->

            if (task.isSuccessful) {
                //intentHandler.openActivityIntent(HomePage)

            } else {
                authModel.signOut()
            }
        }
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

        binding.apply {
            val timesheetUser = authModel.getCurrentUser().toString()
            val timesheetName = etTimesheetName.text.toString().trim()
            val timesheetProjectName = etTimesheetProjectName.text.toString().trim()
            val timesheetStartDate = etTimesheetStartDate.text.toString().trim()
            val timesheetStartTime = etTimesheetStartTime.text.toString().trim()
            val timesheetEndTime = etTimesheetEndTime.text.toString().trim()
            val timesheetDescription = etTimesheetDescription.text.toString().trim()
            //val timesheetPhoto = ivTimesheetImage

            addTimesheet(
                timesheetUser,
                timesheetName,
                timesheetProjectName,
                timesheetStartDate,
                timesheetStartTime,
                timesheetEndTime,
                timesheetDescription,
                //timesheetPhoto
            )
        }


    }


    fun addTimesheet(
        timesheetUser: String,
        timesheetName: String,
        timesheetProjectName: String,
        timesheetStartDate: String,
        timesheetStartTime: String,
        timesheetEndTime: String,
        timesheetDescription: String,
        //timesheetPhoto: String
        ) {

        //get current user
        val user = authInstance.currentUser
        //if the user exists
        user?.let {

            //create timesheet context and assign default values
            val timesheetModelData = TimesheetModel(timesheetUser, timesheetName, timesheetProjectName, timesheetStartDate, timesheetStartTime, timesheetEndTime, timesheetDescription)
            //save the timesheet data to the database
            saveTimesheetDataToFirestore(user.uid, timesheetModelData) { success ->

                if (success) { }
                else { }

            }

        }
    }

    private fun saveTimesheetDataToFirestore(tid: String, timesheetModelData: TimesheetModel, callback: (Boolean) -> Unit) {

        //open the database connection and find/create the timesheet collection
        firestoreInstance.collection("timesheets")
            .document(tid) //create a new document with timesheet tid
            .set(timesheetModelData) //write the timesheet data
            .addOnSuccessListener { callback(true) } //if the data is successfully added
            .addOnFailureListener { callback(false) } //if an error occurs while adding the extra user data
    }
}