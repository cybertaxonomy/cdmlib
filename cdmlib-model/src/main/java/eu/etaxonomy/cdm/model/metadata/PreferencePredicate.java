/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.common.IEnumTerm;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @since 2013-09-09
 *
 */
public enum PreferencePredicate  implements IEnumTerm<PreferencePredicate>{
    Test(UUID.fromString("b71214ab-2524-4b5d-8e2b-0581767ac839"), "Test", "Test"),
    //model
    NomenclaturalCode(UUID.fromString("39c5cb91-9370-4803-abf7-fa01e7dbe4e2"), "Nomenclatural code", "model.name.NC"),
	//editor
    TaxonNodeOrder(UUID.fromString("ce06bd8e-4371-4ee5-8f57-cf23930cfd12"), "Taxon node order", "model.taxon.TNO"),
	NameDetailsView(UUID.fromString("3c4ec5f5-feb5-44a8-8533-c3c3484a6869"), "NameDetailsView", "model.editor.NDV"),
	IsRedList(UUID.fromString("aaf79c57-b2a9-48a6-b037-fc1b4a75a6e9"), "isRedList", "model.editor.RL"),
	DeterminationOnlyForFieldUnits(UUID.fromString("91b9224b-6610-4cf1-b3da-d60d6f9d59b1"), "DeterminationOnlyForFieldUnit", "model.editor.DOFU"),
	ShowCollectingAreasInGeneralSection(UUID.fromString("578a1195-64ce-4dfb-9be9-6f2823288678"), "ShowCollectingAreaInGeneralSection", "model.editor.SCAGS"),
	ShowTaxonAssociations(UUID.fromString("849c24f9-b62b-4f70-b0a0-1b02182b3433"), "ShowTaxonAssociations", "model.editor.STA"),
	ShowLifeForm(UUID.fromString("85870e7d-a6a3-4c9b-97d6-eb27e6516860"), "ShowLifeForm", "model.editor.SLF"),
	AvailableDistributionAreaTerms(UUID.fromString("34469acc-9e23-4f95-92d4-1695e02cb5a0"), "Available Distribution Area Terms", "model.editor.distribution.area.terms"),
	DefaultBehaviourForPublishFlag(UUID.fromString("24e636fc-ef3b-4a2c-8c9e-018143e66949"), "Default behaviour for publish flag", "model.taxon.PF"),
	//import
	AbcdImportConfig(UUID.fromString("65380375-d041-458c-8275-c36cdc1f34df"), "AbcdImportConfig", "model.import.ABCD"),
	BioCaseProvider(UUID.fromString("bd22c85c-f4e8-4771-ae7b-5750868762c4"), "BioCaseProvider", "model.import.BP"),
	//vaadin
	AvailableDistributionStatus(UUID.fromString("6721599e-686b-460e-9d57-cfd364f4b626"), "Available Distribution Status", "model.distribution.status.term"),
	AvailableDistributionAreaVocabularies(UUID.fromString("dd1f35d5-dbf3-426b-9ed3-8b5992cb2e27"), "Available Distribution Area Vocabularies", "model.distribution.area.voc"),

	//distribution editor
	DistributionEditorActivated(UUID.fromString("733e9bce-4394-4fae-97d3-1b7dfc48ee3c"), "Distribution Editor Is activated", "editor.distribution.ACT"),
    AreasSortedByIdInVocabulary(UUID.fromString("513d7de9-fec4-432c-b4dd-75f9f6e74ad0"), "Distribution Editor: Areas sorted by ID in Vocabulary", "editor.distribution.sorted"),
    ShowRankInDistributionEditor(UUID.fromString("fb13b4f2-2d82-4fd3-8abe-2b955a695245"), "Distribution Editor: Show Rank", "editor.distribution.SHR"),
    ShowSymbol(UUID.fromString("1ee6b945-0a81-4f05-b867-f9d105882249"), "Distribution Editor: Show Symbol", "editor.distribution.SS"),
    ShowIdInVocabulary(UUID.fromString("7b671bba-1b7f-4cb9-bbac-c914518a4bf8"), "Distribution Editor: Show ID in Vocabulary", "editor.distribution.SIV"),

