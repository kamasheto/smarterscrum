package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Board extends SmartModel {

	@OneToMany (mappedBy = "board")
	public List<Snapshot> snapshot;
	@OneToMany (mappedBy = "board")
	public List<Column> columns;

	@OneToOne
	public Project project;

	@OneToOne
	public Component component;

	public String name;
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
	 * Default board constructor
	 * */
	public Board () {
		snapshot = new ArrayList<Snapshot>();
		columns = new ArrayList<Column>();
	}

}
