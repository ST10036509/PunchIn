/*
AUTHOR: Ethan Schoonbee
CREATED: 26/04/2024
LAST MODIFIED: 27/04/2024
 */

package za.co.varsitycollege.st10036509.punchin

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await


/**
 * Class to handle Authentication Functionality
 */
class AuthenticationViewModel() :ViewModel() {

    private val authInstance: FirebaseAuth by lazy { FirebaseAuth.getInstance() }//link an instance of the firebase authentication sdk

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


//__________________________________________________________________________________________________signUp


    /**
     * Method to create a user profile
     * @param String email
     * @param String password
     * @param String username
     * @return Pair<Boolean, String> callback
     */
    fun signUp(email: String, password: String, username: String, callback: (Pair<Boolean, String>) -> Unit) {

        //run the firebase authentication method to create a new user account
        authInstance.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task -> //listen for success or failure

                //if the account was successfully created
                if (task.isSuccessful) {

                    //get the user that was just created
                    val user = authInstance.currentUser

                    //if the user exists
                    user?.let {

                        //create user context and assign default values
                        val userData = User(username, 0,0,24)

                        //save the user data to the database
                        saveAdditionalUserDataToFirestore(user.uid, userData) { success ->

                            //if the data is added to the database successfully
                            if (success) {

                                //return callback with no message
                                callback(Pair(true, ""))

                                //if the data is added to the database successfully
                            } else {

                                //return callback with no message
                                callback(Pair(false, "Failed to add user data to database"))

                            }
                        }
                    }

                //if an error occurred during the account creation
                } else {

                    //fetch the task exception and get the error message
                    val errorMessage = task.exception?.message?.let { message ->
                        val parts = message.split(":")
                        if (parts.size > 1) {
                            parts[1].trim()
                        } else {
                            message.trim()
                        }
                    }

                    //return callback with the unhandled error message
                    callback(Pair(false, errorMessage ?: "Unknown Error Occurred"))
                }
            }
    }


//__________________________________________________________________________________________________saveAdditionalUserDataToFirestore


    /**
     * Method to save the additional data of a user to the database
     * @param String user unique id
     * @param HashMap additional user data
     * @return Boolean callback
     */
    private fun saveAdditionalUserDataToFirestore(uid: String, userData: User, callback: (Boolean) -> Unit) {

        //open the database connection and find/create the users collection
        FirestoreConnection.getDatabaseInstance().collection("users")
            .document(uid)//create a new document with user uid
            .set(userData)//write the user data
            .addOnSuccessListener { callback(true) }//if the data is successfully added
            .addOnFailureListener { callback(false) }//if an error occurs while adding the extra user data
    }


//__________________________________________________________________________________________________signIn


    /**
     * Method to log in user who already has an account
     * @param String email or username
     * @param String password
     * @return Pair<Boolean, String> callback
     */
    fun signIn(email: String, password: String, username: String, callback: (Boolean) -> Unit) {

        //run the firebase authentication method to sign an user in
        authInstance.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {task -> //listen for success or failure

                callback(task.isSuccessful)

            }

    }


//__________________________________________________________________________________________________signOut


    /**
     * Method to sign a logged in user from the app
     */
    fun signOut() {

        authInstance.signOut()//sign the user out

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

            return Pair(false, MSG_NULL_INPUTS_ERROR)

        }

        if (!validateUsername(username)) {

            return Pair(false, MSG_USERNAME_TAKEN_ERROR)

        }

        //check if the email is in the correct format
        if (!validateEmail(email)) {

            return Pair(false, MSG_EMAIL_ERROR)

        }

        //check if the password is in the correct format
        if (!isValidPasswordFormat) {

            return Pair(false, passwordFormatErrorMessage)

        }

        //check if the password and password confirmation match
        if (!validatePasswordsMatch(password, passwordConfirmation)) {

            return Pair(false, MSG_PASSWORD_MISSMATCH_ERROR)

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

            return Pair(false, MSG_PASSWORD_LENGTH_ERROR)

        }
        if (!hasUpperCase) {

            return Pair(false, MSG_PASSWORD_UPPERCASE_ERROR)

        }
        if (!hasLowerCase) {

            return Pair(false, MSG_PASSWORD_LOWERCASE_ERROR)

        }
        if (!hasDigit) {

            return Pair(false, MSG_PASSWORD_DIGIT_ERROR)

        }

        return Pair(true, "")

    }
}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\