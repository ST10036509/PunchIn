/*
AUTHOR: Ethan Schoonbee
CREATED: 23/04/2024
LAST MODIFIED: 23/04/2024
 */

package za.co.varsitycollege.st10036509.punchin

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityGoalsBinding

/**
 * Class to handle Goals Activity Functionality
 */
class GoalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalsBinding//bind the GoalsActivity KT and XML files
    private lateinit var headerViewBindings: HeaderViewBindings//initialize HeaderViewBindings
    private val headerSettingsImageView: ImageView by lazy { headerViewBindings.headerSettings }//initialize an instance of the header binds, and pass the binding class (GoalsActivity)//only once its accessed (lazy)


//__________________________________________________________________________________________________onCreate


    /**
     * onCreate Method for Login Activity Page (entry point)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)//inflate UI
        setContentView(binding.root)

        headerViewBindings = HeaderViewBindings(binding)//initialize and instance of the binding, pass the binding class

        //call password toggle click listener
        setupSettingsAccess()//
    }
//__________________________________________________________________________________________________setupSettingsAccess
    /**
     * Method to Call the Settings onClick Event Handler
     */
    fun setupSettingsAccess() {

        //Event Handler for Settings On Click Event
        headerSettingsImageView.setOnClickListener() {
            Toast.makeText(this@GoalsActivity, "Settings Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\