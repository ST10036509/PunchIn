/*
AUTHOR: Ethan Schoonbee
CREATED: 24/04/2024
LAST MODIFIED: 25/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.utils

import android.content.Context
import android.widget.ImageButton
import android.widget.Toast
import androidx.viewbinding.ViewBinding
import za.co.varsitycollege.st10036509.punchin.R

/**
 * Class to handle Hold Header View Binds
 */
class NavbarViewBindingHelper (private val viewBinding: ViewBinding){

    //bind for header settings component
    private val headerSettings: ImageButton? by lazy {
        viewBinding.root.findViewById<ImageButton>(R.id.imgb_HeaderSettingsButton)!!
    }

    //bind for footer home component
    private val footerHome: ImageButton? by lazy {
        viewBinding.root.findViewById<ImageButton>(R.id.imgb_FooterHomeButton)!!
    }

    //bind for footer projects component
    private val footerProjects: ImageButton? by lazy {
        viewBinding.root.findViewById<ImageButton>(R.id.imgb_FooterProjectsButton)!!
    }

    //bind for footer goals component
    private val footerGoals: ImageButton? by lazy {
        viewBinding.root.findViewById<ImageButton>(R.id.imgb_FooterGoalsButton)!!
    }

    //bind for footer analytics component
    private val footerAnalytics: ImageButton? by lazy {
        viewBinding.root.findViewById<ImageButton>(R.id.imgb_FooterAnalyticsButton)!!
    }


//__________________________________________________________________________________________________setupNavBarAccessControls


    /**
     * Event Handler for both Header and Footer NavBar onClick Events
     * @param Context The Activity Context (this@ActivityName) for the UI page for which the NavBar controls need to be handled
     */
    fun setupNavBarAccessControls(context: Context) {
        //handle settings button onClick listener
        headerSettings?.let {buttonAccessControl(it, "Settings Coming Soon!", context)}//HEADER

        //handle home button onClick listener
        footerHome?.let {buttonAccessControl(it, "Home Page Opening...", context)}//FOOTER

        //handle projects button onClick listener
        footerProjects?.let {buttonAccessControl(it, "Projects Page Opening...", context)}//FOOTER

        //handle goals button onClick listener
        footerGoals?.let {buttonAccessControl(it, "Goals Page Opening...", context)}//FOOTER

        //handle analytics button onClick listener
        footerAnalytics?.let {buttonAccessControl(it, "Analytics Page Opening...", context)}//FOOTER

    }


//__________________________________________________________________________________________________buttonAccessControl


    /**
     * On Click Event for the Header NavBar Settings Button
     * @param ImageButton The ImageButton component to set teh onClick Event to.
     * @param String The Message to display via Toast
     * @param Context The Activity Context (this@ActivityName) for the UI page for which the NavBar controls need to be handled
     */
    private fun buttonAccessControl(button: ImageButton, message: String, activity: Context) {

        button.setOnClickListener {
            Toast.makeText(activity, message,
                Toast.LENGTH_SHORT).show()
        }

    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\