/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.taxon;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;

public class TaxonBaseDefaultCacheStrategy<T extends TaxonBase>
        extends StrategyBase
        implements ITaxonCacheStrategy<T> {

    private static final long serialVersionUID = 5769890979070021350L;

    final static UUID uuid = UUID.fromString("931e48f0-2033-11de-8c30-0800200c9a66");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

    @Override
    public String getTitleCache(T taxonBase) {
        return getTitleCache(taxonBase, null);
    }

    @Override
    public List<TaggedText> getTaggedTitle(T taxonBase) {
        if (taxonBase == null){
            return null;
        }

        List<TaggedText> tags = new ArrayList<>();

        if (taxonBase.isProtectedTitleCache()){
            //protected title cache
            tags.add(new TaggedText(TagEnum.name, taxonBase.getTitleCache()));
            return tags;
        }

        if (taxonBase.isDoubtful()){
            tags.add(new TaggedText(TagEnum.separator, "?"));
        }

        boolean isMisapplication = isMisapplication(taxonBase);
        //name
        List<TaggedText> nameTags = getNameTags(taxonBase, isMisapplication);

        if (nameTags.size() > 0){
            tags.addAll(nameTags);
        }else{
            tags.add(new TaggedText(TagEnum.fullName, "???"));
        }

        boolean isSynonym = taxonBase.isInstanceOf(Synonym.class);
        String secSeparator =  isMisapplication ? " sensu " : (isSynonym? " syn." : "") + " sec. ";
        //not used: we currently use a post-separator in the name tags
//                if (nameTags.get(nameTags.size() - 1).getType().equals(TagEnum.nomStatus)){
//                    secSeparator = "," + secSeparator;
//                }

        //sec.
        List<TaggedText> secTags = getSecundumTags(taxonBase, isMisapplication);
        if (!secTags.isEmpty()){
            tags.add(new TaggedText(TagEnum.separator, secSeparator));
            tags.addAll(secTags);
        }else if (isMisapplication && isBlank(taxonBase.getAppendedPhrase())){
            tags.add(new TaggedText(TagEnum.appendedPhrase, "auct."));
        }

        if (isMisapplication){
            TaxonName name = CdmBase.deproxy(taxonBase.getName());
            if (name != null && isNotBlank(name.getAuthorshipCache())){
                tags.add(new TaggedText(TagEnum.separator, ", non "));
                tags.add(new TaggedText(TagEnum.authors, name.getAuthorshipCache()));
            }
        }

        return tags;
    }

    private boolean isMisapplication(T taxonBase) {
        if (! taxonBase.isInstanceOf(Taxon.class)){
            return false;
        }else{
            return CdmBase.deproxy(taxonBase, Taxon.class).isMisapplicationOnly();
        }
    }

    private List<TaggedText> getNameTags(T taxonBase, boolean useNameCache) {
        List<TaggedText> tags = new ArrayList<>();
        TaxonName name = CdmBase.deproxy(taxonBase.getName());

        if (name != null){
            INameCacheStrategy nameCacheStrategy = name.getCacheStrategy();
            useNameCache = (useNameCache || taxonBase.isUseNameCache()) && name.isNonViral() && nameCacheStrategy instanceof INonViralNameCacheStrategy;
            if (useNameCache){
                INonViralNameCacheStrategy nvnCacheStrategy = (INonViralNameCacheStrategy)nameCacheStrategy;
                List<TaggedText> nameCacheTags = nvnCacheStrategy.getTaggedName(name);
                tags.addAll(nameCacheTags);
            }else{
                List<TaggedText> nameTags = nameCacheStrategy.getTaggedTitle(name);
                tags.addAll(nameTags);
                List<TaggedText> statusTags = nameCacheStrategy.getNomStatusTags(name, true, true);
                tags.addAll(statusTags);
            }
            if (isNotBlank(taxonBase.getAppendedPhrase())){
                tags.add(new TaggedText(TagEnum.appendedPhrase, taxonBase.getAppendedPhrase().trim()));
            }
        }

        return tags;
    }

    private List<TaggedText> getSecundumTags(T taxonBase, boolean isMisapplication) {
        List<TaggedText> tags = new ArrayList<>();

        Reference sec = taxonBase.getSec();
        sec = HibernateProxyHelper.deproxy(sec);
        String secRef;
        if (sec == null){
            //missing sec
            if (isBlank(taxonBase.getAppendedPhrase()) && !isMisapplication ){
                secRef = "???";
            }else{
                secRef = null;
            }
        }
        else{
            //existing sec
            if (sec.isProtectedTitleCache() == false &&
                    sec.getCacheStrategy() != null &&
                    sec.getAuthorship() != null &&
                    isNotBlank(sec.getAuthorship().getTitleCache()) &&
                    isNotBlank(sec.getYear())){
                secRef = OriginalSourceFormatter.INSTANCE.format(sec, null);  //microRef is handled later
            }else if ((sec.isWebPage() || sec.isDatabase() || sec.isMap())
                    && titleExists(sec)){  //maybe we should also test protected caches (but which one, the abbrev cache or the titleCache?
                secRef = isNotBlank(sec.getAbbrevTitle())? sec.getAbbrevTitle() : sec.getTitle();
                String secDate = sec.getYear();
                if (isBlank(secDate) && sec.getAccessed() != null){
                    secDate = String.valueOf(sec.getAccessed().getYear());
                }
                secRef = CdmUtils.concat(" ", secRef, secDate);
            }else{
                secRef = sec.getTitleCache();
                //TODO maybe not always correct
                if (secTitleTrailingDotShouldBeRemoved(sec)){
                    secRef = CdmUtils.removeTrailingDots(secRef);
                }
            }
        }
        if (secRef != null){
            tags.add(new TaggedText(TagEnum.secReference, secRef));
        }
        //secMicroReference
        if (isNotBlank(taxonBase.getSecMicroReference())){
            tags.add(new TaggedText(TagEnum.separator, ": "));
            tags.add(new TaggedText(TagEnum.secMicroReference, taxonBase.getSecMicroReference()));
        }
        return tags;
    }


    private boolean secTitleTrailingDotShouldBeRemoved(Reference sec) {
        if (sec.isProtectedTitleCache()){
            return false;
        }else if (sec.getAbbrevTitle()!= null && sec.getTitleCache().endsWith(sec.getAbbrevTitle())){
            return false;
        }else if (sec.getTitle() != null && sec.getTitle().endsWith(".") && sec.getTitleCache().endsWith(sec.getTitle())){
            return false;
        }
        return true;
    }

    private boolean titleExists(Reference ref) {
        return isNotBlank(ref.getAbbrevTitle()) || isNotBlank(ref.getTitle());
    }

    @Override
    public String getTitleCache(T taxonBase, HTMLTagRules htmlTagRules) {
        List<TaggedText> tags = getTaggedTitle(taxonBase);
        if (tags == null){
            return null;
        }else{
            String result = TaggedCacheHelper.createString(tags, htmlTagRules);
            return result;
        }
    }
}