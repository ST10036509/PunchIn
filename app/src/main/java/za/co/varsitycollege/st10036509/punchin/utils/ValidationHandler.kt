/*
AUTHOR: Ethan Schoonbee
CREATED: 27/04/2024
LAST MODIFIED: 27/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.utils

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import za.co.varsitycollege.st10036509.punchin.models.AuthenticationModel


/**
 * Class to handle Authentication Functionality
 */
class ValidationHandler() : ViewModel() {

    //strings to use
    private companion object {
        const val MSG_NULL_INPUTS_ERROR = "Please fill out all inputs!"
        const val MSG_USERNAME_TAKEN_ERROR = "The user you entered is already taken!"
        const val MSG_EMAIL_ERROR = "The email you entered is not valid!"
        const val MSG_PASSWORD_MISSMATCH_ERROR = "The passwords do not match!"
        const val MSG_PASSWORD_DIGIT_ERROR = "Password must contain at least one digit."
        const val MSG_PASSWORD_LOWERCASE_ERROR = "Password must contain at least one lowercase letter."
        const val MSG_PASSWORD_UPPERCASE_ERROR = "Password must contain at least one uppercase letter."
        const val MSG_PASSWORD_LENGTH_ERROR = "Password must be at least 6 characters long."
    }


    //__________________________________________________________________________________________________runValidationChecks


    /**
     * Method to check for inout validity
     * @param String Username
     * @param String Email
     * @param String Password
     * @param String PasswordConfirmation
     * @return Boolean {true} there are no validation errors/ {false} there are validation errors
     */
    suspend fun runValidationChecks(username: String, email: String, password: String, passwordConfirmation: String): Pair<Boolean, String> {

        val (isValidPasswordFormat, passwordFormatErrorMessage) = validatePasswordFormat(password)

        //check if all inputs are valid
        if (checkForNullInputs(username, email, password, passwordConfirmation)) {

            return Pair(false, ValidationHandler.MSG_NULL_INPUTS_ERROR)

        }

        if (!validateUsername(username)) {

            return Pair(false, ValidationHandler.MSG_USERNAME_TAKEN_ERROR)

        }

        //check if the email is in the correct format
        if (!validateEmail(email)) {

            return Pair(false, ValidationHandler.MSG_EMAIL_ERROR)

        }

        //check if the password is in the correct format
        if (!isValidPasswordFormat) {

            return Pair(false, passwordFormatErrorMessage)

        }

        //check if the password and password confirmation match
        if (!validatePasswordsMatch(password, passwordConfirmation)) {

            return Pair(false, ValidationHandler.MSG_PASSWORD_MISSMATCH_ERROR)

        }

        //if all inputs are valid
        return Pair(true, "")

    }


    //__________________________________________________________________________________________________checkForNullInputs


    /**
     * Method to check for null inputs
     * @param String Username
     * @param String Email
     * @param String Password
     * @param String PasswordConfirmation
     * @return Boolean {true} there are no null values/ {false} there are null values present
     */
    private fun checkForNullInputs(username: String, email: String, password: String, passwordConfirmation: String): Boolean {

        return (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirmation.isEmpty())

    }


//__________________________________________________________________________________________________validateUsername


    /**
     * Method to check for null inputs
     * @param String Username
     * @return Boolean {true} username is unique/ {false} username already exists
     */
    private suspend fun validateUsername(username: String): Boolean {

        val usersCollection = FirestoreConnection.getDatabaseInstance().collection("users")

        return try {

            val querySnapshot = usersCollection.whereEqualTo("username", username).get().await()
            querySnapshot.isEmpty


        } catch (e: FirebaseFirestoreException) {

            false

        }

    }


//__________________________________________________________________________________________________validateEmail


    /**
     * Method to validate the given Email
     * @param String Email
     * @return Boolean {true} email is valid/ {false} email is not valid
     */
    private fun validateEmail(email: String): Boolean {

        //format for emails
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$"
        return(email.matches(emailRegex.toRegex()))

    }


//__________________________________________________________________________________________________validatePasswordsMatch


    /**
     * Method to validate the passwords match
     * @param String Password
     * @param String PasswordConfirmation
     * @return Boolean {true} passwords match/ {false} passwords don't match
     */
    private fun validatePasswordsMatch(password: String, passwordConfirmation: String): Boolean {

        return(password == passwordConfirmation)

    }


//__________________________________________________________________________________________________validatePasswordFormat


    /**
     * Method to validate the password matches the required format
     * @param String Password
     * @return Boolean {true} passwords is in the correct format/ {false} passwords is not in the correct format
     */
    private fun validatePasswordFormat(password: String): Pair<Boolean, String> {

        // Check if password meets Firebase Authentication requirements
        val minLength = 6
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }

        // Validate password format based on requirements
        if (password.length < minLength) {

            return Pair(false, ValidationHandler.MSG_PASSWORD_LENGTH_ERROR)

        }
        if (!hasUpperCase) {

            return Pair(false, ValidationHandler.MSG_PASSWORD_UPPERCASE_ERROR)

        }
        if (!hasLowerCase) {

            return Pair(false, ValidationHandler.MSG_PASSWORD_LOWERCASE_ERROR)

        }
        if (!hasDigit) {

            return Pair(false, ValidationHandler.MSG_PASSWORD_DIGIT_ERROR)

        }

        return Pair(true, "")

    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\