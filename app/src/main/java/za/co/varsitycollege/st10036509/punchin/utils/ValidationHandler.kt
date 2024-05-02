/*
AUTHOR: Ethan Schoonbee
CREATED: 27/04/2024
LAST MODIFIED: 27/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.utils

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await


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

        if (usernameExists(username)) {

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
    fun checkForNullInputs(username: String, email: String, password: String, passwordConfirmation: String): Boolean {

        return (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordConfirmation.isEmpty())

    }

    /**
     * Overloaded version of checkForNullInputs method
     * @param String email or username
     * @param String password
     * @return Boolean
     */
    fun checkForNullInputs(emailOrUsername: String, password: String): Boolean {

        return (emailOrUsername.isEmpty() || password.isEmpty())

    }

    /**
     * Overloaded version of checkForNullInputs method
     * @param String project name
     * @param String start date
     * @param String project color
     * @param String hourly rate
     * @param String project description
     * @return Boolean
     */
    fun checkForNullInputs(name: String, date : String, color : String, rate : String, description: String): Boolean {

        return (name.isEmpty() || date.isEmpty() || color.isEmpty() || rate.isEmpty() || description.isEmpty())
    }

//__________________________________________________________________________________________________validateUsername


    /**
     * Method to check for null inputs
     * @param String Username
     * @return Boolean {false} username is unique/ {true} username already exists
     */
    suspend fun usernameExists(username: String): Boolean {

        val usersCollection = FirestoreConnection.getDatabaseInstance().collection("users")

        return try {

            val querySnapshot = usersCollection.whereEqualTo("username", username).get().await()
            querySnapshot.isEmpty
            false

        } catch (e: FirebaseFirestoreException) {

            true

        }

    }


//__________________________________________________________________________________________________validateEmail


    /**
     * Method to validate the given Email
     * @param String Email
     * @return Boolean {true} email is valid/ {false} email is not valid
     */
    fun validateEmail(email: String): Boolean {

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
    fun validatePasswordsMatch(password: String, passwordConfirmation: String): Boolean {

        return(password == passwordConfirmation)

    }


//__________________________________________________________________________________________________validatePasswordFormat


    /**
     * Method to validate the password matches the required format
     * @param String Password
     * @return Boolean {true} passwords is in the correct format/ {false} passwords is not in the correct format
     */
    fun validatePasswordFormat(password: String): Pair<Boolean, String> {

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

    //__________________________________________________________________________________________________validateDouble

    /**
     * Method to validate the double for hourly rate
     * @param Double rate
     * @return Boolean true if the rate is a double
     */
    fun validateDouble(rate: String): Boolean {
        val hourlyRate = rate.toDoubleOrNull()

        // If hourlyRate is not null, return true
        // Otherwise, return false
        return hourlyRate != null
    }



}
/*
░▒▓████████▓▒░▒▓███████▓▒░░▒▓███████▓▒░        ░▒▓██████▓▒░░▒▓████████▓▒░      ░▒▓████████▓▒░▒▓█▓▒░▒▓█▓▒░      ░▒▓████████▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓██████▓▒░ ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓██████▓▒░        ░▒▓██████▓▒░ ░▒▓█▓▒░▒▓█▓▒░      ░▒▓██████▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓████████▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓███████▓▒░        ░▒▓██████▓▒░░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓████████▓▒░▒▓████████▓▒░


*/