package za.co.varsitycollege.st10036509.punchin.activities
/*
AUTHOR: Leonard Bester
CREATED: 29/04/2024
LAST MODIFIED: 02/04/2024
*/
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityProjectViewBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.ProjectsModel
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.NavbarViewBindingHelper
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler

class ProjectViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectViewBinding // Bind the ProjectViewActivity with the KT and XML files
    private lateinit var authModel: AuthenticationModel
    private lateinit var projectModel: ProjectsModel
    private lateinit var toaster: ToastHandler
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private lateinit var loadingDialogHandler: LoadDialogHandler//setup an intent handler for navigating pages
    private lateinit var navbarHelper: NavbarViewBindingHelper//create a NavBarViewBindingsHelper class object
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages



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


        authModel = AuthenticationModel()

        loadingDialogHandler = LoadDialogHandler(this@ProjectViewActivity, progressDialog)//initialise the loading dialog

        // Initialize FAB
        val fabAddProject: FloatingActionButton = findViewById(R.id.fab_add_project)
        // Set OnClickListener to FAB
        fabAddProject.setOnClickListener {
            toaster.showToast("Opening project creation page")
            // Start the activity for project creation
            intentHandler.openActivityIntent(ProjectCreationActivity::class.java)
        }


        // Initialize ProjectsModel
        projectModel = ProjectsModel("", "", "", 0.0, "", 0,0,0.0,"")
        toaster = ToastHandler(this@ProjectViewActivity)

        loadingDialogHandler.showLoadingDialog("Loading Projects...")
        initializePopulate()

    }

    private fun initializePopulate(){

        val currentUser = authModel.getCurrentUser()
        if (currentUser != null) {
            projectModel.countProjects(currentUser.uid) { projects ->
                // Now projects contains the list of projects
                if (projects.isNotEmpty()) {
                    populateHolder(projects)
                    loadingDialogHandler.dismissLoadingDialog()
                    toaster.showToast("Projects Loaded")
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


        // Create an Intent to start the ProjectsDetailActivity
        intentHandler.openActivityIntent(ProjectDetailsActivity::class.java)
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

}

/*
___________           .___         _____  ___________.__.__
\_   _____/ ____    __| _/   _____/ ____\ \_   _____/|__|  |   ____
 |    __)_ /    \  / __ |   /  _ \   __\   |    __)  |  |  | _/ __ \
 |        \   |  \/ /_/ |  (  <_> )  |     |     \   |  |  |_\  ___/
/_______  /___|  /\____ |   \____/|__|     \___  /   |__|____/\___  >
        \/     \/      \/                      \/                 \/
*/