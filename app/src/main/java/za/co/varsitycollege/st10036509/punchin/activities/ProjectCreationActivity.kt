/*
AUTHOR: Leonard Bester
CREATED: 23/04/2024
LAST MODIFIED: 01/05/2024
 */
package za.co.varsitycollege.st10036509.punchin.activities

import android.app.DatePickerDialog
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
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.utils.LoadDialogHandler
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler
import za.co.varsitycollege.st10036509.punchin.utils.ValidationHandler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProjectCreationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectCreationBinding
    private lateinit var projectModel: ProjectsModel
    private var currentUser: FirebaseUser? = null
    private lateinit var validationHandler: ValidationHandler
    private var progressDialog: ProgressDialog? = null
    private lateinit var loadingDialogHandler: LoadDialogHandler
    private lateinit var toaster: ToastHandler
    private lateinit var intentHandler: IntentHandler
    private var startDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intentHandler = IntentHandler(this@ProjectCreationActivity)
        loadingDialogHandler = LoadDialogHandler(this@ProjectCreationActivity, progressDialog)
        validationHandler = ValidationHandler()
        toaster = ToastHandler(this@ProjectCreationActivity)
        val auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val addButton = findViewById<Button>(R.id.btn_Add)
        val returnButton = findViewById<Button>(R.id.btn_Return)
        val addDateButton = binding.llStartDate

        addButton.setOnClickListener {
            handleAddButtonClick()
        }

        returnButton.setOnClickListener {
            handleReturnClick()
        }

        addDateButton.setOnClickListener {
            showDatePicker()
        }

    }

    private fun showDatePicker() {
        // Get current date to set as default in the picker
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog instance with current date as default
        val datePickerDialog = DatePickerDialog(
            this@ProjectCreationActivity,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                // Set the selected date in the startDate variable
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                startDate = selectedDate.time // Update the startDate variable with the selected date
                // If you need to update the UI, you can do it here
                binding.edStartDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time))
            },
            year,
            month,
            day
        )

        // Show the DatePickerDialog
        datePickerDialog.show()
    }





    private fun handleReturnClick() {
        toaster.showToast("Returning to Projects page")
        intentHandler.openActivityIntent(ProjectViewActivity::class.java)
    }

    private fun handleAddButtonClick() {
        val projectNameEditText = binding.edProjectName
        val startDateEditText = binding.edStartDate
        val setColorEditText = binding.edSetColour
        val hourlyRateEditText = binding.edHourlyRate
        val descriptionEditText = binding.edDescription

        val projectName = projectNameEditText.text.toString()
        val startDate = startDateEditText.text.toString()
        val setColor = setColorEditText.text.toString()
        val hourlyRateText = hourlyRateEditText.text.toString()
        val description = descriptionEditText.text.toString()

        validateNullInput(projectName, startDate, setColor, hourlyRateText, description)
    }


    private fun validateNullInput(name: String, date: String, color: String, rate: String, description: String) {
        val checkForNull = validationHandler.checkForNullInputs(name, date, color, rate, description)

        if (!checkForNull) {
            validateDouble(name, date, color, rate, description)
        } else {
            toaster.showToast("Do not leave any fields empty...")
        }
    }

    private fun validateDouble(name: String, date: String, color: String, rate: String, description: String) {
        val checkDouble = validationHandler.validateDouble(rate)

        if (checkDouble) {
            Log.d("Validation", "Hourly rate validation succeeded: $rate")
            writeToDataBase(name, date, color, rate, description)
        } else {
            Log.d("Validation", "Hourly rate validation failed: $rate")
            toaster.showToast("Type in a number for Rate...")
        }
    }

    private fun writeToDataBase(name: String, date : String, color: String, rate: String, description: String) {
        val projectName: String = name
        val setColor: String = color
        val hourlyRate: Double = rate.toDouble()
        val description: String = description

        currentUser?.let { user ->
            val userId = user.uid

            loadingDialogHandler.showLoadingDialog("Saving your project")
            projectModel = ProjectsModel(projectName, startDate, setColor, hourlyRate, description, 0, 0, 0.0, userId)
            projectModel.writeDataToFirestore()
            loadingDialogHandler.dismissLoadingDialog()
            toaster.showToast("Project added!")
            clearInputFields()
        }
    }

    private fun clearInputFields() {
        val projectNameEditText = findViewById<EditText>(R.id.ed_Project_Name)
        val startDateEditText = binding.edStartDate
        val setColorEditText = findViewById<EditText>(R.id.ed_Set_Colour)
        val hourlyRateEditText = findViewById<EditText>(R.id.ed_Hourly_Rate)
        val descriptionEditText = findViewById<EditText>(R.id.ed_Description)

        projectNameEditText.setText("")
        startDateEditText.setText("")
        setColorEditText.setText("")
        hourlyRateEditText.setText("")
        descriptionEditText.setText("")
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