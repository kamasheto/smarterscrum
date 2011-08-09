package controllers;

import play.*;
import play.mvc.*;

import java.util.*;
import java.lang.reflect.*;

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
		List<Log> toFilter = null;
		if (project == null) {
			Security.check(Security.getConnected().isAdmin);
			toFilter = Log.findAll();
		} else {
			Security.check(Security.getConnected().in(project).can("manageLogs"));
			toFilter = project.logs;
		}
		List<Log> filteredLogs = new ArrayList<Log>();
		for (Log log : toFilter) {
			if (filter.length() == 0 || log.message.contains(filter)) {
				filteredLogs.add(log);
			}
		}
		page--;
		List<Log> pageOfLogs = filteredLogs.subList(page * perPage, page * perPage + perPage <= filteredLogs.size() ? page * perPage + perPage : filteredLogs.size());		
		LogSearchResult result = new LogSearchResult();
		result.logs = pageOfLogs;
		result.currentPage = page + 1;
		result.totalPages = (int) filteredLogs.size() / perPage;
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
			List<Log> logs = null;
			if (project == null) {
				Security.check(Security.getConnected().isAdmin);
				logs = Log.findAll();
			} else {
				Security.check(Security.getConnected().in(project).can("manageLogs"));	
				logs = project.logs;
			}
			render(logs, projectId);
		} else if(logId != 0) {
			Log log = Log.findById(logId);
			Security.check(Security.getConnected().in(log.get(Project.class)).can("manageLogs"));
			LogInfo[] logInfo = projectId > 0 ? 
								new LogInfo[] 
									{new LogInfo("User", User.class),
									// new LogInfo("Project", Project.class),
									new LogInfo("Component", Component.class),
									new LogInfo("Task", Task.class),
									new LogInfo("Meeting", Meeting.class),
									new LogInfo("Board", Board.class),
									new LogInfo("Column", BoardColumn.class)
									/*,new LogInfo("Snapshot", Snapshot.class)*/} : 
								new LogInfo[] 
									{new LogInfo("User", User.class),
									new LogInfo("Project", Project.class),
									new LogInfo("Component", Component.class),
									new LogInfo("Task", Task.class),
									new LogInfo("Meeting", Meeting.class),
									new LogInfo("Board", Board.class),
									new LogInfo("Column", BoardColumn.class)
									/*,new LogInfo("Snapshot", Snapshot.class)*/};
								
			render(log, logInfo);	
		}
	}
}
