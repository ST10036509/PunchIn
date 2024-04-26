/*
AUTHOR: Ethan Schoonbee
CREATED: 23/04/2024
LAST MODIFIED: 23/04/2024
 */

package za.co.varsitycollege.st10036509.punchin

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityLoginBinding


/**
 * Class to handle Login Activity Functionality
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding//bind the LoginActivity KT and XML files
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages

    //constant strings for toast messages
    private companion object {
        const val MSG_SIGN_IN = "Signing You In..."
        const val MSG_REGISTER = "Taking you to Register..."
    }


//__________________________________________________________________________________________________onCreate


    /**
     * onCreate Method for Login Activity Page (entry point)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)//inflate UI
        setContentView(binding.root)

        intentHandler = IntentHandler(this@LoginActivity)

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
            llSignInButton.setOnClickListener { showToast(MSG_SIGN_IN) }
            tvRegisterPrompt.setOnClickListener { openRegisterPage() }

            //password toggle onClick listener
            imgTogglePassword.setOnClickListener { togglePasswordVisibility() }

        }

    }


//__________________________________________________________________________________________________openRegisterPage


    /**
     * Method to open the Register page
     */
    private fun openRegisterPage() {

        //launch the register page
        intentHandler.openActivityIntent(RegisterActivity::class.java)

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

//__________________________________________________________________________________________________togglePasswordVisibility


    /**
     * On Click Event for the Password Visibility Toggle
     */
    private fun togglePasswordVisibility() {

        //apply binding to the following lines
        binding.apply{

                //check if the etPassword is visible
                val isPasswordVisible = etPassword.transformationMethod != PasswordTransformationMethod.getInstance()

                etPassword.transformationMethod =
                    // if the password is visible, show eye open toggle image
                    if (isPasswordVisible) PasswordTransformationMethod.getInstance() else null
                imgTogglePassword.setImageResource(
                    // if the password is visible, show eye open image else show a eye closed image
                    if (isPasswordVisible) R.drawable.eye_closed else R.drawable.eye_open
                )
                // reset cursor position to the end of the string
                etPassword.setSelection(etPassword.text.length)

        }

    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\