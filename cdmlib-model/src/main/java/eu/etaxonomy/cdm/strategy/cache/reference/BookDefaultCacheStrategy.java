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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.reference.Reference;

public class BookDefaultCacheStrategy <T extends Reference> extends NomRefDefaultCacheStrategyBase<T>  implements  INomenclaturalReferenceCacheStrategy<T> {
	static final long serialVersionUID = -8535065052672341462L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BookDefaultCacheStrategy.class);
	
	private String prefixEdition = "ed.";
	private String prefixSeries = "ser.";
	private String prefixVolume = "vol.";
	private String blank = " ";
	private String comma = ",";
	private String dot =".";
	
	final static UUID uuid = UUID.fromString("68076ca5-d517-489c-8ae2-01d3c38cc788");
	
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
	public static BookDefaultCacheStrategy NewInstance(){
		return new BookDefaultCacheStrategy<Reference>();
	}
	
	/**
	 * Constructor
	 */
	private BookDefaultCacheStrategy(){
		super();
	}


	@Override
	public String getTitleWithoutYearAndAuthor(T ref, boolean isAbbrev){
		if (ref == null){
			return null;
		}
		//TODO
		String title = CdmUtils.getPreferredNonEmptyString(ref.getTitle(), ref.getAbbrevTitle(), isAbbrev, true);
		String edition = CdmUtils.Nz(ref.getEdition()).trim();
		//TODO
		String series = ""; //nomenclaturalReference.getSeries();
		String volume = CdmUtils.Nz(ref.getVolume()).trim();
		String refYear = "";  //TODO nomenclaturalReference.getYear();


		String nomRefCache = "";
		boolean lastCharIsDouble;
		Integer len;
		String lastChar;
		String character =".";
		len = title.length();
		if (len > 0){
			lastChar = title.substring(len-1, len);
		}
		//lastCharIsDouble = f_core_CompareStrings(RIGHT(@TitelAbbrev,1),character);
		lastCharIsDouble = title.equals(character);

		if(lastCharIsDouble  && edition.length() == 0 && series.length() == 0 && volume.length() == 0 && refYear.length() > 0 ){
			title =  title.substring(1, len-1); //  SUBSTRING(@TitelAbbrev,1,@LEN-1)
		}

		
		boolean needsComma = false;
		//titelAbbrev
		if (!"".equals(title) ){
			String postfix = StringUtils.isNotBlank(edition) ? "" : blank; 
			nomRefCache = title + postfix; 
		}
		//edition
		String editionPart = "";
		if (StringUtils.isNotBlank(edition)){
			editionPart = edition;
			if (isNumeric(edition)){
				editionPart = prefixEdition + blank + editionPart;
			}
			needsComma = true;
		}
		nomRefCache = CdmUtils.concat(", ", nomRefCache, editionPart);
		
		//inSeries
		String seriesPart = "";
		if (StringUtils.isNotBlank(series)){
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
		if (StringUtils.isNotBlank(volume)){
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
