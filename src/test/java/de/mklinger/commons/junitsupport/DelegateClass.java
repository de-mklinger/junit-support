package de.mklinger.commons.junitsupport;

public class DelegateClass {
	private final DelegatedClass delegate;

	public DelegateClass(final DelegatedClass delegate) {
		this.delegate = delegate;
	}

	public void method1() {
		delegate.method1();
	}

	public String method2() {
		return delegate.method2();
	}

	public void method3(final String arg) {
		delegate.method3(arg);
	}

	public void method4(final int arg) {
		delegate.method4(arg);
	}

	public String method5(final int arg) {
		return delegate.method5(arg);
	}
}
