/*
AUTHOR: Ethan Schoonbee
CREATED: 27/04/2024
LAST MODIFIED: 27/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.utils

import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import androidx.viewbinding.ViewBinding
import za.co.varsitycollege.st10036509.punchin.R

class PasswordVisibilityToggler() {

    //__________________________________________________________________________________________________togglePasswordVisibility


    /**
     * On Click Event for the Password Visibility Toggle
     */
    fun <T: ViewBinding> togglePasswordVisibility(binding: T, inputBox: EditText, toggleButton: ImageView) {

        //apply binding to the following lines
        binding.apply {

            //check if the text is visible
            val isPasswordVisible = inputBox.transformationMethod != PasswordTransformationMethod.getInstance()

            //update visibility and eye icon
            inputBox.transformationMethod =
                    // if the password is visible, show eye open toggle image
                if (isPasswordVisible) PasswordTransformationMethod.getInstance() else null
            toggleButton.setImageResource(
                // if the password is visible, show eye open image else show a eye closed image
                if (isPasswordVisible) R.drawable.eye_closed else R.drawable.eye_open

            )
            // reset cursor position to the end of the string
            inputBox.setSelection(inputBox.text.length)

        }
    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\