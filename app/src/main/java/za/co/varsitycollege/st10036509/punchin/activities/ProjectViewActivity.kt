package za.co.varsitycollege.st10036509.punchin.activities
/*
AUTHOR: Leonard Bester
CREATED: 29/04/2024
LAST MODIFIED: 29/04/2024
*/
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityProjectViewBinding


class ProjectViewActivity : AppCompatActivity(){

    private lateinit var binding: ActivityProjectViewBinding // Bind the ProjectViewActivity with the KT and XML files

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityProjectViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Dummy data for demonstration
        val names = arrayOf("Project 1", "Project 2", "Project 3")
        val timesheets = arrayOf("10", "15", "20")
        val hoursLogged = arrayOf("50", "75", "100")

        binding.apply{

            // Combine the data into a single string
            val combinedText = StringBuilder()
            for (i in names.indices) {
                combinedText.append("Name: ${names[i]}, Timesheets: ${timesheets[i]}, Hours Logged: ${hoursLogged[i]}\n")
            }

            // Create a TextView and set its text to the combined string
            val textView = TextView(this@ProjectViewActivity)
            textView.text = combinedText.toString()

            // Get reference to the LinearLayout inside ScrollView
            val linearLayout = svProjects

            // Add the TextView to the LinearLayout
            linearLayout.addView(textView)
        }
    }
}

