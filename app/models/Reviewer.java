package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/*
 * The class Request Reviewer in which the user can request to be reviewer and
 * the Scrum master can respond to that request
 * @author hoksha
 */
@Entity
public class Reviewer extends SmartModel {        
        /**
         * the user request
         */
        @ManyToOne
        public User user;
        /**
         * the component of the user requested to be reviewer
         */
        @ManyToOne
        public Project project;
        /**
         * the type the user Request to be reviewer of
         */
        @ManyToOne
        public TaskType taskType;

        /**
         * if false then pending, else if true then accepted
         */
        public boolean accepted;

        /**
         * This is a Class Constructor Creates a new Requestreviewer object
         *
         * @author hoksha
         * @param user
         *            the user who requests to be reviewer
         * @param component
         *            the component of the user who requested
         * @param types
         *            the type
         * @return void
         * @task C3 S23
         * @sprint 2
         */

        public Reviewer (User user, Project project, TaskType taskType) {
                this.user = user;
                this.project = project;
                this.taskType = taskType;
                this.accepted = false;
        }

}