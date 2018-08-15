/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.taxon;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.mueller
 * @since 13.08.2018
 *
 */
public class TaxonRelationshipFormatter {

    private static final String REL_SEC = ", rel. sec. ";
    private static final String ERR_SEC = ", err. sec. ";
    private static final String SYN_SEC = ", syn. sec. ";
    private static final String UNKNOWN_SEC = "???";
    private static final String NON_SEPARATOR = ", non ";
    private static final String QUOTE_START = "\"";   //TODO
    private static final String QUOTE_END = "\"";   //TODO
    private static final String AUCT = "auct.";
    private static final String SENSU_SEPARATOR = " sensu ";
    private static final String SEC_SEPARATOR = " sec. ";
    private static final String DETAIL_SEPARATOR = ": ";
    private static final String INVERT_SYMBOL = "<-"; //TODO
    private static final String UNDEFINED_SYMBOL = "??";  //TODO

    public List<TaggedText> getTaggedText(TaxonRelationship taxonRelationship, boolean reverse, List<Language> languages) {

        if (taxonRelationship == null){
            return null;
        }

        TaxonRelationshipType type = taxonRelationship.getType();
        boolean isMisapplied = type == null ? false : type.isAnyMisappliedName() && reverse;
        boolean isSynonym = type == null? false : type.isAnySynonym();

        Taxon relatedTaxon = reverse? taxonRelationship.getFromTaxon()
                : taxonRelationship.getToTaxon();

        if (relatedTaxon == null){
            return null;
        }
        TaxonName name = relatedTaxon.getName();

        List<TaggedText> tags = new ArrayList<>();

        //rel symbol
        String symbol = getSymbol(type, reverse, languages);
        tags.add(TaggedText.NewInstance(TagEnum.symbol, symbol));

        //whitespace
        tags.add(TaggedText.NewWhitespaceInstance());

        //name
        if (isMisapplied){
            //starting quote
            String startQuote = QUOTE_START;
            tags.add(TaggedText.NewSeparatorInstance(startQuote));

            //name cache
            List<TaggedText> nameCacheTags = getNameCacheTags(name);
            tags.addAll(nameCacheTags);

            //end quote
            String endQuote = QUOTE_END;
            tags.add(TaggedText.NewSeparatorInstance(endQuote));
        }else{
            //name title cache
            //TODO fullTitle?
            List<TaggedText> nameCacheTags = getNameTitleCacheTags(name);
            tags.addAll(nameCacheTags);
        }


        //sensu (+ Separatoren?)
        if (isNotBlank(relatedTaxon.getAppendedPhrase())){
            tags.add(TaggedText.NewWhitespaceInstance());
            tags.add(TaggedText.NewInstance(TagEnum.appendedPhrase, relatedTaxon.getAppendedPhrase()));
        }
        List<TaggedText> secTags = getSensuTags(relatedTaxon.getSec(), relatedTaxon.getSecMicroReference(), isMisapplied);
        if (!secTags.isEmpty()) {
            tags.add(TaggedText.NewSeparatorInstance(isMisapplied? SENSU_SEPARATOR : SEC_SEPARATOR));
            tags.addAll(secTags);
        }else if (isBlank(relatedTaxon.getAppendedPhrase())) {
            if (isMisapplied){
                tags.add(TaggedText.NewWhitespaceInstance());
                //TODO type unclear sensuReference(?)
                tags.add(TaggedText.NewInstance(TagEnum.authors, AUCT));
            }else{
                tags.add(TaggedText.NewSeparatorInstance(SEC_SEPARATOR + UNKNOWN_SEC));
            }
        }

//        //, non author
        if (isMisapplied && name != null){
            if (name.getCombinationAuthorship() != null){
                tags.add(TaggedText.NewSeparatorInstance(NON_SEPARATOR));
                //TODO add nom. ref. author tags
            }else if (isNotBlank(name.getAuthorshipCache())){
                tags.add(TaggedText.NewSeparatorInstance(NON_SEPARATOR));
                tags.add(TaggedText.NewInstance(TagEnum.authors, name.getAuthorshipCache().trim()));
            }
        }

        //TODO tagEnum for relSec?
        List<TaggedText> relSecTags = getSensuTags /*getCitationTags*/(taxonRelationship.getCitation(),
                taxonRelationship.getCitationMicroReference(), false);
        if (!relSecTags.isEmpty()){
            TaggedText relSecSeparatorToag = TaggedText.NewSeparatorInstance(isSynonym ? SYN_SEC : isMisapplied ? ERR_SEC : REL_SEC);
            tags.add(relSecSeparatorToag);
            tags.addAll(relSecTags);
        }

        return tags;
    }

