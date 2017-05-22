/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import java.util.List;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;

public class TestingBotanicalNameCacheStrategy implements INonViralNameCacheStrategy {

    @Override
    public String getAuthorshipCache(TaxonName nonViralName) {
        return "test.botanical.authorshipCache"+ nonViralName.getId();
    }

    @Override
    public String getLastEpithet(TaxonName taxonName) {
        return "test.botanical.lastEpithet"+ taxonName.getId();
    }

    @Override
    public String getNameCache(TaxonName taxonName) {
        return "test.botanical.nameCache"+ taxonName.getId();
    }

    @Override
    public String getFullTitleCache(TaxonName taxonName) {
        return "test.botanical.fullTitleCache"+ taxonName.getId();
    }

    @Override
    public String getTitleCache(TaxonName object) {
        return "test.botanical.titleCache"+ object.getId();
    }

    @Override
    public List<TaggedText> getTaggedTitle(TaxonName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TaggedText> getTaggedFullTitle(TaxonName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TaggedText> getTaggedName(TaxonName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public String getTitleCache(TaxonName nonViralName, HTMLTagRules htmlTagRules) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullTitleCache(TaxonName nonViralName,
			HTMLTagRules htmlTagRules) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public List<TaggedText> getNomStatusTags(TaxonName taxonName, boolean includeSeparatorBefore,
            boolean includeSeparatorAfter) {
        // TODO Auto-generated method stub
        return null;
    }

}
