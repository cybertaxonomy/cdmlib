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
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.BibtexReference;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.StrategyBase;

public class BibtexDefaultCacheStrategy <T extends BibtexReference> extends StrategyBase implements  INomenclaturalReferenceCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(BibtexDefaultCacheStrategy.class);
	
	private String beforeYear = ". ";
	private String beforeMicroReference = ": ";
	private String afterYear = ".";
	private String afterAuthor = ", ";
	private String prefixSeries = "ser.";
	private String prefixVolume = "vol.";
	private String prefixReferenceJounal = "in";
	private String blank = " ";
	private String comma = ",";
	private String dot =".";
	
	final static UUID uuid = UUID.fromString("ede34ca3-6cf7-4821-b975-046275f60d24");
	
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
	public static BibtexDefaultCacheStrategy NewInstance(){
		return new BibtexDefaultCacheStrategy();
	}
	
	/**
	 * Constructor
	 */
	private BibtexDefaultCacheStrategy(){
		super();
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTokenizedNomenclaturalTitel(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	public String getTokenizedNomenclaturalTitel(T bibtexReference) {
		String result =  getNomRefTitleWithoutYearAndAuthor(bibtexReference);
		result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;
		result = addYear(result, bibtexReference);
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	public String getTitleCache(T bibtexReference) {
		String result =  getNomRefTitleWithoutYearAndAuthor(bibtexReference);
		result = addYear(result, bibtexReference);
		String author = CdmUtils.Nz(bibtexReference.getAuthorTeam().getTitleCache());
		result = author + afterAuthor + result;
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getBeforeMicroReference()
	 */
	public String getBeforeMicroReference(){
		return beforeMicroReference;
	}
	
	private String addYear(String string, T nomRef){
		String result;
		if (string == null){
			return null;
		}
		String year = CdmUtils.Nz(nomRef.getYear());
		if ("".equals(year)){
			result = string + afterYear;
		}else{
			result = string + beforeYear + year + afterYear;
		}
		return result;
	}
	
	
	private String getNomRefTitleWithoutYearAndAuthor(T article){
		
		if (true){
			//TODO
			logger.warn("not yet implemented");
			return null;
		}
		
		
		
		
		
		if (article == null){
			return null;
		}
		//TODO
		String titelAbbrev = CdmUtils.Nz(article.getTitle());
		//TODO
		String series = ""; //nomenclaturalReference.getSeries();
		String volume = CdmUtils.Nz(article.getVolume());

		String nomRefCache = "";
		boolean lastCharIsDouble;
		Integer len;
		String lastChar;
		String character =".";
		len = titelAbbrev.length();
		lastChar = titelAbbrev.substring(len-1, len);
		//lastCharIsDouble = f_core_CompareStrings(RIGHT(@TitelAbbrev,1),character);
		lastCharIsDouble = titelAbbrev.equals(character);

//		if(lastCharIsDouble  && edition.length() == 0 && series.length() == 0 && volume.length() == 0 && refYear.length() > 0 ){
//			titelAbbrev =  titelAbbrev.substring(1, len-1); //  SUBSTRING(@TitelAbbrev,1,@LEN-1)
//		}

		
		boolean needsComma = false;
		
		//inJournal
		nomRefCache = prefixReferenceJounal + blank; 
		
		//titelAbbrev
		String titelAbbrevPart = "";
		if (!"".equals(titelAbbrev)){
			nomRefCache = titelAbbrev + blank; 
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
		
		//delete .
		while (nomRefCache.endsWith(".")){
			nomRefCache = nomRefCache.substring(0, nomRefCache.length()-1);
		}
		
		return nomRefCache;
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
