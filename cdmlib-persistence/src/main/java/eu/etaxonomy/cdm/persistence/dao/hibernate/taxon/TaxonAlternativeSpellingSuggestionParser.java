package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;


import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.AlternativeSpellingSuggestionParser;

@Component
public class TaxonAlternativeSpellingSuggestionParser extends AlternativeSpellingSuggestionParser<TaxonBase> {

	public TaxonAlternativeSpellingSuggestionParser() {
		super(TaxonBase.class);
		Class<? extends TaxonBase> indexedClasses[] = new Class[2];
		indexedClasses[0] = Taxon.class;
		indexedClasses[1] = Synonym.class;
		super.setIndexedClasses(indexedClasses);
		super.setDefaultField("name.titleCache");
	}

	@Override
	@Autowired
	public void setDirectory(@Qualifier("taxonSpellingDirectory")Directory directory) {
		this.directory = directory;
	}
}
