package edu.kit.ipd.alicenlp.ivan.tests;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNot.*;
import static org.junit.Assert.*;
import net.sf.extjwnl.JWNLException;

import org.junit.Test;

import edu.kit.ipd.alicenlp.ivan.rules.NonEntitiesFilterRule;

public class NonEntitiesFilterRuleTest {

	@SuppressWarnings("static-method")
	@Test
	public final void testApply() throws JWNLException {
		{
			// entity contained in word net
			NonEntitiesFilterRule rule = new NonEntitiesFilterRule();
			assertFalse("scope not recognised", rule.apply("scope"));
			assertThat("Wrong class for background", rule.getResult(), is(NonEntitiesFilterRule.EntityType.MODEL));
		}
		{
			NonEntitiesFilterRule rule = new NonEntitiesFilterRule();
			assertTrue("Background not recognised", rule.apply("background"));
			assertThat("Wrong class for background", rule.getResult(), is(NonEntitiesFilterRule.EntityType.DISPLAYABLE));
		}
		{
			NonEntitiesFilterRule rule = new NonEntitiesFilterRule();
			assertTrue("ground not recognised", rule.apply("ground"));
			assertThat("Wrong class for background", rule.getResult(), is(NonEntitiesFilterRule.EntityType.DISPLAYABLE));
		}
		{
			NonEntitiesFilterRule rule = new NonEntitiesFilterRule();
			assertTrue("sky not recognised", rule.apply("sky"));
			assertThat("Wrong class for background", rule.getResult(), is(NonEntitiesFilterRule.EntityType.DISPLAYABLE));
		}
		{
			NonEntitiesFilterRule rule = new NonEntitiesFilterRule();
			assertTrue("time not recognised", rule.apply("time"));
			assertThat("Wrong class for background", rule.getResult(), is(NonEntitiesFilterRule.EntityType.NONDISPLAYABLE));
		}
		{
			NonEntitiesFilterRule rule = new NonEntitiesFilterRule();
			assertTrue("clothes not recognised", rule.apply("clothes"));
			assertThat("Wrong class for background", rule.getResult(), is(NonEntitiesFilterRule.EntityType.DISPLAYABLE));
		}
		{
			// not contained in word net
			NonEntitiesFilterRule rule = new NonEntitiesFilterRule();
			assertFalse("Alice not recognised", rule.apply("Alice"));
			assertThat("Wrong class for background", rule.getResult(), is(NonEntitiesFilterRule.EntityType.MODEL));
		}
		{
			// entity contained in word net
			NonEntitiesFilterRule rule = new NonEntitiesFilterRule();
			assertFalse("tree not recognised", rule.apply("tree"));
			assertThat("Wrong class for background", rule.getResult(), is(NonEntitiesFilterRule.EntityType.MODEL));
		}		
	}

}
