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

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectDetailsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var projectModel: ProjectsModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseFirestore instance
        firestore = FirebaseFirestore.getInstance()

        // Initialize ProjectsModel
        projectModel = ProjectsModel("", "", "", "", "", 0,0,0.0,"")

        // Call method to retrieve data from Firestore
        projectModel.readProjectData("SJ7cy8KHnKSIElKkGfzG") { project, timesheets ->
            // Handle the received project and timesheets objects here
            if (project != null) {

                binding.apply {
                    tvProjectName.text = project.projectName
                    tvColour.text = project.setColor
                    tvProjectName.text =project.projectName
                    tvRate.text = project.hourlyRate
                    tvEarnings.text = project.totalEarnings.toString()
                    tvTimeSheets.text =project.totalTimeSheets.toString()
                    tvHours.text = project.totalHours.toString()
                    tvDescription.text = project.description
                }

                for (timesheet in timesheets){
                    binding.tvList.append("${timesheet.name}\n")
                }



            } else {
                // Handle case where project is null (e.g., project not found)
            }
        }
    }
}










/*
    private fun retrieveDataFromFirestore() {
        // Reference to the project document in Firestore
        val projectRef = firestore.collection("projects").document("SJ7cy8KHnKSIElKkGfzG")

        // Retrieve data from Firestore
        projectRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Parse and populate the TextViews with data
                    val projectName = documentSnapshot.getString("projectName")
                    val organizationName = documentSnapshot.getString("organizationName")
                    val startDate = documentSnapshot.getString("startDate")
                    val setColor = documentSnapshot.getString("setColor")
                    val hourlyRate = documentSnapshot.getString("hourlyRate")
                    val description = documentSnapshot.getString("description")

                    // Update TextViews with retrieved data
                    val tvProjectName = findViewById<TextView>(R.id.tv_project_name)
                    val tvSetColor = findViewById<TextView>(R.id.tv_colour)
                    val tvHourlyRate = findViewById<TextView>(R.id.tv_rate)
                    val tvDescription = findViewById<TextView>(R.id.tv_description)

                    tvProjectName.text = projectName
                    tvSetColor.text = setColor
                    tvHourlyRate.text = hourlyRate
                    tvDescription.text = description
                }
            }
            .addOnFailureListener { exception ->
                // Log any errors that occur while retrieving data
                Log.e("ProjectDetailsActivity", "Error retrieving project data: $exception")
            }
    } */

//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\