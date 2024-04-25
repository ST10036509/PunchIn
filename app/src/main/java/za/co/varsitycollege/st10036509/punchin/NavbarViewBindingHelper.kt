package za.co.varsitycollege.st10036509.punchin

import android.widget.ImageButton
import androidx.viewbinding.ViewBinding

/**
 * Class to handle Hold Header View Binds
 */
class NavbarViewBindingHelper (private val viewBinding: ViewBinding){




    //=========================HEADER COMPONENTS=============================|


    //bind for header settings component
    val headerSettings: ImageButton
        get() = viewBinding.root.findViewById(R.id.imgb_HeaderSettingsButton)


    //=======================================================================|

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //=========================FOOTER COMPONENTS=============================|


    //bind for footer home component
    val footerHome: ImageButton
        get() = viewBinding.root.findViewById(R.id.imgb_FooterHomeButton)


    //bind for footer projects component
    val footerProjects: ImageButton
        get() = viewBinding.root.findViewById(R.id.imgb_FooterProjectsButton)


    //bind for footer goals component
    val footerGoals: ImageButton
        get() = viewBinding.root.findViewById(R.id.imgb_FooterGoalsButton)


    //bind for footer analytics component
    val footerAnalytics: ImageButton
        get() = viewBinding.root.findViewById(R.id.imgb_FooterAnalyticsButton)


    //=======================================================================|


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\