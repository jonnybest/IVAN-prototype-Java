/**
 * 
 */
package edu.kit.ipd.alicenlp.ivan.tests;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsCollectionContaining.*;
import static org.junit.Assert.*;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.IvanInvalidMappingException;
import edu.kit.ipd.alicenlp.ivan.data.EntityInfo;
import edu.kit.ipd.alicenlp.ivan.data.InitialState;

/**
 * @author Jonny
 *
 */
public class InitialStateTest {

	@SuppressWarnings("static-method")
	@Test
	public final void test() throws IvanInvalidMappingException {
		{
			InitialState mystate = new InitialState();
			
			EntityInfo e1 = new EntityInfo("dog", "on the left", "south");
			EntityInfo e2 = new EntityInfo("cat");
			EntityInfo e3 = new EntityInfo("Fluffy", "on the right", "north");
			e3.setIsProperName(true);
			
			mystate.add(e1);
			mystate.add(e2);
			mystate.add(e3);
			
			//mystate.addAlias(e3);
			//mystate.map(/* proper name */ e3.getEntity(), /* entity */ e3);
			mystate.map(e3.getEntity(), e2);
			
			assertThat("missing entity: cat", mystate.get("cat"), hasItem(e3));
			assertThat("missing entity: cat", mystate.get("cat"), hasItem(new EntityInfo("Fluffy", "on the right", "north")));
			assertThat("missing entity: Fluffy", mystate.get("Fluffy"), hasItem(new EntityInfo("cat", null, null)));
		}
		{
			InitialState mystate = new InitialState();
			
			EntityInfo e1 = new EntityInfo("dog", "on the left", "south");
			EntityInfo e2 = new EntityInfo("cat");
			EntityInfo e3 = new EntityInfo("Fluffy", "on the right", "north");
			e3.setIsProperName(false);
			
			mystate.add(e1);
			mystate.add(e2);
			mystate.add(e3);
			
			//mystate.addAlias(e3);
			//mystate.map(/* proper name */ e3.getEntity(), /* entity */ e3);
			mystate.map(e3.getEntity(), e2);
			
			assertThat("missing entity: cat", mystate.get("cat"), hasItem(e3));
			assertThat("missing entity: cat", mystate.get("cat"), hasItem(new EntityInfo("Fluffy", "on the right", "north")));
			assertThat("missing entity: Fluffy", mystate.get("Fluffy"), hasItem(new EntityInfo("cat", null, null)));
		}
	}

	@SuppressWarnings("static-method")
	@Test
	public final void hasNameTest() throws IvanInvalidMappingException
	{
		InitialState mystate = new InitialState();
		EntityInfo e1 = new EntityInfo("dog", "on the left", "south");
		mystate.add(e1);
		assertFalse("unnamed dog wrong", mystate.hasName("dog"));
		
		mystate.map("Spanky", e1);
		assertTrue("named dog wrong", mystate.hasName("dog"));
		assertThat(mystate.getNames("dog"), hasItem("Spanky"));
	}
}
