import static org.junit.Assert.*;
import models.Project;
import models.Request;
import models.User;

import org.junit.Test;

import play.mvc.Before;
import play.test.Fixtures;
import play.test.UnitTest;


public class C1S14Unit2 extends UnitTest{
	@Before
    public void loadFixtures2()
    {
    	Fixtures.deleteAll();
        Fixtures.load("dummy-data.yml");
        
    }
	
	@Test
	public void testrequestIgnore()
	{
		Request x = Request.find("byHash", "11111111").first();
		long oldCount = Request.count();
		//Request x = Request.find("byHash", "12345678").first();
		assertNotNull( x );
		Project y = x.project;
		//Notifications.notifyUsers(x.user, "Role Request Denied", "Your Role request to be "+x.role.name+" in "+y.name+" has been denied",(byte) -1);
		User myUser = User.find("byEmail", "me@sakr.me").first();
		assertNotNull( myUser );
		//Calendar cal = new GregorianCalendar();
		//Logs.addLog(myUser, "RequestDeny", "Request", x.id, y, cal.getTime());
		x.delete();
		assertTrue( oldCount != Request.count() );
	}
	
}
