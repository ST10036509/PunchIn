package za.co.varsitycollege.st10036509.punchin.activities

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.widget.Button
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.compose.ui.text.toUpperCase
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEach
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.activities.TimesheetCreationActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetViewBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.TimesheetModel
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.NavbarViewBindingHelper
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.LocalDate.ofInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimesheetViewActivity : AppCompatActivity() {
    private var currentDate = LocalDate.now()
    private var currentUser: FirebaseUser? = null
    private var authModel = AuthenticationModel()
    private val firestoreInstance = FirestoreConnection.getDatabaseInstance()
    private lateinit var binding: ActivityTimesheetViewBinding
    private lateinit var intentHandler: IntentHandler
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private lateinit var loadingDialogHandler: LoadDialogHandler//setup an intent handler for navigating pages
    private lateinit var timesheetModel: TimesheetModel
    private lateinit var selectedDateStart: Date
    private lateinit var selectedDateEnd: Date
    private lateinit var navbarHelper: NavbarViewBindingHelper//create a NavBarViewBindingsHelper class object
    private val listOfUserTimesheets = mutableListOf<TimesheetModel>()
    private val filteredTimesheets = mutableListOf<TimesheetModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimesheetViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialogHandler = LoadDialogHandler(this@TimesheetViewActivity, progressDialog)//initialise the loading dialog

        intentHandler = IntentHandler(this@TimesheetViewActivity)

        setupListeners()

        //initialize an instance of the NavBarHelper and pass in the current context and binding
        navbarHelper = NavbarViewBindingHelper(this@TimesheetViewActivity, binding)
        //setup listeners for NavBar onClick events
        navbarHelper.setUpNavBarOnClickEvents()

        timesheetModel = TimesheetModel("", "", "", null, null, null, "", null, false)

        currentUser = authModel.getCurrentUser()

        // Check if currentUser is not null before calling searchTimesheetsForUser
        currentUser?.let { searchTimesheetsForUser(it) }

        // Update UI after getting the timesheets
        //updateUI()
    }


    /**
     * Method to setup listeners for UI controls
     */
    private fun setupListeners() {

        // Set onClickListener for the previous week button
        binding.btnPreviousWeek.setOnClickListener {
            filteredTimesheets.clear()
            navPreviousWeek()
        }

        // Set onClickListener for the next week button
        binding.btnNextWeek.setOnClickListener {
            filteredTimesheets.clear()
            navNextWeek()
        }

        binding.fabAddTimesheet.setOnClickListener {
            goToTimesheetCreation()
        }

    }

    private fun goToTimesheetCreation() {
        // Fetch projects associated with the current user
        fetchUserProjects { projects ->
            // Check if the user has any projects
            if (projects.isEmpty()) {
                // User does not have any projects, display a toast message
                Toast.makeText(this, "You need to create a project first", Toast.LENGTH_LONG).show()
            } else {
                // User has projects, proceed to timesheet creation activity
                loadingDialogHandler.showLoadingDialog("Loading...")
                intentHandler.openActivityIntent(TimesheetCreationActivity::class.java)
            }
        }
    }


    private fun updateUI(){
        clearParentLayout()
        filteredTimesheets.clear()
        updateWeekDisplay()
        filterTimesheetsByTimePeriod(selectedDateStart, selectedDateEnd)
        displayTimesheetsForWeek(filteredTimesheets)
    }

    private fun navPreviousWeek() {
        this.currentDate = currentDate.minusWeeks(1)
        updateUI()
    }

    private fun navNextWeek() {
        // Calculate the end date of the next week
        val nextWeekEndDate = currentDate.plusWeeks(1)
            .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))

        // Calculate the end date of the current week
        val currentWeekEndDate = LocalDate.now()
            .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))

        // Check if the end date of the next week is after the end of the current week
        if (nextWeekEndDate.isAfter(currentWeekEndDate)) {
            // Do not update the current date or display
            return
        }
        // Update the current date and display
        currentDate = currentDate.plusWeeks(1)
        updateUI()
    }

    private fun searchTimesheetsForUser(currentUser: FirebaseUser) {
        val userId = currentUser.uid
        // Reference to the collection of timesheets in Firestore
        val timesheetsCollectionRef = firestoreInstance.collection("timesheets")
        val timesheetsQuery = timesheetsCollectionRef.whereEqualTo("userId", userId)

        // Query to search for timesheets with the specified user ID
        timesheetsQuery.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                // Retrieve data from the document
                timesheetModel = TimesheetModel(
                    userId = document.getString("userId") ?: "",
                    timesheetName = document.getString("timesheetName") ?: "",
                    projectId = document.getString("projectId") ?: "",
                    timesheetStartDate = document.getTimestamp("timesheetStartDate")?.toDate(),
                    timesheetStartTime = document.getTimestamp("timesheetStartTime")?.toDate(),
                    timesheetEndTime = document.getTimestamp("timesheetEndTime")?.toDate(),
                    timesheetDescription = document.getString("timesheetDescription") ?: "",
                    timesheetPhoto = document.getString("timesheetPhoto") ?: "",
                    checkSum = false
                )
                listOfUserTimesheets.add(timesheetModel)
            }
            updateUI()
        }
    }


    fun filterTimesheetsByTimePeriod(startDate: Date, endDate: Date) {
        // Iterate through the list of timesheets
        var tempDate =  addOneDay(endDate)
        for (timesheet in listOfUserTimesheets) {
            // Check if the timesheet's start date is after or equal to the specified start date
            // and if the timesheet's end date is before or equal to the specified end date
            if (timesheet.timesheetStartDate!! in startDate..tempDate) {
                // Add the timesheet to the filtered list
                filteredTimesheets.add(timesheet)
            }
        }
    }

    fun addOneDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.time
    }

    private fun updateWeekDisplay() {
        // Calculate the start date of the week
        val createdAt =
            currentDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val startDay = createdAt.dayOfMonth

        // Calculate the end date of the week
        val endDate = createdAt.plusDays(6)

        // Calculate the end date of the current week
        val endOfWeek = LocalDate.now()
            .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))

        // Update UI with the new week time window using data binding or findViewById
        val displayEndDate = if (endOfWeek.isBefore(endDate)) endOfWeek else endDate

        // Update UI with the new week time window using data binding or findViewById
        binding.tvWeeklySelector.text = "${startDay} - ${formatDate(displayEndDate)}"
        selectedDateStart = convertToDate(createdAt)
        selectedDateEnd = convertToDate(endDate)
    }

    fun convertToDate(localDate: LocalDate): Date {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    fun formatDate(date: LocalDate): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return date.format(dateFormatter)
    }

    private fun createLayout(timesheet: TimesheetModel): TableLayout {
        val customFontReglo: Typeface? = ResourcesCompat.getFont(this, R.font.reglo_bold)
        val customFontStaatliches: Typeface? = ResourcesCompat.getFont(this, R.font.staatliches)
        val context = this

        // Create the parent TableLayout
        val tableLayout = TableLayout(context)
        tableLayout.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableLayout.setPadding(20, 20, 20, 20)
        tableLayout.elevation = 4f

        // Create the TableRow for timesheet view
        val tableRow = TableRow(context)
        tableRow.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableRow.setPadding(45, 30, 45, 30)
        tableRow.background = resources.getDrawable(R.drawable.rectangle_wrapper_white_round_corners)
        tableRow.elevation = 4f
        tableLayout.addView(tableRow)

        // Create the LinearLayout for start and stop times
        val startStopCell = LinearLayout(context)
        startStopCell.layoutParams = TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.MATCH_PARENT,
            1f
        )
        startStopCell.orientation = LinearLayout.VERTICAL
        startStopCell.setPadding(10, 0, 10, 0)
        tableRow.addView(startStopCell)

        // Create the TextView for start time
        val tvStartTime = TextView(context)
        tvStartTime.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tvStartTime.setPadding(30, 20, 0, 0)
        tvStartTime.text = timesheet.timesheetStartTime?.let { getHoursAndMinutesFromDate(it) }
        tvStartTime.setTextColor(resources.getColor(R.color.dark_blue_900))
        tvStartTime.typeface = customFontStaatliches
        startStopCell.addView(tvStartTime)

        // Create the TextView for end time
        val tvEndTime = TextView(context)
        tvEndTime.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tvEndTime.setPadding(30, 0, 0, 20)
        tvEndTime.text = timesheet.timesheetEndTime?.let { getHoursAndMinutesFromDate(it) }
        tvEndTime.setTextColor(resources.getColor(R.color.dark_blue_900))
        tvEndTime.typeface = customFontStaatliches
        startStopCell.addView(tvEndTime)

        // Create the ImageView for the divider
        val ivDivider = ImageView(context)
        ivDivider.layoutParams = TableRow.LayoutParams(
            2,
            TableRow.LayoutParams.MATCH_PARENT
        )
        ivDivider.setPadding(10, 0, 10, 0)  // Adjust padding to position it on the right side of the start and end times
        ivDivider.setBackgroundColor(resources.getColor(R.color.dark_blue_900))
        tableRow.addView(ivDivider)

        // Create the LinearLayout for timesheet info
        val timesheetInfoCell = LinearLayout(context)
        timesheetInfoCell.layoutParams = TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.WRAP_CONTENT,
            3f
        )
        timesheetInfoCell.orientation = LinearLayout.VERTICAL
        timesheetInfoCell.setPadding(20, 0, 25, 0)
        tableRow.addView(timesheetInfoCell)

        // Create the TextView for timesheet name
        val tvTimesheetName = TextView(context)
        tvTimesheetName.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tvTimesheetName.setPadding(25, 20, 0, 0)
        tvTimesheetName.text = timesheet.timesheetName
        tvTimesheetName.setTextColor(resources.getColor(R.color.dark_blue_900))
        tvTimesheetName.textSize = 12f
        tvTimesheetName.typeface = customFontReglo
        tvTimesheetName.ellipsize = TextUtils.TruncateAt.END
        tvTimesheetName.maxLines = 1
        timesheetInfoCell.addView(tvTimesheetName)

        // Create the TextView for project name
        val tvTimesheetDescription = TextView(context)
        tvTimesheetDescription.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tvTimesheetDescription.setPadding(25, 5, 0, 10)
        tvTimesheetDescription.text = timesheet.timesheetDescription
        tvTimesheetDescription.setTextColor(resources.getColor(R.color.dark_blue_900))
        tvTimesheetDescription.textSize = 12f
        tvTimesheetDescription.typeface = customFontReglo
        tvTimesheetDescription.ellipsize = TextUtils.TruncateAt.END
        tvTimesheetDescription.maxLines = 1
        timesheetInfoCell.addView(tvTimesheetDescription)

        // Create the TableRow for the timesheet image
        val timesheetImageCell = LinearLayout(context)
        timesheetImageCell.layoutParams = TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f
        )
        timesheetImageCell.orientation = LinearLayout.VERTICAL
        timesheetImageCell.setPadding(0, 0, 25, 0)
        tableRow.addView(timesheetImageCell)

        // Create a new ImageView instance for the timesheet photo
        val timesheetImage = ImageView(context)
        timesheetImage.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        timesheetImage.scaleType = ImageView.ScaleType.CENTER_CROP
        timesheetImage.adjustViewBounds = true
        // Decode the base64 string to Bitmap and set it to the ImageView
        val timesheetString = timesheet.timesheetPhoto
        val timesheetBitmap = timesheetString?.let { decodeBase64ToBitmap(it) }
        timesheetImage.setImageBitmap(timesheetBitmap)
        timesheetImageCell.addView(timesheetImage)

        return tableLayout
    }

    fun getHoursAndMinutesFromDate(date: Date): String {
        // Create a SimpleDateFormat instance to format the date
        val dateFormat = SimpleDateFormat("HH:mm")

        // Format the date to get the hours and minutes as a string
        return dateFormat.format(date)
    }


    private fun displayTimesheetsForWeek(timesheets: List<TimesheetModel>) {
        val customFontReglo: Typeface? = ResourcesCompat.getFont(this, R.font.reglo_bold)
        // Get a reference to the parent layout where the day linear layouts will be added
        val parentLayout = findViewById<LinearLayout>(R.id.ll_ScrollContainer)

        val daysOfWeek =
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        // Iterate through each day of the week
        for (day in daysOfWeek) {
            // Get the timesheets for the current day
            val timesheetsForDay = timesheets.filter { timesheet ->
                timesheet.timesheetStartDate?.let { startDate ->
                    val calendar = Calendar.getInstance()
                    calendar.time = startDate
                    val dayNumberOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                    val dayOfWeek = when (dayNumberOfWeek) {
                        2 -> "MONDAY"
                        3 -> "TUESDAY"
                        4 -> "WEDNESDAY"
                        5 -> "THURSDAY"
                        6 -> "FRIDAY"
                        7 -> "SATURDAY"
                        1 -> "SUNDAY"
                        else -> "Invalid number" // Default value if the dayNumber is not recognized
                    }
                    dayOfWeek == day.toUpperCase()
                } ?: false
            }

            // If there are timesheets for the current day, create a layout and add it to the parent layout
            if (timesheetsForDay.isNotEmpty()) {
                // Create a new linear layout for the day
                val dayLayout = LinearLayout(this)
                dayLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                dayLayout.orientation = LinearLayout.HORIZONTAL

                // Add a TextView to display the day of the week
                val dayTextView = TextView(this)
                dayTextView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                dayTextView.text = day
                dayTextView.setTextColor(ContextCompat.getColor(this, R.color.indigo_900))
                dayTextView.typeface = customFontReglo
                dayTextView.textSize = 20f
                dayTextView.setPadding(20, 10, 20, 10)
                dayLayout.addView(dayTextView)

                // Add the day layout to the parent layout
                parentLayout.addView(dayLayout)

                // Iterate through the timesheets for the current day and add them to the parent layout
                for (timesheet in timesheetsForDay) {
                    val timesheetLayout = createLayout(timesheet)
                    parentLayout.addView(timesheetLayout)
                }
            }
        }
    }


    // Function to convert base64 string to Bitmap
    fun decodeBase64ToBitmap(input: String): Bitmap {
        // Decode base64 string to byte array
        val decodedBytes = ByteArray(input.length / 2)
        for (i in 0 until input.length step 2) {
            decodedBytes[i / 2] = Integer.parseInt(input.substring(i, i + 2), 16).toByte()
        }

        // Convert byte array to Bitmap
        val inputStream = ByteArrayInputStream(decodedBytes)
        return BitmapFactory.decodeStream(inputStream)
    }


    private fun clearParentLayout() {
        val parentLayout = findViewById<LinearLayout>(R.id.ll_ScrollContainer)
        parentLayout.removeAllViews()
    }


    // Function to fetch project names and IDs for the current user
    fun fetchUserProjects(completion: (List<Pair<String, String>>) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        currentUser = authModel.getCurrentUser()

        // Check if user is logged in
        currentUser?.let { user ->
            // Query Firestore for projects created by the current user
            db.collection("projects")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    val projectData = mutableListOf<Pair<String, String>>()
                    for (document in documents) {
                        val projectName = document.getString("projectName")
                        val projectId = document.id // Assuming the document ID is the project ID
                        projectName?.let {
                            projectData.add(it to projectId)
                        }
                    }
                    completion(projectData)
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    completion(emptyList()) // Return empty list on failure
                }
        } ?: run {
            // User is not logged in
            completion(emptyList()) // Return empty list if user is not logged in
        }
    }


}


