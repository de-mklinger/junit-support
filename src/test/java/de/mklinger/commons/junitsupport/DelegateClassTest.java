package de.mklinger.commons.junitsupport;

public class DelegateClassTest extends DelegateClassTestBase {
	public DelegateClassTest() {
		super(DelegatedClass.class, DelegateClass.class, declaredTestMethodsFor(DelegateClass.class));
	}
}
