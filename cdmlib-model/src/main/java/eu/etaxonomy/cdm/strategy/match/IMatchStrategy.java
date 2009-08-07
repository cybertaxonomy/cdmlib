// $Id$
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
 * @created 31.07.2009
 * @version 1.0
 */
public interface IMatchStrategy {

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

}