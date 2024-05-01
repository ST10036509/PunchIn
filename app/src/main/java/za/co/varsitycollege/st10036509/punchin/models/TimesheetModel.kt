/*
AUTHOR: Cameron Brooks
CREATED: 29/04/2024
LAST MODIFIED: 29/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.models

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

private lateinit var authModel: AuthenticationModel

class TimesheetModel(
    var userId: String,
    var name: String,
    var projectId: String,
    var createdAt: Date?,
    var startedAt: Date?,
    var endedAt: Date?,
    var description: String
    //var timesheetPhoto: String,
){
    // Add no-argument constructor
    constructor() : this("", "", "", null, null, null, "")
    fun setData(
        userId: String,
        name: String,
        projectId: String,
        createdAt: Date,
        startedAt: Date?,
        endedAt: Date?,
        description: String
        //timesheetPhoto: String
    ) {
        this.userId = userId
        this.name = name
        this.projectId = projectId
        this.createdAt = createdAt
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.description = description
        //this.timesheetPhoto = timesheetPhoto
    }
    fun getData(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "projectId" to projectId,
            "createdAt" to createdAt,
            "startedAt" to startedAt,
            "endedAt" to endedAt,
            "description" to description
            //"timesheetPhoto" to timesheetPhoto
            )
    }

    fun writeDataToFirestore() {
        // Access Firestore instance
        val firestore = FirebaseFirestore.getInstance()
        // Define the Firestore collection
        val collection = firestore.collection("timesheets")
        //get timesheet data as map
        val timesheetData = getData()

        //add data to firestore
        collection.add(timesheetData)
            .addOnSuccessListener { documentReference ->
                // Data successfully stored in Firestore
                Log.d("TimesheetModel", "Timesheet data stored successfully. Document ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Error storing data in Firestore
                Log.e("TimesheetModel", "Error storing timesheet data: $e")
            }
    }

    fun fetchProjectNames() {

    }

    fun fetchProjectID() {

    }


}