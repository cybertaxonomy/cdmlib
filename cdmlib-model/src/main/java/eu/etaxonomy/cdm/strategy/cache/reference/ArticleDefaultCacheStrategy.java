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
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.Reference;

public class ArticleDefaultCacheStrategy <T extends Reference> extends NomRefDefaultCacheStrategyBase<T> implements  INomenclaturalReferenceCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(ArticleDefaultCacheStrategy.class);
	
	public static final String UNDEFINED_JOURNAL = "- undefined journal -";
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
	public String getTitleCache(T article) {
		if (article.isProtectedTitleCache()){
			return article.getTitleCache();
		}
		String result =  getNomRefTitleWithoutYearAndAuthor(article);
		result = addYear(result, article);
		TeamOrPersonBase<?> team = article.getAuthorTeam();
		result = CdmUtils.concat(" ", article.getTitle(), result);
		if (team != null &&  CdmUtils.isNotEmpty(team.getTitleCache())){
			String authorSeparator = StringUtils.isNotBlank(article.getTitle())? afterAuthor : " ";
			result = team.getTitleCache() + authorSeparator + result;
		}
		return result;
	}


	@Override
	protected String getNomRefTitleWithoutYearAndAuthor(T article){
		if (article == null){
			return null;
		}
		boolean hasJournal = (article.getInReference() != null);
		
		String titelAbbrev;
		if (hasJournal){
			titelAbbrev = CdmUtils.Nz(article.getInReference().getTitle()).trim();
		}else{
			titelAbbrev = UNDEFINED_JOURNAL;
		}
		
		String series = CdmUtils.Nz(article.getSeries()).trim();
		String volume = CdmUtils.Nz(article.getVolume()).trim();
		
		boolean needsComma = false;
		
		String nomRefCache = "";

		//inJournal
		nomRefCache = prefixReferenceJounal + blank; 
		
		//titelAbbrev
		if (CdmUtils.isNotEmpty(titelAbbrev)){
			nomRefCache = nomRefCache + titelAbbrev + blank; 
		}
		
		//inSeries
		String seriesPart = "";
		if (!"".equals(series)){
			seriesPart = series;
			if (CdmUtils.isNumeric(series)){
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
	
}
