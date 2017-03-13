package de.mklinger.commons.junitsupport;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class TestBean5Test extends BeanTestBase<TestBean5> {
	public TestBean5Test() {
		super(TestBean5.class);
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
	public void propertyTestForAllConstructors() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		super.propertyTestForAllConstructors();
	}

	@Override
	@Test(expected = AssertionError.class)
	public void copyConstructorValuesTest() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		super.copyConstructorValuesTest();
	}

	@Override
	@Test(expected = AssertionError.class)
	public void hashCodeTest() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
		super.hashCodeTest();
	}

	@Override
	@Test(expected = AssertionError.class)
	public void equalsValuesTest() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
		super.equalsValuesTest();
	}
}
