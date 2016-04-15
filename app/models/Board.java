package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Board extends SmartModel {

	/***
	 * a board can have many snapshots whereas a snapshot belongs to exactly one board
	 */
	@OneToMany (mappedBy = "board")
	public List<Snapshot> snapshot;
	
	/***
	 * a board has many columns whereas a column belongs to one board
	 */
	@OneToMany (mappedBy = "board")
	public List<BoardColumn> columns;

	/***
	 * a project has one board & a board can only belong to one project
	 */
	@OneToOne
	public Project project;

	/***
	 * a component has one board a board can only belong to one component
	 */
	@OneToOne
	public Component component;

	/***
	 * a flag that determines whether the board is deleted or not
	 */
	public boolean deleted;

	
	/***
	 * Project board constructor
	 * 
	 * @param project 
	 * 			board project
	 */
	public Board (Project project) {
		this();
		this.project = project;
	}

	
	/***
	 * Component board constructor
	 * 
	 * @param component2 
	 * 			component board
	 */
	public Board (Component component2) {
		this();
		component = component2;
	}

	/***
	 * Default board constructor that associated a 
	 * list of snapshots & a list of columns to a board
	 * */
	public Board () {
		snapshot = new ArrayList<Snapshot>();
		columns = new ArrayList<BoardColumn>();
	}

}
