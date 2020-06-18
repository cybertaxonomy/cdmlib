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
public interface IParsedMatchStrategy extends IMatchStrategy{

    public static MatchMode DEFAULT_PARSED_MATCH_MODE = MatchMode.EQUAL_OR_SECOND_NULL;
    public static MatchMode DEFAULT_PARSED_COLLECTION_MATCH_MODE = MatchMode.IGNORE;
    public static MatchMode DEFAULT_PARSED_MATCH_MATCH_MODE = MatchMode.MATCH_OR_SECOND_NULL;


	/**
	 * {@inheritDoc}
	 *
	 * @param <T>
	 * @param fullInstance The more complete instance
	 * @param parsedInstance The parsed instance having only few attributes defined
	 * @throws MatchException
	 */
	@Override
    public <T extends IMatchable> MatchResult invoke(T fullInstance, T parsedInstance) throws MatchException;


}
