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

import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;

public class TestingZoologicalNameCacheStrategy implements
		INonViralNameCacheStrategy<ZoologicalName> {
	
	public String getAuthorshipCache(ZoologicalName nonViralName) {
		return "test.zoological.authorshipCache"+ nonViralName.getId();
	}

	public String getLastEpithet(ZoologicalName taxonNameBase) {
		return "test.zoological.lastEpithet"+ taxonNameBase.getId();
	}
	
	public String getNameCache(ZoologicalName taxonNameBase) {
		return "test.zoological.nameCache"+ taxonNameBase.getId();
	}

	public String getFullTitleCache(ZoologicalName taxonNameBase) {
		return "test.zoological.fullTitleCache"+ taxonNameBase.getId();
	}

	public List<Object> getTaggedName(ZoologicalName taxonNameBase) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitleCache(ZoologicalName object) {
		return "test.zoological.titleCache"+ object.getId();
	}


}
