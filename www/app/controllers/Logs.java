package controllers;

import java.util.Date;
import java.util.List;

import models.Log;
import models.Project;
import models.User;
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
public class Logs extends SmartCRUD {
	/**
	 * Shortcut to more general Logs.addLog
	 * 
	 * @param project
	 * @param action_type
	 * @param resource_type
	 * @param resource_id
	 * @return
	 * @see Logs.addLog(User, String, String, long, Project, Date)
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
		if (user.isAdmin)
			newLog.madeBySysAdmin = true;

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
	public static void list(int page, String filter) {
		Security.check(Security.getConnected().isAdmin);
		int index = page * 25;
		List<Log> logs = null;
		// List<ObjectField> fields = ObjectType.get( Logs.class ).getFields();
		if (filter != null && filter.contains("'")) {
			flash.error("ILLEGAL CHARACTER !!");
			filter = null;
			logs = Log.find("order by date desc").from(index).fetch(25);
			redirect("/admin");
		} else if (filter != null)
			{
			if(filter.charAt(0) == ' ' || filter.charAt(filter.length()-1) == ' ' || filter.contains("  "))
			{
				flash.error("Please Remove any extra Spaces !!");
				filter = null;
				logs = Log.find("order by date desc").from(index).fetch(25);
				redirect("/admin");
			}
			String filter2 = "('" + filter + "')";
			filter2 = filter2.replaceAll(" ", "','");
			filter2 = filter2.toLowerCase();
			//System.out.println(filter2);
			//logs = Log.find("LOWER(user.name) in "+filter2 +" and LOWER(action_type) in " + filter2 + " and LOWER(resource_type) in " + filter2 + " and LOWER(project.name) in " + filter2 + " and LOWER(date) in " + filter2 + " order by date desc").from(index).fetch(25);
			//System.out.println(smartFilter(filter2)+" <<there");
			if(!smartFilter(filter2).isEmpty())
				logs = Log.find(smartFilter(filter2)).from(index).fetch(25);
			}
		else
			logs = Log.find("order by date desc").from(index).fetch(25);
		render(logs, page, filter);
	}
	
	public static String smartFilter(String filter)
	{
		String query="";
		if(User.find("LOWER(name) in "+ filter).first() != null)
			query = query + "LOWER(user.name) in "+filter;
		if(Log.find("LOWER(action_type) in "+filter).first()!=null)
			if(!query.isEmpty())
				query = query + " and LOWER(action_type) in " + filter;
			else
				query = query + "LOWER(action_type) in " + filter;
		if(Log.find("LOWER(resource_type) in " + filter).first()!=null)
			if(!query.isEmpty())
				query = query + " and LOWER(resource_type) in " + filter;
			else
				query = query + "LOWER(resource_type) in " + filter;
		if(Project.find("LOWER(name) in "+filter).first()!=null)
			if(!query.isEmpty())
				query = query + " and LOWER(project.name) in " + filter;
			else
				query = query + "LOWER(project.name) in " + filter;
		if(Log.find("LOWER(date) in "+filter).first()!=null)
			if(!query.isEmpty())
				query = query + " and LOWER(date) in " + filter;
			else
				query = query + "LOWER(date) in " + filter;
		if(query.isEmpty())
			return query;
		else
		{
			query = query + " order by date desc";
			return query;
		}
				
	}

	public static void show(String id) {
		list(0, null);
	}

	public static void save(String id) {
		list(0, null);
	}

	public static void blank() {
		list(0, null);
	}

	public static void create() {
		list(0, null);
	}

	public static void delete(String id) {
		list(0, null);
	}
}
