package controllers;

import java.util.Date;
import java.util.List;

import controllers.CRUD.ObjectType;
import controllers.CRUD.ObjectType.ObjectField;

import models.Board;
import models.Log;
import models.Project;
import models.Sprint;
import models.Task;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.libs.Mail;
import play.mvc.With;

/**
 * @author Amr Tj.Wallas
 * @author Amr Abd El Wahab
 * @author Moataz Mekki
 * @version 642
 * @Task C1S1
 * @Task C1S3
 */
@With (Secure.class)
public class Logs extends CRUD {
	/**
	 * New method by mahmoudsakr
	 * 
	 * @param project
	 * @param action_type
	 * @param resource_type
	 * @param resource_id
	 * @return
	 */
	public static boolean addLog(Project project, String action_type, String resource_type, long resource_id) {
		addLog(Security.getConnected(), action_type, resource_type, resource_id, project, new Date());
		return true;
	}

	/**
	 * This method simply takes the required parameters for a log record in the
	 * database creates a new log and then saves it into the database
	 * 
	 * @param user
	 *            The performer of that action
	 * @param action_type
	 *            The type of action performed <post, delete, edit>
	 * @param resource_type
	 *            The resource type involved in that action <Sprint, Task,
	 *            Board, .. etc>
	 * @param resource_id
	 *            The resource id of that resource in the db.
	 * @param project
	 *            The project that user is performing the action in.
	 * @param date
	 *            Current date/time of action.
	 * @return <code>true</code> if the log entry was successfully saved.
	 * @see models.Log
	 * @Task C1S1
	 */
	public static boolean addLog(User user, String action_type, String resource_type, long resource_id, Project project, Date date) {

		Log newLog = new Log(user, action_type, resource_type, resource_id, project, date);

		newLog.save(); /* Save That Log Entry */

		return true;

	}

	/**
	 * This method fetches next 5 logs from the Database and renders them
	 * ordered by date in descending (Pagination Factor = 5 logs per page for
	 * Testing)
	 * 
	 * @author Amr Tj.Wallas
	 * @param page
	 *            The current logs page number at the view
	 * @override Method "list()" in Crud/Controllers/CRUD.java
	 * @see views/Logs/list.html
	 * @Task C1S3
	 */
	@Check ("systemAdmin")
	public static void list(int page, String filter) {
		int index = page * 25;
		List<Log> logs = null;
		// List<ObjectField> fields = ObjectType.get( Logs.class ).getFields();
		if (filter != null && filter.contains("'")) {
			flash.error("ILLEGAL CHARACTER !!");
			filter = null;
			logs = Log.find("order by date desc").from(index).fetch(25);
			render(logs, page);
		} else if (filter != null)
			logs = Log.find("user.name like '%" + filter + "%' or " + "action_type like '%" + filter + "%' or " + "resource_type like '%" + filter + "%' or " + "project.name like '%" + filter + "%' or " + "date like '%" + filter + "%' order by date desc").from(index).fetch(25);
		else
			logs = Log.find("order by date desc").from(index).fetch(25);
		render(logs, page, filter);
	}

}
