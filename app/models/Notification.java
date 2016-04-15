package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Notification model
 */
@Entity
public class Notification extends SmartModel {

	/**
	 * Who this notification belongs to
	 */
	@ManyToOne
	public User receiver;

	/**
	 * The performer of this action
	 */
	@ManyToOne
	public User actionPerformer;

	/**
	 * Project this notification belongs to
	 */
	@ManyToOne
	public Project project;

	/**
	 * Action type of this action
	 */
	public String actionType;

	/**
	 * Resource URL
	 */
	public String resourceURL;

	/**
	 * Resource type
	 */
	public String resourceType;

	/**
	 * Resource name
	 */
	public String resourceName;

	/**
	 * Importance of this notification (-1 being bad, 1 being positive, and 0
	 * being neutral)
	 */
	public byte importance;

	/**
	 * Whether this notification is unread or not
	 */
	public boolean unread;

	/**
	 * Default constructor
	 * 
	 * @param receiver
	 * @param actionPerformer
	 * @param actionType
	 * @param resourceURL
	 * @param resourceType
	 * @param resourceName
	 * @param importance
	 */
	public Notification (User receiver, User actionPerformer, String actionType, String resourceURL, String resourceType, String resourceName, byte importance) {
		this.receiver = receiver;
		this.actionPerformer = actionPerformer;
		this.actionType = actionType;
		this.resourceURL = resourceURL;
		this.resourceType = resourceType;
		this.resourceName = resourceName;
		this.importance = importance;
		this.unread = true;
	}

}
