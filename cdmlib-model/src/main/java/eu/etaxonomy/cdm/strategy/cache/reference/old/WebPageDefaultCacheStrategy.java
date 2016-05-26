/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference.old;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.reference.Reference;

public class WebPageDefaultCacheStrategy extends NomRefDefaultCacheStrategyBase {
	private static final long serialVersionUID = 7432751464904190022L;


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(WebPageDefaultCacheStrategy.class);

	private String blank = " ";

	final static UUID uuid = UUID.fromString("7b36b548-88ee-4180-afc1-4b860db85fa5");

	@Override
	protected UUID getUuid() {
		return uuid;
	}


	/**
	 * Factory method
	 * @return
	 */
	public static WebPageDefaultCacheStrategy NewInstance(){
		return new WebPageDefaultCacheStrategy();
	}

	/**
	 * Constructor
	 */
	private WebPageDefaultCacheStrategy(){
		super();
	}


	@Override
	public String getFullAbbrevTitleString(Reference webPage) {
	    //shouldn't this be getTitleCache(webPage, true) ?
		return getTitleWithoutYearAndAuthor(webPage, true);
	}

	@Override
	protected String getTitleWithoutYearAndAuthor(Reference webPage, boolean isAbbrev){
		//FIXME this is only a very fast copy and paste from "Generic". Must still be cleaned !

		//titelAbbrev
		//TODO
		String titelAbbrev = CdmUtils.getPreferredNonEmptyString(webPage.getTitle(), webPage.getAbbrevTitle(), isAbbrev, true);

		//titelAbbrev
		String nomRefCache = titelAbbrev + blank;

		//delete .
		while (nomRefCache.endsWith(".")){
			nomRefCache = nomRefCache.substring(0, nomRefCache.length()-1);
		}

		return nomRefCache.trim();
	}

}
