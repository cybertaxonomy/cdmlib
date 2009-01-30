package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;


import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springmodules.lucene.index.factory.IndexFactory;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.AlternativeSpellingSuggestionParser;

//@Component //FIXME
public class TaxonAlternativeSpellingSuggestionParser extends AlternativeSpellingSuggestionParser<TaxonBase> {

	public TaxonAlternativeSpellingSuggestionParser() {
		super(TaxonBase.class);
		super.setDefaultField("name.persistentTitleCache");
	}

	@Override
	@Autowired
	public void setDirectory(@Qualifier("taxonSpellingDirectory")Directory directory) {
		this.directory = directory;
	}

	@Override
	@Autowired
	public void setIndexFactory(@Qualifier("taxonSpellingIndex")IndexFactory indexFactory) {
		this.indexFactory = indexFactory;
	}

}
