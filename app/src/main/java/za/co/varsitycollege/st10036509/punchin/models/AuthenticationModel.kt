/*
AUTHOR: Ethan Schoonbee
CREATED: 26/04/2024
LAST MODIFIED: 27/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.models

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection


/**
 * Class to handle Authentication Functionality
 */
class AuthenticationModel() {

    private val authInstance: FirebaseAuth by lazy { FirebaseAuth.getInstance() }//link an instance of the firebase authentication sdk


//__________________________________________________________________________________________________getCurrentUser

    /**
     * Method to get the currently signed in user data, if applicable
     */
    fun getCurrentUser(): FirebaseUser? {

        return authInstance.currentUser

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
                        val userModelData = UserModel(username, 0,0,24)

                        //save the user data to the database
                        saveAdditionalUserDataToFirestore(user.uid, userModelData) { success ->

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
    private fun saveAdditionalUserDataToFirestore(uid: String, userModelData: UserModel, callback: (Boolean) -> Unit) {

        //open the database connection and find/create the users collection
        FirestoreConnection.getDatabaseInstance().collection("users")
            .document(uid)//create a new document with user uid
            .set(userModelData)//write the user data
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


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\