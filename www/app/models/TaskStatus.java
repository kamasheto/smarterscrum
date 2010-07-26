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

	@Required
	@MaxSize (100)
	public String name;

	@ManyToOne
	public Project project;

	public boolean deleted;

	@OneToMany (mappedBy = "taskStatus", cascade = CascadeType.ALL)
	public List<Task> Tasks;

	@OneToMany (mappedBy = "taskStatus", cascade = CascadeType.ALL)
	public List <Column> columns;
	
	public boolean pending;
	
	public boolean closed;

	public void init() {
		Column column = new Column(name, project.board, this).save();
			for(int i=0;i<project.components.size();i++)
		{
			Board b = project.components.get(i).componentBoard;
			Column c = new Column(name,b,this);
			c.save();
		}
			columns.add(column);
			this.save();
	}

	public TaskStatus () {
		this.pending = false;
		this.closed = false;
		Tasks = new ArrayList<Task>();
		columns = new ArrayList<Column>();
	}

}
