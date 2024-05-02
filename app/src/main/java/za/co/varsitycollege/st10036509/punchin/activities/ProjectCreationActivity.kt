/*
AUTHOR: Leonard Bester
CREATED: 23/04/2024
LAST MODIFIED: 01/05/2024
 */
package za.co.varsitycollege.st10036509.punchin.activities


import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityProjectCreationBinding
import za.co.varsitycollege.st10036509.punchin.models.ProjectsModel
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler
import za.co.varsitycollege.st10036509.punchin.utils.ValidationHandler


class ProjectCreationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectCreationBinding
    private lateinit var projectModel: ProjectsModel
    private var currentUser: FirebaseUser? = null
    private lateinit var validationHandler: ValidationHandler
    private var progressDialog: ProgressDialog? = null//create a loading dialog instance
    private lateinit var loadingDialogHandler: LoadDialogHandler//setup an intent handler for navigating pages
    private lateinit var toaster: ToastHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialogHandler = LoadDialogHandler(this@ProjectCreationActivity, progressDialog)//initialise the loading dialog
        //initialise the validation handler
        validationHandler = ValidationHandler()
        // Initialize FirebaseAuth
        toaster = ToastHandler(this@ProjectCreationActivity)
        val auth = FirebaseAuth.getInstance()



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
        val hourlyRateText= hourlyRateEditText.text.toString()
        val description = descriptionEditText.text.toString()


        validateNullInput(projectName,startDate,setColor,hourlyRateText,description)
    }

    private fun validateNullInput(name: String, date : String, color : String, rate : String, description: String){

        val checkForNull =  validationHandler.checkForNullInputs(name,date,color,rate,description)

        if (!checkForNull){
            validateDouble(name, date, color, rate, description)
        }else{
            // Display error message here & clear input
            toaster.showToast("Do not leave any fields empty...")
        }
    }

    private fun validateDouble(name: String, date : String, color : String, rate : String, description: String){

        val checkDouble = validationHandler.validateDouble(rate)

        if (checkDouble) {
            // Validation succeeded
            Log.d("Validation", "Hourly rate validation succeeded: $rate")
            // Proceed with other operations or write to the database here
            writeToDataBase(name,date,color,rate,description)

        } else {
            // Validation failed
            Log.d("Validation", "Hourly rate validation failed: $rate")
            // Handle the failure, such as displaying an error message to the user
            toaster.showToast("Type in a number for Rate...")
        }

    }


    private fun writeToDataBase(name: String, date : String, color : String, rate : String, description: String){

        val projectName: String = name
        val startDate: String = date
        val setColor: String = color
        val hourlyRate: Double = rate.toDouble()
        val description : String = description


        // Check if the current user is not null
        currentUser?.let { user ->
            // Get user id
            val userId = user.uid

            // Set project data using the setData method of ProjectsModel

          //  projectModel.setData(projectName, startDate, setColor, hourlyRate, description, userId, )

            // Initialize ProjectsModel
            projectModel = ProjectsModel(projectName, startDate, setColor, hourlyRate, description, 0,0,0.0,userId,)
            // Call the method to write project data to Firestore
            projectModel.writeDataToFirestore()
            toaster.showToast("Project added!")
            clearInputFields()
        }
    }

    fun clearInputFields(){

        // Find all EditText views
        val projectNameEditText = findViewById<EditText>(R.id.ed_Project_Name)
        val startDateEditText = findViewById<EditText>(R.id.ed_Start_Date)
        val setColorEditText = findViewById<EditText>(R.id.ed_Set_Colour)
        val hourlyRateEditText = findViewById<EditText>(R.id.ed_Hourly_Rate)
        val descriptionEditText = findViewById<EditText>(R.id.ed_Description)

        // Set text of each EditText to an empty string
        projectNameEditText.setText("")
        startDateEditText.setText("")
        setColorEditText.setText("")
        hourlyRateEditText.setText("")
        descriptionEditText.setText("")
    }
}


//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\