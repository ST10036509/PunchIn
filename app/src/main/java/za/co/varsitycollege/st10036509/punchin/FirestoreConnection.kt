/*
AUTHOR: Ethan Schoonbee
CREATED: 23/04/2024
LAST MODIFIED: 23/04/2024
 */

package za.co.varsitycollege.st10036509.punchin

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Singleton Object to handle the fire database connection
 */
object FirestoreConnection {

    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }//get firestore instance through thread-safe lazy call

    /**
     * Method to fetch the firestore instance
     * @return FirebaseFirestore instance
     */
    fun getFirestoreInstance(): FirebaseFirestore {
        return firestoreInstance
    }
}
//______________________....oooOO0_END_OF_FILE_0OOooo....______________________\\