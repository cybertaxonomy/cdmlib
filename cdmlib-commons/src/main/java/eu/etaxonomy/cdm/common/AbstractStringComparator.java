/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.common;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Abstract Comparator for Strings which allows define substitution rules which
 * are applied to the String to be compared before the actual comparison takes
 * place. By this it is e.g. possible to influence the position of objects in sorted lists etc.
 * <p>
 * <b>Intended usage</b>: To allow maximum flexibility the property
 * {@link #setSubstitutionRules(Map)} should be set in the spring application
 * context.
 * 
 * @author a.kohlbecker
 * @date 24.06.2009
 */
public abstract class AbstractStringComparator<T extends Object> implements Comparator<T> {

	protected Map<Pattern, String> substitutionRules = null;

	/**
	 * Set the private field substitutionRules. The substitutionRules consist of
	 * a regular expression as key and a string to be prepended as value.
	 * 
	 * @param rules
	 */
	public void setSubstitutionRules(Map<String, String> substitutionRules) {
		this.substitutionRules = new HashMap<Pattern, String>(substitutionRules.size());
		for (String regex : substitutionRules.keySet()) {
			this.substitutionRules.put(Pattern.compile(regex), substitutionRules.get(regex));
		}

	}

	/**
	 * Applies the first matching <code>substitutionRules</code> set by
	 * {@link #setSubstitutionRules()} to the given String. A rules is applied
	 * in the following way: If the regular expression matches the given string
	 * <code>s</code> the String mapped by the regular expression is prepended
	 * to <code>s</code>.
	 * 
	 * @param s
	 * @return
	 */
	protected String applySubstitutionRules(String s) {
		if (substitutionRules == null) {
			return s;
		}
		StringBuffer sb = new StringBuffer();
		for (Pattern pattern : substitutionRules.keySet()) {
			if (pattern.matcher(s).matches()) {
				sb.append(substitutionRules.get(pattern)).append(s);
				return sb.toString();
			}

		}
		return s;
	}

}
