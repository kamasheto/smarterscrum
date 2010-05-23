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
import play.db.jpa.Model;

@Entity
public class TaskStatus extends Model {

	@Required
	@MaxSize (100)
	public String name;

	@ManyToOne
	public Project project;

	public boolean deleted;

	@OneToMany (mappedBy = "taskStatus", cascade = CascadeType.ALL)
	public List<Task> Tasks;

	@OneToOne
	public Column column;

	public void init() {
		// Project p = this.project;
		// Board b = p.board;
		column = new Column(name, project.board).save();
		// col.board = project.board;
		// column = new Column().save();
		// System.out.println("Assigning board now: " + project.board);
		// column.board = project.board;
		// col = this;
		// this.column = col;
		// column.name = name;
		// column.save();

		// this.save();
	}

	public TaskStatus () {
		Tasks = new ArrayList<Task>();
	}

}
