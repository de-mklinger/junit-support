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

import org.junit.internal.AssumptionViolatedException;

/**
 * Runtime test ignore util class.
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class RuntimeIgnore {
	private RuntimeIgnore() {
	}

	public static void ignore() {
		throw new AssumptionViolatedException("Test ignored");
	}

	public static void ignoreIf(final boolean shouldIgnore) {
		if (shouldIgnore) {
			throw new AssumptionViolatedException("Test ignored");
		}
	}

	public static void ignoreIfNot(final boolean shouldNotIgnore) {
		if (!shouldNotIgnore) {
			throw new AssumptionViolatedException("Test ignored");
		}
	}
}

