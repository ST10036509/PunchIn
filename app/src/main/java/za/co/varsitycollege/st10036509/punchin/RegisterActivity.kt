/*
AUTHOR: Ethan Schoonbee
CREATED: 26/04/2024
LAST MODIFIED: 26/04/2024
 */

package za.co.varsitycollege.st10036509.punchin

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityRegisterBinding


/**
 * Class to handle Register Activity Functionality
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding//bind the RegisterActivities KT and XML files
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages

    //constant strings for toast messages
    private companion object {
        const val MSG_REGISTER = "Signing You Up..."
        const val MSG_RETURN = "Return to Login Page..."
    }

//__________________________________________________________________________________________________onCreate


    /**
     * onCreate Method for Register Activity Page (entry point)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)//inflate UI
        setContentView(binding.root)

        intentHandler = IntentHandler(this@RegisterActivity)

        //setup listeners for ui controls
        setupListeners()

    }


//__________________________________________________________________________________________________setupListeners


    /**
     * Method to setup listeners for UI controls
     */
    private fun setupListeners() {

        //apply binding to the following lines
        binding.apply {

            //pass appropriate message to toast
            llRegisterButton.setOnClickListener { showToast(RegisterActivity.MSG_REGISTER) }
            llReturnButton.setOnClickListener { openLoginPage() }

            //password toggle onClick listener
            imgTogglePassword.setOnClickListener { togglePasswordVisibility(etPassword, imgTogglePassword) }
            //password toggle onClick listener
            imgTogglePasswordConfirmation.setOnClickListener { togglePasswordVisibility(etPasswordConfirmation, imgTogglePasswordConfirmation) }

        }

    }


//__________________________________________________________________________________________________openLoginPage


    /**
     * Method to open the Login Page
     */
    private fun openLoginPage() {

        //launch the login page
        intentHandler.openActivityIntent(LoginActivity::class.java)

    }

//__________________________________________________________________________________________________togglePasswordVisibility


    /**
     * On Click Event for the Password Visibility Toggle
     */
    private fun togglePasswordVisibility(inputBox: EditText, toggleButton: ImageView) {

        //apply binding to the following lines
        binding.apply {

            //check if the text is visible
            val isPasswordVisible = inputBox.transformationMethod != PasswordTransformationMethod.getInstance()

            //update visibility and eye icon
            inputBox.transformationMethod =
                    // if the password is visible, show eye open toggle image
                if (isPasswordVisible) PasswordTransformationMethod.getInstance() else null
            toggleButton.setImageResource(
                // if the password is visible, show eye open image else show a eye closed image
                if (isPasswordVisible) R.drawable.eye_closed else R.drawable.eye_open
            )
            // reset cursor position to the end of the string
            inputBox.setSelection(inputBox.text.length)

        }

    }


    //__________________________________________________________________________________________________showToast


    /**
     * Method to show the passed String message via Toast
     * @param String The message to show
     */
    private fun showToast(message: String) {

        Toast.makeText(this, message,
            Toast.LENGTH_SHORT).show()

    }
}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\