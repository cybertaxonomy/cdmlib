// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;



/**
 * The term type is used to define the type of a {@link TermBase term}, may it be a vocabulary or a defined term.<BR>
 * It is used to define in which context a term may be used. From v3.3 on this replaces the semantic part of the subclasses
 * of the class {@link DefinedTermBase}. E.g. before v3.3 a term defining a sex and a term defining a stage had to different
 * classes Sex and Stage. With v3.3 they both became {@link DefinedTerm}s but with different types.<BR>
 * The type of a term and the type of its vocabulary should be the same. Before v3.3 it was not always possible to define
 * what the context of an (empty) vocabulary is.
 * In future versions this enum may also be expressed as a term, not an enum. For now the general policy is to 
 * transform former classes to general classes with enums as their types.
 * 
 * @author a.mueller
 * @created 11.06.2013
 */
@XmlEnum
public enum TermType implements IDefinedTerm<TermType>, Serializable{
	
	//0
	/**
	 * Unknown term type is the type to be used if no information is available about the type.
	 * In the current model this type should never be used. However, it is a placeholder in case
	 * we find an appropriate usage in future.
	 */
	@XmlEnumValue("Unknown")
	Unknown(UUID.fromString("b2836c89-3b1d-4758-ba6d-568ef8d6fbc4"), "Unknown term type"),

	//1
	@XmlEnumValue("Language")
	Language(UUID.fromString("5591dc6c-ad1f-4abd-b6c2-4852ea8e46df"), "Language"),
	
	//2
	@XmlEnumValue("NamedArea")
	NamedArea(UUID.fromString("8c9a0bc9-da91-478d-bc8b-44b11565e160"), "Named area"),

	//3
	@XmlEnumValue("Rank")
	Rank(UUID.fromString("8d26b6a9-8a89-45d5-8358-49c3e4f30ade"), "Rank"),	
	
	//4
	@XmlEnumValue("Feature")
	Feature(UUID.fromString("b866a1d6-f962-4c23-bb8e-a3b66d33aedc"), "Feature"),
	
	//5
	@XmlEnumValue("AnnotationType")
	AnnotationType(UUID.fromString("c3aabb64-6174-4152-95b1-7cec57e485cf"), "Annotation type"),
	
	//6
	@XmlEnumValue("MarkerType")
	MarkerType(UUID.fromString("d28a1bf8-95ed-483a-8f02-3515b14998e0"), "MarkerType"),
	
	//7
	@XmlEnumValue("ExtensionType")
	ExtensionType(UUID.fromString("12f5c03b-528a-4909-b81b-e525feabc97c"), "Extension type"),
	
	//8
	@XmlEnumValue("DerivationEventType")
	DerivationEventType(UUID.fromString("ba8e4b10-c792-42e7-a3f5-874708f10094"), "Derivation event type"),

	//9
	@XmlEnumValue("PresenceAbsenceTerm")
	PresenceAbsenceTerm(UUID.fromString("f6b80f88-c8c5-456b-bbd6-d63ecf35606e"), "Presence or absence term"),

	//10
	@XmlEnumValue("NomenclaturalStatusType")
	NomenclaturalStatusType(UUID.fromString("c1acb71a-1d11-4305-8818-c2268d341742"), "Nomenclatural status type"),

	//11
	@XmlEnumValue("HybridRelationshipType")
	HybridRelationshipType(UUID.fromString("aade9e61-eaa1-40fe-9eb1-40f9e8ae1114"), "Hybrid relationship type"),

	//12
	@XmlEnumValue("NameRelationshipType")
	NameRelationshipType(UUID.fromString("acd8189a-23b9-4a53-8f48-1d2aa270a6ba"), "Name relationship type"),

	//13
	@XmlEnumValue("SynonymRelationshipType")
	SynonymRelationshipType(UUID.fromString("1eb4fee0-7716-4531-a9ed-a95327f1f4bb"), "Synonym relationship type"),
	
	//14
	@XmlEnumValue("TaxonRelationshipType")
	TaxonRelationshipType(UUID.fromString("2d4b281c-142e-42c5-8eb5-1747592b54d8"), "Taxon relationship type"),

	//15
	@XmlEnumValue("NameTypeDesignationStatus")
	NameTypeDesignationStatus(UUID.fromString("d3860be6-8a08-4fff-984a-6ee8b42937c9"), "Name type designation status"),

	//16
	@XmlEnumValue("SpecimenTypeDesignationStatus")
	SpecimenTypeDesignationStatus(UUID.fromString("4014d7d4-f2dd-4328-8015-357a1a77c1ed"), "Specimen type designation status"),

	//17
	@XmlEnumValue("InstitutionType")
	InstitutionType(UUID.fromString("09d78265-18b5-4352-b154-d2f39e84d3f3"), "Institution type"),

	//18
	@XmlEnumValue("NamedAreaType")
	NamedAreaType(UUID.fromString("6a9aba35-6272-4373-8386-000cf95b729e"), "Named area type"),
	
