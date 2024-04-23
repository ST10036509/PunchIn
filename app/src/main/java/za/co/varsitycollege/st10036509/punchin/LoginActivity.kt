/*
AUTHOR: Ethan Schoonbee
CREATED: 23/04/2024
LAST MODIFIED: 23/04/2024
 */

package za.co.varsitycollege.st10036509.punchin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityLoginBinding

/**
 * Class to handle Login Activity Functionality
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    /**
     * onCreate Method for Login Activity Page (entry point)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\