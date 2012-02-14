/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.name;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author AM
 *
 */
public abstract class NameCacheStrategyBase<T extends TaxonNameBase> extends StrategyBase implements INameCacheStrategy<T> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(NameCacheStrategyBase.class);

    final static UUID uuid = UUID.fromString("817ae5b5-3ac2-414b-a134-a9ae86cba040");

    /**
     * Constructor
     */
    public NameCacheStrategyBase() {
        super();
    }


    /**
     * Generates and returns the title cache of the given name.
     * The title cache in general includes the name and the authorship and year for some types of names.
     *
     * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.CdmBase)
     * @see eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.IdentifiableEntity)
     */
    public abstract String getTitleCache(T name);


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.strategy.cache.HTMLTagRules)
     */
    public abstract String getTitleCache(T name, HTMLTagRules rules);

    
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy#getFullTitleCache(eu.etaxonomy.cdm.model.name.TaxonNameBase)
     */
    public abstract String getFullTitleCache(T name);

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy#getFullTitleCache(eu.etaxonomy.cdm.model.name.TaxonNameBase, eu.etaxonomy.cdm.strategy.cache.HTMLTagRules)
     */
    public abstract String getFullTitleCache(T name, HTMLTagRules rules);

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy#getTaggedTitle(eu.etaxonomy.cdm.model.name.TaxonNameBase)
     */
    public abstract List<TaggedText> getTaggedTitle(T taxonNameBase);

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy#getTaggedFullTitle(eu.etaxonomy.cdm.model.name.TaxonNameBase)
     */
    public abstract List<TaggedText> getTaggedFullTitle(T taxonNameBase);

}
