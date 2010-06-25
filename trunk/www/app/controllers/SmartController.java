package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Meeting;
import models.Sprint;

import play.mvc.Before;
import play.mvc.Controller;

public class SmartController extends Controller {
	@Before
	public static void addDefaults() {
		renderArgs.put("connected", Security.getConnected());
	}
	@Before
	public static void endsprint() {
		List<Sprint> sprints=Sprint.findAll();
		for (Sprint s : sprints) {
			Date now = Calendar.getInstance().getTime();
			if( s.endDate.before( now ) )
			{
				s.ended = true;
	s.save();
			}
			
		}
	}
}
