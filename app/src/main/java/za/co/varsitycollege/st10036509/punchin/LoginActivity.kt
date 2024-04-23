/*
AUTHOR: Ethan Schoonbee
CREATED: 23/04/2024
LAST MODIFIED: 23/04/2024
 */

package za.co.varsitycollege.st10036509.punchin

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityLoginBinding


/**
 * Class to handle Login Activity Functionality
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding//bind the LoginActivity KT and XML files
    var count = 1

//__________________________________________________________________________________________________onCreate

    /**
     * onCreate Method for Login Activity Page (entry point)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)//inflate UI
        setContentView(binding.root)

        //call password toggle click listener
        setupPasswordVisibilityToggle()
    }


//__________________________________________________________________________________________________setupPasswordVisibilityToggle



    /**
     * On Click Event for the Password Visibility Toggle
     */
    private fun setupPasswordVisibilityToggle() {

        //event listener for imTogglePassword click
        binding.imgTogglePassword.setOnClickListener() {

            //check if the etPassword is visible
            val isPasswordVisible = binding.etPassword.transformationMethod != PasswordTransformationMethod.getInstance()

            if (isPasswordVisible) {
                // if the Password is visible, show eye open toggle image
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.imgTogglePassword.setImageResource(R.drawable.eye_open)
            } else {
                // if the Password is not visible, show eye closed toggle image
                binding.etPassword.transformationMethod = null
                binding.imgTogglePassword.setImageResource(R.drawable.eye_closed)
            }

            // reset cursor position to the end of the string
            binding.etPassword.setSelection(binding.etPassword.text.length)

        }
    }



//__________________________________________________________________________________________________



}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\