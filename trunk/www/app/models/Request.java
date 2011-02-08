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
	/**
	 * user who requested the request
	 */
	@ManyToOne
	public User user;

	/**
	 * the request was in the project
	 */
	// Uncommented in Sprint2 By Tj.Wallas_ , Task C1S14
	@ManyToOne
	public Project project;

	/**
	 * the user requested the "role"
	 */
	@ManyToOne
	public Role role;

	// public boolean pending;

	/**
	 * hash used for accepted or declining the request
	 */
	public String hash; // will have to use for accepting/declining

	/**
	 * whether it's a deletion request
	 */
	public boolean isDeletion;

	/**
	 * the request belongs to the component
	 */
	public Component component;

	// requests
	
	/**
	 * Creates a new request by a user to a certain role.
	 * @param user the user who requested the role
	 * @param role the requested role
	 */
	public Request( User user, Role role )
	{
		this.user = user;
		this.role = role;
		this.hash = Application.random_hash( 8 );
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
		this.isDeletion = true;
		this.hash = Application.random_hash( 8 );
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
		this.isDeletion = true;
		this.hash = Application.random_hash( 8 );
	}

}
