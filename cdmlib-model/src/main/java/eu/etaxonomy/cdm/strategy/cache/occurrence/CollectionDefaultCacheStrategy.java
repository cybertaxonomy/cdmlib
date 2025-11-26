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
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * A default cache strategy for {@link Collection}s.
 *
 * @author a.mueller
 * @since 07.04.2010
 */
public class CollectionDefaultCacheStrategy
        extends StrategyBase
        implements IIdentifiableEntityCacheStrategy<Collection>{

    private static final long serialVersionUID = 457142779236428472L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

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
			String name = CdmUtils.NzTrim(collection.getName());
			String code = CdmUtils.NzTrim(collection.getCode());

			//combine code and name
			if (name != null) {
			    name = name.equals(code)? null : name;
			}
			result = CdmUtils.concat(" - ", code, name);

			//add townOrLocation
			String town = CdmUtils.NzTrim(collection.getTownOrLocation());
			if (isBlank(result)) {
			    result = town;
			} else if (isNotBlank(town)
			        && !result.contains(town)){
			    result += " (" + town+ ")";
			}
			//return
			return isBlank(result) ? collection.toString() : result;
		}
	}
}