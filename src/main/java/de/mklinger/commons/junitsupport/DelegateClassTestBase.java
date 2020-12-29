/*
 * Copyright 2014-present mklinger GmbH - http://www.mklinger.de
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
package de.mklinger.commons.junitsupport;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests a delegate class.
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
@Ignore("Not a test")
public class DelegateClassTestBase extends TestValueFactory {
	private final Class<?> delegateType;
	private final Class<?> classUnderTest;
	private final TestMethod[] testMethods;

	/**
	 * Create a new delegate class test base.
	 *
	 * @param delegateType   The delegation target type
	 * @param classUnderTest The delegating class
	 * @param testMethods    which methods to test. If not given, all non-private
	 *                       declared instance methods of delegateType are used.
	 */
	public DelegateClassTestBase(final Class<?> delegateType, final Class<?> classUnderTest, final TestMethod... testMethods) {
		this.delegateType = delegateType;
		this.classUnderTest = classUnderTest;
		if (testMethods.length == 0) {
			this.testMethods = declaredTestMethodsFor(delegateType);
		} else {
			this.testMethods = testMethods;
		}
	}

	public static TestMethod[] declaredTestMethodsFor(final Class<?> type) {
		final Method[] methods = type.getDeclaredMethods();
		final List<TestMethod> testMethods = new ArrayList<>(methods.length);

		for (final Method method : methods) {
			if (isTestMethodCandidate(method)) {
				testMethods.add(new TestMethod(
						method.getName(),
						method.getReturnType(),
						method.getParameterTypes(),
						null));
			}
		}

		return testMethods.toArray(new TestMethod[0]);
	}

	private static boolean isTestMethodCandidate(final Method method) {
		final int modifiers = method.getModifiers();
		return !Modifier.isStatic(modifiers)
				&& !Modifier.isPrivate(modifiers)
				&& !"$jacocoInit".equals(method.getName());
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
		final boolean haveReturn = testMethod.returnType != null
				&& testMethod.returnType != Void.TYPE;

		Object returnValue = null;
		if (haveReturn) {
			returnValue = createValue(testMethod.returnType);
		}

		final Object delegate = EasyMock.createStrictMock(delegateType);
		final Method method = delegate.getClass().getMethod(testMethod.name, testMethod.parameterTypes);

		final Object[] actualParameters = getParameters(testMethod);

		method.invoke(delegate, actualParameters);

		if (haveReturn) {
			EasyMock.expectLastCall().andReturn(returnValue);
		} else {
			EasyMock.expectLastCall().andVoid();
		}

		EasyMock.replay(delegate);

		final Object instanceUnderTest = createInstanceUnderTest(delegate);
		final Method delegateMethod = instanceUnderTest.getClass().getMethod(testMethod.name, testMethod.parameterTypes);
		final Object actualReturnValue = delegateMethod.invoke(instanceUnderTest, actualParameters);

		if (haveReturn) {
			Assert.assertEquals(returnValue, actualReturnValue);
		}

		EasyMock.verify(delegate);
	}

	private Object[] getParameters(final TestMethod testMethod) {
		if (testMethod.parameters != null) {
			return testMethod.parameters;
		} else {
			final Object[] parameters = new Object[testMethod.parameterTypes.length];
			for (int i = 0; i < testMethod.parameterTypes.length; i++) {
				parameters[i] = createValue(testMethod.parameterTypes[i]);
			}
			return parameters;
		}
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
				throw new AssertionError(msg, e);
			}
		}
	}
}
