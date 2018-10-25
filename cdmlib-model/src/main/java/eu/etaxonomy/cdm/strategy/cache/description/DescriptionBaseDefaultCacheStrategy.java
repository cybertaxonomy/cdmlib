/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.description;

import java.util.Set;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

public abstract class DescriptionBaseDefaultCacheStrategy<T extends DescriptionBase<IIdentifiableEntityCacheStrategy<T>>> extends StrategyBase implements
		IIdentifiableEntityCacheStrategy<T> {

    private static final long serialVersionUID = 2704481466414887850L;

    private static final String FOR = " for ";

	@Override
    public String getTitleCache(T description) {
		String title;
		IdentifiableEntity entity = getDescriptionEntity(description);
		if (entity == null){
			title = getFirstPart(description);
			title = title.replace(FOR, "");
		}else{
			title = entity.getTitleCache();
			title = getFirstPart(description) + title;
		}
		return title;
	}

	protected abstract IdentifiableEntity getDescriptionEntity(T description);

    protected String getFirstPart(T description){
		Set<Marker> markers = description.getMarkers();
		MarkerType markerType = MarkerType.USE();
		Boolean isUseDescription = false;
		for (Marker marker : markers) {
			if(marker.getMarkerType().equals(markerType)) {
				isUseDescription = true;
			}
		}
		String firstPart;
		if (description.isImageGallery()){
			firstPart = "Image gallery"+getSourceString(description);
		} else if (isUseDescription) {
		    firstPart = "Use description"+getSourceString(description);
		} else {
		    firstPart = getDescriptionName()+getSourceString(description);
		}
		return firstPart+FOR;
	}

	protected abstract String getDescriptionName();

    private String getSourceString(T description){
	    String sourceString = "";
	    sourceString = description.getSources().stream()
	            .filter(source->source.getCitation()!=null)
	            .map(source->source.getCitation().getTitleCache())
	            .collect(Collectors.joining(","));
	    if(CdmUtils.isNotBlank(sourceString)){
	        sourceString = " from "+sourceString+" ";
	    }
	    return sourceString;
	}
}
