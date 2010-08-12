package controllers;

import java.util.Date;
import java.util.List;

import models.Column;
import models.Component;
import models.Invite;
import models.Log;
import models.Meeting;
import models.MeetingAttendance;
import models.Priority;
import models.ProductRole;
import models.Project;
import models.Request;
import models.Snapshot;
import models.Sprint;
import models.Task;
import models.TaskStatus;
import models.TaskType;
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
@With( Secure.class )
public class Logs extends SmartCRUD
{
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
	public static Log addLog( Project project, String action_type, String resource_type, long resource_id )
	{
		Log newLog = new Log ( Security.getConnected(), action_type, resource_type, resource_id, project, new Date() );
		if( Security.getConnected().isAdmin )
			newLog.madeBySysAdmin = true;
		newLog.save();
		return newLog;
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
	 * @return <code>Log</code> the log entry that was saved.
	 * @see models.Log
	 * @Task C1S1
	 */
	public static Log addLog( User user, String action_type, String resource_type, long resource_id, Project project, Date date )
	{
		Log newLog = new Log( user, action_type, resource_type, resource_id, project, date );
		if( user.isAdmin )
			newLog.madeBySysAdmin = true;

		newLog.save(); /* Save That Log Entry */
		return newLog;
	}

	/**
	 * This log method should be only used in case of adding system logs or
	 * custom logs. For example logs that are not related to a certain user nor
	 * a project
	 * <p>
	 * <b><font color="red">ADD THE EXACT LOG MESSAGE WITH ALL HTML TAGS EXACTLY
	 * AS IF YOU ARE IN A VIEW</font></b>
	 * </p>
	 * 
	 * @param logMessage
	 *            The custom log message you want to be displayed in the logs
	 *            view.
	 * @return <code>true</code> if the log entry was successfully saved.
	 * @see models.Log
	 * @since Sprint4
	 */
	public static boolean addLog( String logMessage )
	{
		Log newLog = new Log( null, "", "", -1, null, new Date() );
		newLog.logMessage = logMessage;
		newLog.save();
		return true;
	}
	
	/**
	 * <b><font color = "red">WARNING: THIS METHOD SHOULD ONLY BE USED WITH LOGS
	 * THAT ARE RELATED TO BOTH: A USER AND A PROJECT.</font></b><br />
	 * <u>You can use Logs.addLog(..).logMessage("your custom log goes here") to 
	 * customize your log message (Highly recommended in some cases)</u><br />
	 * Smarter addLog method signature. Indicates that an action was performed on
	 * an object.
	 * @param action The action type performed by the connected user
	 * @param object The resource in which this action was performed
	 * @return <code>Log</code> The new saved Log.
	 * @since Sprint4
	 */
	public static Log addLog(String action, Object object)
	{
		User user = Security.getConnected();
		Date date = new Date();
		String resourcetType = object.getClass().getName();
		Object [] projectAndId = getProjectAndId(object);
		Project project = (Project) projectAndId[0];
		long resourceId = (Long) projectAndId[1];
		Log newLog = new Log (user,action,resourcetType,resourceId,project,date);
		if(user.isAdmin)
			newLog.madeBySysAdmin = true;
		newLog.save();
		return newLog;
	}
	
	/**
	 * identifies the project and id of an unknown entity object (someModel)
	 * @param object The instance of a given entity / model
	 * @return <code>Object[]</code> an array consisting of two elements:<br />
	 * 1. The Project Object. <br />
	 * 2. The id of that object or Entity.
	 * @since Sprint4
	 */
	public static Object[] getProjectAndId(Object object)
	{
		Object [] projectAndId = new Object [2];
		String resourceType = object.getClass().getName();
		if (resourceType.equalsIgnoreCase("component"))
		{
			Component c = (Component) object;
			projectAndId[0] = c.project;
			projectAndId[1] = c.id;
		}
		else if (resourceType.equalsIgnoreCase("column"))
		{
			Column c = (Column) object;
			projectAndId[0] = c.board.project;
			projectAndId[1] = c.id;
		}
		else if (resourceType.equalsIgnoreCase("task"))
		{
			Task t = (Task) object;
			projectAndId[0] = t.taskSprint.project;
			projectAndId[1] = t.id;
		}
		else if (resourceType.equalsIgnoreCase("invite"))
		{
			Invite i = (Invite) object;
			projectAndId[0] = i.role.project;
			projectAndId[1] = i.id;
		}
		else if(resourceType.equalsIgnoreCase("meetingAttendance"))
		{
			MeetingAttendance m = (MeetingAttendance) object;
			projectAndId[0] = m.meeting.project;
			projectAndId[1] = m.id;
		}
		else if(resourceType.equalsIgnoreCase("meeting"))
		{
			Meeting m = (Meeting) object;
			projectAndId[0] = m.project;
			projectAndId[1] = m.id;
		}
		else if(resourceType.equalsIgnoreCase("ProductRole"))
		{
			ProductRole r = (ProductRole) object;
			projectAndId[0] = r.project;
			projectAndId[1] = r.id;
		}
		else if(resourceType.equalsIgnoreCase("project"))
		{
			Project p = (Project) object;
			projectAndId[0] = p;
			projectAndId[1] = p.id;
		}
		else if(resourceType.equalsIgnoreCase("taskstatus"))
		{
			TaskStatus ts = (TaskStatus) object;
			projectAndId[0] = ts.project;
			projectAndId[1] = ts.id;
		}
		else if(resourceType.equalsIgnoreCase("tasktype"))
		{
			TaskType tt = (TaskType) object;
			projectAndId[0] = tt.project;
			projectAndId[1] = tt.id;
		}
		else if(resourceType.equalsIgnoreCase("priority"))
		{
			Priority p = (Priority) object;
			projectAndId[0] = p.project;
			projectAndId[1] = p.id;
		}
		else if(resourceType.equalsIgnoreCase("request"))
		{
			Request r = (Request) object;
			projectAndId[0] = r.project;
			projectAndId[1] = r.id;
		}
		else if(resourceType.equalsIgnoreCase("snapshot"))
		{
			Snapshot s = (Snapshot) object;
			projectAndId[0] = s.component.project;
			projectAndId[1] = s.id;
		}
		else if(resourceType.equalsIgnoreCase("sprint"))
		{
			Sprint s = (Sprint) object;
			projectAndId[0] = s.project;
			projectAndId[1] = s.id;
		}
		return projectAndId;
	}

	/**
	 * This method fetches next 25 logs from the Database and renders them
	 * ordered by date in descending (Pagination Factor = 25 logs per page for
	 * Testing)
	 * 
	 * @author Amr Tj.Wallas
	 * @param page
	 *            The current logs page number at the view
	 * @override Method "list()" in Crud/Controllers/CRUD.java
	 * @see views/Logs/list.html
	 * @Task C1S3
	 */
	public static void list( int page, String filter )
	{
		Security.check( Security.getConnected().isAdmin );
		int index = page * 25;
		List<Log> logs = null;
		// List<ObjectField> fields = ObjectType.get( Logs.class ).getFields();
		if( filter != null && filter.contains( "'" ) )
		{
			flash.error( "ILLEGAL CHARACTER !!" );
			filter = null;
			logs = Log.find( "order by date desc" ).from( index ).fetch( 25 );
			redirect( "/admin" );
		}
		else if( filter != null && !filter.isEmpty() )
		{
			if( filter.charAt( 0 ) == ' ' || filter.charAt( filter.length() - 1 ) == ' ' || filter.contains( "  " ) )
			{
				flash.error( "Please Remove any extra Spaces !!" );
				filter = null;
				logs = Log.find( "order by date desc" ).from( index ).fetch( 25 );
				redirect( "/admin" );
			}
			String filter2 = "('" + filter + "')";
			filter2 = filter2.replaceAll( " ", "','" );
			filter2 = filter2.toLowerCase();
			// System.out.println(filter2);
			// logs = Log.find("LOWER(user.name) in "+filter2
			// +" and LOWER(action_type) in " + filter2 +
			// " and LOWER(resource_type) in " + filter2 +
			// " and LOWER(project.name) in " + filter2 + " and LOWER(date) in "
			// + filter2 + " order by date desc").from(index).fetch(25);
			// System.out.println(smartFilter(filter2)+" <<there");
			if( !smartFilter( filter2 ).isEmpty() )
				logs = Log.find( smartFilter( filter2 ) ).from( index ).fetch( 25 );
		}
		else
			logs = Log.find( "order by date desc" ).from( index ).fetch( 25 );
		render( logs, page, filter );
	}
	
	/**
	 * This method is nothing but an awesome smart query builder. It simply takes
	 * a String "filter" <b>WHICH HAS BEEN PARSED INTO A CERTAIN FORMAT</b> from
	 * the keywords (String) entered by user when searching for logs and then builds
	 * a customized smart Query string which will be used to find possible log 
	 * matches. Please refer to list(..) method above.
	 * <br /><font color="blue">PS: The format of filter should be something similar but
	 * not limited to the string: "('something','something else','etc')"</font>
	 * @param filter  The search String <b>Parsed into a certain format</b>
	 * @return <code>String</code>: The Query String that is used to find logs (log matches)
	 * @see Logs.list(..)
	 * @since Sprint3
	 */
	public static String smartFilter( String filter )
	{
		String query = "";
		if( User.find( "LOWER(name) in " + filter ).first() != null )
			query = query + "LOWER(user.name) in " + filter;
		if( Log.find( "LOWER(action_type) in " + filter ).first() != null )
			if( !query.isEmpty() )
				query = query + " and LOWER(action_type) in " + filter;
			else
				query = query + "LOWER(action_type) in " + filter;
		if( Log.find( "LOWER(resource_type) in " + filter ).first() != null )
			if( !query.isEmpty() )
				query = query + " and LOWER(resource_type) in " + filter;
			else
				query = query + "LOWER(resource_type) in " + filter;
		if( Project.find( "LOWER(name) in " + filter ).first() != null )
			if( !query.isEmpty() )
				query = query + " and LOWER(project.name) in " + filter;
			else
				query = query + "LOWER(project.name) in " + filter;
		if( Log.find( "LOWER(date) in " + filter ).first() != null )
			if( !query.isEmpty() )
				query = query + " and LOWER(date) in " + filter;
			else
				query = query + "LOWER(date) in " + filter;
		if( query.isEmpty() )
			return query;
		else
		{
			query = query + " order by date desc";
			return query;
		}

	}

	public static void show( String id )
	{
		list( 0, null );
	}

	public static void save( String id )
	{
		list( 0, null );
	}

	public static void blank()
	{
		list( 0, null );
	}

	public static void create()
	{
		list( 0, null );
	}

	public static void delete( String id )
	{
		list( 0, null );
	}
}
