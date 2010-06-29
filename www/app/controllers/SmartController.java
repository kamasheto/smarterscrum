package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Sprint;
import play.mvc.Before;
import play.mvc.Controller;

public class SmartController extends Controller {
	@Before
	public static void addDefaults() throws Throwable {
		renderArgs.put("connected", Security.getConnected());
		if (Security.isConnected() && Security.getConnected().deleted) {
			Secure.logout();
		}
	}

	@Before
	public static void endsprint() {
		List<Sprint> sprints = Sprint.findAll();
		for (Sprint s : sprints) {
			Date now = Calendar.getInstance().getTime();
			if (s.endDate.before(now)) {
				s.ended = true;
				s.save();
			}

		}
	}
}
