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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * A default cache strategy for collections.
 * TODO This is a preliminary implementation to have at least one default cache strategy.
 * Maybe it will need improvement later on.

 * @author a.mueller
 * @since 07.04.2010
 *
 */
public class CollectionDefaultCacheStrategy extends StrategyBase implements IIdentifiableEntityCacheStrategy<Collection>{
	private static final long serialVersionUID = 457142779236428472L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CollectionDefaultCacheStrategy.class);

	private static final UUID uuid = UUID.fromString("42834de1-698c-4d43-a31a-1c5617a708d6");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    public String getTitleCache(Collection collection) {
		if (collection == null){
			return null;
		}else{
			String result = "";
			result = CdmUtils.concat("", result, collection.getName());
			//add code if it exists
			if (StringUtils.isNotBlank(collection.getCode())){
				if (StringUtils.isNotBlank(result)){
					result += " (" + collection.getCode() +")";
				}else{
					result = collection.getCode();
				}
			}
			//return
			return result;
		}
	}
}
