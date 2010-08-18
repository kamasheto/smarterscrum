package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

/**
 * Project settings. Usually, we'd store the settings as a key/value pair, but considering we might include few settings and we might want to include greater extendibility later by allowing multi-savable set of settings, having them stored per row would pay off
 */
@Entity
public class Setting extends SmartModel {
	/**
	 * Site name
	 */
	public String siteName = "smartsoft - smarterscrum";
	
	/**
	 * Default twitter hash
	 */
	public String twitterHash = "#smarterscrum";
	
	/**
	 * Default number of items per box
	 */
	public int defaultEntriesPerBox = 5;
}

