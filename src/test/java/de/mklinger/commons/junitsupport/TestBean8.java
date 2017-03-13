package de.mklinger.commons.junitsupport;

/**
 * Wrong equals() implementation.
 *
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class TestBean8 {
	private String s;
	private int n;
	private boolean b;

	public TestBean8() {
	}

	public TestBean8(String s, int n, boolean b) {
		this.s = s;
		this.n = n;
		this.b = b;
	}

	public TestBean8(TestBean8 source) {
		this.s = source.s;
		this.n = source.n;
		this.b = source.b;
	}

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public boolean isB() {
		return b;
	}

	public void setB(boolean b) {
		this.b = b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (b ? 1231 : 1237);
		result = prime * result + n;
		result = prime * result + ((s == null) ? 0 : s.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestBean1 [");
		if (s != null) {
			builder.append("s=");
			builder.append(s);
			builder.append(", ");
		}
		builder.append("n=");
		builder.append(n);
		builder.append(", b=");
		builder.append(b);
		builder.append("]");
		return builder.toString();
	}
}
