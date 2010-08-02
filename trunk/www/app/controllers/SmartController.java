package controllers;

	import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import models.Board;
import models.Column;
import models.Component;
import models.Project;
import models.Snapshot;
import models.Sprint;
import models.Task;
import models.User;
import models.Component.ComponentRowh;
import play.mvc.Before;
import play.mvc.Controller;

public class SmartController extends Controller {
	@Before
	public static void beforeActions() throws Throwable {
		renderArgs.put("connected", Security.getConnected());
		if (Security.isConnected() && Security.getConnected().deleted) {
			Secure.logout();
		}
		renderArgs.put("topProjects", Project.findAll());
		
		List<Sprint> sprints = Sprint.findAll();
		for (Sprint s : sprints) {
			Date now = Calendar.getInstance().getTime();
			if (s.endDate != null && s.endDate.before(now)) {
				s.ended = true;
				Project p = s.project;
				Board b = p.board;
				User user = Security.getConnected();
				List<Component> components = p.getComponents();

				ArrayList<ComponentRowh> data = new ArrayList<ComponentRowh>();
				List<Column> columns = b.columns;
				List<Column> CS =new ArrayList<Column>();
				ArrayList<String> Columnsofsnapshot = new ArrayList<String>();
				for( int i=0; i<columns.size();i++)
				{
					if(columns.get( i ).onBoard==true)
					{
						CS.add( columns.get( i ) );
					}
				}
				for (int i = 0; i < CS.size(); i++) {
					Columnsofsnapshot.add(null);
					Columnsofsnapshot.set(i, CS.get(i).name);
				}

				int smallest;
				Column temp;
				for (int i = 0; i < CS.size(); i++) {
					smallest = i;
					for (int j = i + 1; j < CS.size(); j++) {
						if (CS.get(smallest).sequence > CS.get(j).sequence) {
							smallest = j;

						}

					}
					temp = CS.get(smallest);
					CS.set(smallest, columns.get(i));
					CS.set(i, temp);
					Columnsofsnapshot.set(smallest, CS.get(i).name);
					Columnsofsnapshot.set(i, temp.name);
				}

				for (int i = 0; i < components.size(); i++)// for each component get
				// the tasks
				{
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
					
						if(pcol.onBoard&&!pcol.deleted)
						{
						data.get(i).get(CS.indexOf(pcol)).add("(" + task.taskStory.description + ")" + "T" + task.id + "-" + task.description + "-" + task.assignee.name);
						}
					}
				}		
				String type = "sprint "+s.id;
				Snapshot snap = new Snapshot();
				snap.user = user;
				snap.type = type;
				snap.board = b;
				snap.sprint = s;
				snap.data = data;
				snap.Columnsofsnapshot = Columnsofsnapshot;
				snap.save();
				s.finalsnapshot=snap;
				s.save();
			}

		}
	}
	

}
