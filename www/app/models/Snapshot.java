package models;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Component.ComponentRowh;
import play.data.validation.Required;

@Entity
public class Snapshot extends SmartModel {
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
	
	public boolean deleted;

	public Snapshot () {
		date = new Date();
	}
}
