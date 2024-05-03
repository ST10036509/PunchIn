/*
AUTHOR: Ethan Schoonbee
CREATED: 02/05/2024
LAST MODIFIED: 02/05/2024
 */


package za.co.varsitycollege.st10036509.punchin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivitySettingsBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.UserModel
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding//bind the GoalsActivity KT and XML files
    private lateinit var authModel: AuthenticationModel//get the instance of the authentication model
    private lateinit var toaster: ToastHandler//create a ToastHandler to show toast messages
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)//inflate UI
        setContentView(binding.root)

        authModel = AuthenticationModel()//get the instance of the authentication model
        toaster = ToastHandler(this@SettingsActivity)//create a ToastHandler to show toast messages
        intentHandler = IntentHandler(this@SettingsActivity)//setup an intent handler for navigating pages

        binding.tvUsername.text = UserModel.username

        //setup listeners for ui controls
        setupListeners()
    }


//__________________________________________________________________________________________________setupClickListeners


    /**
     * Method to setup listeners for UI controls
     */
    private fun setupListeners() {

        //apply binding to the following lines
        binding.apply {

            //pass appropriate function when onClick occurs
            llSignOutButton.setOnClickListener { signOutUser() }
            llReturnButton.setOnClickListener { returnToHomePage() }

        }
    }


//__________________________________________________________________________________________________signOutUser


    private fun signOutUser() {

        //sign the user out of their account
        authModel.signOut()
        //launch the register page
        intentHandler.openActivityIntent(LoginActivity::class.java)

    }


//__________________________________________________________________________________________________returnToHomePage


    private fun returnToHomePage() {

        //launch the register page
        intentHandler.openActivityIntent(TimesheetViewActivity::class.java)

    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\