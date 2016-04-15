import java.util.List;

import models.Request;
import models.Role;

import org.junit.Test;

import play.mvc.Before;
import play.test.Fixtures;
import play.test.UnitTest;


public class C1S14Unit1 extends UnitTest
{
	@Before
    public void loadFixtures()
    {
    	Fixtures.deleteAll();
        Fixtures.load("dummy-data.yml");
        
    }
	
	@Test
	public void testdeletionRequestAccept()
	{
		Request currentRequest = Request.find("byHash", "12345678").first();
		assertNotNull( currentRequest );
		List<Role> projectRoles = Role.find("project", currentRequest.project).fetch();
		//System.out.println(projectRoles.size());
		for (int i = 0; i < projectRoles.size(); i++) {

			if (projectRoles.get(i).users.contains(currentRequest.user)) {
				int oldCount = projectRoles.get( i ).users.size();
				projectRoles.get(i).removeUser(currentRequest.user);
				assertTrue( oldCount != projectRoles.get( i ).users.size() );
				oldCount = currentRequest.user.roles.size();
				currentRequest.user.roles.remove( projectRoles.get( i ) );
				currentRequest.user.save();
				assertTrue( currentRequest.user.roles.size() != oldCount );
			}
		}
		int oldCount;
		oldCount = currentRequest.project.users.size();
		currentRequest.project.removeUser(currentRequest.user);
		assertTrue( oldCount != currentRequest.project.users.size() );
		oldCount = currentRequest.user.projects.size();
		currentRequest.user.projects.remove( currentRequest.project );
		currentRequest.user.save();
		assertTrue( oldCount != currentRequest.user.projects.size() );
		long count = Request.count();
		currentRequest.delete();
		assertTrue( count != Request.count() );
		
	}
	
}
	