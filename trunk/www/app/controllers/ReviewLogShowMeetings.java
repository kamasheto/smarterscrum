// package controllers;
//
// import java.util.List;
//
// import models.Meeting;
// import models.MeetingAttendance;
// import models.Task;
// import play.mvc.With;
//
// /**
// * reviewLog is the class for manipulating and viewing the review log which
// * contains various information about the meeting as its' tasks and users.
// * <p>
// * Operations which gets the list of meetings are done by sending the list of
// * meetings found in the database to the view by using the findById method.
// * Operations which retrieve the associated items of the selected meeting
// * operate by getting the selected meeting's id from the view and then
// * Retrieving the list of users and tasks of that meeting id.
// * <p>
// *
// * @author Hossam Amer
// */
//
// @With( Secure.class )
// public class ReviewLogShowMeetings extends SmartController
// {
//
// /**
// * Renders the list of meetings to be used by the views.
// */
//
// public static void index()
// {
// /*
// * Meeting a = new Meeting( "M1", "Ihab Amer" ); Meeting b = new
// * Meeting( "M2", "HA" ); Meeting c = new Meeting( "M3", "Nora Ahmed" );
// * Meeting d = new Meeting( "M4", "Samah Amer" ); Meeting e = new
// * Meeting( "M5", "Mostafa Amer" ); Meeting[] rA = { b, a, c, d, e };
// * List<Meeting> reviewMeetings = Arrays.asList( rA );
// */
//
// List<Meeting> reviewMeetings = Meeting.find( "order by id asc" ).fetch();
// render( reviewMeetings );
// }
//
// /**
// * Gets a meeting and its all associated items with a given id and sends its
// * users and tasks to the view.
// *
// * @param id
// * the id of the selected meeting.
// */
//
// public static void view_meeting( long id )
// {
// Meeting x = Meeting.findById( id );
// List<MeetingAttendance> users = x.users;
// List<Task> tasks = x.tasks;
// render( x, users, tasks );
// }
//
// // public static void editReviewLog() {
// // ;
// // }
// }