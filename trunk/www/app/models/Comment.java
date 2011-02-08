package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Comment extends SmartModel{
	
	/***
	 * comment message
	 */
	public String comment;
	
	/***
	 * each task can have many comments & each comment belongs only to one task
	 */
	@ManyToOne
	public Task task;

	/***
	 * each user can add many comments while each comment is added only be one user
	 */
	@ManyToOne
	public User author;
	
	/***
	 * determines the type of the comment
	 */
	public byte type;
	
	/***
	 * determines the time when the comment was added
	 */
	public long timeOfComment;
	
	/***
	 * a flag that determines whether the comment is deleted or not
	 */
	public boolean deleted=false;
	
	/***
	 * Comment constructor that sets the author of the comment,
	 * the task that the comment was added to & the comment itself
	 * 
	 * @param user 
	 * 			author of the comment
	 * @param task_id 
	 * 			task that the comment was add to
	 * @param comment 
	 * 			comment added by the user on a certain task
	 */
	public Comment(User user, long task_id, String comment){
		this.comment = comment;
		task = Task.findById(task_id);
		author = user;
		timeOfComment = new Date().getTime();
	}
	
	/***
	 * Invoked on a comment to delete it by setting its deleted flag to true
	 */
	public  void delete_comment(){
		this.deleted = true;
		this.save();
	}
}
