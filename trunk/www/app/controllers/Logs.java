package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

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
		List<Log> allLogs = project.logs;
		if (projectId < 0) {
			Security.check(Security.getConnected().isAdmin);
			allLogs = Log.findAll();
		} else {
			Security.check(Security.getConnected().in(project).can("manageLogs"));			
		}

		// List<Log> logs = Log.find().from(perPage * page).fetch(perPage);
		page--;
		List<Log> logs = new ArrayList<Log>();
		for (Log log : allLogs) {
			if (filter.length() == 0 || log.message.contains(filter)) {
				logs.add(log);
			}
		}
		LogSearchResult result = new LogSearchResult();		
		result.totalPages = (int) Math.ceil(logs.size() / (double) perPage);
		logs = logs.subList(page * perPage, page * perPage + perPage <= logs.size() ? page * perPage + perPage : logs.size());
		result.logs = logs;
		result.currentPage = page + 1;
		renderJSON(result);
	}
	
	/**
	 * Shows the log with its details, or the whole list of logs in this project
	 * @param projectId the project's id
	 * @param logId the log's id
	 */
	public static void view(long projectId, long logId) {
		if (logId == 0 && projectId != 0) {
			List<Log> logs = project.logs;
			Project project = null;
			if (projectId < 0) {
				Security.check(Security.getConnected().isAdmin);
				logs = Log.findAll();
			} else {
				project = Project.findById(projectId);
				Security.check(Security.getConnected().in(project).can("manageLogs"));
			}
			render(project, logs, projectId);
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

