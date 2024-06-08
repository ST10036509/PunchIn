package za.co.varsitycollege.st10036509.punchin.activities

/*
AUTHOR: Leonard Bester
CREATED: 29/04/2024
LAST MODIFIED: 03/05/2024
*/

import android.app.ProgressDialog
import android.graphics.Color
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
    private var progressDialog: ProgressDialog? = null
    private lateinit var loadingDialogHandler: LoadDialogHandler
    private lateinit var intentHandler: IntentHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialogHandler = LoadDialogHandler(this@ProjectDetailsActivity, progressDialog)
        intentHandler = IntentHandler(this@ProjectDetailsActivity)
        firestore = FirebaseFirestore.getInstance()

        toaster = ToastHandler(this@ProjectDetailsActivity)

        val receivedIntent = intent
        val projectName = receivedIntent.getStringExtra("projectName")
        val hourlyRate = receivedIntent.getDoubleExtra("hourlyRate", 0.0)
        val description = receivedIntent.getStringExtra("description")
        val colorHashId = receivedIntent.getStringExtra("setColor")
        val totalTimeSheets = receivedIntent.getLongExtra("totalTimeSheets", 0)
        val totalHours = receivedIntent.getDoubleExtra("totalHours", 0.0)
        val totalEarnings = receivedIntent.getDoubleExtra("totalEarnings", 0.0)
        val userId = receivedIntent.getStringExtra("userId")
        val sheetNames = receivedIntent.getStringArrayListExtra("sheetNames") // Retrieve the list of timesheet names

        loadingDialogHandler.showLoadingDialog("Loading Data...")
        binding.apply {
            tvProjectName.setTextColor(getTextColorFromHash(colorHashId))
            tvProjectName.text = projectName
            tvColour.text = colorHashId
            tvColour.setTextColor(getTextColorFromHash(colorHashId))
            tvRate.text = hourlyRate.toString()
            tvTimeSheets.text = totalTimeSheets.toString()
            tvHours.text = totalHours.toString()
            tvDescription.text = description
            // Populate tvList with the contents of sheetNames in bullet form
            tvList.text = sheetNames?.joinToString(separator = "\n") { "• $it" }
        }
        loadingDialogHandler.dismissLoadingDialog()

        // Example usage of the retrieved sheet names
        sheetNames?.let {
            // Do something with the sheet names, e.g., display them in a ListView or RecyclerView
            for (name in it) {
                println("Timesheet Name: $name")
            }
        }

        binding.btnReturn.setOnClickListener {
            intentHandler.openActivityIntent(ProjectViewActivity::class.java)
        }
    }

    private fun getTextColorFromHash(hash: String?): Int {
        // Check if the hash is null or empty
        if (hash.isNullOrEmpty()) {
            return Color.BLACK // Return black color as default
        }

        // Remove any leading '#' character
        val colorString = if (hash.startsWith("#")) {
            hash.substring(1)
        } else {
            hash
        }

        // Add alpha channel if the color string is short (e.g., "#RGB")
        val fullColorString = if (colorString.length == 3) {
            // Convert short color string to full format (e.g., "#RRGGBB")
            colorString.map { "$it$it" }.joinToString("")
        } else {
            colorString
        }

        // Parse the color string
        return Color.parseColor("#$fullColorString")
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