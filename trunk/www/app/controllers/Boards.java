package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

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
	public static void loadBoard1(long sprintID,long componentID)
	{
		Sprint s = Sprint.findById(sprintID);
		Project p = s.project;
		Board b = p.board;
		List<Component> components = p.getComponents();
		ArrayList<ComponentRow> data = new ArrayList<ComponentRow>();
		List<Column> columns = b.columns;
		Component c = Component.findById(componentID);
		List<User> users = c.getUsers();
		List<Column> columnsOfBoard=new ArrayList<Column>();
		List<Column> hidencolumnsOfBoard=new ArrayList<Column>();
		for( int i=0; i<columns.size();i++)
		{
			if(columns.get( i ).onBoard==true)
			columnsOfBoard.add( columns.get( i ) );
		}
		columnsOfBoard = orderColumns(columnsOfBoard);
		for( int i=0; i<columns.size();i++)
		{
			if(columns.get( i ).onBoard!=true)
				hidencolumnsOfBoard.add( columns.get( i ) );
		}
		
		for (int i = 0; i < components.size(); i++) {
			data.add(null);
			List<Task> tasks=new ArrayList<Task>();
			if(componentID==-1)
			{
				data.set(i, new ComponentRow(components.get(i).id, components.get(i).name));
			    tasks = components.get(i).returnComponentTasks(s);
			}
			else
			{
				data.set(i, new ComponentRow(users.get(i).id, users.get(i).name));
			    tasks = users.get(i).returnUserTasks(s, componentID);
			}
			for (int j = 0; j < columnsOfBoard.size(); j++) {
				data.get(i).add(null);
				data.get(i).set(j, new ArrayList<Task>());
			}
			for (Task task : tasks) {
				data.get(i).get(columns.indexOf(task.taskStatus.column)).add(task);
			}
		}
	
		long id=Security.getConnected().id;
		boolean found=false;
		LinkedList<Meeting> total = new LinkedList<Meeting>();
		
		if(componentID==-1)
		{
			//companyMetting
		}
		else
		{
			//componentMeeting
		}
	
		//render(data, columnsOfBoard,hidencolumnsOfBoard, u, b, s, p,columns, total,ud,ua,ur,ut,componentID);
		
	}
	
	/**
	 * Renders the data and the columnsOfBoard to be used by the views. this
	 * method is used to fill the 2D array attribute in the board with the data
	 * that should be represented in each column of the project board
	 * (components and their tasks and stories for a specific sprint)
	 * 
	 * @author Hadeer_Diwan
	 * @param SprintID
	 *            the sprint id
	 */

	public static void loadBoard(long sprintID) {
		Sprint s = Sprint.findById(sprintID);
		Project p = s.project;
		Board b = p.board;
		List<Component> components = p.getComponents();
		ArrayList<ComponentRow> data = new ArrayList<ComponentRow>();
		List<Column> columns = b.columns;
		
		List<Column> columnsOfBoard=new ArrayList<Column>();
		List<Column> hidencolumnsOfBoard=new ArrayList<Column>();
		for( int i=0; i<columns.size();i++)
		{
			if(columns.get( i ).onBoard==true)
			{
				columnsOfBoard.add( columns.get( i ) );
			}
		}
		columnsOfBoard = orderColumns(columnsOfBoard);
		for( int i=0; i<columns.size();i++)
		{
			if(columns.get( i ).onBoard!=true)
			{
				hidencolumnsOfBoard.add( columns.get( i ) );
			}
		}
		for (int i = 0; i < components.size(); i++) {
			data.add(null);
			data.set(i, new ComponentRow(components.get(i).id, components.get(i).name));
			List<Task> tasks = components.get(i).returnComponentTasks(s);

			for (int j = 0; j < columnsOfBoard.size(); j++) {
				data.get(i).add(null);
				data.get(i).set(j, new ArrayList<Task>());
			}
			for (Task task : tasks) {
				if(task.taskStatus.column.onBoard==true)
				{
				data.get(i).get(columnsOfBoard.indexOf(task.taskStatus.column)).add(task);
				//System.out.println(task.description);
				//System.out.print( columns. );
				}
			}
		}

		/**
		 * * @author asmaak89
		 * 
		 * @param meetings
		 *            list of project p is used code:- linkedList to save the
		 *            meetings that it's time is in current time and then add to
		 *            another Linkedlist users of all meetings running it render
		 *            this list of Users in views-Boards- loadBoard.html
		 */

		long id=Security.getConnected().id;
		boolean found=false;
		LinkedList<Meeting> total = new LinkedList<Meeting>();		
		for (Meeting m : p.meetings) {
			long now = new Date().getTime();
			if (m.startTime < now && m.endTime > now) {
				for(int i=0;i<m.users.size();i++)
				{
					if(m.users.get(i).id==id)
						if(m.users.get(i).checkConfirmed())
						total.add(m);
				       found=true;
				}
				
			}
		}


		ArrayList<ArrayList<User>> u = new ArrayList<ArrayList<User>>();
		if(found==true)
		{
		for (int i = 0; i < total.size(); i++) {
			Meeting m = Meeting.findById(total.get(i).id);

			u.add(new MeetingUsers(m));
			for (MeetingAttendance k : m.users) {
				if (k.status.equals("confirmed")) {
					u.get(i).add(k.user);
				}

			}

		}
		}
				List <User> usrs = p.users;
		List <User> users_desc = new ArrayList();
		List <User> users_assignee = new ArrayList();
		List <User> users_reviewer = new ArrayList();
		List <User> users_type = new ArrayList();
		for(int i=0;i<usrs.size();i++)
		{
				if(usrs.get(i).in(p).can("changeTaskDescreption"))
					users_desc.add(usrs.get(i));
				if(usrs.get(i).in(p).can("changeAssignee"))
					users_assignee.add(usrs.get(i));
				if(usrs.get(i).in(p).can("changeReviewer"))
					users_reviewer.add(usrs.get(i));
				if(usrs.get(i).in(p).can("changeTaskType"))
					users_type.add(usrs.get(i));

		}
		
		ArrayList ud = new ArrayList (users_desc.size());
		for(int i=0;i<users_desc.size();i++)
		{
			User uu = users_desc.get(i);
			ud.add(uu.id);
		}
		
		ArrayList ua = new ArrayList (users_assignee.size());
		for(int i=0;i<users_assignee.size();i++)
		{
			User uu = users_assignee.get(i);
			ua.add(uu.id);
		}
		
		ArrayList ur = new ArrayList (users_reviewer.size());
		for(int i=0;i<users_reviewer.size();i++)
		{
			User uu = users_reviewer.get(i);
			ur.add(uu.id);
		}
		
		ArrayList ut = new ArrayList (users_type.size());
		for(int i=0;i<users_type.size();i++)
		{
			User uu = users_type.get(i);
			ut.add(uu.id);
		}
		render(data, columnsOfBoard,hidencolumnsOfBoard, u, b, s, p, total,columns,ud,ua,ur,ut);
		
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
	 * Renders the data and the cols to be used by the views. this method is
	 * used to fill the 2D array attribute in the board with the data that
	 * should be represented in each column of the component board (users and
	 * their tasks and stories for a specific sprint)
	 * 
	 * @author Hadeer_Diwan
	 * @param sprintID
	 *            the sprint id
	 * @param componentID
	 *            the component id
	 */
	public static void loadComponentBoard(long sprintID, long componentID) {
		Sprint s = Sprint.findById(sprintID);
		Project p = s.project;
		Board b = p.board;

		Component c = Component.findById(componentID);
		List<User> users = c.getUsers();
		ArrayList<User> u = new ArrayList<User>();
		ArrayList<ComponentRow> data = new ArrayList<ComponentRow>();
		List<Column> columns = b.columns;
		
		List<Column> columnsOfBoard=new ArrayList<Column>();
		List<Column> hidencolumnsOfBoard=new ArrayList<Column>();
		for( int i=0; i<columns.size();i++)
		{
			if(columns.get( i ).onBoard==true)
			{
				columnsOfBoard.add( columns.get( i ) );
			}
		}
		columnsOfBoard = orderColumns(columnsOfBoard);
		for( int i=0; i<columns.size();i++)
		{
			if(columns.get( i ).onBoard!=true)
			{
				hidencolumnsOfBoard.add( columns.get( i ) );
			}
		}
		for (int i = 0; i < users.size(); i++) {
			data.add(null);
			data.set(i, new ComponentRow(users.get(i).id, users.get(i).name));
			List<Task> tasks = users.get(i).returnUserTasks(s, componentID);

			for (int j = 0; j < columnsOfBoard.size(); j++) {
				data.get(i).add(null);
				data.get(i).set(j, new ArrayList<Task>());
			}

			for (Task task : tasks) {
				if(task.taskStatus.column.onBoard==true)
				{
				data.get(i).get(columnsOfBoard.indexOf(task.taskStatus.column)).add(task);
				//System.out.println(task.description);
				//System.out.print( columns. );
				}
			}

		}

		/**
		 * next lines indicates how current Component meeting is held infront of
		 * each component board . as it user Component c and it's list of Users
		 * to search for which meeting running now and which User have status
		 * Confirmed.
		 * 
		 * @param Component
		 *            c (Used thought code lines), List of componentMeetings,
		 *            List of Users for this Component .
		 * @author asmaak89
		 */
     
		long id=Security.getConnected().id;
		boolean found =false;
		LinkedList<Meeting> total = new LinkedList<Meeting>();

		for (Meeting m : c.componentMeetings) {
			long now = new Date().getTime();
			if (m.startTime < now && m.endTime > now) 
			{
                for(int i=0;i<m.users.size();i++)
                {
                	if(m.users.get(i).id==id)
                	{
                		if(m.users.get(i).checkConfirmed())
                		{
                		total.add(m);
                		found=true;
                		}
                	}
                }
				

			}
		}
		for (int i = 0; i < total.size(); i++) {
			Meeting m = Meeting.findById(total.get(i).id);

			for (MeetingAttendance k : m.users) {
				if (k.status.equals("confirmed")) {
					u.add(k.user);
				}
			}

		}
		render(data, columnsOfBoard,hidencolumnsOfBoard, u,b, s, c, p, total);
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
		Logs.addLog(u, "shown", "Column", cid, c.board.project, cal.getTime());
		String message = u.name+" "+ "has shown " + c.name;
		Notifications.notifyUsers(c.board.project, "Show Column", message, "showColumn", (byte) 0);
		
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
		System.out.println(cid);
		Column c=Column.findById( cid );
		System.out.println(c.name);
		c.onBoard=false;
		c.sequence=-1;
		c.save();
		Calendar cal = new GregorianCalendar();
		User u = User.findById(uid);
		Logs.addLog(u, "hided", "Column", c.id, c.board.project, cal.getTime());
		String message = u.name+" " + "has hided " + c.name;
		Notifications.notifyUsers(c.board.project, "Hide Column", message, "hideColumn", (byte) 0);
		
		
	}
	
	/**
	 * @author Dina Helal
	 * @param usr_id: user ID
	 * 
	 * */
	
	public static void getUser(long usr_id)
	{
		User usr = User.findById(usr_id);
		renderJSON(usr);
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