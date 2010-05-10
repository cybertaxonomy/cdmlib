/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.name;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * This class is a default implementation for the INonViralNameCacheStrategy<T extends NonViralName> interface.
 * The method actually implements a cache strategy for botanical names so no method has to be overwritten by
 * a subclass for botanic names.
 * Where differing from this Default BotanicNameCacheStrategy other subclasses should overwrite the existing methods
 * e.g. a CacheStrategy for zoological names should overwrite getAuthorAndExAuthor
 * @author a.mueller
 */
/**
 * @author AM
 *
 * @param <T>
 */
public class NonViralNameDefaultCacheStrategy<T extends NonViralName> extends NameCacheStrategyBase<T> implements INonViralNameCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(NonViralNameDefaultCacheStrategy.class);
	
	final static UUID uuid = UUID.fromString("1cdda0d1-d5bc-480f-bf08-40a510a2f223");
	
	protected String NameAuthorSeperator = " ";
	protected String BasionymStart = "(";
	protected String BasionymEnd = ")";
	protected String ExAuthorSeperator = " ex ";
	protected CharSequence BasionymAuthorCombinationAuthorSeperator = " ";
	
	@Override
	public  UUID getUuid(){
		return uuid;
	}

	
	/**
	 * Factory method
	 * @return NonViralNameDefaultCacheStrategy A new instance of  NonViralNameDefaultCacheStrategy
	 */
	public static NonViralNameDefaultCacheStrategy NewInstance(){
		return new NonViralNameDefaultCacheStrategy();
	}
	
	/**
	 * Constructor
	 */
	protected NonViralNameDefaultCacheStrategy(){
		super();
	}

/* **************** GETTER / SETTER **************************************/
	
	/**
	 * String that separates the NameCache part from the AuthorCache part
	 * @return
	 */
	public String getNameAuthorSeperator() {
		return NameAuthorSeperator;
	}


	public void setNameAuthorSeperator(String nameAuthorSeperator) {
		NameAuthorSeperator = nameAuthorSeperator;
	}


	/**
	 * String the basionym author part starts with e.g. '('.
	 * This should correspond with the {@link NonViralNameDefaultCacheStrategy#getBasionymEnd() basionymEnd} attribute
	 * @return
	 */
	public String getBasionymStart() {
		return BasionymStart;
	}


	public void setBasionymStart(String basionymStart) {
		BasionymStart = basionymStart;
	}


	/**
	 * String the basionym author part ends with e.g. ')'.
	 * This should correspond with the {@link NonViralNameDefaultCacheStrategy#getBasionymStart() basionymStart} attribute
	 * @return
	 */
	public String getBasionymEnd() {
		return BasionymEnd;
	}


	public void setBasionymEnd(String basionymEnd) {
		BasionymEnd = basionymEnd;
	}


	/**
	 * String to seperate ex author from author.
	 * @return
	 */
	public String getExAuthorSeperator() {
		return ExAuthorSeperator;
	}


	public void setExAuthorSeperator(String exAuthorSeperator) {
		ExAuthorSeperator = exAuthorSeperator;
	}


	/**
	 * String that seperates the basionym/original_combination author part from the combination author part
	 * @return
	 */
	public CharSequence getBasionymAuthorCombinationAuthorSeperator() {
		return BasionymAuthorCombinationAuthorSeperator;
	}


	public void setBasionymAuthorCombinationAuthorSeperator(
			CharSequence basionymAuthorCombinationAuthorSeperator) {
		BasionymAuthorCombinationAuthorSeperator = basionymAuthorCombinationAuthorSeperator;
	}

	
