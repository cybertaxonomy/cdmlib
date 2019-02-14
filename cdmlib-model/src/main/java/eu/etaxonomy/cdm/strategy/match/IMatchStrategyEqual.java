/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.strategy.match;

/**
 * @author a.mueller
 * @since 31.07.2009
 */
public interface IMatchStrategyEqual extends IMatchStrategy{


//	public MatchMode getMatchMode(String propertyName);

	/**
	 * Sets the match mode for property propertyName
	 * @param propertyName
	 * @param mergeMode
	 * @throws MatchException
	 * @see {@link #setMatchMode(String, MatchMode, IMatchStrategy)}
	 */
	@Override
    public void setMatchMode(String propertyName, MatchMode matchMode) throws MatchException;


    /**
     * Sets the match mode for property propertyName using the given match strategy.
     * The match strategy has no effect if {@link MatchMode match mode} is none of
     * the 4 MATCH* match modes like {@link MatchMode#MATCH} or {@link MatchMode#MATCH_REQUIRED}.
     * @param propertyName
     * @param matchStrategy
     * @throws MatchException
     * @see {@link #setMatchMode(String, MatchMode)}
     */
    @Override
    void setMatchMode(String propertyName, MatchMode matchMode, IMatchStrategy matchStrategy) throws MatchException;

	/**
	 * Invokes the match check.
	 * If two objects match this method returns true, false otherwise
	 * @param <T>
	 * @param mergeFirst
	 * @param mergeSecond
	 * @throws MatchException
	 */
	@Override
    public <T extends IMatchable> MatchResult invoke(T matchFirst, T matchSecond) throws MatchException;


	/**
	 * Returns the Matching of this match strategy
	 * @return
	 */
	@Override
    public Matching getMatching();
//
//	/**
//	 * Returns the class this match strategy matches for
//	 * @return
//	 */
//	public Class getMatchClass();
//
//	/**
//	 * Returns a map containing all fields this match strategy defines match modes for
//	 * @return
//	 */
//	@Override
//    public Set<String> getMatchFieldPropertyNames();
//
//    /**
//     * Sets the default match mode
//     * @param defaultMatchMode
//     */
//    void setDefaultMatchMode(MatchMode defaultMatchMode);
//
//    /**
//     * Sets the default match mode for collection attributes.
//     * @param defaultCollectionMatchMode
//     */
//    void setDefaultCollectionMatchMode (MatchMode defaultCollectionMatchMode);
//
//    /**
//     * Sets the default match mode for all attributes that again need matching.
//     * @param defaultMatchMatchMode
//     */
//    void setDefaultMatchMatchMode(MatchMode defaultMatchMatchMode);
//
//    /**
//     * @return the default match mode
//     */
//    public MatchMode getDefaultMatchMode();
//
//    /**
//     * @return the default match mode for collections
//     */
//    public MatchMode getDefaultCollectionMatchMode();
//
//    /**
//     * @return the default match mode for other matchable objects
//     */
//    public MatchMode getDefaultMatchMatchMode();
}
