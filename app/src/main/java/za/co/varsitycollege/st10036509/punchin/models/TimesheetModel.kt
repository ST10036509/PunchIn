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
    var timesheetName: String,
    var projectId: String,
    var timesheetStartDate: Date? = null,
    var timesheetStartTime: Date? = null,
    var timesheetEndTime: Date? = null,
    var timesheetDescription: String
    //var timesheetPhoto: String,
){
    // Add no-argument constructor
    constructor() : this("", "", "", null, null, null, "")



    fun setData(
        userId: String,
        timesheetName: String,
        projectId: String,
        timesheetStartDate: Date,
        timesheetStartTime: Date,
        timesheetEndTime: Date,
        timesheetDescription: String
        //timesheetPhoto: String
    ) {
        this.userId = userId
        this.timesheetName = timesheetName
        this.projectId = projectId
        this.timesheetStartDate = timesheetStartDate
        this.timesheetStartTime = timesheetStartTime
        this.timesheetEndTime = timesheetEndTime
        this.timesheetDescription = timesheetDescription
        //this.timesheetPhoto = timesheetPhoto
    }
    fun getData(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "timesheetName" to timesheetName,
            "projectId" to projectId,
            "timesheetStartDate" to timesheetStartDate,
            "timesheetStartTime" to timesheetStartTime,
            "timesheetEndTime" to timesheetEndTime,
            "timesheetDescription" to timesheetDescription
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