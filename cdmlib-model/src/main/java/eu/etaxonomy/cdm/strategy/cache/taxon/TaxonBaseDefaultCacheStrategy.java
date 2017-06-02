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

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
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

        if (taxonBase.isDoubtful()){
            tags.add(new TaggedText(TagEnum.separator, "?"));
        }
        if (taxonBase.isProtectedTitleCache()){
            //protected title cache
            tags.add(new TaggedText(TagEnum.name, taxonBase.getTitleCache()));
        }else{
            //name
            List<TaggedText> nameTags = getNameTags(taxonBase);

            if (nameTags.size() > 0){
                tags.addAll(nameTags);
            }else{
                tags.add(new TaggedText(TagEnum.fullName, "???"));
            }

            boolean isSynonym = taxonBase.isInstanceOf(Synonym.class);
            String secSeparator =  (isSynonym? " syn." : "") + " sec. ";
            //not used: we currently use a post-separator in the name tags
//                if (nameTags.get(nameTags.size() - 1).getType().equals(TagEnum.nomStatus)){
//                    secSeparator = "," + secSeparator;
//                }

            //ref.
            List<TaggedText> secTags = getSecundumTags(taxonBase);

            //sec.
            if (!secTags.isEmpty()){
                tags.add(new TaggedText(TagEnum.separator, secSeparator));
                tags.addAll(secTags);
            }

        }
        return tags;
    }

    private List<TaggedText> getNameTags(T taxonBase) {
        List<TaggedText> tags = new ArrayList<>();
        TaxonNameBase<?,INameCacheStrategy<TaxonNameBase>> name = CdmBase.deproxy(taxonBase.getName());

        if (name != null){
            INameCacheStrategy<TaxonNameBase> nameCacheStrategy = name.getCacheStrategy();
            if (taxonBase.isUseNameCache() && name.isInstanceOf(NonViralName.class)){
                List<TaggedText> nameCacheTags = nameCacheStrategy.getTaggedName(name);
                tags.addAll(nameCacheTags);
            }else{
                List<TaggedText> nameTags = nameCacheStrategy.getTaggedTitle(name);
                tags.addAll(nameTags);
                List<TaggedText> statusTags = nameCacheStrategy.getNomStatusTags(name, true, true);
                tags.addAll(statusTags);
            }
            if (StringUtils.isNotBlank(taxonBase.getAppendedPhrase())){
                tags.add(new TaggedText(TagEnum.appendedPhrase, taxonBase.getAppendedPhrase().trim()));
            }
        }

        return tags;
    }

    private List<TaggedText> getSecundumTags(T taxonBase) {
        List<TaggedText> tags = new ArrayList<>();

        Reference ref = taxonBase.getSec();
        ref = HibernateProxyHelper.deproxy(ref, Reference.class);
        String secRef;
        if (ref == null){
            //missing sec
            if (isBlank(taxonBase.getAppendedPhrase())){
                secRef = "???";
            }else{
                secRef = null;
            }
        }
        else{
            //existing sec
            if (ref.isProtectedTitleCache() == false &&
                    ref.getCacheStrategy() != null &&
                    ref.getAuthorship() != null &&
                    isNotBlank(ref.getAuthorship().getTitleCache()) &&
                    isNotBlank(ref.getYear())){
                secRef = ref.getCacheStrategy().getCitation(ref);
            }else{
                secRef = ref.getTitleCache();
            }
        }
        if (secRef != null){
            tags.add(new TaggedText(TagEnum.secReference, secRef));
        }
        //secMicroReference
        if (StringUtils.isNotBlank(taxonBase.getSecMicroReference())){
            tags.add(new TaggedText(TagEnum.separator, ": "));
            tags.add(new TaggedText(TagEnum.secReference, taxonBase.getSecMicroReference()));

        }
        return tags;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaggedText> getMisappliedTaggedTitle(Taxon taxon) {
        if (taxon == null){
            return null;
        }

        List<TaggedText> tags = new ArrayList<>();

        if (taxon.isDoubtful()){
            tags.add(new TaggedText(TagEnum.separator, "?"));
        }
        if (taxon.isProtectedTitleCache()){
            //protected title cache
            tags.add(new TaggedText(TagEnum.name, taxon.getTitleCache()));
        }else{
            //name
            List<TaggedText> nameTags = getMisappliedNameTags(taxon);

            if (nameTags.size() > 0){
                tags.addAll(nameTags);
            }else{
                tags.add(new TaggedText(TagEnum.fullName, "???"));
            }

            String appendedPhrase = taxon.getAppendedPhrase();
            List<TaggedText> secTags = new ArrayList<>();;
            taxon.getSec();
            if (!hasSecOrAppendedPhrase(taxon)){
                tags.add(new TaggedText(TagEnum.appendedPhrase, "auct."));
            }else{
                if (isNotBlank(taxon.getAppendedPhrase())){
                    tags.add(new TaggedText(TagEnum.appendedPhrase, taxon.getAppendedPhrase()));
                }
                String secSeparator =  "err. sec. ";
                if (taxon.getSec() != null){

                }

                //ref. //FIXME
                secTags = getSecundumTags((T)taxon);
            }

            //not used: we currently use a post-separator in the name tags
//                if (nameTags.get(nameTags.size() - 1).getType().equals(TagEnum.nomStatus)){
//                    secSeparator = "," + secSeparator;
//                }

            ////FIXME
            String secSeparator = null;
            //sec.
            if (!secTags.isEmpty()){
                tags.add(new TaggedText(TagEnum.separator, secSeparator));
                tags.addAll(secTags);
            }

        }
        return tags;
    }

    /**
     * @param taxon
     * @return
     */
    private boolean hasSecOrAppendedPhrase(Taxon taxon) {
        return isNotBlank(taxon.getAppendedPhrase())
                || taxon.getSec() != null
                || isNotBlank(taxon.getSecMicroReference());
    }

    private List<TaggedText> getMisappliedNameTags(Taxon taxon) {
        List<TaggedText> tags = new ArrayList<>();
        TaxonNameBase<?,INameCacheStrategy<TaxonNameBase>> name = CdmBase.deproxy(taxon.getName());

        if (name != null){
            INameCacheStrategy<TaxonNameBase> nameCacheStrategy = name.getCacheStrategy();

            List<TaggedText> nameCacheTags = nameCacheStrategy.getTaggedName(name);
            tags.addAll(nameCacheTags);

            //FIXME
            if (StringUtils.isNotBlank(taxon.getAppendedPhrase())){
                tags.add(new TaggedText(TagEnum.appendedPhrase, taxon.getAppendedPhrase().trim()));
            }
        }

        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMisappliedTitle(Taxon taxon) {
        List<TaggedText> tags = getMisappliedTaggedTitle(taxon);
        if (tags == null){
            return null;
        }else{
            HTMLTagRules htmlTagRules = null;
            String result = TaggedCacheHelper.createString(tags, htmlTagRules);
            return result;
        }
    }
}
