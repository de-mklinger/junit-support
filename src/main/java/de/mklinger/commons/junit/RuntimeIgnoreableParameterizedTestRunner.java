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

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;

/**
 * A test runner that allows to programmatically ignore tests.
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 * @author Inspired by http://programmaticallyspeaking.blogspot.de/2008/10/run-time-equivalent-to-junits-ignore.html
 */
public class RuntimeIgnoreableParameterizedTestRunner extends Parameterized {
	public RuntimeIgnoreableParameterizedTestRunner(final Class<?> klass) throws Throwable {
		super(klass);
	}

	@Override
	public void run(final RunNotifier notifier) {
		super.run(getWrappedNotifier(notifier));
	}

	private RunNotifier getWrappedNotifier(final RunNotifier notifier) {
		return new RuntimeIgnoreRunNotifier(notifier);
	}
}

