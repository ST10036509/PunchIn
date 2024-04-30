package za.co.varsitycollege.st10036509.punchin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetCreationBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel

class TimesheetCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimesheetCreationBinding //binds the ActivityTimesheetCreation
    private lateinit var authModel: AuthenticationModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimesheetCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
            btnReturn.setOnClickListener { addTimesheet() }
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
    private fun addTimesheet() {

        binding.apply {
            val timesheetName = etTimesheetName.text.toString().trim()
            val timesheetProjectName = etTimesheetProjectName.text.toString().trim()
            val timesheetStartDate = etTimesheetStartDate
            val timesheetTimer = pbTimer
            val timesheetDescription = etTimesheetDescription.text.toString().trim()
            val timesheetPhoto = etTimesheetPhoto

        }

    }


}