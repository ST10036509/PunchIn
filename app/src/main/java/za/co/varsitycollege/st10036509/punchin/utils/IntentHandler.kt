/*
AUTHOR: Ethan Schoonbee
CREATED: 23/04/2024
LAST MODIFIED: 23/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.utils

import android.content.Context
import android.content.Intent

/**
 * Class to handle intents
 * @param Context The Current Application Context.
 */
class IntentHandler(private val context: Context) {
    /**
     * Method to handle basic activity navigation with no data
     * @param Class<*> The Class name of the Activity to open.
     */
    fun openActivityIntent(activity: Class<*>) {
        val intent = Intent(context, activity)//create intent object
        context.startActivity(intent)//open activity
    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\