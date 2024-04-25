/*
AUTHOR: Ethan Schoonbee
CREATED: 23/04/2024
LAST MODIFIED: 25/04/2024
 */

package za.co.varsitycollege.st10036509.punchin

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityGoalsBinding

/**
 * Class to handle Goals Activity Functionality
 */
class GoalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalsBinding//bind the GoalsActivity KT and XML files
    private lateinit var navbarViewBindingHelper: NavbarViewBindingHelper//create a NavBarViewBindingsHelper class object

    //constant strings for toast messages
    private companion object {
        const val MSG_INC_MIN_GOAL = "Increased Minimum Goal"
        const val MSG_DEC_MIN_GOAL = "Decreased Minimum Goal"
        const val MSG_INC_MAX_GOAL = "Increased Maximum Goal"
        const val MSG_DEC_MAX_GOAL = "Decreased Maximum Goal"
        const val MSG_UPDATE_GOAL = "Updated Goal"
    }


//__________________________________________________________________________________________________onCreate


    /**
     * onCreate Method for Login Activity Page (entry point)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)//inflate UI
        setContentView(binding.root)

        //initialize an instance of the header binds, and pass the binding class (ActivityGoalsBinding)
        navbarViewBindingHelper = NavbarViewBindingHelper(this.binding)
        //onClick event handler for all header and footer navbar controls
        navbarViewBindingHelper.setupNavBarAccessControls(this@GoalsActivity)

        //setup listeners for ui controls
        setupListeners()

    }


//__________________________________________________________________________________________________setupClickListeners


    /**
     * Method to setup listeners for UI controls
     */
    private fun setupListeners() {

        //apply binding to the following lines
        binding.apply {

            //pass appropriate message to toast
            imgbMinDecrement.setOnClickListener { showToast(MSG_INC_MIN_GOAL) }
            imgbMinIncrement.setOnClickListener { showToast(MSG_DEC_MIN_GOAL) }
            imgbMaxDecrement.setOnClickListener { showToast(MSG_INC_MAX_GOAL) }
            imgbMaxIncrement.setOnClickListener { showToast(MSG_DEC_MAX_GOAL) }
            llUpdateButton.setOnClickListener { showToast(MSG_UPDATE_GOAL) }

        }

    }


//__________________________________________________________________________________________________showToast


    /**
     * Method to show the passed String message via Toast
     * @param String The message to show
     */
    private fun showToast(message: String) {

        Toast.makeText(this, message,
            Toast.LENGTH_SHORT).show()

    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\