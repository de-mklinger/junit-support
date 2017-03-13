package de.mklinger.commons.junitsupport;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class TestBean7Test extends BeanTestBase<TestBean7> {
	public TestBean7Test() {
		super(TestBean7.class);
	}

	@Override
	protected ConstructorParameters[] getConstructorParameters() {
		return new ConstructorParameters[] {
				DEFAULT_CONSTRUCTOR_PARAMETERS,
				new ConstructorParameters(new Class[] { String.class, Integer.TYPE, Boolean.TYPE }, new String[] { "s", "n", "b" })
		};
	}

	@Override
	@Test(expected = AssertionError.class)
	public void hashCodeTest() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
		super.hashCodeTest();
	}
}
