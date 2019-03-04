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

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * @author a.mueller
 * @since 2013-09-09
 *
 */
public enum PreferencePredicate implements IEnumTerm<PreferencePredicate>, IPreferencePredicate<Object>{
    Test(UUID.fromString("b71214ab-2524-4b5d-8e2b-0581767ac839"), "Test", "Test", "Test"),

    //names
    NomenclaturalCode(UUID.fromString("39c5cb91-9370-4803-abf7-fa01e7dbe4e2"), "Nomenclatural code", "model.name.NC", eu.etaxonomy.cdm.model.name.NomenclaturalCode.ICNAFP),

    //taxonGraph
    TaxonGraphSecRefUuid(UUID.fromString("86ba874c-7491-4f4a-a3b4-aa2d1ea9c411"), "TaxonGraph SecReference Uuid", "model.taxonGraph.secRefUuid", null),

    //taxon
    //TODO needs "taxon" in modelKey as behavior might be different for specimen and future publishable classes
//    DefaultBehaviourForPublishFlag(UUID.fromString("24e636fc-ef3b-4a2c-8c9e-018143e66949"), "Default behaviour for publish flag", "defaultBehaviourForPublishFlag", null),  //default inherit from parent


    //vaadin + distribution editor
	AvailableDistributionStatus(UUID.fromString("6721599e-686b-460e-9d57-cfd364f4b626"), "Available Distribution Status", "distribution.status.term", null),
	AvailableDistributionAreaVocabularies(UUID.fromString("dd1f35d5-dbf3-426b-9ed3-8b5992cb2e27"), "Available Distribution Area Vocabularies", "distribution.area.voc", null),


    //common name area vocabularies

    CommonNameAreaVocabularies(UUID.fromString("59d68062-b4ff-4c3a-b29d-66bf850c1d82"), "Common Names: Available Vocabularies for Areas of Common Names", "commonname.area.voc", null),
    CommonNameReferencesWithMarker(UUID.fromString("41402495-96a8-47be-9129-cf9b2a4bc189"), "Common Names: Use only References with Common Name Marker", "commonname.reference.useMarked", Boolean.FALSE),

    //PreferencePredicates from TaxEditor:

    TaxonNodeOrder(UUID.fromString("ce06bd8e-4371-4ee5-8f57-cf23930cfd12"), "Taxon node order", "model.taxon.TNO", NodeOrderEnum.RankAndNameOrder),  //default alphabet + rank
    NameDetailsView(UUID.fromString("3c4ec5f5-feb5-44a8-8533-c3c3484a6869"), "NameDetailsView", "model.editor.NDV", null),
    DeterminationOnlyForFieldUnits(UUID.fromString("91b9224b-6610-4cf1-b3da-d60d6f9d59b1"), "DeterminationOnlyForFieldUnit", "model.editor.DOFU", Boolean.FALSE),

    //taxeditor UI
    ShowMediaView(UUID.fromString("ba7ba1bb-47e3-4b68-bc44-c5ac45775db4"), "Show media view", "views.showMediaView", Boolean.TRUE),
    ShowTaxonNodeWizard(UUID.fromString("af06fbec-635b-4676-8b60-0b98aefda6aa"), "Show TaxonNode wizard", "views.showTaxonNodeWizard", Boolean.TRUE),
    DisableMultiClassification(UUID.fromString("abdf7e40-afe6-4131-9af3-c6b6779ee6be"), "Disable multi-classification functionality", "menu.disableMultiClassification", Boolean.FALSE),
    ShowChecklistPerspective(UUID.fromString("6058762b-893c-4330-bfe7-45d5717d02b2"), "Show checklist perspective as default", "perspective.checklist", Boolean.FALSE),


    //sources
    ShowIdInSource(UUID.fromString("de291d1b-d89e-42ee-a7b5-15f306f50785"), "Source Details: Show ID in Source", "sourceDetails.IdInSource", Boolean.TRUE),
    //I did rename this "Name"-> "Namespace" (later we should adapt the modelKey similarly
    ShowNamespaceInSource(UUID.fromString("f2e881bb-03a1-4bf9-aff1-7bfd4d355a7c"), "Source Details: Show Namespace in Source", "sourceDetails.NameInSource", Boolean.TRUE),

