package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class GameSession extends Model implements Comparable {

	@ManyToOne
	public User user;

	@ManyToOne
	public Game game;

	public long lastClick;

	public long started;

	public int compareTo(Object o) {
		GameSession o2 = (GameSession) o;
		if (this.started < o2.started) {
			return -1;
		} else if (this.started > o2.started)
			return 1;
		else
			return 0;
	}
}
