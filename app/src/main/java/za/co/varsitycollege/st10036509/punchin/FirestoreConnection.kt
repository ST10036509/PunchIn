package za.co.varsitycollege.st10036509.punchin

import com.google.firebase.firestore.FirebaseFirestore
///
// Singleton Class (object) for connecting to the firebase cloud database
///
object FirestoreConnection {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun getFirestoreInstance(): FirebaseFirestore {
        return firestoreInstance
    }
}