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

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;

public class TestingBotanicalNameCacheStrategy implements
        INonViralNameCacheStrategy<BotanicalName> {

    public String getAuthorshipCache(BotanicalName nonViralName) {
        return "test.botanical.authorshipCache"+ nonViralName.getId();
    }

    public String getLastEpithet(BotanicalName taxonNameBase) {
        return "test.botanical.lastEpithet"+ taxonNameBase.getId();
    }

    public String getNameCache(BotanicalName taxonNameBase) {
        return "test.botanical.nameCache"+ taxonNameBase.getId();
    }

    public String getFullTitleCache(BotanicalName taxonNameBase) {
        return "test.botanical.fullTitleCache"+ taxonNameBase.getId();
    }

    public String getTitleCache(BotanicalName object) {
        return "test.botanical.titleCache"+ object.getId();
    }

    @Override
    public List<TaggedText> getTaggedTitle(BotanicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TaggedText> getTaggedFullTitle(BotanicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TaggedText> getTaggedName(BotanicalName taxonName) {
        // TODO Auto-generated method stub
        return null;
    }

}
