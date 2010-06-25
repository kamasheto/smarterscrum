package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import models.Component;
import models.Project;
import models.Requestreviewer;
import models.TaskType;
import models.User;

public class Requestreviewers extends SmartController {
	/**
	 * Views the requestToBeReviewer form in where the user will choose the task
	 * he wants
	 * 
	 * @author hoksha
	 * @parm void
	 * @return void
	 * @task C3,S23
	 * @Sprint2
	 */
	public static void ListTypesOfReviewers() {
		User user = Security.getConnected();

		List<Project> projects = user.projects;
		boolean check = projects ==null;
		render(projects, check);
	}

	/**
	 * this method saves the request of the user in the data base in the entity
	 * Requestreviewer
	 * 
	 * @author hoksha
	 * @parm long ID TaskType id
	 * @parm long Pid project id
	 * @return void
	 * @task C3,S23
	 * @Sprint2
	 */
	public static void requestToBeReviewer(long ID, long Pid) {
		Date todayDate = new GregorianCalendar().getTime();
		String message = "";
		TaskType task = TaskType.findById(ID);
		Project project = Project.findById(Pid);
		User user = Security.getConnected();
		User xx = null;
		List<Requestreviewer> request = Requestreviewer.find("order by id desc").fetch();
		if (request == null) {
			for (int i = 0; i < project.users.size(); i++) {
				if (project.users.get(i).isAdmin) {
					xx = project.users.get(i);
				}
			}

			if (task == null) {
				message = "task type is null";
			} else {
				if (project == null) {
					message = "project is null";
				} else {
					if (user == null) {
						message = "no user";
					} else {
						int z = -1;
						for (int i = 0; i < user.components.size(); i++) {
							for (int j = 0; j < project.components.size(); j++) {
								if (user.components.get(i) == project.components.get(j)) {
									z = j;

								}
							}
						}
						if (z == -1) {
							message = "there is no component related between the project and  TaskType";

						} else {
							Requestreviewer x = new Requestreviewer(user, project.components.get(z), task);
							x.save();
							String header = "User: " + "\'" + Security.getConnected().name + "\'" + " requested to be a " + "\'" + task.name + "\'" + " reviewer.";
							String body = "User: " + "\'" + Security.getConnected().name + "\'" 
							    + " requested to be a " + "\'" + task.name + "\'" + " reviewer." + '\n' 
								+ " Requested at: " + new Date(System.currentTimeMillis()) + ".";
							Logs.addLog(Security.getConnected(), "Request to be a reviewer", "Task Type", ID, project, new Date(System.currentTimeMillis()));
							Notifications.notifyUsers(project, header, body, "Request To Be Reviewer",new Byte((byte) 0));
							message = "The request for " + task.name +" has been sent succesfully";

						}
					}
				}
			}
		} else {
			boolean flag = false;
			for (int i = 0; i < request.size(); i++) {
				if (task != null) {
					if (request.get(i).user == user && request.get(i).types == task) {
						flag = true;
					}
				}
			}
			if (flag == true) {
				message = "you already requested to be the reviewer of " + task.name;

			} else {
				for (int i = 0; i < project.users.size(); i++) {
					if (project.users.get(i).isAdmin) {
						xx = project.users.get(i);
					}
				}

				if (task == null) {
					message = "task type is null";
				} else {
					if (project == null) {
						message = "project is null";
					} else {
						if (user == null) {
							message = "no user";
						} else {
							int z = -1;
							for (int i = 0; i < user.components.size(); i++) {
								for (int j = 0; j < project.components.size(); j++) {
									if (user.components.get(i) == project.components.get(j)) {
										z = j;

									}
								}
							}
							if (z == -1) {
								message = "there is no component related between the project and  TaskType";

							} else {
								Requestreviewer x = new Requestreviewer(user, project.components.get(z), task);
								x.save();
								String header = "User: " + "\'" + Security.getConnected().name + "\'" + " requested to be a " + "\'" + task.name + "\'" + " reviewer.";
								String body = "User: " + "\'" + Security.getConnected().name + "\'" 
								    + " requested to be a " + "\'" + task.name + "\'" + " reviewer." + '\n' 
									+ " Requested at: " + new Date(System.currentTimeMillis()) + ".";
								Logs.addLog(Security.getConnected(), "Request to be a reviewer", "Task Type", ID, project, new Date(System.currentTimeMillis()));
								Notifications.notifyUsers(project, header, body, "Request To Be Reviewer",new Byte((byte) 0));
								message = "The request for " + task.name +" has been sent succesfully";

							}
						}
					}
				}
			}
		}
		renderText(message);
	}

