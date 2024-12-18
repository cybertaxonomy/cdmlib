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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;

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
    private static final Logger logger = LogManager.getLogger();

    private static final UUID uuid = UUID.fromString("2df06501-0e2f-4255-b1e6-091be7293f7c");

    private static final String SOURCE_SEPARATOR = ", ";
    private static final String icon = "[icon]";

    public static MediaSpecimenDefaultCacheStrategy NewInstance() {
        return new MediaSpecimenDefaultCacheStrategy();
    }

    public static MediaSpecimenDefaultCacheStrategy NewInstance(boolean withReference) {
        MediaSpecimenDefaultCacheStrategy result = new MediaSpecimenDefaultCacheStrategy();
        result.withReference = withReference;
        return result;
    }

    private boolean withReference = false;

    private MediaSpecimenDefaultCacheStrategy(){}

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    protected String doGetTitleCache(MediaSpecimen specimen) {

	    String result;

	    //add code if it exists
        String collectionData = getCollectionAndAccession(specimen);
        Media media = specimen.getMediaSpecimen();
        if (media == null) {
            result = collectionData;
        } else {
            //TODO the following is semantically mostly redundant with the implementation
            //     in TypeDesignationGroupContainerFormatter.buildTaggedTextForSpecimenTypeDesignation()
            //     We should switch here to TaggedText and merge the 2 methods
            //     #10573 and related

            if (isBlank(collectionData)){
                String mediaTitle = media.getTitleCache();
                if (mediaTitle.startsWith("- empty media - <") ){  //empty media we do not want to handle on Media level but on MediaSpecimen level
                    mediaTitle = "";
                }

                if (!withReference || media.getSources().isEmpty()) {
                    result = mediaTitle;
                }else {
                    mediaTitle = media.getTitle() == null ? "" : media.getTitle().getText();
                    result = CdmUtils.concat(" ", mediaTitle, "in ");
                    result = addSources(result, media);
                }
            }else{ //collection data exists
                result = collectionData;
                LanguageString titleLs = specimen.getMediaSpecimen().getTitle();
                if (titleLs != null && isNotBlank(titleLs.getText())) {
                    result += " (" + titleLs.getText() +")";
                }
            }
        }
        return CdmUtils.concat(" ", icon, result);
	}

    private String addSources(String result, Media media) {
        int count = 0;
        for(IdentifiableSource source : media.getSources()){
            //TODO add sourceTypes to configuration
            if (source.getType().isPublicSource()){
                if (count++ > 0){
                    result += SOURCE_SEPARATOR;
                }
                result = addSource(result, source);
            }
        }
        return result;
    }

    private String addSource(String result,
            OriginalSourceBase source) {
        Reference ref = source.getCitation();
        if (ref != null){
            String citation = OriginalSourceFormatter.INSTANCE.format(source);
            result += citation;
//            workingsetBuilder.add(TaggedText.NewReferenceInstance(TagEnum.reference, citation, ref));
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