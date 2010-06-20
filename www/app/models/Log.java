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
 * @Task C1S1
 * @Task C1S3
 */
@Entity
public class Log extends SmartModel {

	@ManyToOne
	@Required
	public User user;
	@Required
	public String action_type;
	@Required
	public String resource_type;
	@Required
	public long resource_id;
	@ManyToOne
	@Required
	public Project project;
	@Required
	public Date date;
	boolean deleted;
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
	 * @Task C1S1
	 * @Task C1S3
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
	 * than or equal to the date of the log this method is invoked on.
	 * 
	 * @return <code>Log: </code>the first log in the database that has a date
	 *         less than or equal to the date of the log this method is invoked
	 *         on.
	 */
	public Log next() {
		return Log.find("date <= ? order by date desc", date).first();
	}

	/**
	 * This method returns the first log in the database that has a date greater
	 * than or equal to the date of the log this method is invoked on.
	 * 
	 * @return <code>Log: </code>the first log in the database that has a date
	 *         greater than or equal to the date of the log this method is
	 *         invoked on.
	 */
	public Log prev() {
		return Log.find("date >= ? order by date asc", date).first();
	}

}
