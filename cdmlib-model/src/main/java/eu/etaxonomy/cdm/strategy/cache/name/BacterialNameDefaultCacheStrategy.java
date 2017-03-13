/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.name;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.BacterialName;

/**
 * The bacterial name default cache strategy.
 *
 * @author a.mueller
 *
 */
public class BacterialNameDefaultCacheStrategy extends NonViralNameDefaultCacheStrategy<BacterialName> implements  INonViralNameCacheStrategy<BacterialName> {
	private static final long serialVersionUID = 7369285557176649585L;
	private static final Logger logger = Logger.getLogger(BacterialNameDefaultCacheStrategy.class);
	final static UUID uuid = UUID.fromString("b97cf0af-2f97-487e-8d06-cbe924f3222a");

	static {
		logger.info("BacterialNameDefaultCacheStrategy not yet really implemented. Its just a copy of BotanicalNameDefaultCacheStrategy right now !!");
	}

	@Override
	public  UUID getUuid(){
		return uuid;
	}


	public static BacterialNameDefaultCacheStrategy NewInstance(){
		return new BacterialNameDefaultCacheStrategy();
	}

}
