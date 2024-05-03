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
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import java.util.Date
import kotlin.math.absoluteValue


class ProjectsModel (
    var projectName: String,
    var startDate: Date? = null,
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
    private var timesheets: MutableList<TimesheetModel> = mutableListOf()//declare an array to hold user related timesheets


    fun getProjectTotalHours():Long{

        var totalHoursWorked = 0.0

        for (timesheet in timesheets) {

            val durationMillis = (timesheet.timesheetEndTime!!.time - timesheet.timesheetStartTime!!.time).absoluteValue
            val hoursWorked = durationMillis.toDouble() / (1000 * 60 * 60)
            totalHoursWorked += hoursWorked
        }

        return totalHoursWorked.toLong()
    }

    fun getTotalTimesheets():Long{

        var total: Long = 0

        for (timesheet in timesheets){
            total++
        }

        return total
    }

fun getUserRelatedTimesheets(projectId: String, callback: (Boolean)-> Unit){


    val timesheetRef = firestore.collection("timesheets")
    timesheetRef.whereEqualTo("projectId", projectId)
        .get()
        .addOnSuccessListener{timesheetDocuments ->
            if(!timesheetDocuments.isEmpty) {
                for (timesheetDocument in timesheetDocuments) {

                    var newTimesheet = timesheetDocument.toObject(TimesheetModel::class.java)
                    timesheets.add(newTimesheet)
                }
                callback(true)
            }else{
                callback(false)
            }
        }
        .addOnFailureListener{
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
                Log.d("ProjectsModel", "Project data stored successfully. Document ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Error storing data in Firestore
                Log.e("ProjectsModel", "Error storing project data: $e")
            }
    }


    fun getProjectList(userId: String, callback: (List<ProjectsModel>) -> Unit) {


        val currentUser = authInstance.getCurrentUser()

        val firestore = FirebaseFirestore.getInstance()
        val projectRef = firestore.collection("projects")

        // Retrieve the project
        projectRef.whereEqualTo("userId", currentUser?.uid.toString())
            .get()
            .addOnSuccessListener { projectDoc ->
                if (!projectDoc.isEmpty) {

                    val projects = mutableListOf<ProjectsModel>()

                    for (document in projectDoc) {
                        val projectName = document.getString("projectName")
                        val startDate = document.getDate("startDate")
                        val setColor = document.getString("setColor")
                        val hourlyRate = document.getDouble("hourlyRate")
                        val description = document.getString("description")
                        var totalTimeSheets: Long = 0
                        var totalHours: Long = 0
                        getUserRelatedTimesheets(document.id) { success ->
                            if (success) {
                                totalTimeSheets = getTotalTimesheets()
                                totalHours = getProjectTotalHours()
                            }
                        }
                        val totalEarnings = 0.0
                        val userId = document.getString("userId")

                        val newProject: ProjectsModel= ProjectsModel(
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
                        projects.add(newProject)
                    }
                    callback(projects)
                }
            }
            .addOnFailureListener {e ->
                // Error handling
                Log.e("ProjectsModel", "Error counting projects: $e")
                callback(emptyList()) // Return an empty list in case of failure
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