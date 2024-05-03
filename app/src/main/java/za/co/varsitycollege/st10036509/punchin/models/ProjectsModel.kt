package za.co.varsitycollege.st10036509.punchin.models
/*
AUTHOR: Leonard Bester
CREATED: 30/04/2024
LAST MODIFIED: 02/05/2024
 */

/*
Model of Projects designed to handle variables and data specific
functions and variables
 */
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import kotlin.math.absoluteValue


class ProjectsModel (
    var projectName: String,
    var startDate: Timestamp? = null,
    var setColor: String,
    var hourlyRate: Double,
    var description: String,
    var totalTimeSheets: Long = 0,
    var totalHours: Long = 0,
    var totalEarnings:Double = 0.0,
    var userId: String
) {
    constructor() : this("", null, "", 0.0, "", 0, 0, 0.0, "")

    private var firestore = FirestoreConnection.getDatabaseInstance()

    private var authInstance = AuthenticationModel()
    private var timesheets: MutableList<TimesheetModel> =
        mutableListOf()//declare an array to hold user related timesheets
    private val projects = mutableListOf<ProjectsModel>()


    fun getProjectTotalHours(): Long {

        var totalHoursWorked = 0.0

        for (timesheet in timesheets) {

            val durationMillis =
                (timesheet.timesheetEndTime!!.time - timesheet.timesheetStartTime!!.time).absoluteValue
            val hoursWorked = durationMillis.toDouble() / (1000 * 60 * 60)
            totalHoursWorked += hoursWorked
        }

        return totalHoursWorked.toLong()
    }

    fun getTotalTimesheets(): Long {

        var total: Long = 0

        for (timesheet in timesheets) {
            total++
        }

        return total
    }

    fun getUserRelatedTimesheets(projectId: String, callback: (Boolean) -> Unit) {


        val timesheetRef = firestore.collection("timesheets")
        timesheetRef.whereEqualTo("projectId", projectId)
            .get()
            .addOnSuccessListener { timesheetDocuments ->
                if (!timesheetDocuments.isEmpty) {
                    for (timesheetDocument in timesheetDocuments) {

                        var newTimesheet = timesheetDocument.toObject(TimesheetModel::class.java)
                        timesheets.add(newTimesheet)
                    }
                    callback(true)
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Method to get project data as a map
    fun getData(): Map<String, Any?> {
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
                Log.d(
                    "ProjectsModel",
                    "Project data stored successfully. Document ID: ${documentReference.id}"
                )
            }
            .addOnFailureListener { e ->
                // Error storing data in Firestore
                Log.e("ProjectsModel", "Error storing project data: $e")
            }
    }


    fun getProjectList(userUid: String, callback: (List<ProjectsModel>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val projectRef = firestore.collection("projects")

        projectRef.whereEqualTo("userId",userUid)
            .get()
            .addOnSuccessListener { projectDoc ->
                val projects = mutableListOf<ProjectsModel>()

                if (!projectDoc.isEmpty) {
                    val deferredProjects = projectDoc.documents.map { document ->
                        val projectName = document.getString("projectName")

                        val startDate = document["startDate"] as? Timestamp // Retrieve startDate as Timestamp
                        val setColor = document.getString("setColor")
                        val hourlyRate = document.getDouble("hourlyRate")
                        val description = document.getString("description")
                        val userId = document.getString("userId")

                        if (userId == userUid) { // Check if the document belongs to the requested user
                            val deferredProject = CompletableDeferred<ProjectsModel?>()

                            getUserRelatedTimesheets(document.id) { success ->
                                if (success) {
                                    val totalTimeSheets = getTotalTimesheets()
                                    val totalHours = getProjectTotalHours()
                                    val totalEarnings = 0.0

                                    val newProject = ProjectsModel(
                                        projectName!!,
                                        startDate,
                                        setColor!!,
                                        hourlyRate!!,
                                        description!!,
                                        totalTimeSheets,
                                        totalHours,
                                        totalEarnings,
                                        userId!!
                                    )
                                    deferredProject.complete(newProject)
                                } else {
                                    val totalTimeSheets: Long = 0
                                    val totalHours: Long = 0
                                    val totalEarnings = 0.0

                                    val newProjectWithNoTimeSheets = ProjectsModel(
                                        projectName!!,
                                        startDate,
                                        setColor!!,
                                        hourlyRate!!,
                                        description!!,
                                        totalTimeSheets,
                                        totalHours,
                                        totalEarnings,
                                        userId!!
                                    )
                                    deferredProject.complete(newProjectWithNoTimeSheets)
                                }
                            }

                            deferredProject
                        } else {
                            null // Return null for projects not associated with the requested user
                        }
                    }.filterNotNull()

                    CoroutineScope(Dispatchers.Main).launch {
                        deferredProjects.forEach { deferredProject ->
                            val project = deferredProject.await()
                            if (project != null) {
                                projects.add(project)
                            }
                        }
                        callback(projects)
                    }
                } else {
                    callback(emptyList())
                }
            }
            .addOnFailureListener { e ->
                // Error handling
                Log.e("ProjectsModel", "Error counting projects: $e")
                callback(emptyList())
            }
    }


}



    /*

     */

        /*
        val firestore = FirebaseFirestore.getInstance()
        val projectsCollectionRef = firestore.collection("projects")

        // Query projects where userId field equals the passed userId
        val query = projectsCollectionRef.whereEqualTo("userId", userId)

        // Get the projects matching the query
        query.get()
            .addOnSuccessListener { querySnapshot ->
                val projects = mutableListOf<ProjectsModel>()
                for (doc in querySnapshot.documents) {
                    val project = doc.toObject(ProjectsModel::class.java)
                    project?.let {
                        projects.add(it)
                    }
                }
                callback(projects)
            }
            .addOnFailureListener { e ->
                // Error handling
                Log.e("ProjectsModel", "Error counting projects: $e")
                callback(emptyList()) // Return an empty list in case of failure
            }


         */

/*
░▒▓████████▓▒░▒▓███████▓▒░░▒▓███████▓▒░        ░▒▓██████▓▒░░▒▓████████▓▒░      ░▒▓████████▓▒░▒▓█▓▒░▒▓█▓▒░      ░▒▓████████▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓██████▓▒░ ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓██████▓▒░        ░▒▓██████▓▒░ ░▒▓█▓▒░▒▓█▓▒░      ░▒▓██████▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░░▒▓█▓▒░      ░▒▓█▓▒░░▒▓█▓▒░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓█▓▒░      ░▒▓█▓▒░
░▒▓████████▓▒░▒▓█▓▒░░▒▓█▓▒░▒▓███████▓▒░        ░▒▓██████▓▒░░▒▓█▓▒░             ░▒▓█▓▒░      ░▒▓█▓▒░▒▓████████▓▒░▒▓████████▓▒░



*/