/*
AUTHOR: Ethan Schoonbee
CREATED: 27/04/2024
LAST MODIFIED: 27/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.models

import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection

private lateinit var authModel: AuthenticationModel

/**
 * Class to represent the User Context and handle user specific functionality
 * @param String username
 * @param Int points
 * @param Int minGoal
 * @param Int maxGoal
 */
object UserModel{

    var _username: String = ""
    var _email: String = ""
    var _points: Int = 0
    var _minGoal: Int = 0
    var _maxGoal: Int = 0


    fun populateUserModel(username: String, email: String, points: Int, minGoal: Int, maxGoal: Int) {

        this._username = username
        this._email = email
        this._points = points
        this._minGoal = minGoal
        this._maxGoal = maxGoal

    }

    fun fetchUserDataFromFireStore(uid: String, callback: (Boolean) -> Unit) {

        val firestoreInstance = FirestoreConnection.getDatabaseInstance()

        val docReference = firestoreInstance.collection("users").document(uid)

        docReference.get()
            .addOnSuccessListener { documentSnapshot ->

                if (documentSnapshot.exists()) {

                    val data = documentSnapshot.data

                    if (data != null) {

                       populateUserModel(
                           data["username"] as? String ?: "",
                           data["email"] as? String ?: "",
                           (data["points"] as? Long)?.toInt() ?: 0,
                           (data["minGoal"] as? Long)?.toInt() ?: 0,
                           (data["maxGoal"] as? Long)?.toInt() ?: 0,
                       )

                        callback(true)

                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                callback(false)
            }
    }
}




//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\