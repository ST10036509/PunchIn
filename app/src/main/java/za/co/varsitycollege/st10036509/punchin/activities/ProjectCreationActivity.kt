/*
AUTHOR: Leonard Bester
CREATED: 23/04/2024
LAST MODIFIED: 30/04/2024
 */
package za.co.varsitycollege.st10036509.punchin.activities


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityProjectCreationBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection

class ProjectCreationActivity : AppCompatActivity() {

    private var firestoreInstance = FirestoreConnection.getDatabaseInstance()
    private lateinit var authModel: AuthenticationModel


    private lateinit var binding: ActivityProjectCreationBinding //bind the ProjectCreationActivity KT and XML files
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Call the method to set up click listeners
        setupClickListeners()

    }

    private fun setupClickListeners() {
        // Find the "Add" button
        val addButton = findViewById<Button>(R.id.btn_Add)

        // Set a click listener for the "Add" button
        addButton.setOnClickListener {
            // Call the method to handle the click event
            handleAddButtonClick()
        }
    }

    private fun handleAddButtonClick() {
        // Find all EditText views
        val projectNameEditText = findViewById<EditText>(R.id.ed_Project_Name)
        val organizationNameEditText = findViewById<EditText>(R.id.ed_Organization_Name)
        val startDateEditText = findViewById<EditText>(R.id.ed_Start_Date)
        val setColorEditText = findViewById<EditText>(R.id.ed_Set_Colour)
        val hourlyRateEditText = findViewById<EditText>(R.id.ed_Hourly_Rate)
        val descriptionEditText = findViewById<EditText>(R.id.ed_Description)

        // Get text from EditText views
        val projectName = projectNameEditText.text.toString()
        val organizationName = organizationNameEditText.text.toString()
        val startDate = startDateEditText.text.toString()
        val setColor = setColorEditText.text.toString()
        val hourlyRate = hourlyRateEditText.text.toString()
        val description = descriptionEditText.text.toString()



        // Define the Firestore collection
        val collection = firestoreInstance.collection("projects")

        // Store the project data in Firestore
        val projectData = hashMapOf(
            "projectName" to projectName,
            "organizationName" to organizationName,
            "startDate" to startDate,
            "setColor" to setColor,
            "hourlyRate" to hourlyRate,
            "description" to description
        )

        // Add the project data to Firestore
        collection.add(projectData)
            .addOnSuccessListener { documentReference ->
                // Data successfully stored in Firestore
                Log.d("ProjectCreationActivity", "Project data stored successfully. Document ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Error storing data in Firestore
                Log.e("ProjectCreationActivity", "Error storing project data: $e")
            }

        retrieveDataFromFirestore()
    }

    private fun retrieveDataFromFirestore() {
        // Define the Firestore collection
        val collection = firestoreInstance.collection("projects")

        // Retrieve data from Firestore
        collection.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // Log each document's data
                    Log.d("ProjectCreationActivity", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { e ->
                // Error retrieving data from Firestore
                Log.e("ProjectCreationActivity", "Error retrieving project data: $e")
            }
    }



}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\