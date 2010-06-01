/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.name;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

public class ZooNameDefaultCacheStrategy <T extends ZoologicalName> extends NonViralNameDefaultCacheStrategy<T> implements  INonViralNameCacheStrategy<T> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ZooNameDefaultCacheStrategy.class);
	
	final static UUID uuid = UUID.fromString("950c4236-8156-4675-b866-785df33bc4d9");

	protected String AuthorYearSeperator = ", ";
	
	@Override
	public UUID getUuid(){
		return uuid;
	}
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static ZooNameDefaultCacheStrategy NewInstance(){
		return new ZooNameDefaultCacheStrategy();
	}
	
	/**
	 * Constructor
	 */
	private ZooNameDefaultCacheStrategy(){
		super();
	}
	
	@Override
	protected String getSpeciesNameCache(NonViralName nonViralName){
		String result;
		result = CdmUtils.Nz(nonViralName.getGenusOrUninomial());
		if (CdmUtils.isNotEmpty(nonViralName.getInfraGenericEpithet())){
			result += " (" + nonViralName.getInfraGenericEpithet().trim() + ")";
		}
		
		result += " " + CdmUtils.Nz(nonViralName.getSpecificEpithet()).trim().replace("null", "");
		result = addAppendedPhrase(result, nonViralName).trim();
		result = result.replace("\\s\\", " ");
		return result;
	}
	
	@Override
	protected String getInfraGenusNameCache(NonViralName zooName){
		String result;
		Rank rank = zooName.getRank();
		if (rank.isSpeciesAggregate()){
			return getSpeciesAggregateCache(zooName);
		}
		//String infraGenericMarker = "'unhandled infrageneric rank'";
		
		result = CdmUtils.Nz(zooName.getGenusOrUninomial());
		 
		result += " (" + (CdmUtils.Nz(zooName.getInfraGenericEpithet())).trim().replace("null", "") + ")";
		result = addAppendedPhrase(result, zooName).trim();
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getNonCacheAuthorshipCache(eu.etaxonomy.cdm.model.name.NonViralName)
	 */
	@Override
	protected String getNonCacheAuthorshipCache(T nonViralName) {
		if (nonViralName == null){
			return null;
		}
		String result = "";
		INomenclaturalAuthor combinationAuthor = nonViralName.getCombinationAuthorTeam();
		INomenclaturalAuthor exCombinationAuthor = nonViralName.getExCombinationAuthorTeam();
		INomenclaturalAuthor basionymAuthor = nonViralName.getBasionymAuthorTeam();
		INomenclaturalAuthor exBasionymAuthor = nonViralName.getExBasionymAuthorTeam();
		Integer publicationYear = nonViralName.getPublicationYear();
		Integer originalPublicationYear = nonViralName.getOriginalPublicationYear();

		String basionymPart = "";
		String authorPart = "";
		//basionym
		if (basionymAuthor != null || exBasionymAuthor != null || originalPublicationYear != null ){
			String authorAndEx = getAuthorAndExAuthor(basionymAuthor, exBasionymAuthor);
			String originalPublicationYearString = originalPublicationYear == null ? null : String.valueOf(originalPublicationYear);
			String authorAndExAndYear = CdmUtils.concat(", ", authorAndEx, originalPublicationYearString );
			basionymPart = BasionymStart + authorAndExAndYear +BasionymEnd;
		}
		if (combinationAuthor != null || exCombinationAuthor != null){
			String authorAndEx = getAuthorAndExAuthor(combinationAuthor, exCombinationAuthor);
			String publicationYearString = publicationYear == null ? null : String.valueOf(publicationYear);
			authorPart = CdmUtils.concat(", ", authorAndEx, publicationYearString);
		}
		result = CdmUtils.concat(BasionymAuthorCombinationAuthorSeperator, basionymPart, authorPart);
		return result;
	}
	

	/**
	 * @return Strings that separates the author part and the year part
	 * @return
	 */
	public String getAuthorYearSeperator() {
		return AuthorYearSeperator;
	}


	public void setAuthorYearSeperator(String authorYearSeperator) {
		AuthorYearSeperator = authorYearSeperator;
	}
	
	protected String getInfraSpeciesNameCache(NonViralName nonViralName){
		//boolean includeMarker = ! isAutonym(nonViralName);
		boolean includeMarker = false;
		return getInfraSpeciesNameCache(nonViralName, includeMarker);
	}
	
	
	/*public String getTitleCache(T nonViralName) {
		if (nonViralName == null){
			return null;
		}
		
		if (nonViralName.isProtectedTitleCache()){
			return nonViralName.getTitleCache();
		}
		String result = "";
		//Autonym
		/*if (isAutonym(nonViralName)){
			String speciesPart = getSpeciesNameCache(nonViralName);
			//TODO should this include basionym authors and ex authors
			INomenclaturalAuthor author = nonViralName.getCombinationAuthorTeam();
			String authorPart = "";
			if (author != null){
				authorPart = CdmUtils.Nz(author.getNomenclaturalTitle());
			}
			INomenclaturalAuthor basAuthor = nonViralName.getBasionymAuthorTeam();
			String basAuthorPart = "";
			if (basAuthor != null){
				basAuthorPart = CdmUtils.Nz(basAuthor.getNomenclaturalTitle());
			}
			if (! "".equals(basAuthorPart)){
				authorPart = "("+ basAuthorPart +")" + authorPart;
			}
			String infraSpeciesPart = (CdmUtils.Nz(nonViralName.getInfraSpecificEpithet()));

			String infraSpeciesSeparator = "";
			if (nonViralName.getRank() == null || !nonViralName.getRank().isInfraSpecific()){
				//TODO handle exception
				logger.warn("Rank for autonym does not exist or is not lower than species !!");
			}else{
				infraSpeciesSeparator = nonViralName.getRank().getAbbreviation();
			}
			
			result = CdmUtils.concat(" ", new String[]{speciesPart, authorPart, infraSpeciesSeparator, infraSpeciesPart});
			result = result.trim().replace("null", "");
		}else{ //not Autonym
			String nameCache = nonViralName.getNameCache();  //OLD: CdmUtils.Nz(getNameCache(nonViralName));
			if (nameIncludesAuthorship(nonViralName)){
				String authorCache = CdmUtils.Nz(getAuthorshipCache(nonViralName));
				result = CdmUtils.concat(NameAuthorSeperator, nameCache, authorCache);
			}else{
				result = nameCache;
			}
		//}
		return result;
	}
*/
}
