package za.co.varsitycollege.st10036509.punchin.activities

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.type.DateTime
import za.co.varsitycollege.st10036509.punchin.R
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

        val timesheetNameET = findViewById<EditText>(R.id.et_Timesheet_Name)
        val timesheetProjectNameET = findViewById<EditText>(R.id.et_Timesheet_ProjectName)
        val timesheetStartDateET = findViewById<EditText>(R.id.et_Timesheet_StartDate)
        val timesheetStartTimeET = findViewById<EditText>(R.id.et_Timesheet_Start_Time)
        val timesheetEndTimeET = findViewById<EditText>(R.id.et_Timesheet_End_Time)
        val timesheetDescriptionET = findViewById<EditText>(R.id.et_Timesheet_Description)
        //photo

        val timesheetUser = authModel.getCurrentUser().toString()
        val timesheetName = timesheetNameET.text.toString().trim()
        val timesheetProjectName = timesheetProjectNameET.text.toString().trim()
        val timesheetStartDate = timesheetStartDateET.text.toString().trim()
        val timesheetStartTime = timesheetStartTimeET.text.toString().trim()
        val timesheetEndTime = timesheetEndTimeET.text.toString().trim()
        val timesheetDescription = timesheetDescriptionET.text.toString().trim()
        //val timesheetPhoto = ivTimesheetImage

        // Define the Firestore collection
        val collection = firestoreInstance.collection("timesheets")

        //store data in database
        val timesheetData = hashMapOf(
            "timesheetUser" to timesheetUser,
            "timesheetName" to timesheetName,
            "timesheetProjectName" to timesheetProjectName,
            "timesheetStartDate" to timesheetStartDate,
            "timesheetStartTime" to timesheetStartTime,
            "timesheetEndTime" to timesheetEndTime,
            "timesheetDescription" to timesheetDescription
            //"timesheetPhoto" to timesheetPhoto
        )

        //add timesheet data to database
        collection.add(timesheetData)
            .addOnSuccessListener { documentReference ->
                //data successfully stored
                Log.d("ProjectCreationActivity", "Timesheet data stored successfully. Document ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Error storing data in Firestore
                Log.e("ProjectCreationActivity", "Error storing timesheet data: $e")
            }

        retrieveDataFromFirestore()
    }

    private fun retrieveDataFromFirestore() {
        //Define the firestore collection
        val collection = firestoreInstance.collection("timesheets")

        //retrieve data from firestore
        collection.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    //log each documents data
                    Log.d("TimesheetCreationActivity", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { e ->
                //Error retrieving data
                Log.e("TimesheetCreationActivity", "Error retrieving project data: $e")
            }
    }

}
