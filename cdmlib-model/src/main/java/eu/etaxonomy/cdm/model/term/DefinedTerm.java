/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.Language;


/**
 * @author a.mueller
 * @since 2013-06-19
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DefinedTerm")
@XmlRootElement(name = "DefinedTerm")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.term.DefinedTermBase")
@Audited
public class DefinedTerm
        extends DefinedTermBase<DefinedTerm> {

    private static final long serialVersionUID = -6965540410672076893L;

	//Determination modifier
	public static final UUID uuidConfer = UUID.fromString("20db670a-2db2-49cc-bbdd-eace33694b7f");
	public static final UUID uuidAffinis = UUID.fromString("128f0b54-73e2-4efb-bfda-a6243185a562");

	//Sex
	private static final UUID uuidMale = UUID.fromString("600a5212-cc02-431d-8a80-2bf595bd1eab");
	private static final UUID uuidFemale = UUID.fromString("b4cfe0cb-b35c-4f97-9b6b-2b3c096ea2c0");
	private static final UUID uuidHermaphrodite = UUID.fromString("0deddc65-2505-4c77-91a7-17d0de24afcc");
	private static final UUID uuidUnknown = UUID.fromString("4f5e4c51-a664-48ad-8238-2e9f49eaf8dd");

	//Marker
	private static final UUID uuidIts1 = UUID.fromString("3f2de4f6-d1a2-4c3a-be70-3c997ef92543");

	//kindOfUnit
	public static final UUID uuidSpecimenScan = UUID.fromString("acda15be-c0e2-4ea8-8783-b9b0c4ad7f03");
	public static final UUID uuidDetailImage = UUID.fromString("31eb8d02-bf5d-437c-bcc6-87a626445f34");


	//Identifier Type
	public static final UUID uuidSampleDesignation = UUID.fromString("fadeba12-1be3-4bc7-9ff5-361b088d86fc");
	public static final UUID uuidLsid = UUID.fromString("26729412-9df6-4cc3-9e5d-501531ca21f0");
	public static final UUID uuidAlternativeFieldNumber = UUID.fromString("054fd3d1-1961-42f8-b024-b91184ac9e0c");
    public static final UUID uuidTropicosNameIdentifier = UUID.fromString("6205e531-75b0-4f2a-9a9c-b1247fb080ab");
    public static final UUID uuidIpniNameIdentifier = UUID.fromString("009a602f-0ff6-4231-93db-f458e8229aca");
    public static final UUID uuidWfoNameIdentifier = UUID.fromString("048e0cf9-f59c-42dd-bfeb-3a5cba0191c7");
    //currently only used in Caryophyllales_spp
    public static final UUID uuidPlantListIdentifier = UUID.fromString("06e4c3bd-7bf6-447a-b96e-2844b279f276");


	//TaxonNode Agent Relation Type
	public static final UUID uuidLastScrutiny = UUID.fromString("b4495910-28e9-4a49-86cf-a0476c78f460");
    public static final UUID uuidAuthorOfSubtree = UUID.fromString("c2c7e9b7-d6a7-48a0-b13f-a7a2e2a9b8c9");

    //Occurrence status type
    public static final UUID uuidNotExtant = UUID.fromString("6b4888d8-194a-45c3-a16a-e5b5465a8f3d");
    public static final UUID uuidDestroyed = UUID.fromString("d41cce17-9e68-47f8-904f-fcea1b0029bc");
    public static final UUID uuidLost = UUID.fromString("9d82974f-9535-432d-af5b-f233016abe51");
    public static final UUID uuidNotFound = UUID.fromString("c7df83c2-6e4b-42e8-8fa7-c07720a78e2b");
    public static final UUID uuidNotSeen = UUID.fromString("44f5f025-8814-49dd-b1ef-17557a13ea3e");
    public static final UUID uuidNotPreserved = UUID.fromString("87447824-06d8-4449-abe6-0c11ed77fdc9");
    public static final UUID uuidDriedOut = UUID.fromString("01fe0283-0da6-46a5-a050-22f9be338461");

	protected static Map<UUID, DefinedTerm> termMap = null;

	public static DefinedTerm getTermByUuid(UUID uuid){
	    if (termMap == null || termMap.isEmpty()){
            return getTermByClassAndUUID(DefinedTerm.class, uuid);
        } else{
			return termMap.get(uuid);
		}
	}

	/**
     * Creates a new empty {@link DefinedTerm} instance.
     *
     * @see #NewInstance(String, String, String)
     */
    public static DefinedTerm NewInstance(TermType termType) {
        return new DefinedTerm(termType);
    }

	public static DefinedTerm NewInstance(TermType termType, Set<Representation> representations){
	    DefinedTerm term = null;
	    for (Representation representation : representations) {
            if(term==null){
                term = new DefinedTerm(termType);
            }
            term.addRepresentation(representation);
        }
	    return term;
	}

	public static DefinedTerm NewInstance(TermType termType, String description, String label, String labelAbbrev, Language lang){
		return new DefinedTerm(termType, description, label, labelAbbrev, lang);
	}
	public static DefinedTerm NewInstance(TermType termType, String description, String label, String labelAbbrev){
		return new DefinedTerm(termType, description, label, labelAbbrev, null);
	}


	public static DefinedTerm NewModifierInstance(String description, String label, String labelAbbrev){
		return new DefinedTerm(TermType.Modifier, description, label, labelAbbrev);
	}

	public static DefinedTerm NewInstitutionTypeInstance(String description, String label, String labelAbbrev){
		return new DefinedTerm(TermType.InstitutionType, description, label, labelAbbrev);
	}

	public static DefinedTerm NewStageInstance(String description, String label, String labelAbbrev){
		return new DefinedTerm(TermType.Stage, description, label, labelAbbrev);
	}

	public static DefinedTerm NewSexInstance(String description, String label, String labelAbbrev){
		return new DefinedTerm(TermType.Sex, description, label, labelAbbrev);
	}

	public static DefinedTerm NewScopeInstance(String description, String label, String labelAbbrev){
		return new DefinedTerm(TermType.Scope, description, label, labelAbbrev);
	}

	public static DefinedTerm NewDnaMarkerInstance(String description, String label, String labelAbbrev){
		return new DefinedTerm(TermType.DnaMarker, description, label, labelAbbrev);
	}

	public static DefinedTerm NewKindOfUnitInstance(String description, String label, String labelAbbrev){
		return new DefinedTerm(TermType.KindOfUnit, description, label, labelAbbrev);
	}

	public static DefinedTerm NewIdentifierTypeInstance(String description, String label, String labelAbbrev){
		return new DefinedTerm(TermType.IdentifierType, description, label, labelAbbrev);
	}

    public static DefinedTerm NewTaxonNodeAgentRelationTypeInstance(String description, String label, String labelAbbrev){
        return new DefinedTerm(TermType.TaxonNodeAgentRelationType, description, label, labelAbbrev);
    }

