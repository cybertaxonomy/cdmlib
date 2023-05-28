/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.NaturalLanguageTerm;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author n.hoffmann
 * @since 03.06.2009
 **/
public enum VocabularyEnum {

	Language("45ac7043-7f5e-4f37-92f2-3874aaaef2de", Language.class, 0),
	UndefinedLanguage("7fd1e6d0-2e76-4dfa-bad9-2673dd042c28", Language.class, 0),
	NamedAreaType("e51d52d6-965b-4f7d-900f-4ba9c6f5dd33", NamedAreaType.class, 0),
	NamedAreaLevel("49034253-27c8-4219-97e8-f8d987d3d122", NamedAreaLevel.class, 1),
	Continent("e72cbcb6-58f8-4201-9774-15d0c6abc128", NamedArea.class, 1),
	Country("006b1870-7347-4624-990f-e5ed78484a1a", Country.class, 1),
	Waterbody("35a62b25-f541-4f12-a7c7-17d90dec3e03", NamedArea.class, 1),
	Rank("ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b", Rank.class, 1),
	SpecimenTypeDesignationStatus("ab177bd7-d3c8-4e58-a388-226fff6ba3c2", SpecimenTypeDesignationStatus.class, 1),
	NomenclaturalStatusType("bb28cdca-2f8a-4f11-9c21-517e9ae87f1f", NomenclaturalStatusType.class, 1),
	NomenclaturalStatusTypeIczn("5e3c08e9-13a9-498e-861e-b9b5656ab6ac", NomenclaturalStatusType.class, 1),
	HybridRelationshipType("fc4abe52-9c25-4cfa-a682-8615bf4bbf07", HybridRelationshipType.class, 1),
	NameRelationshipType("6878cb82-c1a4-4613-b012-7e73b413c8cd", NameRelationshipType.class, 1),
	TaxonRelationshipType("15db0cf7-7afc-4a86-a7d4-221c73b0c9ac", TaxonRelationshipType.class, 1),
	TermRelationshipType("4e5bb743-5ddf-4ee2-b893-36fbd386a5ee", TermRelationshipType.class, 1),
    MarkerType("19dffff7-e142-429c-a420-5d28e4ebe305", MarkerType.class, 0),
	AnnotationType("ca04609b-1ba0-4d31-9c2e-aa8eb2f4e62d", AnnotationType.class, 0),
    MeasurementUnit("3b82c375-66bb-4636-be74-dc9cd087292a", MeasurementUnit.class, 0),
	Feature("b187d555-f06f-4d65-9e53-da7c93f8eaa8", Feature.class, 0),
	NameFeature("a7ca3eef-4092-49e1-beec-ed5096193e5e", Feature.class, 0),
	TdwgArea("1fb40504-d1d7-44b0-9731-374fbe6cac77", NamedArea.class, 1),
	PresenceAbsenceTerm("adbbbe15-c4d3-47b7-80a8-c7d104e53a05", PresenceAbsenceTerm.class, 1),
	Sex("9718b7dd-8bc0-4cad-be57-3c54d4d432fe", DefinedTerm.class, 0),
	DerivationEventType("398b50bb-348e-4fe0-a7f5-a75afd846d1f", DerivationEventType.class, 0),
//	PreservationMethod("a7dc20c9-e6b3-459e-8f05-8d6d8fceb465", DefinedTerm.class),
	DeterminationModifier("fe87ea8d-6e0a-4e5d-b0da-0ab8ea67ca77", DefinedTerm.class, 0),
	StatisticalMeasure("066cc62e-7213-495e-a020-97a1233bc037", StatisticalMeasure.class, 0),
	RightsType("8627c526-73af-44d9-902c-11c1f11b60b4", RightsType.class, 0),
	NameTypeDesignationStatus("ab60e738-4d09-4c24-a1b3-9466b01f9f55", NameTypeDesignationStatus.class, 1),
	ExtensionType("117cc307-5bd4-4b10-9b2f-2e14051b3b20",ExtensionType.class, 0),
	ReferenceSystem("ec6376e5-0c9c-4f5c-848b-b288e6c17a86",ReferenceSystem.class, 0),
	InstitutionType("29ad808b-3126-4274-be81-4561e7afc76f", DefinedTerm.class, 0),
	Scope("109bf76d-6f55-43d5-9ec5-6115d9490faa", DefinedTerm.class, 0),
	Stage("4d475bea-c3ae-4494-be16-6796f22fabac", DefinedTerm.class, 0),
	State("ceb65a53-c6cf-42bb-862f-daca7f2fc11d", State.class, 1),
	PresenceState("849d147c-2a43-4a30-a56e-b21c5d23a8a8", State.class, 1),
	TextFormat("ac3926d2-5f6b-45a5-9cf9-be2a5a43f9b3", TextFormat.class, 1),
	NaturalLanguageTerm("fdaba4b0-5c14-11df-a08a-0800200c9a66", NaturalLanguageTerm.class, 0),
	Modifier("b9434774-4931-4f1e-99cb-40a3ecd61777", DefinedTerm.class, 0),
	DnaMarkerEukaryote("d219a463-1cc9-4800-b82f-3146d0afe31e", DefinedTerm.class, 0),
	DnaMarkerChloroplast("e572d370-a27b-40d3-974c-cb17562b59cf", DefinedTerm.class, 0),
	PlantKindOfUnit("5167c47b-7726-4dba-b02f-dafb7629481b", DefinedTerm.class, 0),
	SpecimenKindOfUnit("b0344ec4-12f7-40d3-82c1-0092e9780bbd", DefinedTerm.class, 0),
	MediaSpecimenKindOfUnit("56f47c83-8d42-404a-88fc-03c57b560f6d", DefinedTerm.class, 0),
	IdentifierType("67d91839-484e-4183-8b4c-6a4a80dfc066", IdentifierType.class, 0),
	DnaQualityType("55746f7b-78a8-4e5f-8e70-ee9ce047c835", OrderedTerm.class, 0),
	TaxonNodeAgentRelationType("0aa8e0c6-c7b5-42dd-91b7-0bd273a64b2c", DefinedTerm.class, 0),
	OccurrenceStatusType("6d06b750-4f48-42de-85b5-220256e4e5ba", DefinedTerm.class, 0),
	;

