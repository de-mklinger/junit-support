package de.mklinger.commons.junitsupport;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class TestBean1Test extends BeanTestBase<TestBean1> {
	public TestBean1Test() {
		super(TestBean1.class);
	}

	@Override
	protected ConstructorParameters[] getConstructorParameters() {
		return new ConstructorParameters[] {
				DEFAULT_CONSTRUCTOR_PARAMETERS,
				new ConstructorParameters(new Class[] { String.class, Integer.TYPE, Boolean.TYPE }, new String[] { "s", "n", "b" })
		};
	}
}
