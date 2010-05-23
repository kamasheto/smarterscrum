package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

/*
 * The class Request Reviewer in which the user can request to be reviewer and
 * the Scrum master can respond to that request
 * @author hoksha
 */
@Entity
public class Requestreviewer extends Model {
	private static final long serialVersionUID = 1L;
	/*
	 * the user request
	 */
	@ManyToOne
	public User user;
	/*
	 * the component of the user requested to be reviewer
	 */
	@ManyToOne
	public Component component;
	/*
	 * the type the user Request to be reviewer of
	 */
	@ManyToOne
	public TaskType types;

	/*
	 * a boolean variable to show if it is accepted or not
	 */
	public boolean accepted;

	/**
	 * This is a Class Constructor Creates a new Requestreviewer object
	 * 
	 * @author hoksha
	 * @param user
	 *            the user who requests to be reviewer
	 * @param component
	 *            the component of the user who requested
	 * @param types
	 *            the type
	 * @return void
	 * @task C3 S23
	 * @sprint 2
	 */

	public Requestreviewer (User user, Component component, TaskType types) {
		this.user = user;
		this.component = component;
		this.types = types;
		this.accepted = false;
	}

}