    //name details view
    SimpleDetailsViewActivated(UUID.fromString("78666fde-9ee5-4c09-923b-be98604aace6"), "Name Details: Activated", "editor.nameDetails.activated", Boolean.FALSE),
    ShowTaxon(UUID.fromString("0853e47a-68a8-49af-94d6-505a0479cb28"), "Name Details: Show Taxon", "editor.nameDetails.taxon", Boolean.TRUE),
    ShowLSID(UUID.fromString("fa26eba7-43ed-4498-83a5-8f306a3ef6b3"), "Name Details: Show LSID", "editor.nameDetails.LSID", Boolean.TRUE),
    ShowNomenclaturalCode(UUID.fromString("b54c7670-a4eb-4d41-b847-2c1a48efdd25"), "Name Details: Show Nomenclatural Code", "editor.nameDetails.NC", Boolean.TRUE),
    ShowNameCache(UUID.fromString("d2e74b5b-90ca-4f5a-8979-d3c065512d63"), "Name Details: Show Namecache", "editor.nameDetails.nameCache", Boolean.TRUE),
    ShowNameEpithets(UUID.fromString("816b7edd-0e1e-4277-9931-f73f1970dfe2"), "Name Details: Show Name Epithets", "editor.nameDetails.NE", Boolean.TRUE),
    ShowAppendedPhrase(UUID.fromString("ca0b3d02-09f0-458f-af88-9c9d281f029d"), "Name Details: Show Appended Phrase", "editor.nameDetails.AP", Boolean.TRUE),
    ShowRankInNameDetails(UUID.fromString("b1181426-e87a-4cd2-80bb-40224182efda"), "Name Details: Show Rank", "editor.nameDetails.rank", Boolean.TRUE),
    ShowAuthorship(UUID.fromString("ce6e1a79-f0c3-4542-a93c-2dbd0023220e"), "Name Details: Show Authorship", "editor.nameDetails.AS", Boolean.TRUE),
    ShowAuthorshipCache(UUID.fromString("dbc95c3c-b984-406c-b060-67bde64bb968"), "Name Details: Show AuthorshipCache", "editor.nameDetails.ASC", Boolean.TRUE),
    ShowNomenclaturalReference(UUID.fromString("5b318525-8830-4dbd-8f31-df93bbc69800"), "Name Details: Show Nomenclatural Reference", "editor.nameDetails.NR", Boolean.TRUE),
    ShowNomenclaturalStatus(UUID.fromString("075eba36-90fc-42a2-8e2d-98a61ee88205"), "Name Details: Show Nomenclatural Status", "editor.nameDetails.NS", Boolean.TRUE),
    ShowProtologue(UUID.fromString("fd1105cf-5c98-4325-9db3-117907dac448"), "Name Details: Show Protologue", "editor.nameDetails.SP", Boolean.TRUE),
    ShowTypeDesignation(UUID.fromString("dc79a014-5307-457e-9e92-7353a47a4df6"), "Name Details: Show Typedesignation", "editor.nameDetails.TD", Boolean.TRUE),
    ShowNameRelations(UUID.fromString("7173817e-c15f-4618-8dea-2035b84f7757"), "Name Details: Show Namerelationships", "editor.nameDetails.NRS", Boolean.TRUE),
    ShowHybrid(UUID.fromString("f58a4e82-8162-4d2e-905d-466d40069df8"), "Name Details: Show Hybrid Section", "editor.nameDetails.hybrids", Boolean.TRUE),

    //distribution editor
    DistributionEditorActivated(UUID.fromString("733e9bce-4394-4fae-97d3-1b7dfc48ee3c"), "Distribution Editor Is activated", "distribution.editor.activated",  Boolean.TRUE),
    AreasSortedInDistributionEditor(UUID.fromString("513d7de9-fec4-432c-b4dd-75f9f6e74ad0"), "Distribution Editor: Areas sorted in Distribution Editor", "distribution.editor.areas.sortedInDistributionEditor", TermOrder.IdInVoc),  //unsure about correct default value, please decide
    //the following 4 should be replaced by termLabelPreferenceEnum , enum should contain label, abbrevLabel, idInVoc, symbol1, symbol2, ...
    DisplayOfStatus(UUID.fromString("1ee6b945-0a81-4f05-b867-f9d105882249"), "Distribution Editor: Display of Status", "distribution.editor.status.display", TermDisplayEnum.Title),
    DisplayOfAreasInDistributionEditor(UUID.fromString("7b671bba-1b7f-4cb9-bbac-c914518a4bf8"), "Distribution Editor: Display of Areas", "distribution.editor.areas.displayAreas",  TermDisplayEnum.Title),
    OwnDescriptionForDistributionEditor(UUID.fromString("38282571-049c-473e-bec3-bde6f65f796a"), "Distribution Editor: Create distribution editor TaxonDescription", "distribution.editor.distributionOwnDescription",  Boolean.FALSE),
    AvailableDistributionAreaTerms(UUID.fromString("34469acc-9e23-4f95-92d4-1695e02cb5a0"), "Available Distribution Area Terms", "distribution.area.terms", null),  //default: all/no filter


