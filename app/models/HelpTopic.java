package models;

import javax.persistence.Entity;

/**
 * Help Topic Model. Holds the casts here for easier reference in the future.
 */
@Entity
public class HelpTopic extends SmartModel {

	/**
	 * Summary of this help topic
	 */
	public String summary;

	/**
	 * URL of this topic.. saved in case we need to include casts from external
	 * sources
	 */
	public String url;

	/**
	 * Default constructor
	 * 
	 * @param summary
	 *            Summary of this help topic
	 * @param filename
	 *            Filename of this cast, used for easier reference to our local
	 *            casts
	 */
	public HelpTopic (String summary, String filename) {
		this.summary = summary;
		this.url = "/public/screencasts/" + filename;
	}

	/**
	 * Static object of HelpTopic
	 */
	public static class Object {
		/**
		 * Summary of this help topic
		 */
		public String summary;

		/**
		 * filename of this help topic, for easier reference
		 */
		public String filename;

		/**
		 * Default constructor
		 * 
		 * @param s
		 *            Summary of this topic
		 * @param f
		 *            filename of this topic
		 */
		public Object (String s, String f) {
			summary = s;
			filename = f;
		}
	}
}
