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

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

public class TaxonBaseDefaultCacheStrategy<T extends TaxonBase> extends StrategyBase implements
		IIdentifiableEntityCacheStrategy<T> {
    private static final long serialVersionUID = 5769890979070021350L;
    final static UUID uuid = UUID.fromString("931e48f0-2033-11de-8c30-0800200c9a66");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
	public String getTitleCache(T taxonBase) {
		String title;
		if (taxonBase.isProtectedTitleCache()){
		    return taxonBase.getTitleCache();
		}else if (taxonBase.getName() != null && taxonBase.getName().getTitleCache() != null){
			String namePart = getNamePart(taxonBase);

			title = namePart + " sec. ";
			title += getSecundumPart(taxonBase);
		}else{
			title = taxonBase.toString();
		}
		if (taxonBase.isDoubtful()){
			title = "?" + title;
		}
		return title;
	}

	/**
	 * @param taxonBase
	 * @param title
	 * @return
	 */
	private String getSecundumPart(T taxonBase) {
		String result;
		Reference<?> sec = taxonBase.getSec();
		if (sec != null){
			if (sec.getCacheStrategy() != null &&
					sec.getAuthorship() != null &&
					isNotBlank(sec.getAuthorship().getTitleCache()) &&
					isNotBlank(sec.getYear())){
				result = sec.getCacheStrategy().getCitation(sec);
//				 sec.getAuthorTeam().getTitleCache() + sec.getYear();
			}else{
				result = taxonBase.getSec().getTitleCache();
			}
		}else{
			result = "???";
		}
		return result;
	}

	/**
	 * @param name
	 */
	private String getNamePart(TaxonBase<?> taxonBase) {
		TaxonNameBase<?,?> nameBase = taxonBase.getName();
		String result = nameBase.getTitleCache();
		//use name cache instead of title cache if required
		if (taxonBase.isUseNameCache() && nameBase.isInstanceOf(NonViralName.class)){
			NonViralName<?> nvn = HibernateProxyHelper.deproxy(nameBase, NonViralName.class);
			result = nvn.getNameCache();
		}
		if (StringUtils.isNotBlank(taxonBase.getAppendedPhrase())){
			result = result.trim() + " " +  taxonBase.getAppendedPhrase().trim();
		}
		return result;
	}

}
