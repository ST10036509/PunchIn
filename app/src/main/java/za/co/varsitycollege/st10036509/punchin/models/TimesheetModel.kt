/*
AUTHOR: Cameron Brooks
CREATED: 29/04/2024
LAST MODIFIED: 29/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.models

import com.google.type.DateTime

/**
 * Class to represent the Timesheet Context and handle timesheet specific functionality
 * @param String timesheetUser
 * @param String timesheetName
 * @param String timesheetProjectName
 * @param DateTime timesheetStartDate
 * @param Int timesheetStartTime
 * @param Int timesheetEndTime
 * @param String timesheetDescription
 * //@param String timesheetPhoto
 */
class TimesheetModel (

    var timesheetUser: String,
    var timesheetName: String,
    var timesheetProjectName: String,
    var timesheetStartDate: String,
    var timesheetStartTime: String,
    var timesheetEndTime: String,
    var timesheetDescription: String,
    //var timesheetPhoto: String,
){

}