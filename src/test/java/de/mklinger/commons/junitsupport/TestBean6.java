package de.mklinger.commons.junitsupport;

/**
 * Getter wrong return.
 *
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class TestBean6 {
	private String s;
	private int n;
	private boolean b;

	public TestBean6() {
	}

	public TestBean6(String s, int n, boolean b) {
		this.s = s;
		this.n = n;
		this.b = b;
	}

	public TestBean6(TestBean6 source) {
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
		return 100;
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TestBean6 other = (TestBean6)obj;
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