    ShowRankInDistributionEditor(UUID.fromString("fb13b4f2-2d82-4fd3-8abe-2b955a695245"), "Distribution Editor: Show Rank", "distribution.editor.showRank", Boolean.FALSE),


    //import
    ShowImportExportMenu(UUID.fromString("92839352-beee-4d66-b078-77fd4f500ab4"), "Show Import Export Menu Items", "io.showMenu", Boolean.TRUE),
    AbcdImportConfig(UUID.fromString("65380375-d041-458c-8275-c36cdc1f34df"), "AbcdImportConfig", "io.ABCD.config", null),  //default defined in TaxEditor
    BioCaseProvider(UUID.fromString("bd22c85c-f4e8-4771-ae7b-5750868762c4"), "BioCaseProvider", "io.ABCD.providerList", null),  //default defined in TaxEditor

    //specimen
    ShowSpecimen(UUID.fromString("a925f874-b953-429f-9db7-f28e3beb576f"), "Show Specimen", "showSpecimen", Boolean.TRUE),
    ShowCollectingAreasInGeneralSection(UUID.fromString("578a1195-64ce-4dfb-9be9-6f2823288678"), "Show Collecting Area in General Section", "specimen.detail.showCollectingAreaInGeneralSection", Boolean.FALSE),
    ShowTaxonAssociations(UUID.fromString("849c24f9-b62b-4f70-b0a0-1b02182b3433"), "Show Taxon Associations", "specimen.detail.showTaxonAssociations", Boolean.FALSE),
    ShowLifeForm(UUID.fromString("85870e7d-a6a3-4c9b-97d6-eb27e6516860"), "Show Life Form", "showLifeForm", Boolean.FALSE),


    DefaultBehaviourForPublishFlag(UUID.fromString("24e636fc-ef3b-4a2c-8c9e-018143e66949"), "Default behaviour for publish flag", "defaultBehaviourForPublishFlag", PublishEnum.InheritFromParent)




    ;

    private Object defaultValue;

	private PreferencePredicate(UUID uuid, String messageString, String key, Object defaultValue){
		this(uuid, messageString, key, null, defaultValue);
	}

	private PreferencePredicate(UUID uuid, String messageString, String modelKey, PreferencePredicate parent, Object defaultValue){
	    delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, messageString, modelKey, parent);
	    this.defaultValue = defaultValue;

	}

	// *************************** DELEGATE **************************************/

	/**
     * @return the delegateVocTerm
     */
    public IEnumTerm<PreferencePredicate> getDelegateVocTerm() {
        return delegateVocTerm;
    }

    /**
     * @param delegateVocTerm the delegateVocTerm to set
     */
    public void setDelegateVocTerm(IEnumTerm<PreferencePredicate> delegateVocTerm) {
        this.delegateVocTerm = delegateVocTerm;
    }

    private static EnumeratedTermVoc<PreferencePredicate> delegateVoc;
	private IEnumTerm<PreferencePredicate> delegateVocTerm;

	static {
		delegateVoc = EnumeratedTermVoc.getVoc(PreferencePredicate.class);
	}


    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

	/**
     * {@inheritDoc}
     */
    @Override
	public String getKey(){return delegateVocTerm.getKey();}

	/**
     * {@inheritDoc}
     */
    @Override
    public String getMessage(){return delegateVocTerm.getMessage();}

	/**
     * {@inheritDoc}
     */
    @Override
    public String getMessage(Language language){return delegateVocTerm.getMessage(language);}


	/**
     * {@inheritDoc}
     */
    @Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

	/**
     * {@inheritDoc}
     */
    @Override
    public PreferencePredicate getKindOf() {return delegateVocTerm.getKindOf();}

	/**
     * {@inheritDoc}
     */
    @Override
    public Set<PreferencePredicate> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

	/**
     * {@inheritDoc}
     */
    @Override
	public boolean isKindOf(PreferencePredicate ancestor) {return delegateVocTerm.isKindOf(ancestor);	}

	/**
     * {@inheritDoc}
     */
    @Override
    public Set<PreferencePredicate> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

	public static IPreferencePredicate getByKey(String key){return delegateVoc.getByKey(key);}
    public static IPreferencePredicate getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

}
