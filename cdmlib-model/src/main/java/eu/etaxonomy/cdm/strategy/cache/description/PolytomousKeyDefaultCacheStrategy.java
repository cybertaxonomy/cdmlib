/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.cache.description;

import java.util.UUID;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

public class PolytomousKeyDefaultCacheStrategy extends StrategyBase implements IIdentifiableEntityCacheStrategy<PolytomousKey> {
	private static final long serialVersionUID = 4371087085809422025L;
	final static UUID uuid = UUID.fromString("876c4247-bfc4-4303-b631-b2c1c4435571");
	
	public static PolytomousKeyDefaultCacheStrategy NewInstance(){
		return new PolytomousKeyDefaultCacheStrategy();
	}
	
	private PolytomousKeyDefaultCacheStrategy(){
	}
	
	@Override
	protected UUID getUuid() {
		return uuid;
	}

	public String getTitleCache(PolytomousKey key) {
		String title;
		if (key.getTaxonomicScope().size()>0){
			String taxa = "";
			for (Taxon taxon : key.getTaxonomicScope()){
				taxa = CdmUtils.concat(",", taxa, taxon.getName().getTitleCache());
			}
			title = "Key for %s";
			title = String.format(title, taxa);
		}else{
			title = new IdentifiableEntityDefaultCacheStrategy().getTitleCache(key);
		}
		return title;
	}
	
//	private String getFirstPart(TaxonDescription taxonDescription){
//		if (taxonDescription.isImageGallery()){
//			return "Image gallery for " ;
//		}else {
//			return "Taxon description for ";
//		}
//	}
}
