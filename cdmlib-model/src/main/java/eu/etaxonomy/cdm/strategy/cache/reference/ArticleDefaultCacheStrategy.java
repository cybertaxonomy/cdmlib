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
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class ArticleDefaultCacheStrategy <T extends ReferenceBase> extends NomRefDefaultCacheStrategyBase<T> implements  INomenclaturalReferenceCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(ArticleDefaultCacheStrategy.class);
	
	private String prefixSeries = "ser.";
	private String prefixVolume = "vol.";
	private String prefixReferenceJounal = "in";
	private String blank = " ";
	private String comma = ",";
	private String dot =".";
	
	final static UUID uuid = UUID.fromString("0d45343a-0c8a-4a64-97ca-e94974b65c96");
	
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
	public static ArticleDefaultCacheStrategy NewInstance(){
		return new ArticleDefaultCacheStrategy();
	}
	
	/**
	 * Constructor
	 */
	private ArticleDefaultCacheStrategy(){
		super();
	}

	@Override
	protected String getNomRefTitleWithoutYearAndAuthor(T article){
		if (article == null){
			return null;
		}
		if (article.getInReference() == null){
			return null;
		}
		
		String titelAbbrev = CdmUtils.Nz(article.getInReference().getTitle()).trim();
		String series = CdmUtils.Nz(article.getSeries()).trim();
		String volume = CdmUtils.Nz(article.getVolume()).trim();
		
		boolean lastCharIsDouble;
		Integer len;
		String lastChar;
		String character =".";
		len = titelAbbrev.length();
		if (len > 0){lastChar = titelAbbrev.substring(len-1, len);}
		//lastCharIsDouble = f_core_CompareStrings(RIGHT(@TitelAbbrev,1),character);
		lastCharIsDouble = titelAbbrev.equals(character);

//		if(lastCharIsDouble  && edition.length() == 0 && series.length() == 0 && volume.length() == 0 && refYear.length() > 0 ){
//			titelAbbrev =  titelAbbrev.substring(1, len-1); //  SUBSTRING(@TitelAbbrev,1,@LEN-1)
//		}

		
		boolean needsComma = false;
		
		String nomRefCache = "";

		//inJournal
		nomRefCache = prefixReferenceJounal + blank; 
		
		//titelAbbrev
		if (!"".equals(titelAbbrev)){
			nomRefCache = nomRefCache + titelAbbrev + blank; 
		}
		
		//inSeries
		String seriesPart = "";
		if (!"".equals(series)){
			seriesPart = series;
			if (isNumeric(series)){
				seriesPart = prefixSeries + blank + seriesPart;
			}
			if (needsComma){
				seriesPart = comma + seriesPart;
			}
			needsComma = true;
		}
		nomRefCache += seriesPart;
		
		
		//volume Part
		String volumePart = "";
		if (!"".equals(volume)){
			volumePart = volume;
			if (needsComma){
				volumePart = comma + blank + volumePart;
			}
			//needsComma = false;
		}
		nomRefCache += volumePart;
		
		//delete "."
		while (nomRefCache.endsWith(".")){
			nomRefCache = nomRefCache.substring(0, nomRefCache.length()-1);
		}
		
		return nomRefCache.trim();
	}
	
	private boolean isNumeric(String string){
		if (string == null){
			return false;
		}
		try {
			Double.valueOf(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
		
	}
}
