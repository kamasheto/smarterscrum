package controllers;

import java.util.List;

import models.Comment;
import models.Task;
import models.CollaborateUpdate;
import play.mvc.With;

@With( Secure.class )
public class Comments extends SmartController
{
	/**
	 * Deletes a comment
	 * 
	 * @param id
	 *            , Comment ID
	 */
	public static void deleteComment( long id )
	{
		Comment comment = Comment.findById( id );
		Security.check( Security.getConnected().in(comment.task.project).can( "modifyTask" ));
		if( comment.deleted )
			notFound();
		if( comment == null )
			renderText( "Comment not found." );
		else
		{
			Task task = comment.task;
			comment.deleteComment();
			CollaborateUpdate.update( task.project , "refresh('comments_"+task.id+"')" );
			renderText( "Comment deleted successfully." );
		}
	}

	/**
	 * Lists all the comments associated with a certain task
	 * 
	 * @param tId
	 *            , Task ID
	 */
	public static void listCommentsofTask( long tId )
	{
		Task task = Task.findById( tId );
		Security.check( Security.getConnected().projects.contains(task.project ));
		if(task.deleted)
			notFound();
		List<Comment> comments = Comment.find( "byTask", task ).fetch();
		render( comments );
	}

	public static void addComment(long taskId, String comment)
	{
		Task task = Task.findById( taskId );
		if(task.deleted)
			notFound();
		Security.check( Security.getConnected().projects.contains(task.project ));
		Comment c = new Comment(Security.getConnected(), taskId, comment);
		c.save();
		task.comments.add( c );
		task.save();
		CollaborateUpdate.update( task.project , "refresh('comments_"+task.id+"')" );
		renderText("The comment was added successfully");
	}
}
