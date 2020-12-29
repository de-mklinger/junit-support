package de.mklinger.commons.junitsupport;

public class TestException1 extends Exception {
	private static final long serialVersionUID = 1L;

	public TestException1() {
		super();
	}

	public TestException1(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TestException1(final String message, final Throwable cause) {
		super(message, cause);
	}

	public TestException1(final String message) {
		super(message);
	}

	public TestException1(final Throwable cause) {
		super(cause);
	}
}
