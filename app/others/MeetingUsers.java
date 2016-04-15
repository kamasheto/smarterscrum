package others;

/*
 * @author asmaak89
 * this class was made to have and instance of it's type in
 * Controller Boards to be able to Render a list of Users for 
 * each meeting and for each Users List there is Certain Meeting
 * @param Meeting m
 *    Meeting Send from Controller Boards.
 */

import java.util.ArrayList;

import models.Meeting;
import models.User;

public class MeetingUsers extends ArrayList<User> {
	Meeting meeting;

	public MeetingUsers(Meeting m) {
		meeting = m;
	}
}