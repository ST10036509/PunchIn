/*
AUTHOR: Ethan Schoonbee
CREATED: 26/04/2024
LAST MODIFIED: 27/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.models


import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import za.co.varsitycollege.st10036509.punchin.utils.ValidationHandler


/**
 * Class to handle Authentication Functionality
 */
class AuthenticationModel() {

    private val authInstance: FirebaseAuth by lazy { FirebaseAuth.getInstance() }//link an instance of the firebase authentication sdk
    private var validationsHandler = ValidationHandler()//initialise an instance of the validations handler
    private val firestoreInstance = FirestoreConnection.getDatabaseInstance()


    //strings to use
    private companion object {
        const val MSG_NULL = ""
        const val MSG_DATABASE_ADD_ERROR = "Failed to add user data to database"
        const val MSG_UNEXPECTED_ERROR = "Unexpected Error Occurred"
        const val MSG_INVALID_CREDENTIALS = "No account found matches these credentials"
        const val MSG_USER_DATA_ERROR = "Could not find user data for these credentials. Please create a new account!"
    }
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
                        UserModel.populateUserModel(username, email, 0, 6, 8)

                        //save the user data to the database
                        saveAdditionalUserDataToFirestore(user.uid) { success ->

                            //if the data is added to the database successfully
                            if (success) {

                                //return callback with no message
                                callback(Pair(true, AuthenticationModel.MSG_NULL))

                                //if the data is added to the database successfully
                            } else {

                                //return callback with no message
                                callback(Pair(false, AuthenticationModel.MSG_DATABASE_ADD_ERROR))

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
                    callback(Pair(false, errorMessage ?: AuthenticationModel.MSG_UNEXPECTED_ERROR))
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
    private fun saveAdditionalUserDataToFirestore(uid: String, callback: (Boolean) -> Unit) {

        val userModelData = UserModel

        //open the database connection and find/create the users collection
        firestoreInstance.collection("users")
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
    fun signIn(emailOrUsername: String, password: String, callback: (Pair<Boolean, String>) -> Unit) {

        if (validationsHandler.validateEmail(emailOrUsername)) {

            signInWithEmail(emailOrUsername, password) { task ->

                if (task.first) {

                    fetchUserDataAndCallback(callback)

                } else {

                    callback(Pair(false, task.second))

                }
            }

        } else {

            signInWithUsername(emailOrUsername, password) { task ->

                if (task.first) {

                    fetchUserDataAndCallback(callback)

                } else {

                    callback(Pair(false, task.second))

                }
            }
        }
    }


    private fun fetchUserDataAndCallback(callback: (Pair<Boolean, String>) -> Unit) {

        val currentUser = authInstance.currentUser

        if (currentUser != null) {

            UserModel.fetchUserDataFromFireStore(currentUser.uid) { success ->

                if (success) {

                    callback(Pair(true, AuthenticationModel.MSG_NULL))

                } else {

                    callback(Pair(false, AuthenticationModel.MSG_NULL))

                }
            }

        } else {

            // Current user is null, handle the error
            callback(Pair(false, AuthenticationModel.MSG_UNEXPECTED_ERROR))

        }
    }


//__________________________________________________________________________________________________signInWithEmail


    private fun signInWithEmail(email: String, password: String, callback: (Pair<Boolean, String>) -> Unit) {

        //run the firebase authentication method to sign a user in
        authInstance.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {task -> //listen for success or failure

                if (task.isSuccessful) {

                    callback(Pair(true, AuthenticationModel.MSG_NULL))

                } else {

                    //return callback with the unhandled error message
                    callback(Pair(false, MSG_INVALID_CREDENTIALS))

                }
            }
    }


//__________________________________________________________________________________________________signInWithUsername


    private fun signInWithUsername(username: String, password: String, callback: (Pair<Boolean, String>) -> Unit) {

        getEmailByUsername(username) { email ->

            if (email != null) {

                signInWithEmail(email, password, callback)

            } else {

                callback(Pair(false, AuthenticationModel.MSG_INVALID_CREDENTIALS))

            }
        }
    }


//__________________________________________________________________________________________________getEmailByUsername


    private fun getEmailByUsername(username: String, callback: (String?) -> Unit) {

        val usersCollection = firestoreInstance.collection("users")

        usersCollection.whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->

                if(!querySnapshot.isEmpty) {

                    val document = querySnapshot.documents.first()

                    val userEmail = document.getString("email")

                    callback(userEmail)
                } else {

                    callback(null)

                }
            }
            .addOnFailureListener {exception ->
                Log.e("Login", "Error getting documents:", exception )
                callback(null)
            }
    }


//__________________________________________________________________________________________________signOut


    /**
     * Method to sign a logged in user from the app
     */
    fun signOut(callback: (Boolean) -> Unit) {

        authInstance.signOut()//sign the user out

        //return callback for handling
        authInstance.addAuthStateListener { auth ->
            val isSignedOut = auth.currentUser == null
            callback(isSignedOut)
        }
    }


}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\