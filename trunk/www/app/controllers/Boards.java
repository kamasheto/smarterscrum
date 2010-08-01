package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import notifiers.Notifications;

import controllers.Security;

import models.Board;
import models.Column;
import models.Component;
import models.Meeting;
import models.MeetingAttendance;
import models.Project;
import models.Sprint;
import models.Task;
import models.User;
import models.Component.ComponentRow;
import others.MeetingUsers;
import play.mvc.With;

@With (Secure.class)
public class Boards extends SmartCRUD {
	

	/**
	 * This method is used to generate the project board 
	 * & the component board for a certain sprint
	 * @author Hadeer Diwan, Dina Helal
	 * @param sprintID: sprint id
	 * @param componentID: component id
	 */

	public static void loadboard1(long sprintID, long componentID)
	{
		Sprint s = Sprint.findById(sprintID);  //sprint
		if(s.deleted)
			notFound();
		Project p = s.project;                 //project
		Board b = p.board;					   //project board
		ArrayList<ComponentRow> data = new ArrayList<ComponentRow>();     //tasks
		List<Column> columns = new ArrayList<Column>();     //columns
		if(componentID==0)
		columns = b.columns;				   //columns of project board
		else
		{
			Component comp = Component.findById(componentID);
			if(comp.deleted)
				notFound();
			columns = comp.componentBoard.columns;  //columns of component board
		}
				
		List<Column> columnsOfBoard=new ArrayList<Column>();       //columns that should appear on the board with boolean onBoard=true
		List<Column> hidencolumnsOfBoard=new ArrayList<Column>();  //columns that shouldn't appear on the board with boolean onBoard=false
		for( int i=0; i<columns.size();i++)
		{
			if(columns.get( i ).onBoard&&!columns.get( i ).deleted)
			{
				columnsOfBoard.add( columns.get( i ) );
			}
		}
		columnsOfBoard = orderColumns(columnsOfBoard);
		for( int i=0; i<columns.size();i++)
		{
			if(!columns.get( i ).onBoard&&!columns.get( i ).deleted)
			{
				hidencolumnsOfBoard.add( columns.get( i ) );
			}
		}
		ArrayList ud = getDescPerm(p);    //list of users with permission to edit task description
		ArrayList ua = getAssiPerm(p);	  //list of users with permission to edit task assignee
		ArrayList ur = getRevPerm(p);     //list of users with permission to edit task reviewer
		ArrayList ut = getTypePerm(p);    //list of users with permission to edit task type
		ArrayList us = getStatusPerm(p);  //list of users with permission to edit task status
		if(componentID==0)
		{
			List<Component> components = p.getComponents();     //component of project p
			for (int i = 0; i < components.size(); i++) {
				data.add(null);
				data.set(i, new ComponentRow(components.get(i).id, components.get(i).name));
				List<Task> tasks = components.get(i).returnComponentTasks(s);

				for (int j = 0; j < columnsOfBoard.size(); j++) {
					data.get(i).add(null);
					data.get(i).set(j, new ArrayList<Task>());
				}
				for (Task task : tasks) {
					Column pcol = new Column();
					for(int k=0;k<task.taskStatus.columns.size();k++)
					{
						pcol = task.taskStatus.columns.get(k);     //task column in project
						if(pcol.board.id==b.id)
						{
							break;
						}
					}
					if(pcol.onBoard&&!pcol.deleted)
					{
					data.get(i).get(columnsOfBoard.indexOf(pcol)).add(task);
					}
				}
			}
			ArrayList<ArrayList<User>> u=Meetingloadboard( p,componentID);     //list of users attending meeting infront of board (not admins)
			ArrayList<ArrayList<User>> uAdmin=MeetingloadboardAdmin( p, componentID);   //list of users attending meeting infront of board (admins)
			render(data, columnsOfBoard,hidencolumnsOfBoard, u, uAdmin, b, s, p,columns,ud,ua,ur,ut,us);
			}
		else
		{
			Component comp = Component.findById(componentID);    //component
			if(comp.deleted)
				notFound();
			List<User> users = comp.getUsers();                  //usres in component
			for (int i = 0; i < users.size(); i++) {
				data.add(null);
				data.set(i, new ComponentRow(users.get(i).id, users.get(i).name));
				List<Task> tasks = users.get(i).returnUserTasks(s, componentID);

				for (int j = 0; j < columnsOfBoard.size(); j++) {
					data.get(i).add(null);
					data.get(i).set(j, new ArrayList<Task>());
				}

				for (Task task : tasks) {
					Column pcmp = new Column();                //task column in component
					for(int k=0;k<task.taskStatus.columns.size();k++)
					{
						pcmp = task.taskStatus.columns.get(k);
						if(pcmp.board.id==comp.componentBoard.id)
						{
							break;
						}
					}
					if(pcmp.onBoard&&!pcmp.deleted)
					{
					data.get(i).get(columnsOfBoard.indexOf(pcmp)).add(task);
					}
					}

			}
			ArrayList<ArrayList<User>> u=Meetingloadboard( p,componentID);    //list of users attending meeting infront of board (not admins)
			ArrayList<ArrayList<User>> uAdmin=MeetingloadboardAdmin( p, componentID);   //list of users attending meeting infront of board (admins)
			render(data, columnsOfBoard,hidencolumnsOfBoard, u, uAdmin,b, s,comp, p,columns,ud,ua,ur,ut,us);
		
		}
	}	
	
