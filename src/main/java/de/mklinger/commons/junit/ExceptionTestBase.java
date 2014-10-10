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

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests an exception class.
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 * @param <T> The exception class
 */
@Ignore("Not a test")
public class ExceptionTestBase<T extends Throwable> {
	private static final String THE_MESSAGE = "The message";
	private final Class<T> exceptionClass;

	/**
	 * Create a new ExceptionTestBase instance.
	 * @param exceptionClass The exception class
	 */
	public ExceptionTestBase(final Class<T> exceptionClass) {
		this.exceptionClass = exceptionClass;
	}

	/**
	 * Tests constuctor.
	 */
	@Test
	public void testConstructor1() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final T exce = exceptionClass.getConstructor(new Class[0]).newInstance(new Object[0]);
		Assert.assertNull(exce.getMessage());
		Assert.assertNull(exce.getCause());
	}

	/**
	 * Tests constuctor.
	 */
	@Test
	public void testConstructor2() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final T exce = exceptionClass.getConstructor(String.class).newInstance(THE_MESSAGE);
		Assert.assertEquals(THE_MESSAGE, exce.getMessage());
		Assert.assertNull(exce.getCause());
	}

	/**
	 * Tests constuctor.
	 */
	@Test
	public void testConstructor3() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Throwable cause = new IllegalStateException();
		final T exce = exceptionClass.getConstructor(Throwable.class).newInstance(cause);
		Assert.assertEquals(cause.toString(), exce.getMessage());
		Assert.assertNotNull(exce.getCause());
		Assert.assertEquals(cause, exce.getCause());
	}

	/**
	 * Tests constuctor.
	 */
	@Test
	public void testConstructor4() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Throwable cause = new IllegalStateException();
		final T exce = exceptionClass.getConstructor(String.class, Throwable.class).newInstance(THE_MESSAGE, cause);
		Assert.assertEquals(THE_MESSAGE, exce.getMessage());
		Assert.assertNotNull(exce.getCause());
		Assert.assertEquals(cause, exce.getCause());
	}
}
