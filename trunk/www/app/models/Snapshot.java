package models;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import models.Component.ComponentRowh;
import play.data.validation.Required;

@Entity
public class Snapshot extends SmartModel {
	/**
	 * The user took this snapshot.
	 */
	@ManyToOne
	public User user;
	/**
	 * The Type of the snapshot like used in filtering and giving titles.
	 */
	public String type;
	/**
	 * The date the snapshot was taken.
	 */
	public Date date;
	/**
	 * The sprint which this snapshot is taken in.
	 */
	@ManyToOne
	public Sprint sprint;
	/**
	 * The component for which the final snapshot is taken for in the end of
	 * each sprint.
	 */
	@ManyToOne
	public Component component;
	/**
	 * The Board the snapshot is taken for.
	 */
	@ManyToOne
	public Board board;
	/**
	 * The data array list of component row h which has a title and list of
	 * strings"the task info"
	 */
	@Required
	public ArrayList<ComponentRowh> data;
	/**
	 * The list of strings of names of columns.
	 */
	@Required
	public ArrayList<String> Columnsofsnapshot;
	/**
	 * If true is deleted
	 */
	public boolean deleted;
/**
 * class constructor initializing the snapshot's date
 */
	public Snapshot() {
		date = new Date();
	}
}
