import java.util.ArrayList;
import java.util.Date;

import models.Artifact;
import models.Meeting;
import models.Project;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Checks Getting the artifacts C4S17
 * 
 * @author Hossam Amer
 */

public class c4S17 extends UnitTest
{

	@Before
	public void setUp()
	{
		Fixtures.deleteAll();

	}

	@Test
	public void getArtifacts()
	{
		ArrayList<String> emails = new ArrayList<String>();

		emails.addAll( Arrays.asList( new String[] { "eminem.virus2010@gmail.com", "me@sakr.me", "moataz.mekki@gmail.com", "omar.nabil10@gmail.com", "ahmed.behairy9@gmail.com", "amorotto@Gmail.com", "ghadafakhry@gmail.com", "hossam.sharaf@gmail.com", "minazakiz@gmail.com", "galalaly28@gmail.com", "justheba@gmail.com", "evilmonster.300@gmail.com", "mohamed.monayri@gmail.com", "moumen.elteir@gmail.com", "ahmed.k.abdelhameed@gmail.com", "eabdelrahman89@gmail.com", "iistcrimi@gmail.com", "hossam.amer12@gmail.com", "menna.ghoneim@gmail.com", "amr.mohamed.abdelwahab@gmail.com", "asmaa89alkomy@gmail.com", "dina.e.helal@gmail.com", "hadeer.diwan@gmail.com", "joseph.hajj90@gmail.com" } ) );
		ArrayList<User> users = new ArrayList<User>();
		ArrayList<Project> projects = new ArrayList<Project>();
		for( int i = 0; i < emails.size(); i++ )
		{
			User u = new User( emails.get( i ).split( "@" )[0], emails.get( i ), "test" );
			u.isActivated = u.isAdmin = true;
			u.save();
			users.add( u );
		}

		Project p1 = new Project( "Smartsoft", "The best company project ever!" ).save();
		p1.init();
		projects.add( p1 );

		Artifact a1 = new Artifact( "Notes", "First note" );
		a1.save();
		Artifact a2 = new Artifact( "Notes", "Second note" );
		a2.save();
		Meeting M = new Meeting( "ERD", users.get( 0 ), "anything", new Date().getTime() + 1000000000, new Date().getTime(), "C1", "meeting", p1, null );
		M.artifacts.add( a1 );
		M.artifacts.add( a2 );
		a1.meetingsArtifacts.add( M );
		a2.meetingsArtifacts.add( M );
		M.save();

		assertNotNull( a1.getArtifacts( 1 ) );
	}
}