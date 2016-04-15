package controllers;

import java.util.List;

import models.HelpTopic;

/**
 * Controller to handle and show all help topics
 * 
 * @author ms
 */
public class HelpTopics extends SmartController {

	/**
	 * Shows all topics (per project)
	 * 
	 * @param projectId
	 *            Project id, we might need this in the future if we're going to
	 *            let project admins add topics per project
	 */
	public static void topics(long projectId) {
		List<HelpTopic> topics = HelpTopic.findAll();
		render(topics);
	}

	/**
	 * Shows this topic
	 * 
	 * @param id
	 *            topic id
	 */
	public static void topic(long id) {
		HelpTopic topic = HelpTopic.findById(id);
		render(topic);
	}
}