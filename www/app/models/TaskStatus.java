package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.data.validation.MaxSize;
import play.data.validation.Required;

@Entity
public class TaskStatus extends SmartModel {
	/**
	 * name
	 */

	@Required
	@MaxSize (100)
	public String name;
	/**
	 * Its project
	 */

	@ManyToOne
	public Project project;
	/**
	 * If this status is new its true .
	 */

	public boolean isNew;
	/**
	 * if deleted is true
	 */

	public boolean deleted;
	/**
	 * List of tasks that have this task status.
	 */

	@OneToMany (mappedBy = "taskStatus", cascade = CascadeType.ALL)
	public List<Task> Tasks;
	/**
	 * Columns indicating this status in each board.
	 */

	@OneToMany (mappedBy = "taskStatus", cascade = CascadeType.ALL)
	public List <BoardColumn> columns;
	/**
	 * If this status stands for a pending status
	 */

	public boolean pending;
	/**
	 * If this status stands for a closed status
	 */
	public boolean closed;
	/**
	 * Initialize a column for every status in every board in the project.
	 */
	public void init() {
		BoardColumn column = new BoardColumn(name, project.board, this).save();
			for(int i=0;i<project.components.size();i++)
		{
			Board b = project.components.get(i).board;
			BoardColumn c = new BoardColumn(name,b,this);
			c.save();
		}
			columns.add(column);
			this.save();
	}
	/**
	 * Class constructor just initializing the list of tasks for this task status.
	 * 
	
	 */

	public TaskStatus () {
		this.pending = false;
		this.closed = false;
		Tasks = new ArrayList<Task>();
		columns = new ArrayList<BoardColumn>();
	}

}
