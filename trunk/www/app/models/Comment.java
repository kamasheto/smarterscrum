package models;

import java.util.Date;

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
	
	public long timeOfComment;
	
	public boolean deleted=false;
	
	/***
	 * Comment constructor
	 * 
	 * @param user 
	 * 			comment author
	 * @param taskId 
	 * 			task that the comment was add for
	 * @param comment 
	 * 			comment added by the use on a certain task
	 */
	public Comment(User user, long taskId, String comment){
		this.comment = comment;
		task = Task.findById(taskId);
		author = user;
		//this.type = type;
		timeOfComment = new Date().getTime();
	}
	
	/***
	 * Invoked on a comment to delete it
	 */
	public  void deleteComment(){
		this.deleted = true;
		this.save();
	}
}
