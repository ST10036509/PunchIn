/*
AUTHOR: Ethan Schoonbee
CREATED: 23/04/2024
LAST MODIFIED: 23/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.activities

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityLoginBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.TimesheetModel
import za.co.varsitycollege.st10036509.punchin.models.UserModel
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.PasswordVisibilityToggler
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler
import za.co.varsitycollege.st10036509.punchin.utils.ValidationHandler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.exp
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.roundToInt


/**
 * Class to handle Login Activity Functionality
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding//bind the LoginActivity KT and XML files
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages
    private lateinit var passwordVisibilityToggler: PasswordVisibilityToggler//setup an instance of the password visibility handler
    private lateinit var authModel: AuthenticationModel
    private var firestoreInstance = FirestoreConnection.getDatabaseInstance()
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private lateinit var loadingDialogHandler: LoadDialogHandler//setup an intent handler for navigating pages
    private lateinit var validationHandler: ValidationHandler
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var toaster: ToastHandler

    //constant strings for toast messages
    private companion object {
        const val MSG_LOGIN_SUCCESS = "Account Logged In Successfully!"
        const val MSG_CHECKING_INPUTS = "Validating inputs..."
        const val MSG_LOGIN_IN_USER = "Logging you in..."
        const val MSG_NULL_INPUTS_ERROR = "Please fill out all inputs!"
        const val MSG_NULL = ""
        const val MSG_CORRUPT_ACCOUNT_DATA = "Your account data is corrupted. Please make a new account!"
        const val MAX_POINTS = 100
        const val DECAY_VALUE = 1
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
        toaster = ToastHandler(this@LoginActivity)
        authModel = AuthenticationModel()

        //setup listeners for ui controls
        setupListeners()

    }



//__________________________________________________________________________________________________onStart


    /**
     * Method to sign a user out when the page first starts if their token is expired
     */
    override fun onStart() {

        super.onStart()

        loadingDialogHandler.showLoadingDialog(LoginActivity.MSG_NULL)

        val currentUser = authModel.getCurrentUser()

        if (currentUser != null) {

            currentUser.reload().addOnCompleteListener() { task ->

                if (task.isSuccessful) {

                    UserModel.fetchUserDataFromFireStore(currentUser.uid.toString()) { success ->

                        if(success) {

                            updateGoalsScore()
                            intentHandler.openActivityIntent(TimesheetViewActivity::class.java)

                        } else {

                            handleAccountCorruption()

                        }
                    }
                } else {

                    handleTokenExpiration()

                }
            }
        } else {

            loadingDialogHandler.dismissLoadingDialog()

        }
    }


    fun handleAccountCorruption() {

        toaster.showToast(LoginActivity.MSG_CORRUPT_ACCOUNT_DATA)
        authModel.signOut(){ success ->
            if (success) {
                toaster.showToast(LoginActivity.MSG_NULL)
            }
        }
        loadingDialogHandler.dismissLoadingDialog()

    }


