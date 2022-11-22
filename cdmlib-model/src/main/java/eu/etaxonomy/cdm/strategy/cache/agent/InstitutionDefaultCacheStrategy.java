/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.agent;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * A default cache strategy for institutions.
 * TODO This is a preliminary implementation to have at least one default cache strategy.
 * Maybe it will need improvement later on.
 * @author a.mueller
 * @since 07.04.2010
 */
public class InstitutionDefaultCacheStrategy
        extends StrategyBase
        implements IIdentifiableEntityCacheStrategy<Institution> {

    private static final long serialVersionUID = 4586884860596045736L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	public static UUID uuid = UUID.fromString("20a61a6f-aac9-422e-a95f-20dfacd35b65");

    public static InstitutionDefaultCacheStrategy NewInstance() {
        return new InstitutionDefaultCacheStrategy();
    }

    private InstitutionDefaultCacheStrategy(){}

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@Override
    public String getTitleCache(Institution institution) {
		if (institution == null){
			return null;
		}else{
			String result = "";
			result = CdmUtils.concat("", result, institution.getName());
			//add code if it exists
			if (StringUtils.isNotBlank(institution.getCode())){
				if (StringUtils.isNotBlank(result)){
					result += " (" + institution.getCode() +")";
				}else{
					result = institution.getCode();
				}
			}
			//return
			return result;
		}
	}


}
