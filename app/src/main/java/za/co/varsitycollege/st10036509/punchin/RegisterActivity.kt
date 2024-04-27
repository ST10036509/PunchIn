/*
AUTHOR: Ethan Schoonbee
CREATED: 26/04/2024
LAST MODIFIED: 26/04/2024
 */

package za.co.varsitycollege.st10036509.punchin


import android.app.ProgressDialog
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityRegisterBinding


/**
 * Class to handle Register Activity Functionality
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding//bind the RegisterActivities KT and XML files
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages
    private lateinit var authModel: AuthenticationViewModel//setup an instance of the authentication context
    private  var progressDialog: ProgressDialog? = null

    //constant strings for toast messages
    private companion object {
        const val MSG_REGISTER_SUCCESS = "Account Registered Successfully!"
        const val MSG_CHECKING_INPUTS = "Validating inputs..."
        const val MSG_REGISTERING_USER = "Registering your account..."
    }

//__________________________________________________________________________________________________onCreate


    /**
     * onCreate Method for Register Activity Page (entry point)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)//inflate UI
        setContentView(binding.root)

        intentHandler = IntentHandler(this@RegisterActivity)//initialise the intentHandler
        authModel = AuthenticationViewModel()//initialise the authentication context

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

            //run appropriate method when buttons are clicked
            llRegisterButton.setOnClickListener { registerUser() }
            llReturnButton.setOnClickListener { openLoginPage() }

            //password toggle onClick listener
            imgTogglePassword.setOnClickListener { togglePasswordVisibility(etPassword, imgTogglePassword) }
            //password toggle onClick listener
            imgTogglePasswordConfirmation.setOnClickListener { togglePasswordVisibility(etPasswordConfirmation, imgTogglePasswordConfirmation) }

        }
    }


//__________________________________________________________________________________________________registerUser


    /**
     * Method to Register the User.
     * If successful launch the TimesheetView page.
     * If unsuccessful show Error Message
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun registerUser() {

        //check if the username is already taken
        GlobalScope.launch {
            binding.apply {

                //capture user inputs
                val username = etUsername.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val passwordConfirmation = etPasswordConfirmation.text.toString().trim()

                withContext(Dispatchers.Main) {

                    showLoadingDialog(MSG_CHECKING_INPUTS)//display a loading dialog

                    val (inputsValid, inputValidationErrorMessage) = authModel.runValidationChecks(
                        username,
                        email,
                        password,
                        passwordConfirmation
                    )

                    dismissLoadingDialog()

                    //check if inputs are all valid
                    if (inputsValid) {

                        showLoadingDialog(MSG_REGISTERING_USER)//display a loading dialog
                        //attempt to sign the user up
                        authModel.signUp(
                            email,
                            password,
                            username,
                            ::handleSignUpCallBack
                        )

                    } else {

                        showToast(inputValidationErrorMessage)

                    }
                }
            }
        }
    }


//__________________________________________________________________________________________________clearInputs


    /**
     * Method to clear inputs text
     */
    private fun clearInputs() {

        binding.apply {

            etUsername.text.clear()
            etEmail.text.clear()
            etPassword.text.clear()
            etPasswordConfirmation.text.clear()

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


//__________________________________________________________________________________________________handleSignUpCallBack


    /**
     * Method to handle callback when an unhandled serverside error occurs
     * @param Pair<Boolean, String>
     */

    private fun handleSignUpCallBack(result: Pair<Boolean, String>) {

        //if there were no errors
        if (result.first) {

            dismissLoadingDialog()//close loading icon
            showToast(RegisterActivity.MSG_REGISTER_SUCCESS)//show success message
            clearInputs()//clear input boxes

            //if if there were no errors
        } else {

            dismissLoadingDialog()//close loading icon
            showToast(result.second)//show given error message

        }
    }


//__________________________________________________________________________________________________showLoadingDialog


    /**
     * Method to show loading dialog
     */
    private fun showLoadingDialog(message: String) {

        progressDialog?.dismiss()

        progressDialog = ProgressDialog(this@RegisterActivity).apply {

            setTitle(message)
            setCancelable(false)
            show()

        }
    }


//__________________________________________________________________________________________________dismissLoadingDialog


    /**
     * Method to discard loading dialog
     */
    private fun dismissLoadingDialog() {

        progressDialog?.dismiss()

        progressDialog = null

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