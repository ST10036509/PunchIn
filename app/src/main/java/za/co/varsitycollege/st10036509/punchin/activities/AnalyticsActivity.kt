package za.co.varsitycollege.st10036509.punchin.activities

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.graphics.Typeface
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityAnalyticsBinding
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.NavbarViewBindingHelper
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.TimesheetModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import za.co.varsitycollege.st10036509.punchin.R
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject




class AnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyticsBinding

    //private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart
    private val firestoreInstance = FirestoreConnection.getDatabaseInstance()
    private var currentUser: FirebaseUser? = null
    private var authModel = AuthenticationModel()
    private lateinit var navbarHelper: NavbarViewBindingHelper//create a NavBarViewBindingsHelper class object
    private lateinit var intentHandler: IntentHandler
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private lateinit var loadingDialogHandler: LoadDialogHandler//setup an intent handler for navigating pages
    private var currentDate = LocalDate.now()
    private lateinit var selectedDateStart: Date
    private lateinit var selectedDateEnd: Date


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
        loadingDialogHandler =
            LoadDialogHandler(this@AnalyticsActivity, progressDialog)//initialise the loading dialog
        intentHandler = IntentHandler(this@AnalyticsActivity)
        navbarHelper = NavbarViewBindingHelper(this@AnalyticsActivity, binding)
        navbarHelper.setUpNavBarOnClickEvents()
        barChart = binding.barChart
        currentUser = authModel.getCurrentUser()
        updateWeekDisplay()
    }

    //--------------------------------------------------------------------------------------------------------------------//
    private fun setupListeners() {
        // Set onClickListener for the previous week button
        binding.btnPreviousWeek.setOnClickListener {
            navPreviousWeek()
        }

        // Set onClickListener for the next week button
        binding.btnNextWeek.setOnClickListener {
            navNextWeek()
        }
    }

//--------------------------------------------------------------------------------------------------------------------//

    private fun retrieveAndPlotData(currentUser: FirebaseUser, startDate: Date, endDate: Date) {
        val userId = currentUser.uid
        val timesheetsRef = firestoreInstance.collection("timesheets")

        val startTimestamp = startDate
        val endTimestamp = endDate

        val timesheetsQuery = timesheetsRef
            .whereEqualTo("userId", userId)

        timesheetsQuery.get()
            .addOnSuccessListener { documents ->
                val timesheetEntries = mutableListOf<BarEntry>()
                val dateLabels = mutableListOf<String>()
                val hoursWorkedMap = mutableMapOf<String, Float>()

                if (documents.isEmpty) {
                    Log.w("AnalyticsActivity", "No documents found for userId: $userId")
                } else {
                    Log.d("AnalyticsActivity", "Documents found: ${documents.size()}")
                }

                for (document in documents) {
                    val timesheet = document.toObject(TimesheetModel::class.java)
                    Log.d("AnalyticsActivity", "Retrieved timesheet: $timesheet")

                    val startTimeTimestamp = timesheet.timesheetStartTime?.time
                    val endTimeTimestamp = timesheet.timesheetEndTime?.time
                    val startDateTimestamp = timesheet.timesheetStartDate

                    if (startDateTimestamp!! in startTimestamp..endTimestamp) {
                        val pattern = "EEE MMM dd HH:mm:ss zzz yyyy"
                        val formatter = SimpleDateFormat(pattern, Locale.ENGLISH)
                        val date = formatter.parse(timesheet.timesheetStartDate.toString())

                        val cal = Calendar.getInstance().apply { time = date }
                        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH).toFloat()
                        val month = cal.get(Calendar.MONTH) + 1 // Month is zero-based
                        val formattedDate = String.format("%02d/%02d", dayOfMonth.toInt(), month)

                        val durationInMillis = endTimeTimestamp?.minus(startTimeTimestamp!!)
                        val hoursWorked = durationInMillis?.div((1000 * 60 * 60).toFloat())

                        if (hoursWorked != null) {
                            hoursWorkedMap[formattedDate] =
                                hoursWorkedMap.getOrDefault(formattedDate, 0f) + hoursWorked
                        }
                    }
                }

                val cal = Calendar.getInstance()
                cal.time = startDate

                var index = 0f
                while (cal.time.before(endDate) || cal.time.equals(endDate)) {
                    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH).toFloat()
                    val month = cal.get(Calendar.MONTH) + 1 // Month is zero-based
                    val formattedDate = String.format("%02d/%02d", dayOfMonth.toInt(), month)
                    dateLabels.add(formattedDate)

                    val hoursWorked = hoursWorkedMap.getOrDefault(formattedDate, 0f)
                    timesheetEntries.add(BarEntry(index, hoursWorked))
                    index += 1f

                    cal.add(Calendar.DAY_OF_MONTH, 1)
                }

                val dataSet = BarDataSet(timesheetEntries, "Hours Worked")
                dataSet.color = android.graphics.Color.BLUE
                dataSet.valueTextColor = android.graphics.Color.BLACK
                dataSet.valueTextSize = 14f // Increase data value text size
                dataSet.valueTypeface = Typeface.DEFAULT_BOLD // Change data value font style

                val barData = BarData(dataSet)
                barChart.data = barData

                val description = Description()
                description.text = ""
                barChart.description = description

                // Customize X axis
                val xAxis = barChart.xAxis
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return dateLabels.getOrNull(value.toInt()) ?: value.toString()
                    }
                }
                val leftAxis: YAxis = barChart.axisLeft
                val rightAxis: YAxis = barChart.axisRight

                leftAxis.setDrawGridLines(false)
                rightAxis.setDrawGridLines(false)
                barChart.invalidate()
            }
            .addOnFailureListener { exception ->
                Log.w("AnalyticsActivity", "Error getting documents: ", exception)
            }
    }

