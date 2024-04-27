/*
AUTHOR: Ethan Schoonbee
CREATED: 27/04/2024
LAST MODIFIED: 27/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.utils

import android.app.ProgressDialog
import android.content.Context


/**
 * Class to handle loading dialog functionality
 */
class LoadDialogHandler(context: Context, dialog: ProgressDialog?) {

    private var progressDialog = dialog
    private var displayContext = context

//__________________________________________________________________________________________________showLoadingDialog


    /**
     * Method to show loading dialog
     */
    fun showLoadingDialog(message: String) {

        progressDialog?.dismiss()

        progressDialog = ProgressDialog(displayContext).apply {

            setTitle(message)
            setCancelable(false)
            show()

        }
    }


//__________________________________________________________________________________________________dismissLoadingDialog


    /**
     * Method to discard loading dialog
     */
    fun dismissLoadingDialog() {

        progressDialog?.dismiss()

        progressDialog = null

    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\