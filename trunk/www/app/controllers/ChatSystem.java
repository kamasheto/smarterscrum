package controllers;

import java.util.List;

import models.ChatRoom;
import models.Message;
import models.User;

public class ChatSystem extends SmartController
{

	/**
	 * Add message method that adds a new message being sent to the system in
	 * order to make others retrieve it.
	 * 
	 * @author Amr Hany
	 * @param message
	 * @param id
	 *            : which is the room ID
	 */
	public static void addMessage( String message, long id )
	{
		ChatRoom room = ChatRoom.findById( id );
		if(room.deleted)
			notFound();
		Security.check( Security.getConnected().projects.contains( room.project ) );
		new Message( Security.getConnected().name, message, room ).save();
	}

	/**
	 * This method is the core of the Chat system as it is used in order to
	 * retrieve the new messages and render them back to the view in order to
	 * display them.
	 * 
	 * @author Amr Hany
	 * @param message
	 * @param id
	 *            : which is the room ID
	 */
	public static void newMessages( long id )
	{
		ChatRoom room = ChatRoom.findById( id );
		if(room.deleted)
			notFound();
		Security.check( Security.getConnected().projects.contains( room.project ) );
		List<Message> messages = Message.find( "room = ?1 and stamp > ?2", room, request.date.getTime() ).fetch();
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
	 * This method is called when a User enter the chat room
	 * 
	 * @author Amr Hany
	 * @param message
	 * @param id
	 *            which is the room ID
	 */
	public static void enterChat( long id )
	{
		ChatRoom room = ChatRoom.findById( id );
		if(room.deleted)
			notFound();
		User currentUser = Security.getConnected();
		Security.check( Security.getConnected().projects.contains( room.project ) );// ||
		// !(currentUser.openChats.size()==1)
		// );
		if( !currentUser.openChats.contains( room ) )
		{
			currentUser.openChats.add( room );
			currentUser.save();
		}
		new Message( "notice", currentUser.name + " has entered the chat", room ).save();
	}

	/**
	 * This method is called when a User leave the chat room of a project
	 * 
	 * @author Amr Hany
	 * @param message
	 * @param id
	 *            which is the room ID
	 */
	public static void leaveChat( long id )
	{
		ChatRoom room = ChatRoom.findById( id );
		Security.check( Security.getConnected().projects.contains( room.project ) );
		User currentUser = Security.getConnected();
		new Message( "notice", currentUser.name + " has left the chat", room ).save();
		currentUser.openChats.remove( room );
		currentUser.save();
	}

	/**
	 * Method to render the room to in stand alone view.
	 * 
	 * @author Amr Hany
	 * @param id
	 *            :room id
	 */

	public static void viewRoom( long id )
	{
		ChatRoom room = ChatRoom.findById( id );
		if(room.deleted)
			notFound();
		Security.check( Security.getConnected().projects.contains( room.project ) );
		User currentUser = Security.getConnected();
		render( room, currentUser );
	}

	/**
	 * retrieveSinceLastLogin method that gets the messages that are on the DB
	 * for a specific room after a specific user signed in
	 * 
	 * @author Amr Hany
	 * @param userId
	 * @param roomId
	 */
	public static void retrieveSinceLastLogin( long userId, long roomId )
	{
		User user = User.findById( userId );
		Message lastLogMessage = Message.find( "author like ?1 and message like ?2 and room.id = ?3 order by stamp desc", "notice", user.name + " has entered the chat", roomId ).first();
		Long lastLogIn = lastLogMessage.stamp;
		List<Message> messages = Message.find( "room.id = ?1 and stamp >= ?2 order by stamp", roomId, lastLogIn ).fetch();
		for( Message m : messages )
		{
			m.room = null;
		}
		renderJSON( messages );
	}
}
