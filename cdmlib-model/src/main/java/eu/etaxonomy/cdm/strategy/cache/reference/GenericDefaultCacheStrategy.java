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

public class GenericDefaultCacheStrategy extends InRefDefaultCacheStrategyBase implements INomenclaturalReferenceCacheStrategy {
	private static final long serialVersionUID = 6687224678019228192L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GenericDefaultCacheStrategy.class);


	private static final String inRefTypeStr = "generic reference";

	@Override
	protected String getInRefType() {
		return inRefTypeStr;
	}

	private String prefixEdition = "ed.";
	private String prefixSeries = "ser.";
//	private String prefixVolume = "vol.";
	private String blank = " ";
	private String comma = ",";
//	private String dot =".";

	final static UUID uuid = UUID.fromString("95cceb30-6b16-4dc3-8243-c15e746565bc");

	@Override
	protected UUID getUuid() {
		return uuid;
	}


	/**
	 * Factory method
	 * @return
	 */
	public static GenericDefaultCacheStrategy NewInstance(){
		return new GenericDefaultCacheStrategy();
	}

	/**
	 * Constructor
	 */
	private GenericDefaultCacheStrategy(){
		super();
	}


	@Override
	protected String getTitleWithoutYearAndAuthor(Reference genericReference, boolean isAbbrev){
		if (genericReference == null){
			return null;
		}
		//TODO
		String titel = CdmUtils.getPreferredNonEmptyString(genericReference.getTitle(), genericReference.getAbbrevTitle(), isAbbrev, true);
		String edition = CdmUtils.Nz(genericReference.getEdition());
		//TODO
		String series = CdmUtils.Nz(genericReference.getSeriesPart()).trim(); //nomenclaturalReference.getSeries();
		String volume = CdmUtils.Nz(genericReference.getVolume()).trim();

		String result = "";
		boolean lastCharIsDouble;
		Integer len;
		String lastChar ="";
		String character =".";
		len = titel.length();
		if (len > 0){lastChar = titel.substring(len-1, len);}
		//lastCharIsDouble = f_core_CompareStrings(RIGHT(@TitelAbbrev,1),character);
		lastCharIsDouble = titel.equals(character);

//		if(lastCharIsDouble  && edition.length() == 0 && series.length() == 0 && volume.length() == 0 && refYear.length() > 0 ){
//			titelAbbrev =  titelAbbrev.substring(1, len-1); //  SUBSTRING(@TitelAbbrev,1,@LEN-1)
//		}


		boolean needsComma = false;
		//titelAbbrev
		if (titel.length() > 0 ){
			String postfix = StringUtils.isNotBlank(edition) ? "" : blank;
			result = titel + postfix;
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
		result = CdmUtils.concat(", ", result, editionPart);

		//inSeries
		String seriesPart = "";
		if (isNotBlank(series)){
			seriesPart = series;
			if (isNumeric(series)){
				seriesPart = prefixSeries + blank + seriesPart;
			}
			if (needsComma){
				seriesPart = comma + seriesPart;
			}
			needsComma = true;
		}
		result += seriesPart;


		//volume Part
		String volumePart = "";
		if (!"".equals(volume)){
			volumePart = volume;
			if (needsComma){
				volumePart = comma + blank + volumePart;
			}
			//needsComma = false;
		}
		result += volumePart;

		//delete .
		while (result.endsWith(".")){
			result = result.substring(0, result.length()-1);
		}



		//	--Edition and series are null or numeric

//		if (isNumeric(edition) ){
//			if (titelAbbrev.length() > 0 && edition.length() > 0 &&  series.length() > 0 && isNumeric(series) && volume.length() > 0 && refYear.length() > 0 ){
//				nomRefCache = titelAbbrev + blank + prefixEdition + blank + edition + comma + blank + prefixSeries + blank + series + comma + blank + volume + dot + blank + refYear + dot;
//			}
//		}


//  FROM BERLIN MODEL TRIGGER:
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

		return result.trim();
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
