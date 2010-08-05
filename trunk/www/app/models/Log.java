package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.data.validation.Required;

/**
 * This class represents The Log Entity in the database and defines it's
 * relations with other entities.
 * 
 * @author Amr Tj.Wallas
 * @version 643
 */
@Entity
public class Log extends SmartModel {
	/**
	 * Users may have many logs, whereas each log may have only one user
	 */
	@ManyToOne
	public User user;

	/**
	 * Action of the log, usually something of the sort: Create, Delete, Edit
	 */
	public String action_type;

	/**
	 * Resource type, something of the sort of an entity, eg: Meeting, Sprint,
	 * User
	 */
	public String resource_type;

	/**
	 * The resource of the resource_type
	 */
	public long resource_id;

	/**
	 * A project may have many logs, whereas each log may only belong to one
	 * project
	 */
	@ManyToOne
	public Project project;

	/**
	 * The date (time) of this log event
	 */
	public Date date;

	/**
	 * Whether this log item is deleted or not
	 * 
	 * @deprecated
	 */
	public boolean deleted;

	/**
	 * Whether this action was performed by a system admin this is used to keep
	 * track of system admin events in comparison to events by users with
	 * permission
	 */
	public boolean madeBySysAdmin;

	/**
	 * A User "user" Performs a certain Action "action_type" using a certain
	 * resource "resource_type , resource_id" in a specific project "project" at
	 * a certain Date/Time "date".
	 * 
	 * @param user
	 *            The user who made that action
	 * @param action_type
	 *            Post , modify , delete , ... etc)
	 * @param resource_type
	 *            Story , Sprint , .... etc)
	 * @param resource_id
	 *            The id of Story, Sprint, ... etc)
	 * @param project
	 *            The project that user made that action in
	 * @param date
	 *            Date/time of that action
	 * @param deleted
	 *            Set to True if log has been deleted
	 */

	public Log (@Required User user, @Required String action_type, @Required String resource_type, @Required long resource_id, @Required Project project, @Required Date date) {
		this.user = user;
		this.action_type = action_type;
		this.resource_type = resource_type;
		this.resource_id = resource_id;
		this.project = project;
		this.date = date;
		this.deleted = false;
	}

	/**
	 * This method returns the first log in the database that has a date less
	 * than or equal to the date of the log this method is invoked on. Notice
	 * that for some reason this method does not consider the logs in a project,
	 * and returns the next overall log action.
	 * 
	 * @return <code>Log</code> the log that happened right before this log
	 *         action
	 */
	public Log next() {
		return Log.find("date <= ? order by date desc", date).first();
	}

	/**
	 * This method returns the first log in the database that has a date greater
	 * than or equal to the date of the log this method is invoked on. Notice
	 * that for some reason this method does not consider the logs in a project,
	 * and returns the previous overall log action.
	 * 
	 * @return <code>Log</code> the log that happened right after this log
	 *         action
	 */
	public Log prev() {
		return Log.find("date >= ? order by date asc", date).first();
	}

}
