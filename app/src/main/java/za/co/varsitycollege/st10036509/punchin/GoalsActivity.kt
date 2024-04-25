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
    private lateinit var layoutViewBindingHelper: LayoutViewBindingHelper//initialize HeaderViewBindings
    private val headerSettingsButton: ImageView by lazy { layoutViewBindingHelper.headerSettings }//initialize a reference to the settings button bind only once its accessed (lazy)


//__________________________________________________________________________________________________onCreate


    /**
     * onCreate Method for Login Activity Page (entry point)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)//inflate UI
        setContentView(binding.root)

        //initialize an instance of the header binds, and pass the binding class (GoalsActivity)
        layoutViewBindingHelper = LayoutViewBindingHelper(this.binding)

        //HEADER AND FOOTER NAVBAR CONTROLS:
        //call settings button onClick listener
        setupSettingsAccessControl()//Header
        //----------------------------------

        //minimum increment button onClick listener
        setupMinimumIncrementControl()
        //minimum decrement button onClick listener
        setupMinimumDecrementControl()
        //maximum increment button onClick listener
        setupMaximumIncrementControl()
        //maximum decrement button onClick listener
        setupMaximumDecrementControl()

        //update button onClick listener
        setupGoalUpdaterControl()

    }


//__________________________________________________________________________________________________setupSettingsAccess


    /**
     * On Click Event for the Settings Button
     */
    private fun setupSettingsAccessControl() {

        //Event Handler for Settings On Click Event
        headerSettingsButton.setOnClickListener() {
            Toast.makeText(this@GoalsActivity,
                "Settings Coming Soon!",
                Toast.LENGTH_SHORT).show()//show message
        }

    }


//__________________________________________________________________________________________________setupMinimumIncrementControl


    /**
     * On Click Event for the Minimum Goal Increment Button
     */
    private fun setupMinimumIncrementControl() {

        //Event Handler for Minimum Goal Increment On Click Event
        binding.imgbMinIncrement.setOnClickListener() {
            Toast.makeText(this@GoalsActivity,
                "Increased Minimum Goal",
                Toast.LENGTH_SHORT).show()//show message
        }

    }


//__________________________________________________________________________________________________setupMinimumDecrementControl


    /**
     * On Click Event for the Minimum Goal Decrement Button
     */
    private fun setupMinimumDecrementControl() {

        //Event Handler for Minimum Goal Decrement On Click Event
        binding.imgbMinDecrement.setOnClickListener() {
            Toast.makeText(this@GoalsActivity,
                "Decreased Minimum Goal",
                Toast.LENGTH_SHORT).show()//show message
        }

    }


//__________________________________________________________________________________________________setupMaximumIncrementControl


    /**
     * On Click Event for the Maximum Goal Increment Button
     */
    private fun setupMaximumIncrementControl() {

        //Event Handler for Maximum Goal Increment On Click Event
        binding.imgbMaxIncrement.setOnClickListener() {
            Toast.makeText(this@GoalsActivity,
                "Increased Maximum Goal",
                Toast.LENGTH_SHORT).show()//show message
        }

    }


//__________________________________________________________________________________________________setupMaximumDecrementControl


    /**
     * On Click Event for the Maximum Goal Decrement Button
     */
    private fun setupMaximumDecrementControl() {

        //Event Handler for Maximum Goal Decrement On Click Event
        binding.imgbMaxDecrement.setOnClickListener() {
            Toast.makeText(this@GoalsActivity,
                "Decreased Maximum Goal",
                Toast.LENGTH_SHORT).show()//show message
        }

    }

    private fun setupGoalUpdaterControl() {

        //Event Handler for Update Button On Click Event
        binding.llUpdateButton.setOnClickListener() {
            Toast.makeText(this@GoalsActivity,
                "Updated Goal",
                Toast.LENGTH_SHORT).show()//show message
        }

    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\