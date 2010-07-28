package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Project;
import models.Sprint;
import play.mvc.Before;
import play.mvc.Controller;

public class SmartController extends Controller {
	@Before
	public static void beforeActions() throws Throwable {
		renderArgs.put("connected", Security.getConnected());
		if (Security.isConnected() && Security.getConnected().deleted) {
			Secure.logout();
		}
		renderArgs.put("topProjects", Project.findAll());
		
		List<Sprint> sprints = Sprint.findAll();
		for (Sprint s : sprints) {
			Date now = Calendar.getInstance().getTime();
			if (s.endDate != null && s.endDate.before(now)) {
				s.ended = true;
				s.save();
			}

		}
	}
}
