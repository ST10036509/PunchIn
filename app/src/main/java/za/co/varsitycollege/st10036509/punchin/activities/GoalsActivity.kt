/*
AUTHOR: Ethan Schoonbee
CREATED: 23/04/2024
LAST MODIFIED: 25/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.utils.NavbarViewBindingHelper
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityGoalsBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.TimesheetModel
import za.co.varsitycollege.st10036509.punchin.models.UserModel
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler
import java.util.Calendar
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Class to handle Goals Activity Functionality
 */
class GoalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalsBinding//bind the GoalsActivity KT and XML files

    private lateinit var navbarHelper: NavbarViewBindingHelper//create a NavBarViewBindingsHelper class object
    private var authModel = AuthenticationModel()//get the instance of the authentication model
    private var firestoreInstance = FirestoreConnection.getDatabaseInstance()
    private lateinit var toaster: ToastHandler//create a ToastHandler to show toast messages
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private lateinit var loadingDialogHandler: LoadDialogHandler//setup an intent handler for navigating pages

    private var currentUser: FirebaseUser? = null//variable for storing current user
    private var displayedMinimumGoal = UserModel.minGoal//variable to hold the currently displayed user Minimum Goal
    private var displayedMaximumGoal = UserModel.maxGoal//variable to hold the currently displayed user Maximum Goal
    private var timesheets: MutableList<TimesheetModel> = mutableListOf()//declare an array to hold user related timesheets

    //constants for app runtime
    private companion object {
        const val MSG_UPDATE_GOALS_SUCCESS = "Updated Goals Successfully!"
        const val MSG_MAX_HOURS_IN_A_DAY = "Love your enthusiasm! However, there are no more hours in the day!"
        const val MSG_PREPARING_PAGE = "Preparing the page..."
        const val MSG_UPDATING_GOALS = "Updating your goals..."
        const val MSG_UPDATE_GOALS_ERROR = "Failed to update your goals. Please Try again..."
        const val DELAY_BEFORE_DISMISS_LOADING_DIALOG = 500L
        const val MSG_NO_TIMESHEETS_ERROR = "No timesheets found for today!"
    }


