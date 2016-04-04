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
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

public class TaxonDescriptionDefaultCacheStrategy extends StrategyBase implements
		IIdentifiableEntityCacheStrategy<TaxonDescription> {

	final static UUID uuid = UUID.fromString("0517ae48-597d-4d6b-9f18-8752d689720d");
	
	@Override
	protected UUID getUuid() {
		return uuid;
	}

	public String getTitleCache(TaxonDescription taxonDescription) {
		String title;
		Taxon taxon = taxonDescription.getTaxon(); 
		if (taxon == null){
			title = getFirstPart(taxonDescription);
			title = title.replace(" for ", "");
		}else{
			title = taxon.getTitleCache();
			int secPos = title.indexOf("sec.");
			if (secPos > 2){
				title = title.substring(0, secPos).trim();
			}
			title = getFirstPart(taxonDescription) + title;
		}
		return title;
	}
	
	private String getFirstPart(TaxonDescription taxonDescription){
		Set<Marker> markers = taxonDescription.getMarkers();
		MarkerType markerType = MarkerType.USE();
		Boolean isUseDescription = false;
		for (Marker marker : markers) {
			if(marker.getMarkerType().equals(markerType)) {
				isUseDescription = true;
			}
		} 
		if (taxonDescription.isImageGallery()){
			return "Image gallery for " ;
		} else if (isUseDescription) {
			return "Use description for  ";
		} else {
			return "Factual data for ";
		}
	}
}
