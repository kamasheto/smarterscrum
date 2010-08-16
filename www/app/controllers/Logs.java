package controllers;

import play.*;
import play.mvc.*;

import java.util.*;
import java.lang.reflect.*;

import com.google.gson.reflect.TypeToken;

import models.*;
import others.*;

@With(Secure.class)
public class Logs extends SmartController {
	/**
	 * Lists the logs of this project
	 * @param projectId Project Id to list logs of
	 */
	public static void list(long projectId, int page, int perPage, String filter) {
		if (perPage == 0) {
			perPage = 10;
		}
		
		Project project = Project.findById(projectId);
		Security.check(Security.getConnected().in(project).can("manageLogs"));
		List<Log> logs = Log.find(filter).from(page * perPage).from(perPage * page).fetch(perPage);
		LogSearchResult result = new LogSearchResult();
		result.logs = logs;
		result.currentPage = page;
		result.totalPages = (int) Log.count() / perPage;
		Type listType = new TypeToken<List<String>>() {}.getType();
		renderJSON(result);
	}
	
	/**
	 * Shows the log with its details, or the whole list of logs in this project
	 * @param projectId the project's id
	 * @param logId the log's id
	 */
	public static void view(long projectId, long logId) {
		if (logId == 0 && projectId != 0) {
			Project project = Project.findById(projectId);
			Security.check(Security.getConnected().in(project).can("manageLogs"));
			render(project);
		} else if(logId != 0) {
			Log log = Log.findById(logId);
			Security.check(Security.getConnected().in(log.get(Project.class)).can("manageLogs"));
			LogInfo[] logInfo = {new LogInfo("User", User.class),
								// new LogInfo("Project", Project.class),
								new LogInfo("Component", Component.class),
								new LogInfo("Task", Task.class),
								new LogInfo("Meeting", Meeting.class),
								new LogInfo("Board", Board.class),
								new LogInfo("Column", Column.class),
								new LogInfo("Snapshot", Snapshot.class)};
			render(log, logInfo);	
		}
	}
}

