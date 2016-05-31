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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImplRegExBase;


/**
 * This class is a default implementation for the INonViralNameCacheStrategy<T extends NonViralName>
 * interface.<BR>
 * The method implements a cache strategy for botanical names so no method has to be overwritten by
 * a subclass for botanic names.
 * Where differing from this default botanic name strategy other subclasses should overwrite the
 * existing methods, e.g. a CacheStrategy for zoological names should overwrite getAuthorAndExAuthor
 * @author a.mueller
 */
public class NonViralNameDefaultCacheStrategy<T extends NonViralName<?>>
        extends NameCacheStrategyBase<T>
        implements INonViralNameCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(NonViralNameDefaultCacheStrategy.class);
	private static final long serialVersionUID = -6577757501563212669L;

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
     * Factory method
     * @return NonViralNameDefaultCacheStrategy A new instance of  NonViralNameDefaultCacheStrategy
     */
    public static <T extends NonViralName<?>> NonViralNameDefaultCacheStrategy<T> NewInstance(Class<T> clazz){
        return new NonViralNameDefaultCacheStrategy<T>();
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
     * String to separate ex author from author.
     * @return
     */
    public String getExAuthorSeperator() {
        return ExAuthorSeperator;
    }


    public void setExAuthorSeperator(String exAuthorSeperator) {
        ExAuthorSeperator = exAuthorSeperator;
    }


    /**
     * String that separates the basionym/original_combination author part from the combination author part
     * @return
     */
    public CharSequence getBasionymAuthorCombinationAuthorSeperator() {
        return BasionymAuthorCombinationAuthorSeperator;
    }


    public void setBasionymAuthorCombinationAuthorSeperator( CharSequence basionymAuthorCombinationAuthorSeperator) {
        BasionymAuthorCombinationAuthorSeperator = basionymAuthorCombinationAuthorSeperator;
    }


//** *****************************************************************************************/

    @Override
    public String getTitleCache(T nonViralName) {
    	return getTitleCache(nonViralName, null);
    }

    @Override
	public String getTitleCache(T nonViralName, HTMLTagRules htmlTagRules) {
    	List<TaggedText> tags = getTaggedTitle(nonViralName);
		if (tags == null){
			return null;
		}else{
			String result = createString(tags, htmlTagRules);
		    return result;
		}
    }

	@Override
	public String getFullTitleCache(T nonViralName, HTMLTagRules htmlTagRules) {
		List<TaggedText> tags = getTaggedFullTitle(nonViralName);
	    if (tags == null){
	    	return null;
	    }else{
	    	String result = createString(tags, htmlTagRules);
	    	return result;
	    }
	}

    @Override
    public String getFullTitleCache(T nonViralName) {
    	return getFullTitleCache(nonViralName, null);
    }


    /**
     * Generates and returns the "name cache" (only scientific name without author teams and year).
     * @see eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy#getNameCache(eu.etaxonomy.cdm.model.name.TaxonNameBase)
     */
    @Override
    public String getNameCache(T nonViralName) {
        List<TaggedText> tags = getTaggedName(nonViralName);
        if (tags == null){
            return null;
        }else{
            String result = createString(tags);
            return result;
        }
    }







// ******************* Authorship ******************************/


    @Override
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
     * Returns the authorshipcache string for the atomized authorship fields. Does not use the authorship field.
     * @throws NullPointerException if nonViralName is null.
     * @param nonViralName
     * @return
     */
    protected String getNonCacheAuthorshipCache(T nonViralName){
        String result = "";
        INomenclaturalAuthor combinationAuthor = nonViralName.getCombinationAuthorship();
        INomenclaturalAuthor exCombinationAuthor = nonViralName.getExCombinationAuthorship();
        INomenclaturalAuthor basionymAuthor = nonViralName.getBasionymAuthorship();
        INomenclaturalAuthor exBasionymAuthor = nonViralName.getExBasionymAuthorship();
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
//        if ("".equals(result)){
//        	result = null;
//        }
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


    /**
     * Checks if the given name should include the author in it's cached version.<BR>
     * This is usually the case but not for <i>species aggregates</i>.
     * @param nonViralName
     * @return
     */
    protected boolean nameIncludesAuthorship(NonViralName<?> nonViralName){
        Rank rank = nonViralName.getRank();
        if (rank != null && rank.isSpeciesAggregate()){
            return false;
        }else{
            return true;
        }
    }

// ************* TAGGED NAME ***************************************/

    @Override
    public List<TaggedText> getTaggedFullTitle(T nonViralName) {
        List<TaggedText> tags = new ArrayList<TaggedText>();

        //null
        if (nonViralName == null){
            return null;
        }

        //protected full title cache
        if (nonViralName.isProtectedFullTitleCache()){
            tags.add(new TaggedText(TagEnum.fullName, nonViralName.getFullTitleCache()));
            return tags;
        }

        //title cache
//		String titleCache = nonViralName.getTitleCache();
        List<TaggedText> titleTags = getTaggedTitle(nonViralName);
        tags.addAll(titleTags);


        //reference
        String microReference = nonViralName.getNomenclaturalMicroReference();
        INomenclaturalReference ref = nonViralName.getNomenclaturalReference();
        String referenceCache = null;
        if (ref != null){
            Reference reference = HibernateProxyHelper.deproxy(ref, Reference.class);
            referenceCache = reference.getNomenclaturalCitation(microReference);
        }
            //add to tags
        if (StringUtils.isNotBlank(referenceCache)){
            if (! referenceCache.trim().startsWith("in ")){
                String refConcat = ", ";
                tags.add(new TaggedText(TagEnum.separator, refConcat));
            }
            tags.add(new TaggedText(TagEnum.reference, referenceCache));
        }

        //nomenclatural status
        tags.addAll(getNomStatusTags(nonViralName, true, false));
        return tags;

    }


    /**
     * @param nonViralName
     * @param tags
     * @return
     */
    @Override
    public List<TaggedText> getNomStatusTags(T nonViralName, boolean includeSeparatorBefore,
            boolean includeSeparatorAfter) {
        Set<NomenclaturalStatus> ncStati = nonViralName.getStatus();
        Iterator<NomenclaturalStatus> iterator = ncStati.iterator();
        List<TaggedText> nomStatusTags = new ArrayList<TaggedText>();
        while (iterator.hasNext()) {
            NomenclaturalStatus ncStatus = iterator.next();
            // since the NewInstance method of nomencatural status allows null as parameter
            // we have to check for null values here
            String nomStatusStr = "not defined";
            if(ncStatus.getType() != null){
                NomenclaturalStatusType statusType =  ncStatus.getType();
                Language lang = Language.LATIN();
                Representation repr = statusType.getRepresentation(lang);
                if (repr != null){
                    nomStatusStr = repr.getAbbreviatedLabel();
                }else{
                    String message = "No latin representation available for nom. status. " + statusType.getTitleCache();
                    logger.warn(message);
                    throw new IllegalStateException(message);
                }
            }else if(StringUtils.isNotBlank(ncStatus.getRuleConsidered())){
                nomStatusStr = ncStatus.getRuleConsidered();
            }
            String statusSeparator = ", ";
            if (includeSeparatorBefore){
                nomStatusTags.add(new TaggedText(TagEnum.separator, statusSeparator));
            }
            nomStatusTags.add(new TaggedText(TagEnum.nomStatus, nomStatusStr));
            if (includeSeparatorAfter){
                nomStatusTags.add(new TaggedText(TagEnum.postSeparator, ","));
            }
        }
        return nomStatusTags;
    }

    @Override
    public List<TaggedText> getTaggedTitle(T nonViralName) {
        if (nonViralName == null){
            return null;
        }

        List<TaggedText> tags = new ArrayList<TaggedText>();

        //TODO how to handle protected fullTitleCache here?

        if (nonViralName.isProtectedTitleCache()){
            //protected title cache
            tags.add(new TaggedText(TagEnum.name, nonViralName.getTitleCache()));
            return tags;
        }else if (nonViralName.isHybridFormula()){
            //hybrid formula
            String hybridSeparator = NonViralNameParserImplRegExBase.hybridSign;
            boolean isFirst = true;
            List<HybridRelationship> rels = nonViralName.getOrderedChildRelationships();
            for (HybridRelationship rel: rels){
                if (! isFirst){
                    tags.add(new TaggedText(TagEnum.hybridSign, hybridSeparator));
                }
                isFirst = false;
                tags.addAll(getTaggedTitle((T)rel.getParentName()));
            }
            return tags;
        }else if (nonViralName.isAutonym()){
            //Autonym
            tags.addAll(handleTaggedAutonym(nonViralName));
        }else{ //not Autonym
//			String nameCache = nonViralName.getNameCache();  //OLD: CdmUtils.Nz(getNameCache(nonViralName));
            List<TaggedText> nameTags = getTaggedName(nonViralName);
            tags.addAll(nameTags);
            String authorCache = getAuthorshipCache(nonViralName);
            if (StringUtils.isNotBlank(authorCache)){
                tags.add(new TaggedText(TagEnum.authors, authorCache));
            }
        }
        return tags;
    }

    /**
     * Returns the tag list of the name part (without author and reference).
     * @param nonViralName
     * @return
     */
    @Override
    public List<TaggedText> getTaggedName(T nonViralName) {
        if (nonViralName == null){
            return null;
        }
        List<TaggedText> tags = new ArrayList<TaggedText>();
        Rank rank = nonViralName.getRank();

        if (nonViralName.isProtectedNameCache()){
            tags.add(new TaggedText(TagEnum.name, nonViralName.getNameCache()));
        }else if (rank == null){
            tags = getRanklessTaggedNameCache(nonViralName);
//		}else if (nonViralName.isInfragenericUnranked()){
//			result = getUnrankedInfragenericNameCache(nonViralName);
        }else if (rank.isInfraSpecific()){
            tags = getInfraSpeciesTaggedNameCache(nonViralName);
        }else if (rank.isSpecies() || isAggregateWithAuthorship(nonViralName, rank) ){ //exception see #4288
            tags = getSpeciesTaggedNameCache(nonViralName);
        }else if (rank.isInfraGeneric()){
            tags = getInfraGenusTaggedNameCache(nonViralName);
        }else if (rank.isGenus()){
            tags = getGenusOrUninomialTaggedNameCache(nonViralName);
        }else if (rank.isSupraGeneric()){
            tags = getGenusOrUninomialTaggedNameCache(nonViralName);
        }else{
            logger.warn("Name Strategy for Name (UUID: " + nonViralName.getUuid() +  ") not yet implemented");
        }
        //TODO handle appended phrase here instead of different places, check first if this is true for all
        //cases

        return tags;

    }




//***************************** PRIVATES ***************************************/


    private boolean isAggregateWithAuthorship(T nonViralName, Rank rank) {
		if (rank == null){
			return false;
		}else{
			return rank.isSpeciesAggregate() && ( isNotBlank(nonViralName.getAuthorshipCache()) || nonViralName.getNomenclaturalReference() != null );
		}
	}


	/**
     * Returns the tag list for an autonym taxon.
     *
     * @see NonViralName#isAutonym()
     * @see BotanicalName#isAutonym()
     * @param nonViralName
     * @return
     */
    private List<TaggedText> handleTaggedAutonym(T nonViralName) {
    	List<TaggedText> tags = null;
    	if (nonViralName.isInfraSpecific()){
	        //species part
	        tags = getSpeciesTaggedNameCache(nonViralName);

	        //author
	        String authorCache = getAuthorshipCache(nonViralName);
	        if (StringUtils.isNotBlank(authorCache)){
	            tags.add(new TaggedText(TagEnum.authors, authorCache));
	        }


	        //infra species marker
	        if (nonViralName.getRank() == null || !nonViralName.getRank().isInfraSpecific()){
	            //TODO handle exception
	            logger.warn("Rank for autonym does not exist or is not lower than species !!");
	        }else{
	            String infraSpeciesMarker = nonViralName.getRank().getAbbreviation();
	            if (StringUtils.isNotBlank(infraSpeciesMarker)){
	                tags.add(new TaggedText(TagEnum.rank, infraSpeciesMarker));
	            }
	        }

	        //infra species
	        String infraSpeciesPart = CdmUtils.Nz(nonViralName.getInfraSpecificEpithet()).trim();
	        if (StringUtils.isNotBlank(infraSpeciesPart)){
	            tags.add(new TaggedText(TagEnum.name, infraSpeciesPart));
	        }

        } else if (nonViralName.isInfraGeneric()){
        	//genus part
	       tags =getGenusOrUninomialTaggedNameCache(nonViralName);


	        //infra species marker
	        if (nonViralName.getRank() == null || !nonViralName.getRank().isInfraGeneric()){
	            //TODO handle exception
	            logger.warn("Rank for autonym does not exist or is not lower than species !!");
	        }else{
	        	Rank rank = nonViralName.getRank();
	            String infraGenericMarker = rank.getAbbreviation();
                if (rank.equals(Rank.SECTION_BOTANY()) || rank.equals(Rank.SUBSECTION_BOTANY())){
                	infraGenericMarker = infraGenericMarker.replace("(bot.)", "");
                }
	            if (StringUtils.isNotBlank(infraGenericMarker)){
	                tags.add(new TaggedText(TagEnum.rank, infraGenericMarker));
	            }
	        }

	        //infra species
	        String infraGenericPart = CdmUtils.Nz(nonViralName.getInfraGenericEpithet()).trim();
	        if (StringUtils.isNotBlank(infraGenericPart)){
	            tags.add(new TaggedText(TagEnum.name, infraGenericPart));
	        }

        }

        return tags;
    }



    /**
     * Returns the tag list for rankless taxa.
     * @param nonViralName
     * @return
     */
    protected List<TaggedText> getRanklessTaggedNameCache(NonViralName<?> nonViralName){
        List<TaggedText> tags = getUninomialTaggedPart(nonViralName);
        String speciesEpi = CdmUtils.Nz(nonViralName.getSpecificEpithet()).trim();
        if (StringUtils.isNotBlank(speciesEpi)){
            tags.add(new TaggedText(TagEnum.name, speciesEpi));
        }

        String infraSpeciesEpi = CdmUtils.Nz(nonViralName.getInfraSpecificEpithet());
        if (StringUtils.isNotBlank(infraSpeciesEpi)){
            tags.add(new TaggedText(TagEnum.name, infraSpeciesEpi));
        }

        //result += " (rankless)";
        addAppendedTaggedPhrase(tags, nonViralName);
        return tags;
    }

    /**
     * Returns the tag list for the first epithet (including a hybrid sign if required).
     * @param nonViralName
     * @return
     */
    private List<TaggedText> getUninomialTaggedPart(NonViralName<?> nonViralName) {
        List<TaggedText> tags = new ArrayList<TaggedText>();

        if (nonViralName.isMonomHybrid()){
            addHybridPrefix(tags);
        }

        String uninomial = CdmUtils.Nz(nonViralName.getGenusOrUninomial()).trim();
        if (StringUtils.isNotBlank(uninomial)){
            tags.add(new TaggedText(TagEnum.name, uninomial));
        }

        return tags;
    }

    /**
     * Returns the tag list for an genus or higher taxon.
     *
     * @param nonViralName
     * @return
     */
    protected List<TaggedText> getGenusOrUninomialTaggedNameCache(NonViralName<?> nonViralName){
        List<TaggedText> tags = getUninomialTaggedPart(nonViralName);
        addAppendedTaggedPhrase(tags, nonViralName);
        return tags;
    }

    /**
     * Returns the tag list for an infrageneric taxon (including species aggregates).
     *
     * @see #getSpeciesAggregateTaggedCache(NonViralName)
     * @param nonViralName
     * @return
     */
    protected List<TaggedText> getInfraGenusTaggedNameCache(NonViralName<?> nonViralName){
        Rank rank = nonViralName.getRank();
        if (rank != null && rank.isSpeciesAggregate() && isBlank(nonViralName.getAuthorshipCache())){
            return getSpeciesAggregateTaggedCache(nonViralName);
        }

        //genus
        List<TaggedText> tags = getUninomialTaggedPart(nonViralName);

        //marker
        String infraGenericMarker;
        if (rank != null){
            try {
                infraGenericMarker = rank.getInfraGenericMarker();
                if (rank.equals(Rank.SECTION_BOTANY()) || rank.equals(Rank.SUBSECTION_BOTANY())){
                	infraGenericMarker = infraGenericMarker.replace("(bot.)", "");
                }
            } catch (UnknownCdmTypeException e) {
                infraGenericMarker = "'unhandled infrageneric rank'";
            }
        }else{
        	infraGenericMarker = "'undefined infrageneric rank'";
        }
        String infraGenEpi = CdmUtils.Nz(nonViralName.getInfraGenericEpithet()).trim();

        addInfraGenericPart(nonViralName, tags, infraGenericMarker, infraGenEpi);

        addAppendedTaggedPhrase(tags, nonViralName);
        return tags;
    }


	/**
	 * Default implementation for the infrageneric part of a name.
	 * This is usually the infrageneric marker and the infrageneric epitheton. But may be implemented differently e.g. for zoological
	 * names the infrageneric epitheton may be surrounded by brackets and the marker left out.
	 * @param nonViralName
	 * @param tags
	 * @param infraGenericMarker
	 */
	protected void addInfraGenericPart(NonViralName<?> name, List<TaggedText> tags, String infraGenericMarker, String infraGenEpi) {
		//add marker
		tags.add(new TaggedText(TagEnum.rank, infraGenericMarker));

		//add epitheton
		if (StringUtils.isNotBlank(infraGenEpi)){
            tags.add(new TaggedText(TagEnum.name, infraGenEpi));
        }
	}

    /**
     * Returns the tag list for a species aggregate (or similar) taxon.<BR>
     * Possible ranks for a <i>species aggregate</i> are "aggr.", "species group", ...
     * @param nonViralName
     * @return
     */
    protected List<TaggedText> getSpeciesAggregateTaggedCache(NonViralName<?> nonViralName){
        if (! isBlank(nonViralName.getAuthorshipCache())){
        	List<TaggedText> result = getSpeciesTaggedNameCache(nonViralName);
        	return result;
        }


    	List<TaggedText> tags = getGenusAndSpeciesTaggedPart(nonViralName);

        addSpeciesAggregateTaggedEpithet(tags, nonViralName);
        addAppendedTaggedPhrase(tags, nonViralName);
        return tags;
    }

    /**
     * Adds the aggregate tag to the tag list.
     * @param tags
     * @param nonViralName
     */
    private void addSpeciesAggregateTaggedEpithet(List<TaggedText> tags, NonViralName<?> nonViralName) {
        String marker;
        try {
            marker = nonViralName.getRank().getInfraGenericMarker();
        } catch (UnknownCdmTypeException e) {
            marker = "'unknown aggregat type'";
        }
        if (StringUtils.isNotBlank(marker)){
            tags.add(new TaggedText(TagEnum.rank, marker));
        }
    }


    /**
     * Returns the tag list for a species taxon.
     * @param nonViralName
     * @return
     */
    protected List<TaggedText> getSpeciesTaggedNameCache(NonViralName<?> nonViralName){
        List<TaggedText> tags = getGenusAndSpeciesTaggedPart(nonViralName);
        addAppendedTaggedPhrase(tags, nonViralName);
        return tags;
    }

    /**
     * Creates the tag list for an infraspecific taxon. In include is true the result will contain
     * @param nonViralName
     * @return
     */
    protected List<TaggedText> getInfraSpeciesTaggedNameCache(T nonViralName){
        return getInfraSpeciesTaggedNameCache(nonViralName, true);
    }

    /**
     * Creates the tag list for an infraspecific taxon. If include is true the result will contain
     * the infraspecific marker (e.g. "var.")
     * @param nonViralName
     * @param includeMarker
     * @return
     */
    protected List<TaggedText> getInfraSpeciesTaggedNameCache(NonViralName<?> nonViralName, boolean includeMarker){
        List<TaggedText> tags = getGenusAndSpeciesTaggedPart(nonViralName);
        if (includeMarker){
            String marker = (nonViralName.getRank().getAbbreviation()).trim().replace("null", "");
            if (StringUtils.isNotBlank(marker)){
                tags.add(new TaggedText(TagEnum.rank, marker));
            }
        }
        String infrSpecEpi = CdmUtils.Nz(nonViralName.getInfraSpecificEpithet());
        if (nonViralName.isTrinomHybrid()){
            addHybridPrefix(tags);
        }

        infrSpecEpi = infrSpecEpi.trim().replace("null", "");
        if (StringUtils.isNotBlank(infrSpecEpi)){
            tags.add(new TaggedText(TagEnum.name, infrSpecEpi));
        }

        addAppendedTaggedPhrase(tags, nonViralName);
        return tags;
    }


    /**
     * Adds a tag for the hybrid sign and an empty separator to avoid trailing whitespaces.
     * @param tags
     */
    private void addHybridPrefix(List<TaggedText> tags) {
        tags.add(new TaggedText(TagEnum.hybridSign, NonViralNameParserImplRegExBase.hybridSign));
        tags.add(new TaggedText(TagEnum.separator, "")); //no whitespace separator
    }

    /**
     * Creates the tag list for the genus and species part.
     * @param nonViralName
     * @return
     */
    private List<TaggedText> getGenusAndSpeciesTaggedPart(NonViralName<?> nonViralName) {
        //Uninomial
        List<TaggedText> tags = getUninomialTaggedPart(nonViralName);

        //InfraGenericEpi
        boolean hasInfraGenericEpi = StringUtils.isNotBlank(nonViralName.getInfraGenericEpithet());
        if (hasInfraGenericEpi){
            String infrGenEpi = nonViralName.getInfraGenericEpithet().trim();
            if (nonViralName.isBinomHybrid()){
//					addHybridPrefix(tags);  FIXME hybridSign should be tag, but then we need to handle "(" ")" differently.
                infrGenEpi = NonViralNameParserImplRegExBase.hybridSign + infrGenEpi;
            }
            infrGenEpi = "(" + infrGenEpi + ")";
            tags.add(new TaggedText(TagEnum.name, infrGenEpi));
        }

        //Species Epi
        String specEpi = CdmUtils.Nz(nonViralName.getSpecificEpithet()).trim();
        if (! hasInfraGenericEpi && nonViralName.isBinomHybrid() ||
                hasInfraGenericEpi && nonViralName.isTrinomHybrid()){
            addHybridPrefix(tags);
        }
        if (StringUtils.isNotBlank(specEpi)){
            tags.add(new TaggedText(TagEnum.name, specEpi));
        }
        return tags;
    }

    /**
     * Adds the tag for the appended phrase if an appended phrase exists
     * @param tags
     * @param nonViralName
     */
    protected void addAppendedTaggedPhrase(List<TaggedText> tags, NonViralName<?> nonViralName){
        String appendedPhrase = nonViralName ==null ? null : nonViralName.getAppendedPhrase();
        String originalName = getOriginalNameString(nonViralName, tags);
        if (StringUtils.isNotBlank(originalName)){
            tags.add(new TaggedText(TagEnum.name, originalName));
        }
        if (StringUtils.isNotEmpty(appendedPhrase)){
            tags.add(new TaggedText(TagEnum.name, appendedPhrase));
        }
    }

    private String getOriginalNameString(NonViralName<?> currentName, List<TaggedText> originalNameTaggs) {
		List<String> originalNameStrings = new ArrayList<String>(1);
    	for (NameRelationship nameRel : currentName.getRelationsToThisName()){  //handle list, just in case we have strange data; this may result in strange looking results
			NameRelationshipType type = nameRel.getType();
    		if(type != null && type.equals(NameRelationshipType.ORIGINAL_SPELLING())){
    			String originalNameString;
    			TaxonNameBase<?,?> originalName = nameRel.getFromName();
    			if (!originalName.isInstanceOf(NonViralName.class)){
    				originalNameString = originalName.getTitleCache();
    			}else{
    				NonViralName<?> originalNvName = CdmBase.deproxy(originalName, NonViralName.class);
    				originalNameString = makeOriginalNameString(currentName, originalNvName, originalNameTaggs);
    			}
    			originalNameStrings.add("'" + originalNameString +"'");
    		}
		}
    	if (originalNameStrings.size() > 0){
    		String result = CdmUtils.concat("", originalNameStrings.toArray(new String[originalNameStrings.size()])) ;
	    	return result;
    	}else{
    		return null;
    	}
	}


	private String makeOriginalNameString(NonViralName<?> currentName, NonViralName<?> originalName, List<TaggedText> currentNameTags) {
		//use cache if necessary
		String cacheToUse = null;
		if (originalName.isProtectedNameCache() && StringUtils.isNotBlank(originalName.getNameCache())){
			cacheToUse = originalName.getNameCache();
		}else if (originalName.isProtectedTitleCache() && StringUtils.isNotBlank(originalName.getTitleCache())){
			cacheToUse = originalName.getTitleCache();
		}else if (originalName.isProtectedFullTitleCache() && StringUtils.isNotBlank(originalName.getFullTitleCache())){
			cacheToUse = originalName.getFullTitleCache();
		}
		if (cacheToUse != null){
			return cacheToUse;
		}
		//use atomized data
		//get originalNameParts array
		String originalNameString = originalName.getNameCache();
		if (originalNameString == null){
			originalNameString = originalName.getTitleCache();
		}
		if (originalNameString == null){  //should not happen
			originalNameString = originalName.getFullTitleCache();
		}
		String[] originalNameSplit = originalNameString.split("\\s+");

		//get current name parts
		String currentNameString = createString(currentNameTags);
		String[] currentNameSplit = currentNameString.split("\\s+");

		//compute string
		String result = originalNameString;
		for (int i = 0; i < Math.min(originalNameSplit.length, currentNameSplit.length); i++){
			if (originalNameSplit[i].equals(currentNameSplit[i])){
				result = result.replaceFirst(originalNameSplit[i], "").trim();
			}
		}
		//old
//		if (originalName.getGenusOrUninomial() != null && originalName.getGenusOrUninomial().equals(currentName.getGenusOrUninomial())){
//
//		}
		return result;
	}


	@Override
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


    /**
     * @param tags
     * @return
     */
    private String createString(List<TaggedText> tags) {
        return TaggedCacheHelper.createString(tags);
    }

    /**
     * @param tags
     * @param htmlTagRules
     * @return
     */
    private String createString(List<TaggedText> tags, HTMLTagRules htmlTagRules) {
        return TaggedCacheHelper.createString(tags, htmlTagRules);
    }



}
