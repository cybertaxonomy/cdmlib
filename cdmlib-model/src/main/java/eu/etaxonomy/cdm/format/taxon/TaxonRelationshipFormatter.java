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

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;
import eu.etaxonomy.cdm.strategy.cache.agent.PersonDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;

/**
 * Formatter for TaxonRelationships.
 *
 * @author a.mueller
 * @since 13.08.2018
 */
public class TaxonRelationshipFormatter {

    private static final String DOUBTFUL_TAXON_MARKER = "?" + UTF8.NARROW_NO_BREAK;
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

    private static TaxonRelationshipFormatter instance;

    public static TaxonRelationshipFormatter NewInstance(){
        return new TaxonRelationshipFormatter();
    }

    public static TaxonRelationshipFormatter INSTANCE(){
        if (instance == null){
            instance = NewInstance();
        }
        return instance;
    }

    private TaxonRelationshipFormatter(){

    }

    public List<TaggedText> getTaggedText(TaxonRelationship taxonRelationship, boolean reverse, List<Language> languages) {
        return getTaggedText(taxonRelationship, reverse, languages, false);
    }

    public List<TaggedText> getTaggedText(TaxonRelationship taxonRelationship, boolean reverse,
            List<Language> languages, boolean withoutName) {

        if (taxonRelationship == null){
            return null;
        }

        TaxonRelationshipType type = taxonRelationship.getType();
        boolean isMisapplied = type == null ? false : type.isMisappliedName() && reverse;
        boolean isSynonym = type == null? false : type.isAnySynonym();

        Taxon relatedTaxon = reverse? taxonRelationship.getFromTaxon()
                : taxonRelationship.getToTaxon();

        if (relatedTaxon == null){
            return null;
        }

        String doubtfulTaxonStr = relatedTaxon.isDoubtful() ? DOUBTFUL_TAXON_MARKER : "";
        String doubtfulRelationStr = taxonRelationship.isDoubtful() ? "?" : "";

        TaxonName name = relatedTaxon.getName();

        TaggedTextBuilder builder = new TaggedTextBuilder();

        //rel symbol
        String symbol = doubtfulRelationStr + getSymbol(type, reverse, languages);
        builder.add(TagEnum.symbol, symbol);

        //name
        if (!withoutName){
            if (isMisapplied){
                //starting quote
                String startQuote = " " + doubtfulTaxonStr + QUOTE_START;
                builder.addSeparator(startQuote);

                //name cache
                List<TaggedText> nameCacheTags = getNameCacheTags(name);
                builder.addAll(nameCacheTags);

                //end quote
                String endQuote = QUOTE_END;
                builder.add(TagEnum.postSeparator, endQuote);
            }else{
                builder.addSeparator(" " + doubtfulTaxonStr);
                //name full title cache
                List<TaggedText> nameCacheTags = getNameTitleCacheTags(name);
                builder.addAll(nameCacheTags);
            }
        }else{
            if (isNotBlank(doubtfulTaxonStr)){
                builder.addSeparator(" " + doubtfulTaxonStr);
            }
        }

        //sec/sensu (+ Separatoren?)
        if (isNotBlank(relatedTaxon.getAppendedPhrase())){
            builder.addWhitespace();
            builder.add(TagEnum.appendedPhrase, relatedTaxon.getAppendedPhrase());
        }
        List<TaggedText> secTags = getReferenceTags(relatedTaxon.getSec(), relatedTaxon.getSecMicroReference(),
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
            if (isNotBlank(name.getAuthorshipCache())){
                builder.addSeparator(NON_SEPARATOR);
                builder.add(TagEnum.authors, name.getAuthorshipCache().trim());
            }
        }

        List<TaggedText> relSecTags = getReferenceTags(taxonRelationship.getCitation(),
                taxonRelationship.getCitationMicroReference(),true);
        if (!relSecTags.isEmpty()){
            builder.addSeparator(isSynonym ? SYN_SEC : isMisapplied ? ERR_SEC : REL_SEC);
            builder.addAll(relSecTags);
        }

        return builder.getTaggedText();
    }

    private List<TaggedText> getReferenceTags(Reference ref, String detail, /*boolean isSensu,*/ boolean isRelation) {
        List<TaggedText> result = new ArrayList<>();
        String secRef;

        if (ref != null){
            TeamOrPersonBase<?> author = CdmBase.deproxy(ref.getAuthorship());

            //TODO distinguish linked and unlinked usage,
            // if reference is not linked short citation should only be used
            //   if both author and year exists, also initials should be added in this case
            //
            if (ref.isProtectedTitleCache() == false &&
                    author != null &&
                    isNotBlank(author.getTitleCache())){
                if (author.isInstanceOf(Person.class)){
                    secRef = PersonDefaultCacheStrategy.INSTANCE().getFamilyTitle((Person)author);
                }else{
                    //#9624
                    secRef = TeamDefaultCacheStrategy.INSTANCE_ET_AL_2().getFamilyTitle((Team)author);
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

    private List<TaggedText> getNameCacheTags(TaxonName name) {
        List<TaggedText> result = name.cacheStrategy().getTaggedName(name);
        return result;
    }

    private List<TaggedText> getNameTitleCacheTags(TaxonName name) {

        //TODO full title?
        List<TaggedText> result = name.cacheStrategy().getTaggedFullTitle(name);
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