	public static void loadMeetings(long pid, long cid, long sid)
	{
		Project p = Project.findById(pid);
		ArrayList ud = getDescPerm(p);    //list of users with permission to edit task description
		ArrayList ua = getAssiPerm(p);	  //list of users with permission to edit task assignee
		ArrayList ur = getRevPerm(p);     //list of users with permission to edit task reviewer
		ArrayList ut = getTypePerm(p);    //list of users with permission to edit task type
		ArrayList us = getStatusPerm(p);  //list of users with permission to edit task status
		if(cid==0)
		{
			ArrayList<ArrayList<User>> u=Meetingloadboard( p,cid);     //list of users attending meeting infront of board (not admins)
			ArrayList<ArrayList<User>> uAdmin=MeetingloadboardAdmin( p, cid);   //list of users attending meeting infront of board (admins)
			render(ud, ua, ur, ut, us, u, uAdmin,cid, sid);
		}
		else
		{
			ArrayList<ArrayList<User>> u=Meetingloadboard( p,cid);    //list of users attending meeting infront of board (not admins)
			ArrayList<ArrayList<User>> uAdmin=MeetingloadboardAdmin( p, cid);   //list of users attending meeting infront of board (admins)
			render(ud, ua, ur, ut, us, u, uAdmin, sid);
		}
	}
	
		/**
		 * order any list of columns and return the ordered list.
		 * 
		 * @author Josephhajj
		 * @param cols
		 */	
		
	public static List<Column> orderColumns(List<Column> cols) {
		int smallest;
		Column temp;
		for (int i = 0; i < cols.size(); i++) {
			smallest = i;
			for (int j = i + 1; j < cols.size(); j++) {
				if (cols.get(smallest).sequence > cols.get(j).sequence) {
					smallest = j;

				}

			}
			temp = cols.get(smallest);
			cols.set(smallest, cols.get(i));
			cols.set(i, temp);
		}
		return cols;
	}

	 /**
	  * This method is used to get a list of list of users attending a meeting that 
	  * the logged in user is an attendee in
	  * @author asmaak89, Dina Helal
	  * @param P: Project
	  * @param cid: Component id
	  */
	
	public static ArrayList<ArrayList<User>> Meetingloadboard(Project p, long cid)
	{
		
			long id=Security.getConnected().id;
			LinkedList<Meeting> total = new LinkedList<Meeting>();		
			ArrayList<ArrayList<User>> u = new ArrayList<ArrayList<User>>();
		
			if(cid==0)
			{for (Meeting m : p.meetings) 
			{
				long now = new Date().getTime();
				if (m.startTime <= now && m.endTime > now) 
				{
					for(int i=0;i<m.users.size();i++)
					{
						if(m.users.get(i).user.id==id)
							if(m.users.get(i).checkConfirmed())
							total.add(m);
					}
					
				}
			}}
			else{
				Component c = Component.findById(cid);
				if(c.deleted)
					notFound();
			for (Meeting m : c.componentMeetings) 
			{
				long now = new Date().getTime();
				if (m.startTime <= now && m.endTime > now) 
				{
	                
					for(int i=0;i<m.users.size();i++)
					{
						if(m.users.get(i).user.id==id)
						{
							if(m.users.get(i).checkConfirmed())
							{
								total.add(m);
							}
						}
					}
			     }
			}}
			for (int i = 0; i < total.size(); i++) 
			{
				Meeting m = Meeting.findById(total.get(i).id);
				u.add(new MeetingUsers(m));
				for (MeetingAttendance k : m.users) 
				{
					if (k.status.equals("confirmed")) 
					{
						u.get(i).add(k.user);
					}
					
				}
			}
			return u;
			
	
	}
	
