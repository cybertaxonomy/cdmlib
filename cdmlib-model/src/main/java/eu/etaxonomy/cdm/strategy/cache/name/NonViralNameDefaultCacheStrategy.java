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
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
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
public class NonViralNameDefaultCacheStrategy
        extends NameCacheStrategyBase
        implements INonViralNameCacheStrategy {

    private static final Logger logger = Logger.getLogger(NonViralNameDefaultCacheStrategy.class);
	private static final long serialVersionUID = -6577757501563212669L;

    final static UUID uuid = UUID.fromString("1cdda0d1-d5bc-480f-bf08-40a510a2f223");

    protected String nameAuthorSeperator = " ";
    protected String basionymStart = "(";
    protected String basionymEnd = ")";
    protected String exAuthorSeperator = " ex ";
    private static String NOTHO = "notho";
    protected CharSequence basionymAuthorCombinationAuthorSeperator = " ";

    protected String zooAuthorYearSeperator = ", ";



    @Override
    public  UUID getUuid(){
        return uuid;
    }

// ************************** FACTORY  ******************************/

    public static NonViralNameDefaultCacheStrategy NewInstance(){
        return new NonViralNameDefaultCacheStrategy();
    }


// ************ CONSTRUCTOR *******************/

    protected NonViralNameDefaultCacheStrategy(){
        super();
    }

/* **************** GETTER / SETTER **************************************/

    /**
     * String that separates the NameCache part from the AuthorCache part
     * @return
     */
    public String getNameAuthorSeperator() {
        return nameAuthorSeperator;
    }
    public void setNameAuthorSeperator(String nameAuthorSeperator) {
        this.nameAuthorSeperator = nameAuthorSeperator;
    }


    /**
     * String the basionym author part starts with e.g. '('.
     * This should correspond with the {@link NonViralNameDefaultCacheStrategy#getBasionymEnd() basionymEnd} attribute
     * @return
     */
    public String getBasionymStart() {
        return basionymStart;
    }
    public void setBasionymStart(String basionymStart) {
        this.basionymStart = basionymStart;
    }

    /**
     * String the basionym author part ends with e.g. ')'.
     * This should correspond with the {@link NonViralNameDefaultCacheStrategy#getBasionymStart() basionymStart} attribute
     * @return
     */
    public String getBasionymEnd() {
        return basionymEnd;
    }
    public void setBasionymEnd(String basionymEnd) {
        this.basionymEnd = basionymEnd;
    }


    /**
     * String to separate ex author from author.
     * @return
     */
    public String getExAuthorSeperator() {
        return exAuthorSeperator;
    }
    public void setExAuthorSeperator(String exAuthorSeperator) {
        this.exAuthorSeperator = exAuthorSeperator;
    }


    /**
     * String that separates the basionym/original_combination author part from the combination author part
     * @return
     */
    public CharSequence getBasionymAuthorCombinationAuthorSeperator() {
        return basionymAuthorCombinationAuthorSeperator;
    }


    public void setBasionymAuthorCombinationAuthorSeperator( CharSequence basionymAuthorCombinationAuthorSeperator) {
        this.basionymAuthorCombinationAuthorSeperator = basionymAuthorCombinationAuthorSeperator;
    }

    public String getZooAuthorYearSeperator() {
        return zooAuthorYearSeperator;
    }
    public void setZooAuthorYearSeperator(String authorYearSeperator) {
        this.zooAuthorYearSeperator = authorYearSeperator;
    }


//** *****************************************************************************************/




// ******************* Authorship ******************************/

    @Override
    public String getAuthorshipCache(TaxonName taxonName) {
        if (taxonName == null){
            return null;
        }
        //cache protected
        if (taxonName.isProtectedAuthorshipCache() == true) {
            return taxonName.getAuthorshipCache();
        }
        return getNonCacheAuthorshipCache(taxonName);
    }

    /**
     * Returns the authorshipcache string for the atomized authorship fields. Does not use the authorship field.
     * @throws NullPointerException if nonViralName is null.
     * @param nonViralName
     * @return
     */
    protected String getNonCacheAuthorshipCache(TaxonName nonViralName){
        if (nonViralName.getNameType().isZoological()){
            return this.getZoologicalNonCacheAuthorshipCache(nonViralName);
        }else{
            String result = "";
            INomenclaturalAuthor combinationAuthor = nonViralName.getCombinationAuthorship();
            INomenclaturalAuthor exCombinationAuthor = nonViralName.getExCombinationAuthorship();
            INomenclaturalAuthor basionymAuthor = nonViralName.getBasionymAuthorship();
            INomenclaturalAuthor exBasionymAuthor = nonViralName.getExBasionymAuthorship();
            String basionymPart = "";
            String authorPart = "";
            //basionym
            if (basionymAuthor != null || exBasionymAuthor != null){
                basionymPart = basionymStart + getAuthorAndExAuthor(basionymAuthor, exBasionymAuthor) + basionymEnd;
            }
            if (combinationAuthor != null || exCombinationAuthor != null){
                authorPart = getAuthorAndExAuthor(combinationAuthor, exCombinationAuthor);
            }
            result = CdmUtils.concat(basionymAuthorCombinationAuthorSeperator, basionymPart, authorPart);
//        if ("".equals(result)){
//        	result = null;
//        }
            return result;
        }
    }

    protected String getZoologicalNonCacheAuthorshipCache(TaxonName nonViralName) {
        if (nonViralName == null){
            return null;
        }
        String result = "";
        INomenclaturalAuthor combinationAuthor = nonViralName.getCombinationAuthorship();
        INomenclaturalAuthor exCombinationAuthor = nonViralName.getExCombinationAuthorship();
        INomenclaturalAuthor basionymAuthor = nonViralName.getBasionymAuthorship();
        INomenclaturalAuthor exBasionymAuthor = nonViralName.getExBasionymAuthorship();
        Integer publicationYear = nonViralName.getPublicationYear();
        Integer originalPublicationYear = nonViralName.getOriginalPublicationYear();

        String basionymPart = "";
        String authorPart = "";
        //basionym
        if (basionymAuthor != null || exBasionymAuthor != null || originalPublicationYear != null ){
            String authorAndEx = getAuthorAndExAuthor(basionymAuthor, exBasionymAuthor);
            String originalPublicationYearString = originalPublicationYear == null ? null : String.valueOf(originalPublicationYear);
            String authorAndExAndYear = CdmUtils.concat(zooAuthorYearSeperator, authorAndEx, originalPublicationYearString );
            basionymPart = basionymStart + authorAndExAndYear + basionymEnd;
        }
        if (combinationAuthor != null || exCombinationAuthor != null){
            String authorAndEx = getAuthorAndExAuthor(combinationAuthor, exCombinationAuthor);
            String publicationYearString = publicationYear == null ? null : String.valueOf(publicationYear);
            authorPart = CdmUtils.concat(zooAuthorYearSeperator, authorAndEx, publicationYearString);
        }
        result = CdmUtils.concat(basionymAuthorCombinationAuthorSeperator, basionymPart, authorPart);
        if (result == null){
            result = "";
        }
        return result;
    }


    /**
     * Returns the AuthorCache part for a combination of an author and an ex author. This applies on
     * combination authors as well as on basionym/orginal combination authors.
     * The correct order is exAuthor ex author though some botanist do not know about and do it the
     * other way round. (see 46.4-46.6 ICBN (Vienna Code, 2006))
     *
     * @param author the author
     * @param exAuthor the ex-author
     * @return
     */
    protected String getAuthorAndExAuthor(INomenclaturalAuthor author, INomenclaturalAuthor exAuthor){
        String authorString = "";
        String exAuthorString = "";
        if (author != null){
            authorString = CdmUtils.Nz(author.getNomenclaturalTitle());
        }
        if (exAuthor != null){
            exAuthorString = CdmUtils.Nz(exAuthor.getNomenclaturalTitle());
            exAuthorString += exAuthorSeperator;
        }
        String result = exAuthorString + authorString;
        return result;
    }



    /**
     * Checks if the given name should include the author in it's cached version.<BR>
     * This is usually the case but not for <i>species aggregates</i>.
     * @param nonViralName
     * @return
     */
    protected boolean nameIncludesAuthorship(INonViralName nonViralName){
        Rank rank = nonViralName.getRank();
        if (rank != null && rank.isSpeciesAggregate()){
            return false;
        }else{
            return true;
        }
    }

// ************* TAGGED NAME ***************************************/

    @Override
    public List<TaggedText> getTaggedFullTitle(TaxonName nonViralName) {
        List<TaggedText> tags = new ArrayList<>();

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

        addOriginalSpelling(tags, nonViralName);

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
    public List<TaggedText> getNomStatusTags(TaxonName nonViralName, boolean includeSeparatorBefore,
            boolean includeSeparatorAfter) {

        Set<NomenclaturalStatus> ncStati = nonViralName.getStatus();
        Iterator<NomenclaturalStatus> iterator = ncStati.iterator();
        List<TaggedText> nomStatusTags = new ArrayList<>();
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
    public List<TaggedText> getTaggedTitle(TaxonName nonViralName) {
        if (nonViralName == null){
            return null;
        }

        List<TaggedText> tags = new ArrayList<>();

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
                tags.addAll(getTaggedTitle(rel.getParentName()));
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
    public List<TaggedText> getTaggedName(TaxonName nonViralName) {
        if (nonViralName == null){
            return null;
        }
        List<TaggedText> tags = new ArrayList<>();
        Rank rank = nonViralName.getRank();

        if (nonViralName.isProtectedNameCache()){
            tags.add(new TaggedText(TagEnum.name, nonViralName.getNameCache()));
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
                tags.addAll(getTaggedName(rel.getParentName()));
            }
            return tags;

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


    private boolean isAggregateWithAuthorship(TaxonName nonViralName, Rank rank) {
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
    private List<TaggedText> handleTaggedAutonym(TaxonName nonViralName) {
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
	            if (nonViralName.isTrinomHybrid()){
	                infraSpeciesMarker = CdmUtils.concat("", NOTHO, infraSpeciesMarker);
	            }
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
    protected List<TaggedText> getRanklessTaggedNameCache(INonViralName nonViralName){
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
    private List<TaggedText> getUninomialTaggedPart(INonViralName nonViralName) {
        List<TaggedText> tags = new ArrayList<>();

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
    protected List<TaggedText> getGenusOrUninomialTaggedNameCache(INonViralName nonViralName){
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
    protected List<TaggedText> getInfraGenusTaggedNameCache(INonViralName nonViralName){
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
        if (nonViralName.isBinomHybrid()){
            infraGenericMarker = CdmUtils.concat("", NOTHO, infraGenericMarker);
        }

        addInfraGenericPart(nonViralName, tags, infraGenericMarker, infraGenEpi);

        addAppendedTaggedPhrase(tags, nonViralName);
        return tags;
    }


	/**
	 * Default implementation for the infrageneric part of a name.
	 * This is usually the infrageneric marker and the infrageneric epitheton. But may be
	 * implemented differently e.g. for zoological names the infrageneric epitheton
	 * may be surrounded by brackets and the marker left out.
	 * @param nonViralName
	 * @param tags
	 * @param infraGenericMarker
	 */
	protected void addInfraGenericPart(
	        @SuppressWarnings("unused") INonViralName name,
	        List<TaggedText> tags,
	        String infraGenericMarker,
	        String infraGenEpi) {
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
    protected List<TaggedText> getSpeciesAggregateTaggedCache(INonViralName nonViralName){
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
    private void addSpeciesAggregateTaggedEpithet(List<TaggedText> tags, INonViralName nonViralName) {
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
    protected List<TaggedText> getSpeciesTaggedNameCache(INonViralName nonViralName){
        List<TaggedText> tags = getGenusAndSpeciesTaggedPart(nonViralName);
        addAppendedTaggedPhrase(tags, nonViralName);
        return tags;
    }

    /**
     * Creates the tag list for an infraspecific taxon. In include is true the result will contain
     * @param nonViralName
     * @return
     */
    protected List<TaggedText> getInfraSpeciesTaggedNameCache(TaxonName nonViralName){
        if (nonViralName.getNameType().isZoological()){
            boolean includeMarker = ! (nonViralName.isAutonym());
            return getInfraSpeciesTaggedNameCache(nonViralName, includeMarker);
        }else{
            return getInfraSpeciesTaggedNameCache(nonViralName, true);
        }
    }

    /**
     * Creates the tag list for an infraspecific taxon. If include is true the result will contain
     * the infraspecific marker (e.g. "var.")
     * @param nonViralName
     * @param includeMarker
     * @return
     */
    protected List<TaggedText> getInfraSpeciesTaggedNameCache(INonViralName nonViralName, boolean includeMarker){
        List<TaggedText> tags = getGenusAndSpeciesTaggedPart(nonViralName);
        if (includeMarker || nonViralName.isTrinomHybrid()){
            String marker = (nonViralName.getRank().getAbbreviation()).trim().replace("null", "");
            if (nonViralName.isTrinomHybrid()){
                marker = CdmUtils.concat("", NOTHO, marker);
            }
            if (StringUtils.isNotBlank(marker)){
                tags.add(new TaggedText(TagEnum.rank, marker));
            }

        }
        String infrSpecEpi = CdmUtils.Nz(nonViralName.getInfraSpecificEpithet());

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
    private List<TaggedText> getGenusAndSpeciesTaggedPart(INonViralName nonViralName) {
        //Uninomial
        List<TaggedText> tags = getUninomialTaggedPart(nonViralName);

        //InfraGenericEpi
        boolean hasInfraGenericEpi = StringUtils.isNotBlank(nonViralName.getInfraGenericEpithet());
        if (hasInfraGenericEpi){
            String infrGenEpi = nonViralName.getInfraGenericEpithet().trim();
            if (nonViralName.isBinomHybrid()){
                //maybe not correct but not really expected to happen
                infrGenEpi = UTF8.HYBRID + infrGenEpi;
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
    protected void addAppendedTaggedPhrase(List<TaggedText> tags, INonViralName nonViralName){
        String appendedPhrase = nonViralName ==null ? null : nonViralName.getAppendedPhrase();
        if (StringUtils.isNotEmpty(appendedPhrase)){
            tags.add(new TaggedText(TagEnum.name, appendedPhrase));
        }
    }


	@Override
    public String getLastEpithet(TaxonName taxonName) {
        Rank rank = taxonName.getRank();
        if(rank.isGenus() || rank.isSupraGeneric()) {
            return taxonName.getGenusOrUninomial();
        } else if(rank.isInfraGeneric()) {
            return taxonName.getInfraGenericEpithet();
        } else if(rank.isSpecies()) {
            return taxonName.getSpecificEpithet();
        } else {
            return taxonName.getInfraSpecificEpithet();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TaggedText> doGetTaggedTitle(TaxonName nonViralName) {
        List<TaggedText> tags = new ArrayList<>();
        if (nonViralName.isHybridFormula()){
            //hybrid formula
            String hybridSeparator = NonViralNameParserImplRegExBase.hybridSign;
            boolean isFirst = true;
            List<HybridRelationship> rels = nonViralName.getOrderedChildRelationships();
            for (HybridRelationship rel: rels){
                if (! isFirst){
                    tags.add(new TaggedText(TagEnum.hybridSign, hybridSeparator));
                }
                isFirst = false;
                tags.addAll(getTaggedTitle(rel.getParentName()));
            }
            return tags;
        }else if (nonViralName.isAutonym()){
            //Autonym
            tags.addAll(handleTaggedAutonym(nonViralName));
        }else{ //not Autonym
    //      String nameCache = nonViralName.getNameCache();  //OLD: CdmUtils.Nz(getNameCache(nonViralName));

            List<TaggedText> nameTags = getTaggedName(nonViralName);
            tags.addAll(nameTags);
            String authorCache = getAuthorshipCache(nonViralName);
            if (StringUtils.isNotBlank(authorCache)){
                tags.add(new TaggedText(TagEnum.authors, authorCache));
            }
        }
        return tags;
    }

}
