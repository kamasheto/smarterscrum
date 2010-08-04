package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import models.Board;
import models.Column;
import models.Component;
import models.Meeting;
import models.Project;
import models.Snapshot;
import models.Sprint;
import models.Task;
import models.User;
import models.Component.ComponentRowh;

public class Snapshots extends SmartController {
	
	public static void  TakeSnapshot(long sprintID, long componentID, long meetingID){
		Sprint s = Sprint.findById(sprintID);
		Project p = s.project;
		Board b ;
		List<Column> columns;
		User user = Security.getConnected();
		
		if(componentID==0){
		b= p.board;
				}
		else{
		Component c = Component.findById(componentID);
		 b = c.componentBoard;
			
		}
		List<Column> CS =new ArrayList<Column>();
		 columns = b.columns;
			
		ArrayList<ComponentRowh> data = new ArrayList<ComponentRowh>();
	
		ArrayList<String> Columnsofsnapshot = new ArrayList<String>();
		for( int i=0; i<columns.size();i++)
		{
			if(columns.get( i ).onBoard==true)
			{CS.add( null );
				CS.set(i, columns.get( i ) );
			}
		}for( int i=0; i<columns.size();i++)
		{
			if(columns.get( i ).onBoard==true)
			{
				CS.set(columns.get(i).sequence, columns.get( i ) );
			}
		}
		for (int i = 0; i < CS.size(); i++) {
			Columnsofsnapshot.add(null);
			Columnsofsnapshot.set(i, CS.get(i).name);
		}

//		int smallest;
//		Column temp;
//		for (int i = 0; i < CS.size(); i++) {
//			smallest = i;
//			System.out.println(CS.get(i).name);
//			System.out.println(CS.get(i).sequence);
//			for (int j = i + 1; j < CS.size(); j++) {
//				if (CS.get(smallest).sequence > CS.get(j).sequence) {
//					smallest = j;
//
//				}
//
//			}
//			temp = CS.get(smallest);
//			CS.set(smallest, columns.get(i));
//			CS.set(i, temp);
//			Columnsofsnapshot.set(smallest, CS.get(i).name);
//			Columnsofsnapshot.set(i, temp.name);}
//		}for (int i = 0; i < CS.size(); i++) {
//		System.out.println(CS.get(i).name);
//		System.out.println(CS.get(i).sequence);}
if(componentID==0){
	List<Component> components = p.getComponents();
	String type = p.name;
	for (int i = 0; i < components.size(); i++)// for each component get
		// the tasks
		{
data.add(null);if(components.get(i).number!=0){
			data.add(null);
			data.set(i, new ComponentRowh(components.get(i).id, components.get(i).name));
			List<Task> tasks = components.get(i).returnComponentTasks(s);

			for (int j = 0; j < CS.size(); j++) {
				data.get(i).add(null);
				data.get(i).set(j, new ArrayList<String>());
			}

			for (Task task : tasks) {
				Column pcol = new Column();
				for(int k=0;k<task.taskStatus.columns.size();k++)
				{
					pcol = task.taskStatus.columns.get(k);
					if(pcol.board.id==b.id)
					{
						break;
					}
				}
				if(pcol.onBoard==true&&!pcol.deleted)
				{
				data.get(i).get(pcol.sequence).add("(" + "T" + task.id + "-" + task.description + "-" + task.assignee.name);
				}
			}
		}}	
	
	Snapshot snap = new Snapshot();
	snap.user = user;
	snap.type = type;
	snap.board = b;
	snap.sprint = s;
	snap.data = data;
	snap.Columnsofsnapshot = Columnsofsnapshot;
	snap.save();
	if(meetingID!=0){
		Meeting M=Meeting.findById(meetingID);
		M.snapshot=snap;
		M.save();
		snap.type =M.name+ " Meeting";
		snap.save();
		Calendar cal = new GregorianCalendar();
		Logs.addLog(user, "Took", "Snapshot", snap.id, p, cal.getTime());
		
	}
//	else{
//	Calendar cal = new GregorianCalendar();
//	Logs.addLog(user, "Took", "Snapshot", snap.id, p, cal.getTime());		}
	

}

else{
Component c = Component.findById(componentID);
String type = c.name;
List<User> users = c.getUsers();
	for (int i = 0; i < users.size(); i++)
	{
		data.add(null);
		data.set(i, new ComponentRowh(users.get(i).id, users.get(i).name));
		List<Task> tasks = users.get(i).returnUserTasks(s, componentID);

		for (int j = 0; j < CS.size(); j++) {
			data.get(i).add(null);
			data.get(i).set(j, new ArrayList<String>());
		}

		for (Task task : tasks) {
			Column pcol = new Column();
			for(int k=0;k<task.taskStatus.columns.size();k++)
			{
				pcol = task.taskStatus.columns.get(k);
				if(pcol.board.id==b.id)
				{
					break;
				}
			}
			if(pcol.onBoard==true&&!pcol.deleted)
			{
			data.get(i).get(CS.indexOf(pcol)).add( "T" + task.id + "-" + task.description + "-" + task.assignee.name);
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
	if(meetingID!=0){
		Meeting M=Meeting.findById(meetingID);
		M.snapshot=snap;
		M.save();
		snap.type =M.name+ " Meeting";
		snap.save();
		Calendar cal = new GregorianCalendar();
		Logs.addLog(user, "Took", "Snapshot", snap.id, p, cal.getTime());
		
	}

	
}

		}


	/**
	 * Renders the data needed to load the snapshot
	 * 
	 * @author Amr Abdelwahab
	 * @param id
	 */

	public static void LoadSnapShot(long id) {
		Snapshot snap = Snapshot.findById(id);
		ArrayList<String> Columnsofsnapshot = snap.Columnsofsnapshot;
		ArrayList<ComponentRowh> data = snap.data;
		long cid=0;
		if(snap.board.component!=null)
			cid=snap.board.component.id;
		render(Columnsofsnapshot, data,snap,cid);

	}

	/**
	 * Renders a list of the snapshots now sorted by date
	 * 
	 * @author Amr Abdelwahab
	 *@param id
	 *            the id of the sprint
	 *@param type
	 *            the string that filters the list of board according to its
	 *            type
	 */
	public static void index(long sid, long pid, long cid) {

		Sprint s = Sprint.findById(sid);
		List<Snapshot> snapshots = new ArrayList();
		if(pid!=0)
		{
			Project p = Project.findById(pid);
			snapshots = Snapshot.find("byBoardAndSprintAndType",p.board,s,p.name).fetch();
			render(snapshots,p,pid,cid);
		}
		else
		{
			Component c = Component.findById(cid);
			snapshots = Snapshot.find("byBoardAndSprintAndType",c.componentBoard,s,c.name).fetch();
			render(snapshots,c,pid,cid);
		}
		//List<Snapshot> snapshots = Snapshot.find("sprint.id = ? and type = ? ", id, type).fetch();
		//if(snapshots.size()!=0){Sprint s=snapshots.get(0).sprint;
		//render(snapshots,type,s);}
		//render(snapshots,type);
	}
	public static void boardsnapshots(long sid, long pid, long cid) {

		Sprint s = Sprint.findById(sid);
		List<Snapshot> snapshots = new ArrayList();
		if(pid!=0)
		{
			Project p = Project.findById(pid);
			snapshots = Snapshot.find("byBoardAndSprintAndType",p.board,s,p.name).fetch();
			render(snapshots,p,pid,cid);
		}
		else
		{
			Component c = Component.findById(cid);
			snapshots = Snapshot.find("byBoardAndSprintAndType",c.componentBoard,s,c.name).fetch();
			render(snapshots,c,pid,cid);
		}
	}

	public static void indexuser(long id, String type) {

		List<Snapshot> snapshots = Snapshot.find("sprint.id = ? and type = ? and user=? ", id, type,Security.getConnected()).fetch();
		
		render(snapshots,type);

	}

}