//** *****************************************************************************************/
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getNameCache()
	 */
	@Override
	public String getTitleCache(T nonViralName) {
		if (nonViralName == null){
			return null;
		}
		
		if (nonViralName.isProtectedTitleCache()){
			return nonViralName.getTitleCache();
		}
		String result = "";
		//Autonym
		if (isAutonym(nonViralName)){
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
		}
		return result;
	}
	
	protected boolean nameIncludesAuthorship(NonViralName nonViralName){
		Rank rank = nonViralName.getRank();
		if (rank != null && rank.isSpeciesAggregate()){
			return false;
		}else{
			return true;
		}
	}
	
	
	@Override
	public String getFullTitleCache(T nonViralName) {
		//null
		if (nonViralName == null){
			return null;
		}
		//full title cache
		if (nonViralName.isProtectedFullTitleCache() == true) {
			return nonViralName.getFullTitleCache();
		}
		
		String result = "";
		//title cache
		String titleCache = nonViralName.getTitleCache();   // OLD: getTitleCache(nonViralName);
		
		String microReference = nonViralName.getNomenclaturalMicroReference();
		INomenclaturalReference ref = nonViralName.getNomenclaturalReference();
		String referenceBaseCache = null;
		if (ref != null){
			INomenclaturalReference nomenclaturalReference = HibernateProxyHelper.deproxy(ref, INomenclaturalReference.class);
			nomenclaturalReference.setCacheStrategy(nomenclaturalReference.getType().getCacheStrategy());
			referenceBaseCache = nomenclaturalReference.getNomenclaturalCitation(microReference);
		}
		
		//make nomenclatural status
		String ncStatusCache = "";
		Set<NomenclaturalStatus> ncStati = nonViralName.getStatus();
		Iterator<NomenclaturalStatus> iterator = ncStati.iterator();
		while (iterator.hasNext()) {
			NomenclaturalStatus ncStatus = (NomenclaturalStatus)iterator.next();
			NomenclaturalStatusType statusType =  ncStatus.getType();
			Language lang = Language.LATIN();
			Representation repr = statusType.getRepresentation(lang);
			ncStatusCache = ", " + repr.getAbbreviatedLabel();
		}
		String refConcat = " ";
		if (referenceBaseCache != null && ! referenceBaseCache.trim().startsWith("in ")){
			refConcat = ", ";
		}
		result = CdmUtils.concat(refConcat, titleCache, referenceBaseCache);
		result = CdmUtils.concat("", result, ncStatusCache);
		return result;
	}
	
	
	/**
	 * Generates and returns the "name cache" (only scientific name without author teams and year).
	 * @see eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy#getNameCache(eu.etaxonomy.cdm.model.name.TaxonNameBase)
	 */
	public String getNameCache(T nonViralName) {
		if (nonViralName == null){
			return null;
		}
		String result;
		Rank rank = nonViralName.getRank();
		
		if (nonViralName.isProtectedNameCache()){
			result = nonViralName.getNameCache();
		}else if (rank == null){
			result = getRanklessNameCache(nonViralName);
		}else if (rank.isInfraSpecific()){
			result = getInfraSpeciesNameCache(nonViralName);
		}else if (rank.isSpecies()){
			result = getSpeciesNameCache(nonViralName);
		}else if (rank.isInfraGeneric()){
			result = getInfraGenusNameCache(nonViralName);
		}else if (rank.isGenus()){
			result = getGenusOrUninomialNameCache(nonViralName);
		}else if (rank.isSupraGeneric()){
			result = getGenusOrUninomialNameCache(nonViralName);
		}else{ 
			logger.warn("Name Strategy for Name (UUID: " + nonViralName.getUuid() +  ") not yet implemented");
			result = "";
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy#getAuthorCache(eu.etaxonomy.cdm.model.name.NonViralName)
	 */
	public String getAuthorshipCache(T nonViralName) {
		if (nonViralName == null){
			return null;
		}
		//cache protected
		if (nonViralName.isProtectedAuthorshipCache() == true) {
			return nonViralName.getAuthorshipCache();
		}
		return getNonCacheAuthorshipCache(nonViralName);

	}
	
	/**
	 * Returns the authorshipcache string for the atomized authorship fields. Does not use the authorshipfield.
	 * @throws NullPointerException if nonViralName is null.
	 * @param nonViralName
	 * @return
	 */
	protected String getNonCacheAuthorshipCache(T nonViralName){
		String result = "";
		INomenclaturalAuthor combinationAuthor = nonViralName.getCombinationAuthorTeam();
		INomenclaturalAuthor exCombinationAuthor = nonViralName.getExCombinationAuthorTeam();
		INomenclaturalAuthor basionymAuthor = nonViralName.getBasionymAuthorTeam();
		INomenclaturalAuthor exBasionymAuthor = nonViralName.getExBasionymAuthorTeam();
		String basionymPart = "";
		String authorPart = "";
		//basionym
		if (basionymAuthor != null || exBasionymAuthor != null){
			basionymPart = BasionymStart + getAuthorAndExAuthor(basionymAuthor, exBasionymAuthor) + BasionymEnd;
		}
		if (combinationAuthor != null || exCombinationAuthor != null){
			authorPart = getAuthorAndExAuthor(combinationAuthor, exCombinationAuthor);
		}
		result = CdmUtils.concat(BasionymAuthorCombinationAuthorSeperator, basionymPart, authorPart);
		return result;
	}
	
	/**
	 * Returns the AuthorCache part for a combination of an author and an ex author. This applies on combination authors
	 * as well as on basionym/orginal combination authors.
	 * @param author the author
	 * @param exAuthor the ex-author
	 * @return
	 */
	protected String getAuthorAndExAuthor(INomenclaturalAuthor author, INomenclaturalAuthor exAuthor){
		String result = "";
		String authorString = "";
		String exAuthorString = "";
		if (author != null){
			authorString = CdmUtils.Nz(author.getNomenclaturalTitle());
		}
		if (exAuthor != null){
			exAuthorString = CdmUtils.Nz(exAuthor.getNomenclaturalTitle());
		}
		if (exAuthorString.length() > 0 ){
			exAuthorString = exAuthorString + ExAuthorSeperator;
		}
		result = exAuthorString + authorString;
		return result;
 
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getTaggedName(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	public List<Object> getTaggedName(T nonViralName) {
		List<Object> tags = new ArrayList<Object>();
		tags.add(nonViralName.getGenusOrUninomial());
		if (nonViralName.isSpecies() || nonViralName.isInfraSpecific()){
			tags.add(nonViralName.getSpecificEpithet());			
		}
		
		// No autonym 
		if (nonViralName.isInfraSpecific() && ! nonViralName.getSpecificEpithet().equals(nonViralName.getInfraSpecificEpithet())){
			tags.add(nonViralName.getRank());			
			tags.add(nonViralName.getInfraSpecificEpithet());			
		}
		
		if (nonViralName.isInfraGeneric()){
			//TODO choose right strategy or generic approach?
			// --- strategy 1 --- 
			tags.add(nonViralName.getRank());			
			tags.add(nonViralName.getInfraGenericEpithet());			
			// --- strategy 2 --- 
//			tags.add('('+nvn.getInfraGenericEpithet()+')');	
		}
		Team authorTeam = Team.NewInstance();
		authorTeam.setProtectedTitleCache(true);
		authorTeam.setTitleCache(nonViralName.getAuthorshipCache());
		tags.add(authorTeam);
		
		// Name is an autonym. Rank and infraspecific eitheton follow the author
		if (nonViralName.isInfraSpecific() && nonViralName.getSpecificEpithet().equals(nonViralName.getInfraSpecificEpithet())){
			tags.add(nonViralName.getRank());			
			tags.add(nonViralName.getInfraSpecificEpithet());			
		}
		
		if(! "".equals(nonViralName.getAppendedPhrase())){
			tags.add(nonViralName.getAppendedPhrase());
		}
		
		return tags;
	}
	

	/************** PRIVATES ****************/
		
		protected String getRanklessNameCache(NonViralName nonViralName){
			String result = "";
			result = (result + (nonViralName.getGenusOrUninomial())).trim().replace("null", "");
			result += " " + (CdmUtils.Nz(nonViralName.getSpecificEpithet())).trim();
			result += " " + (CdmUtils.Nz(nonViralName.getInfraSpecificEpithet())).trim();
			result = result.trim().replace("null", "");
			//result += " (rankless)";
			result = addAppendedPhrase(result, nonViralName);
			return result;			
		}
	
	
		protected String getGenusOrUninomialNameCache(NonViralName nonViralName){
			String result;
			result = CdmUtils.Nz(nonViralName.getGenusOrUninomial());
			result = addAppendedPhrase(result, nonViralName).trim();
			return result;
		}
		
		protected String getInfraGenusNameCache(NonViralName nonViralName){
			String result;
			Rank rank = nonViralName.getRank();
			if (rank.isSpeciesAggregate()){
				return getSpeciesAggregateCache(nonViralName);
			}
			String infraGenericMarker = "'unhandled infrageneric rank'";
			if (rank != null){
				try {
					infraGenericMarker = rank.getInfraGenericMarker();
				} catch (UnknownCdmTypeException e) {
					infraGenericMarker = "'unhandled infrageneric rank'";
				}
			}
			result = CdmUtils.Nz(nonViralName.getGenusOrUninomial());
			result += " " + infraGenericMarker + " " + (CdmUtils.Nz(nonViralName.getInfraGenericEpithet())).trim().replace("null", "");
			result = addAppendedPhrase(result, nonViralName).trim();
			return result;
		}

//		aggr.|agg.|group
		protected String getSpeciesAggregateCache(NonViralName nonViralName){
			String result;
			result = CdmUtils.Nz(nonViralName.getGenusOrUninomial());
			result += " " + CdmUtils.Nz(nonViralName.getSpecificEpithet()).trim().replace("null", "");
			String marker;
			try {
				marker = nonViralName.getRank().getInfraGenericMarker();
			} catch (UnknownCdmTypeException e) {
				marker = "'unknown aggregat type'";
			}
			result += " " + marker;
			result = addAppendedPhrase(result, nonViralName).trim();
			return result;
		}
		
		protected String getSpeciesNameCache(NonViralName nonViralName){
			String result;
			result = CdmUtils.Nz(nonViralName.getGenusOrUninomial());
			result += " " + CdmUtils.Nz(nonViralName.getSpecificEpithet()).trim().replace("null", "");
			result = addAppendedPhrase(result, nonViralName).trim();
			result = result.replace("\\s\\", " ");
			return result;
		}
		
		
		protected String getInfraSpeciesNameCache(NonViralName nonViralName){
			return getInfraSpeciesNameCache(nonViralName, true);
		}
		
		protected String getInfraSpeciesNameCache(NonViralName nonViralName, boolean includeMarker){
			String result;
			result = CdmUtils.Nz(nonViralName.getGenusOrUninomial());
			result += " " + (CdmUtils.Nz(nonViralName.getSpecificEpithet()).trim()).replace("null", "");
			if (includeMarker){ 
				result += " " + (nonViralName.getRank().getAbbreviation()).trim().replace("null", "");
			}
			result += " " + (CdmUtils.Nz(nonViralName.getInfraSpecificEpithet())).trim().replace("null", "");
			result = addAppendedPhrase(result, nonViralName).trim();
			return result;
		}

		
		
		/**
		 * @param name
		 * @return true, if name has Rank, Rank is below species and species epithet equals infraSpeciesEpithtet, else false
		 */
		protected boolean isAutonym(NonViralName nonViralName){
			if (nonViralName != null && nonViralName.getRank() != null && nonViralName.getSpecificEpithet() != null && nonViralName.getInfraSpecificEpithet() != null && 
					nonViralName.getRank().isInfraSpecific() && nonViralName.getSpecificEpithet().trim().equals(nonViralName.getInfraSpecificEpithet().trim())){
				return true;
			}else{
				return false;
			}
		}
		
		protected String addAppendedPhrase(String resultString, NonViralName nonViralName){
			String appendedPhrase = nonViralName ==null ? null : nonViralName.getAppendedPhrase();
			if (resultString == null){
				return appendedPhrase;
			}else if(appendedPhrase == null || "".equals(appendedPhrase.trim())) {
				return resultString;
			}else if ("".equals(resultString)){
				return resultString + appendedPhrase;
			}else {
				return resultString + " " + appendedPhrase;
			}
		}


		public String getLastEpithet(T taxonNameBase) {
			Rank rank = taxonNameBase.getRank();
			if(rank.isGenus() || rank.isSupraGeneric()) {
				return taxonNameBase.getGenusOrUninomial();
			} else if(rank.isInfraGeneric()) {
				return taxonNameBase.getInfraGenericEpithet();
			} else if(rank.isSpecies()) {
				return taxonNameBase.getSpecificEpithet();
			} else {
				return taxonNameBase.getInfraSpecificEpithet();
			}
		}
}