	/**
	 * this method saves the respond of the user in the data base in the entity
	 * Requestreviewer
	 * 
	 * @author hoksha
	 * @parm void
	 * @return void
	 * @task C3,S24
	 * @Sprint2
	 */
	//@Check ("canrespond")
	public static void respond(long id) {
		Project project = Project.findById(id);
		List<Requestreviewer> requests = new ArrayList<Requestreviewer>();
		User user = User.find("byEmail", Security.connected()).first();
		Security.check(user.in(project).can("respond"));
		for(Component component : project.components){
			List<Requestreviewer> list = Requestreviewer.findBy("byComponentAndAcceptedAndRejected", component, false, false);
			requests.addAll(list);
		}
		boolean check = requests.size()==0;
		render(requests, check, project);
	}

	/**
	 * this method saves the respond of the scrum master in the data base in the
	 * entity Requestreviewer if he accepts
	 * 
	 * @author hoksha
	 * @parm long requestID the ID of the Requestreviwer he choose
	 * @return void
	 * @task C3,S24
	 * @Sprint2
	 */
	public static void accept(long requestID) {
		Requestreviewer requests = Requestreviewer.findById(requestID);
		String message = "";
		if (requests != null) {
			requests.accepted = true;
			message = "the request has been accepted ";
			requests.save();
			String header = "User: " + "\'" + requests.user.name + "\'" + " request to be a " + "\'" + requests.types.name + "\'" + " reviewer has been accepted.";
			String body = "User: " + "\'" + requests.user.name + "\'" + " request to be a " + "\'" + requests.types.name + "\'" + " reviewer has been accepted." + '\n' + '\n'   
			    + " Accepted by: " + Security.getConnected().name + "." + '\n' 
				+ " Accepted at: " + new Date(System.currentTimeMillis()) + ".";
			Logs.addLog(Security.getConnected(), "Accept to be a reviewer request", "Task Type", requestID, requests.component.project, new Date(System.currentTimeMillis()));
			Notifications.notifyUsers(requests.component.project, header, body, "Accept To Be Reviewer Request",new Byte((byte) 1));
		}

		renderText(message);
	}

	/**
	 * this method saves the respond of the scrum master in the data base in the
	 * entity Requestreviewer if he rejects
	 * 
	 * @author hoksha
	 * @parm long requestID the ID of the Requestreviwer he choose
	 * @return void
	 * @task C3,S24
	 * @Sprint2
	 */
	public static void reject(long requestID) {
		Requestreviewer x = Requestreviewer.findById(requestID);
		if (x != null) {
			x.rejected=true;
			x.save();
			String header = "User: " + "\'" + x.user.name + "\'" + " request to be a " + "\'" + x.types.name + "\'" + " reviewer has been rejected.";
			String body = "User: " + "\'" + x.user.name + "\'" + " request to be a " + "\'" + x.types.name + "\'" + " reviewer has been rejected." + '\n' + '\n'   
			    + " Rejected by: " + Security.getConnected().name + "." + '\n' 
				+ " Rejwcted at: " + new Date(System.currentTimeMillis()) + ".";
			Logs.addLog(Security.getConnected(), "Reject to be a reviewer request", "Task Type", requestID, x.component.project, new Date(System.currentTimeMillis()));
			Notifications.notifyUsers(x.component.project, header, body, "Reject To Be Reviewer Request",new Byte((byte) -1));
		}
		renderText("the request has been rejected");
	}
}
