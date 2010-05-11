package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Permission extends Model {

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
