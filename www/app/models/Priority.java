package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Priority
 */
@Entity
public class Priority extends SmartModel implements Comparable {

	/**
	 * Title of this priority
	 */
	public String title;

	/**
	 * Integer value of this priority
	 */
	public int priority;

	/**
	 * Deleted or not?
	 * 
	 * @deprecated
	 */
	public boolean deleted;

	/**
	 * Project this priority belongs to (one prio -> one project, one project ->
	 * many prios)
	 */
	@ManyToOne
	public Project project;

	/**
	 * @override
	 */
	public int compareTo(Object o) {
		Priority mine = (Priority) o;
		return this.priority > mine.priority ? 1 : this.priority < mine.priority ? -1 : 0;
	}

}
