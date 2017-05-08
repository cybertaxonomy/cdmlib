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

import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;

public class TestingZoologicalNameCacheStrategy implements INonViralNameCacheStrategy<IZoologicalName> {

    @Override
    public String getAuthorshipCache(IZoologicalName nonViralName) {
        return "test.zoological.authorshipCache"+ nonViralName.getId();
    }

    @Override
    public String getLastEpithet(IZoologicalName taxonName) {
        return "test.zoological.lastEpithet"+ taxonName.getId();
    }

    @Override
    public String getNameCache(IZoologicalName taxonName) {
        return "test.zoological.nameCache"+ taxonName.getId();
    }

    @Override
    public String getFullTitleCache(IZoologicalName taxonName) {
        return "test.zoological.fullTitleCache"+ taxonName.getId();
    }


    @Override
    public String getTitleCache(IZoologicalName object) {
        return "test.zoological.titleCache"+ object.getId();
    }

    @Override
    public List<TaggedText> getTaggedTitle(IZoologicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TaggedText> getTaggedFullTitle(IZoologicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TaggedText> getTaggedName(IZoologicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public String getTitleCache(IZoologicalName nonViralName, HTMLTagRules htmlTagRules) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullTitleCache(IZoologicalName nonViralName,
			HTMLTagRules htmlTagRules) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public List<TaggedText> getNomStatusTags(IZoologicalName taxonName, boolean includeSeparatorBefore,
            boolean includeSeparatorAfter) {
        // TODO Auto-generated method stub
        return null;
    }

}
