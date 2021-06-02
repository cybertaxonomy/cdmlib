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
 * Enumeration of cdmlib wide {@link PreferencePredicate}s. For the usage of
 * PreferencesPredicates within {@link CdmPreference}s see the {@link CdmPreference
 * CdmPreference documentation}.
 *
 * NOTE: Some of the below predicates do not clearly follow the rule of dividing
 * subject and predicate defined in {@link CdmPreference}. Those holding subject
 * information may be adapted in future.
 * Also the string representation of the predicates does not follow a common rule.
 * This may also be adapted in future.
 *
 * @author a.mueller
 * @since 2013-09-09
 */
public enum PreferencePredicate
        implements IEnumTerm<PreferencePredicate>, IPreferencePredicate<Object>{

    Test(UUID.fromString("b71214ab-2524-4b5d-8e2b-0581767ac839"), "Test", "Test", "Test"),

    //Names
    NomenclaturalCode(UUID.fromString("39c5cb91-9370-4803-abf7-fa01e7dbe4e2"), "Nomenclatural code", "model.name.NC", eu.etaxonomy.cdm.model.name.NomenclaturalCode.ICNAFP),

    //TaxonGraph
    TaxonGraphSecRefUuid(UUID.fromString("86ba874c-7491-4f4a-a3b4-aa2d1ea9c411"), "TaxonGraph SecReference Uuid", "model.taxonGraph.secRefUuid", null),

    //taxon
    //TODO needs "taxon" in modelKey as behavior might be different for specimen and future publishable classes
//    DefaultBehaviourForPublishFlag(UUID.fromString("24e636fc-ef3b-4a2c-8c9e-018143e66949"), "Default behaviour for publish flag", "defaultBehaviourForPublishFlag", null),  //default inherit from parent

    //cdm-vaadin (Distribution UI, Registration UI,  etc)
	AvailableDistributionStatus(UUID.fromString("6721599e-686b-460e-9d57-cfd364f4b626"), "Available Distribution Status", "distribution.status.term", null),
	AvailableDistributionAreaVocabularies(UUID.fromString("dd1f35d5-dbf3-426b-9ed3-8b5992cb2e27"), "Available Distribution Area Vocabularies", "distribution.area.voc", null),

    //Common name area vocabularies
    CommonNameAreaVocabularies(UUID.fromString("59d68062-b4ff-4c3a-b29d-66bf850c1d82"), "Common Names: Available Vocabularies for Areas of Common Names", "commonname.area.voc", null),
    CommonNameReferencesWithMarker(UUID.fromString("41402495-96a8-47be-9129-cf9b2a4bc189"), "Common Names: Use only References with Common Name Marker", "commonname.reference.useMarked", Boolean.FALSE),
    CommonNameLanguages(UUID.fromString("860b0395-09ad-42f5-9dc7-a277ebc034ea"), "Common name: Languages", "commonname.languages", null),

    //PreferencePredicates from TaxEditor:
    TaxonNodeOrder(UUID.fromString("ce06bd8e-4371-4ee5-8f57-cf23930cfd12"), "Taxon node order", "model.taxon.TNO", NodeOrderEnum.RankAndNameOrder),  //default alphabet + rank
    NameDetailsView(UUID.fromString("3c4ec5f5-feb5-44a8-8533-c3c3484a6869"), "NameDetailsView", "model.editor.NDV", null),
    DeterminationOnlyForFieldUnits(UUID.fromString("91b9224b-6610-4cf1-b3da-d60d6f9d59b1"), "DeterminationOnlyForFieldUnit", "model.editor.DOFU", Boolean.FALSE),

    //Taxeditor UI
    ShowMediaView(UUID.fromString("ba7ba1bb-47e3-4b68-bc44-c5ac45775db4"), "Show media view", "views.showMediaView", Boolean.TRUE),
    ShowTaxonNodeWizard(UUID.fromString("af06fbec-635b-4676-8b60-0b98aefda6aa"), "Show TaxonNode wizard", "views.showTaxonNodeWizard", Boolean.TRUE),
    DisableMultiClassification(UUID.fromString("abdf7e40-afe6-4131-9af3-c6b6779ee6be"), "Disable multi-classification functionality", "menu.disableMultiClassification", Boolean.FALSE),
    ShowChecklistPerspective(UUID.fromString("6058762b-893c-4330-bfe7-45d5717d02b2"), "Show checklist perspective as default", "perspective.checklist", Boolean.FALSE),

    //Sources
    ShowIdInSource(UUID.fromString("de291d1b-d89e-42ee-a7b5-15f306f50785"), "Source Details: Show ID in Source", "sourceDetails.IdInSource", Boolean.TRUE),
    //I did rename this "Name"-> "ID Namespace" (later we should adapt the modelKey similarly
    ShowNamespaceInSource(UUID.fromString("f2e881bb-03a1-4bf9-aff1-7bfd4d355a7c"), "Source Details: Show ID Namespace in Source", "sourceDetails.NameInSource", Boolean.TRUE),

    ShowUuidAndIdInSupplementalDataView(UUID.fromString("f0b23cde-b52f-4f36-bbfb-b8ba803ceec1"), "Show Uuid And Id In Supplemental DataView", "supplementalData.UuidAndId", Boolean.FALSE),
    ExtensionTypes(UUID.fromString("412adf43-ad4b-42a9-a4a6-faf96d3e7c02"), "Extension Types", "supplementalData.extension.types", null),
    MarkerTypes(UUID.fromString("2c080f46-aad0-4420-9af1-fe32f3cea73a"), "Marker Types", "supplementalData.marker.types", null),

    //Name details view
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

    //Distribution editor
    DistributionEditorActivated(UUID.fromString("733e9bce-4394-4fae-97d3-1b7dfc48ee3c"), "Distribution Editor Is activated", "distribution.editor.activated",  Boolean.TRUE),
    AreasSortedInDistributionEditor(UUID.fromString("513d7de9-fec4-432c-b4dd-75f9f6e74ad0"), "Distribution Editor: Areas sorted in Distribution Editor", "distribution.editor.areas.sortedInDistributionEditor", TermOrder.IdInVoc),  //unsure about correct default value, please decide
    StatusSortedInDistributionEditor(UUID.fromString("83e9ddb7-e498-4e00-bfdc-69c68f3ad5eb"), "Distribution Editor: Status sorted in Distribution Editor", "distribution.editor.areas.sortedStatusInDistributionEditor", TermOrder.IdInVoc),  //unsure about correct default value, please decide
    //the following 4 should be replaced by termLabelPreferenceEnum , enum should contain label, abbrevLabel, idInVoc, symbol1, symbol2, ...
    DisplayOfStatus(UUID.fromString("1ee6b945-0a81-4f05-b867-f9d105882249"), "Distribution Editor: Display of Status", "distribution.editor.status.display", TermDisplayEnum.Title),
    DisplayOfStatusInCombo(UUID.fromString("a8ff1763-987f-4935-aaba-910f3a870d07"), "Distribution Editor: Display of Status", "distribution.editor.status.combo_display", TermComboEnum.TermDisplayPlusTitle),
    DisplayOfAreasInDistributionEditor(UUID.fromString("7b671bba-1b7f-4cb9-bbac-c914518a4bf8"), "Distribution Editor: Display of Areas", "distribution.editor.areas.displayAreas",  TermDisplayEnum.Title),
    NumberOfVisibleStatusInDropDown(UUID.fromString("5e5ed585-2212-4d07-bbaf-c5e19a61dace"), "Distribution Editor: Number of visible status in drop down", "distribution.editor.status.numberVisibleStatus",  10),
    OwnDescriptionForDistributionEditor(UUID.fromString("38282571-049c-473e-bec3-bde6f65f796a"), "Distribution Editor: Create distribution editor TaxonDescription", "distribution.editor.distributionOwnDescription",  Boolean.FALSE),
    AvailableDistributionAreaTerms(UUID.fromString("34469acc-9e23-4f95-92d4-1695e02cb5a0"), "Available Distribution Area Terms", "distribution.area.terms", null),  //default: all/no filter

    //Character matrix
    CharacterMatrixTableState(UUID.fromString("c2b52b1e-3e39-4890-88ce-3bf0abb49709"), "Character Matrix Table State", "character.matrix.table.state",  null),

    ShowRankInDistributionEditor(UUID.fromString("fb13b4f2-2d82-4fd3-8abe-2b955a695245"), "Distribution Editor: Show Rank", "distribution.editor.showRank", Boolean.FALSE),


    //Import
    ShowImportExportMenu(UUID.fromString("92839352-beee-4d66-b078-77fd4f500ab4"), "Show Import Export Menu Items", "io.showMenu", Boolean.TRUE),
    AbcdImportConfig(UUID.fromString("65380375-d041-458c-8275-c36cdc1f34df"), "AbcdImportConfig", "io.ABCD.config", null),  //default defined in TaxEditor
    BioCaseProvider(UUID.fromString("bd22c85c-f4e8-4771-ae7b-5750868762c4"), "BioCaseProvider", "io.ABCD.providerList", null),  //default defined in TaxEditor

    //Specimen
    ShowSpecimen(UUID.fromString("a925f874-b953-429f-9db7-f28e3beb576f"), "Show Specimen", "showSpecimen", Boolean.TRUE),
    ShowCollectingAreasInGeneralSection(UUID.fromString("578a1195-64ce-4dfb-9be9-6f2823288678"), "Show Collecting Area in General Section", "specimen.detail.showCollectingAreaInGeneralSection", Boolean.FALSE),
    ShowTaxonAssociations(UUID.fromString("849c24f9-b62b-4f70-b0a0-1b02182b3433"), "Show Taxon Associations", "specimen.detail.showTaxonAssociations", Boolean.FALSE),
    ShowLifeForm(UUID.fromString("85870e7d-a6a3-4c9b-97d6-eb27e6516860"), "Show Life Form", "showLifeForm", Boolean.FALSE),
    MeasurementUnit(UUID.fromString("f1d6a2e7-8903-46e0-b1ea-deb74110f83a"), "Measurement Units", "measurementUnits", ""),
    ShowSpecimenListEditor(UUID.fromString("298d0c47-a4dc-4ab1-a3ef-442df8c67bd0"), "Show Specimen List Editor", "specimen.editor.showListEditor", Boolean.TRUE),
    //Taxa
    DefaultBehaviourForPublishFlag(UUID.fromString("24e636fc-ef3b-4a2c-8c9e-018143e66949"), "Default behaviour for publish flag", "defaultBehaviourForPublishFlag", PublishEnum.InheritFromParent),
    DefaultBehaviourForSecundum(UUID.fromString("b62bfebd-2633-46da-828c-5dcc181a137f"), "Default behaviour for secundum reference when moving synonym to accepted taxon", "defaultBehaviourSecMovSyn", SecReferenceHandlingEnum.KeepWhenSame),
    ConceptRelationshipType(UUID.fromString("04e4c69c-6741-4224-81ef-8df0a03bb81e"), "Concept Relationship Types", "conceptRelationshipTypes", null),

    SecundumForSubtreeConfigurator(UUID.fromString("aa43a7de-4098-4b2e-9d76-5caceda70a39"), "Default configurator for set secundum for subtree", "defaultSetSecForSubtreeConfig", null),

    ShowExperimentalFeatures(UUID.fromString("ba617c58-1801-4feb-9819-859aea5869a2"), "Show Experimental Features", "editor.experimentalFeatures", Boolean.TRUE),

    //External Services
    MobotOpenUrlServiceAccessPoint(UUID.fromString("9b956f7c-03c4-47d6-b270-ba14dca8c6ef"), "Mobot Open Url Service Access Point", "externalService.mobotOpenUrlService.accessPoint", "http://www.biodiversitylibrary.org/openurl"),
    MobotOpenUrlServiceMaxHeight(UUID.fromString("cd905b7e-b719-4a0a-b13b-0763b7e279fd"), "Mobot Open Url Service Max Height", "externalService.mobotOpenUrlService.maxHeight", 1000),
    MobotOpenUrlServiceMaxWidth(UUID.fromString("f753f495-776e-41f4-b331-9349658075af"), "Mobot Open Url Service Max Width", "externalService.mobotOpenUrlService.maxWidth", 1000),
    EditMapServiceAccessPoint(UUID.fromString("ec741a66-c7a2-4d7b-b558-54fb2a81327f"), "Edit Map Service Access Point", "externalService.editMapService.accessPoint", "http://edit.africamuseum.be/edit_wp5/v1.2/rest_gen.php"),

    //Facts
    NamedAreaTypes(UUID.fromString("960088e6-c02f-44c4-9fa1-c4552e030ce3"), "Named Area Types", "facts.namedArea.types", null),
    NameFeatures(UUID.fromString("ad0dc9f9-7d14-406d-b0e0-478182fca8f6"), "Name Fetaures", "facts.nameFeatures", null),
    TaxonFeatures(UUID.fromString("ee50e338-4adf-49be-993e-efcbee2c801f"), "Taxon Fetaures", "facts.taxonFeatures", null),
    Stage(UUID.fromString("01ed3f58-c07a-43fd-84e8-7e7cba4e462e"), "Stages", "facts.stages", null),
    ShowModifier(UUID.fromString("a3367065-d580-4d30-8d28-ce06fdc6a033"),"Show Modifiers", "facts.modifier.show", Boolean.TRUE),
    ShowModifierFreeText(UUID.fromString("53d8e5eb-b659-45a1-b093-5f4b285028e3"),"Show Modifier Freetext", "facts.modifier.showFreeText", Boolean.TRUE),
    NamedAreaWithIdInVoc(UUID.fromString("edb0d2df-474b-4fac-b32b-8a0416ed39f8"),"Show Id In Vocabulary in Textfield", "facts.namedArea.showIdInVocabulary", Boolean.FALSE),
    EnableComputedDescription(UUID.fromString("eee33ea4-eb85-41fd-8b9d-787420a16463"),"Disable Computed Descriptions", "facts.computedDescriptions", EnabledComputedDescription.Disabled),


    //Names
    AvailableRanks(UUID.fromString("d6559e49-9d05-43bb-b13a-4a70514100af"), "Available Ranks", "AvailableRanks", null),
    AvailableNomenclaturalStatus(UUID.fromString("fd36dc63-fd98-4d86-9a11-edced6773bd3"), "Available Nomenclatural Status", "name.nomenclaturalStatus", null),
    NameRelationshipType(UUID.fromString("c1830d41-0101-480f-8444-f9c0b07a5f2f"), "Name Relationship Types", "name.nameRelationshipType", null),
    TypeDesignations(UUID.fromString("7b824a7e-49e1-4484-956f-6f6f14628956"), "Type Designations", "name.typeDesignations", null),
    SpecimenTypeDesignationStatus(UUID.fromString("8a155724-2667-42a7-b789-7d826d0cafc9"), "Specimen Type Designation Status", "name.specimenTypeDesignationStatus", null),
    NameTypeDesignationStatus(UUID.fromString("6091668a-9bcf-4c79-8c53-7e4b98de1ccb"), "Name Type Designation Status", "name.nameTypeDesignationStatus", null),
    SortRanksHierarchichally(UUID.fromString("073c34f3-5e20-4dcd-a12b-ca7336f37fb5"), "Sort Ranks Hierarchically", "name.sortRankHierarchically", true),
    //Language
    DefaultLanguage(UUID.fromString("0817d7f3-e88c-4064-be09-bc7adf5c2bd7"), "Default Language", "languages.defaultLanguage", Language.ENGLISH()),
    AvailableLanguages(UUID.fromString("b6851dcb-4138-4a9c-ad56-a3b16f6ee69d"), "Available Languages", "languages.availableLanguages", null),
    MultiLanguageTextEditing(UUID.fromString("373f38ae-981d-492c-a01c-71134034208d"), "Multi Language Text Editing", "languages.multiLanguageTextEditing", Boolean.FALSE),

    //Search
    ShowIdInSelectionDialog(UUID.fromString("2b2dbafe-186e-4411-aca0-74348c0946b7"), "Show Id In Selection Dialog", "search.selectionDialog.showId", Boolean.TRUE),
    SearchForIdentifierAsDefault(UUID.fromString("b059f371-c9ee-4dca-b419-bdf142f14876"), "Search For Identifier As Default", "search.selectionDialog.searchForIdentifierAsDefault", Boolean.FALSE),
    SearchForIdentifierAndTitleCache(UUID.fromString("5b5c2c6d-071a-4b3c-b645-8db5c704b63b"), "Search For Identifier And TitleCache", "search.selectionDialog.searchForIdentifierAndTitleCache", Boolean.TRUE),
    SortTaxaByRankAndName(UUID.fromString("2b7e90b2-1722-4bbf-989a-113281fa0b30"), "Sort Taxa By Rank And Name", "search.selectionDialog.sortTaxaByRankAndName", Boolean.FALSE),
    NamedAreaSearchForAbbrev(UUID.fromString("1db58b79-048b-4981-9cd1-51cb2b915c8a"), "Search Field for Named Areas", "search.selectionDialog.searchField", TermSearchField.NoAbbrev),

    //Media
    AdditionalMediaCopyright(UUID.fromString("c55aa7ca-4a6a-4a40-b4f1-e75030cd38bc"), "Additional media copyright","copyright.additional.media", null),
    MediaRepresentationTransformations(UUID.fromString("7d5151a4-6354-43da-b784-f167da236020"), "Transformations rules for media representations", "media.representationTransformations", null),
    MediaMetadataKeynameIncludes(UUID.fromString("3fe84169-892b-4e80-af69-d23a1f2d7620"), "Positive list of media metadata keywords", "media.metadataKeyword.icludes", String.join(",",
            "title", "artist", "rights", "location", "copyright", "date", "photographer", "locality", "taxon", "collection", "description")),
    MediaMetadataKeynameExcludes(UUID.fromString("f1375a03-259a-4cf3-b360-ac8339249464"), "Negative list of media metadata keywords", "media.metadataKeyword.excludes", null),

    CdmLightExportCondensedDistributionString(UUID.fromString("76e7951e-d213-4121-b0bb-18d4299102e2"), "Create condensed distribution string in CdmLight export", "io.cdmLight.createCondensedString", Boolean.FALSE),
    CondensedDistributionRecipe(UUID.fromString("b24349f6-c152-4dc2-ab7a-667f658f0c5f"), "The recipe for the condensed distribution string", "condensedStringRecipe", null)

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

    @Override
	public String getKey(){return delegateVocTerm.getKey();}

    @Override
    public String getLabel(){return delegateVocTerm.getLabel();}

    @Override
    public String getLabel(Language language){return delegateVocTerm.getLabel(language);}

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

	public static IPreferencePredicate<?> getByKey(String key){return delegateVoc.getByKey(key);}
    public static IPreferencePredicate<?> getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

}
