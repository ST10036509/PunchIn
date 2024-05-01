package za.co.varsitycollege.st10036509.punchin.models
/*
AUTHOR: Leonard Bester
CREATED: 30/04/2024
LAST MODIFIED: 1/05/2024
 */

/*
Model of Projects designed to handle variables and data specific
functions and variables
 */
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class ProjectsModel (
    var projectName: String,
    var organizationName: String,
    var startDate: String,
    var setColor: String,
    var hourlyRate: String,
    var description: String,
    var totalTimeSheets: String,
    var totalHours: String,
    var totalEarnings: String,
    var userId: String
) {
    // Method to set project data
    fun setData(
        projectName: String,
        organizationName: String,
        startDate: String,
        setColor: String,
        hourlyRate: String,
        description: String,
        totalTimeSheets: String,
        totalHours: String,
        totalEarnings: String,
        userId: String
    ) {
        this.projectName = projectName
        this.organizationName = organizationName
        this.startDate = startDate
        this.setColor = setColor
        this.hourlyRate = hourlyRate
        this.description = description
        this.totalTimeSheets = totalTimeSheets
        this.totalHours = totalHours
        this.totalEarnings = totalEarnings
        this.userId = userId
    }

    // Method to get project data as a map
    fun getData(): Map<String, Any> {
        return mapOf(
            "projectName" to projectName,
            "organizationName" to organizationName,
            "startDate" to startDate,
            "setColor" to setColor,
            "hourlyRate" to hourlyRate,
            "description" to description,
            "totalTimeSheets" to totalTimeSheets,
            "totalHours" to totalHours,
            "totalEarnings" to totalEarnings,
            "userId" to userId
        )
    }

    // Method to write project data to Firestore
    fun writeDataToFirestore() {
        // Access Firestore instance
        val firestore = FirebaseFirestore.getInstance()

        // Define the Firestore collection
        val collection = firestore.collection("projects")

        // Get project data as a map
        val projectData = getData()

        // Add the project data to Firestore
        collection.add(projectData)
            .addOnSuccessListener { documentReference ->
                // Data successfully stored in Firestore
                Log.d("ProjectsModel", "Project data stored successfully. Document ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Error storing data in Firestore
                Log.e("ProjectsModel", "Error storing project data: $e")
            }
    }
}
