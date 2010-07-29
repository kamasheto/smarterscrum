package controllers;

import models.Comment;
import play.mvc.With;

@With (Secure.class)
public class Comments extends SmartController {
	
	public static void deleteComment(long id){
		Comment comment = Comment.findById(id);
		if(comment == null)
			renderText("Comment not found.");
		else
		{
			comment.deleteComment();
			renderText("Comment deleted successfully.");
		}
	}
}
