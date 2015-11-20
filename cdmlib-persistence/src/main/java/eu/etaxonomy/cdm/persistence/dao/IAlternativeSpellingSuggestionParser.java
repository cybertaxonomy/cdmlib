/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.persistence.dao;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

/**
 * @author b.clark
 */
public interface IAlternativeSpellingSuggestionParser {
	public Query parse(String queryString) throws ParseException;
	public Query suggest(String queryString) throws ParseException;

	public void refresh();
}
