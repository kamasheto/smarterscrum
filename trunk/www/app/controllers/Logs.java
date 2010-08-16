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
	public static void list(long projectId) {
		Project project = Project.findById(projectId);
		Security.check(Security.getConnected().in(project).can("manageLogs"));
		
		// It's handset here so that we can enhance it in the future to accomodate for all logs (where project = null, perhaps)
		// accordingly, the views loop over logs, not project.logs
		List<Log> logs = project.logs;
		render(project, logs);
	}
	
	/**
	 * Shows the log with its details
	 * @param logId the log's id
	 */
	public static void view(long logId) {
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

