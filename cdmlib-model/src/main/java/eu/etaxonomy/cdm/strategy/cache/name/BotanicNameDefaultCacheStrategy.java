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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.name.BotanicalName;

/**
 * T
 * @author a.mueller
 *
 */
public class BotanicNameDefaultCacheStrategy<T extends BotanicalName> extends NonViralNameDefaultCacheStrategy<T> implements  INonViralNameCacheStrategy<T> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BotanicNameDefaultCacheStrategy.class);
	private static final long serialVersionUID = 2679554971446197111L;

	final static UUID uuid = UUID.fromString("1cdda0d1-d5bc-480f-bf08-40a510a2f223");

	@Override
	public  UUID getUuid(){
		return uuid;
	}


	public static BotanicNameDefaultCacheStrategy NewInstance(){
		return new BotanicNameDefaultCacheStrategy();
	}

	/**
	 * Returns the AuthorCache part for a combination of an author and an ex author. This applies on combination authors
	 * as well as on basionym/orginal combination authors.
	 * The correct order is exAuthor ex author though some botanist do not know about and do it the
	 * other way round. (see 46.4-46.6 ICBN (Vienna Code, 2006))
	 * @param author the author
	 * @param exAuthor the ex-author
	 * @return
	 */
	@Override
	protected String getAuthorAndExAuthor(INomenclaturalAuthor author, INomenclaturalAuthor exAuthor){
		String result = "";
		String authorString = "";
		String exAuthorString = "";

		if (author != null){
			authorString = CdmUtils.Nz(author.getNomenclaturalTitle());
		}
		if (exAuthor != null){
			exAuthorString = CdmUtils.Nz(exAuthor.getNomenclaturalTitle());
			exAuthorString += ExAuthorSeperator;
		}

		result = exAuthorString + authorString;
		return result;

	}

}
