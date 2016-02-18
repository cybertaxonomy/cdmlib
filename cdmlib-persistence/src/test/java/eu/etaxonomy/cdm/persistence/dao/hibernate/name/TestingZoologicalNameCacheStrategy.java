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

import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;

public class TestingZoologicalNameCacheStrategy implements INonViralNameCacheStrategy<ZoologicalName> {

    @Override
    public String getAuthorshipCache(ZoologicalName nonViralName) {
        return "test.zoological.authorshipCache"+ nonViralName.getId();
    }

    @Override
    public String getLastEpithet(ZoologicalName taxonNameBase) {
        return "test.zoological.lastEpithet"+ taxonNameBase.getId();
    }

    @Override
    public String getNameCache(ZoologicalName taxonNameBase) {
        return "test.zoological.nameCache"+ taxonNameBase.getId();
    }

    @Override
    public String getFullTitleCache(ZoologicalName taxonNameBase) {
        return "test.zoological.fullTitleCache"+ taxonNameBase.getId();
    }


    @Override
    public String getTitleCache(ZoologicalName object) {
        return "test.zoological.titleCache"+ object.getId();
    }

    @Override
    public List<TaggedText> getTaggedTitle(ZoologicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TaggedText> getTaggedFullTitle(ZoologicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TaggedText> getTaggedName(ZoologicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public String getTitleCache(ZoologicalName nonViralName, HTMLTagRules htmlTagRules) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullTitleCache(ZoologicalName nonViralName,
			HTMLTagRules htmlTagRules) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public List<TaggedText> getNomStatusTags(ZoologicalName taxonName, boolean includeSeparatorBefore,
            boolean includeSeparatorAfter) {
        // TODO Auto-generated method stub
        return null;
    }

}
