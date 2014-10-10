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

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

public class RuntimeIgnoreRunNotifier extends RunNotifier {
	private final RunNotifier delegate;

	public RuntimeIgnoreRunNotifier(final RunNotifier delegate) {
		this.delegate = delegate;
	}

	@Override
	public void addListener(final RunListener listener) {
		delegate.addListener(listener);
	}

	@Override
	public void removeListener(final RunListener listener) {
		delegate.removeListener(listener);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public void fireTestRunStarted(final Description description) {
		delegate.fireTestRunStarted(description);
	}

	@Override
	public void fireTestRunFinished(final Result result) {
		delegate.fireTestRunFinished(result);
	}

	@Override
	public void fireTestStarted(final Description description) throws StoppedByUserException {
		delegate.fireTestStarted(description);
	}

	@Override
	public void fireTestFailure(final Failure failure) {
		if (failure.getException() != null && failure.getException().getClass() == RuntimeIgnore.RuntimeIgnoreException.class) {
			fireTestIgnored(failure.getDescription());
		} else {
			delegate.fireTestFailure(failure);
		}
	}

	@Override
	public void fireTestAssumptionFailed(final Failure failure) {
		delegate.fireTestAssumptionFailed(failure);
	}

	@Override
	public boolean equals(final Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public void fireTestIgnored(final Description description) {
		delegate.fireTestIgnored(description);
	}

	@Override
	public void fireTestFinished(final Description description) {
		delegate.fireTestFinished(description);
	}

	@Override
	public void pleaseStop() {
		delegate.pleaseStop();
	}

	@Override
	public void addFirstListener(final RunListener listener) {
		delegate.addFirstListener(listener);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}