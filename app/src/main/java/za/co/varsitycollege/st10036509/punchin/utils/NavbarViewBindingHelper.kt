/*
AUTHOR: Ethan Schoonbee
CREATED: 24/04/2024
LAST MODIFIED: 25/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.utils

import android.app.Activity
import android.content.Context
import android.widget.ImageButton
import androidx.viewbinding.ViewBinding
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.activities.GoalsActivity

/**
 * Class to handle Hold Header View Binds
 */
class NavbarViewBindingHelper (context: Context, binding: ViewBinding){

    private var intentHandler = IntentHandler(context)//initialise an IntentHandler for page navigation
    private var toaster = ToastHandler(context)//initialise an ToastHandler for displaying toast messages

    //get the references for each navbar button
    private val timesheetsButtonId: ImageButton? = binding.root.findViewById<ImageButton>(R.id.imgb_FooterHomeButton)
    private val projectsButtonId: ImageButton? = binding.root.findViewById<ImageButton>(R.id.imgb_FooterProjectsButton)
    private val goalsButtonId: ImageButton? = binding.root.findViewById<ImageButton>(R.id.imgb_FooterGoalsButton)
    private val analyticsButtonId: ImageButton? = binding.root.findViewById<ImageButton>(R.id.imgb_FooterAnalyticsButton)
    private val settingsButtonId: ImageButton? = binding.root.findViewById<ImageButton>(R.id.imgb_HeaderSettingsButton)


//__________________________________________________________________________________________________setUpNavBarOnClickEvents


    /**
     * Method to setup listeners for NavBars
     */
    fun setUpNavBarOnClickEvents() {

        //create onClick methods:
        buttonAddOnClickEvent(timesheetsButtonId)

        buttonAddOnClickEvent(projectsButtonId)

        buttonAddOnClickEvent(goalsButtonId)

        buttonAddOnClickEvent(analyticsButtonId)

        buttonAddOnClickEvent(settingsButtonId)
    }


//__________________________________________________________________________________________________buttonAddOnClickEvent


    /**
     * On Click Event Setter for the NavBar
     * @param ImageButton The ImageButton component to set teh onClick Event to.
     */
    private fun buttonAddOnClickEvent(button: ImageButton?) {

        //set the onClick method for the given button to the handleButtonClickEvents method call
        button?.setOnClickListener{
            handleButtonClickEvents(button)
        }
    }


//__________________________________________________________________________________________________handleButtonClickEvents


    /**
     * Method to handle on click event of passed button
     * @param ImageButton button that was clicked
     */
    private fun handleButtonClickEvents(button: ImageButton?) {
        when (button) {

            //perform task based on button clicked:
            timesheetsButtonId -> {
                //intentHandler.openActivityIntent(::class:java)
                toaster.showToast("Opening Timesheets Page...")
            }

            projectsButtonId -> {
                //intentHandler.openActivityIntent(::class:java)
                toaster.showToast("Opening Projects Page...")
            }

            goalsButtonId -> {
                intentHandler.openActivityIntent(GoalsActivity::class.java)
            }

            analyticsButtonId -> {
                //intentHandler.openActivityIntent(::class:java)
                toaster.showToast("Analytics Page Coming Soon!")
            }

            settingsButtonId -> {
                //intentHandler.openActivityIntent(::class:java)
                toaster.showToast("Settings Page Coming Soon!")
            }
        }
    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\