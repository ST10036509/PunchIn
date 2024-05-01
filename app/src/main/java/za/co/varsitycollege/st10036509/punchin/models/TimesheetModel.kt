/*
AUTHOR: Cameron Brooks
CREATED: 29/04/2024
LAST MODIFIED: 29/04/2024
 */

package za.co.varsitycollege.st10036509.punchin.models

import com.google.type.DateTime
import java.util.Date

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
    var timesheetName: String,
    var projectUid: String,
    var startDate: Date?,
    var startTimestamp: Date?,
    var endTimestamp: Date?,
    var timesheetDescription: String,
    var userId: String
){

}