package models;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Comment extends SmartModel{
	
	//The comment
	public String comment;
	
	//Relation with Task
	@ManyToOne
	public Task task;
	
	//Relation with User
	@ManyToOne
	public User author;
	
	public byte type;
	
	public Comment(User user, long taskId, String comment){
		this.comment = comment;
		task = Task.findById(taskId);
		author = user;
		//this.type = type;
	}
}