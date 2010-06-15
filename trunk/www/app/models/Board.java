package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class Board extends SmartModel {

	// public ArrayList<ArrayList<ArrayList<String>>> data;
	@OneToMany (mappedBy = "board")
	public List<Snapshot> snapshot;
	@OneToMany (mappedBy = "board")
	public List<Column> columns;

	// @OneToMany (mappedBy = "board")
	// public List<HistoryBoard> historyBoard;

	@OneToOne
	public Project project;

	@OneToOne
	public Component component;

	public String name;
	public boolean deleted;

	// public String[] data;

	public Board (Project project) {
		this();
		this.project = project;
	}

	public Board (Component component2) {
		this();
		component = component;
	}

	public Board () {
		snapshot = new ArrayList<Snapshot>();
		columns = new ArrayList<Column>();
	}

}