//__________________________________________________________________________________________________setupListeners


    fun handleTokenExpiration() {

        authModel.signOut(){ success ->
            if (success) {
                toaster.showToast(LoginActivity.MSG_NULL)
            }
        }
        loadingDialogHandler.dismissLoadingDialog()

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
                        toaster.showToast(LoginActivity.MSG_NULL_INPUTS_ERROR)//display error message
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
            updateGoalsScore()
            toaster.showToast(LoginActivity.MSG_LOGIN_SUCCESS)//show success message
            intentHandler.openActivityIntent(TimesheetViewActivity::class.java)//open goals page
            clearInputs()//clear input boxes

        //if if there were no errors
        } else {

            loadingDialogHandler.dismissLoadingDialog()//close loading icon
            toaster.showToast(result.second)//show given error message

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

//__________________________________________________________________________________________________updateGoalsScore


    /**
     * Method to update score if applicable
     */
    private fun updateGoalsScore() {

        var currentUser = authModel.getCurrentUser()

        //get all the users timsheets >> list
        fetchAllUserTimesheets(currentUser) { usersTimesheets ->
            //fetch all user's timesheets that have a false check sum (if they exists) >> list
            fetchUncheckedTimesheets(currentUser) { uncheckedTimesheets: List<TimesheetModel> ->
                if (uncheckedTimesheets.isNotEmpty()) {
                    //group the timesheets by day >> mapped list and string
                    val usersTimesheetsByDay = groupTimesheetsByDay(usersTimesheets)
                    //group the timesheets by day >> mapped list and string
                    val uncheckedTimesheetsByDay = groupTimesheetsByDay(uncheckedTimesheets)
                    //calculate the total hours of work done on each  day and equate to points total (+/-)
                    calculateRunningTotalPoints(uncheckedTimesheetsByDay, usersTimesheetsByDay) { runningTotalPoints ->
                        //update the user score if needed (if the calc points is 0 then nothing needs to happen)
                        updateUserScore(currentUser, runningTotalPoints, uncheckedTimesheets)
                    }
                }
            }
        }
    }


//__________________________________________________________________________________________________fetchUncheckedTimesheets


    /**
     * Method to fetch all user related timesheets that have not been checked for points
     */
    private fun fetchUncheckedTimesheets(currentUser: FirebaseUser?, callback: (List<TimesheetModel>) -> Unit) {

        val currentStartOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        firestoreInstance.collection("timesheets")
            .whereEqualTo("userId", currentUser?.uid)
            .whereEqualTo("checkSum", false)
            .get()
            .addOnSuccessListener { documents ->
                val timesheets = documents.map { document ->
                    document.toObject(TimesheetModel::class.java)
                }.filter { timesheets ->
                    timesheets.timesheetStartDate?.before(currentStartOfDay) == true
                }
                callback(timesheets)
            }
            .addOnFailureListener {exception ->
                exception.printStackTrace()
                callback(emptyList())
            }
    }


    //__________________________________________________________________________________________________fetchAllUserTimesheets


    /**
     * Method to fetch all user related timesheets
     */
    private fun fetchAllUserTimesheets(currentUser: FirebaseUser?, callback: (List<TimesheetModel>) -> Unit) {

        if (currentUser == null) {
            callback(emptyList())
        }

        val timesheets = mutableListOf<TimesheetModel>()

        firestoreInstance.collection("timesheets")
            .whereEqualTo("userId", currentUser?.uid)
            .whereEqualTo("checkSum", true)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val timesheet = document.toObject(TimesheetModel::class.java)
                    timesheets.add(timesheet)
                }
                callback(timesheets)
            }
            .addOnFailureListener {exception ->
                exception.printStackTrace()
                callback(emptyList())
            }
    }

//__________________________________________________________________________________________________groupTimesheetsByDay


    /**
     * Method to group the users unchecked timesheets by date
     */
    private fun groupTimesheetsByDay(timesheets: List<TimesheetModel>): Map<String, List<TimesheetModel>> {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        return timesheets.groupBy { timesheet ->
            dateFormat.format(timesheet.timesheetStartDate)
        }
    }


//__________________________________________________________________________________________________calculateRunningTotalPoints


    /**
     * Method to calculate the total points to be added/deducted from the users account
     */
    private fun calculateRunningTotalPoints(
        uncheckedTimesheetsByDay: Map<String, List<TimesheetModel>>,
        userTimesheetsByDay: Map<String, List<TimesheetModel>>,
        callback: (Int) -> Unit
    ) {
        val currentUser = authModel.getCurrentUser()
        if (currentUser == null) {
            callback(0)
            return
        }

        UserModel.fetchUserDataFromFireStore(currentUser.uid) { success ->
            if (success) {
                var runningTotalPoints = UserModel.points
                val minGoal = UserModel.minGoal.toDouble()
                val maxGoal = UserModel.maxGoal.toDouble()

                for ((date, timesheets) in uncheckedTimesheetsByDay) {
                    // Calculate the total hours worked from unchecked timesheets
                    val newTotalHoursWorked = getTotalHoursWorked(timesheets)

                    // Calculate the current total hours worked for that day from userTimesheetsByDay
                    val currentTotalHoursWorked = userTimesheetsByDay[date]?.let { getTotalHoursWorked(it) } ?: 0.0

                    // Combine the new hours with the current hours
                    val combinedTotalHoursWorked = newTotalHoursWorked + currentTotalHoursWorked

                    val dailyPoints = if (combinedTotalHoursWorked in minGoal..maxGoal) {
                        allocatePoints(combinedTotalHoursWorked, minGoal, maxGoal)
                    } else {
                        deductPoints(combinedTotalHoursWorked)
                    }

                    runningTotalPoints += dailyPoints
                }

                // Return the calculated points through the callback
                callback(runningTotalPoints)
            } else {
                toaster.showToast("There was an error updating your account data!")
                callback(0)
            }
        }
    }


