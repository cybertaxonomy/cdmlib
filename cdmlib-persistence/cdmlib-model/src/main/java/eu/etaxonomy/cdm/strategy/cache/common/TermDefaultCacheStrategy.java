// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.common;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * @author a.mueller
 * @date 19.05.2010
 *
 */
public class TermDefaultCacheStrategy<T extends TermBase> extends StrategyBase implements IIdentifiableEntityCacheStrategy<T> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermDefaultCacheStrategy.class);
	
	final static UUID uuid = UUID.fromString("9cdf52c1-bac4-4b6c-a7f9-1a87401bd8f9");
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
	@Override
	protected UUID getUuid() {
		return uuid;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.IdentifiableEntity)
	 */
	public String getTitleCache(T term) {
		String result = null;
		if (term.getRepresentations().size() > 0) {
			//use default representation (or if not exist any other)
			Representation representation = term.getRepresentation(Language.DEFAULT());
			if (representation == null){
				representation = term.getRepresentations().iterator().next();
			}
			//return label, or if not exists abbreviated label, of if not exists description
			result = representation.getLabel();
			if (CdmUtils.isEmpty(result)){
					result = representation.getAbbreviatedLabel();
			}
			if (CdmUtils.isEmpty(result)){
				result = representation.getText();
				representation.getDescription();
			}
		}
		//if still empty return toString
		if (CdmUtils.isEmpty(result)){
			result = toString();
		}
		return result;
	}

}
