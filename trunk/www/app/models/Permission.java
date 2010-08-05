package models;

import javax.persistence.Entity;

/**
 * Permissions in our system - these should be defined once, and only once
 */
@Entity
public class Permission extends SmartModel {

	/**
	 * Name of this permission (unique)
	 */
	public String name;

	/**
	 * Description of this permission
	 */
	public String description;

	/**
	 * Default constructor
	 * 
	 * @param perm
	 *            Name of this permission
	 * @param perm2
	 *            description of this permission
	 */
	public Permission (String perm, String perm2) {
		this.name = perm;
		this.description = perm2;
	}

	/**
	 * Returns string representation of this permission (the description)
	 */
	public String toString() {
		return description;
	}
}
