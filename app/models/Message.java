package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

/**
 * Message Entity, Used to save messages in the DB in order to show them in the
 * chat System
 * 
 * @author Amr Hany
 */
@Entity
public class Message extends SmartModel
{

	/**
	 * Message deleted marker
	 * 
	 * @deprecated
	 */
	public boolean deleted;

	/**
	 * timestamp of this message
	 */
	public long stamp;

	/**
	 * author of this message, name is hardcoded here to reduce overhead
	 */
	public String author;

	/**
	 * Body of this message
	 */
	@Lob
	public String message;

	/**
	 * Room of this message (a room may have many messages, a message has only
	 * one room)
	 */
	@ManyToOne
	public ChatRoom room;

	/**
	 * Default constructor
	 * 
	 * @param authr
	 *            Author of this message
	 * @param mssage
	 *            Body of this message
	 * @param r
	 *            Chatroom of this message
	 */
	public Message( String authr, String mssage, ChatRoom r )
	{
		this.room = r;
		this.author = authr;
		this.message = mssage;
		this.stamp = new Date().getTime();
	}

	/**
	 * Default constructor
	 * 
	 * @param authr
	 *            Author of this message
	 * @param mssage
	 *            Body of this message
	 * @param r
	 *            Chatroom of this message
	 * @param stmp
	 *            time stamp to be sent
	 */
	public Message( String authr, String mssage, ChatRoom r, long stmp )
	{
		this.room = r;
		this.author = authr;
		this.message = mssage;
		this.stamp = stmp;
	}
}