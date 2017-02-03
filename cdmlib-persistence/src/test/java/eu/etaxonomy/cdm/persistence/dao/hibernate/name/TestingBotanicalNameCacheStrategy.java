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

import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;

public class TestingBotanicalNameCacheStrategy implements INonViralNameCacheStrategy<IBotanicalName> {

    @Override
    public String getAuthorshipCache(IBotanicalName nonViralName) {
        return "test.botanical.authorshipCache"+ nonViralName.getId();
    }

    @Override
    public String getLastEpithet(IBotanicalName taxonNameBase) {
        return "test.botanical.lastEpithet"+ taxonNameBase.getId();
    }

    @Override
    public String getNameCache(IBotanicalName taxonNameBase) {
        return "test.botanical.nameCache"+ taxonNameBase.getId();
    }

    @Override
    public String getFullTitleCache(IBotanicalName taxonNameBase) {
        return "test.botanical.fullTitleCache"+ taxonNameBase.getId();
    }

    @Override
    public String getTitleCache(IBotanicalName object) {
        return "test.botanical.titleCache"+ object.getId();
    }

    @Override
    public List<TaggedText> getTaggedTitle(IBotanicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TaggedText> getTaggedFullTitle(IBotanicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TaggedText> getTaggedName(IBotanicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public String getTitleCache(IBotanicalName nonViralName, HTMLTagRules htmlTagRules) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullTitleCache(IBotanicalName nonViralName,
			HTMLTagRules htmlTagRules) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public List<TaggedText> getNomStatusTags(IBotanicalName taxonName, boolean includeSeparatorBefore,
            boolean includeSeparatorAfter) {
        // TODO Auto-generated method stub
        return null;
    }

}
