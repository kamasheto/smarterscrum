package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import controllers.Application;

/**
 * Invite model
 * 
 * @author mahmoudsakr
 */
@Entity
public class Invite extends SmartModel {
	/**
	 * *..1 relation with users
	 */
	@ManyToOne
	public User user;

	/**
	 * *..1 relation with role
	 */
	@ManyToOne
	public Role role;

	/**
	 * invite hash
	 */
	public String hash;

	/**
	 * Full constructor. Sets a default 20-char (random) hash
	 * 
	 * @param user
	 *            user to invite
	 * @param role
	 *            role to invite user to
	 */
	public Invite (User user, Role role) {
		this.user = user;
		this.role = role;
		hash = Application.randomHash();
	}

}
