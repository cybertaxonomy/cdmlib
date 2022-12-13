/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

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
import eu.etaxonomy.cdm.model.taxon.SynonymType;
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
    SynonymType("48917fde-d083-4659-b07d-413db843bd50", SynonymType.class, 1),
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
	IdentifierType("67d91839-484e-4183-8b4c-6a4a80dfc066", DefinedTerm.class, 0),
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

	public static VocabularyEnum getVocabularyEnum(Class<?> clazz){

		for(VocabularyEnum vocabulary : VocabularyEnum.values()){
			if(vocabulary.getClazz().equals(clazz)){
				return vocabulary;
			}
		}

		return null;
	}

    /**
     * @return the orderRelevant
     */
    public boolean isOrderRelevant() {
        return orderRelevant;
    }
}
