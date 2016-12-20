/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.lsid.impl;

/**
 * simple struct for patterns
 * <p>
 * <p>
 */
public class Pattern {
	    public static final Pattern NO_LSID_PATTERN = new Pattern();
	    public static final String WILDCARD = "*";
	    public static final String SEPARATOR = ":";
		
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
	        if (!(other instanceof Pattern)) {
	        	return false;
	        }

	        final Pattern pattern = (Pattern) other;

	        if (!toString().equals(pattern.toString())) {
	        	return false;
	        }

	        return true;
		}
		
		public int hashCode() {
			return this.toString().hashCode();
		}

		private String authority;
		private String namespace;

		public String toString() {
			if (authority == null)
				return "NO_LSID_PATTERN";
			return authority + Pattern.SEPARATOR + namespace;
		}

		public String getAuthority() {
			return authority;
		}

		public String getNamespace() {
			return namespace;
		}

		public void setAuthority(String authority) {
			this.authority = authority.toLowerCase();
		}

		public void setNamespace(String namespace) {
			this.namespace = namespace.toLowerCase();
		}
}
