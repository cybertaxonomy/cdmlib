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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.format.reference.NomenclaturalSourceFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImplRegExBase;

/**
 * @author AM
 */
public abstract class NameCacheStrategyBase
        extends StrategyBase
        implements INameCacheStrategy {
    private static final long serialVersionUID = -2322348388258675517L;

    private static final Logger logger = Logger.getLogger(NameCacheStrategyBase.class);

    final static UUID uuid = UUID.fromString("817ae5b5-3ac2-414b-a134-a9ae86cba040");

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

    @Override
    public List<TaggedText> getNomStatusTags(TaxonName taxonName, boolean includeSeparatorBefore,
            boolean includeSeparatorAfter) {

        Set<NomenclaturalStatus> ncStati = taxonName.getStatus();
        Iterator<NomenclaturalStatus> iterator = ncStati.iterator();
        List<TaggedText> nomStatusTags = new ArrayList<>();
        while (iterator.hasNext()) {
            NomenclaturalStatus ncStatus = iterator.next();
            // since the NewInstance method of nomencatural status allows null as parameter
            // we have to check for null values here
            String nomStatusStr = "not defined";
            if(ncStatus.getType() != null){
                NomenclaturalStatusType statusType =  ncStatus.getType();
                List<Language> prefLangs = Arrays.asList(new Language[]{Language.LATIN(), Language.DEFAULT()});
                Representation repr = statusType.getPreferredRepresentation(prefLangs);
                if (repr != null){
                    if(!Language.LATIN().equals(repr.getLanguage())){
                        String message = "No latin representation available for nom. status. " + statusType.getTitleCache();
                        logger.info(message);
                    }
                    nomStatusStr = repr.getAbbreviatedLabel();
                }else{
                    String message = "No representation available for nom. status. " + statusType.getTitleCache();
                    logger.warn(message);
                    nomStatusStr = statusType.getTitleCache();
                }
            }else if(isNotBlank(ncStatus.getRuleConsidered())){
                nomStatusStr = ncStatus.getRuleConsidered();
            }
            String statusSeparator = ", ";
            if (includeSeparatorBefore){
                nomStatusTags.add(new TaggedText(TagEnum.separator, statusSeparator));
            }
            nomStatusTags.add(new TaggedText(TagEnum.nomStatus, nomStatusStr, new TypedEntityReference<>(ncStatus.getClass(), ncStatus.getUuid())));
            if (includeSeparatorAfter){
                nomStatusTags.add(new TaggedText(TagEnum.postSeparator, ","));
            }
        }
        return nomStatusTags;
    }

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

    @Override
    public String getNameCache(TaxonName nonViralName, HTMLTagRules htmlTagRules) {
        List<TaggedText> tags = getTaggedName(nonViralName);
        if (tags == null){
            return null;
        }else{
            String result = createString(tags, htmlTagRules);
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
    public String getTitleCache(TaxonName taxonName) {
        return getTitleCache(taxonName, null);
    }

    @Override
    public String getTitleCache(TaxonName taxonName, HTMLTagRules htmlTagRules) {
        List<TaggedText> tags = getTaggedTitle(taxonName);
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
    public List<TaggedText> getTaggedFullTitle(TaxonName taxonName) {
        List<TaggedText> tags = new ArrayList<>();

        //null
        if (taxonName == null){
            return null;
        }

        //protected full title cache
        if (taxonName.isProtectedFullTitleCache()){
            tags.add(new TaggedText(TagEnum.fullName, taxonName.getFullTitleCache()));
            return tags;
        }

        //title cache
//      String titleCache = nonViralName.getTitleCache();
        List<TaggedText> titleTags = getTaggedTitle(taxonName);
        tags.addAll(titleTags);

        //reference
        String referenceCache = NomenclaturalSourceFormatter.INSTANCE().format(taxonName.getNomenclaturalSource());
            //add to tags
        if (isNotBlank(referenceCache)){
            if (! referenceCache.trim().startsWith("in ")){
                String refConcat = ", ";
                tags.add(new TaggedText(TagEnum.separator, refConcat));
            }
            tags.add(new TaggedText(TagEnum.reference, referenceCache));
        }

        addOriginalSpelling(tags, taxonName);

        //nomenclatural status
        tags.addAll(getNomStatusTags(taxonName, true, false));
        return tags;
    }

    protected void addOriginalSpelling(List<TaggedText> tags, TaxonName currentName){

        currentName = CdmBase.deproxy(currentName);
        //Hibernate.initialize(currentName.getRelationsToThisName());
        TaxonName originalName = currentName.getOriginalSpelling();
        if (originalName != null){
            String originalNameString;
            tags.add(TaggedText.NewSeparatorInstance(" [as \""));
            if (!originalName.isNonViral()){
                originalNameString = originalName.getTitleCache();
                tags.add(new TaggedText(TagEnum.name, originalNameString));
            }else{
                INonViralName originalNvName = CdmBase.deproxy(originalName);
                originalNameString = makeOriginalNameString(originalNvName, tags);
                for (String split : originalNameString.split(" ")){
                    if (split.matches(NonViralNameParserImplRegExBase.infraSpeciesMarker)
                            || split.matches(NonViralNameParserImplRegExBase.oldInfraSpeciesMarker)) {
                        tags.add(new TaggedText(TagEnum.rank, split));
                    }else{
                        tags.add(new TaggedText(TagEnum.name, split));
                    }
                }
            }
            tags.add(TaggedText.NewSeparatorInstance("\"]"));
        }else{
            return;
        }
    }

    private String makeOriginalNameString(INonViralName originalName,
            List<TaggedText> currentNameTags) {
        //use cache if necessary
        String cacheToUse = null;
        if (originalName.isProtectedNameCache() && isNotBlank(originalName.getNameCache())){
            cacheToUse = originalName.getNameCache();
        }else if (originalName.isProtectedTitleCache() && isNotBlank(originalName.getTitleCache())){
            cacheToUse = originalName.getTitleCache();
        }else if (originalName.isProtectedFullTitleCache() && isNotBlank(originalName.getFullTitleCache())){
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
        Integer firstDiff = null;
        Integer lastDiff = -1;
        for (int i = 0; i < Math.min(originalNameSplit.length, currentNameSplit.length); i++){
            if (!originalNameSplit[i].equals(currentNameSplit[i])){
                lastDiff = i;
                firstDiff = (firstDiff == null) ? i : firstDiff;
            }
        }
        if (firstDiff != null){
            result = CdmUtils.concat(" ", Arrays.asList(originalNameSplit).subList(firstDiff, lastDiff+1).toArray(new String[0]));
        }

        return result;
    }

    protected String createString(List<TaggedText> tags) {
        return TaggedCacheHelper.createString(tags);
    }

    protected String createString(List<TaggedText> tags, HTMLTagRules htmlTagRules) {
        return TaggedCacheHelper.createString(tags, htmlTagRules);
    }
}