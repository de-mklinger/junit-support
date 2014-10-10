/* Copyright 2014 mklinger GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mklinger.commons.junit;

/**
 * Runtime test ignore util class.
 * @see RuntimeIgnoreableTestRunner
 * @see RuntimeIgnoreableParameterizedTestRunner
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 * @author Inspired by http://programmaticallyspeaking.blogspot.de/2008/10/run-time-equivalent-to-junits-ignore.html
 */
public final class RuntimeIgnore {
	static class RuntimeIgnoreException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		RuntimeIgnoreException() {
			super("If you see this, then you have forgot to run the test-case with the " + RuntimeIgnoreableTestRunner.class.getName() + " JUnit runner.");
		}
	}

	private RuntimeIgnore() {
	}

	public static void ignore() {
		throw new RuntimeIgnoreException();
	}

	public static void ignoreIf(final boolean shouldIgnore) {
		if (shouldIgnore) {
			throw new RuntimeIgnoreException();
		}
	}

	public static void ignoreIfNot(final boolean shouldNotIgnore) {
		if (!shouldNotIgnore) {
			throw new RuntimeIgnoreException();
		}
	}
}

