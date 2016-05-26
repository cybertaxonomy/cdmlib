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
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.reference.NewDefaultReferenceCacheStrategy;

public class ArticleDefaultCacheStrategy extends NomRefDefaultCacheStrategyBase {
	private static final long serialVersionUID = -1639068590864589314L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ArticleDefaultCacheStrategy.class);

//	public static final String UNDEFINED_JOURNAL = "- undefined journal -";
	private String prefixReferenceJounal = "in";
	private String blank = " ";
	private String comma = ",";
	private String prefixSeries = "ser.";


	final static UUID uuid = UUID.fromString("0d45343a-0c8a-4a64-97ca-e94974b65c96");

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
	public String getTitleCache(Reference article) {
		if (article.isProtectedTitleCache()){
			return article.getTitleCache();
		}
		String result =  getTitleWithoutYearAndAuthor(article, false);
		result = addYear(result, article, false);
		TeamOrPersonBase<?> team = article.getAuthorship();
		result = CdmUtils.concat(" ", article.getTitle(), result);
		if (team != null &&  isNotBlank(team.getTitleCache())){
			String authorSeparator = isNotBlank(article.getTitle())? afterAuthor : " ";
			result = team.getTitleCache() + authorSeparator + result;
		}
		return result;
	}

	@Override
	public String getFullAbbrevTitleString(Reference article) {
		if (article.isProtectedAbbrevTitleCache()){
			return article.getAbbrevTitleCache();
		}
		String result =  getTitleWithoutYearAndAuthor(article, true);
		result = addYear(result, article, false);
		TeamOrPersonBase<?> team = article.getAuthorship();
		String articleTitle = CdmUtils.getPreferredNonEmptyString(article.getAbbrevTitle(), article.getTitle(), false, true);
		result = CdmUtils.concat(" ", articleTitle, result);  //Article should maybe left out for nomenclatural references (?)
		if (team != null &&  isNotBlank(team.getNomenclaturalTitle())){
			String authorSeparator = isNotBlank(articleTitle) ? afterAuthor : " ";
			result = team.getNomenclaturalTitle() + authorSeparator + result;
		}
		return result;
	}

	@Override
	protected String getTitleWithoutYearAndAuthor(Reference article, boolean isAbbrev){
		if (article == null){
			return null;
		}
		IJournal journal = article.getInReference();

		String journalTitel;
		if (journal != null){
			journalTitel = CdmUtils.getPreferredNonEmptyString(journal.getTitle(), journal.getAbbrevTitle(), isAbbrev, true);
		}else{
			journalTitel = NewDefaultReferenceCacheStrategy.UNDEFINED_JOURNAL;
		}

		String series = Nz(article.getSeriesPart()).trim();
		String volume = Nz(article.getVolume()).trim();

		boolean needsComma = false;

		String nomRefCache = "";

		//inJournal
		nomRefCache = prefixReferenceJounal + blank;

		//titelAbbrev
		if (isNotBlank(journalTitel)){
			nomRefCache = nomRefCache + journalTitel;
			needsComma = makeNeedsComma(needsComma, nomRefCache, volume, series);
			if (! needsComma){
				nomRefCache = nomRefCache + blank;
			}
		}

		//series and vol.
		nomRefCache = getSeriesAndVolPart(series, volume, needsComma, nomRefCache);

		//delete "."
		while (nomRefCache.endsWith(".")){
			nomRefCache = nomRefCache.substring(0, nomRefCache.length()-1);
		}

		return nomRefCache.trim();
	}

	private boolean makeNeedsComma(boolean needsComma, String nomRefCache, String volume, String series) {
		if (needsComma){
			return true;
		}else{
			nomRefCache = nomRefCache.toLowerCase();
			int serIndex = nomRefCache.indexOf(" ser. ");
			int sectIndex = nomRefCache.indexOf(" sect. ");
			int abtIndex = nomRefCache.indexOf(" abt. ");
			int index = Math.max(Math.max(serIndex, sectIndex), abtIndex);
			int commaIndex = nomRefCache.indexOf(",", index);
			if (index > -1 && commaIndex == -1 && isNotBlank(volume)){
				return true;
			}else if (isNotBlank(series)){
				return true;
			}else{
				return false;
			}
		}
	}

	protected String getSeriesAndVolPart(String series, String volume,
			boolean needsComma, String nomRefCache) {
		//inSeries
		String seriesPart = "";
		if (isNotBlank(series)){
			seriesPart = series;
			if (CdmUtils.isNumeric(series)){
				seriesPart = prefixSeries + blank + seriesPart;
			}
//			if (needsComma){
				seriesPart = comma + blank + seriesPart;
//			}
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
		return nomRefCache;
	}
}
