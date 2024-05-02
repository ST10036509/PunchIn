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
    var startDate: String,
    var setColor: String,
    var hourlyRate: Double,
    var description: String,
    var totalTimeSheets: Long = 0,
    var totalHours: Long = 0,
    var totalEarnings:Double = 0.0,
    var userId: String
) {
    constructor() : this("", "", "", 0.0, "", 0, 0, 0.0, "")

    private lateinit var firestore: FirebaseFirestore
    fun setData(
        projectName: String,
        startDate: String,
        setColor: String,
        hourlyRate: Double,
        description: String,
        userId: String
    ) {
        this.projectName = projectName
        this.startDate = startDate
        this.setColor = setColor
        this.hourlyRate = hourlyRate
        this.description = description
        this.userId = userId
    }

    fun readProjectData(projectid: String, callback: (ProjectsModel?, List<TimesheetModel>) -> Unit) {
        val projectId: String = projectid

        val firestore = FirebaseFirestore.getInstance()
        val projectRef = firestore.collection("projects").document(projectId)

        // Retrieve the project
        projectRef.get()
            .addOnSuccessListener { projectDoc ->
                if (projectDoc.exists()) {
                    val projectData = projectDoc.toObject(ProjectsModel::class.java)
                    projectData?.let { project ->
                        Log.d("ProjectsModel", "Project Data: $project")

                        // Now, retrieve timesheets associated with this project
                        val timesheetsCollectionRef = firestore.collection("timesheets")
                        val timesheetsQuery = timesheetsCollectionRef.whereEqualTo("projectUid", projectId)

                        timesheetsQuery.get()
                            .addOnSuccessListener { timesheetsSnapshot ->
                                val timesheets = mutableListOf<TimesheetModel>()  // Corrected type here
                                for (timesheetDoc in timesheetsSnapshot.documents) {
                                    val timesheetData = timesheetDoc.toObject(TimesheetModel::class.java)
                                    timesheetData?.let { timesheet ->
                                        timesheets.add(timesheet)
                                    }
                                }

                                // Return the project and its associated timesheets
                                callback(project, timesheets)
                            }
                            .addOnFailureListener { e ->
                                Log.e("ProjectsModel", "Error reading timesheets data: $e")
                                callback(null, emptyList())
                            }
                    }
                } else {
                    Log.d("ProjectsModel", "No project found with ID: $projectId")
                    callback(null, emptyList())
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProjectsModel", "Error reading project data: $e")
                callback(null, emptyList())
            }
    }

    // Method to get project data as a map
    fun getData(): Map<String, Any> {
        return mapOf(
            "projectName" to projectName,
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
