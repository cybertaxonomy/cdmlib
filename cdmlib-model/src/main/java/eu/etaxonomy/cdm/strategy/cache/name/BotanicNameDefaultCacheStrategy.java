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
import eu.etaxonomy.cdm.model.name.BotanicalName;

/**
 * T
 * @author a.mueller
 *
 */
public class BotanicNameDefaultCacheStrategy<T extends BotanicalName> extends NonViralNameDefaultCacheStrategy<T> implements  INonViralNameCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(BotanicNameDefaultCacheStrategy.class);
	
	final static UUID uuid = UUID.fromString("1cdda0d1-d5bc-480f-bf08-40a510a2f223");
	
	@Override
	public  UUID getUuid(){
		return uuid;
	}

	
	public static BotanicNameDefaultCacheStrategy NewInstance(){
		return new BotanicNameDefaultCacheStrategy();
	}
	
}
