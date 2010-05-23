package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class Priority extends Model implements Comparable {

	public String title;
	public int priority;
	public boolean deleted = false;

	// Relation with Project many to One
	@ManyToOne
	public Project project;

	// @Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		Priority mine = (Priority) o;
		if (this.priority > mine.priority) {
			return 1;
		} else if (this.priority < mine.priority) {
			return -1;
		} else
			return 0;
	}

}
