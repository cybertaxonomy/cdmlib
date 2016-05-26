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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.reference.Reference;

public class BookDefaultCacheStrategy extends NomRefDefaultCacheStrategyBase {
	static final long serialVersionUID = -8535065052672341462L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BookDefaultCacheStrategy.class);

	private String prefixEdition = "ed.";
	private String prefixSeries = ", ser.";
	private String blank = " ";
	private String comma = ",";

	final static UUID uuid = UUID.fromString("68076ca5-d517-489c-8ae2-01d3c38cc788");

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


	@Override
	public String getTitleWithoutYearAndAuthor(Reference ref, boolean isAbbrev){
		if (ref == null){
			return null;
		}
		//TODO
		String title = CdmUtils.getPreferredNonEmptyString(ref.getTitle(), ref.getAbbrevTitle(), isAbbrev, true);
		String edition = Nz(ref.getEdition()).trim();
		String series = ""; //TODO ref.getSeries();  //SeriesPart is handled later
		String refSeriesPart = Nz(ref.getSeriesPart());
		String volume = CdmUtils.Nz(ref.getVolume()).trim();
		String refYear = "";  //TODO nomenclaturalReference.getYear();


		String nomRefCache = "";
		Integer len;
		String lastChar = "";
		String character =".";
		len = title.length();
		if (len > 0){
			lastChar = title.substring(len-1, len);
		}
		//lastCharIsDouble = f_core_CompareStrings(RIGHT(@TitelAbbrev,1),character);
		boolean lastCharIsDouble = title.equals(character);

		if(lastCharIsDouble  && edition.length() == 0 && refSeriesPart.length() == 0 && volume.length() == 0 && refYear.length() > 0 ){
			title =  title.substring(1, len-1); //  SUBSTRING(@TitelAbbrev,1,@LEN-1)
		}


		boolean needsComma = false;
		//titelAbbrev
		if (!"".equals(title) ){
			String postfix = isNotBlank(edition + refSeriesPart) ? "" : blank;
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
		if (StringUtils.isNotBlank(refSeriesPart)){
			seriesPart = refSeriesPart;
			if (isNumeric(refSeriesPart)){
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
			}else{
				volumePart = "" + volumePart;
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
