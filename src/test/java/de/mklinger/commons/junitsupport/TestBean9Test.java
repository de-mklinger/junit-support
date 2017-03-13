package de.mklinger.commons.junitsupport;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class TestBean9Test extends BeanTestBase<TestBean9> {
	public TestBean9Test() {
		super(TestBean9.class);
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
	public void toStringTestForAllConstructorsEmpty() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		super.toStringTestForAllConstructorsEmpty();
	}

	@Override
	@Test(expected = AssertionError.class)
	public void toStringTestForAllConstructorsFilled() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		super.toStringTestForAllConstructorsFilled();
	}
}
