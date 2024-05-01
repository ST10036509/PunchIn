/*
AUTHOR: Leonard Bester
CREATED: 23/04/2024
LAST MODIFIED: 01/05/2024
 */
package za.co.varsitycollege.st10036509.punchin.activities


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityProjectCreationBinding
import za.co.varsitycollege.st10036509.punchin.models.ProjectsModel

class ProjectCreationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectCreationBinding
    private lateinit var projectModel: ProjectsModel
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        val auth = FirebaseAuth.getInstance()

        // Initialize ProjectsModel
        projectModel = ProjectsModel("", "", "", "", "", 0,0,0,"",)



        // Get current user
        currentUser = auth.currentUser

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
        val startDateEditText = findViewById<EditText>(R.id.ed_Start_Date)
        val setColorEditText = findViewById<EditText>(R.id.ed_Set_Colour)
        val hourlyRateEditText = findViewById<EditText>(R.id.ed_Hourly_Rate)
        val descriptionEditText = findViewById<EditText>(R.id.ed_Description)

        // Get text from EditText views
        val projectName = projectNameEditText.text.toString()
        val startDate = startDateEditText.text.toString()
        val setColor = setColorEditText.text.toString()
        val hourlyRate = hourlyRateEditText.text.toString()
        val description = descriptionEditText.text.toString()

        // Check if the current user is not null
        currentUser?.let { user ->
            // Get user id
            val userId = user.uid

            // Set project data using the setData method of ProjectsModel
            projectModel.setData(projectName, startDate, setColor, hourlyRate, description, userId, )

            // Call the method to write project data to Firestore
            projectModel.writeDataToFirestore()

            clearInputFields()
        }
    }

    fun clearInputFields(){

        // Find all EditText views
        val projectNameEditText = findViewById<EditText>(R.id.ed_Project_Name)
        val organizationNameEditText = findViewById<EditText>(R.id.ed_Organization_Name)
        val startDateEditText = findViewById<EditText>(R.id.ed_Start_Date)
        val setColorEditText = findViewById<EditText>(R.id.ed_Set_Colour)
        val hourlyRateEditText = findViewById<EditText>(R.id.ed_Hourly_Rate)
        val descriptionEditText = findViewById<EditText>(R.id.ed_Description)

        // Set text of each EditText to an empty string
        projectNameEditText.setText("")
        organizationNameEditText.setText("")
        startDateEditText.setText("")
        setColorEditText.setText("")
        hourlyRateEditText.setText("")
        descriptionEditText.setText("")
    }
}


//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\