/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

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
