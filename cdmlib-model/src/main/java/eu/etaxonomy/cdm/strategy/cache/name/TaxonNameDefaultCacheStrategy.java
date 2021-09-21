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
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;
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
public class TaxonNameDefaultCacheStrategy
        extends NameCacheStrategyBase
        implements INonViralNameCacheStrategy {

    private static final Logger logger = Logger.getLogger(TaxonNameDefaultCacheStrategy.class);
	private static final long serialVersionUID = -6577757501563212669L;

    final static UUID uuid = UUID.fromString("1cdda0d1-d5bc-480f-bf08-40a510a2f223");

    protected String nameAuthorSeperator = " ";
    protected String basionymStart = "(";
    protected String basionymEnd = ")";
    protected String exAuthorSeperator = " ex ";
    private static String NOTHO = "notho";
    protected CharSequence basionymAuthorCombinationAuthorSeperator = " ";

    protected String zooAuthorYearSeperator = ", ";

    private String cultivarStart = "'";
    private String cultivarEnd = "'";

    @Override
    public UUID getUuid(){
        return uuid;
    }

// ************************** FACTORY  ******************************/

    public static TaxonNameDefaultCacheStrategy NewInstance(){
        return new TaxonNameDefaultCacheStrategy();
    }


// ************ CONSTRUCTOR *******************/

    protected TaxonNameDefaultCacheStrategy(){
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
     * This should correspond with the {@link TaxonNameDefaultCacheStrategy#getBasionymEnd() basionymEnd} attribute
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
     * This should correspond with the {@link TaxonNameDefaultCacheStrategy#getBasionymStart() basionymStart} attribute
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

// ******************* Authorship ******************************/

    @Override
    public String getAuthorshipCache(TaxonName taxonName) {
        if (taxonName == null){
            return null;
        }else if (taxonName.isViral()){
            return null;
        }else if(taxonName.isProtectedAuthorshipCache() == true) {
            //cache protected
            return taxonName.getAuthorshipCache();
        }else{
            return getNonCacheAuthorshipCache(taxonName);
        }
    }

    /**
     * Returns the authorshipcache string for the atomized authorship fields. Does not use the authorship field.
     * @throws NullPointerException if nonViralName is null.
     * @param taxonName
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
            if (isCultivar(nonViralName) ){
                exCombinationAuthor = null;
                basionymAuthor = null;
                exBasionymAuthor = null;
            }

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

    private boolean isCultivar(TaxonName name) {
        return name.isCultivar() || isNotBlank(name.getCultivarEpithet()) || isNotBlank(name.getCultivarGroupEpithet());
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
     */
    protected String getAuthorAndExAuthor(INomenclaturalAuthor author, INomenclaturalAuthor exAuthor){
        String authorString = "";
        String exAuthorString = "";
        if (author != null){
            authorString = getNomAuthorTitle(author);
        }
        if (exAuthor != null){
            exAuthorString = getNomAuthorTitle(exAuthor);
            exAuthorString += exAuthorSeperator;
        }
        String result = exAuthorString + authorString;
        return result;
    }

    private String getNomAuthorTitle(INomenclaturalAuthor author) {
        return CdmUtils.Nz(author.getNomenclaturalTitleCache());
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
    protected List<TaggedText> doGetTaggedTitle(TaxonName taxonName) {
        List<TaggedText> tags = new ArrayList<>();
        if (taxonName.getNameType().isViral()){
            String acronym = taxonName.getAcronym();
            if (isNotBlank(taxonName.getAcronym())){
                //this is not according to the code
                tags.add(new TaggedText(TagEnum.name, acronym));
            }
            return tags;
        }else if (taxonName.isHybridFormula()){
            //hybrid formula
            String hybridSeparator = NonViralNameParserImplRegExBase.hybridSign;
            boolean isFirst = true;
            List<HybridRelationship> rels = taxonName.getOrderedChildRelationships();
            for (HybridRelationship rel: rels){
                if (! isFirst){
                    tags.add(new TaggedText(TagEnum.hybridSign, hybridSeparator));
                }
                isFirst = false;
                tags.addAll(getTaggedTitle(rel.getParentName()));
            }
            return tags;
        }else if (taxonName.isAutonym()){
            //Autonym
            tags.addAll(handleTaggedAutonym(taxonName));
        }else{ //not Autonym
            List<TaggedText> nameTags = getTaggedName(taxonName);
            tags.addAll(nameTags);
            String authorCache = getAuthorshipCache(taxonName);
            if (isNotBlank(authorCache)){
                tags.add(new TaggedText(TagEnum.authors, authorCache));
            }
        }
        return tags;
    }

    /**
     * Returns the tag list of the name part (without author and reference).
     * @param taxonName
     * @return
     */
    @Override
    public List<TaggedText> getTaggedName(TaxonName taxonName) {
        if (taxonName == null){
            return null;
        }else if (taxonName.isViral()){
            return null;
        }
        List<TaggedText> tags = new ArrayList<>();
        Rank rank = taxonName.getRank();

        if (taxonName.isProtectedNameCache()){
            tags.add(new TaggedText(TagEnum.name, taxonName.getNameCache()));
        }else if (taxonName.isHybridFormula()){
            //hybrid formula
            //TODO graft-chimera (see also cultivars)
            String hybridSeparator = NonViralNameParserImplRegExBase.hybridSign;
            boolean isFirst = true;
            List<HybridRelationship> rels = taxonName.getOrderedChildRelationships();
            for (HybridRelationship rel: rels){
                if (! isFirst){
                    tags.add(new TaggedText(TagEnum.hybridSign, hybridSeparator));
                }
                isFirst = false;
                tags.addAll(getTaggedName(rel.getParentName()));
            }
            return tags;

        }else if (rank == null){
            tags = getRanklessTaggedNameCache(taxonName);
		}else if (rank.isCultivar()){
			tags = getCultivarTaggedNameCache(taxonName);
        }else if (rank.isInfraSpecific()){
            tags = getInfraSpeciesTaggedNameCache(taxonName);
        }else if (rank.isSpecies() || isAggregateWithAuthorship(taxonName, rank) ){ //exception see #4288
            tags = getSpeciesTaggedNameCache(taxonName);
        }else if (rank.isInfraGeneric()){
            tags = getInfraGenusTaggedNameCache(taxonName);
        }else if (rank.isGenus()){
            tags = getGenusOrUninomialTaggedNameCache(taxonName);
        }else if (rank.isSupraGeneric()){
            tags = getGenusOrUninomialTaggedNameCache(taxonName);
        }else{
            tags = getRanklessTaggedNameCache(taxonName);
            logger.warn("Rank does not belong to a rank class: " + rank.getTitleCache() + ". Used rankless nameCache for name " + taxonName.getUuid());
        }
        //TODO handle appended phrase here instead of different places, check first if this is true for all
        //cases

        return tags;
    }

//***************************** PRIVATES ***************************************/

    private List<TaggedText> getCultivarTaggedNameCache(TaxonName taxonName) {
        List<TaggedText> scientificNameTags;
        TaggedTextBuilder builder = TaggedTextBuilder.NewInstance();
        if (isNotBlank(taxonName.getInfraSpecificEpithet())){
            scientificNameTags = getInfraSpeciesTaggedNameCache(taxonName);
        } else if (isNotBlank(taxonName.getSpecificEpithet())){
            scientificNameTags = getSpeciesTaggedNameCache(taxonName);
        } else if (isNotBlank(taxonName.getInfraGenericEpithet())){
            scientificNameTags = getInfraGenusTaggedNameCache(taxonName);
        } else /*if (isNotBlank(taxonName.getGenusOrUninomial())) */{
            scientificNameTags = getGenusOrUninomialTaggedNameCache(taxonName);
        }

        UUID rankUuid = taxonName.getRank().getUuid();
        boolean rankIsHandled = true;
        String cultivarStr = null;
        String groupStr = taxonName.getCultivarGroupEpithet();
        if (rankUuid.equals(Rank.uuidCultivar)){
            cultivarStr = surroundedCultivarEpithet(taxonName.getCultivarEpithet());
            if (isNotBlank(cultivarStr) && isNotBlank(groupStr)){
                groupStr = surroundGroupWithBracket(groupStr);
            }
            cultivarStr = CdmUtils.concat(" ", groupStr, cultivarStr);
        }else if (rankUuid.equals(Rank.uuidCultivarGroup)){
            cultivarStr = CdmUtils.concat(" ", groupStr, checkHasGroupEpithet(groupStr)? null: "Group");
        }else if (rankUuid.equals(Rank.uuidGrex)){
            cultivarStr = CdmUtils.concat(" ", groupStr, checkHasGrexEpithet(groupStr)? null: "grex");
        }else{
            rankIsHandled = false;
        }
        if (rankIsHandled){
            builder.addAll(scientificNameTags);
            if (isNotBlank(cultivarStr)){
                builder.add(TagEnum.cultivar, cultivarStr);
            }
        }else if (rankUuid.equals(Rank.uuidGraftChimaera)){
            //TODO not yet fully implemented
            cultivarStr = "+ " + CdmUtils.concat(" ", taxonName.getGenusOrUninomial(), surroundedCultivarEpithet(taxonName.getCultivarEpithet()));
            builder.add(TagEnum.cultivar, cultivarStr);
        } else { //(!rankIsHandled)
            throw new IllegalStateException("Unsupported rank " + taxonName.getRank().getTitleCache() + " for cultivar.");
        }

        return builder.getTaggedText();
    }

    private String surroundGroupWithBracket(String groupStr) {
        if (groupStr.matches(NonViralNameParserImplRegExBase.grex + "$")){
            return groupStr;
        }else if (groupStr.matches(".*" + NonViralNameParserImplRegExBase.grex + ".+")){
            Matcher matcher = Pattern.compile(NonViralNameParserImplRegExBase.grex + "\\s*" ).matcher(groupStr);
            matcher.find();
            return groupStr.substring(0, matcher.end()) + "("+ groupStr.substring(matcher.end())+ ")";
        }else{
            return "("+ groupStr + ")";
        }
    }

    private boolean checkHasGroupEpithet(String group) {

        String[] splits = group == null? new String[0]: group.split("\\s+");
        if (splits.length <= 1){
            return false;
        }else if (splits[0].matches(NonViralNameParserImplRegExBase.group)
                || splits[splits.length-1].matches(NonViralNameParserImplRegExBase.group)){
            return true;
        }else{
            return false;
        }
    }

    private boolean checkHasGrexEpithet(String grex) {
        String[] splits = grex == null? new String[0]: grex.split("\\s+");
        if (splits.length <= 1){
            return false;
        }else if (splits[splits.length-1].matches(NonViralNameParserImplRegExBase.grex)){
            return true;
        }else{
            return false;
        }
    }

    private String surroundedCultivarEpithet(String cultivarEpi) {
        return cultivarStart + CdmUtils.Nz(cultivarEpi) + cultivarEnd;
    }

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
	        if (isNotBlank(authorCache)){
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
	            if (isNotBlank(infraSpeciesMarker)){
	                tags.add(new TaggedText(TagEnum.rank, infraSpeciesMarker));
	            }
	        }

	        //infra species
	        String infraSpeciesPart = CdmUtils.Nz(nonViralName.getInfraSpecificEpithet()).trim();
	        if (isNotBlank(infraSpeciesPart)){
	            tags.add(new TaggedText(TagEnum.name, infraSpeciesPart));
	        }

        } else if (nonViralName.isInfraGeneric()){
        	//genus part
	       tags =getGenusOrUninomialTaggedNameCache(nonViralName);

	       //author
           String authorCache = getAuthorshipCache(nonViralName);
           if (isNotBlank(authorCache)){
               tags.add(new TaggedText(TagEnum.authors, authorCache));
           }

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
	            if (isNotBlank(infraGenericMarker)){
	                tags.add(new TaggedText(TagEnum.rank, infraGenericMarker));
	            }
	        }

	        //infra genus
	        String infraGenericPart = CdmUtils.Nz(nonViralName.getInfraGenericEpithet()).trim();
	        if (isNotBlank(infraGenericPart)){
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
        if (isNotBlank(speciesEpi)){
            tags.add(new TaggedText(TagEnum.name, speciesEpi));
        }

        String infraSpeciesEpi = CdmUtils.Nz(nonViralName.getInfraSpecificEpithet());
        if (isNotBlank(infraSpeciesEpi)){
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
        if (isNotBlank(uninomial)){
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
		if (isNotBlank(infraGenEpi)){
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
        if (isNotBlank(marker)){
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

    protected List<TaggedText> getInfraSpeciesTaggedNameCache(TaxonName name){
        if (name.getNameType().isZoological()){
            boolean includeMarker =includeInfraSpecificMarkerForZooNames(name);
            return getInfraSpeciesTaggedNameCache(name, includeMarker);
        }else{
            return getInfraSpeciesTaggedNameCache(name, true);
        }
    }

    protected boolean includeInfraSpecificMarkerForZooNames(TaxonName name){
        return ! (name.isAutonym());  //only exclude marker if autonym, see also ZooNameNoMarkerCacheStrategy
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
            if (isNotBlank(marker)){
                tags.add(new TaggedText(TagEnum.rank, marker));
            }

        }
        String infrSpecEpi = CdmUtils.Nz(nonViralName.getInfraSpecificEpithet());

        infrSpecEpi = infrSpecEpi.trim().replace("null", "");

        if (isNotBlank(infrSpecEpi)){
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
        boolean hasInfraGenericEpi = isNotBlank(nonViralName.getInfraGenericEpithet());
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
        if (isNotBlank(specEpi)){
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
        if (isNotBlank(appendedPhrase)){
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


}
