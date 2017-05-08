/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.name;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.IViralName;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.mueller
 * @date 07.05.2017
 *
 */
public class ViralNameDefaultCacheStrategy
        extends NameCacheStrategyBase<IViralName>
        implements IViralNameCacheStrategy<IViralName> {

    private static final long serialVersionUID = 2732652334900146554L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ViralNameDefaultCacheStrategy.class);

    final static UUID uuid = UUID.fromString("1cdda0d1-d5bc-480f-bf08-40a510a2f223");


    @Override
    protected UUID getUuid() {
        return uuid;
    }

    /**
     * Factory method
     * @return NonViralNameDefaultCacheStrategy A new instance of  NonViralNameDefaultCacheStrategy
     */
    public static ViralNameDefaultCacheStrategy NewInstance(){
        return new ViralNameDefaultCacheStrategy();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TaggedText> doGetTaggedTitle(IViralName viralName) {
        List<TaggedText> tags = new ArrayList<>();
        String acronym = viralName.getAcronym();
        tags.add(new TaggedText(TagEnum.name, acronym));
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaggedText> getTaggedName(IViralName taxonName) {
        return null /*this.getTaggedTitle(taxonName)*/;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthorshipCache(IViralName nonViralName) {
        return null;
    }


}
