package models;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.validation.MaxSize;
import play.data.validation.Required;

@Entity
public class TaskType extends SmartModel {
	/**
	 * name
	 */
	@Required
	@MaxSize(100)
	public String name;
	/**
	 * the project it belongs to
	 */
	@ManyToOne
	public Project project;
	/**
	 * if deleted=true
	 */
	public boolean deleted;
	/**
	 * The list of tasks that have this type
	 */
	@OneToMany(mappedBy = "taskType", cascade = CascadeType.ALL)
	public List<Task> Tasks;
	/**
	 * The color used to indicate the task type in the board.
	 */
	String hexColor = "#"
			+ Integer.toHexString(new Color(new Random().nextInt(50) + 180,
					new Random().nextInt(50) + 180,
					new Random().nextInt(50) + 180).getRGB() & 0xFFFFFF);

	/**
	 * Class constructor just initializing the list of tasks for this task type.
	 * 
	
	 */

	public TaskType() {
		Tasks = new ArrayList<Task>();
	}

}
