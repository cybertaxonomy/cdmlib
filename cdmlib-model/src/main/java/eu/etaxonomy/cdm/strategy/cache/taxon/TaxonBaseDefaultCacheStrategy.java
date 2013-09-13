/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.cache.taxon;

import java.util.UUID;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

public class TaxonBaseDefaultCacheStrategy<T extends TaxonBase> extends StrategyBase implements
		IIdentifiableEntityCacheStrategy<T> {

	final static UUID uuid = UUID.fromString("931e48f0-2033-11de-8c30-0800200c9a66");
	
	@Override
	protected UUID getUuid() {
		return uuid;
	}

	public String getTitleCache(T taxonBase) {
		String title;
		if (taxonBase.getName() != null && taxonBase.getName().getTitleCache() != null){
			String namePart = getNamePart(taxonBase);
			
			title = namePart + " sec. ";
			if (taxonBase.getSec() != null){
				title += taxonBase.getSec().getTitleCache();
			}else{
				title += "???";
			}
		}else{
			title = taxonBase.toString();
		}
		if (taxonBase.isDoubtful()){
			title = "?" + title;
		}
		return title;
	}

	/**
	 * @param name
	 */
	private String getNamePart(TaxonBase taxonBase) {
		TaxonNameBase nameBase = taxonBase.getName();
		String result = nameBase.getTitleCache();
		//use name cache instead of title cache if required
		if (taxonBase.isUseNameCache() && nameBase.isInstanceOf(NonViralName.class)){
			NonViralName nvn = HibernateProxyHelper.deproxy(nameBase, NonViralName.class);
			result = nvn.getNameCache();
		}
		if (CdmUtils.isNotEmpty(taxonBase.getAppendedPhrase())){
			result = result.trim() + " " +  taxonBase.getAppendedPhrase().trim(); 
		}
		return result;
	}

}
