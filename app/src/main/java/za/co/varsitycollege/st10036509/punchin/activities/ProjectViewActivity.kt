package za.co.varsitycollege.st10036509.punchin.activities
/*
AUTHOR: Leonard Bester
CREATED: 29/04/2024
LAST MODIFIED: 03/05/2024
*/
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityProjectViewBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.ProjectsModel
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.NavbarViewBindingHelper
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler
import za.co.varsitycollege.st10036509.punchin.utils.ValidationHandler
import java.util.Calendar

class ProjectViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectViewBinding // Bind the ProjectViewActivity with the KT and XML files
    private lateinit var authModel: AuthenticationModel
    private lateinit var projectModel: ProjectsModel
    private lateinit var toaster: ToastHandler
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private lateinit var loadingDialogHandler: LoadDialogHandler//setup an intent handler for navigating pages
    private lateinit var navbarHelper: NavbarViewBindingHelper//create a NavBarViewBindingsHelper class object
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages
    private var currentUser: FirebaseUser?= null
    private var startDate: Timestamp? = null
    private var endDate: Timestamp? = null
    private lateinit var validationHandler: ValidationHandler



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intentHandler = IntentHandler(this@ProjectViewActivity)//setup an intent handler for navigating pages
        //NAVBAR INITIALIZATION
        //initialize an instance of the NavBarHelper and pass in the current context and binding
        navbarHelper = NavbarViewBindingHelper(this@ProjectViewActivity, binding)
        //setup listeners for NavBar onClick events
        navbarHelper.setUpNavBarOnClickEvents()
        validationHandler = ValidationHandler()

        authModel = AuthenticationModel()
        currentUser = authModel.getCurrentUser()
        endDate = null
        startDate = null
        loadingDialogHandler = LoadDialogHandler(this@ProjectViewActivity, progressDialog)//initialise the loading dialog

        // Initialize FAB
        val fabAddProject: FloatingActionButton = findViewById(R.id.fab_add_project)
        // Set OnClickListener to FAB
        fabAddProject.setOnClickListener {
            toaster.showToast("Opening project creation page")
            // Start the activity for project creation
            intentHandler.openActivityIntent(ProjectCreationActivity::class.java)
        }
        val btnAlpha =binding.btnAlphabet
        btnAlpha.setOnClickListener{
            rearrangeComponentsAlphabetically()
        }
        val btnStartDate = binding.btnStartDate
        btnStartDate.setOnClickListener {
            showStartDatePicker()
        }
        val btnEndDate = binding.btnEndDate
        btnEndDate.setOnClickListener {
            showEndDatePicker()
        }

        val btnSort = binding.btnSortDate
        btnSort.setOnClickListener {
            validateNullInput()
        }





        // Initialize ProjectsModel
        projectModel = ProjectsModel("", null, "", 0.0, "", 0,0.0,0.0,"")
        toaster = ToastHandler(this@ProjectViewActivity)

        loadingDialogHandler.showLoadingDialog("Loading Projects...")
        initializePopulate()
    }

    private fun validateNullInput() {

        val check = validationHandler.checkForNonNullInputs(startDate, endDate)

        if (!check) {
            validateDateComparison()

        } else {
            toaster.showToast("Please enter the start and end date")
        }
    }

    private fun validateDateComparison(){
        val check = validationHandler.checkEndDateBeforeStartDate(startDate,endDate)

        if (check){
            toaster.showToast("End date can not be before start date")
        }else{
            rearrangeComponentsByDateRange()
        }

    }


    private fun showEndDatePicker() {
        // Get current date to set as default in the picker
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog instance with current date as default
        val datePickerDialog = DatePickerDialog(
            this@ProjectViewActivity,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                // Set the selected date in the endDate variable
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val date = selectedDate.time // Convert Calendar to Date
                endDate = Timestamp(date) // Convert Date to Timestamp and update endDate
            },
            year,
            month,
            day
        )
        // Show the DatePickerDialog
        datePickerDialog.show()
    }


    private fun showStartDatePicker() {
        // Get current date to set as default in the picker
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog instance with current date as default
        val datePickerDialog = DatePickerDialog(
            this@ProjectViewActivity,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                // Set the selected date in the startDate variable
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val date = selectedDate.time // Convert Calendar to Date
                startDate = Timestamp(date) // Convert Date to Timestamp and update startDate
            },
            year,
            month,
            day
        )
        // Show the DatePickerDialog
        datePickerDialog.show()
    }



    private fun rearrangeComponentsAlphabetically() {
        // Remove all existing views from ll_holder
        binding.llHolder.removeAllViews()

        val currentUser = authModel.getCurrentUser()
        if (currentUser != null) {
            projectModel.getProjectList(currentUser.uid) { projects ->
                // Now projects contains the list of projects
                if (projects.isNotEmpty()) {
                    // Sort the list of projects alphabetically regardless of capitalization
                    val sortedProjects = projects.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.projectName })

                    // Create and add XML components for each project to ll_holder
                    for (project in sortedProjects) {
                        createXmlComponent(project)
                    }

                    toaster.showToast("Projects Loaded and rearranged alphabetically")
                } else {
                    toaster.showToast("No projects found")
                    Log.e("ProjectViewActivity", "No projects found for current user")
                }
            }
        } else {
            toaster.showToast("No projects found")
            Log.e("ProjectViewActivity", "Current user is null")
        }
    }


    private fun rearrangeComponentsByDateRange() {
        // Remove all existing views from ll_holder
        binding.llHolder.removeAllViews()

        val currentUser = authModel.getCurrentUser()
        if (currentUser != null) {
            projectModel.getProjectList(currentUser.uid) { projects ->
                // Now projects contains the list of projects
                if (projects.isNotEmpty()) {
                    // Convert startDate and endDate to milliseconds since epoch
                    val startMillis = startDate?.toDate()?.time ?: Long.MIN_VALUE
                    val endMillis = endDate?.toDate()?.time ?: Long.MAX_VALUE

                    // Filter the projects based on the start date range
                    val filteredProjects = projects.filter { project ->
                        project.startDate?.toDate()?.time ?: Long.MIN_VALUE in startMillis..endMillis
                    }

                    // Sort the filtered list of projects by start date
                    val sortedProjects = filteredProjects.sortedByDescending { it.startDate?.toDate()?.time ?: 0 }

                    // Create and add XML components for each project to ll_holder
                    for (project in sortedProjects) {
                        createXmlComponent(project)
                    }

                    toaster.showToast("Projects Loaded and rearranged by date range")
                } else {
                    toaster.showToast("No projects found")
                    Log.e("ProjectViewActivity", "No projects found for current user")
                }
            }
        } else {
            toaster.showToast("No projects found")
            Log.e("ProjectViewActivity", "Current user is null")
        }
    }





    private fun initializePopulate(){

        if (currentUser != null) {
            projectModel.getProjectList(currentUser?.uid.toString()) { projects ->
                // Now projects contains the list of projects
                if (projects.isNotEmpty()) {
                    populateHolder(projects)
                    loadingDialogHandler.dismissLoadingDialog()

                } else {
                    toaster.showToast("No projects found")
                    loadingDialogHandler.dismissLoadingDialog()
                    Log.e("ProjectViewActivity", "No projects found for current user")
                }
            }
        } else {
            toaster.showToast("No projects found")
            loadingDialogHandler.dismissLoadingDialog()
            Log.e("ProjectViewActivity", "Current user is null")
        }
    }


    private fun populateHolder(projects: List<ProjectsModel>){

        for (project in projects) {
            // Here you can create the XML components and add them to the layout using the data from each project
            // For example:
            createXmlComponent(project)
        }
    }


    private fun createXmlComponent(project: ProjectsModel) {
        val linearLayout = LinearLayout(this)
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            70.dpToPx()
        )
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.setBackgroundResource(R.drawable.rectangle_wrapper_white_round_corners)
        linearLayout.elevation = 2.dpToPx().toFloat()

        val textView = TextView(this)
        textView.layoutParams = LinearLayout.LayoutParams(
            227.dpToPx(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textView.gravity = android.view.Gravity.CENTER
        textView.setBackgroundResource(R.drawable.rectangle_wrapper_white_round_corners)
        textView.text = project.projectName
        textView.textSize = 27f
        textView.setTextColor(getColor(R.color.dark_blue_900))

        val imageView = ImageView(this)
        imageView.layoutParams = LinearLayout.LayoutParams(
            2.dpToPx(),
            65.dpToPx()
        )
        imageView.setBackgroundColor(getColor(android.R.color.darker_gray))

        val nestedLinearLayout = LinearLayout(this)
        nestedLinearLayout.layoutParams = LinearLayout.LayoutParams(
            152.dpToPx(),
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        nestedLinearLayout.orientation = LinearLayout.VERTICAL

        val textTimesheets = TextView(this)
        textTimesheets.layoutParams = LinearLayout.LayoutParams(
            88.dpToPx(),
            28.dpToPx()
        )
        textTimesheets.gravity = android.view.Gravity.CENTER
        textTimesheets.text = "TimeSheets: " + project.totalTimeSheets.toString()
        textTimesheets.setTextColor(getColor(R.color.dark_blue_900))

        val imageSeparator = ImageView(this)
        imageSeparator.layoutParams = LinearLayout.LayoutParams(
            122.dpToPx(),
            2.dpToPx()
        )
        imageSeparator.setBackgroundColor(getColor(android.R.color.darker_gray))

        val textHrsLogged = TextView(this)
        textHrsLogged.layoutParams = LinearLayout.LayoutParams(
            94.dpToPx(),
            27.dpToPx()
        )
        textHrsLogged.gravity = android.view.Gravity.CENTER_HORIZONTAL
        textHrsLogged.text = "Hrs Logged: " + project.totalHours.toString()
        textHrsLogged.setTextColor(getColor(R.color.dark_blue_900))

        // Add views to nestedLinearLayout
        nestedLinearLayout.addView(textTimesheets)
        nestedLinearLayout.addView(imageSeparator)
        nestedLinearLayout.addView(textHrsLogged)

        // Add views to linearLayout
        linearLayout.addView(textView)
        linearLayout.addView(imageView)
        linearLayout.addView(nestedLinearLayout)

        // Set click listener to the linear layout
        linearLayout.setOnClickListener {
            handleProjectClick(project)
        }

        // Add linearLayout to sv_projects ScrollView using binding
        binding.llHolder.addView(linearLayout)
    }

    private fun handleProjectClick(project: ProjectsModel){

        //val jsonString = Json.encodeToString(project)

        var intent = Intent(this@ProjectViewActivity, ProjectDetailsActivity::class.java)
        intent.putExtra("projectName", project.projectName)
        intent.putExtra("startDate", project.startDate)
        intent.putExtra("setColor", project.setColor) // Pass the color resource name as a string
        intent.putExtra("hourlyRate", project.hourlyRate)
        intent.putExtra("description", project.description)
        intent.putExtra("totalTimeSheets", project.totalTimeSheets)
        intent.putExtra("totalHours", project.totalHours)
        intent.putExtra("totalEarnings", project.totalEarnings)
        intent.putExtra("userId", project.userId)
        startActivity(intent)


    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

}

/*
░▒▓████████▓▒░▒▓███████▓▒░░▒▓███████▓▒░        ░▒▓██████▓▒░░▒▓████████▓▒░      ░▒▓████████▓▒░▒▓█▓▒░▒▓█▓▒░      ░▒▓████████▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓██████▓▒░ ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓██████▓▒░        ░▒▓██████▓▒░ ░▒▓█▓▒░▒▓█▓▒░      ░▒▓██████▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓████████▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓███████▓▒░        ░▒▓██████▓▒░░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓████████▓▒░▒▓████████▓▒░


*/