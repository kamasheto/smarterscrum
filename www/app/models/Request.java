package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import controllers.Application;

/**
 * The Request model class represents a given request made by one or more users
 * in a specific <b>PROJECT</b> . Requests may be join requests with a certain
 * role or deletion requests from that specific project.
 * 
 * @author moataz_mekki
 * @author Amr Tj.Wallas
 * @Task C1S14
 */
@Entity
public class Request extends SmartModel
{
	@ManyToOne
	public User user;

	// Uncommented in Sprint2 By Tj.Wallas_ , Task C1S14
	@ManyToOne
	public Project project;

	@ManyToOne
	public Role role;

	// public boolean pending;

	public String hash; // will have to use for accepting/declining

	public boolean deleted;

	public Component component;

	// requests

	public Request( User user, Role role )
	{
		this.user = user;
		this.role = role;
		this.hash = Application.randomHash( 8 );
		this.project = role.project;
		// this.pending = pending;
	}

	/**
	 * This Constructor Creates a new <b>DELETION</b> request made by a user
	 * "user" in a Project "project". In other words the User "user" has
	 * requested to be deleted from the project "project".
	 * 
	 * @author Amr Tj.Wallas
	 * @param user
	 *            The User who requested to be deleted from the project.
	 * @param project
	 *            The Project that user wants to be deleted from.
	 * @param hash
	 *            Hash value of that request.
	 * @since Sprint 2.
	 * @Task C1S14
	 */
	public Request( User user, Project project )
	{
		this.user = user;
		this.project = project;
		this.deleted = true;
		this.hash = Application.randomHash( 8 );
	}

	/**
	 * @author OmarNabil This is a constructor for the request of deletion from
	 *         a component It takes the user that wants to be deleted and the
	 *         component that he is requesting to be deleted from it and
	 *         initiates new request
	 * @param user
	 * @param component
	 */
	public Request( User user, Component component )
	{
		this.user = user;
		this.component = component;
		this.deleted = true;
		this.hash = Application.randomHash( 8 );
	}

}
