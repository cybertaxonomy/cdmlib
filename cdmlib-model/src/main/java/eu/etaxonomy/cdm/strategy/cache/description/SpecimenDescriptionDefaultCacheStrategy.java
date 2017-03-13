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
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

public class SpecimenDescriptionDefaultCacheStrategy extends StrategyBase implements
		IIdentifiableEntityCacheStrategy<SpecimenDescription> {
    private static final long serialVersionUID = 310092633142719872L;

    final static UUID uuid = UUID.fromString("73c03fc4-0429-4ca1-b2cb-b9a56aad4d22");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    public String getTitleCache(SpecimenDescription specimenDescription) {
		String title;
		SpecimenOrObservationBase specimen = specimenDescription.getDescribedSpecimenOrObservation();
		if (specimen == null){
			title = getFirstPart(specimenDescription);
			title = title.replace(" for ", "");
		}else{
			title = specimen.getTitleCache();
			title = getFirstPart(specimenDescription) + title;
		}
		return title;
	}

	private String getFirstPart(SpecimenDescription specimenDescription){
		Set<Marker> markers = specimenDescription.getMarkers();
		MarkerType markerType = MarkerType.USE();
		Boolean isUseDescription = false;
		for (Marker marker : markers) {
			if(marker.getMarkerType().equals(markerType)) {
				isUseDescription = true;
			}
		}
		if (specimenDescription.isImageGallery()){
			return "Image gallery for " ;
		} else if (isUseDescription) {
			return "Use description for  ";
		} else {
			return "Specimen description for ";
		}
	}
}
