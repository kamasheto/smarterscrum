import models.Artifact;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class C4S18 extends UnitTest {
	@Before
	public void setUp() {
		Fixtures.deleteAll();

	}

	@Test
	public void addNote() {

		Long countBeforeCreation = (long) Artifact.count();
		Artifact x = new Artifact("Notes", "i added a note");
		x.save();
		Long newCount = (long) Artifact.count();
		assertEquals(newCount - countBeforeCreation, 1);

	}

}