    private List<TaggedText> getSensuTags(Reference ref, String detail, boolean isSensu) {
        List<TaggedText> result = new ArrayList<>();
        String secRef;

        if (ref != null){
            TeamOrPersonBase<?> author = ref.getAuthorship();
            //TODO distinguish linked and unlinked usage,
            // if reference is not linked short citation should only be used
            //   if both author and year exists, also initials should be added in this case
            //
            if (ref.isProtectedTitleCache() == false &&
                    author != null &&
                    isNotBlank(author.getTitleCache())){
                //TODO move to authorFormatter
                String familyNames = getFamilyNames(author);
                if (isNotBlank(familyNames)){
                    secRef = familyNames;
                }else{
                    secRef = ref.getAuthorship().getTitleCache();
                }
                if (isNotBlank(ref.getYear())){
                   secRef += " " + ref.getYear();
                }
            }else{
                secRef = ref.getTitleCache();
            }
            TagEnum secType = isSensu? TagEnum.sensuReference : TagEnum.secReference;
            TaggedText refTag = TaggedText.NewInstance(secType, secRef);
            refTag.setEntityReference(new TypedEntityReference<>(ref.getClass(), ref.getUuid(), secRef));
            result.add(refTag);
        }
        if (isNotBlank(detail)){
            result.add(TaggedText.NewSeparatorInstance(DETAIL_SEPARATOR));
            //TODO do we need a sensu micro reference??
            TagEnum detailType = isSensu? TagEnum.sensuMicroReference : TagEnum.secMicroReference;
            TaggedText microTag = TaggedText.NewInstance(detailType, detail);
            result.add(microTag);
        }
        return result;
    }

    /**
     * @param author
     * @return
     */
    private String getFamilyNames(TeamOrPersonBase<?> author) {
        if (author.isInstanceOf(Person.class)){
            Person person = CdmBase.deproxy(author, Person.class);
            return isNotBlank(person.getFamilyName())? person.getFamilyName() : null;
        }else{
            Team team = CdmBase.deproxy(author, Team.class);
            String result = null;
            int n = team.getTeamMembers().size();
            int index = 0;
            if (team.isHasMoreMembers()){
                n++;
            }
            for (Person member : team.getTeamMembers()){
                String name = isNotBlank(member.getFamilyName())? member.getFamilyName(): member.getTitleCache();
                String separator = index < n ? ", " : " & ";
                result = CdmUtils.concat(separator, result, name);
                index++;
            }
            if (team.isHasMoreMembers()){
                //TODO or et al.???
                result += " & al.";
            }
            return result;
        }
    }


    /**
     * @param sec
     * @param secMicroReference
     * @return
     */
    private List<TaggedText> getCitationTags(Reference ref, String secMicroReference) {
        List<TaggedText> result = new ArrayList<>();
        String secRef;

        if (ref != null){
            //copied from TaxonBaseDefaultCacheStrategy
            if (ref.isProtectedTitleCache() == false &&
                    ref.getCacheStrategy() != null &&
                    ref.getAuthorship() != null &&
                    isNotBlank(ref.getAuthorship().getTitleCache()) &&
                    isNotBlank(ref.getYear())){
                secRef = ref.getCacheStrategy().getCitation(ref);
            }else{
                secRef = ref.getTitleCache();
            }
            //TODO do we need a sensuReference type?
            TaggedText refTag = TaggedText.NewInstance(TagEnum.secReference, secRef);
            refTag.setEntityReference(new TypedEntityReference<>(ref.getClass(), ref.getUuid(), secRef));
            result.add(refTag);
        }
        if (isNotBlank(secMicroReference)){
            result.add(TaggedText.NewSeparatorInstance(DETAIL_SEPARATOR));
            TaggedText microTag = TaggedText.NewInstance(TagEnum.secMicroReference, secMicroReference);
            result.add(microTag);
        }
        return result;
    }


    /**
     * @param name
     * @return
     */
    private List<TaggedText> getNameCacheTags(TaxonName name) {
        List<TaggedText> result = name.getCacheStrategy().getTaggedName(name);
        return result;
    }

    private List<TaggedText> getNameTitleCacheTags(TaxonName name) {

        //TODO full title?
        List<TaggedText> result = name.getCacheStrategy().getTaggedTitle(name);
        return result;
    }


    /**
     * @param type the taxon relationship type
     * @param reverse is the relationship used reverse
     * @param languages list of preferred languages
     * @return the symbol for the taxon relationship
     */
    private String getSymbol(TaxonRelationshipType type, boolean reverse, List<Language> languages) {
        if (type == null){
            return UNDEFINED_SYMBOL;
        }

        //symbol
        String symbol = reverse? type.getInverseSymbol():type.getSymbol();
        if (isNotBlank(symbol)){
            return symbol;
        }

        boolean isSymmetric = type.isSymmetric();
        //symmetric inverted symbol
        String invertedSymbol = reverse? type.getSymbol() : type.getInverseSymbol();
        if (isSymmetric && isNotBlank(invertedSymbol)){
            return invertedSymbol;
        }

        //abbrev label
        Representation representation = reverse? type.getPreferredRepresentation(languages): type.getPreferredInverseRepresentation(languages);
        String abbrevLabel = representation.getAbbreviatedLabel();
        if (isNotBlank(abbrevLabel)){
            return abbrevLabel;
        }

        //symmetric inverted abbrev label
        Representation invertedRepresentation = reverse? type.getPreferredInverseRepresentation(languages):type.getPreferredRepresentation(languages);
        String invertedAbbrevLabel = invertedRepresentation.getAbbreviatedLabel();
        if (isSymmetric && isNotBlank(invertedAbbrevLabel)){
            return invertedAbbrevLabel;
        }

        //non symmetric inverted symbol
        if (!isSymmetric && isNotBlank(invertedSymbol)){
            return INVERT_SYMBOL + invertedSymbol;
        }

        //non symmetric inverted abbrev label
        if (!isSymmetric && isNotBlank(invertedAbbrevLabel)){
            return INVERT_SYMBOL + invertedAbbrevLabel;
        }

        return UNDEFINED_SYMBOL;
    }

    private boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }

    private boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }
}