	private UUID uuid;
	private Class<? extends DefinedTermBase<?>> clazz;
	private boolean orderRelevant;

	private VocabularyEnum(String uuidString, Class<? extends DefinedTermBase<?>> clazz, int ordered){
		this.uuid = UUID.fromString(uuidString);
		this.clazz = clazz;
		orderRelevant = (ordered == 1);
	}

	/**
	 * @return the <code>UUID</code> this vocabulary is uniquely identified with
	 */
	public UUID getUuid(){
		return uuid;
	}

	/**
	 * @return the Class of a specific term vocabulary
	 */
	public Class<? extends DefinedTermBase<?>> getClazz(){
		return clazz;
	}

	public static VocabularyEnum getVocabularyEnumByUuid(UUID uuid){

        for(VocabularyEnum vocabulary : VocabularyEnum.values()){
            if(vocabulary.getUuid().equals(uuid)){
                return vocabulary;
            }
        }

        return null;
    }

    public boolean isOrderRelevant() {
        return orderRelevant;
    }

    public static List<UUID> ontologyStructureVocabularyUuids(){
        List<UUID> result = new ArrayList<>();
        result.add(UUID.fromString("cb2e5f49-1cc8-4a6e-a476-db9a22de305d"));  //entire plant
        result.add(UUID.fromString("4c13949e-50f5-461b-83df-21fec53437e8"));  //generic structures
        result.add(UUID.fromString("01b4d3ab-afbe-44f8-8e1c-30a9b7950f62"));  //generative structures
        result.add(UUID.fromString("785c325d-b9f8-4972-a4b5-d5ab3bdaa456"));  //veg. structures
        result.add(UUID.fromString("e48c8fb6-acae-437e-acfa-8186c83216cc"));  //substances
        result.add(UUID.fromString("b9a3562c-0ecc-48a0-9e44-3ab55eff1633"));  //structures in adj. form
        result.add(UUID.fromString("004a0b82-feb1-4c96-8542-26f72c740555"));  //temporal modifier
        result.add(UUID.fromString("70774cb7-2634-4a77-8d80-fe1e76fdb57f"));  //spatial modifier
        result.add(UUID.fromString("ca9803d5-1ed7-47ec-99e1-9ff2054991c1"));  //struc. modifier
        result.add(UUID.fromString("f8f8e819-9ec5-41bd-99da-b0247163d5b1"));  //cond. modifier
        return result;
    }

