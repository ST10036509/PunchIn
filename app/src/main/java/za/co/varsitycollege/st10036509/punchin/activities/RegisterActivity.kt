/*
AUTHOR: Ethan Schoonbee
CREATED: 26/04/2024
LAST MODIFIED: 26/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.activities


import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityRegisterBinding
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.PasswordVisibilityToggler
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler
import za.co.varsitycollege.st10036509.punchin.utils.ValidationHandler


/**
 * Class to handle Register Activity Functionality
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding//bind the RegisterActivities KT and XML files
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages
    private lateinit var authModel: AuthenticationModel//setup an instance of the authentication context
    private lateinit var validationHandler: ValidationHandler//setup an instance of the validation handler
    private lateinit var loadingDialogHandler: LoadDialogHandler//setup an instance of the loading dialog handler
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var passwordVisibilityToggler: PasswordVisibilityToggler//setup an instance of the password visibility handler
    private lateinit var toaster: ToastHandler

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
        validationHandler = ValidationHandler()//initialise the validation handler
        loadingDialogHandler = LoadDialogHandler(this@RegisterActivity, progressDialog)//initialise the loading dialog
        passwordVisibilityToggler = PasswordVisibilityToggler()//initialise the password visibility toggler
        authModel = AuthenticationModel()
        toaster = ToastHandler(this@RegisterActivity)

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
            imgTogglePassword.setOnClickListener { passwordVisibilityToggler.togglePasswordVisibility(binding, etPassword, imgTogglePassword) }
            //password toggle onClick listener
            imgTogglePasswordConfirmation.setOnClickListener { passwordVisibilityToggler.togglePasswordVisibility(binding, etPasswordConfirmation, imgTogglePasswordConfirmation) }

        }
    }


//__________________________________________________________________________________________________registerUser


    /**
     * Method to Register the User.
     * If successful launch the TimesheetView page.
     * If unsuccessful show Error Message
     */
    private fun registerUser() {

        coroutineScope.launch {
            binding.apply {

                //capture user inputs
                val username = etUsername.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val passwordConfirmation = etPasswordConfirmation.text.toString().trim()

                withContext(Dispatchers.Main) {

                    loadingDialogHandler.showLoadingDialog(MSG_CHECKING_INPUTS)//display input checks loading dialog

                    val (inputsValid, inputValidationErrorMessage) =  validationHandler.runValidationChecks(
                            username,
                            email,
                            password,
                            passwordConfirmation
                    )

                    loadingDialogHandler.dismissLoadingDialog()//close loading dialog

                    //check if inputs are all valid
                    if (inputsValid) {

                        loadingDialogHandler.showLoadingDialog(MSG_REGISTERING_USER)//display register loading dialog

                        //attempt to sign the user up
                        authModel.signUp(
                            email,
                            password,
                            username,
                            ::handleSignUpCallBack
                        )
                    } else {
                        toaster.showToast(inputValidationErrorMessage)//display error message
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


//__________________________________________________________________________________________________handleSignUpCallBack


    /**
     * Method to handle callback when an unhandled serverside error occurs
     * @param Pair<Boolean, String>
     */

    private fun handleSignUpCallBack(result: Pair<Boolean, String>) {

        //if there were no errors
        if (result.first) {

            loadingDialogHandler.dismissLoadingDialog()//close loading icon
            toaster.showToast(MSG_REGISTER_SUCCESS)//show success message
            intentHandler.openActivityIntent(TimesheetViewActivity::class.java)//open goals page
            clearInputs()//clear input boxes

            //if if there were no errors
        } else {

            loadingDialogHandler.dismissLoadingDialog()//close loading icon
            toaster.showToast(result.second)//show given error message

        }
    }


//__________________________________________________________________________________________________onDestroy


    /**
     * Method to cancel coroutine when activity is destroyed
     */
    override fun onDestroy() {

        super.onDestroy()

        //cancel coroutine
        coroutineScope.cancel()

    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\