//--------------------------------------------------------------------------------------------------------------------//

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

        currentUser?.let { retrieveAndPlotData(it, selectedDateStart, selectedDateEnd) }
        currentUser?.let { retrieveAndDisplayStats(it) }
    }

    fun convertToDate(localDate: LocalDate): Date {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    fun formatDate(date: LocalDate): String {
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return date.format(dateFormatter)
    }

    private fun navPreviousWeek() {
        this.currentDate = currentDate.minusWeeks(1)
        updateWeekDisplay()
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
        updateWeekDisplay()
    }

//--------------------------------------------------------------------------------------------------------------------//

    private fun retrieveAndDisplayStats(currentUser: FirebaseUser) {
        val userId = currentUser.uid
        val timesheetsRef = firestoreInstance.collection("timesheets")
        val userRef = firestoreInstance.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { userDocument ->
                val userPoints = userDocument.getLong("points") ?: 0L

                timesheetsRef.whereEqualTo("userId", userId).get()
                    .addOnSuccessListener { documents ->
                        var totalHours = 0.0
                        var totalAmount = 0.0

                        val timesheetTasks = documents.map { document ->
                            val timesheet = document.toObject(TimesheetModel::class.java)
                            val startTime = timesheet.timesheetStartTime
                            val endTime = timesheet.timesheetEndTime

                            if (startTime != null && endTime != null) {
                                val hoursWorked =
                                    (endTime.time - startTime.time) / (1000 * 60 * 60).toDouble()
                                totalHours += hoursWorked

                                val projectRef = firestoreInstance.collection("projects")
                                    .document(timesheet.projectId)
                                projectRef.get().continueWith { task ->
                                    if (task.isSuccessful) {
                                        val projectDocument = task.result
                                        if (projectDocument != null && projectDocument.exists()) {
                                            val hourlyRate =
                                                projectDocument.getDouble("hourlyRate") ?: 0.0
                                            totalAmount += hoursWorked * hourlyRate
                                        }
                                    }
                                }
                            } else {
                                Tasks.forResult(null) // return a completed task with no result
                            }
                        }

                        Tasks.whenAll(timesheetTasks).addOnSuccessListener {
                            // Update UI
                            findViewById<TextView>(R.id.tv_DisplayedHours).text =
                                String.format("%.2f hours", totalHours)
                            findViewById<TextView>(R.id.tv_BillableHours).text =
                                String.format(
                                    "%.2f hours",
                                    totalHours
                                ) // Assuming all hours are billable
                            findViewById<TextView>(R.id.tv_DisplayedAmount).text =
                                String.format("R%.2f", totalAmount)
                            findViewById<TextView>(R.id.tv_UserPoints).text = userPoints.toString()
                        }.addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting projects: ", exception)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting timesheets: ", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting user document: ", exception)
            }
    }

    //--------------------------------------------------------------------------------------------------------------------//

}
//----------------------------------------------END OF FILE-------------------------------------------------------------//
