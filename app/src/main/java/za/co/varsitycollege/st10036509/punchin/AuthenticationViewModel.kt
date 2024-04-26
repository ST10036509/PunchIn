package za.co.varsitycollege.st10036509.punchin

import android.content.Context
import android.service.autofill.UserData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthenticationViewModel(context: Context) :ViewModel() {

    private val authInstance: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun signUp(email: String, password: String, username: String, callback: (Boolean) -> Unit) {

        authInstance.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val user = authInstance.currentUser

                    user?.let {

                        val userData = hashMapOf (

                            "username" to username,
                            "points" to 0,
                            "minGoal" to 0,
                            "maxGoal" to 24

                        )

                        saveAdditionalUserDataToFirestore(user.uid, userData, callback)

                    }

                } else {
                    callback(false)
                }
            }
    }

    private fun saveAdditionalUserDataToFirestore(uid: String, userData: Map<String, Any>, callback: (Boolean) -> Unit) {

        FirestoreConnection.getDatabaseInstance().collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun signIn(email: String, password: String, username: String, callback: (Boolean) -> Unit) {

        authInstance.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {task ->

                callback(task.isSuccessful)

            }

    }

    fun signOut() {

        authInstance.signOut()

    }

}