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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests a delegate class.
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
@Ignore("Not a test")
public class DelegateClassTestBase {
	private final Class<?> delegateType;
	private final Class<?> classUnderTest;
	private final TestMethod[] testMethods;

	/** Create a new delegate class test base. */
	public DelegateClassTestBase(final Class<?> delegateType, final Class<?> classUnderTest, final TestMethod... testMethods) {
		this.delegateType = delegateType;
		this.classUnderTest = classUnderTest;
		this.testMethods = testMethods;
	}

	/** A test method to be delegated. */
	public static class TestMethod {
		private final String name;
		private final Class<?> returnType;
		private final Class<?>[] parameterTypes;
		private final Object[] parameters;

		/** Create a new test method. */
		public TestMethod(final String name, final Class<?> returnType, final Class<?>[] parameterTypes, final Object[] parameters) {
			this.name = name;
			this.returnType = returnType;
			this.parameterTypes = parameterTypes;
			this.parameters = parameters;
		}

		/** Create a new void test method. */
		public TestMethod(final String name, final Class<?>[] parameterTypes, final Object[] parameters) {
			this(name, null, parameterTypes, parameters);
		}
	}

	private void testDelegateMethod(final TestMethod testMethod) throws Exception {
		Object returnValue = null;
		if (testMethod.returnType != null) {
			returnValue = EasyMock.createStrictMock(testMethod.returnType);
		}
		final Object delegate = EasyMock.createStrictMock(delegateType);
		final Method method = delegate.getClass().getMethod(testMethod.name, testMethod.parameterTypes);
		method.invoke(delegate, testMethod.parameters);
		if (testMethod.returnType != null) {
			EasyMock.expectLastCall().andReturn(returnValue);
		} else {
			EasyMock.expectLastCall();
		}
		if (testMethod.returnType != null) {
			EasyMock.replay(returnValue);
		}
		EasyMock.replay(delegate);
		final Object instanceUnderTest = createInstanceUnderTest(delegate);
		final Method delegateMethod = instanceUnderTest.getClass().getMethod(testMethod.name, testMethod.parameterTypes);
		final Object actualReturnValue = delegateMethod.invoke(instanceUnderTest, testMethod.parameters);
		if (testMethod.returnType != null) {
			Assert.assertEquals(returnValue, actualReturnValue);
			EasyMock.verify(returnValue);
		}
		EasyMock.verify(delegate);
	}

	private Object createInstanceUnderTest(final Object delegate) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		final Constructor<?> constructor = classUnderTest.getConstructor(delegateType);
		final Object instanceUnderTest = constructor.newInstance(delegate);
		return instanceUnderTest;
	}

	/** Test all configured delegate methods. */
	@Test
	public void testDelegateMethods() throws Exception {
		for (final TestMethod testMethod : testMethods) {
			try {
				testDelegateMethod(testMethod);
			} catch (final Exception e) {
				final String msg = "Error testing delegate method " + testMethod.name + "(" + Arrays.toString(testMethod.parameterTypes) + "): " + e.toString();
				System.err.println(msg);
				e.printStackTrace();
				Assert.fail(msg);
			}
		}
	}
}
