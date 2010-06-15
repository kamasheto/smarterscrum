package models;

import javax.persistence.Entity;

@Entity
public class Permission extends SmartModel {

	public String name;

	public String description;

	public Permission (String perm, String perm2) {
		this.name = perm;
		this.description = perm2;
	}

	public String toString() {
		return description;
	}
}
