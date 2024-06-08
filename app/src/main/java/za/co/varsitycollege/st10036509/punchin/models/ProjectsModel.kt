package za.co.varsitycollege.st10036509.punchin.models

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.varsitycollege.st10036509.punchin.utils.FirestoreConnection
import kotlin.math.absoluteValue

class ProjectsModel(
    var projectName: String,
    var startDate: Timestamp? = null,
    var setColor: String,
    var hourlyRate: Double,
    var description: String,
    var totalTimeSheets: Long = 0,
    var totalHours: Double = 0.0,
    var totalEarnings: Double = 0.0,
    var userId: String,
    var sheetNames: MutableList<String> = mutableListOf() // Add sheetNames list
) {
    constructor() : this("", null, "", 0.0, "", 0, 0.0, 0.0, "", mutableListOf())

    private var firestore = FirestoreConnection.getDatabaseInstance()
    private var authInstance = AuthenticationModel()
    private var timesheets: MutableList<TimesheetModel> = mutableListOf()
    private val projects = mutableListOf<ProjectsModel>()

    fun getProjectTotalHours(): Double {
        var totalHoursWorked = 0.0

        for (timesheet in timesheets) {
            val durationMillis = (timesheet.timesheetEndTime!!.time - timesheet.timesheetStartTime!!.time).absoluteValue
            val hoursWorked = durationMillis.toDouble() / (1000 * 60 * 60)
            totalHoursWorked += hoursWorked
        }

        // Round to the second decimal place
        return totalHoursWorked.round(2)
    }

    // Extension function to round a Double to a specified number of decimal places
    fun Double.round(decimals: Int): Double = "%.${decimals}f".format(this).toDouble()

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
                        val newTimesheet = timesheetDocument.toObject(TimesheetModel::class.java)
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
        val firestore = FirebaseFirestore.getInstance()
        val collection = firestore.collection("projects")
        val projectData = getData()

        collection.add(projectData)
            .addOnSuccessListener { documentReference ->
                Log.d("ProjectsModel", "Project data stored successfully. Document ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("ProjectsModel", "Error storing project data: $e")
            }
    }

    fun getProjectList(userUid: String, callback: (List<ProjectsModel>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val projectRef = firestore.collection("projects")

        projectRef.whereEqualTo("userId", userUid)
            .get()
            .addOnSuccessListener { projectDoc ->
                val projects = mutableListOf<ProjectsModel>()

                if (!projectDoc.isEmpty) {
                    val deferredProjects = projectDoc.documents.map { document ->
                        val projectName = document.getString("projectName")
                        val startDate = document["startDate"] as? Timestamp
                        val setColor = document.getString("setColor")
                        val hourlyRate = document.getDouble("hourlyRate")
                        val description = document.getString("description")
                        val userId = document.getString("userId")

                        if (userId == userUid) {
                            val deferredProject = CompletableDeferred<ProjectsModel?>()

                            // Load timesheets before creating the project
                            loadTimesheets(document.id) { timesheets ->
                                val sheetNames = timesheets.map { it.timesheetName }.filterNotNull().toMutableList()

                                if (timesheets.isNotEmpty()) {
                                    val totalTimeSheets = timesheets.size.toLong()
                                    val totalHours = calculateTotalHours(timesheets)
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
                                        userId!!,
                                        sheetNames // Add sheet names to project
                                    )
                                    deferredProject.complete(newProject)
                                } else {
                                    val totalTimeSheets: Long = 0
                                    val totalHours: Double = 0.0
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
                                        userId!!,
                                        sheetNames // Add sheet names to project
                                    )
                                    deferredProject.complete(newProjectWithNoTimeSheets)
                                }
                            }

                            deferredProject
                        } else {
                            null
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
                Log.e("ProjectsModel", "Error counting projects: $e")
                callback(emptyList())
            }
    }

    private fun loadTimesheets(projectId: String, callback: (List<TimesheetModel>) -> Unit) {
        val timesheetRef = firestore.collection("timesheets")
        timesheetRef.whereEqualTo("projectId", projectId)
            .get()
            .addOnSuccessListener { timesheetDocuments ->
                if (!timesheetDocuments.isEmpty) {
                    val timesheets = timesheetDocuments.map { it.toObject(TimesheetModel::class.java) }
                    callback(timesheets)
                } else {
                    callback(emptyList())
                }
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    private fun calculateTotalHours(timesheets: List<TimesheetModel>): Double {
        var totalHoursWorked = 0.0
        for (timesheet in timesheets) {
            val durationMillis = (timesheet.timesheetEndTime!!.time - timesheet.timesheetStartTime!!.time).absoluteValue
            val hoursWorked = durationMillis.toDouble() / (1000 * 60 * 60)
            totalHoursWorked += hoursWorked
        }
        return totalHoursWorked.round(2)
    }
}
