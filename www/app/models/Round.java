package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Round extends SmartModel {

	int roundNo;

	@ManyToOne
	public Story story;

	@ManyToOne
	public Game game;

	public Round (int no) {
		roundNo = no + 1;
	}

	public boolean isDone() {
		// List<Trick> tricks = Trick.find("byRound", this).fetch();
		List<GameSession> sessions = GameSession.find("byGame", this.game).fetch();
		for (GameSession session : sessions) {
			if (session.lastClick + 5000 >= new Date().getTime()) {
				// if he does NOT have a trick.. return false
				Trick t = Trick.find("byRoundAndUser", this, session.user).first();
				if (t == null) {
					return false;
				}
			}
		}
		return true;

	}

	public String info() {
		String info = "";
		List<Trick> tricks = Trick.find("byRound", this).fetch();

		for (Trick trick : tricks) {
			info += trick.user.name + "|" + trick.estimate + "||";
		}
		return info.substring(0, info.length() - 2);
	}

}
