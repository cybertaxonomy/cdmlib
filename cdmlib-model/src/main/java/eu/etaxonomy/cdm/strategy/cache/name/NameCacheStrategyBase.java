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
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author AM
 */
public abstract class NameCacheStrategyBase
        extends StrategyBase
        implements INameCacheStrategy {
    private static final long serialVersionUID = -2322348388258675517L;

    private static final Logger logger = Logger.getLogger(NameCacheStrategyBase.class);

    final static UUID uuid = UUID.fromString("817ae5b5-3ac2-414b-a134-a9ae86cba040");

    /**
     * Constructor
     */
    public NameCacheStrategyBase() {
        super();
    }


    @Override
    public String getFullTitleCache(TaxonName taxonName, HTMLTagRules htmlTagRules) {
        List<TaggedText> tags = getTaggedFullTitle(taxonName);
        if (tags == null){
            return null;
        }else{
            String result = createString(tags, htmlTagRules);
            return result;
        }
    }

    @Override
    public String getFullTitleCache(TaxonName taxonName) {
        return getFullTitleCache(taxonName, null);
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


    /**
     * Generates and returns the "name cache" (only scientific name without author teams and year).
     * @see eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy#getNameCache(eu.etaxonomy.cdm.model.name.TaxonName)
     */
    @Override
    public String getNameCache(TaxonName nonViralName) {
        List<TaggedText> tags = getTaggedName(nonViralName);
        if (tags == null){
            return null;
        }else{
            String result = createString(tags);
            return result;
        }
    }


    /**
     * Generates and returns the title cache of the given name.
     * The title cache in general includes the name and the authorship and year for some types of names.
     *
     * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.CdmBase)
     * @see eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.IdentifiableEntity)
     */
    @Override
    public String getTitleCache(TaxonName nonViralName) {
        return getTitleCache(nonViralName, null);
    }

    @Override
    public String getTitleCache(TaxonName nonViralName, HTMLTagRules htmlTagRules) {
        List<TaggedText> tags = getTaggedTitle(nonViralName);
        if (tags == null){
            return null;
        }else{
            String result = createString(tags, htmlTagRules);
            return result;
        }
    }

    @Override
    public List<TaggedText> getTaggedTitle(TaxonName taxonName) {
        if (taxonName == null){
            return null;
        }
        //TODO how to handle protected fullTitleCache here?

        if (taxonName.isProtectedTitleCache()){
            //protected title cache
            List<TaggedText> tags = new ArrayList<>();
            tags.add(new TaggedText(TagEnum.name, taxonName.getTitleCache()));
            return tags;
        }else{
            return doGetTaggedTitle(taxonName);
        }

    }

    protected abstract List<TaggedText> doGetTaggedTitle(TaxonName taxonName);

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
//      String titleCache = nonViralName.getTitleCache();
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

    protected void addOriginalSpelling(List<TaggedText> tags, TaxonName name){
        String originalName = getOriginalNameString(name, tags);
        if (StringUtils.isNotBlank(originalName)){
            tags.add(new TaggedText(TagEnum.name, originalName));
        }
    }

    private String getOriginalNameString(TaxonName currentName, List<TaggedText> originalNameTaggs) {
        List<String> originalNameStrings = new ArrayList<>(1);
        currentName = CdmBase.deproxy(currentName);
        //Hibernate.initialize(currentName.getRelationsToThisName());
        for (NameRelationship nameRel : currentName.getRelationsToThisName()){  //handle list, just in case we have strange data; this may result in strange looking results
            NameRelationshipType type = nameRel.getType();
            if(type != null && type.equals(NameRelationshipType.ORIGINAL_SPELLING())){
                String originalNameString;
                TaxonName originalName = nameRel.getFromName();
                if (!originalName.isNonViral()){
                    originalNameString = originalName.getTitleCache();
                }else{
                    INonViralName originalNvName = CdmBase.deproxy(originalName);
                    originalNameString = makeOriginalNameString(currentName, originalNvName, originalNameTaggs);
                }
                originalNameStrings.add("[as " + UTF8.QUOT_DBL_LOW9 + originalNameString + UTF8.QUOT_DBL_LEFT + "]");
            }
        }
        if (originalNameStrings.size() > 0){
            String result = CdmUtils.concat("", originalNameStrings.toArray(new String[originalNameStrings.size()])) ;
            return result;
        }else{
            return null;
        }
    }

    private String makeOriginalNameString(TaxonName currentName, INonViralName originalName,
            List<TaggedText> currentNameTags) {
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
//      if (originalName.getGenusOrUninomial() != null && originalName.getGenusOrUninomial().equals(currentName.getGenusOrUninomial())){
//
//      }
        return result;
    }

    /**
     * @param tags
     * @return
     */
    protected String createString(List<TaggedText> tags) {
        return TaggedCacheHelper.createString(tags);
    }

    /**
     * @param tags
     * @param htmlTagRules
     * @return
     */
    protected String createString(List<TaggedText> tags, HTMLTagRules htmlTagRules) {
        return TaggedCacheHelper.createString(tags, htmlTagRules);
    }

}
