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
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;

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
        boolean isMisapplied = type == null ? false : type.isMisappliedNameOrInvalidDesignation() && reverse;
        boolean isSynonym = type == null? false : type.isAnySynonym();


        Taxon relatedTaxon = reverse? taxonRelationship.getFromTaxon()
                : taxonRelationship.getToTaxon();

        if (relatedTaxon == null){
            return null;
        }
        boolean isDoubtful = taxonRelationship.isDoubtful() || relatedTaxon.isDoubtful();
        String doubtfulStr = isDoubtful ? "?" : "";

        TaxonName name = relatedTaxon.getName();

//        List<TaggedText> tags = new ArrayList<>();
        TaggedTextBuilder builder = new TaggedTextBuilder();

        //rel symbol
        String symbol = getSymbol(type, reverse, languages);
        builder.add(TagEnum.symbol, symbol);

        //name
        if (isMisapplied){
            //starting quote
            String startQuote = " " + doubtfulStr + QUOTE_START;
            builder.addSeparator(startQuote);// .add(TaggedText.NewSeparatorInstance(startQuote));

            //name cache
            List<TaggedText> nameCacheTags = getNameCacheTags(name);
            builder.addAll(nameCacheTags);

            //end quote
            String endQuote = QUOTE_END;
            builder.add(TagEnum.postSeparator, endQuote);
        }else{
            builder.addSeparator(" " + doubtfulStr);
            //name full title cache
            List<TaggedText> nameCacheTags = getNameTitleCacheTags(name);
            builder.addAll(nameCacheTags);
        }


        //sensu (+ Separatoren?)
        if (isNotBlank(relatedTaxon.getAppendedPhrase())){
            builder.addWhitespace();
            builder.add(TagEnum.appendedPhrase, relatedTaxon.getAppendedPhrase());
        }
        List<TaggedText> secTags = getSensuTags(relatedTaxon.getSec(), relatedTaxon.getSecMicroReference(),
               /* isMisapplied,*/ false);
        if (!secTags.isEmpty()) {
            builder.addSeparator(isMisapplied? SENSU_SEPARATOR : SEC_SEPARATOR);
            builder.addAll(secTags);
        }else if (isBlank(relatedTaxon.getAppendedPhrase())) {
            if (isMisapplied){
                builder.addWhitespace();
                //TODO type unclear sensuReference(?)
                builder.add(TagEnum.appendedPhrase, AUCT);
            }else{
                builder.addSeparator(SEC_SEPARATOR + UNKNOWN_SEC);
            }
        }

//        //, non author
        if (isMisapplied && name != null){
            if (name.getCombinationAuthorship() != null && isNotBlank(name.getCombinationAuthorship().getNomenclaturalTitle())){
                builder.addSeparator(NON_SEPARATOR);
                builder.add(TagEnum.authors, name.getCombinationAuthorship().getNomenclaturalTitle());
            }else if (isNotBlank(name.getAuthorshipCache())){
                builder.addSeparator(NON_SEPARATOR);
                builder.add(TagEnum.authors, name.getAuthorshipCache().trim());
            }
        }

        List<TaggedText> relSecTags = getSensuTags(taxonRelationship.getCitation(),
                taxonRelationship.getCitationMicroReference(),true);
        if (!relSecTags.isEmpty()){
            builder.addSeparator(isSynonym ? SYN_SEC : isMisapplied ? ERR_SEC : REL_SEC);
            builder.addAll(relSecTags);
        }

        return builder.getTaggedText();
    }

    private List<TaggedText> getSensuTags(Reference ref, String detail, /*boolean isSensu,*/ boolean isRelation) {
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
            TagEnum secType = /*isSensu? TagEnum.sensuReference : */ isRelation? TagEnum.relSecReference : TagEnum.secReference;
            TaggedText refTag = TaggedText.NewInstance(secType, secRef);
            refTag.setEntityReference(new TypedEntityReference<>(CdmBase.deproxy(ref).getClass(), ref.getUuid()));
            result.add(refTag);
        }
        if (isNotBlank(detail)){
            result.add(TaggedText.NewSeparatorInstance(DETAIL_SEPARATOR));
            TagEnum detailType = /*isSensu? TagEnum.sensuMicroReference : */ isRelation? TagEnum.relSecMicroReference :TagEnum.secMicroReference;
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
                String separator = index < n ? TeamDefaultCacheStrategy.STD_TEAM_CONCATINATION : TeamDefaultCacheStrategy.FINAL_TEAM_CONCATINATION;
                result = CdmUtils.concat(separator, result, name);
                index++;
            }
            if (team.isHasMoreMembers()){
                //TODO or et al.???
                result += TeamDefaultCacheStrategy.ET_AL_TEAM_CONCATINATION_FULL + "al.";
            }
            return result;
        }
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
        List<TaggedText> result = name.getCacheStrategy().getTaggedFullTitle(name);
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
