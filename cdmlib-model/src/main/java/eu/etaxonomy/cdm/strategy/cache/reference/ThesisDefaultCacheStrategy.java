/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.reference.Reference;

public class ThesisDefaultCacheStrategy <T extends Reference> extends NomRefDefaultCacheStrategyBase<T> implements  INomenclaturalReferenceCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(ThesisDefaultCacheStrategy.class);
	

	private String prefixEdition = "ed.";
	private String prefixSeries = "ser.";
	private String prefixVolume = "vol.";
	private String blank = " ";
	private String comma = ",";
	private String dot =".";
	
	final static UUID uuid = UUID.fromString("95cceb30-6b16-4dc3-8243-c15e746565bc");
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
	@Override
	protected UUID getUuid() {
		return uuid; 
	}
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static ThesisDefaultCacheStrategy NewInstance(){
		return new ThesisDefaultCacheStrategy();
	}
	
	/**
	 * Constructor
	 */
	private ThesisDefaultCacheStrategy(){
		super();
	}


	@Override
	public String getAbbrevTitleCache(T thesis) {
		return getTitleWithoutYearAndAuthor(thesis, true);
	}
	
	@Override
	protected String getTitleWithoutYearAndAuthor(T thesis, boolean isAbbrev){
		//FIXME this is only a very fast copy and paste from "Generic". Must still be cleaned !
		
		if (thesis == null){
			return null;
		}
		
		//titelAbbrev
		//TODO
		String titelAbbrev = CdmUtils.getPreferredNonEmptyString(thesis.getTitle(), thesis.getAbbrevTitle(), isAbbrev, true);
		
		//titelAbbrev
		String nomRefCache = titelAbbrev + blank;
	
		//delete .
		while (nomRefCache.endsWith(".")){
			nomRefCache = nomRefCache.substring(0, nomRefCache.length()-1);
		}
		
		return nomRefCache.trim();
	}


}
