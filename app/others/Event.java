package others;

import java.util.ArrayList;
import java.util.List;

import models.Meeting;
import models.Sprint;

public class Event {
	public List<Sprint.Object> sprints;
	public List<Meeting.Object> meetings;
	
	public Event(){
		sprints = new ArrayList<Sprint.Object>();
		meetings = new ArrayList<Meeting.Object>();
	}
}
