/*
AUTHOR: Cameron Brooks
CREATED: 29/04/2024
LAST MODIFIED: 29/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.models

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.Date
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
private lateinit var authModel: AuthenticationModel

class TimesheetModel(

    var timesheetUid: String,
    var timesheetName: String,
    var projectUid: String,
    var timesheetStartDate: String,
    var timesheetStartTime: String,
    var timesheetEndTime: String,
    var timesheetDescription: String
    //var timesheetPhoto: String,
){
    fun setData(
        timesheetUid: String,
        timesheetName: String,
        projectUid: String,
        timesheetStartDate: String,
        timesheetStartTime: String,
        timesheetEndTime: String,
        timesheetDescription: String
        //timesheetPhoto: String
    ) {
        this.timesheetUid = timesheetUid
        this.timesheetName = timesheetName
        this.projectUid = projectUid
        this.timesheetStartDate = timesheetStartDate
        this.timesheetStartTime = timesheetStartTime
        this.timesheetEndTime = timesheetEndTime
        this.timesheetDescription = timesheetDescription
        //this.timesheetPhoto = timesheetPhoto
    }
    fun getData(): Map<String, Any?> {
        return mapOf(
            "timesheetUid" to timesheetUid,
            "timesheetName" to timesheetName,
            "projectUid" to projectUid,
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