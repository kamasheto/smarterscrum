package controllers;

import java.util.Calendar;
import java.util.GregorianCalendar;

import models.Board;
import models.BoardColumn;
import models.Component;
import models.Log;
import models.Project;
import models.Sprint;
import models.User;
import play.mvc.With;

@With( Secure.class )
public class Columns extends SmartController
{
	/**
	 * Stores the changes the positions of columns
	 * 
	 * @author Dina_Helal
	 * @param id
	 *            , Board ID
	 * @param pos1
	 *            , Starting position of the column
	 * @param pos2
	 *            , Finishing position of column
	 */

	public static void changeColumnPosition( long id, int pos1, int pos2, long userId, long cid )
	{
		Sprint s = Sprint.findById( id );
		Project p = s.project;
		Security.check( p, "EditColumnsPositions" );
		Board b;
		if( cid == 0 )
			b = p.board;
		else
		{
			Component c = Component.findById( cid );
			b = c.board;
		}
		if( userId == 0 )
			userId = Security.getConnected().id;
		Calendar cal = new GregorianCalendar();
		User u = User.findById( userId );		
		BoardColumn c1 = BoardColumn.find( "bySequenceAndBoardAndDeleted", pos1 - 1, b, false ).first();
		BoardColumn c2 = BoardColumn.find( "bySequenceAndBoardAndDeleted", pos2 - 1, b, false ).first();
		// Logs.addLog( u, "edit", "Column Position", c1.id, p, cal.getTime() );
		Log.addLog("Edit column ("+c1.name+") position", u, c1, b, p);
		int x = c2.sequence;
		if( c1.sequence < c2.sequence )
		{
			for( int i = c1.sequence + 1; i <= c2.sequence; i++ )
			{
				BoardColumn temp = BoardColumn.find( "bySequenceAndBoard", i, b ).first();
				temp.sequence--;
				temp.save();
			}
			c1.sequence = x;
			c1.save();

		}
		else
		{
			for( int i = c1.sequence - 1; i >= c2.sequence; i-- )
			{
				BoardColumn temp = BoardColumn.find( "bySequenceAndBoard", i, b ).first();
				temp.sequence++;
				temp.save();
			}
			c1.sequence = x;
			c1.save();

		}

	}

	/**
	 * Stores the changes of positions of columns (using settings)
	 * 
	 * @author josephhajj
	 * @param id
	 *            , Sprint ID
	 * @param pos1
	 *            , Starting position of the column
	 * @param pos2
	 *            , Finishing position of column
	 */
	public static void changeColumnPosition2( long id, int pos1, int pos2, long user_id, long cid )
	{
		if( user_id == 0 )
		{
			user_id = Security.getConnected().id;
		}

		Sprint s = Sprint.findById( id );
		Project p = s.project;
		User user = User.findById( user_id );
		Security.check( user.in( p ).can( "editColumnsPositions" ) );
		Board b;
		if( cid == 0 )
			b = p.board;
		else
		{
			Component c = Component.findById( cid );
			b = c.board;
		}
		BoardColumn c1 = BoardColumn.find( "bySequenceAndBoardAndDeleted", pos1, b, false ).first();
		BoardColumn c2 = BoardColumn.find( "bySequenceAndBoardAndDeleted", pos2, b, false ).first();
		c1.sequence = pos2;
		c2.sequence = pos1;
		c1.save();
		c2.save();
		Calendar cal = new GregorianCalendar();		
		// Logs.addLog( user, "edit", "Column Position", c1.id, p, cal.getTime() );
		Log.addLog(user.name + "edited column ("+c1.name+") position", user, c1, p, b);
	}

	/**
	 * Saves the new column name
	 * 
	 * @author Dina_Helal
	 * @param id
	 *            , Column ID
	 * @param name
	 *            , New name for the column
	 */
	public static boolean editColumnName( long id, String name, long userId )
	{
		BoardColumn c = BoardColumn.find( "byId", id ).first();
		Project p = c.board.project;
		Security.check( p, "renameColumns" );		
		c.name = name;
		c.save();
		if( userId == 0 )
			userId = Security.getConnected().id;
		Calendar cal = new GregorianCalendar();
		User u = User.findById( userId );
		// Logs.addLog( u, "rename", "Column Name", c.id, c.board.project, cal.getTime() );		
		Log.addLog(u.name + " renamed column ("+c.name+") name", c, c.board, c.board.project, u);
		return true;
	}
}