    public static List<UUID> ontologyPropertyVocabularyUuids(){
        List<UUID> result = new ArrayList<>();
        result.add(UUID.fromString("7e75ab3f-ca7f-428c-bb0d-4c0fd2f6dad0"));   //properties
        return result;
    }

    public static List<UUID> ontologyStateVocabularyUuids(){
        List<UUID> result = new ArrayList<>();
        result.add(UUID.fromString("1bb9d2ed-0791-438a-a92c-03c746609263"));   //architecture
        result.add(UUID.fromString("795e9d8b-12a5-4997-b41c-60c63e2ea7c5"));   //architecture ref. to taxa
        result.add(UUID.fromString("c1af25bb-8eb1-4b63-8d0c-77f7b0a7430d"));   //arrangement
        result.add(UUID.fromString("4a395830-5a57-44bf-8c84-58550edf1545"));   //behavior
        result.add(UUID.fromString("ee3c4b99-a25e-42b9-8cf1-93a5364be477"));   //branching
        result.add(UUID.fromString("1e9b543a-bbc7-4b3a-af1f-e1d669dfc3c3"));   //coaching
        result.add(UUID.fromString("a62479aa-3327-45ce-8cfd-1ba4befbeac6"));   //coloration
        result.add(UUID.fromString("5bdfa0ad-ca81-4772-968a-b4cce5c1bcf7"));   //condition
        result.add(UUID.fromString("7df07c33-82db-4cb0-b5f3-1fca5139357b"));   //course
        result.add(UUID.fromString("0ba29576-ba6b-43f7-8ecd-0c7a1b5d3041"));   //dehiscence
        result.add(UUID.fromString("036b15df-b47d-4763-b64e-bcef5a83f187"));   //density
        result.add(UUID.fromString("7669f2fa-81bb-4e56-bcf2-a2c0b6432a99"));   //depth
        result.add(UUID.fromString("869ffc64-c317-44b6-b712-70bf6875d6dc"));   //derivation
        result.add(UUID.fromString("d08d2e34-2a5d-4dfa-b135-bd62129f1c44"));   //development
        result.add(UUID.fromString("fdd414d7-a8a4-425b-9bc9-e63f68a7525e"));   //duration
        result.add(UUID.fromString("e817377f-c2ac-413b-9afa-d0524ab334fd"));   //ecological adaptations
        result.add(UUID.fromString("5b5c056f-92dd-4f56-9c63-37374092f2e3"));   //fixation
        result.add(UUID.fromString("2e47d48a-0fb1-4279-901e-263784627a1f"));   //fragility
        result.add(UUID.fromString("03d23015-a4e8-4a18-a3a3-4c73cbb9737f"));   //function
        result.add(UUID.fromString("0ef84649-c3c5-4937-bb42-484ce0a199d5"));   //fusion
        result.add(UUID.fromString("31997373-8edf-44e2-b469-7e54a0a1192e"));   //germination
        result.add(UUID.fromString("5a1c2af5-8fda-405b-a12c-8f18b490ec21"));   //growth form
        result.add(UUID.fromString("904741dd-193b-405e-9ae6-e9ccf829d8e4"));   //growth order
        result.add(UUID.fromString("ca386b91-bb91-474f-9cd3-c6e0cc297413"));   //habitat
        result.add(UUID.fromString("945263ba-492e-4fed-80c0-13491ed1956f"));   //height
        result.add(UUID.fromString("3512e1c4-b5db-494f-b7eb-ff0536098849"));   //length
        result.add(UUID.fromString("6c5799a9-1868-4df8-9020-72f3cf197276"));   //life cycle
        result.add(UUID.fromString("c53e08ed-9ea3-4ea3-87c7-ce1384b19c26"));   //location
        result.add(UUID.fromString("97c4a87c-cc43-4e98-95b8-63f28e6e3333"));   //maturatin
        result.add(UUID.fromString("57bed7c3-017d-4d70-b338-04039777ec42"));   //nutrition
        result.add(UUID.fromString("ac928b99-3724-4c74-b7b8-2a13dcb07e0e"));   //odor
        result.add(UUID.fromString("64df01b7-c9c3-4018-92a1-f7f002820163"));   //orientation
        result.add(UUID.fromString("3c17afa1-a7c1-4c98-8eb6-756904ab55da"));   //ploidy
        result.add(UUID.fromString("5fdd551e-fa18-47dc-b73d-4cf4193dc777"));   //position
        result.add(UUID.fromString("919a570f-2ce2-4764-b7ff-44b74d1ec0a8"));   //position relational
        result.add(UUID.fromString("849d147c-2a43-4a30-a56e-b21c5d23a8a8"));   //presence states
        result.add(UUID.fromString("3d8b076b-9bc5-48d6-af02-a14e7b2f6772"));   //prominence
        result.add(UUID.fromString("4a44418a-38cb-4ce1-98ff-a93be647cc2f"));   //pubescence
        result.add(UUID.fromString("96626d35-89ba-46cd-808e-ceb0a1d26e6e"));   //quantity
        result.add(UUID.fromString("e2907dc8-4e92-41d4-af63-44161b6bb5f5"));   //reflectance
        result.add(UUID.fromString("542b1879-d8da-4f2c-9898-6de78dca5fd2"));   //relief
        result.add(UUID.fromString("2aa5945c-9199-46f6-9cdb-746535687232"));   //reproduction
        result.add(UUID.fromString("33922250-e8ae-49dc-a989-ec0415a02608"));   //season
        result.add(UUID.fromString("35ddf096-3b0f-4a94-a7ee-b8671ae1dbc1"));   //shape
        result.add(UUID.fromString("62f637d1-2deb-4d24-97cc-1b857dd6d599"));   //size
        result.add(UUID.fromString("d2d2405d-7564-4db6-95df-d31b7309adb4"));   //taste
        result.add(UUID.fromString("4d2702cb-14fa-4c30-ad0e-1b2852631308"));   //texture
        result.add(UUID.fromString("3962cb0f-1a0e-4de5-a6a9-74539546834b"));   //toxicity
        result.add(UUID.fromString("52fad05d-ff3e-4536-9212-f5f22f815906"));   //variability
        result.add(UUID.fromString("a8446e51-0af0-461a-a88b-f9efb322d692"));   //vernation
        result.add(UUID.fromString("7154207c-64c5-4c55-8b9a-81f0a8fc2a79"));   //width
        return result;
    }

    public static List<UUID> ontologyModifierVocabularyUuids(){
        List<UUID> result = new ArrayList<>();
        result.add(UUID.fromString("7acb6493-0284-4a0b-a159-1ee42f162fb2"));   //certainty
        result.add(UUID.fromString("714e407b-c9fc-4ba4-9bd2-de80f5991651"));   //coverage
        result.add(UUID.fromString("40554224-5b16-42bf-aade-84ea355ee7d5"));   //degree
        result.add(UUID.fromString("2b2c5f2b-1ca8-4a67-9915-7f56aac8d18f"));   //frequency
        return result;
    }

    public static List<UUID> ontologyTreeUuids(){
        List<UUID> result = new ArrayList<>();
        result.add(UUID.fromString("17941710-059e-4e0b-a617-6439d66a39a6"));   //structures
        result.add(UUID.fromString("a4598d3f-0acf-4ad1-a6c9-0c31485da535"));   //properties
        result.add(UUID.fromString("fa9e8602-65b8-4f29-89f3-79132df994ca"));   //states
        return result;
    }
}