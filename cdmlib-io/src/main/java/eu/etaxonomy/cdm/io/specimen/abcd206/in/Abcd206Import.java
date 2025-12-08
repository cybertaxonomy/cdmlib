/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.DeleteResult;
import eu.etaxonomy.cdm.api.service.config.SpecimenDeleteConfigurator;
import eu.etaxonomy.cdm.common.StreamUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.ext.occurrence.bioCase.BioCaseQueryServiceWrapper;
import eu.etaxonomy.cdm.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportBase;
import eu.etaxonomy.cdm.io.specimen.SpecimenUserInteraction;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.molecular.AbcdDnaParser;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author p.kelbert
 * @author p.plitzner
 * @author k.luther
 * @since 20.10.2008
 */
@Component
public class Abcd206Import extends SpecimenImportBase<Abcd206ImportConfigurator, Abcd206ImportState> {

    private static final long serialVersionUID = 3918095362150986307L;
    private static final Logger logger = LogManager.getLogger();

    private static final UUID SPECIMEN_SCAN_TERM = UUID.fromString("acda15be-c0e2-4ea8-8783-b9b0c4ad7f03");

    public Abcd206Import() {
        super();
    }

    @Override
    // @SuppressWarnings("rawtypes")
    public void doInvoke(Abcd206ImportState state) {

        Abcd206ImportConfigurator config = state.getConfig();
        Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
        Map<String, Person> personStoreCollector = new HashMap<>();
        state.setPersonStoreCollector(personStoreCollector);
        Map<String, Team> teamStoreCollector = new HashMap<>();
        state.setTeamStoreCollector(teamStoreCollector);

        Map<String, Person> personStoreAuthor = new HashMap<>();
        state.setPersonStoreAuthor(personStoreAuthor);
        Map<String, Team> teamStoreAuthor = new HashMap<>();
        state.setTeamStoreAuthor(teamStoreAuthor);
        MapWrapper<Reference> referenceStore = (MapWrapper<Reference>) stores.get(ICdmIO.REFERENCE_STORE);
        createKindOfUnitsMap(state);
        URI sourceUri = config.getSourceUri();
        try {
            state.setTx(startTransaction());
            logger.info("INVOKE Specimen Import from ABCD2.06 XML ");
            InputStream response = null;
            // init cd repository
            if (state.getCdmRepository() == null) {
                state.setCdmRepository(this);
            }
            if (config.getOccurenceQuery() != null) {
                BioCaseQueryServiceWrapper queryService = new BioCaseQueryServiceWrapper();
                try {

                    response = queryService.query(config.getOccurenceQuery(), sourceUri);
                    state.setActualAccessPoint(sourceUri);

                } catch (Exception e) {
                    logger.error("An error during ABCD import");
                }
            }
            SpecimenUserInteraction sui = state.getConfig().getSpecimenUserInteraction();

            if (state.getRef() == null) {
                String name = NB(state.getConfig().getSourceReferenceTitle());
                for (Reference reference : referenceStore.getAllValues()) {
                    if (!StringUtils.isBlank(reference.getTitleCache())) {
                        if (reference.getTitleCache().equalsIgnoreCase(name)) {
                            state.setRef(reference);

                        }
                    }
                }
                if (state.getRef() == null) {
                    if (state.getConfig().getSourceReference() != null) {
                        state.setRef(state.getConfig().getSourceReference());
                        state.addImportReference(state.getRef());

                    } else {
                        state.setRef(ReferenceFactory.newGeneric());
                        state.getRef().setUri(sourceUri);

                        if (sourceUri != null) {
                            state.getRef().setTitle(StringUtils.substringAfter(sourceUri.toString(), "dsa="));
                        }

                        if (!StringUtils.isBlank(state.getConfig().getSourceReferenceTitle())) {
                            state.getRef().setTitle(state.getConfig().getSourceReferenceTitle());
                        }
                        state.addImportReference(state.getRef());
                    }
                }
            }

            save(state.getRef(), state);
            state.getConfig().setSourceReference(state.getRef());

            if (state.getConfig().getClassificationUuid() != null) {
                // load classification from config if it exists
                state.setClassification(getClassificationService().load(state.getConfig().getClassificationUuid()));
            }
            if (state.getClassification() == null) {// no existing classification was
                                                    // set in config
                List<Classification> classificationList = getClassificationService().list(Classification.class, null,
                        null, null, null);
                // get classification via user interaction
                if (state.getConfig().isUseClassification() && state.getConfig().isInteractWithUser()) {
                    Map<String, Classification> classMap = new HashMap<>();
                    for (Classification tree : classificationList) {
                        if (!StringUtils.isBlank(tree.getTitleCache())) {
                            classMap.put(tree.getTitleCache(), tree);
                        }
                    }
                    state.setClassification(sui.askForClassification(classMap));
                    if (state.getClassification() == null) {
                        String cla = sui.createNewClassification();
                        if (classMap.get(cla) != null) {
                            state.setClassification(classMap.get(cla));
                        } else {
                            state.setClassification(
                                    Classification.NewInstance(cla, state.getRef(), Language.DEFAULT()));
                        }
                    }
                    save(state.getClassification(), state);
                }
                // use default classification as the classification to import
                // into
                if (state.getClassification() == null) {
                    String name = NB(state.getConfig().getClassificationName());
                    for (Classification classif : classificationList) {
                        if (classif.getTitleCache() != null && classif.getTitleCache().equalsIgnoreCase(name)) {
                            state.setClassification(classif);
                        }
                    }
                    if (state.getClassification() == null) {
                        state.setClassification(Classification.NewInstance(name, state.getRef(), Language.DEFAULT()));
                        // we do not need a default classification when creating
                        // an empty new one
                        state.setDefaultClassification(state.getClassification());
                        save(state.getDefaultClassification(false), state);
                    }
                    save(state.getClassification(), state);
                }
            }

            if (response == null) {
                response = state.getConfig().getSource();
            }
            UnitAssociationWrapper unitAssociationWrapper = AbcdParseUtility.parseUnitsNodeList(response,
                    state.getReport());
            NodeList unitsList = unitAssociationWrapper.getAssociatedUnits();
            state.setPrefix(unitAssociationWrapper.getPrefix());

            if (unitsList != null) {
                String message = "nb units to insert: " + unitsList.getLength();
                logger.info(message);
                state.getConfig().getProgressMonitor().beginTask("Importing ABCD file", unitsList.getLength() + 3);
                updateProgress(state, message);

                state.setDataHolder(new Abcd206DataHolder());
                state.getDataHolder().reset();

                Abcd206XMLFieldGetter abcdFieldGetter = new Abcd206XMLFieldGetter(state.getDataHolder(),
                        state.getPrefix());
                if (config.getNomenclaturalCode() != null) {
                    state.getDataHolder().setNomenclatureCode(config.getNomenclaturalCode().getKey());
                }
                prepareCollectors(state, unitsList, abcdFieldGetter);

                // save collectors

                     commitTransaction(state.getTx());
                state.setTx(startTransaction());
                if (state.getDefaultClassification(false) != null) {
                    state.setDefaultClassification(
                            getClassificationService().load(state.getDefaultClassification(false).getUuid()));
                }
                if (state.getClassification() != null) {
                    state.setClassification(getClassificationService().load(state.getClassification().getUuid()));
                }
                state.setAssociationRefs(new ArrayList<>());
                state.setDescriptionRefs(new ArrayList<>());
                state.setDerivedUnitSources(new ArrayList<>());
                for (int i = 0; i < unitsList.getLength(); i++) {
                    commitTransaction(state.getTx());
                    state.setTx(startTransaction());

                    if (state.getConfig().getProgressMonitor().isCanceled()) {
                        break;
                    }

                    state.reset();
                    state.getDataHolder().setNomenclatureCode(state.getConfig().getNomenclaturalCode()!= null? state.getConfig().getNomenclaturalCode().getKey() : null);
                    Element item = (Element) unitsList.item(i);
                    Abcd206ImportParser.setUnitPropertiesXML(item, abcdFieldGetter, state);
                    updateProgress(state, "Importing data for unit " + state.getDataHolder().getUnitID() + " (" + i
                            + "/" + unitsList.getLength() + ")");

                    // import unit + field unit data
                    state.setAssociatedUnitIds(state.getDataHolder().getAssociatedUnitIds());

                    this.handleSingleUnit(state, item, true);
                    state.setLastFieldUnit(null);

                }
                commitTransaction(state.getTx());
                state.setTx(startTransaction());
                if (state.getConfig().isDeduplicateReferences()) {
                    getReferenceService().deduplicate(Reference.class, null, null);
                }
                if (state.getConfig().isDeduplicateClassifications()) {
                    getClassificationService().deduplicate(Classification.class, null, null);
                }
            }
            commitTransaction(state.getTx());
        } catch (Exception e) {
            String errorDuringImport = "Exception during import!";
            logger.error(errorDuringImport, e);
            state.getReport().addException(errorDuringImport, e);
        } finally {
            //state.getReport().printReport(state.getConfig().getReportUri());
        }
        if (state.getConfig().isDownloadSequenceData()) {
            for (URI uri : state.getSequenceDataStableIdentifier()) {
                // Files.createDirectories(file.getParent()); // optional, make
                // sure parent dir exists
                try {
                    StreamUtils.downloadFile(uri.toURL(), "temp");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return;
    }

    /**
     *
     */
    private void createKindOfUnitsMap(Abcd206ImportState state) {

        ICdmRepository cdmRepository = state.getConfig().getCdmAppController();
        if (cdmRepository == null) {
            cdmRepository = this;
        }

        List<DefinedTerm> terms = cdmRepository.getTermService().listByTermType(TermType.KindOfUnit, null, 0, null,
                null);
        kindOfUnitsMap = new HashMap<>();
        for (DefinedTerm kindOfUnit : terms) {
            if (kindOfUnit != null && kindOfUnit.getLabel() != null) {
                String kindOfUnitLabel = kindOfUnit.getLabel().toLowerCase();
                kindOfUnitsMap.put(kindOfUnitLabel, kindOfUnit);
            }
        }
    }

    private void getSiblings(Abcd206ImportState state, Object item, DerivedUnitFacade facade) {
        String unitId = facade.getCatalogNumber();
        if (unitId == null) {
            unitId = facade.getAccessionNumber();
        }
        if (unitId == null){
            unitId = facade.getBarcode();
        }

        UnitAssociationParser unitParser = new UnitAssociationParser(state.getPrefix(), state.getReport(),
                state.getCdmRepository());
        UnitAssociationWrapper unitAssociationWrapper = null;
        for (URI accessPoint : state.getAllAccesPoint()) {
            unitAssociationWrapper = unitParser.parseSiblings(unitId, accessPoint);
            if (unitAssociationWrapper != null && unitAssociationWrapper.getAssociatedUnits() != null) {
                break;
            }
        }

        DerivedUnit currentUnit = state.getDerivedUnitBase();
        // DerivationEvent currentDerivedFrom = currentUnit.getDerivedFrom();
        FieldUnit currentFieldUnit = facade.getFieldUnit(false);
        if (unitAssociationWrapper != null) {
            NodeList associatedUnits = unitAssociationWrapper.getAssociatedUnits();
            if (associatedUnits != null) {
                for (int m = 0; m < associatedUnits.getLength(); m++) {
                    if (associatedUnits.item(m) instanceof Element) {
                        state.reset();
                        String associationType = AbcdParseUtility
                                .parseFirstTextContent(((Element) associatedUnits.item(m))
                                        .getElementsByTagName(state.getPrefix() + "AssociationType"));

                        Abcd206ImportParser.setUnitPropertiesXML((Element) associatedUnits.item(m),
                                new Abcd206XMLFieldGetter(state.getDataHolder(), unitAssociationWrapper.getPrefix()),
                                state);
                        // logger.debug("derived unit: " +
                        // state.getDerivedUnitBase().toString() + " associated
                        // unit: " +state.getDataHolder().getKindOfUnit() + ", "
                        // + state.getDataHolder().accessionNumber + ", " +
                        // state.getDataHolder().getRecordBasis() + ", " +
                        // state.getDataHolder().getUnitID());

                        handleSingleUnit(state, associatedUnits.item(m), false);

                        DerivedUnit associatedUnit = state.getDerivedUnitBase();
                        FieldUnit associatedFieldUnit = null;
                        commitTransaction(state.getTx());
                        state.setTx(startTransaction());
                        java.util.Collection<FieldUnit> associatedFieldUnits = null;
                        try {
                            associatedFieldUnits = state.getCdmRepository().getOccurrenceService()
                                    .findFieldUnits(associatedUnit.getUuid(), null);
                        } catch (NullPointerException e) {
                            logger.error("Search for associated field unit creates a NPE" + e.getMessage());
                        }
                        // ignore field unit if associated unit has more than
                        // one
                        if (associatedFieldUnits != null && associatedFieldUnits.size() > 1) {
                            state.getReport()
                                    .addInfoMessage(String.format("%s has more than one field unit.", associatedUnit));
                        } else if (associatedFieldUnits != null && associatedFieldUnits.size() == 1) {
                            associatedFieldUnit = associatedFieldUnits.iterator().next();
                        }
                        // parent-child relation:
                        if (associationType != null && (associationType.contains("individual") || associationType.contains("culture")
                                || associationType.contains("sample") || associationType.contains("isolated"))) {
                            DerivationEvent updatedDerivationEvent = DerivationEvent.NewSimpleInstance(currentUnit,
                                    associatedUnit, DerivationEventType.ACCESSIONING());

                            updatedDerivationEvent.setDescription(associationType);
                            if (associatedFieldUnit != null && associatedFieldUnit != currentFieldUnit && currentFieldUnit != null) {
                                // associatedFieldUnit.removeDerivationEvent(updatedDerivationEvent);
                                // save(associatedFieldUnit, state);
                                // if
                                // (associatedFieldUnit.getDerivationEvents().isEmpty()){
                                SpecimenDeleteConfigurator config = new SpecimenDeleteConfigurator();
                                config.setDeleteChildren(false);
                                DeleteResult result = state.getCdmRepository().getOccurrenceService()
                                        .delete(associatedFieldUnit, config);
                                if (!result.isOk()) {
                                    logger.debug("Deletion of field unit " + associatedFieldUnit.getFieldNumber()
                                            + " not successfull");
                                }
                                state.setFieldUnit(currentFieldUnit);

                                // }

                            }
                            state.getReport().addDerivate(associatedUnit, currentUnit, state.getConfig());
                        }
                        save(associatedUnit, state);

                    }
                }
            }
        }
        state.reset();
        state.setDerivedUnitBase(currentUnit);

    }

    /**
     * Handle a single unit
     *
     * @param state
     * @param item
     */
    @Override
    public void handleSingleUnit(Abcd206ImportState state, Object itemObject) {
        handleSingleUnit(state, itemObject, true);
    }

    @SuppressWarnings("rawtypes")
    public void handleSingleUnit(Abcd206ImportState state, Object itemObject, boolean handleAssociatedUnits) {

        Element item = (Element) itemObject;

        Abcd206ImportConfigurator config = state.getConfig();
        if (logger.isDebugEnabled()) {
            logger.info("handleSingleUnit " + state.getRef());
        }
        try {
            ICdmRepository cdmAppController = state.getConfig().getCdmAppController();
            if (cdmAppController == null) {
                cdmAppController = this;
            }
            // check if unit already exists
            DerivedUnitFacade derivedUnitFacade = null;
            if (state.getConfig().isIgnoreImportOfExistingSpecimen() && state.getDataHolder().getUnitID() != null) {

                SpecimenOrObservationBase<?> existingSpecimen = findExistingSpecimen(
                        state.getDataHolder().getUnitID(), state);
                if (existingSpecimen != null && existingSpecimen.isInstanceOf(DerivedUnit.class)) {
                    DerivedUnit derivedUnit = HibernateProxyHelper.deproxy(existingSpecimen, DerivedUnit.class);
                    state.setDerivedUnitBase(derivedUnit);
                    derivedUnitFacade = DerivedUnitFacade.NewInstance(state.getDerivedUnitBase());
                    if (handleAssociatedUnits) {
                        importAssociatedUnits(state, item, derivedUnitFacade);
                    }
                    if (state.getConfig().getDnaSoure() != null) {
                        importAssociatedDna(state, item, derivedUnitFacade);
                    }

                    state.getReport().addAlreadyExistingSpecimen(SpecimenImportUtility.getUnitID(derivedUnit, config),
                            derivedUnit);

                    return;
                }
            } else {
                logger.error("dataholder does not contain unit id");
            }


            // import DNA unit
            if (state.getDataHolder().getKindOfUnit() != null
                    && state.getDataHolder().getKindOfUnit().equalsIgnoreCase("dna")) {
                AbcdDnaParser dnaParser = new AbcdDnaParser(state.getPrefix(), state.getReport(),
                        state.getCdmRepository());
                Set<CdmBase> entitiesToSave = new HashSet<>();

                DnaSample dnaSample = dnaParser.createDNASampleAndFieldUnit(state);
                NodeList specimenUnitList = item.getElementsByTagName(state.getPrefix()+"SpecimenUnit");
                if(specimenUnitList.item(0)!=null && specimenUnitList.item(0) instanceof Element){
                    dnaParser.parseSpecimenUnit((Element)specimenUnitList.item(0), dnaSample, state, entitiesToSave);
                }
                entitiesToSave.stream().forEach(e->save(e, state));
                dnaParser.parse(item, state, dnaSample, entitiesToSave);
                //dnaSample.addSource(OriginalSourceType.Import, dnaSample.getAccessionNumber(), "", state.getImportReference(state.getActualAccessPoint()), "");
                save(dnaSample, state);
                entitiesToSave.stream().forEach(e->save(e, state));
                // set dna as derived unit to avoid creating an extra specimen
                // for this dna sample (instead just the field unit will be
                // created)
                state.setDerivedUnitBase(dnaSample);
                derivedUnitFacade = DerivedUnitFacade.NewInstance(state.getDerivedUnitBase());
            } else {
                // create facade
                derivedUnitFacade = getFacade(state);
                state.setDerivedUnitBase(derivedUnitFacade.innerDerivedUnit());

            }

            /**
             * GATHERING EVENT
             */

            // look for existing fieldUnit
            FieldUnit fieldUnit = null;
            String fieldNumber = state.getDataHolder().getFieldNumber();
            if (isNotBlank(fieldNumber) && !fieldNumber.equals("0") && !fieldNumber.equals("s.n.")){
                fieldUnit = state.getFieldUnit(fieldNumber);
                if (fieldUnit != null){
                    state.setLastFieldUnit(fieldUnit);
                }
            }else{
                if (isBlank(fieldNumber)|| fieldNumber.equals("0") || fieldNumber.equals("s.n.")){
                    state.getReport().addInfoMessage("Field Unit without field number: " + state.getDataHolder().locality + ", " + state.getDataHolder().gatheringAgentsText);
                }
//                else {
//                    fieldUnit = state.getLastFieldUnit();
//                }

            }
            if (fieldUnit == null){
                if (state.getDerivedUnitBase() != null && state.getLastFieldUnit() != null) {//this should be a DNA sample and the field unit should be the same
                    fieldUnit = state.getLastFieldUnit();
                }else {
                    fieldUnit = FieldUnit.NewInstance();
                    fieldUnit.setFieldNumber(fieldNumber);
                    state.setLastFieldUnit(fieldUnit);
                }
            }

            // gathering event
            UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(cdmAppController.getTermService(),
                    state.getDataHolder().locality, state.getDataHolder().languageIso, state.getDataHolder().longitude,
                    state.getDataHolder().latitude, state.getDataHolder().getGatheringCoordinateErrorMethod(),
                    state.getDataHolder().getGatheringElevationText(), state.getDataHolder().getGatheringElevationMin(),
                    state.getDataHolder().getGatheringElevationMax(), state.getDataHolder().getGatheringElevationUnit(),
                    state.getDataHolder().getGatheringDateText(), state.getDataHolder().getGatheringNotes(),
                    state.getDataHolder().getGatheringMethod(),
                    state.getTransformer().getReferenceSystemByKey(state.getDataHolder().getGatheringSpatialDatum()),
                    state.getConfig());

            unitsGatheringEvent.setGatheringDepth(state.getDataHolder().getGatheringDepthText(),
                    state.getDataHolder().getGatheringDepthMin(), state.getDataHolder().getGatheringDepthMax(),
                    state.getDataHolder().getGatheringDepthUnit());

            if (state.getDataHolder().gatheringAgentsList.isEmpty()) {
                String agentsText = state.getDataHolder().gatheringAgentsText;
                TeamOrPersonBase teamOrPerson = parseAgentString(state, agentsText, true);
                Person person = null;
                if (teamOrPerson != null) {
                    person = state.getPersonStoreCollector().get(teamOrPerson.getCollectorTitleCache());
                }
                Team team = null;
                if (person == null && teamOrPerson != null) {
                    team = state.getTeamStoreCollector().get(teamOrPerson.getCollectorTitleCache());
                }
                if (team == null && person == null && StringUtils.isNotBlank(agentsText)){
//                    teamOrPerson = parseAgentString(agentsText, true);
                    findMatchingAgentAndFillStore(state, teamOrPerson, true);

                }else {
                    if (person != null) {
                        unitsGatheringEvent.setCollector(person, config);
                    }else {
                        unitsGatheringEvent.setCollector(team, config);
                    }
                }
            } else {
                Team tempTeam = Team.NewInstance();
                for (String gatheringAgentString: state.getDataHolder().gatheringAgentsList) {
                    TeamOrPersonBase teamOrPerson = parseAgentString(state, gatheringAgentString, true);
                    if (teamOrPerson instanceof Person) {
                        tempTeam.addTeamMember((Person)teamOrPerson);
                    }else {
                        //state.getResult().addError("element of list seems to be a team: " + gatheringAgentString);
                        tempTeam = (Team)teamOrPerson;
                    }
                }

                Team team = state.getTeamStoreCollector().get(tempTeam.getCollectorTitleCache());
                if (team == null){
                    Person person = state.getPersonStoreCollector().get(tempTeam.getCollectorTitleCache());
                    if (person == null) {
                        TeamOrPersonBase teamOrPerson = parseAgentString(state, state.getDataHolder().gatheringAgentsList.toString(), true);
                        findMatchingAgentAndFillStore(state, teamOrPerson, true);
                        unitsGatheringEvent.setCollector(teamOrPerson, config);
                    }

                }
                if (team != null){
                    unitsGatheringEvent.setCollector(team, config);
                }
            }
            saveTeamOrPersons(state, true);

            // count
            UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea();

            unitsGatheringArea.setParams(state.getDataHolder().isocountry, state.getDataHolder().country,
                    (state.getConfig()), cdmAppController.getTermService(),
                    cdmAppController.getVocabularyService());

            DefinedTermBase<?> areaCountry = unitsGatheringArea.getCountry();

            // other areas
            unitsGatheringArea = new UnitsGatheringArea();
            // unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(),getTermService());

            unitsGatheringArea.setAreas(state.getDataHolder().getNamedAreaList(), (state.getConfig()),
                    cdmAppController.getTermService(), cdmAppController.getVocabularyService());

            ArrayList<DefinedTermBase> nas = unitsGatheringArea.getAreas();
            for (DefinedTermBase namedArea : nas) {
                unitsGatheringEvent.addArea(namedArea);
            }

            // copy gathering event to facade
            GatheringEvent gatheringEvent = unitsGatheringEvent.getGatheringEvent();
            if (fieldUnit != null) {
                derivedUnitFacade.setFieldUnit(fieldUnit);
                if (derivedUnitFacade.getGatheringPeriod() == null && gatheringEvent.getTimeperiod() != null){
                    derivedUnitFacade.setGatheringPeriod(gatheringEvent.getTimeperiod());
                }
                if (derivedUnitFacade.getLocality() == null && gatheringEvent.getLocality() != null){
                    derivedUnitFacade.setLocality(gatheringEvent.getLocality());
                }
                if (derivedUnitFacade.getExactLocation() == null && gatheringEvent.getExactLocation() != null){
                    derivedUnitFacade.setExactLocation(gatheringEvent.getExactLocation());
                }

                if (derivedUnitFacade.getCollector() == null && gatheringEvent.getCollector() != null){
                    derivedUnitFacade.setCollector(gatheringEvent.getCollector());
                }
                if (derivedUnitFacade.getCountry() == null && areaCountry != null){
                    derivedUnitFacade.setCountry((NamedArea) areaCountry);
                }
                if (StringUtils.isBlank(derivedUnitFacade.getAbsoluteElevationText()) && StringUtils.isNotBlank(gatheringEvent.getAbsoluteElevationText())){
                    derivedUnitFacade.setAbsoluteElevationText(gatheringEvent.getAbsoluteElevationText());
                }
                if (derivedUnitFacade.getAbsoluteElevation() == null && gatheringEvent.getAbsoluteElevation() != null){
                    derivedUnitFacade.setAbsoluteElevation(gatheringEvent.getAbsoluteElevation());
                }
                if (derivedUnitFacade.getAbsoluteElevationMaximum() == null && gatheringEvent.getAbsoluteElevationMax() != null){
                    derivedUnitFacade.setAbsoluteElevationMax(gatheringEvent.getAbsoluteElevationMax());
                }
                if (StringUtils.isBlank(derivedUnitFacade.getDistanceToGroundText()) && StringUtils.isNotBlank(gatheringEvent.getDistanceToGroundText())){
                    derivedUnitFacade.setDistanceToGroundText(gatheringEvent.getDistanceToGroundText());
                }
                if (derivedUnitFacade.getDistanceToGroundMax() == null && gatheringEvent.getDistanceToGroundMax() != null){
                    derivedUnitFacade.setDistanceToGroundMax(gatheringEvent.getDistanceToGroundMax());
                }
                if (derivedUnitFacade.getDistanceToGround() == null && gatheringEvent.getDistanceToGround() != null){
                    derivedUnitFacade.setDistanceToGround(gatheringEvent.getDistanceToGround());
                }
                if (StringUtils.isBlank(derivedUnitFacade.getDistanceToWaterSurfaceText()) && StringUtils.isNotBlank(gatheringEvent.getDistanceToWaterSurfaceText())){
                    derivedUnitFacade.setDistanceToWaterSurfaceText(gatheringEvent.getDistanceToWaterSurfaceText());
                }
                if (derivedUnitFacade.getDistanceToWaterSurfaceMax() == null && gatheringEvent.getDistanceToWaterSurfaceMax() != null){
                    derivedUnitFacade.setDistanceToWaterSurfaceMax(gatheringEvent.getDistanceToWaterSurfaceMax());
                }
                if (derivedUnitFacade.getDistanceToWaterSurface() == null && gatheringEvent.getDistanceToWaterSurface() != null){
                    derivedUnitFacade.setDistanceToWaterSurface(gatheringEvent.getDistanceToWaterSurface());
                }
                if (derivedUnitFacade.getGatheringPeriod() == null && gatheringEvent.getTimeperiod() != null){
                    derivedUnitFacade.setGatheringPeriod(gatheringEvent.getTimeperiod());
                }
                if (derivedUnitFacade.getCollectingMethod() == null && gatheringEvent.getCollectingMethod() != null){
                    derivedUnitFacade.setCollectingMethod(gatheringEvent.getCollectingMethod());
                }
                for (DefinedTermBase<?> area : unitsGatheringArea.getAreas()) {
                    derivedUnitFacade.addCollectingArea((NamedArea) area);
                }
             // add unitNotes
                if (state.getDataHolder().getUnitNotes() != null) {
                    derivedUnitFacade
                            .addAnnotation(Annotation.NewDefaultLanguageInstance(NB(state.getDataHolder().getUnitNotes())));
                }
                if (gatheringEvent.getAnnotations() != null) {
                    for (Annotation annotation : gatheringEvent.getAnnotations()) {
                        derivedUnitFacade.getGatheringEvent(true).addAnnotation(annotation);
                    }
                }

            }else{
//TODO??
            }
            // TODO exsiccatum

            // add fieldNumber
            if (derivedUnitFacade.getFieldUnit(false) != null) {
                fieldNumber = derivedUnitFacade.getFieldUnit(false).getFieldNumber();
                if (fieldNumber == null){
                    derivedUnitFacade.setFieldNumber(NB(state.getDataHolder().getFieldNumber()));
                }
            }

            save(unitsGatheringEvent.getLocality(), state);


            // //add Multimedia URLs
            if (state.getDataHolder().getMultimediaObjects().size() != -1) {
                for (String multimediaObject : state.getDataHolder().getMultimediaObjects().keySet()) {
                    Media media;
                    try {
                        media = extractMedia(state, multimediaObject);
                        if (media == null) {
                            continue;
                        }
                        if (!state.getConfig().isAddMediaAsMediaSpecimen()) {
                            derivedUnitFacade.addDerivedUnitMedia(media);
                        } else {
                            // add media also as specimen scan
                            MediaSpecimen mediaSpecimen = MediaSpecimen
                                    .NewInstance(SpecimenOrObservationType.StillImage);
                            mediaSpecimen.setMediaSpecimen(media);
                            // do it only once!!
                            DefinedTermBase specimenScanTerm = getTermService().load(SPECIMEN_SCAN_TERM);
                            if (specimenScanTerm instanceof DefinedTerm) {
                                mediaSpecimen.setKindOfUnit((DefinedTerm) specimenScanTerm);
                            }
                            DerivationEvent derivationEvent = DerivationEvent
                                    .NewInstance(DerivationEventType.PREPARATION());
                            derivationEvent.addDerivative(mediaSpecimen);
                            derivedUnitFacade.innerDerivedUnit().addDerivationEvent(derivationEvent);
                        }
                    } catch (MalformedURLException e) {
                        logger.warn(e.getMessage());
                        state.getReport().addException("Malformed URL for media object", e);
                    }
                }
            }

            // multimedia for fieldUnit
            if (state.getDataHolder().getGatheringMultimediaObjects().size() != -1) {
                for (String multimediaObject : state.getDataHolder().getGatheringMultimediaObjects().keySet()) {
                    Media media;
                    try {
                        media = extractMedia(state, multimediaObject);
                       if (media == null) {
                            continue;
                        }
                        derivedUnitFacade.addFieldObjectMedia(media);

                    } catch (MalformedURLException e) {
                        logger.error(e.getMessage());
                        state.getReport().addException("Malformed URL for media object", e);
                    }
                }
            }

            if (derivedUnitFacade.getFieldUnit(false) != null) {
                state.setFieldUnit(derivedUnitFacade.getFieldUnit(false));
            }

            // handle collection data
            setCollectionData(state, derivedUnitFacade);

            // Reference stuff
            SpecimenUserInteraction sui = config.getSpecimenUserInteraction();
            Map<String, OriginalSourceBase> sourceMap = new HashMap<>();

            state.getDataHolder().setDocSources(new ArrayList<>());

            IdentifiableSource sour = getIdentifiableSource(state.getImportReference(state.getActualAccessPoint()), null);
            String idInSource = derivedUnitFacade.getAccessionNumber() != null ? derivedUnitFacade.getAccessionNumber()
                    : derivedUnitFacade.getCatalogNumber() != null ? derivedUnitFacade.getCatalogNumber() : derivedUnitFacade.getBarcode();

            sour.setIdInSource(idInSource);
            try {
                if (sour.getCitation() != null) {
                    if (StringUtils.isNotBlank(sour.getCitationMicroReference())) {
                        state.getDataHolder().getDocSources()
                                .add(sour.getCitation().getTitleCache() + "---" + sour.getCitationMicroReference());
                    } else {
                        state.getDataHolder().getDocSources().add(sour.getCitation().getTitleCache());
                    }
                }
            } catch (Exception e) {
                logger.warn("oups");
            }
            save(sour.getCitation(), state);
            derivedUnitFacade.addSource(sour);


            save(state.getDerivedUnitBase(), state);

            Map<UUID, DescriptionBase> deriveedUnitDescriptions = saveDescriptions(state.getDerivedUnitBase().getSpecimenDescriptions(), state);
            if (derivedUnitFacade.getFieldUnit(false) != null) {
                Map<UUID, DescriptionBase> fieldUnitDescriptions = saveDescriptions(derivedUnitFacade.getFieldUnit(false).getSpecimenDescriptions(), state);
            }

            if (logger.isDebugEnabled()) {
                logger.info("saved ABCD specimen ...");
            }

            // handle identifications
            handleIdentifications(state, derivedUnitFacade);

            // associatedUnits
            if (handleAssociatedUnits) {
                importAssociatedUnits(state, item, derivedUnitFacade);
            }
            if (state.getConfig().getDnaSoure() != null ) {
                boolean uriCorrect = true;
                try{
                    state.getConfig().getDnaSoure().toString();
                }catch(Exception e){
                    uriCorrect = false;
                }
                if (uriCorrect){
                    try{
                        importAssociatedDna(state, item, derivedUnitFacade);
                    }catch(Exception e){
                        String message = "Error when importing Dna! " + itemObject.toString();
                        state.getReport().addException(message, e);
                        state.setUnsuccessfull();
                    }
                }
            }

            // siblings/ other children
            if (derivedUnitFacade.getType() != null
                    && (derivedUnitFacade.getType().equals(SpecimenOrObservationType.LivingSpecimen)
                            || derivedUnitFacade.getType().equals(SpecimenOrObservationType.TissueSample)
                            || derivedUnitFacade.getType().equals(SpecimenOrObservationType.OtherSpecimen)
                            || derivedUnitFacade.getType().equals(SpecimenOrObservationType.MaterialSample))
                    && state.getConfig().isGetSiblings()) {
                getSiblings(state, item, derivedUnitFacade);
            }


        } catch (Exception e) {
            String message = "Error when reading record! " + itemObject.toString();
            state.getReport().addException(message, e);
            state.setUnsuccessfull();
            e.printStackTrace();
        }

        return;
    }

    /**
     * @param state
     * @param multimediaObject
     * @return
     * @throws MalformedURLException
     */
    private Media extractMedia(Abcd206ImportState state, String multimediaObject) throws MalformedURLException {
        Media media;
        media = getImageMedia(multimediaObject, READ_MEDIA_DATA);

        Map<String, String> attributes = state.getDataHolder().getMultimediaObjects().get(multimediaObject);
        if (attributes == null) {
            attributes = state.getDataHolder().getGatheringMultimediaObjects().get(multimediaObject);
            if (attributes == null) {
                logger.error(multimediaObject + " does not exist in dataHolder");
                state.getResult().addError(multimediaObject + " does not exist in dataHolder");
                return null;
            }
        }
        if (attributes.containsKey("Context")) {
            LanguageString description = LanguageString.NewInstance(attributes.get("Context"), Language.ENGLISH());
            media.putDescription(description);
        }
        if (attributes.containsKey("Comment")) {
            LanguageString description = LanguageString.NewInstance(attributes.get("Comment"), Language.ENGLISH());
            media.putDescription(description);
        }
        if (attributes.containsKey("Creators")) {
            String creators = attributes.get("Creators");

            if (creators != null) {
                TeamOrPersonBase creator = parseAgentString(state, creators, false);
                creator = findMatchingAgentAndFillStore(state, creator, false);
                saveTeamOrPersons(state, false);
                media.setArtist(creator);

            }

        }
        if (attributes.containsKey("CreateDate")) {
            String createDate = attributes.get("CreateDate");
            if (createDate != null) {
                media.setMediaCreated(TimePeriodParser.parseString(createDate));
            }
        }

        if (attributes.containsKey("License")) {
            String licence = attributes.get("License");

            if (licence != null) {
                Rights right = Rights.NewInstance(licence, Language.ENGLISH(), RightsType.LICENSE());
                media.addRights(right);
            }

        }
        return media;
    }

    @Override
    protected void importAssociatedUnits(Abcd206ImportState state, Object itemObject,
            DerivedUnitFacade derivedUnitFacade) {
        SpecimenDeleteConfigurator deleteConfig = new SpecimenDeleteConfigurator();
        deleteConfig.setDeleteChildren(false);
        Abcd206ImportConfigurator config = state.getConfig();
        // import associated units
        FieldUnit currentFieldUnit = derivedUnitFacade.innerFieldUnit();
        // TODO: push state (think of implementing stack architecture for state
        DerivedUnit currentUnit = state.getDerivedUnitBase();
        DerivationEvent currentDerivedFrom = currentUnit.getDerivedFrom();
        URI currentAccessPoint = state.getActualAccessPoint();
        String currentPrefix = state.getPrefix();
        Element item = null;
        if (itemObject instanceof Element) {
            item = (Element) itemObject;
        }
        NodeList unitAssociationList = null;
        if (item != null) {
            unitAssociationList = item.getElementsByTagName(currentPrefix + "UnitAssociation");
            for (int k = 0; k < unitAssociationList.getLength(); k++) {
                if (unitAssociationList.item(k) instanceof Element) {
                    Element unitAssociation = (Element) unitAssociationList.item(k);
                    UnitAssociationParser unitAssociationParser = new UnitAssociationParser(currentPrefix,
                            state.getReport(), state.getCdmRepository());
                    UnitAssociationWrapper associationWrapper = unitAssociationParser.parse(unitAssociation);
                    if (associationWrapper != null) {
                        state.setActualAccessPoint(associationWrapper.getAccesPoint());
                        NodeList associatedUnits = associationWrapper.getAssociatedUnits();
                        if (associatedUnits != null) {
                            for (int m = 0; m < associatedUnits.getLength(); m++) {
                                if (associatedUnits.item(m) instanceof Element) {
                                    state.reset();
                                    state.setPrefix(associationWrapper.getPrefix());
                                    Abcd206ImportParser.setUnitPropertiesXML((Element) associatedUnits.item(m),
                                            new Abcd206XMLFieldGetter(state.getDataHolder(), state.getPrefix()), state);

                                    handleSingleUnit(state, associatedUnits.item(m), true);

                                    DerivedUnit associatedUnit = state.getDerivedUnitBase();
                                    FieldUnit associatedFieldUnit = null;
                                    java.util.Collection<FieldUnit> associatedFieldUnits = state.getCdmRepository()
                                            .getOccurrenceService().findFieldUnits(associatedUnit.getUuid(), null);
                                    // ignore field unit if associated unit has
                                    // more than one
                                    if (associatedFieldUnits != null && associatedFieldUnits.size() > 1) {
                                        state.getReport().addInfoMessage(
                                                String.format("%s has more than one field unit.", associatedUnit));
                                    } else if (associatedFieldUnits != null && associatedFieldUnits.size() == 1) {
                                        associatedFieldUnit = associatedFieldUnits.iterator().next();
                                    }


                                    if (currentDerivedFrom == null) {
                                        state.getReport()
                                                .addInfoMessage(String.format(
                                                        "No derivation event found for unit %s. Defaulting to ACCESSIONING event.",
                                                        SpecimenImportUtility.getUnitID(currentUnit, config)));
                                        DerivationEvent.NewSimpleInstance(associatedUnit, currentUnit,
                                                DerivationEventType.ACCESSIONING());
                                    } else {
                                        DerivationEvent updatedDerivationEvent = DerivationEvent.NewSimpleInstance(
                                                associatedUnit, currentUnit, currentDerivedFrom.getType());
                                        updatedDerivationEvent.setActor(currentDerivedFrom.getActor());
                                        updatedDerivationEvent.setDescription(currentDerivedFrom.getDescription());
                                        updatedDerivationEvent.setInstitution(currentDerivedFrom.getInstitution());
                                        updatedDerivationEvent.setTimeperiod(currentDerivedFrom.getTimeperiod());

                                    }
                                    state.getReport().addDerivate(associatedUnit, currentUnit, config);


                                    // delete current field unit if replaced
                                    if (currentFieldUnit != null && currentDerivedFrom != null
                                            && currentFieldUnit.getDerivationEvents().size() == 1
                                            && currentFieldUnit.getDerivationEvents().contains(currentDerivedFrom) // making
                                                                                                                   // sure
                                                                                                                   // that
                                                                                                                   // the
                                                                                                                   // field
                                                                                                                   // unit
                                            && currentDerivedFrom.getDerivatives().size() == 0
                                            && currentDerivedFrom != currentUnit.getDerivedFrom() // <-
                                                                                                  // derivation
                                                                                                  // has
                                                                                                  // been
                                                                                                  // replaced
                                                                                                  // and
                                                                                                  // can
                                                                                                  // be
                                                                                                  // deleted
                                    ) {
                                        currentFieldUnit.removeDerivationEvent(currentDerivedFrom);
                                        if (associatedFieldUnit != null && currentFieldUnit.getGatheringEvent().getActor() != null){
                                            if (associatedFieldUnit.getGatheringEvent() == null) {
                                                associatedFieldUnit.setGatheringEvent(currentFieldUnit.getGatheringEvent());
                                            }else {
                                                associatedFieldUnit.getGatheringEvent().setActor(currentFieldUnit.getGatheringEvent().getActor());
                                            }
                                        }
                                        if (associatedFieldUnit != null && associatedFieldUnit.getFieldNumber() == null && currentFieldUnit.getFieldNumber() != null) {
                                            associatedFieldUnit.setFieldNumber(currentFieldUnit.getFieldNumber());
                                        }

                                        if (currentFieldUnit.getDerivationEvents().isEmpty()) {
                                            DeleteResult result = state.getCdmRepository().getOccurrenceService()
                                                    .delete(currentFieldUnit, deleteConfig);


                                        } else {

                                            logger.debug("there are still derivation events in fieldUnit "
                                                    + currentFieldUnit.getId());
                                        }

                                    }
                                    state.setLastFieldUnit(associatedFieldUnit);
                                    save(associatedUnit, state);
                                }
                            }
                        }
                    }
                }
            }
        }
        // TODO: pop state
        state.reset();
        state.setDerivedUnitBase(currentUnit);
        state.setActualAccessPoint(currentAccessPoint);
        state.setPrefix(currentPrefix);
    }

    private void importAssociatedDna(Abcd206ImportState state, Object itemObject, DerivedUnitFacade derivedUnitFacade) {
        URI dnaSource = state.getConfig().getDnaSoure();
        String unitId = derivedUnitFacade.getCatalogNumber();
        if (unitId == null) {
            unitId = derivedUnitFacade.getAccessionNumber();
        }
        if (unitId == null) {
            unitId = derivedUnitFacade.getBarcode();
        }

        UnitAssociationParser unitParser = new UnitAssociationParser(state.getPrefix(), state.getReport(),
                state.getCdmRepository());
        UnitAssociationWrapper unitAssociationWrapper = null;

        unitAssociationWrapper = unitParser.parseSiblings(unitId, dnaSource);

        DerivedUnit currentUnit = state.getDerivedUnitBase();
        // DerivationEvent currentDerivedFrom = currentUnit.getDerivedFrom();
        FieldUnit currentFieldUnit = derivedUnitFacade.getFieldUnit(false);
        if (unitAssociationWrapper != null) {
            NodeList associatedUnits = unitAssociationWrapper.getAssociatedUnits();
            if (associatedUnits != null) {
                for (int m = 0; m < associatedUnits.getLength(); m++) {
                    if (associatedUnits.item(m) instanceof Element) {
                        state.reset();
                        String associationType = AbcdParseUtility
                                .parseFirstTextContent(((Element) associatedUnits.item(m))
                                        .getElementsByTagName(unitAssociationWrapper.getPrefix() + "AssociationType"));
                        Abcd206XMLFieldGetter fieldGetter = new Abcd206XMLFieldGetter(state.getDataHolder(), unitAssociationWrapper.getPrefix());
                        Abcd206ImportParser.setUnitPropertiesXML((Element) associatedUnits.item(m),
                                fieldGetter,
                                state);

                        URI lastAccessPoint = state.getActualAccessPoint();
                        state.setActualAccessPoint(dnaSource);
                        String oldPrefix = state.getPrefix();
                        state.setPrefix(unitAssociationWrapper.getPrefix());
                        handleSingleUnit(state, associatedUnits.item(m), false);
                        state.setActualAccessPoint(lastAccessPoint);
                        state.setPrefix(oldPrefix);
                        DerivedUnit associatedUnit = state.getDerivedUnitBase();
                        FieldUnit associatedFieldUnit = null;
                        java.util.Collection<FieldUnit> associatedFieldUnits = state.getCdmRepository()
                                .getOccurrenceService().findFieldUnits(associatedUnit.getUuid(), null);
                        // ignore field unit if associated unit has more than
                        // one
                        if (associatedFieldUnits.size() > 1) {
                            state.getReport()
                                    .addInfoMessage(String.format("%s has more than one field unit.", associatedUnit));
                        } else if (associatedFieldUnits.size() == 1) {
                            associatedFieldUnit = associatedFieldUnits.iterator().next();
                        }
                        // parent-child relation:
                        if (associationType != null && (associationType.contains("individual")
                                || associationType.contains("culture") || associationType.contains("sample")
                                || associationType.contains("isolated"))) {
                            DerivationEvent updatedDerivationEvent = DerivationEvent.NewSimpleInstance(currentUnit,
                                    associatedUnit, DerivationEventType.ACCESSIONING());

                            updatedDerivationEvent.setDescription(associationType);
                            if (associatedFieldUnit != null && !associatedFieldUnit.equals(currentFieldUnit) && currentFieldUnit != null) {
                                associatedFieldUnit.removeDerivationEvent(updatedDerivationEvent);
                                if ((associatedFieldUnit.getGatheringEvent() != null && associatedFieldUnit.getGatheringEvent().getActor() != null) && (currentFieldUnit.getGatheringEvent() != null && currentFieldUnit.getGatheringEvent().getActor() == null)){
                                    currentFieldUnit.getGatheringEvent().setActor(associatedFieldUnit.getGatheringEvent().getActor());
                                }
                                if (associatedFieldUnit.getFieldNumber() != null && currentFieldUnit.getFieldNumber() == null){
                                    currentFieldUnit.setFieldNumber(associatedFieldUnit.getFieldNumber());
                                }



                                SpecimenDeleteConfigurator deleteConfig = new SpecimenDeleteConfigurator();
                                deleteConfig.setDeleteChildren(false);
                                DeleteResult result = state.getCdmRepository().getOccurrenceService()
                                        .delete(associatedFieldUnit, deleteConfig);
                                state.setFieldUnit(currentFieldUnit);

                                // }
                                if (updatedDerivationEvent.getOriginals().isEmpty()) {
                                    // state.getCdmRepository().getOccurrenceService().deleteDerivationEvent(updatedDerivationEvent);
                                }
                            }else{
                                state.setLastFieldUnit(associatedFieldUnit);
                            }
                            state.getReport().addDerivate(associatedUnit, currentUnit, state.getConfig());
                        }
                        save(associatedUnit, state);

                    }
                }
            }
        }
        state.reset();
        state.setDerivedUnitBase(currentUnit);

    }

    /**
     * @param derivedUnitFacade
     * @param sour
     * @return
     */
    private boolean sourceNotLinkedToElement(DerivedUnitFacade derivedUnitFacade, OriginalSourceBase source) {
        Set<IdentifiableSource> linkedSources = derivedUnitFacade.getSources();
        for (IdentifiableSource is : linkedSources) {
            if (is.getCitation() != null && source.getCitation() != null
                    && is.getCitation().getTitleCache().equalsIgnoreCase(source.getCitation().getTitleCache())) {
                String isDetail = is.getCitationMicroReference();
                if ((StringUtils.isBlank(isDetail) && StringUtils.isBlank(source.getCitationMicroReference()))
                        || (isDetail != null && isDetail.equalsIgnoreCase(source.getCitationMicroReference()))) {
                    return false;
                }
            }
        }
        return true;
    }



    /**
     * setCollectionData : store the collection object into the
     * derivedUnitFacade
     *
     * @param state
     */
    protected void setCollectionData(Abcd206ImportState state, DerivedUnitFacade derivedUnitFacade) {

        Abcd206ImportConfigurator config = state.getConfig();
        SpecimenImportUtility.setUnitID(derivedUnitFacade.innerDerivedUnit(), state.getDataHolder().getUnitID(),
                config);
        if (!config.isMapUnitIdToAccessionNumber()) {
            derivedUnitFacade.setAccessionNumber(NB(state.getDataHolder().accessionNumber));
        }


        /*
         * INSTITUTION & COLLECTION
         */
        // manage institution
        Institution institution = this.getInstitution(NB(state.getDataHolder().institutionCode), state);
        // manage collection
        Collection collection = this.getCollection(institution, NB(state.getDataHolder().collectionCode), state);
        // link specimen & collection
        derivedUnitFacade.setCollection(collection);
    }

    /**
     * getFacade : get the DerivedUnitFacade based on the recordBasis
     *
     * @param state
     *
     * @return DerivedUnitFacade
     */
    @Override
    protected DerivedUnitFacade getFacade(Abcd206ImportState state) {
        if (logger.isDebugEnabled()) {
            logger.info("getFacade()");
        }
        SpecimenOrObservationType type = null;
        DefinedTerm kindOfUnit = null;

        // create specimen
        if (NB((state.getDataHolder()).getRecordBasis()) != null) {
            if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("living") > -1) {
                type = SpecimenOrObservationType.LivingSpecimen;
            } else if (state.getDataHolder().getRecordBasis().toLowerCase().startsWith("s")
                    || state.getDataHolder().getRecordBasis().toLowerCase().indexOf("specimen") > -1) {// specimen
                type = SpecimenOrObservationType.PreservedSpecimen;
            } else if (state.getDataHolder().getRecordBasis().toLowerCase().startsWith("o")
                    || state.getDataHolder().getRecordBasis().toLowerCase().indexOf("observation") > -1) {
                if (state.getDataHolder().getRecordBasis().toLowerCase().contains("machine")
                        && state.getDataHolder().getRecordBasis().toLowerCase().contains("observation")) {
                    type = SpecimenOrObservationType.MachineObservation;
                } else if (state.getDataHolder().getRecordBasis().toLowerCase().contains("human")
                        && state.getDataHolder().getRecordBasis().toLowerCase().contains("observation")) {
                    type = SpecimenOrObservationType.HumanObservation;
                } else {
                    type = SpecimenOrObservationType.Observation;
                }
            } else if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("fossil") > -1) {
                type = SpecimenOrObservationType.Fossil;
            } else if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("materialsample") > -1) {
                type = SpecimenOrObservationType.MaterialSample;
            } else if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("sample") > -1) {
                type = SpecimenOrObservationType.TissueSample;
            }

            if (type == null) {
                logger.info("The basis of record does not seem to be known: " + state.getDataHolder().getRecordBasis());
                type = SpecimenOrObservationType.DerivedUnit;
            }
        } else {
            logger.info("The basis of record is null");
            type = SpecimenOrObservationType.DerivedUnit;
        }