	//19
	@XmlEnumValue("NamedAreaLevel")
	NamedAreaLevel(UUID.fromString("62c16c74-dc79-4970-9031-bb1504be46f5"), "Named area level"),

	//20
	@XmlEnumValue("RightsTerm")
	RightsTerm(UUID.fromString("80e06b04-8d0d-4bd5-bcd6-d35f73c24d55"), "Rights term"),
	
	//21
	@XmlEnumValue("MeasurementUnit")
	MeasurementUnit(UUID.fromString("f9e6c44f-f0d6-428b-9bc0-bb00a6514883"), "Measurement unit"),

	//22
	@XmlEnumValue("StatisticalMeasure")
	StatisticalMeasure(UUID.fromString("a22d19cd-a342-4af2-b156-d688a7aa8a6b"), "Statistical measure"),

	//23
	@XmlEnumValue("PreservationMethod")
	PreservationMethod(UUID.fromString("9cadbee4-22b5-40f5-bb37-e4f5340d246e"), "Preservation method"),

	//24
	@XmlEnumValue("TextFormat")
	TextFormat(UUID.fromString("d26cfdb4-baeb-43d0-a51b-a3428d838790"), "Text format"),
	
	//25
	@XmlEnumValue("Scope")
	Scope(UUID.fromString("8862b66e-9059-4ea4-885e-47a373357075"), "Scope"),

	//26
	/** The stage type represents the restriction (scope) concerning the life stage for
	 * the applicability of {@link TaxonDescription taxon descriptions}. The life stage of a
	 * {@link SpecimenOrObservationBase specimen or observation}
	 * does not belong to a {@link SpecimenDescription specimen description} but is an attribute of
	 * the specimen itself.<BR>
	 * A stage is a specification of Scope.
	 */
	@XmlEnumValue("Stage")
	Stage(UUID.fromString("cf411ef0-8eee-4461-99e9-c03f4f0a1656"), "Stage"),

	//27
	/**
	 * TODO
	 * 
	 * A sex is a specification of Scope.
	 */
	@XmlEnumValue("Sex")
	Sex(UUID.fromString("4046f91f-063b-4b84-b34a-6245c2abc06f"), "Sex"),
	
	//28
	@XmlEnumValue("ReferenceSystem")
	ReferenceSystem(UUID.fromString("b8cfa986-ef90-465e-9609-1dadae2a0f5b"), "Reference system"),

	//29
	@XmlEnumValue("State")
	State(UUID.fromString("5e5b8b60-7300-440a-8706-72fbf31a594f"), "State"),

	//30
	@XmlEnumValue("NaturalLanguageTerm")
	NaturalLanguageTerm(UUID.fromString("9a42ac4e-c175-4633-8b31-74ba8203566a"), "Natural language term"),

	//31
	/**
	 * TODO
	 */
	@XmlEnumValue("Modifier")
	Modifier(UUID.fromString("97c4db67-ccf5-40bf-9fb8-83fb7446a364"), "Modifier"),

	//32
	/**
	 * TODO
	 * 
	 * A determination modifier is a specification of a Modifier.
	 */
	@XmlEnumValue("DeterminationModifier")
	DeterminationModifier(UUID.fromString("ce910516-bc5d-4ac5-be4d-f3c14c27dd85"), "Determination modifier"),
	
	
	;
	
	
	private static final Logger logger = Logger.getLogger(TermType.class);

	private String readableString;
	private UUID uuid;

	private TermType(UUID uuid, String defaultString){
		this.uuid = uuid;
		readableString = defaultString;
	}

	@Transient
	public String getMessage(){
		return getMessage(eu.etaxonomy.cdm.model.common.Language.DEFAULT());
	}
	public String getMessage(Language language){
		//TODO make multi-lingual
		return readableString;
	}
	

	@Override
    public TermType readCsvLine(Class<TermType> termClass,
			List<String> csvLine, java.util.Map<UUID, DefinedTermBase> terms) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
    public void writeCsvLine(CSVWriter writer, TermType term) {
		logger.warn("write csvLine not yet implemented");
	}


	@Override
    public UUID getUuid() {
		return this.uuid;
	}


	@Override
    public TermType getByUuid(UUID uuid) {
		for (TermType type : TermType.values()){
			if (type.getUuid().equals(uuid)){
				return type;
			}
		}
		return null;
	}


	@Override
    public TermType getKindOf() {
		return null;
	}


	@Override
    public Set<TermType> getGeneralizationOf() {
		return new HashSet<TermType>();
	}


	@Override
    public TermType getPartOf() {
		return null;
	}


	@Override
    public Set<TermType> getIncludes() {
		return new HashSet<TermType>();
	}


	@Override
    public Set<Media> getMedia() {
		return new HashSet<Media>();
	}
	
	@Override
	public String getIdInVocabulary() {
		return this.toString();
	}

	@Override
	public void setIdInVocabulary(String idInVocabulary) {
		//not applicable
	}

}
