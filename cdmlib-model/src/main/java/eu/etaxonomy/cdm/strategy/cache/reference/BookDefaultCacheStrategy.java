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
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.StrategyBase;

public class BookDefaultCacheStrategy <T extends Book> extends StrategyBase implements  INomenclaturalReferenceCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(BookDefaultCacheStrategy.class);
	
	private String beforeYear = ". ";
	private String beforeMicroReference = ": ";
	private String afterYear = ".";
	private String afterAuthor = ", ";
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
		return new BookDefaultCacheStrategy();
	}
	
	/**
	 * Constructor
	 */
	private BookDefaultCacheStrategy(){
		super();
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTokenizedNomenclaturalTitel(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	public String getTokenizedNomenclaturalTitel(T nomenclaturalReference) {
		String result =  getNomRefTitleWithoutYearAndAuthor(nomenclaturalReference);
		result += INomenclaturalReference.MICRO_REFERENCE_TOKEN;
		result = addYear(result, nomenclaturalReference);
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.reference.INomenclaturalReference)
	 */
	public String getTitleCache(T nomenclaturalReference) {
		String result =  getNomRefTitleWithoutYearAndAuthor(nomenclaturalReference);
		result = addYear(result, nomenclaturalReference);
		String author = CdmUtils.Nz(nomenclaturalReference.getAuthorTeam().getTitleCache());
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
	
	
	private String getNomRefTitleWithoutYearAndAuthor(T nomenclaturalReference){
		if (nomenclaturalReference == null){
			return null;
		}
		//TODO
		String titelAbbrev = CdmUtils.Nz(nomenclaturalReference.getTitle());
		String edition = CdmUtils.Nz(nomenclaturalReference.getEdition());
		//TODO
		String series = ""; //nomenclaturalReference.getSeries();
		String volume = CdmUtils.Nz(nomenclaturalReference.getVolume());
		String refYear = "";  //TODO nomenclaturalReference.getYear();


		String nomRefCache = "";
		boolean lastCharIsDouble;
		Integer len;
		String lastChar;
		String character =".";
		len = titelAbbrev.length();
		lastChar = titelAbbrev.substring(len-1, len);
		//lastCharIsDouble = f_core_CompareStrings(RIGHT(@TitelAbbrev,1),character);
		lastCharIsDouble = titelAbbrev.equals(character);

		if(lastCharIsDouble  && edition.length() == 0 && series.length() == 0 && volume.length() == 0 && refYear.length() > 0 ){
			titelAbbrev =  titelAbbrev.substring(1, len-1); //  SUBSTRING(@TitelAbbrev,1,@LEN-1)
		}

		
		boolean needsComma = false;
		//titelAbbrev
		String titelAbbrevPart = "";
		if (!"".equals(titelAbbrev)){
			nomRefCache = titelAbbrev + blank; 
		}
		//edition
		String editionPart = "";
		if (!"".equals(edition)){
			editionPart = edition;
			if (isNumeric(edition)){
				editionPart = prefixEdition + blank + editionPart;
			}
			needsComma = true;
		}
		nomRefCache += editionPart;
		
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
		
		
		

		
		//	--Edition and series are null or numeric

//		if (isNumeric(edition) ){
//			if (titelAbbrev.length() > 0 && edition.length() > 0 &&  series.length() > 0 && isNumeric(series) && volume.length() > 0 && refYear.length() > 0 ){
//				nomRefCache = titelAbbrev + blank + prefixEdition + blank + edition + comma + blank + prefixSeries + blank + series + comma + blank + volume + dot + blank + refYear + dot;
//			}
//		}

		
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @prefixEdition + @blank + @Edition +  @comma + @blank + @prefixSeries + @blank + @Series + @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) = 0 AND len(@RefYear) > 0) SET @NomRefCache = @TitelAbbrev + @blank + @prefixEdition + @blank + @Edition +  @comma + @blank + @prefixSeries + @blank + @Series + @comma +  @blank + @Refyear + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @prefixEdition + @blank + @Edition +  @comma + @blank + @prefixSeries + @blank + @Series + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) = 0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @prefixEdition+ @blank + @Edition +  @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) = 0 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @prefixEdition + @blank + @Edition +  @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) = 0 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @prefixSeries + @blank + @Series + @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) = 0 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @prefixSeries + @blank + @Series + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) = 0 AND len(@Series) = 0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) = 0 AND len(@Series) = 0 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @prefixEdition + @blank + @Edition +  @comma + @blank + @prefixSeries + @blank + @Series + @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @prefixEdition + @blank + @Edition +  @comma + @blank + @prefixSeries + @blank + @Series + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) = 0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @prefixEdition + @blank + @Edition +  @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) = 0 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @prefixEdition + @blank + @Edition +  @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) = 0 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @prefixSeries + @blank + @Series + @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) = 0 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @prefixSeries + @blank + @Series + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) = 0 AND len(@Series) = 0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) = 0 AND len(@Series) = 0 AND len(@Volume) = 0 AND len(@RefYear) > 0) SET @NomRefCache = @Refyear + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) = 0 AND len(@Series) = 0 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = NULL
//
//
//
//
//
//
//	--Edition and/or Series is not numeric
//
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @Edition +  @comma + @blank + @Series + @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) = 0 AND len(@RefYear) > 0) SET @NomRefCache = @TitelAbbrev + @blank + @Edition +  @comma + @blank + @Series + @comma +  @blank + @Refyear + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @Edition +  @comma + @blank + @Series + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) = 0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @Edition +  @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) = 0 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @Edition +  @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) = 0 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @Series + @comma + @blank + @Volume + @dot
//
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) = 0 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @Series + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @Edition +  @comma + @blank + @Series + @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @Edition +  @comma + @blank + @Series + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) = 0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @Edition +  @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) = 0 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @Edition +  @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) = 0 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @Series + @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) = 0 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @Series + @dot
//
//
//
//
//	--Edition is numeric and series is not numeric
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @prefixEdition + @blank + @Edition +  @comma + @blank + @Series + @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) = 0 AND len(@RefYear) > 0) SET @NomRefCache = @TitelAbbrev + @blank + @prefixEdition + @blank + @Edition +  @comma + @blank + @Series + @comma +  @blank + @Refyear + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @prefixEdition + @blank + @Edition +  @comma + @blank + @Series + @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=1 AND len(@Series) > 0 AND isnumeric(@Series)=0 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @prefixEdition + @blank + @Edition+  @comma + @blank + @Series + @dot
//
//
//
//	--Series is numeric and editon is not numeric
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @TitelAbbrev + @blank + @Edition +  @comma + @blank + @prefixSeries + @blank + @Series + @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) > 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) = 0 AND len(@RefYear) > 0) SET @NomRefCache = @TitelAbbrev + @blank + @Edition +  @comma + @blank + @prefixSeries + @blank + @Series + @comma +  @blank + @Refyear + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) > 0 AND len(@RefYear) = 0) SET @NomRefCache = @Edition +  @comma + @blank + @prefixSeries + @blank + @Series + @comma + @blank + @Volume + @dot
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0 AND len(@Edition) > 0 AND isnumeric(@Edition)=0 AND len(@Series) > 0 AND isnumeric(@Series)=1 AND len(@Volume) = 0 AND len(@RefYear) = 0) SET @NomRefCache = @Edition+  @comma + @blank + @prefixSeries + @blank + @Series + @dot
//
//	--Changes (Marc Geoffroy)
//
//		IF (len(@Authorteam) = 0 AND len(@TitelAbbrev) = 0)  SET @NomRefCache = NULL
//
//
//		Return @NomRefCache

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
