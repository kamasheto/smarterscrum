package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * Chat room Model ,done to make chatroom to each project
 * 
 * @author Amr Hany
 */
@Entity
public class ChatRoom extends SmartModel
{

	/***
	 * a flag that determines whether the chatroom is deleted or not
	 */
	public boolean deleted;

	/***
	 * each chatroom can have many chat while each chat belongs only to one room
	 */
	@OneToMany( mappedBy = "room" )
	public List<Message> chats;

	/***
	 * Project chat room constructor that associates to the room a list of chat
	 * messages
	 * 
	 * @param p
	 *            project that contains the chat room
	 */
	public ChatRoom()
	{
		chats = new ArrayList<Message>();
	}

}