	/**
	  * This method is used to get a list of list of users attending running meetings for the admin to see
	  * @author asmaak89, Dina Helal
	  * @param P: Project
	  * @param cid: Component id
	  */
	
	
	public static ArrayList<ArrayList<User>> MeetingloadboardAdmin(Project p, long cid)
	{
		long id=Security.getConnected().id;
		LinkedList<Meeting> totalAdmin = new LinkedList<Meeting>();		
		ArrayList<ArrayList<User>> uAdmin = new ArrayList<ArrayList<User>>();
		boolean flag=false;
		if(cid==0){
		for (Meeting m : p.meetings) 
		{
			flag=false;
			long now = new Date().getTime();
			if (m.startTime <= now && m.endTime > now) 
			{
				for(int i=0;i<m.users.size();i++)
				{
					if(m.users.get(i).user.id==id)
						if(m.users.get(i).checkConfirmed())
						flag=true;
				}
				
				
				
				if(Security.getConnected().isAdmin)
				{
					if(!flag)
					totalAdmin.add(m);
				}		
			}
		}
		}
		else
		{
			Component c = Component.findById(cid);
			if(c.deleted)
				notFound();
			for (Meeting m : c.componentMeetings) 
			{
				flag=false;
				long now = new Date().getTime();
				if (m.startTime <= now && m.endTime > now) 
				{
					for(int i=0;i<m.users.size();i++)
					{
						if(m.users.get(i).user.id==id)
							if(m.users.get(i).checkConfirmed())
							flag=true;
					}
					
					
					
					if(Security.getConnected().isAdmin)
					{
						if(!flag)
						totalAdmin.add(m);
					}		
				}
			}
		}
		for (int i = 0; i < totalAdmin.size(); i++) 
		{
			Meeting m = Meeting.findById(totalAdmin.get(i).id);
			uAdmin.add(new MeetingUsers(m));
			for (MeetingAttendance k : m.users) 
			{
				if (k.status.equals("confirmed")) 
				{
					uAdmin.get(i).add(k.user);
				}
				
			}
		}
		return uAdmin;

	}

/**
	 * this method is used to search for a specific column and change the value
	 * of the boolean variable of it called onBoard to true
	 * so as to let this column appear on the board
	 * 
	 * @author Hadeer_Diwan
	 * @param cid
	 *            the component id
	 * @param uid
	 *            the user id
	 */
	
	public static void showHiddenColumn(long cid,long uid)
	{
		Column c=Column.findById( cid );
		if(c.deleted)
			notFound();
		int count=0;
		for(int i=0;i<c.board.columns.size();i++)
		{
			if(c.board.columns.get(i).onBoard==true)
				count++;
		}
		c.sequence=count;
		c.onBoard=true;
		c.save();
		Calendar cal = new GregorianCalendar();
		User u = User.findById(uid);
		if(u.deleted)
			notFound();
		Logs.addLog(u, "shown", "Column", cid, c.board.project, cal.getTime());
		String message = u.name+" "+ "has shown " + c.name;
		Notifications.notifyProjectUsers(c.board.project, "Show Column",message, "addColumn", (byte) 0);
		
	}
	/**
	 * this method is used to search for a specific column and change the value
	 * of the boolean variable of it called onBoard to false
	 * so as to hide this column 
	 * 
	 * @author Hadeer_Diwan
	 * @param cid
	 *            the component id
	 * @param uid
	 *            the user id
	 */
	public static void hideColumn(long cid,long uid)
	{
		Column c=Column.findById( cid );
		if(c.deleted)
			notFound();
		c.onBoard=false;
		c.sequence=-1;
		c.save();
		int count=0;
		for(int i=0;i<c.board.columns.size();i++)
		{
			if(c.board.columns.get(i).onBoard==true)
			{
				c.board.columns.get(i).sequence=count;
				c.board.columns.get(i).save();
				count++;
			}
		}
		Calendar cal = new GregorianCalendar();
		User u = User.findById(uid);
		if(u.deleted)
			notFound();
		Logs.addLog(u, "hided", "Column", c.id, c.board.project, cal.getTime());
		String message = u.name+" " + "has hided " + c.name;
		Notifications.notifyProjectUsers(c.board.project, "Hide Column", message, "deleteColumn", (byte) 0);
		
		
	}
	/**
	 * This method takes project P & retrn a list of users 
	 * who can edit task description in this project
	 * 
	 * @author Dina Helal
	 * @param p 
	 * 			Project p
	 * */
	
