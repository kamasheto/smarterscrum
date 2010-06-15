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

	@Required
	@MaxSize (100)
	public String name;

	@ManyToOne
	public Project project;

	public boolean deleted;

	@OneToMany (mappedBy = "taskType", cascade = CascadeType.ALL)
	public List<Task> Tasks;

	String hexColor = "#" + Integer.toHexString(new Color(new Random().nextInt(50) + 180, new Random().nextInt(50) + 180, new Random().nextInt(50) + 180).getRGB() & 0xFFFFFF);

	public TaskType () {
		Tasks = new ArrayList<Task>();
	}

}
