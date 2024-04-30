package za.co.varsitycollege.st10036509.punchin.activities
/*
AUTHOR: Leonard Bester
CREATED: 23/04/2024
LAST MODIFIED: 30/04/2024
 */

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10036509.punchin.R

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        // Initialize FirebaseFirestore instance
        firestore = FirebaseFirestore.getInstance()

        // Call method to retrieve data from Firestore
        retrieveDataFromFirestore()

    }

    private fun retrieveDataFromFirestore() {
        // Reference to the project document in Firestore
        val projectRef = firestore.collection("projects").document("GA0Ya1qpe3qPUymnFoyd")

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
                    val tvOrganizationName = findViewById<TextView>(R.id.tv_organization)
                    val tvSetColor = findViewById<TextView>(R.id.tv_colour)
                    val tvHourlyRate = findViewById<TextView>(R.id.tv_rate)
                    val tvDescription = findViewById<TextView>(R.id.tv_description)

                    tvProjectName.text = projectName
                    tvOrganizationName.text = organizationName
                    tvSetColor.text = setColor
                    tvHourlyRate.text = hourlyRate
                    tvDescription.text = description
                }
            }
            .addOnFailureListener { exception ->
                // Log any errors that occur while retrieving data
                Log.e("ProjectDetailsActivity", "Error retrieving project data: $exception")
            }
    }
}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\