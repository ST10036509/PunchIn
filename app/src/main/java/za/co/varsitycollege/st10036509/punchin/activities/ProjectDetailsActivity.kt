package za.co.varsitycollege.st10036509.punchin.activities
/*
AUTHOR: Leonard Bester
CREATED: 23/04/2024
LAST MODIFIED: 30/04/2024
 */

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityProjectDetailsBinding
import za.co.varsitycollege.st10036509.punchin.models.ProjectsModel
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectDetailsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var projectModel: ProjectsModel
    private lateinit var toaster: ToastHandler
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private lateinit var loadingDialogHandler: LoadDialogHandler//setup an intent handler for navigating pages
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialogHandler = LoadDialogHandler(this@ProjectDetailsActivity, progressDialog)//initialise the loading dialog
        intentHandler = IntentHandler(this@ProjectDetailsActivity)//setup an intent handler for navigating pages
        // Initialize FirebaseFirestore instance
        firestore = FirebaseFirestore.getInstance()


        toaster = ToastHandler(this@ProjectDetailsActivity)



        val recievedIntent = intent
        // Retrieve data from intent extras
        val projectName = intent.getStringExtra("projectName")
        val startDate = intent.getStringExtra("startDate")
        val setColor = intent.getStringExtra("setColor")
        val hourlyRate = intent.getDoubleExtra("hourlyRate", 0.0)
        val description = intent.getStringExtra("description")
        val totalTimeSheets = intent.getIntExtra("totalTimeSheets", 0)
        val totalHours = intent.getDoubleExtra("totalHours", 0.0)
        val totalEarnings = intent.getDoubleExtra("totalEarnings", 0.0)
        val userId = intent.getStringExtra("userId")



        binding.apply {
            tvProjectName.text = projectName
            tvColour.text = setColor
            tvProjectName.text =projectName
            tvRate.text = hourlyRate.toString()
            tvEarnings.text = totalEarnings.toString()
            tvTimeSheets.text =totalTimeSheets.toString()
            tvHours.text = totalHours.toString()
            tvDescription.text = description
        }
        // Initialize ProjectsModel
       // projectModel = ProjectsModel("", null, "", 0.0, "", 0,0,0.0,"")

        loadingDialogHandler.showLoadingDialog("Loading Data...")
                binding.apply {
                    tvProjectName.text = projectName
                    tvColour.text = setColor
                    tvRate.text = hourlyRate.toString()
                    tvTimeSheets.text = totalTimeSheets.toString()
                    tvHours.text = totalHours.toString()
                    tvDescription.text = description
                }
        loadingDialogHandler.dismissLoadingDialog()


        // Find the return button and set OnClickListener
        binding.btnReturn.setOnClickListener {
            toaster.showToast("Returning")
            intentHandler.openActivityIntent(ProjectViewActivity::class.java)
        }


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