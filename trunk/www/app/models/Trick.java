package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

@Entity
public class Trick extends Model {

	@ManyToOne
	public User user;

	public double estimate;

	@ManyToOne
	public Round round;
}
