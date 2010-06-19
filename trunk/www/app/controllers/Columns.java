package controllers;

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
public class Columns extends SmartCRUD {
	@Check ("canEditColumn")
	/**
	 * this method is used by CRUD when we want to edit items in this class
	 * i used it to edit the column name to satisfy my task that says:
	 * As a scrum master i can rename the columns of the board.
	 * @param id  takes the id of the column to be edited
	 */
	public static void save(String id) throws Exception {
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		JPASupport object = type.findById(id);
		validation.valid(object.edit("object", params));
		if (validation.hasErrors()) {
			renderArgs.put("error", Messages.get("crud.hasErrors"));
			try {
				render(request.controller.replace(".", "/") + "/show.html", type, object);
			} catch (TemplateNotFoundException e) {
				render("CRUD/show.html", type, object);
			}
		}
		object.save();
		flash.success(Messages.get("crud.saved", type.modelName, object.getEntityId()));
		if (params.get("_save") != null) {
			redirect(request.controller + ".list");
		}
		redirect(request.controller + ".show", object.getEntityId());
	}

	public static void add() {
		render();
	}

	/**
	 * Renders a message to the user saying that the column name was changed
	 * successfully.
	 * 
	 * @author Hadeer_Diwan
	 * @param name
	 *            the new column name
	 * @param id
	 *            the id of the column
	 */

	public static void changeColumnName(String name, long id) {
		Column cell = Column.findById(id);
		cell.name = name;
		cell.save();
		String message = "Column name is edited successfully and the new name is " + cell.name;
		render(message);
	}

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

	@Check ("canEditColumnsPositions")
	public static void changeColumnPosition(long id, int pos1, int pos2, long userId) {
		System.out.println(id + " " + pos1 + " " + pos2);
		Sprint s = Sprint.findById(id);
		Project p = s.project;
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
	@Check ("canEditColumnsPositions")
	public static void changeColumnPosition2(long id, int pos1, int pos2, long user_id) {
		if (user_id == 0) {
			user_id = Security.getConnected().id;
		}

		Sprint s = Sprint.findById(id);
		Project p = s.project;
		Board b = p.board;
		Column c1 = Column.find("bySequence", pos1).first();
		Column c2 = Column.find("bySequence", pos2).first();
		c1.sequence = pos2;
		c2.sequence = pos1;
		c1.save();
		c2.save();
		Calendar cal = new GregorianCalendar();
		User user = User.findById(user_id);
		Logs.addLog(user, "edit", "Column Position", c1.id, p, cal.getTime());
		String message = user.name + " has swapped the position of column " + c1.name + "with" + c2.name;
		Notifications.notifyUsers(p, "swapped Column Position", message, "Column Position", (byte) 0);
	}

	@Check ("canEditColumnsPositions")
	public static void changeColumnPosition3(long id, int pos1, int pos2) {
		pos1 = pos1 - 1;
		pos2 = pos2 - 1;

		int min = 0;
		int max = 0;
		if (pos1 > pos2) {
			min = pos2;
			max = pos1;
			Column temp = Column.find("bySequence", max).first();
			for (int i = max - 1; i >= min; i--) {
				Column col = Column.find("bySequence", i).first();
				col.sequence = col.sequence + 1;
				col.save();
			}
			System.out.println(temp.sequence);
			temp.sequence = min;
			System.out.println(temp.sequence);
			temp.save();
		} else {
			min = pos1;
			max = pos2;
			Column temp = Column.find("bySequence", min).first();
			for (int i = min + 1; i <= max; i++) {
				Column col = Column.find("bySequence", i).first();
				col.sequence = col.sequence - 1;
				col.save();
			}
			temp.sequence = max;
			temp.save();
		}

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

	@Check ("canRenameColumns")
	public static boolean editColumnName(long id, String name, long userId) {

		Column c = Column.find("byId", id).first();
		String oldname = c.name;
		c.name = name;
		c.save();
		if (userId == 0)
			userId = Security.getConnected().id;
		Calendar cal = new GregorianCalendar();
		User u = User.findById(userId);
		Logs.addLog(u, "rename", "Column Name", c.id, c.board.project, cal.getTime());
		String message = u.name + " has renamed column " + oldname + " to " + c.name;
		Notifications.notifyUsers(c.board.project, "Rename Column", message, "renameColumn", (byte) 0);
		return true;
	}

}
