package de.mklinger.commons.junitsupport;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class TestBean8Test extends BeanTestBase<TestBean8> {
	public TestBean8Test() {
		super(TestBean8.class);
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
	public void copyConstructorEmptyEqualsTest() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		super.copyConstructorEmptyEqualsTest();
	}

	@Override
	@Test(expected = AssertionError.class)
	public void copyConstructorEqualsTest() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		super.copyConstructorEqualsTest();
	}

	@Override
	@Test(expected = AssertionError.class)
	public void equalsValuesTest() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
		super.equalsValuesTest();
	}
}
