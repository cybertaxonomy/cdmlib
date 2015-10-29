/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;


import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.AlternativeSpellingSuggestionParser;

//spelling support currently disabled in appcontext, see spelling.xml ... "
//@Component
public class TaxonAlternativeSpellingSuggestionParser extends AlternativeSpellingSuggestionParser<TaxonBase> {

	public TaxonAlternativeSpellingSuggestionParser() {
		super(TaxonBase.class);
		Class<? extends TaxonBase> indexedClasses[] = new Class[2];
		indexedClasses[0] = Taxon.class;
		indexedClasses[1] = Synonym.class;
		super.setIndexedClasses(indexedClasses);
		super.setDefaultField("name.titleCache_tokenized");
	}

	@Override
	@Autowired
	public void setDirectory(@Qualifier("taxonSpellingDirectory")Directory directory) {
		this.directory = directory;
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.IAlternativeSpellingSuggestionParser#suggest(java.lang.String)
     */
    @Override
    public Query suggest(String queryString) throws ParseException {
        throw new RuntimeException("Currently not implemented");
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.IAlternativeSpellingSuggestionParser#refresh()
     */
    @Override
    public void refresh() {
        throw new RuntimeException("Currently not implemented");
    }
}
