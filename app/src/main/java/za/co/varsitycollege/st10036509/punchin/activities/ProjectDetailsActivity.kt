package za.co.varsitycollege.st10036509.punchin.activities
/*
AUTHOR: Leonard Bester
CREATED: 23/04/2024
LAST MODIFIED: 30/04/2024
 */

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityProjectDetailsBinding
import za.co.varsitycollege.st10036509.punchin.models.ProjectsModel
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectDetailsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var projectModel: ProjectsModel
    private lateinit var toaster: ToastHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseFirestore instance
        firestore = FirebaseFirestore.getInstance()

        toaster = ToastHandler(this@ProjectDetailsActivity)

        // Initialize ProjectsModel
        projectModel = ProjectsModel("", "", "", 0.0, "", 0,0,0.0,"")

        // Call method to retrieve data from Firestore
        projectModel.readProjectData("SJ7cy8KHnKSIElKkGfzG") { project, timesheets ->
            // Handle the received project and timesheets objects here
            if (project != null) {

                binding.apply {
                    tvProjectName.text = project.projectName
                    tvColour.text = project.setColor
                    tvProjectName.text =project.projectName
                    //tvRate.text = project.hourlyRate
                    tvEarnings.text = project.totalEarnings.toString()
                    tvTimeSheets.text =project.totalTimeSheets.toString()
                    tvHours.text = project.totalHours.toString()
                    tvDescription.text = project.description
                }

                for (timesheet in timesheets){
                    binding.tvList.append("${timesheet.timesheetName}\n")
                }
            } else {
                // Handle case where project is null (e.g., project not found)
                toaster.showToast("No project found...")
            }
        }
    }
}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\