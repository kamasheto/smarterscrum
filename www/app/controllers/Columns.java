package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import models.Board;
import models.Column;
import models.Project;
import models.Sprint;
import models.User;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

@With (Secure.class)
public class Columns extends SmartCRUD{
		/**
	 * a method that stores the changes of positions of columns in the database
	 * 
	 * @author Dina_Helal
	 * @param id
	 *            : board id
	 * @param pos1
	 *            : starting position of the column
	 * @param pos2
	 *            : finishing position of column Story 17
	 */

	public static void changeColumnPosition(long id, int pos1, int pos2, long userId) {
		Sprint s = Sprint.findById(id);
		Project p = s.project;
		Security.check(p, "canEditColumnsPositions");
		Board b = p.board;
		if (userId == 0)
		userId = Security.getConnected().id;
		Calendar cal = new GregorianCalendar();
		User u = User.findById(userId);
		List<Column> cols = b.columns;
		Column c1 = Column.find("bySequenceAndBoard", pos1 - 1, b).first();
		Column c2 = Column.find("bySequenceAndBoard", pos2 - 1, b).first();
		Logs.addLog(u, "edit", "Column Position", c1.id, p, cal.getTime());
		String message = u.name + " has changed the position of " + c1.name + " from " + c1.sequence + " to " + c2.sequence;
		Notifications.notifyUsers(p, "Edit Column Position", message, "editColumnPosition", (byte) 0);
		int x = c2.sequence;
		if (c1.sequence < c2.sequence) {
			for (int i = c1.sequence + 1; i <= c2.sequence; i++) {
				Column temp = Column.find("bySequenceAndBoard", i, b).first();
				temp.sequence--;
				temp.save();
			}
			c1.sequence = x;
			c1.save();

		} else {
			for (int i = c1.sequence - 1; i >= c2.sequence; i--) {
				Column temp = Column.find("bySequenceAndBoard", i, b).first();
				temp.sequence++;
				temp.save();
			}
			c1.sequence = x;
			c1.save();

		}

	}
	
	/**
	 * a method that stores the changes of positions of columns in the database
	 * using settings
	 * 
	 * @author josephhajj
	 * @param id
	 *            : Sprint id
	 * @param pos1
	 *            : starting position of the column
	 * @param pos2
	 *            : finishing position of column Story 17
	 */
	public static void changeColumnPosition2(long id, int pos1, int pos2, long user_id) {
		if (user_id == 0) {
			user_id = Security.getConnected().id;
		}

		
		
		Sprint s = Sprint.findById(id);
		Project p = s.project;
		Security.check(p, "editColumnsPositions");
		Board b = p.board;
		Column c1 = Column.find("bySequenceAndBoard", pos1,b).first();
		Column c2 = Column.find("bySequenceAndBoard", pos2,b).first();
		c1.sequence = pos2;
		c2.sequence = pos1;
		c1.save();
		c2.save();
		Calendar cal = new GregorianCalendar();
		User user = User.findById(user_id);
		Logs.addLog(user, "edit", "Column Position", c1.id, p, cal.getTime());
		String message = user.name + " has swapped the position of column " + c1.name + " with " + c2.name;
		Notifications.notifyUsers(p, "swapped Column Position", message, "Column Position", (byte) 0);
	}

	/**
	 * this method saves the new column name in the database
	 * 
	 * @author Dina_Helal
	 * @param id
	 *            : column id
	 * @param name
	 *            : new name
	 */
	public static boolean editColumnName(long id, String name, long userId) {

		Column c = Column.find("byId", id).first();
		String oldname = c.name;
		c.name = name;
		c.save();
		Project p=c.board.project;
		Security.check(p, "renameColumns");
		if (userId == 0)
			userId = Security.getConnected().id;
		Calendar cal = new GregorianCalendar();
		User u = User.findById(userId);
		Logs.addLog(u, "rename", "Column Name", c.id, c.board.project, cal.getTime());
		String message = u.name + " has renamed column " + oldname + " to " + c.name;
		Notifications.notifyUsers(c.board.project, "Rename Column", message, "renameColumn", (byte) 0);
		return true;
	}

	public static void show(String id) {
		forbidden();
	}

	public static void delete(String id) {
		forbidden();
	}

	public static void blank() {
		forbidden();
	}

	public static void create() {
		forbidden();
	}

	public static void save(String id) {
		forbidden();
	}

	public static void list(int page, String search, String searchFields, String orderBy, String order) {
		forbidden();
	}
}