//******************* CONSTRUCTOR ***********************************/

    //for hibernate use only, *packet* private required by bytebuddy
	@Deprecated
	DefinedTerm(){super(TermType.Unknown);}

	protected DefinedTerm(TermType termType){
	    super(termType);
	}

	public DefinedTerm(TermType type, String description, String label, String labelAbbrev, Language lang) {
		super(type, description, label, labelAbbrev, lang);
	}
	public DefinedTerm(TermType type, String description, String label, String labelAbbrev) {
		super(type, description, label, labelAbbrev);
	}

//*************************** TERM MAP *********************/


	public static final DefinedTerm DETERMINATION_MODIFIER_AFFINIS(){
		return getTermByUuid(uuidAffinis);
	}

	public static final DefinedTerm DETERMINATION_MODIFIER_CONFER(){
		return getTermByUuid(uuidConfer);
	}

	public static DefinedTerm SEX_MALE(){
		return getTermByUuid(uuidMale);
	}

	public static DefinedTerm SEX_FEMALE(){
		return getTermByUuid(uuidFemale);
	}

	public static DefinedTerm SEX_HERMAPHRODITE(){
		return getTermByUuid(uuidHermaphrodite);
	}

	public static DefinedTerm SEX_UNKNOWN(){
		return getTermByUuid(uuidUnknown);
	}

	public static DefinedTerm ITS1_MARKER(){
		return getTermByUuid(uuidIts1);
	}

    public static DefinedTerm IDENTIFIER_NAME_TROPICOS(){
        return getTermByUuid(uuidTropicosNameIdentifier);
    }
    public static DefinedTerm IDENTIFIER_NAME_IPNI(){
        return getTermByUuid(uuidIpniNameIdentifier);
    }
    public static DefinedTerm IDENTIFIER_NAME_WFO(){
        return getTermByUuid(uuidWfoNameIdentifier);
    }

    public static DefinedTerm WFO_NAME_IDENTIFIER(){
        return getTermByUuid(uuidWfoNameIdentifier);
    }


	@Override
	public void resetTerms() {
		termMap = null;
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<DefinedTerm> termVocabulary) {
		if (termMap == null){
			termMap = new HashMap<>();
		}
		for (DefinedTerm term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

	@Override
	protected int partOfCsvLineIndex(){
		return 5;
	}

//	@Override
//	void readIsPartOf(DefinedTerm newInstance, List<String> csvLine, Map<UUID, DefinedTermBase> terms) {
//        int index = 7;
//		String partOfString = csvLine.get(index);
//
//        if(StringUtils.isNotBlank(partOfString)) {
//            UUID partOfUuid = UUID.fromString(partOfString);
//            DefinedTerm partOf = (DefinedTerm)terms.get(partOfUuid);
//            partOf.addIncludes(newInstance);
//        }
//
//	}

}
