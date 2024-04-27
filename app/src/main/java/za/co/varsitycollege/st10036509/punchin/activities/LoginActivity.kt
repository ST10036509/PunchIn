/*
AUTHOR: Ethan Schoonbee
CREATED: 23/04/2024
LAST MODIFIED: 23/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.activities

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityLoginBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.utils.PasswordVisibilityToggler


/**
 * Class to handle Login Activity Functionality
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding//bind the LoginActivity KT and XML files
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages
    private lateinit var passwordVisibilityToggler: PasswordVisibilityToggler//setup an instance of the password visibility handler
    private lateinit var authModel: AuthenticationModel

    //constant strings for toast messages
    private companion object {
        const val MSG_SIGN_IN = "Signing You In..."
    }


//__________________________________________________________________________________________________onCreate


    /**
     * onCreate Method for Login Activity Page (entry point)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)//inflate UI
        setContentView(binding.root)

        intentHandler = IntentHandler(this@LoginActivity)//setup an intent handler for navigating pages
        passwordVisibilityToggler = PasswordVisibilityToggler()//initialise the password visibility toggler

        //setup listeners for ui controls
        setupListeners()

    }



//__________________________________________________________________________________________________onStart


    /**
     * Method to sign a user out when the page first starts if their token is expired
     */
    override fun onStart() {

        super.onStart()

        authModel = AuthenticationModel()
        val currentUser = authModel.getCurrentUser()

        currentUser?.reload()?.addOnCompleteListener() { task ->

            if (task.isSuccessful) {
                //intentHandler.openActivityIntent(HomePage)
                Toast.makeText(this@LoginActivity, currentUser.uid.toString(), Toast.LENGTH_SHORT).show()

            } else {
                authModel.signOut()
            }
        }
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
            imgTogglePassword.setOnClickListener { passwordVisibilityToggler.togglePasswordVisibility(binding, etPassword, imgTogglePassword) }

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