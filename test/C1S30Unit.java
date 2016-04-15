import models.User;

import org.junit.Test;

import controllers.Secure;

import play.mvc.Before;
import play.test.Fixtures;
import play.test.UnitTest;


public class C1S30Unit extends UnitTest
{
	@Before
    public void loadFixtures()
    {
    	Fixtures.deleteAll();
        Fixtures.load("data.yml");
        
    }
	
	@Test
	public void testdoActivation()
	{
		User currentUser = User.find( "name", "root" ).first();
		assertNotNull( currentUser );
		boolean oldActivated = currentUser.isActivated;
		if(currentUser != null && !currentUser.isActivated)
		{	
			currentUser.isActivated = true;
			currentUser.save();
			//flash.success( "Thank you , your Account has been Activated! . Login Below" );
		}
		User user = User.find( "name", "root" ).first();
		assertTrue( oldActivated && user.isActivated );
		//else
			//flash.error( "This activation link is not valid or has expired. Activation Failed!" );
		//Secure.login();
	}
}