        if (NB((state.getDataHolder()).getKindOfUnit()) != null) {
            kindOfUnit = getKindOfUnit(state, null, state.getDataHolder().getKindOfUnit().toLowerCase(), null, null,
                    null);
            if (kindOfUnit == null) {
                if (state.getDataHolder().getKindOfUnit().toLowerCase().indexOf("clone") > -1) {
                    kindOfUnit = getKindOfUnit(state, null, "clone culture", "clone culture", "cc", null);
                } else if (state.getDataHolder().getKindOfUnit().toLowerCase().startsWith("live")) {
                    kindOfUnit = getKindOfUnit(state, null, "live sample", "live sample", "ls", null);
                } else if (state.getDataHolder().getKindOfUnit().toLowerCase().startsWith("microscopic slide")) {
                    kindOfUnit = getKindOfUnit(state, null, "microscopic slide", "microscopic slide", "ms", null);
                }

                if (kindOfUnit == null) {
                    logger.info("The kind of unit does not seem to be known: " + state.getDataHolder().getKindOfUnit());

                }
            }
        } else {
            logger.info("The kind of unit is null");

        }
        DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(type);
        //this only works for field units without 's.n.' as field number, so we have to check the field number,
        //do we need this at all?
        if (state.getDataHolder().getFieldNumber().equals("0") || state.getDataHolder().getFieldNumber().equals("s.n.")) {
            derivedUnitFacade.setFieldUnit(null);
        }else {
            derivedUnitFacade.setFieldUnit(state.getFieldUnit(state.getDataHolder().getFieldNumber()));
        }
        derivedUnitFacade.setDerivedUnitKindOfUnit(kindOfUnit);
        derivedUnitFacade.setPreferredStableUri(state.getDataHolder().getPreferredStableUri());
        // derivedUnitFacade.setDerivedUnitKindOfUnit(kindOfUnit);
        return derivedUnitFacade;
    }

    private void getCollectorsFromXML(Element root, Abcd206XMLFieldGetter abcdFieldGetter, Abcd206ImportState state) {
        NodeList group;

        group = root.getChildNodes();
        for (int i = 0; i < group.getLength(); i++) {
            if (group.item(i).getNodeName().equals(state.getPrefix() + "Identifications")) {
                group = group.item(i).getChildNodes();
                break;
            }
        }

        abcdFieldGetter.getType(root);
        abcdFieldGetter.getGatheringPeople(root);
    }


    /**
     * Load the list of names from the ABCD file and save them
     *
     * @param state
     *            : the current ABCD import state
     * @param unitsList
     *            : the unit list from the ABCD file
     * @param abcdFieldGetter
     *            : the ABCD parser
     */
    private void prepareCollectors(Abcd206ImportState state, NodeList unitsList,
            Abcd206XMLFieldGetter abcdFieldGetter) {



        for (int i = 0; i < unitsList.getLength(); i++) {
            this.getCollectorsFromXML((Element) unitsList.item(i), abcdFieldGetter, state);
            if (!(state.getDataHolder().gatheringAgentsList.isEmpty())) {
                TeamOrPersonBase<?> teamOrPerson = null;
                if (state.getDataHolder().gatheringAgentsList.size() == 1) {

                    teamOrPerson = parseAgentString(state, state.getDataHolder().gatheringAgentsList.get(0), true);
                } else {
                    Team team = Team.NewInstance();
                    for (String collector : state.getDataHolder().gatheringAgentsList) {
                        teamOrPerson = parseAgentString(state, collector, true);
                        if (teamOrPerson instanceof Person) {
                            team.addTeamMember((Person) teamOrPerson);
                        } else {
                            for (Person person : ((Team) teamOrPerson).getTeamMembers()) {
                                team.addTeamMember(person);
                            }
                        }
                    }
                    if (team.getTeamMembers() != null && !team.getTeamMembers().isEmpty()){
                        teamOrPerson = team;
                    }

                }
                findMatchingAgentAndFillStore(state, teamOrPerson, true);
            }
            if (!StringUtils.isBlank(state.getDataHolder().gatheringAgentsText)
                    && state.getDataHolder().gatheringAgentsList.isEmpty()) {
                TeamOrPersonBase<?> teamOrPerson = parseAgentString(state, state.getDataHolder().gatheringAgentsText, true);

                if (!state.getPersonStoreCollector().containsKey(teamOrPerson.getCollectorTitleCache()) && !state.getTeamStoreCollector().containsKey(teamOrPerson.getCollectorTitleCache())) {
                    findMatchingAgentAndFillStore(state, teamOrPerson, true);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Stored author " + state.getDataHolder().gatheringAgentsText);
                    }
                    logger.warn("Not imported author with duplicated aut_id "
                            + state.getDataHolder().gatheringAgentsList.toString());
                }
            }
       }

        saveTeamOrPersons(state, true);

    }

    @Override
    protected boolean doCheck(Abcd206ImportState state) {
        logger.warn("Checking not yet implemented for " + this.getClass().getSimpleName());
        return true;
    }

    @Override
    protected boolean isIgnore(Abcd206ImportState state) {
        return false;
    }

    @Override
    protected DefinedTerm getKindOfUnit(Abcd206ImportState state, UUID uuid, String label, String description,
            String labelAbbrev, TermVocabulary<DefinedTerm> voc) {

        DefinedTerm unit = null;

        if (uuid == null) {
            unit = this.kindOfUnitsMap.get(label.toLowerCase());
        } else {
            unit = state.getKindOfUnit(uuid);

        }
        if (unit == null) {
            unit = (DefinedTerm) getTermService().find(uuid);
            if (unit == null) {
                if (uuid == null) {
                    uuid = UUID.randomUUID();
                }
                unit = DefinedTerm.NewKindOfUnitInstance(description, label, labelAbbrev);
                unit.setUuid(uuid);
                if (voc == null) {
                    boolean isOrdered = false;
                    voc = getVocabulary(state, TermType.KindOfUnit, uuidUserDefinedKindOfUnitVocabulary,
                            "User defined vocabulary for kind-of-units", "User Defined Measurement kind-of-units", null,
                            null, isOrdered, unit);
                }
                voc.addTerm(unit);
                getTermService().save(unit);
            }
            state.putKindOfUnit(unit);
            kindOfUnitsMap.put(unit.getLabel().toLowerCase(), unit);
        }
        return unit;
    }



}