//__________________________________________________________________________________________________getTotalHoursWorked


    /**
     * Method to get total hours worked for an set of timesheets
     */
    private fun getTotalHoursWorked(timesheets: List<TimesheetModel>): Double {

        var totalHoursWorked = 0.0

        for (timesheet in timesheets) {
            val durationMillis = (timesheet.timesheetEndTime!!.time - timesheet.timesheetStartTime!!.time).absoluteValue
            val hoursWorked = durationMillis.toDouble() / (1000 * 60 * 60)
            totalHoursWorked += hoursWorked
        }

        return totalHoursWorked.roundToDecimalPlaces(3)
    }


//__________________________________________________________________________________________________roundToDecimalPlaces


    /**
     * Method to round given Double value to a certain number of decimal places
     */
    fun Double.roundToDecimalPlaces(decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces)
        return (this * factor) / factor
    }


//__________________________________________________________________________________________________allocatePoints


    /**
     * Method to add add find out how many points to add to the users account
     */
    private fun allocatePoints(totalHoursWorked: Double, minGoal: Double, maxGoal: Double): Int {

        //find midpoint of number set (numbers between min and max - inclusive)
        var midpoint =  (minGoal + maxGoal) / 2.0

        //find hours worked value distance from midpoint
        var normalisedHours = (totalHoursWorked - midpoint) / (maxGoal - midpoint)

        //ensure distance is positive
        var distance = abs(normalisedHours)

        //exponential decay function to calculate points based on a maximum and a decay rate
        var points = (MAX_POINTS * exp(-DECAY_VALUE * distance)).roundToInt()

        toaster.showToast("You've gotten $points points while you were away!")

        return points
    }


//__________________________________________________________________________________________________deductPoints


    /**
     * Method to add add find out how many points to deduct from the users account
     */
    private fun deductPoints(totalHoursWorked: Double): Int {

        val pointsToDeduct = -(MAX_POINTS / 2)

        if ((UserModel.points + pointsToDeduct) <= 0)
        {
            return -(UserModel.points)
        } else {
            return pointsToDeduct
        }

    }


//__________________________________________________________________________________________________updateUserScore


    /**
     * Method to update the users points in the database
     */
    private fun updateUserScore(currentUser: FirebaseUser?, runningTotalPoints: Int, timesheets: List<TimesheetModel>) {

        if (currentUser == null) {
            toaster.showToast("No current user found")
            return
        }

        val batch = firestoreInstance.batch()
        var tasksCompleted = 0
        val totalTasks = timesheets.size

        for (timesheet in timesheets) {

            firestoreInstance.collection("timesheets")
                .whereEqualTo("userId", timesheet.userId)
                .whereEqualTo("timesheetStartDate", timesheet.timesheetStartDate)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        batch.update(document.reference, "checkSum", true)
                    }
                    tasksCompleted++
                    if (tasksCompleted == totalTasks) {
                        updateUserPointsDb(currentUser, runningTotalPoints, batch)
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    tasksCompleted++
                    if (tasksCompleted == totalTasks) {
                        updateUserPointsDb(currentUser, runningTotalPoints, batch)
                    }
                }
        }
    }


//__________________________________________________________________________________________________updateUserPointsDb


    private fun updateUserPointsDb(currentUser: FirebaseUser?, runningTotalPoints: Int, batch: WriteBatch) {

        val userRef = firestoreInstance.collection("users").document(currentUser?.uid.toString())
        batch.update(userRef, "points", runningTotalPoints)

        batch.commit()
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                toaster.showToast("Batch failed to update")
            }
    }
}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\