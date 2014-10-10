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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic bean test.
 * @param <T> The bean type
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
@Ignore("Not a test")
@RunWith(RuntimeIgnoreableTestRunner.class)
public class BeanTestBase<T> {
	/** Default constructor parameters. */
	protected static final ConstructorParameters DEFAULT_CONSTRUCTOR_PARAMETERS = new ConstructorParameters(new Class<?>[0], new String[0]);

	private static final Logger LOG = LoggerFactory.getLogger(BeanTestBase.class);

	private static final String SETTER_PREFIX = "set";
	private static final int TIME_MULT = 100000;
	private static final float DELTA = 0.0000000000001f;
	private static final int CREATED_ARRAY_LENGTH = 5;
	private static final String CLASS_AND_FIELD_SEPERATOR = "#";

	private final Class<T> beanClass;
	private long nextValue = 1;
	private Map<Class<?>[], Object[]> allConstructorPropertyValues;

	/**
	 * Constructor parameter descriptor.
	 * @author Marc Klinger - marc[at]nightlabs[dot]de - klingerm
	 */
	protected static class ConstructorParameters {
		private final Class<?>[] types;
		private final String[] propertyNames;

		/**
		 * Create a new instance.
		 * @param types The types
		 * @param propertyNames The property names
		 */
		public ConstructorParameters(final Class<?>[] types, final String[] propertyNames) {
			if (types == null || propertyNames == null || types.length != propertyNames.length) {
				throw new IllegalArgumentException();
			}
			this.types = types;
			this.propertyNames = propertyNames;
		}

		/**
		 * Get the types.
		 * @return the types
		 */
		public Class<?>[] getTypes() {
			return types;
		}

		/**
		 * Get the propertyNames.
		 * @return the propertyNames
		 */
		public String[] getPropertyNames() {
			return propertyNames;
		}
	}

	/**
	 * Create a new BeanTestBase instance.
	 * @param beanClass The bean class
	 */
	public BeanTestBase(final Class<T> beanClass) {
		this.beanClass = beanClass;
	}

	private static void addAllFields(final Class<?> clazz, final List<Field> allFields) {
		allFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		final Class<?> superclass = clazz.getSuperclass();
		if (superclass != Object.class) {
			addAllFields(superclass, allFields);
		}
	}

	/**
	 * Get all fields for the bean class. This also includes fields from parent classes whether or not they are visible.
	 * @return All fields
	 */
	private Field[] getAllFields() {
		final List<Field> allFields = new ArrayList<Field>();
		addAllFields(beanClass, allFields);
		return allFields.toArray(new Field[allFields.size()]);
	}

	private static void addAllSetters(final Class<?> clazz, final List<Method> allSetters) {
		final Method[] methods = clazz.getDeclaredMethods();
		for (final Method method : methods) {
			if (method.getName().startsWith(SETTER_PREFIX) && method.getParameterTypes().length == 1) {
				allSetters.add(method);
			}
		}
		final Class<?> superclass = clazz.getSuperclass();
		if (superclass != Object.class) {
			addAllSetters(superclass, allSetters);
		}
	}

	/**
	 * Get all setter methods for the bean class. This also includes setters from parent classes whether or not they are visible.
	 * @return All setters
	 */
	private Method[] getAllSetters() {
		final List<Method> allSetters = new ArrayList<Method>();
		addAllSetters(beanClass, allSetters);
		return allSetters.toArray(new Method[allSetters.size()]);
	}

	/**
	 * Get all bean property names.
	 * @see #isBeanFieldCandidate(Field)
	 * @return The bean property names
	 */
	protected Collection<String> getBeanPropertyNames() {
		final Collection<String> beanPropertyNames = new HashSet<String>();
		final Field[] allFields = getAllFields();
		for (final Field field : allFields) {
			if (isBeanFieldCandidate(field)) {
				beanPropertyNames.add(field.getName());
			}
		}
		final Method[] allSetters = getAllSetters();
		for (final Method setter : allSetters) {
			String propertyName = setter.getName().substring(SETTER_PREFIX.length());
			if (propertyName.length() == 1) {
				propertyName = propertyName.toLowerCase();
			} else {
				propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
			}
			final Method getter = getGetter(propertyName);
			if (getter != null && !isIgnoreProperty(propertyName)) {
				beanPropertyNames.add(propertyName);
			}
		}
		return beanPropertyNames;
	}

