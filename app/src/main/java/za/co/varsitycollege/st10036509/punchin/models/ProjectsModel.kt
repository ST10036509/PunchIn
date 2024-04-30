package za.co.varsitycollege.st10036509.punchin.models
/*
AUTHOR: Leonard Bester
CREATED: 30/04/2024
LAST MODIFIED: 30/04/2024
 */

/*
Model of Projects designed to handle variables and data specific
functions and variables
 */
class ProjectsModel (
    var totalHours: String,
    var totalTimesheets: String,
    var hourlyRate: String,
    var totalEarnings: String,
    var projectColor: String,
    var organizationName: String,
    var timesheetsList: String,
    var projectDescription: String,
) { // Beyond this point is heavy on chatGPT
    // Method to set project data
    fun setData(
        totalHours: String,
        totalTimesheets: String,
        hourlyRate: String,
        totalEarnings: String,
        projectColor: String,
        organizationName: String,
        timesheetsList: String,
        projectDescription: String
    ) {
        this.totalHours = totalHours
        this.totalTimesheets = totalTimesheets
        this.hourlyRate = hourlyRate
        this.totalEarnings = totalEarnings
        this.projectColor = projectColor
        this.organizationName = organizationName
        this.timesheetsList = timesheetsList
        this.projectDescription = projectDescription
    }

    // Method to get project data as a map
    fun getData(): Map<String, String> {
        return mapOf(
            "totalHours" to totalHours,
            "totalTimesheets" to totalTimesheets,
            "hourlyRate" to hourlyRate,
            "totalEarnings" to totalEarnings,
            "projectColor" to projectColor,
            "organizationName" to organizationName,
            "timesheetsList" to timesheetsList,
            "projectDescription" to projectDescription
        )
    }
}