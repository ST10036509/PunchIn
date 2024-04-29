/*
AUTHOR: Ethan Schoonbee
CREATED: 23/04/2024
LAST MODIFIED: 23/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityLoginBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.PasswordVisibilityToggler
import za.co.varsitycollege.st10036509.punchin.utils.ValidationHandler


/**
 * Class to handle Login Activity Functionality
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding//bind the LoginActivity KT and XML files
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages
    private lateinit var passwordVisibilityToggler: PasswordVisibilityToggler//setup an instance of the password visibility handler
    private lateinit var authModel: AuthenticationModel
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private lateinit var loadingDialogHandler: LoadDialogHandler
    private lateinit var validationHandler: ValidationHandler
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    //constant strings for toast messages
    private companion object {
        const val MSG_LOGIN_SUCCESS = "Account Logged In Successfully!"
        const val MSG_LOGIN_FAILED = "Log In Failed Unexpectedly!"
        const val MSG_CHECKING_INPUTS = "Validating inputs..."
        const val MSG_LOGIN_IN_USER = "Logging you in..."
        const val MSG_NULL_INPUTS_ERROR = "Please fill out all inputs!"
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
        validationHandler = ValidationHandler()//initialise the validation handler
        loadingDialogHandler = LoadDialogHandler(this@LoginActivity, progressDialog)//initialise the loading dialog


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
            llSignInButton.setOnClickListener { loginUser() }
            tvRegisterPrompt.setOnClickListener { openRegisterPage() }

            //password toggle onClick listener
            imgTogglePassword.setOnClickListener { passwordVisibilityToggler.togglePasswordVisibility(binding, etPassword, imgTogglePassword) }

        }
    }


//__________________________________________________________________________________________________loginUser


    /**
     * Method to Log In the User.
     * If successful launch the TimesheetView page.
     * If unsuccessful show Error Message
     */
    private fun loginUser() {

        coroutineScope.launch {
            binding.apply {

                //capture user inputs
                val emailOrUsername = etEmailOrUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()

                withContext(Dispatchers.Main) {

                    loadingDialogHandler.showLoadingDialog(LoginActivity.MSG_CHECKING_INPUTS)//display input checks loading dialog

                    val inputsValid = validationHandler.checkForNullInputs(emailOrUsername, password)

                    loadingDialogHandler.dismissLoadingDialog()//close loading dialog

                    //check if inputs are all valid
                    if (!inputsValid) {

                        loadingDialogHandler.showLoadingDialog(LoginActivity.MSG_LOGIN_IN_USER)//display register loading dialog

                        //attempt to sign the user up
                        authModel.signIn(
                            emailOrUsername,
                            password,
                            ::handleSignInCallBack
                        )

                    } else {
                        showToast(LoginActivity.MSG_NULL_INPUTS_ERROR)//display error message
                    }
                }

            }
        }

    }


//__________________________________________________________________________________________________handleSignInCallBack


    /**
     * Method to handle callback when an unhandled serverside error occurs
     * @param Pair<Boolean, String>
     */

    private fun handleSignInCallBack(result: Pair<Boolean, String>) {

        //if there were no errors
        if (result.first) {

            loadingDialogHandler.dismissLoadingDialog()//close loading icon
            showToast(LoginActivity.MSG_LOGIN_SUCCESS)//show success message
            clearInputs()//clear input boxes

        //if if there were no errors
        } else {

            loadingDialogHandler.dismissLoadingDialog()//close loading icon
            showToast(result.second)//show given error message

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


//__________________________________________________________________________________________________clearInputs


    /**
     * Method to clear inputs text
     */
    private fun clearInputs() {

        binding.apply {

            etEmailOrUsername.text.clear()
            etPassword.text.clear()

        }
    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\