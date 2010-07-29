package controllers;

import java.util.List;

import models.Comment;
import models.Task;
import play.mvc.With;

@With (Secure.class)
public class Comments extends SmartController {
	
	public static void deleteComment(long id){
		Comment comment = Comment.findById(id);
		if(comment.deleted)
			notFound();
		if(comment == null)
			renderText("Comment not found.");
		else
		{
			comment.deleteComment();
			renderText("Comment deleted successfully.");
		}
	}
	
	public static void listCommentsofTask(long tId){
		Task task = Task.findById(tId);
		List<Comment> comments = Comment.find("byTask",task).fetch();
		render(comments);
	}
}
