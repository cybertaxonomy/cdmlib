/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache;

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
	
	public  UUID getUuid(){
		return uuid;
	}

	
	public static BotanicNameDefaultCacheStrategy NewInstance(){
		return new BotanicNameDefaultCacheStrategy();
	}
	
}
