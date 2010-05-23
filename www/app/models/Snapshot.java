package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import models.Component.ComponentRow;
import models.Component.ComponentRowh;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Snapshot extends Model {
	@ManyToOne
	public User user;
	public String type;
	public Date date;
	@ManyToOne
	public Sprint sprint;

	@ManyToOne
	public Board board;
	@Required
	public ArrayList<ComponentRowh> data;
	@Required
	public ArrayList<String> Columnsofsnapshot;

	public Snapshot () {
		date = new Date();
	}
}