	/**
	 * Get all existing fields for properties of the bean.
	 * @return The fields
	 */
	protected Collection<Field> getBeanFields() {
		final Collection<String> beanPropertyNames = getBeanPropertyNames();
		final Collection<Field> beanFields = new ArrayList<Field>(beanPropertyNames.size());
		for (final String propertyName : beanPropertyNames) {
			Field field;
			try {
				field = getDeclaredField(propertyName);
				field.setAccessible(true);
				beanFields.add(field);
			} catch (final NoSuchFieldException e) {
				// ignore
				LOG.debug("getBeanFields()", e);
			}
		}
		return beanFields;
	}

	private Field getDeclaredField(final String propertyName) throws NoSuchFieldException {
		return getDeclaredFieldRecursive(beanClass, propertyName);
	}

	private Field getDeclaredFieldRecursive(final Class<?> startClass, final String propertyName) throws NoSuchFieldException {
		try {
			return startClass.getDeclaredField(propertyName);
		} catch (final NoSuchFieldException e) {
			final Class<?> superclass = startClass.getSuperclass();
			if (superclass != Object.class) {
				return getDeclaredFieldRecursive(superclass, propertyName);
			} else {
				throw new NoSuchFieldException("No field found for property " + propertyName);
			}
		}
	}

	/** Get the next test value. */
	protected long getNextTestValue() {
		final long value = nextValue;
		nextValue++;
		return value;
	}

	/**
	 * Create a test value for the given type.
	 * @param type The type
	 * @return The test value
	 */
	protected Object createValue(final Type type) {
		Class<?> clazz = null;
		ParameterizedType parameterizedType = null;
		if (type instanceof Class<?>) {
			clazz = (Class<?>) type;
		} else
			if (type instanceof ParameterizedType) {
				parameterizedType = (ParameterizedType) type;
				final Type rawType = parameterizedType.getRawType();
				if (rawType instanceof Class<?>) {
					clazz = (Class<?>) rawType;
				}
			}
		if (clazz != null && clazz.isArray()) {
			final Object array = Array.newInstance(clazz.getComponentType(), CREATED_ARRAY_LENGTH);
			for (int i = 0; i < CREATED_ARRAY_LENGTH; i++) {
				Array.set(array, i, createValue(clazz.getComponentType()));
			}
			return array;
		} else if (clazz != null && clazz.isEnum()) {
			try {
				final Method valuesMethod = clazz.getMethod("values", new Class<?>[0]);
				final Object[] values = (Object[]) valuesMethod.invoke(null, new Object[0]);
				final int n = (Integer) createValue(Integer.TYPE);
				return values[n % values.length];
			} catch (final Exception e) {
				// ignore
				LOG.debug("Error getting enum value", e);
			}
		} else if (parameterizedType != null && parameterizedType.getRawType() == Map.class) {
			final Map<Object, Object> result = new HashMap<Object, Object>();
			final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
			if (actualTypeArguments.length != 2) {
				throw new IllegalStateException("Have map with actualTypeArguments.length != 2");
			}
			for (int i = 0; i < CREATED_ARRAY_LENGTH; i++) {
				result.put(createValue(actualTypeArguments[0]), createValue(actualTypeArguments[1]));
			}
			return Collections.unmodifiableMap(result);
		} else if (parameterizedType != null && parameterizedType.getRawType() == List.class) {
			final List<Object> result = new ArrayList<Object>();
			addValuesToCollection(result, parameterizedType);
			return Collections.unmodifiableList(result);
		} else if (parameterizedType != null && parameterizedType.getRawType() == Set.class) {
			final Set<Object> result = new HashSet<Object>();
			addValuesToCollection(result, parameterizedType);
			return Collections.unmodifiableSet(result);
		} else if (parameterizedType != null && parameterizedType.getRawType() == Collection.class) {
			final Collection<Object> result = new HashSet<Object>();
			addValuesToCollection(result, parameterizedType);
			return Collections.unmodifiableCollection(result);
		} else if (parameterizedType != null && parameterizedType.getRawType() == AtomicReference.class) {
			final AtomicReference<Object> result = new AtomicReference<Object>();
			final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
			if (actualTypeArguments.length != 1) {
				throw new IllegalStateException("Have parameterizedType with actualTypeArguments.length != 1");
			}
			result.set(createValue(actualTypeArguments[0]));
			return result;
		} else if (type == Long.TYPE || type == Long.class) {
			return getNextTestValue();
		} else if (type == Integer.TYPE || type == Integer.class) {
			return (int) getNextTestValue();
		} else if (type == Character.TYPE || type == Character.class) {
			return (char) getNextTestValue();
		} else if (type == Short.TYPE || type == Short.class) {
			return (short) getNextTestValue();
		} else if (type == Byte.TYPE || type == Byte.class) {
			return (byte) getNextTestValue();
		} else if (type == Float.TYPE || type == Float.class) {
			return (float) getNextTestValue();
		} else if (type == Double.TYPE || type == Double.class) {
			return (double) getNextTestValue();
		} else if (type == Boolean.TYPE || type == Boolean.class) {
			return (getNextTestValue() % 2) == 0;
		} else if (type == String.class) {
			return Long.toHexString(getNextTestValue());
		} else if (type == Date.class) {
			return new Date(System.currentTimeMillis() - getNextTestValue() * TIME_MULT);
		} else if (type == Object.class) {
			return createValue(String.class);
		}
		throw new UnsupportedOperationException("Test " + getClass() + " must override createValue(Type type) and return a value for type " + type);
	}