    //name details view
    SimpleDetailsViewActivated(UUID.fromString("78666fde-9ee5-4c09-923b-be98604aace6"), "Name Details: Activated", "editor.nameDetails.activated"),
    ShowTaxon(UUID.fromString("0853e47a-68a8-49af-94d6-505a0479cb28"), "Name Details: Show Taxon", "editor.nameDetails.taxon"),
    ShowLSID(UUID.fromString("fa26eba7-43ed-4498-83a5-8f306a3ef6b3"), "Name Details: Show LSID", "editor.nameDetails.LSID"),
    ShowNomenclaturalCode(UUID.fromString("b54c7670-a4eb-4d41-b847-2c1a48efdd25"), "Name Details: Show Nomenclatural Code", "editor.nameDetails.NC"),
    ShowNameCache(UUID.fromString("d2e74b5b-90ca-4f5a-8979-d3c065512d63"), "Name Details: Show Namecache", "editor.nameDetails.nameCache"),
    ShowNameEpithets(UUID.fromString("816b7edd-0e1e-4277-9931-f73f1970dfe2"), "Name Details: Show Name Epithets", "editor.nameDetails.NE"),
    ShowAppendedPhrase(UUID.fromString("ca0b3d02-09f0-458f-af88-9c9d281f029d"), "Name Details: Show Appended Phrase", "editor.nameDetails.AP"),
    ShowRankInNameDetails(UUID.fromString("b1181426-e87a-4cd2-80bb-40224182efda"), "Name Details: Show Rank", "editor.nameDetails.rank"),
    ShowAuthorship(UUID.fromString("ce6e1a79-f0c3-4542-a93c-2dbd0023220e"), "Name Details: Show Authorship", "editor.nameDetails.AS"),
    ShowAuthorshipCache(UUID.fromString("dbc95c3c-b984-406c-b060-67bde64bb968"), "Name Details: Show AuthorshipCache", "editor.nameDetails.ASC"),
    ShowNomenclaturalReference(UUID.fromString("5b318525-8830-4dbd-8f31-df93bbc69800"), "Name Details: Show Nomenclatural Reference", "editor.nameDetails.NR"),
    ShowNomenclaturalStatus(UUID.fromString("075eba36-90fc-42a2-8e2d-98a61ee88205"), "Name Details: Show Nomenclatural Status", "editor.nameDetails.NS"),
    ShowProtologue(UUID.fromString("fd1105cf-5c98-4325-9db3-117907dac448"), "Name Details: Show Protologue", "editor.nameDetails.SP"),
    ShowTypeDesignation(UUID.fromString("dc79a014-5307-457e-9e92-7353a47a4df6"), "Name Details: Show Typedesignation", "editor.nameDetails.TD"),
    ShowNameRelations(UUID.fromString("7173817e-c15f-4618-8dea-2035b84f7757"), "Name Details: Show Namerelationships", "editor.nameDetails.NRS"),
    ShowHybrid(UUID.fromString("f58a4e82-8162-4d2e-905d-466d40069df8"), "Name Details: Show Hybrid Section", "editor.nameDetails.hybrids"),

    ;




	private PreferencePredicate(UUID uuid, String defaultString, String key){
		this(uuid, defaultString, key, null);
	}

	private PreferencePredicate(UUID uuid, String defaultString, String modelKey, PreferencePredicate parent){

	    delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, modelKey, parent);
	}

	// *************************** DELEGATE **************************************/

	private static EnumeratedTermVoc<PreferencePredicate> delegateVoc;
	private IEnumTerm<PreferencePredicate> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(PreferencePredicate.class);
	}

	@Override
	public String getKey(){return delegateVocTerm.getKey();}

	@Override
    public String getMessage(){return delegateVocTerm.getMessage();}

	@Override
    public String getMessage(Language language){return delegateVocTerm.getMessage(language);}


	@Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

	@Override
    public PreferencePredicate getKindOf() {return delegateVocTerm.getKindOf();}

	@Override
    public Set<PreferencePredicate> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	@Override
	public boolean isKindOf(PreferencePredicate ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	@Override
    public Set<PreferencePredicate> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

	public static PreferencePredicate getByKey(String key){return delegateVoc.getByKey(key);}
    public static PreferencePredicate getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}
}
