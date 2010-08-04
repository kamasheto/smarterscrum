package controllers;

public class Loading extends SmartController {
	public static void loading() {
		render("Application/loading.html");
	}

	/**
	 * This action handles all drops caused by dragging and dropping items on
	 * workspaces.
	 * 
	 * @author mahmoudsakr
	 */
	public static void dynamicDrop(String from, String to) {
		String[] arr = from.split("-"); // meeting-1
		String[] arr2 = to.split("-"); // user-3

		from = arr[0].toLowerCase();
		to = arr2[0].toLowerCase();
		long id = Long.parseLong(arr[1]), id2 = Long.parseLong(arr2[1]);

		if (from.equals("user") && to.equals("component")) {
			// inviting user id to component id2
			Users.chooseUsers(id2, id);
		} else if (from.equals("task") && to.equals("component")) {
			// associating task to component
			Tasks.associateToComponent(id, id2);
		} else if (from.equals("task") && to.equals("meeting")) {
			// associate task to meeting
			Meetings.addTask(id2, id);
		} else if (from.equals("user") && to.equals("meeting")) {
			// inviting user to a meeting (Amr Hany)
			Meetings.inviteUser(id2, id);
		} else if (from.equals("component") && to.equals("meeting")) {
			// inviting component to a meeting (Amr Hany)
			Meetings.inviteComponent(id2, id);
		}else if( from.equals("task") && to.equals( "user" ) )
		{
			Tasks.assignTaskAssignee(id,id2);
		}
		else if  (from.equals("user") && to.equals( "task" ) )
		{
			Tasks.assignTaskReviewer(id2, id);
		}
		
	}
}