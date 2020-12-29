package de.mklinger.commons.junitsupport;

import org.junit.AssumptionViolatedException;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class RuntimeIgnoreTest {
	@Test(expected = AssumptionViolatedException.class)
	public void testIgnore() {
		RuntimeIgnore.ignore();
	}

	@Test(expected = AssumptionViolatedException.class)
	public void testIgnoreIf1() {
		RuntimeIgnore.ignoreIf(true);
	}

	@Test
	public void testIgnoreIf2() {
		RuntimeIgnore.ignoreIf(false);
	}

	@Test(expected = AssumptionViolatedException.class)
	public void testIgnoreIfNot1() {
		RuntimeIgnore.ignoreIfNot(false);
	}

	@Test
	public void testIgnoreIfNot2() {
		RuntimeIgnore.ignoreIfNot(true);
	}

	@Test
	public void ignored() {
		RuntimeIgnore.ignore();
	}
}
