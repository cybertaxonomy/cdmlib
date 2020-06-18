/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.match;

/**
 * @author a.mueller
 * @since 20.10.2018
 */
public interface IMatchStrategy {

    public static MatchMode DEFAULT_MATCH_MODE = MatchMode.EQUAL;
    public static MatchMode DEFAULT_COLLECTION_MATCH_MODE = MatchMode.IGNORE;
    public static MatchMode DEFAULT_MATCH_MATCH_MODE = MatchMode.MATCH;

    /**
     * Sets the match mode for property propertyName
     * @param propertyName
     * @param mergeMode
     * @throws MatchException
     * @see {@link #setMatchMode(String, MatchMode, IMatchStrategy)}
     */
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
    void setMatchMode(String propertyName, MatchMode matchMode, IMatchStrategy matchStrategy) throws MatchException;

    /**
     * Invokes the match check.
     * If two objects match this method returns <code>true</code>, <code>false</code> otherwise
     * @param <S>
     * @param mergeFirst
     * @param mergeSecond
     * @throws MatchException
     */
    public <S extends IMatchable> MatchResult invoke(S matchFirst, S matchSecond) throws MatchException;

    public  <S extends IMatchable> MatchResult invoke(S matchFirst, S matchSecond, boolean failAll) throws MatchException;

    public <S extends IMatchable> void invoke(S matchFirst, S matchSecond, MatchResult matchResult, boolean failAll) throws MatchException;

    /**
     * Returns the Matching of this match strategy
     * @return
     */
    public Matching getMatching();

}
