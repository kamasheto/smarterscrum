package controllers;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import models.Component;
import models.Project;
import models.Sprint;
import models.Story;
import models.User;
import play.mvc.With;

import controllers.SmartController;


public class ProductBacklogs extends SmartController {
	/**
	 * Gets the list of stories in the project or the component that has the id
	 * sent from the project or component page , and renders this list with the
	 * index.html file in the ProductBacklogs View
	 * <p>
	 * the boolean variable running says if this project has a running sprint so
	 * as not to allow editing
	 * 
	 * @param id
	 *            this is the id of the Project
	 * @param isComp
	 *            is an int value that is set to 0 if the id is of a project and
	 *            1 if the id is of a component(@author menna_ghoneim)
	 * @see list of user stories
	 */
	public static void index(long id, int isComp) {
		
		Project project1 = Project.findById( id );
		Security.check( project1.users.contains( Security.getConnected()));
		User user = Security.getConnected();
		
		
		boolean inproj = user.isAdmin;
		String name = "";
		if (isComp == 0) {
			Project project = Project.findById(id);
			name = project.name;
			List<List<Story>> stories = new LinkedList<List<Story>>();
			List<Sprint> sprints = project.sprints;
			boolean running = false;
			long isRunning = project.runningSprint();
			for (int i = 0; i < sprints.size(); i++) {
				System.out.println("in loop");
				if (sprints.get(i).id == isRunning)
					running = true;
			}
			int i =0;
			for (Component component : project.components) {
				stories.add(new LinkedList<Story>());
				for (Story story : component.componentStories) {
					stories.get(i).add(story);
				
				}
	i++;
			}
			
		
			
			
			// for (int i = 0; i < user.roles.size(); i++) {
			// if (user.roles.get(i).project.equals(project)) {
			// inproj = user.roles.get(i).canEditBacklog;
			// break;
			// }
			// }
			inproj = user.in(project).can("editBacklog");
			if (!(project.users.contains(user) || inproj)) {
				stories = null;
				inproj = false;
			} else {
				inproj = true;
			}
			
			
			for(int a =0 ; a<stories.size();a++)
			{
				for(int j=0; j<stories.get( a ).size();j++)
					{
					String [] temp=stories.get( a ).get(j).succussSenario.split("/n");
					String temp1="";
					for(int k=0;k<temp.length;k++)
					temp1=temp1+temp[k]+" <br/> " ;
					stories.get( a ).get( j ).succussSenario= temp1;
					
					}
			}
			
			render(stories, project, running, isComp, inproj, name);

		} else {
			Component component = Component.findById(id);
			name = component.name;
			Long compId = component.id;
			List<Story> stories = component.componentStories;
			Project project = component.project;
			List<Sprint> sprints = project.sprints;
			boolean running = false;
			Date date = new Date();
			long currentDate = date.getTime();
			long sprintEnd;
			long sprintStart;
			for (Sprint sprint : sprints) {
				sprintEnd = sprint.endDate.getTime();
				sprintStart = sprint.startDate.getTime();
				if (currentDate >= sprintStart && currentDate <= sprintEnd)
					running = true;
			}
			// for (int i = 0; i < user.roles.size(); i++) {
			// if (user.roles.get(i).project.equals(project)) {
			// inproj = user.roles.get(i).canEditBacklog;
			// break;
			// }
			// }
			inproj = user.in(project).can("editBacklog");
			if (!(user.components.contains(component) || inproj)) {
				stories = null;
				inproj = false;
			} else {
				inproj = true;
			}
			
			
			render(stories, project, running, isComp, inproj, name, compId);
		}

	}

	/**
	 * @author eabdelrahman
	 * @author Hadeer younis
	 * @param id
	 *            is the ID of the project of the requested chart
	 * @param componentID
	 *            is the ID of the component of the requested chart
	 * @return renders the string containing the data and the method of project
	 *         to generate graph and the sprints in it
	 */

	public static void showGraph( long id, long componentId )
	{
		Project temp = Project.findById( id );

		Security.check( Security.getConnected().projects.contains( temp) );
		Component myComponent = Component.findById( componentId );
		String Data = temp.fetchData( componentId );
		List<Sprint> SprintsInProject = temp.sprints;
		int maxDays =0;
		for(int i =0; i<SprintsInProject.size(); i++)
		{
			if(SprintsInProject.get(i).tasks.size()==0)
				SprintsInProject.remove( i );
			else
			{
				for(int j =0; j<SprintsInProject.size();j++)
				{
					if(SprintsInProject.get( j ).getDuration()>=SprintsInProject.get( i ).getDuration())
						maxDays= SprintsInProject.get( j ).getDuration();
				}
			}
		}
		if (Data.startsWith("GenerateFullGraph([[[]]"))
			Data = null;
		else
			Data = Data.substring( 0,18 )+maxDays+","+Data.substring( 18 );
		render(Data, SprintsInProject, temp, componentId, myComponent);
	}
}
