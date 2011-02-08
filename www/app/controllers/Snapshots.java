package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import models.Board;
import models.BoardColumn;
import models.Component;
import models.Log;
import models.Meeting;
import models.Project;
import models.Snapshot;
import models.Sprint;
import models.Task;
import models.User;
import models.Component.ComponentRowh;

/**
 * Represents the Snapshot Entity in the Database and it's relations with other
 * entities.
 * 
 * @see models.Snapshot
 */
public class Snapshots extends SmartController {

	/**
	 * Takes a snap shot of the board.
	 * 
	 * @param sprintID
	 *            The sprint id.
	 * @param componentID
	 *            The component id.
	 * @param meetingID
	 *            The meeting id.
	 * @return void
	 */
	public static void TakeSnapshot(long sprintID, long componentID,
			long meetingID) {
		Sprint s = Sprint.findById(sprintID);
		Project p = s.project;
		Board b;
		List<BoardColumn> columns;
		User user = Security.getConnected();
		if (componentID == 0)
			b = p.board;
		else {
			Component c = Component.findById(componentID);
			b = c.board;
		}
		List<BoardColumn> CS = new ArrayList<BoardColumn>();
		columns = b.columns;
		ArrayList<ComponentRowh> data = new ArrayList<ComponentRowh>();
		ArrayList<String> Columnsofsnapshot = new ArrayList<String>();
		for (int i = 0; i < columns.size(); i++) {
			if (columns.get(i).on_board == true) {
				CS.add(null);
				CS.set(i, columns.get(i));
			}
		}
		for (int i = 0; i < columns.size(); i++) {
			if (columns.get(i).on_board == true) {
				CS.set(columns.get(i).sequence, columns.get(i));
			}
		}
		for (int i = 0; i < CS.size(); i++) {
			Columnsofsnapshot.add(null);
			Columnsofsnapshot.set(i, CS.get(i).name);
		}
		if (componentID == 0) {
			List<Component> components = p.getComponents();
			String type = p.name;
			for (int i = 0; i < components.size(); i++)// for each component get
														// the tasks
			{
				data.add(null);
				if (components.get(i).number != 0) {
					data.add(null);
					data.set(i, new ComponentRowh(components.get(i).id,
							components.get(i).name));
					List<Task> tasks = components.get(i).comp_sprint_not_parent_tasks(s);
					for (int j = 0; j < CS.size(); j++) {
						data.get(i).add(null);
						data.get(i).set(j, new ArrayList<String>());
					}
					for (Task task : tasks) {
						BoardColumn pcol = new BoardColumn();
						for (int k = 0; k < task.status.columns.size(); k++) {
							pcol = task.status.columns.get(k);
							if (pcol.board.id == b.id)
								break;
						}
						if (pcol.on_board == true && !pcol.deleted)
							data.get(i).get(pcol.sequence).add(
									"(" + "T" + task.id + "-"
											+ task.description + "-"
											+ task.assignee.name);
					}
				}
			}
			Snapshot snap = new Snapshot();
			snap.user = user;
			snap.type = type;
			snap.board = b;
			snap.sprint = s;
			snap.data = data;
			snap.Columnsofsnapshot = Columnsofsnapshot;
			snap.save();
			if (meetingID != 0) {
				Meeting M = Meeting.findById(meetingID);
				M.snapshot = snap;
				M.save();
				snap.type = M.name + " Meeting";
				snap.save();
				Calendar cal = new GregorianCalendar();

				// Logs.addLog(user, "Took", "Snapshot", snap.id, p, cal.getTime());	
				Log.addUserLog("Took snapshot", snap, p, b, s);
			}
		} else {
			Component c = Component.findById(componentID);
			String type;
			if (c.number == 0) {
 type=c.project.name;
			} else {
				type = c.name;
			}
			List<User> users = c.get_users();
			for (int i = 0; i < users.size(); i++) {
				data.add(null);
				data.set(i, new ComponentRowh(users.get(i).id,
						users.get(i).name));
				List<Task> tasks = users.get(i).returnUserTasks(s, componentID);
				for (int j = 0; j < CS.size(); j++) {
					data.get(i).add(null);
					data.get(i).set(j, new ArrayList<String>());
				}
				for (Task task : tasks) {
					BoardColumn pcol = new BoardColumn();
					for (int k = 0; k < task.status.columns.size(); k++) {
						pcol = task.status.columns.get(k);
						if (pcol.board.id == b.id)
							break;
					}
					data.get(i).get(CS.indexOf(pcol)).add(
							"T" + task.id + "-" + task.description + "-"
									+ task.assignee.name);
				}
			}
			Snapshot snap = new Snapshot();
			snap.user = user;
			snap.type = type;
			snap.board = b;
			snap.sprint = s;
			snap.data = data;
			snap.Columnsofsnapshot = Columnsofsnapshot;
			snap.save();
			if (meetingID != 0) {
				Meeting M = Meeting.findById(meetingID);
				M.snapshot = snap;
				M.save();
				snap.type = M.name + " Meeting";
				snap.save();
				Calendar cal = new GregorianCalendar();

				// Logs.addLog(user, "Took", "Snapshot", snap.id, p, cal.getTime());
				Log.addUserLog("Took snapshot", snap, b, s, p);
			}
		}
	}

