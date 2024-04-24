package za.co.varsitycollege.st10036509.punchin

import android.widget.ImageView
import androidx.viewbinding.ViewBinding

/**
 * Class to handle Hold Header View Binds
 */
class HeaderViewBindings (private val viewBinding: ViewBinding){

//__________________________________________________________________________________________________headerSettings


    //bind for header settings component
    val headerSettings: ImageView
        get() = viewBinding.root.findViewById(R.id.img_HeaderSettings)

}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\