/*
AUTHOR: Ethan Schoonbee
CREATED: 02/05/2024
LAST MODIFIED: 02/05/2024
 */


package za.co.varsitycollege.st10036509.punchin.activities

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivitySettingsBinding
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel
import za.co.varsitycollege.st10036509.punchin.models.UserModel
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler
import za.co.varsitycollege.st10036509.punchin.utils.ToastHandler

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding//bind the GoalsActivity KT and XML files
    private lateinit var authModel: AuthenticationModel//get the instance of the authentication model
    private lateinit var toaster: ToastHandler//create a ToastHandler to show toast messages
    private lateinit var intentHandler: IntentHandler//setup an intent handler for navigating pages

    private companion object {
        const val MSG_SIGN_OUT_SUCCESS = "Signed Out Successfully!"
        const val MSG_SIGN_OUT_FAILED = "Something Went Wrong... Try Again!"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)//inflate UI
        setContentView(binding.root)

        authModel = AuthenticationModel()//get the instance of the authentication model
        toaster = ToastHandler(this@SettingsActivity)//create a ToastHandler to show toast messages
        intentHandler = IntentHandler(this@SettingsActivity)//setup an intent handler for navigating pages

        binding.tvUsername.text = UserModel.username

        //setup listeners for ui controls
        setupListeners()

        updateBadge()

        // Apply shine animation
        val shineAnimation = AnimationUtils.loadAnimation(this, R.anim.shine_animation)
        binding.imgShine.startAnimation(shineAnimation)
    }


//__________________________________________________________________________________________________updateBadge


    /**
     * Method to update badge based on users points
     */
    private fun updateBadge() {

        binding.apply {

            fetchUserPoints() { points ->
                val color = when (points) {
                in 0..100 -> ContextCompat.getColor(root.context, R.color.Badge_1_Bronze)
                in 101..500 -> ContextCompat.getColor(root.context, R.color.Badge_2_Silver)
                in 501..2000 -> ContextCompat.getColor(root.context, R.color.Badge_3_Gold)
                in 2001..10000 -> ContextCompat.getColor(root.context, R.color.Badge_4_Diamond)
                else -> ContextCompat.getColor(root.context, R.color.Badge_5_Obsidian)
            }
                ImageViewCompat.setImageTintList(imgBadge, ColorStateList.valueOf(color))
            }
        }
    }

//__________________________________________________________________________________________________fetchUserPoints

    private fun fetchUserPoints(callback: (Int) -> Unit) {

        val currentUser = authModel.getCurrentUser()
        val firestoreInstance = FirestoreConnection.getDatabaseInstance()

        // Ensure currentUser is not null
        val currentUid = currentUser?.uid
        if (currentUid == null) {
            callback(0)
            return
        }

        // Reference to the user's document in Firestore
        val userRef = firestoreInstance.collection("users").document(currentUid)

        // Fetch the document
        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Retrieve the points from the document
                    val points = documentSnapshot.getLong("points")?.toInt() ?: 0
                    callback(points)
                } else {
                    // Document does not exist
                    callback(0)
                }
            }
            .addOnFailureListener { exception ->
                // Log the error and return 0 points as a fallback
                exception.printStackTrace()
                callback(0)
            }
    }

//__________________________________________________________________________________________________setupClickListeners


    /**
     * Method to setup listeners for UI controls
     */
    private fun setupListeners() {

        //apply binding to the following lines
        binding.apply {

            //pass appropriate function when onClick occurs
            llSignOutButton.setOnClickListener { signOutUser() }
            llReturnButton.setOnClickListener { returnToHomePage() }

        }
    }


//__________________________________________________________________________________________________signOutUser


    private fun signOutUser() {

        //sign the user out of their account
        authModel.signOut() { success ->
            if (success) {

                //launch the register page
                intentHandler.openActivityIntent(LoginActivity::class.java)
                toaster.showToast(SettingsActivity.MSG_SIGN_OUT_SUCCESS)
                finish()

            } else {

                //display failure message
                toaster.showToast(SettingsActivity.MSG_SIGN_OUT_FAILED)

            }
        }
    }


//__________________________________________________________________________________________________returnToHomePage


    private fun returnToHomePage() {

        //launch the register page
        intentHandler.openActivityIntent(TimesheetViewActivity::class.java)

    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\