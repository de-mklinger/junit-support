package de.mklinger.commons.junitsupport;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class TestException2Test extends ExceptionTestBase<TestException2> {
	public TestException2Test() {
		super(TestException2.class);
	}

	@Override
	@Test(expected = AssertionError.class)
	public void testConstructor4() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		super.testConstructor4();
	}
}
