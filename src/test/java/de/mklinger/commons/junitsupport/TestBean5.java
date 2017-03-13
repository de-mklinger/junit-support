package de.mklinger.commons.junitsupport;

import java.util.Random;

/**
 * Setter wrong assignment.
 *
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class TestBean5 {
	private String s;
	private int n;
	private boolean b;

	public TestBean5() {
	}

	public TestBean5(String s, int n, boolean b) {
		this.s = s;
		this.n = n;
		this.b = b;
	}

	public TestBean5(TestBean5 source) {
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

	private static Random random = new Random();

	public void setN(int n) {
		this.n = random.nextInt();
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TestBean5 other = (TestBean5)obj;
		if (b != other.b) {
			return false;
		}
		if (n != other.n) {
			return false;
		}
		if (s == null) {
			if (other.s != null) {
				return false;
			}
		} else if (!s.equals(other.s)) {
			return false;
		}
		return true;
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