	/**
	 * Renders the data needed to load the snapshot.
	 * 
	 * @author Amr Abdelwahab
	 * @param id
	 *            The snap shot id
	 * @return void
	 */

	public static void LoadSnapShot(long id) {
		Snapshot snap = Snapshot.findById(id);
		ArrayList<String> Columnsofsnapshot = snap.Columnsofsnapshot;
		ArrayList<ComponentRowh> data = snap.data;
		long cid = 0;
		if (snap.board.component != null)
			cid = snap.board.component.id;
		render(Columnsofsnapshot, data, snap, cid);

	}

	/**
	 * Renders a list of the snapshots now sorted by date
	 * 
	 * @author Amr Abdelwahab
	 * @param id
	 *            The id of the sprint.
	 * @param type
	 *            The string that filters the list of board according to its
	 *            type.
	 * @return void
	 */
	public static void index(long sid, long pid, long cid) {

		Sprint s = Sprint.findById(sid);
		List<Snapshot> snapshots = new ArrayList();
		if (pid != 0) {
			Project p = Project.findById(pid);
			snapshots = Snapshot.find("byBoardAndSprintAndType", p.board, s,
					p.name).fetch();
			render(snapshots, p, pid, cid);
		} else {
			Component c = Component.findById(cid);
				if(c.number!=0){
			snapshots = Snapshot.find("byBoardAndSprintAndType",
					c.board, s, c.name).fetch();}
				else{
					snapshots = Snapshot.find("byBoardAndSprintAndType",
							c.board, s, c.project.name).fetch();
					}
			
			render(snapshots, c, pid, cid);
		}
		// List<Snapshot> snapshots =
		// Snapshot.find("sprint.id = ? and type = ? ", id, type).fetch();
		// if(snapshots.size()!=0){Sprint s=snapshots.get(0).sprint;
		// render(snapshots,type,s);}
		// render(snapshots,type);
	}

	/**
	 * Renders a list of the snapshots of the board given a project or a
	 * component given a specific sprint.
	 * 
	 * @param sid
	 *            The sprint id.
	 * @param pid
	 *            The project id.
	 * @param cid
	 *            The component id.
	 * @return void
	 */
	public static void boardsnapshots(long sid, long pid, long cid) {

		Sprint s = Sprint.findById(sid);
		List<Snapshot> snapshots = new ArrayList();
		if (pid != 0) {
			Project p = Project.findById(pid);
			snapshots = Snapshot.find("byBoardAndSprintAndType", p.board, s,
					p.name).fetch();
			render(snapshots, p, pid, cid);
		} else {
			Component c = Component.findById(cid);
			if(c.number!=0){
				snapshots = Snapshot.find("byBoardAndSprintAndType",
						c.board, s, c.name).fetch();}
					else{
						snapshots = Snapshot.find("byBoardAndSprintAndType",
								c.board, s, c.project.name).fetch();
						}
			render(snapshots, c, pid, cid);
		}
	}

	/**
	 * Filters the snapshots that have been taken by the connected user.
	 * 
	 * @param id
	 *            The sprint id.
	 * @param type
	 *            The Type of the snapshot like used in filtering and giving
	 *            titles.
	 * @return void
	 */
	public static void indexuser(long id, String type) {

		List<Snapshot> snapshots = Snapshot.find(
				"sprint.id = ? and type = ? and user=? ", id, type,
				Security.getConnected()).fetch();

		render(snapshots, type);

	}

}
