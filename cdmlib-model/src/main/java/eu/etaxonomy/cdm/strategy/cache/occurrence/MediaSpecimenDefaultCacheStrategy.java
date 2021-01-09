/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.occurrence;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * A default cache strategy for MediaSpecimen.
 * TODO This is a <b>preliminary</b> implementation to have at least one default cache strategy.
 * It will need improvement later on.
 *
 * Also see DerivedUnitFacadeCacheStrategy in Service Layer.
 *
 * @author a.mueller
 * @since 08.01.2021
 */
public class MediaSpecimenDefaultCacheStrategy
        extends StrategyBase
        implements IIdentifiableEntityCacheStrategy<MediaSpecimen>{

    private static final long serialVersionUID = 798148956185549004L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MediaSpecimenDefaultCacheStrategy.class);

	private static final UUID uuid = UUID.fromString("2df06501-0e2f-4255-b1e6-091be7293f7c");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    public String getTitleCache(MediaSpecimen mediaSpecimen) {
		if (mediaSpecimen == null){
			return null;
		}else{
			String result = "";
			//add code if it exists
			if (mediaSpecimen.getCollection() != null){
			    Collection collection = mediaSpecimen.getCollection();
			    if (isNotBlank(collection.getCode())){
			        result = CdmUtils.concat("", result, collection.getCode());
			    }
			}
			result = CdmUtils.concat(" ", result, CdmUtils.Ne(mediaSpecimen.getAccessionNumber()));
			String mediaTitle = mediaSpecimen.getMediaSpecimen() == null? null : mediaSpecimen.getMediaSpecimen().getTitleCache();
			if (isNotBlank(mediaTitle)){
			    if (isNotBlank(result)){
			        result += " (" + mediaTitle +")";
			    }else{
			        result = mediaTitle;
			    }
			}
			if (isBlank(result)){
			    result = mediaSpecimen.toString();
			}

			return result;
		}
	}
}