//__________________________________________________________________________________________________onCreate


    /**
     * onCreate Method for Login Activity Page (entry point)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)//inflate UI
        setContentView(binding.root)

        //||NAVBAR INITIALIZATION||
        //initialize an instance of the NavBarHelper and pass in the current context and binding
        navbarHelper = NavbarViewBindingHelper(this@GoalsActivity, binding)
        //setup listeners for NavBar onClick events
        navbarHelper.setUpNavBarOnClickEvents()
        //||NAVBAR INITIALIZATION||

        toaster = ToastHandler(this@GoalsActivity)//initialise the toast handler
        loadingDialogHandler = LoadDialogHandler(this@GoalsActivity, progressDialog)//initialise the loading dialog
        currentUser = authModel.getCurrentUser()//get current user form authentication model
        loadingDialogHandler.showLoadingDialog(GoalsActivity.MSG_PREPARING_PAGE)//show loading dialog

        //prepare page asynchronously
        preparePage()

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
            imgbMinIncrement.setOnClickListener { increaseMinimumGoal() }
            imgbMinDecrement.setOnClickListener { decreaseMinimumGoal() }
            imgbMaxIncrement.setOnClickListener { increaseMaximumGoal() }
            imgbMaxDecrement.setOnClickListener { decreaseMaximumGoal() }
            llUpdateButton.setOnClickListener {

                loadingDialogHandler.showLoadingDialog(GoalsActivity.MSG_UPDATING_GOALS)

                updateUserGoals() { success ->

                    if (success) {

                        decoratePageWithUserData()

                        loadingDialogHandler.dismissLoadingDialog()

                        toaster.showToast(GoalsActivity.MSG_UPDATE_GOALS_SUCCESS)

                    } else {

                        loadingDialogHandler.dismissLoadingDialog()
                        toaster.showToast(GoalsActivity.MSG_UPDATE_GOALS_ERROR)

                    }
                }
            }
        }
    }


//__________________________________________________________________________________________________preparePage


    private fun preparePage() {

        Handler(Looper.getMainLooper()).postDelayed({

            //update display to show user related data
            decoratePageWithNewUserData()

            loadingDialogHandler.dismissLoadingDialog()
        }, DELAY_BEFORE_DISMISS_LOADING_DIALOG)
    }


//__________________________________________________________________________________________________decoratePageWithUserData


    private fun decoratePageWithNewUserData() {

        binding.apply {

            updateGoalsProgressBarWithNewData()

            tvMinimumGoalHours.text = UserModel.minGoal.toString()
            tvMaximumGoalHours.text = UserModel.maxGoal.toString()
            tvLeftGoalDisplay.text = "${UserModel.minGoal.toString()} HRS"
            tvRightGoalDisplay.text = "${UserModel.maxGoal.toString()} HRS"

            tvMinimumGoalHours.setTextColor(this@GoalsActivity.getColor(R.color.blue_gray_50))
            tvMaximumGoalHours.setTextColor(this@GoalsActivity.getColor(R.color.blue_gray_50))

        }

        loadingDialogHandler.dismissLoadingDialog()
    }


//__________________________________________________________________________________________________decoratePageWithUserData


    private fun decoratePageWithUserData() {

        binding.apply {

            updateGoalsProgressBar()

            tvMinimumGoalHours.text = UserModel.minGoal.toString()
            tvMaximumGoalHours.text = UserModel.maxGoal.toString()
            tvLeftGoalDisplay.text = "${UserModel.minGoal.toString()} HRS"
            tvRightGoalDisplay.text = "${UserModel.maxGoal.toString()} HRS"

            tvMinimumGoalHours.setTextColor(this@GoalsActivity.getColor(R.color.blue_gray_50))
            tvMaximumGoalHours.setTextColor(this@GoalsActivity.getColor(R.color.blue_gray_50))

        }

        loadingDialogHandler.dismissLoadingDialog()
    }


//__________________________________________________________________________________________________updateGoalsProgressBar


    private fun updateGoalsProgressBarWithNewData() {

        getUserRelatedTimesheets() { success ->

            if (success){

                getProgressByHours()

            } else {

                toaster.showToast(GoalsActivity.MSG_NO_TIMESHEETS_ERROR)

            }
        }
    }


//__________________________________________________________________________________________________updateGoalsProgressBar


    private fun updateGoalsProgressBar() {

        getProgressByHours()

    }


//__________________________________________________________________________________________________getUserRelatedTimesheets


    private fun getUserRelatedTimesheets(callback: (Boolean) -> Unit) {

        val timesheetCollection = firestoreInstance.collection("timesheets")
        val currentUid = currentUser?.uid.toString()

        val startOfDay = Calendar.getInstance().apply {

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

        }

        val endOfDay = Calendar.getInstance().apply {

            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)

        }

        val startTimestamp = Timestamp(startOfDay.timeInMillis / 1000, 0)
        val endTimestamp = Timestamp(endOfDay.timeInMillis / 1000, 999000000)

        timesheetCollection.whereEqualTo("userId", currentUid)
            .whereGreaterThanOrEqualTo("timesheetStartDate", startTimestamp)
            .whereLessThanOrEqualTo("timesheetStartDate", endTimestamp)
            .get()
            .addOnSuccessListener { querySnapshot ->

                if (!querySnapshot.isEmpty) {

                    for (document in querySnapshot){
                        val userId = document.getString("userId") ?:""
                        val timesheetName = document.getString("timesheetName") ?:""
                        val projectId = document.getString("projectId") ?:""
                        val timesheetStartDate = document.getDate("timesheetStartDate")
                        val timesheetStartTime = document.getDate("timesheetStartTime")
                        val timesheetEndTime = document.getDate("timesheetEndTime")
                        val timesheetDescription = document.getString("timesheetDescription") ?:""

                        val newTimesheet = TimesheetModel(
                            userId,
                            timesheetName,
                            projectId,
                            timesheetStartDate,
                            timesheetStartTime,
                            timesheetEndTime,
                            timesheetDescription,
                            timesheetPhoto = null
                        )

                        timesheets.add(newTimesheet)
                    }

                    callback(true)
                } else {

                    callback(false)

                }
            }
            .addOnFailureListener {exception ->

                println(exception.message.toString())

                callback(false)

            }
    }


//__________________________________________________________________________________________________getTotalHoursWorked


    private fun getTotalHoursWorked(): Double {

        var totalHoursWorked = 0.0

        for (timesheet in timesheets) {

            val durationMillis = (timesheet.timesheetEndTime!!.time - timesheet.timesheetStartTime!!.time).absoluteValue
            val hoursWorked = durationMillis.toDouble() / (1000 * 60 * 60)
            totalHoursWorked += hoursWorked

        }

        return totalHoursWorked.roundToDecimalPlaces(3)

    }


    fun Double.roundToDecimalPlaces(decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces)
        return (this * factor).roundToInt() / factor
    }

//__________________________________________________________________________________________________getProgressByHours


    private fun getProgressByHours() {

        val totalHoursWorked = getTotalHoursWorked()
        val minGoal = UserModel.minGoal
        val maxGoal = UserModel.maxGoal

        val progressPercentage = when {
            totalHoursWorked <= minGoal -> 0f
            totalHoursWorked >= maxGoal -> 100f
            else -> calculatePercentageOfWorkedHours(minGoal, maxGoal, totalHoursWorked).toFloat()
        }

        binding.pbProgressBarTracker.progress = Math.round(progressPercentage)


        updateHoursWorkedText(totalHoursWorked, minGoal, maxGoal)

    }


//__________________________________________________________________________________________________calculatePercentageOfWorkedHours


    private fun calculatePercentageOfWorkedHours(min: Int, max: Int, hoursWorked: Double): Double {

        val range = max - min

        val hoursExcess = hoursWorked - min

        val percentage = (hoursExcess.toDouble() / range.toDouble()) * 100.0

        return percentage
    }


//__________________________________________________________________________________________________updateMinimumGoalsText


    private fun updateMinimumGoalsText() {

        binding.apply {

            tvMinimumGoalHours.text = displayedMinimumGoal.toString()
            updateTextColor()

        }
    }


//__________________________________________________________________________________________________updateMaximumGoalsText


    private fun updateMaximumGoalsText() {

        binding.apply {

            tvMaximumGoalHours.text = displayedMaximumGoal.toString()
            updateTextColor()

        }
    }


//__________________________________________________________________________________________________updateMaximumGoalsText


    private fun updateHoursWorkedText(totalHours: Double, minGoal: Int, maxGoal: Int) {

        val context = this@GoalsActivity
        val textColor: Int

        binding.apply {

            tvHoursWorked.text = "${(totalHours.toInt()).toString()} HRS"

            if (totalHours > maxGoal || totalHours < minGoal) {

                textColor = context.getColor(R.color.red_300)
                tvHoursWorked.setTextColor(textColor)

            } else if (totalHours <= maxGoal || totalHours >= minGoal) {

                textColor = context.getColor(R.color.green_200)
                tvHoursWorked.setTextColor(textColor)

            } else {

                textColor = context.getColor(R.color.dark_blue_900)
                tvHoursWorked.setTextColor(textColor)

            }

        }
    }


//__________________________________________________________________________________________________updateTextColor


    private fun updateTextColor() {

        val totalHours = displayedMinimumGoal + displayedMaximumGoal
        val context = this@GoalsActivity
        val textColor: Int

        if (totalHours == 24) {

            textColor = context.getColor(R.color.red_300)
            toaster.showToast(GoalsActivity.MSG_MAX_HOURS_IN_A_DAY)


        } else {

            textColor = context.getColor(R.color.blue_gray_50)

        }

        binding.apply {

            tvMinimumGoalHours.setTextColor(textColor)
            tvMaximumGoalHours.setTextColor(textColor)

        }
    }


//__________________________________________________________________________________________________increaseMinimumGoal


    private fun increaseMinimumGoal() {

        binding.apply {

            if ((displayedMinimumGoal + displayedMaximumGoal) < 24 && displayedMinimumGoal < displayedMaximumGoal) {
                displayedMinimumGoal++
                updateMinimumGoalsText()
            }
        }
    }


//__________________________________________________________________________________________________decreaseMinimumGoal


    private fun decreaseMinimumGoal() {

        binding.apply {

            if (displayedMinimumGoal > 0) {
                displayedMinimumGoal--
                updateMinimumGoalsText()
            }
        }
    }


//__________________________________________________________________________________________________increaseMaximumGoal


    private fun increaseMaximumGoal() {

        binding.apply {

            if ((displayedMaximumGoal + displayedMinimumGoal) < 24) {
                displayedMaximumGoal++
                updateMaximumGoalsText()
            }
        }

    }


//__________________________________________________________________________________________________decreaseMaximumGoal


    private fun decreaseMaximumGoal() {

        binding.apply {

            if (displayedMaximumGoal > 1 && displayedMaximumGoal > displayedMinimumGoal) {
                displayedMaximumGoal--
                updateMaximumGoalsText()
            }
        }
    }


//__________________________________________________________________________________________________updateUserGoals


    private fun updateUserGoals(callback:  (Boolean) -> Unit) {

        val uid = currentUser?.uid

        if (uid != null) {

            val userReference = firestoreInstance.collection("users").document(uid)

            userReference
                .update(

                    mapOf(

                        "minGoal" to displayedMinimumGoal,
                        "maxGoal" to displayedMaximumGoal

                    )

                )
                .addOnSuccessListener {

                    UserModel.minGoal = displayedMinimumGoal
                    UserModel.maxGoal = displayedMaximumGoal

                    callback(true)

                }
                .addOnFailureListener {

                    callback(false)

                }
        }
    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\