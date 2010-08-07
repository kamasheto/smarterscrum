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
import notifiers.Notifications;

public class Requestreviewers extends SmartController {
	/**
	 * Views the requestToBeReviewer form in where the user will choose the type of the task he
	 * wants to review.
	 * 
	 * @author hoksha
	 * @param void
	 * @return void
	 * 
	 */
	public static void ListTypesOfReviewers() {
		User user = Security.getConnected();
		List<Project> projects = user.projects;
		boolean check = projects == null;
		render(projects, check);
	}

	/**
	 * This method saves the requests of the user.
	 * 
	 * @author hoksha
	 * @parm long ID TaskType id
	 * 				the Type the user chose to review.
	 * @parm long Pid project id
	 * 				the project in which the user wants to be a reviewer in.
	 * @return void
	 * 
	 */
	public static void requestToBeReviewer(long ID) {		
		String message = "";
		TaskType task = TaskType.findById(ID);
		// modified by mahmoudsakr
		// seen so2al.. why on earth do we need the project if we have the task type?
		// Project project = Project.findById(Pid);
		Project project = task.project;
		User user = Security.getConnected();
		Security.check(user.projects.contains(project)); // make sure he's in this project.. added by ms bardo
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
							Logs.addLog(Security.getConnected(), "Request to be a reviewer", "Task Type", ID, project, new Date(System.currentTimeMillis()));
							// Notifications.notifyProjectUsers(project, header,
							// body, "requestToBeReviewer", (byte) 0);
							message = "The request for " + task.name + " has been sent succesfully";

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
								Logs.addLog(Security.getConnected(), "Request to be a reviewer", "Task Type", ID, project, new Date(System.currentTimeMillis()));
								// Notifications.notifyUsers(project, header,
								// body, "RequestToBeReviewer", new Byte((byte)
								// 0));
								message = "The request for " + task.name + " has been sent succesfully";

							}
						}
					}
				}
			}
		}
		renderText(message);
	}

	/**
	 * this method lists the reviewer requests done by users to the projects' administrators.
	 * 
	 * @author hoksha
	 * @param void
	 * @return void
	 * @
	 */
	public static void respond(long id) {
		Project project = Project.findById(id);
		List<Requestreviewer> requests = new ArrayList<Requestreviewer>();
		// User user = User.find("byEmail", Security.connected()).first();
		User user = Security.getConnected();
		Security.check(user.in(project).can("respond"));
		for (Component component : project.components) {
			List<Requestreviewer> list = Requestreviewer.findBy("byComponentAndAcceptedAndRejected", component, false, false);
			requests.addAll(list);
		}
		boolean check = requests.size() == 0;
		render(requests, check, project);
	}

	/**
	 * this method saves the acceptance of the projects' administrators to the reviewer requests
	 * done by users
	 * 
	 * @author hoksha
	 * @param long requestID the ID of the Request 
	 * @return void
	 *
	 */
	public static void accept(long requestID) {
		Requestreviewer requests = Requestreviewer.findById(requestID);
		Security.check(Security.getConnected().in(requests.component.project).can("manageReviewerRequests"));
		String message = "";
		if (requests != null) {
			requests.accepted = true;
			message = "the request has been accepted ";
			requests.save();			
			Logs.addLog(Security.getConnected(), "Accept to be a reviewer request", "Task Type", requestID, requests.component.project, new Date(System.currentTimeMillis()));
			String url = "@{Application.externalOpen("+requests.component.project.id+", '/users/listUserProjects?userId="+requests.user.id+"&boxId=2&projectId="+requests.component.project.id+"&currentProjectId="+requests.component.project.id+"', false)}";			
			//Notifications.notifyProjectUsers(requests.component.project, "AcceptToBeReviewerRequest", url, "Review Request", requests.user.name, (byte) 1);			
		}

		renderText(message);
	}

	/**
	 * this method saves the rejection of the projects' administrators to the reviewer requests
	 * done by users
	 * 	  
	 * @author hoksha
	 * @param long requestID the ID of the Request
	 * @return void
	 *
	 */
	public static void reject(long requestID) {
		Requestreviewer x = Requestreviewer.findById(requestID);
		Security.check(Security.getConnected().in(x.component.project).can("manageReviewerRequests"));
		if (x != null) {
			x.rejected = true;
			x.save();			
			Logs.addLog(Security.getConnected(), "Reject to be a reviewer request", "Task Type", requestID, x.component.project, new Date(System.currentTimeMillis()));
			// Notifications.notifyProjectUsers(x.component.project, header,
			// body, "RejectToBeReviewerRequest", (byte) -1);
		}
		renderText("the request has been rejected");
	}

	/**
	 * A method that removes an old reviewer request done by the user in case he wants to cancel 
	 * his request.
	 * @param taskTypeId the id of the type he wanted to review.
	 */
	public static void removeRequest(long taskTypeId) {
		User user = Security.getConnected();
		TaskType taskType = TaskType.findById(taskTypeId);
		Requestreviewer r = Requestreviewer.find("byUserAndTypes", user, taskType).first();
		if (r != null) {
			r.delete();
			renderText("Your request was cancelled successfully");
		} else {
			renderText("Not found. Most probably request was removed before.");
		}
	}
}
