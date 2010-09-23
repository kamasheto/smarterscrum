package models;

import javax.persistence.Entity;
import play.i18n.Messages;

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
	 * Default constructor
	 * 
	 * @param perm
	 *            Name of this permission
	 */
	public Permission (String perm) {
		this.name = perm;
	}

	/**
	 * Returns string representation of this permission (the description)
	 */
	public String toString() {
		return Messages.get("perm_" + this.name);
	}
}