	private void addValuesToCollection(final Collection<Object> result, final ParameterizedType parameterizedType) {
		final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		if (actualTypeArguments.length != 1) {
			throw new IllegalStateException("Have collection with actualTypeArguments.length != 1");
		}
		for (int i = 0; i < CREATED_ARRAY_LENGTH; i++) {
			result.add(createValue(actualTypeArguments[0]));
		}
	}

	/**
	 * Set a value for the given field using a setter if possible.
	 */
	protected Object fillProperty(final T entity, final String propertyName) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		final Object value;
		try {
			value = createValue(getPropertyGenericType(propertyName));
		} catch (final UnsupportedOperationException e) {
			throw new UnsupportedOperationException("Could not create value for property '" + propertyName + "': " + e.getMessage(), e);
		}
		return fillProperty(entity, propertyName, value);
	}

	private Object fillProperty(final T entity, final String propertyName, final Object value) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		final Method setter = getSetter(getPropertyType(propertyName), propertyName);
		if (setter != null) {
			setter.invoke(entity, value);
		} else {
			final Field field = getDeclaredFieldRecursive(beanClass, propertyName);
			field.setAccessible(true);
			field.set(entity, value);
		}
		return value;
	}

	private Method getSetter(final Class<?> propertyType, final String propertyName) {
		String upperName;
		if (propertyName.length() == 1) {
			upperName = propertyName.toUpperCase();
		} else {
			upperName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
		}
		final String setterName = SETTER_PREFIX + upperName;
		try {
			return getMethod(beanClass, setterName, new Class<?>[] {propertyType});
		} catch (final NoSuchMethodException e) {
			LOG.warn("No setter found for property: " + beanClass.getName() + CLASS_AND_FIELD_SEPERATOR
					+ propertyName);
			return null;
		}
	}

	private Method getGetter(final String propertyName) {
		String upperName;
		if (propertyName.length() == 1) {
			upperName = propertyName.toUpperCase();
		} else {
			upperName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
		}
		String getterName = "get" + upperName;
		try {
			return getMethod(beanClass, getterName, new Class<?>[0]);
		} catch (final NoSuchMethodException e) {
			getterName = "is" + upperName;
			try {
				return getMethod(beanClass, getterName, new Class<?>[0]);
			} catch (final NoSuchMethodException e2) {
				LOG.warn("No getter found for property: " + beanClass.getName() + CLASS_AND_FIELD_SEPERATOR
						+ propertyName);
				return null;
			}
		}
	}

	private Method getMethod(final Class<?> clazz, final String name, final Class<?>... parameterTypes) throws NoSuchMethodException {
		try {
			return clazz.getMethod(name, parameterTypes);
		} catch (final NoSuchMethodException e) {
			final Class<?> superclass = clazz.getSuperclass();
			if (superclass != null) {
				return getMethod(superclass, name, parameterTypes);
			}
		}
		throw new NoSuchMethodException(name);
	}

	/**
	 * Get the value of the given field using a getter if possible.
	 */
	protected Object getFieldValue(final T entity, final String propertyName) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		Object fieldValue;
		final Method getter = getGetter(propertyName);
		if (getter != null) {
			getter.setAccessible(true);
			fieldValue = getter.invoke(entity, new Object[0]);
		} else {
			final Field field = getDeclaredField(propertyName);
			field.setAccessible(true);
			fieldValue = field.get(entity);
		}
		return fieldValue;
	}

	/**
	 * Get the property names to ignore in this test.
	 * @return The property names to ignore.
	 */
	protected String[] getIgnorePropertyNames() {
		return null;
	}

	private boolean isIgnoreProperty(final String propertyName) {
		final String[] ignorePropertyNames = getIgnorePropertyNames();
		if (ignorePropertyNames == null) {
			return false;
		}
		for (final String ignoreName : ignorePropertyNames) {
			if (ignoreName != null && ignoreName.equals(propertyName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Is the given field a bean field candidate?
	 * @param field The field
	 * @return <code>true</code> if the given field is a bean field candidate
	 */
	protected boolean isBeanFieldCandidate(final Field field) {
		return ((field.getModifiers() & Modifier.STATIC) == 0 && (field.getModifiers() & Modifier.TRANSIENT) == 0) && !isIgnoreProperty(field.getName());
	}

	/**
	 * Create a bean instance using this parameter constructor.
	 * Property values are reused on subsequent calls to this method.
	 * @return The instance
	 */
	protected T createInstance(final ConstructorParameters constructorParameters) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		final Class<?>[] types = constructorParameters.getTypes();
		final Constructor<T> constructor = beanClass.getDeclaredConstructor(types);
		constructor.setAccessible(true);
		if (allConstructorPropertyValues == null) {
			allConstructorPropertyValues = new HashMap<Class<?>[], Object[]>();
		}
		Object[] propertyValues = allConstructorPropertyValues.get(types);
		if (propertyValues == null) {
			final Type[] genericTypes = constructor.getGenericParameterTypes();
			assert genericTypes.length == types.length;
			propertyValues = new Object[genericTypes.length];
			for (int i = 0; i < genericTypes.length; i++) {
				propertyValues[i] = createValue(genericTypes[i]);
			}
			allConstructorPropertyValues.put(types, propertyValues);
		}
		return constructor.newInstance(propertyValues);
	}

	/**
	 * Get all constructor parameter sets.
	 * @return The constructor parameters array
	 */
	protected ConstructorParameters[] getConstructorParameters() {
		return new ConstructorParameters[] { DEFAULT_CONSTRUCTOR_PARAMETERS };
	}

	/**
	 * Assert equals with support for Double/double and Float/float.
	 */
	protected static void assertEquals(final String message, final Object expected, final Object actual) {
		if (expected != null && actual != null) {
			if ((expected.getClass() == Double.TYPE || expected.getClass() == Double.class) && (actual.getClass() == Double.TYPE || actual.getClass() == Double.class)) {
				Assert.assertEquals(((Double) expected).doubleValue(), ((Double) actual).doubleValue(), DELTA);
				return;
			} else if ((expected.getClass() == Float.TYPE || expected.getClass() == Float.class) && (actual.getClass() == Float.TYPE || actual.getClass() == Float.class)) {
				Assert.assertEquals(((Float) expected).floatValue(), ((Float) actual).floatValue(), DELTA);
				return;
			} else if (Collection.class.isAssignableFrom(expected.getClass()) && !List.class.isAssignableFrom(expected.getClass())) {
				assertEquals(message, (Collection<?>)expected, (Collection<?>)actual);
				return;
			}
		}
		Assert.assertEquals(message, expected, actual);
	}

	protected static void assertEquals(final String message, final Collection<?> expected, final Collection<?> actual) {
		if (expected == actual) {
			return;
		}
		if (expected == null) {
			Assert.assertNull(message, actual);
			return;
		}
		Assert.assertNotNull(message, actual);
		Assert.assertEquals(message, expected.size(), actual.size());
		Assert.assertTrue(message, actual.containsAll(expected));
	}

	private Type getPropertyGenericType(final String propertyName) {
		try {
			final Field field = getDeclaredField(propertyName);
			return field.getGenericType();
		} catch (final NoSuchFieldException e) {
			final Method getter = getGetter(propertyName);
			if (getter == null) {
				throw new IllegalStateException(String.format("Property '%s' not found as field or getter", propertyName));
			}
			return getter.getGenericReturnType();
		}
	}

	private Class<?> getPropertyType(final String propertyName) {
		try {
			final Field field = getDeclaredField(propertyName);
			return field.getType();
		} catch (final NoSuchFieldException e) {
			final Method getter = getGetter(propertyName);
			if (getter == null) {
				throw new IllegalStateException(String.format("Property '%s' not found as field org getter", propertyName));
			}
			return getter.getReturnType();
		}
	}

	private static Set<String> toSet(final String[] s) {
		Set<String> set = null;
		if (s != null && s.length > 0) {
			set = new HashSet<String>(s.length);
			Collections.addAll(set, s);
		}
		return set;
	}

	/**
	 * @return The property name to value map
	 */
	protected Map<String, Object> fillBean(final T bean, final String[] propertiesToOmit) throws IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		final Set<String> propsToOmit = toSet(propertiesToOmit);
		final Map<String, Object> result = new HashMap<String, Object>();
		final Collection<String> beanPropertyNames = getBeanPropertyNames();
		for (final String propertyName : beanPropertyNames) {
			if (propsToOmit == null || !propsToOmit.contains(propertyName)) {
				final Object value = fillProperty(bean, propertyName);
				result.put(propertyName, value);
			}
		}
		return result;
	}

	private boolean declaresEquals() throws NoSuchMethodException {
		return beanClass.getMethod("equals", Object.class).getDeclaringClass() != Object.class;
	}

	private boolean declaresHashCode() throws NoSuchMethodException {
		return beanClass.getMethod("hashCode", new Class<?>[0]).getDeclaringClass() != Object.class;
	}

	/**
	 * Property test for all constructors.
	 */
	@Test
	public void propertyTestForAllConstructors() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		final ConstructorParameters[] allConstructorParameters = getConstructorParameters();

		for (final ConstructorParameters constructorParameters : allConstructorParameters) {
			LOG.info("Testing properties for constructor with parameter types " + Arrays.toString(constructorParameters.getTypes()));
			final T bean = createInstance(constructorParameters);
			final String[] constructorPropertyNames = constructorParameters.getPropertyNames();
			final Map<String, Object> propertyValues = fillBean(bean, constructorPropertyNames);

			// test constructor values
			final Object[] constructorPropertyValues = allConstructorPropertyValues.get(constructorParameters.getTypes());
			for (int i = 0; i < constructorPropertyValues.length; i++) {
				final Object expectedValue = constructorPropertyValues[i];
				final String propertyName = constructorPropertyNames[i];
				if (!isIgnoreProperty(propertyName)) {
					final Object actualValue = getFieldValue(bean, propertyName);
					assertEquals("Constructor property value " + propertyName, expectedValue, actualValue);
				}
			}

			// test property values
			final Set<String> propsToOmit = toSet(constructorPropertyNames);
			final Collection<String> propertyNames = getBeanPropertyNames();
			for (final String propertyName : propertyNames) {
				if (propsToOmit == null || !propsToOmit.contains(propertyName)) {
					final Object expectedValue = propertyValues.get(propertyName);
					final Object actualValue = getFieldValue(bean, propertyName);
					assertEquals("Property value " + propertyName, expectedValue, actualValue);
				}
			}
		}
	}

	/**
	 * Test for copy constructor, if any, and equals.
	 */
	@Test
	public void copyConstructorEqualsTest() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		if (declaresEquals()) {
			final Constructor<T> copyConstructor = getCopyConstructor();
			if (copyConstructor != null) {
				final ConstructorParameters[] allConstructorParameters = getConstructorParameters();
				for (final ConstructorParameters constructorParameters : allConstructorParameters) {
					LOG.info("Testing equals (equality) for constructor with parameter types " + Arrays.toString(constructorParameters.getTypes()));
					final String[] constructorPropertyNames = constructorParameters.getPropertyNames();
					final T bean1 = createInstance(constructorParameters);
					fillBean(bean1, constructorPropertyNames);
					final T bean2 = copyConstructor.newInstance(bean1);
					Assert.assertEquals("Bean created with copy constructor is not equal to original bean", bean1, bean2);
				}
			} else {
				LOG.info(String.format("Skipping copy constructor / equals (values) test as '%s' does not implement a copy constructor.", beanClass.getName()));
				// ignore this test
				RuntimeIgnore.ignore();
			}
		} else {
			LOG.info(String.format("Skipping copy constructor / equals (values) test as '%s' does not implement equals.", beanClass.getName()));
			// ignore this test
			RuntimeIgnore.ignore();
		}
	}

	/**
	 * Test for copy constructor, if any.
	 */
	@Test
	public void copyConstructorValuesTest() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		final Constructor<T> copyConstructor = getCopyConstructor();
		if (copyConstructor != null) {
			final ConstructorParameters[] allConstructorParameters = getConstructorParameters();
			for (final ConstructorParameters constructorParameters : allConstructorParameters) {
				LOG.info("Testing properties for copy constructor vs. constructor with parameter types " + Arrays.toString(constructorParameters.getTypes()));
				final T bean = createInstance(constructorParameters);
				final String[] constructorPropertyNames = constructorParameters.getPropertyNames();
				final Map<String, Object> propertyValues = fillBean(bean, constructorPropertyNames);
				final T copiedBean = copyConstructor.newInstance(bean);

				// test constructor values
				final Object[] constructorPropertyValues = allConstructorPropertyValues.get(constructorParameters.getTypes());
				for (int i = 0; i < constructorPropertyValues.length; i++) {
					final Object expectedValue = constructorPropertyValues[i];
					final String propertyName = constructorPropertyNames[i];
					if (!isIgnoreProperty(propertyName)) {
						final Object actualValue = getFieldValue(copiedBean, propertyName);
						assertEquals("Constructor property value " + propertyName, expectedValue, actualValue);
					}
				}

				// test property values
				final Set<String> propsToOmit = toSet(constructorPropertyNames);
				final Collection<String> propertyNames = getBeanPropertyNames();
				for (final String propertyName : propertyNames) {
					if (propsToOmit == null || !propsToOmit.contains(propertyName)) {
						final Object expectedValue = propertyValues.get(propertyName);
						final Object actualValue = getFieldValue(copiedBean, propertyName);
						assertEquals("Property value " + propertyName, expectedValue, actualValue);
					}
				}
			}
		} else {
			LOG.info(String.format("Skipping copy constructor test as '%s' does not implement a copy constructor.", beanClass.getName()));
			// ignore this test
			RuntimeIgnore.ignore();
		}
	}

	/**
	 * Test for copy constructor, if any, and equals.
	 */
	@Test
	public void copyConstructorEmptyEqualsTest() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		if (declaresEquals()) {
			final Constructor<T> copyConstructor = getCopyConstructor();
			if (copyConstructor != null) {
				final ConstructorParameters[] allConstructorParameters = getConstructorParameters();
				for (final ConstructorParameters constructorParameters : allConstructorParameters) {
					LOG.info("Testing equals (equality) for constructor with parameter types " + Arrays.toString(constructorParameters.getTypes()));
					final T bean1 = createInstance(constructorParameters);
					final T bean2 = copyConstructor.newInstance(bean1);
					Assert.assertEquals("Bean created with copy constructor is not equal to original bean", bean1, bean2);
				}
			} else {
				LOG.info(String.format("Skipping copy constructor / equals (values) test as '%s' does not implement a copy constructor.", beanClass.getName()));
				// ignore this test
				RuntimeIgnore.ignore();
			}
		} else {
			LOG.info(String.format("Skipping copy constructor / equals (values) test as '%s' does not implement equals.", beanClass.getName()));
			// ignore this test
			RuntimeIgnore.ignore();
		}
	}

	/**
	 * Test for copy constructor, if any.
	 */
	@Test
	public void copyConstructorEmptyValuesTest() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		final Constructor<T> copyConstructor = getCopyConstructor();
		if (copyConstructor != null) {
			final ConstructorParameters[] allConstructorParameters = getConstructorParameters();
			for (final ConstructorParameters constructorParameters : allConstructorParameters) {
				LOG.info("Testing properties for copy constructor vs. constructor with parameter types " + Arrays.toString(constructorParameters.getTypes()));
				final T bean = createInstance(constructorParameters);
				final String[] constructorPropertyNames = constructorParameters.getPropertyNames();
				final T copiedBean = copyConstructor.newInstance(bean);

				// test property values
				final Set<String> propsToOmit = toSet(constructorPropertyNames);
				final Collection<String> propertyNames = getBeanPropertyNames();
				for (final String propertyName : propertyNames) {
					if (propsToOmit == null || !propsToOmit.contains(propertyName)) {
						final Object expectedValue = getFieldDefaultValue(propertyName);
						final Object actualValue = getFieldValue(copiedBean, propertyName);
						assertEquals("Property value " + propertyName, expectedValue, actualValue);
					}
				}
			}
		} else {
			LOG.info(String.format("Skipping copy constructor test as '%s' does not implement a copy constructor.", beanClass.getName()));
			// ignore this test
			RuntimeIgnore.ignore();
		}
	}

	private Constructor<T> getCopyConstructor() {
		try {
			return beanClass.getConstructor(beanClass);
		} catch (final NoSuchMethodException e) {
			return null;
		}
	}

	protected Object getFieldDefaultValue(final String propertyName) {
		final Type type = getPropertyType(propertyName);

		if (type == Long.TYPE) {
			return 0L;
		} else if (type == Integer.TYPE) {
			return 0;
		} else if (type == Character.TYPE) {
			return (char) 0;
		} else if (type == Short.TYPE) {
			return (short) 0;
		} else if (type == Byte.TYPE) {
			return (byte) 0;
		} else if (type == Float.TYPE) {
			return 0.0f;
		} else if (type == Double.TYPE) {
			return 0.0d;
		} else if (type == Boolean.TYPE) {
			return false;
		}

		return null;
	}

	/**
	 * Calling toString() on an empty bean.
	 */
	@Test
	public void toStringTestForAllConstructorsEmpty() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		final ConstructorParameters[] allConstructorParameters = getConstructorParameters();
		for (final ConstructorParameters constructorParameters : allConstructorParameters) {
			LOG.info("Testing properties for constructor with parameter types " + Arrays.toString(constructorParameters.getTypes()));
			final T bean = createInstance(constructorParameters);
			Assert.assertNotNull(bean.toString());
		}
	}

	/**
	 * Calling toString() on an filled bean.
	 */
	@Test
	public void toStringTestForAllConstructorsFilled() throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		final ConstructorParameters[] allConstructorParameters = getConstructorParameters();
		for (final ConstructorParameters constructorParameters : allConstructorParameters) {
			LOG.info("Testing properties for constructor with parameter types " + Arrays.toString(constructorParameters.getTypes()));
			final T bean = createInstance(constructorParameters);
			final String[] constructorPropertyNames = constructorParameters.getPropertyNames();
			fillBean(bean, constructorPropertyNames);
			Assert.assertNotNull(bean.toString());
		}
	}

	/**
	 * Test the equals method with identical instances.
	 */
	@Test
	public void equalsIdentityTest() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
		if (declaresEquals()) {
			final ConstructorParameters[] allConstructorParameters = getConstructorParameters();
			for (final ConstructorParameters constructorParameters : allConstructorParameters) {
				LOG.info("Testing equals (identity) for constructor with parameter types " + Arrays.toString(constructorParameters.getTypes()));
				final T bean = createInstance(constructorParameters);
				final String[] constructorPropertyNames = constructorParameters.getPropertyNames();
				fillBean(bean, constructorPropertyNames);
				Assert.assertEquals("Same beans are not equal", bean, bean);
			}
		} else {
			LOG.info(String.format("Skipping equals (identity) test as '%s' does not implement equals.", beanClass.getName()));
			// ignore this test
			RuntimeIgnore.ignore();
		}
	}

	/**
	 * Test the equals method with equal values.
	 */
	@Test
	public void equalsValuesTest() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
		if (declaresEquals()) {
			final ConstructorParameters[] allConstructorParameters = getConstructorParameters();
			for (final ConstructorParameters constructorParameters : allConstructorParameters) {
				LOG.info("Testing equals (equality) for constructor with parameter types " + Arrays.toString(constructorParameters.getTypes()));
				final String[] constructorPropertyNames = constructorParameters.getPropertyNames();
				final T bean1 = createInstance(constructorParameters);
				final Map<String, Object> values = fillBean(bean1, constructorPropertyNames);
				final T bean2 = createInstance(constructorParameters);
				for (final Map.Entry<String, Object> e : values.entrySet()) {
					fillProperty(bean2, e.getKey(), e.getValue());
				}
				Assert.assertEquals("Beans with same property values are not equal", bean1, bean2);
			}
		} else {
			LOG.info(String.format("Skipping equals (values) test as '%s' does not implement equals.", beanClass.getName()));
			// ignore this test
			RuntimeIgnore.ignore();
		}
	}

	/**
	 * Test the hashCode method.
	 */
	@Test
	public void hashCodeTest() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
		if (declaresHashCode()) {
			final ConstructorParameters[] allConstructorParameters = getConstructorParameters();
			for (final ConstructorParameters constructorParameters : allConstructorParameters) {
				LOG.info("Testing hashCode for constructor with parameter types " + Arrays.toString(constructorParameters.getTypes()));
				final String[] constructorPropertyNames = constructorParameters.getPropertyNames();
				final T bean1 = createInstance(constructorParameters);
				final Map<String, Object> values = fillBean(bean1, constructorPropertyNames);
				final T bean2 = createInstance(constructorParameters);
				for (final Map.Entry<String, Object> e : values.entrySet()) {
					fillProperty(bean2, e.getKey(), e.getValue());
				}
				Assert.assertEquals("Beans with same property values do not have same hashCode", bean1.hashCode(), bean2.hashCode());
			}
		} else {
			LOG.info("Skipping hashCode test as " + beanClass + " does not implement hashCode.");
			// ignore this test
			RuntimeIgnore.ignore();
		}
	}
}
