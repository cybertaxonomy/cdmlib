/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.strategy.match;

import java.util.Set;


/**
 * @author a.mueller
 * @created 31.07.2009
 */
public interface IMatchStrategy {

	public static MatchMode defaultMatchMode = MatchMode.EQUAL;
	public static MatchMode defaultCollectionMatchMode = MatchMode.IGNORE;
	public static MatchMode defaultMatchMatchMode = MatchMode.MATCH;

	public MatchMode getMatchMode(String propertyName);

	/**
	 * Sets the match mode for property propertyName
	 * @param propertyName
	 * @param mergeMode
	 * @throws MatchException
	 */
	public void setMatchMode(String propertyName, MatchMode matchMode) throws MatchException;

	/**
	 * Invokes the match check.
	 * If two objects match this method returns true, false otherwise
	 * @param <T>
	 * @param mergeFirst
	 * @param mergeSecond
	 * @throws MatchException
	 */
	public <T extends IMatchable> boolean invoke(T matchFirst, T matchSecond) throws MatchException;


	/**
	 * Returns the Matching of this match strategy
	 * @return
	 */
	public Matching getMatching();

	/**
	 * Returns the class this match strategy matches for
	 * @return
	 */
	public Class getMatchClass();

	/**
	 * Returns a map containing all fields this match strategy defines match modes for
	 * @return
	 */
	public Set<String> getMatchFieldPropertyNames();
}
