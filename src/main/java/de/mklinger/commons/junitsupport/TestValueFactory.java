package de.mklinger.commons.junitsupport;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class TestValueFactory {
	private static final Logger LOG = LoggerFactory.getLogger(TestValueFactory.class);

	private static final int TIME_MULT = 100000;
	private static final int CREATED_ARRAY_MIN_LENGTH = 3;
	private static final int CREATED_ARRAY_MAX_LENGTH = 10;

	private final LongSupplier testValueGenerator;

	public TestValueFactory() {
		this((String)null);
	}

	public TestValueFactory(final String seedPropertyName) {
		final Random random = new Random(getSeed(seedPropertyName));
		this.testValueGenerator = () -> random.nextLong();
	}

	public TestValueFactory(final LongSupplier testValueGenerator) {
		this.testValueGenerator = testValueGenerator;
	}

	private final long getSeed(String seedPropertyName) {
		if (seedPropertyName == null) {
			seedPropertyName = getClass().getName() + ".seed";
		}

		final long value;
		final String s = System.getProperty(seedPropertyName);
		if (s != null && !s.isEmpty()) {
			value = Long.parseLong(s);
		} else {
			value = new SecureRandom().nextLong();
		}

		LOG.info("Set system property {}={} to reproduce test values", seedPropertyName, value);

		return value;
	}

	/** Get the next test value. */
	protected long getNextTestValue() {
		return testValueGenerator.getAsLong();
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
		} else if (type instanceof ParameterizedType) {
			parameterizedType = (ParameterizedType) type;
			final Type rawType = parameterizedType.getRawType();
			if (rawType instanceof Class<?>) {
				clazz = (Class<?>) rawType;
			}
		}
		if (clazz != null && clazz.isArray()) {
			final int len = createArrayLength();
			final Object array = Array.newInstance(clazz.getComponentType(), len);
			for (int i = 0; i < len; i++) {
				Array.set(array, i, createValue(clazz.getComponentType()));
			}
			return array;
		} else if (clazz != null && clazz.isEnum()) {
			try {
				final Method valuesMethod = clazz.getMethod("values");
				final Object[] values = (Object[]) valuesMethod.invoke(null);
				final int n = createUnsignedInt();
				return values[n % values.length];
			} catch (final Exception e) {
				// ignore
				LOG.debug("Error getting enum value", e);
			}
		} else if (parameterizedType != null && parameterizedType.getRawType() == Map.class) {
			final Map<Object, Object> result = new HashMap<>();
			final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
			if (actualTypeArguments.length != 2) {
				throw new IllegalStateException("Have map with actualTypeArguments.length != 2");
			}
			final int len = createArrayLength();
			for (int i = 0; i < len; i++) {
				result.put(createValue(actualTypeArguments[0]), createValue(actualTypeArguments[1]));
			}
			return Collections.unmodifiableMap(result);
		} else if (parameterizedType != null && parameterizedType.getRawType() == List.class) {
			final List<Object> result = new ArrayList<>();
			addValuesToCollection(result, parameterizedType);
			return Collections.unmodifiableList(result);
		} else if (parameterizedType != null && parameterizedType.getRawType() == Set.class) {
			final Set<Object> result = new HashSet<>();
			addValuesToCollection(result, parameterizedType);
			return Collections.unmodifiableSet(result);
		} else if (parameterizedType != null && parameterizedType.getRawType() == Collection.class) {
			final Collection<Object> result = new HashSet<>();
			addValuesToCollection(result, parameterizedType);
			return Collections.unmodifiableCollection(result);
		} else if (parameterizedType != null && parameterizedType.getRawType() == AtomicReference.class) {
			final AtomicReference<Object> result = new AtomicReference<>();
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

	private int createArrayLength() {
		return createUnsignedInt(CREATED_ARRAY_MIN_LENGTH, CREATED_ARRAY_MAX_LENGTH);
	}

	private int createUnsignedInt() {
		int n;
		do {
			n = (int) createValue(Integer.TYPE);
		} while (n == Integer.MAX_VALUE);
		n = Math.abs(n);
		assert n >= 0;
		return n;
	}

	private int createUnsignedInt(final int max) {
		assert max >= 0;
		final int n = createUnsignedInt() % (max + 1);
		assert n >= 0;
		assert n <= max;
		return n;
	}

	private int createUnsignedInt(final int min, final int max) {
		assert min >= 0;
		assert max >= 0;
		assert min <= max;
		final int n = createUnsignedInt(max - min) + min;
		assert n >= 0;
		assert n >= min;
		assert n <= max;
		return n;
	}

	private void addValuesToCollection(final Collection<Object> result, final ParameterizedType parameterizedType) {
		final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		if (actualTypeArguments.length != 1) {
			throw new IllegalStateException("Have collection with actualTypeArguments.length != 1");
		}
		final int len = createArrayLength();
		for (int i = 0; i < len; i++) {
			result.add(createValue(actualTypeArguments[0]));
		}
	}
}
