package eu.etaxonomy.cdm.database;

import org.springframework.util.Assert;

public class NamedContextHolder{

	private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

	public static void setContextKey(Object contextKey) {
		Assert.notNull(contextKey, "contextKey cannot be null");
		Assert.isInstanceOf(String.class, contextKey, "contextKey is not a String instance");
		contextHolder.set((String)contextKey);
	}

	public static String getContextKey() {
		return (String) contextHolder.get();
	}

	public static void clearContextKey() {
		contextHolder.remove();
	}

}
