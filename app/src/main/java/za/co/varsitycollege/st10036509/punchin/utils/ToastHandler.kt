package za.co.varsitycollege.st10036509.punchin.utils

import android.content.Context
import android.widget.Toast

class ToastHandler(private val context: Context) {

    //__________________________________________________________________________________________________showToast


    /**
     * Method to show the passed String message via Toast
     * @param String The message to show
     */
    fun showToast(message: String) {

        Toast.makeText(context, message,
            Toast.LENGTH_SHORT).show()

    }

}