	public static void sprintBoards(long sprintID)
	{
		Sprint s = Sprint.findById(sprintID);
		if(s.deleted)
			notFound();
		Project p = s.project;
		List <Board> boards = new ArrayList <Board>();
		for(int i=0;i<p.components.size();i++)
		{
			if(p.components.get(i).componentBoard!=null)
			boards.add(p.components.get(i).componentBoard);
		}
		render(boards,sprintID,p,s);
	}
	
	public static ArrayList getDescPerm (Project p)
	{
		List <User> usrs = p.users;
		List <User> users_desc = new ArrayList();
		for(int i=0;i<usrs.size();i++)
		{
			if(usrs.get(i).in(p).can("changeTaskDescreption"))
				users_desc.add(usrs.get(i));
		}
		ArrayList ud = new ArrayList (users_desc.size());
		for(int i=0;i<users_desc.size();i++)
		{
			User uu = users_desc.get(i);
			ud.add(uu.id);
		}
		return ud;
	}
	
	/**
	 * This method takes project P & retrn a list of users 
	 * who can edit task assignee in this project
	 * 
	 * @author Dina Helal
	 * @param p 
	 * 			Project p
	 * */
	
	public static ArrayList getAssiPerm (Project p)
	{
		List <User> usrs = p.users;
		List <User> users_assignee = new ArrayList();
		for(int i=0;i<usrs.size();i++)
		{
			if(usrs.get(i).in(p).can("changeAssignee"))
				users_assignee.add(usrs.get(i));
		}	
		ArrayList ua = new ArrayList (users_assignee.size());
		for(int i=0;i<users_assignee.size();i++)
		{
			User uu = users_assignee.get(i);
			ua.add(uu.id);
		}
		return ua;
	}
	
	/**
	 * This method takes project P & retrn a list of users 
	 * who can edit task reviewer in this project
	 * 
	 * @author Dina Helal
	 * @param p 
	 * 			Project p
	 * */
	
	public static ArrayList getRevPerm (Project p)
	{
		List <User> usrs = p.users;
		List <User> users_reviewer = new ArrayList();
		for(int i=0;i<usrs.size();i++)
		{
			if(usrs.get(i).in(p).can("changeReviewer"))
				users_reviewer.add(usrs.get(i));
		}
		ArrayList ur = new ArrayList (users_reviewer.size());
		for(int i=0;i<users_reviewer.size();i++)
		{
			User uu = users_reviewer.get(i);
			ur.add(uu.id);
		}
		return ur;
	}
	
	/**
	 * This method takes project P & retrn a list of users 
	 * who can edit task type in this project
	 * 
	 * @author Dina Helal
	 * @param p 
	 * 			Project p
	 * */
	public static ArrayList getTypePerm (Project p) 
	{
		List <User> usrs = p.users;
		List <User> users_type = new ArrayList();
		for(int i=0;i<usrs.size();i++)
		{
				if(usrs.get(i).in(p).can("changeTaskType"))
					users_type.add(usrs.get(i));

		}	
		ArrayList ut = new ArrayList (users_type.size());
		for(int i=0;i<users_type.size();i++)
		{
			User uu = users_type.get(i);
			ut.add(uu.id);
		}
		return ut;
	}
	/**
	 * This method takes project P & retrn a list of users 
	 * who can edit task status in this project
	 * 
	 * @author Dina Helal
	 * @param p 
	 * 			Project p
	 * */
	public static ArrayList getStatusPerm (Project p) 
	{
		List <User> usrs = p.users;
		List <User> users_status = new ArrayList();
		for(int i=0;i<usrs.size();i++)
		{
				if(usrs.get(i).in(p).can("changeTaskStatus"))
					users_status.add(usrs.get(i));

		}	
		ArrayList ut = new ArrayList (users_status.size());
		for(int i=0;i<users_status.size();i++)
		{
			User uu = users_status.get(i);
			ut.add(uu.id);
		}
		return ut;
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