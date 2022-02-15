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

import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;

/**
 * A default cache strategy for {@link MediaSpecimen}.
 * TODO This is a <b>preliminary</b> implementation to have at least one default cache strategy.
 * It will need improvement later on.
 *
 *  #5573, #7612
 *
 * Also see DerivedUnitFacadeCacheStrategy in Service Layer.
 *
 * @author a.mueller
 * @since 08.01.2021
 */
public class MediaSpecimenDefaultCacheStrategy
        extends OccurrenceCacheStrategyBase<MediaSpecimen>{

    private static final long serialVersionUID = 798148956185549004L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MediaSpecimenDefaultCacheStrategy.class);

	private static final UUID uuid = UUID.fromString("2df06501-0e2f-4255-b1e6-091be7293f7c");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    protected String doGetTitleCache(MediaSpecimen specimen) {
	    //add code if it exists
        String result = getCollectionAndAccession(specimen);
        if (specimen.getMediaSpecimen() != null){
            if (isBlank(result)){
                result = specimen.getMediaSpecimen().getTitleCache();
                if (result.startsWith("- empty media - <")){  //empty media we do not want to handle Media level but on MediaSpecimen level
                    result = null;
                }
            }else{
                LanguageString titleLs = specimen.getMediaSpecimen().getTitle();
                if (titleLs != null && isNotBlank(titleLs.getText())) {
                    result += " (" + titleLs.getText() +")";
                }
            }
        }

		return result;
	}

	//NOTE: this is a first implementation, it may be adapted in future
    @Override
    protected String doGetIdentityCache(MediaSpecimen mediaSpecimen) {
        String collectionAndAccession = getCollectionAndAccession(mediaSpecimen);
        if (isBlank(collectionAndAccession)){
            return getTitleCache(mediaSpecimen);
        }else{
            return collectionAndAccession;
        }
    }
}