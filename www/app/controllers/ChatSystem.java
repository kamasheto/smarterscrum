package controllers;

import java.util.List;

import models.ChatRoom;
import models.Game;
import models.Message;
import models.Project;
import models.User;

public class ChatSystem extends SmartController
{

	/**
	 * Adds a new message to a certain room, to help new message retrieval
	 * 
	 * @author Amr Hany
	 * @param message
	 *            , The chat message
	 * @param id
	 *            ,Room ID
	 */
	public static void addMessage( String message, long id )
	{
		ChatRoom room = ChatRoom.findById( id );
		if( room.deleted )
			notFound();
		Project project = Project.find( "byChatroomAndDeleted", room, false ).first();
		if( project == null )
		{
			Game game = Game.find( "byChatroom", room ).first();
			if( game != null )
				project = game.component.project;

		}
		Security.check( Security.getConnected().projects.contains( project ) );
		new Message( Security.getConnected().name, message, room ).save();
	}

	/**
	 * Retrieves new messages and renders them.
	 * 
	 * @author Amr Hany
	 * @param id
	 *            , Room ID
	 */
	public static void newMessages( long id )
	{
		ChatRoom room = ChatRoom.findById( id );
		if( room.deleted )
			notFound();
		Project project = Project.find( "byChatroomAndDeleted", room, false ).first();
		if( project == null )
		{
			Game game = Game.find( "byChatroom", room ).first();
			if( game != null )
				project = game.component.project;

		}
		Security.check( Security.getConnected().projects.contains( project ) );
		List<Message> messages = Message.find( "room = ?1 and stamp > ?2", room, request.date.getTime() - 400 ).fetch();
		if( messages.isEmpty() )
		{
			suspend( "1s" );

		}
		for( Message m : messages )
		{
			m.room = null;
		}

		renderJSON( messages );
	}

	/**
	 * Signs in the currently connected user to the chatroom
	 * 
	 * @author Amr Hany
	 * @param id
	 *            , Room ID
	 */
	public static void enterChat( long id )
	{
		ChatRoom room = ChatRoom.findById( id );
		if( room.deleted )
			notFound();
		User currentUser = Security.getConnected();
		Project project = Project.find( "byChatroomAndDeleted", room, false ).first();
		if( project == null )
		{

			Game game = Game.find( "byChatroom", room ).first();

			if( game != null )
				project = game.component.project;

		}
		Security.check( Security.getConnected().projects.contains( project ) );// ||
		// !(currentUser.openChats.size()==1)
		// );
		if( !currentUser.openChats.contains( room ) )
		{
			// System.out.println( "Adding " + room.id + " to " + currentUser.id
			// );
			currentUser.openChats.add( room );
			currentUser.refresh();
			currentUser.save();
			new Message( "notice", currentUser.name + " has entered the chat", room ).save();
		}
	}

	/**
	 * Signs out the currently connected user from the chatroom
	 * 
	 * @author Amr Hany
	 * @param id
	 *            , Room ID
	 */
	public static void leaveChat( long id )
	{
		ChatRoom room = ChatRoom.findById( id );
		Project project = Project.find( "byChatroomAndDeleted", room, false ).first();
		if( project == null )
		{
			Game game = Game.find( "byChatroom", room ).first();
			project = game.component.project;

		}
		Security.check( Security.getConnected().projects.contains( project ) );
		User currentUser = Security.getConnected();
		new Message( "notice", currentUser.name + " has left the chat", room ).save();
		currentUser.openChats.remove( room );
		currentUser.save();
		boolean empty = true;
		for( User U : project.users )
		{
			if( U.openChats.contains( room ) )
			{
				empty = false;
			}
		}
		if( empty )
		{
			List<Message> toBeDeleted = Message.find( "byRoom", room ).fetch();
			for( Message m : toBeDeleted )
			{
				m.delete();
			}
		}

	}

	/**
	 * Renders the room in a stand-alone view.
	 * 
	 * @author Amr Hany
	 * @param id
	 *            ,Room ID
	 */

	public static void viewRoom( long id )
	{
		ChatRoom room = ChatRoom.findById( id );

		if( room.deleted )
			notFound();
		Project project = Project.find( "byChatroomAndDeleted", room, false ).first();
		if( project == null )
		{
			Game game = Game.find( "byChatroom", room ).first();
			if( game != null )
				project = game.component.project;

		}

		Security.check( Security.getConnected().projects.contains( project ) );
		User currentUser = Security.getConnected();

		render( room, currentUser );
	}

	/**
	 * Fetches all the messages from a certain chatroom that were sent after the
	 * user's last login
	 * 
	 * @author Amr Hany
	 * @param userId
	 *            , User ID
	 * @param roomId
	 *            , Room ID
	 */
	public static void retrieveSinceLastLogin( long userId, long roomId )
	{
		User user = User.findById( userId );
		Message lastLogMessage = Message.find( "author like ?1 and message like ?2 and room.id = ?3 order by stamp desc", "notice", user.name + " has entered the chat", roomId ).first();
		List<Message> messages;
		if( lastLogMessage != null )
		{
			Long lastLogIn = lastLogMessage.stamp;
			messages = Message.find( "room.id = ?1 and stamp >= ?2 order by stamp", roomId, lastLogIn ).fetch();
		}
		else
		{
			messages = Message.find( "room.id = ?1 order by stamp", roomId ).fetch();
		}
		for( Message m : messages )
		{
			m.room = null;
		}
		renderJSON( messages );
	}
}
