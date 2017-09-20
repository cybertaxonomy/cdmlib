/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.ext.occurrence.bioCase.BioCaseQueryServiceWrapper;
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
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
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
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author p.kelbert
 * @author p.plitzner
 * @created 20.10.2008
 */
@Component
public class Abcd206Import extends SpecimenImportBase<Abcd206ImportConfigurator, Abcd206ImportState> {

    private static final long serialVersionUID = 3918095362150986307L;

    private static final UUID SPECIMEN_SCAN_TERM = UUID.fromString("acda15be-c0e2-4ea8-8783-b9b0c4ad7f03");

    private static final Logger logger = Logger.getLogger(Abcd206Import.class);


    public Abcd206Import() {
        super();
    }


    @Override
//    @SuppressWarnings("rawtypes")
    public void doInvoke(Abcd206ImportState state) {
        Abcd206ImportConfigurator config = state.getConfig();
        Map<String, MapWrapper<? extends CdmBase>> stores = state.getStores();
        MapWrapper<TeamOrPersonBase<?>> authorStore = (MapWrapper<TeamOrPersonBase<?>>)stores.get(ICdmIO.TEAM_STORE);
        state.setPersonStore(authorStore);
        MapWrapper<Reference> referenceStore = (MapWrapper<Reference>)stores.get(ICdmIO.REFERENCE_STORE);
        URI sourceUri = config.getSourceUri();
        try{
            state.setTx(startTransaction());
            logger.info("INVOKE Specimen Import from ABCD2.06 XML ");
            InputStream response = null;
            //init cd repository
            if(state.getCdmRepository()==null){
                state.setCdmRepository(this);
            }
            if (config.getOccurenceQuery() != null){
                BioCaseQueryServiceWrapper queryService = new BioCaseQueryServiceWrapper();
                try {

                   response = queryService.query(config.getOccurenceQuery(), sourceUri);
                   state.setActualAccessPoint(sourceUri);

                }catch(Exception e){
                    logger.error("An error during ABCD import");
                }
            }
            SpecimenUserInteraction sui = state.getConfig().getSpecimenUserInteraction();

            //init import reference
        //  List<Reference> references = getReferenceService().list(Reference.class, null, null, null, null);
         //   List<Reference> references = new ArrayList<Reference>();

//            if (state.getConfig().isInteractWithUser()){
//                Map<String,Reference> refMap = new HashMap<String, Reference>();
//                for (Reference reference : references) {
//                    if (! StringUtils.isBlank(reference.getTitleCache())) {
//                        refMap.put(reference.getTitleCache(),reference);
//                    }
//                }
//                state.setRef(sui.askForReference(refMap));
//
//                if (state.getRef() == null){
//                    String cla = sui.createNewReference();
//                    if (refMap.get(cla)!= null) {
//                        state.setRef(refMap.get(cla));
//                    } else {
//                        state.setRef(ReferenceFactory.newGeneric());
//                        state.getRef().setTitle(cla);
//                    }
//                }
//                else{
//                    state.setRef(getReferenceService().find(state.getRef().getUuid()));
//                }
//            }else{
                if (state.getRef()==null){
                    String name = NB(state.getConfig().getSourceReferenceTitle());
                    for (Reference reference : referenceStore.getAllValues()) {
                        if (! StringUtils.isBlank(reference.getTitleCache())) {
                            if (reference.getTitleCache().equalsIgnoreCase(name)) {
                                state.setRef(reference);
                            }
                        }
                    }
                    if (state.getRef() == null){
                        if (state.getConfig().getSourceReference() != null){
                            state.setRef(state.getConfig().getSourceReference());
                        }else{
                            state.setRef(ReferenceFactory.newGeneric());

                            if (state.getConfig().getSourceReferenceTitle() != null){
                                state.getRef().setTitle(state.getConfig().getSourceReferenceTitle());
                            } else{
                                state.getRef().setTitle("ABCD Import Source Reference");
                            }
                        }

                    }
                }
            //}

            save(state.getRef(), state);
            state.getConfig().setSourceReference(state.getRef());

            if(state.getConfig().getClassificationUuid()!=null){
                //load classification from config if it exists
                state.setClassification(getClassificationService().load(state.getConfig().getClassificationUuid()));
            }
            if(state.getClassification()==null){//no existing classification was set in config
                List<Classification> classificationList = getClassificationService().list(Classification.class, null, null, null, null);
                //get classification via user interaction
                if (state.getConfig().isUseClassification() && state.getConfig().isInteractWithUser()){
                    Map<String,Classification> classMap = new HashMap<>();
                    for (Classification tree : classificationList) {
                        if (! StringUtils.isBlank(tree.getTitleCache())) {
                            classMap.put(tree.getTitleCache(),tree);
                        }
                    }
                    state.setClassification(sui.askForClassification(classMap));
                    if (state.getClassification() == null){
                        String cla = sui.createNewClassification();
                        if (classMap.get(cla)!= null) {
                            state.setClassification(classMap.get(cla));
                        } else {
                            state.setClassification(Classification.NewInstance(cla, state.getRef(), Language.DEFAULT()));
                        }
                    }
                    save(state.getClassification(), state);
                }
                // use default classification as the classification to import into
                if (state.getClassification() == null) {
                    String name = NB(state.getConfig().getClassificationName());
                    for (Classification classif : classificationList){
                        if (classif.getTitleCache() != null && classif.getTitleCache().equalsIgnoreCase(name)) {
                            state.setClassification(classif);
                        }
                    }
                    if (state.getClassification() == null){
                        state.setClassification(Classification.NewInstance(name, state.getRef(), Language.DEFAULT()));
                        //we do not need a default classification when creating an empty new one
                        state.setDefaultClassification(state.getClassification());
                        save(state.getDefaultClassification(false), state);
                    }
                    save(state.getClassification(), state);
                }
            }

            if (response == null){
                response =state.getConfig().getSource();
            }
            UnitAssociationWrapper unitAssociationWrapper = AbcdParseUtility.parseUnitsNodeList(response, state.getReport());
            NodeList unitsList = unitAssociationWrapper.getAssociatedUnits();
            state.setPrefix(unitAssociationWrapper.getPrefix());

            if (unitsList != null) {
                String message = "nb units to insert: " + unitsList.getLength();
                logger.info(message);
                state.getConfig().getProgressMonitor().beginTask("Importing ABCD file", unitsList.getLength() + 3);
                updateProgress(state, message);

                state.setDataHolder(new Abcd206DataHolder());
                state.getDataHolder().reset();

                Abcd206XMLFieldGetter abcdFieldGetter = new Abcd206XMLFieldGetter(state.getDataHolder(), state.getPrefix());

                prepareCollectors(state, unitsList, abcdFieldGetter);


                // save authors
                getAgentService().saveOrUpdate((java.util.Collection)state.getPersonStore().objects());

                commitTransaction(state.getTx());
                state.setTx(startTransaction());
                if (state.getDefaultClassification(false) != null){
                    state.setDefaultClassification(getClassificationService().load(state.getDefaultClassification(false).getUuid()));
                }
                if (state.getClassification() != null){
                    state.setClassification(getClassificationService().load(state.getClassification().getUuid()));
                }
                state.setAssociationRefs(new ArrayList<>());
                state.setDescriptionRefs(new ArrayList<>());
                state.setDerivedUnitSources(new ArrayList<>());
                for (int i = 0; i < unitsList.getLength(); i++) {
                    if(state.getConfig().getProgressMonitor().isCanceled()){
                        break;
                    }

                    state.reset();

                    Element item = (Element) unitsList.item(i);
                    Abcd206ImportParser.setUnitPropertiesXML( item, abcdFieldGetter, state);
                    updateProgress(state, "Importing data for unit "+state.getDataHolder().getUnitID()+" ("+i+"/"+unitsList.getLength()+")");

                    //import unit + field unit data
                    state.setAssociatedUnitIds(state.getDataHolder().getAssociatedUnitIds());
                    this.handleSingleUnit(state, item, true);

                }
                if(state.getConfig().isDeduplicateReferences()){
                    getReferenceService().deduplicate(Reference.class, null, null);
                }
                if(state.getConfig().isDeduplicateClassifications()){
                    getClassificationService().deduplicate(Classification.class, null, null);
                }
            }
            commitTransaction(state.getTx());
        }
        catch(Exception e){
            String errorDuringImport = "Exception during import!";
            logger.error(errorDuringImport, e);
            state.getReport().addException(errorDuringImport, e);
        }
        finally{
            state.getReport().printReport(state.getConfig().getReportUri());
        }
        return;
    }


    /**
     * @param state
     * @param item
     */
    private void getSiblings(Abcd206ImportState state, Object item, DerivedUnitFacade facade) {
        String unitId = facade.getCatalogNumber();

        UnitAssociationParser unitParser = new UnitAssociationParser(state.getPrefix(), state.getReport(), state.getCdmRepository());
        UnitAssociationWrapper unitAssociationWrapper = null;
        for (URI accessPoint: state.getActualAccesPoint()){
            unitAssociationWrapper = unitParser.parseSiblings(unitId, accessPoint);
            if (unitAssociationWrapper.getAssociatedUnits() != null){
                break;
            }
        }

       DerivedUnit currentUnit = state.getDerivedUnitBase();
     //  DerivationEvent currentDerivedFrom = currentUnit.getDerivedFrom();
       FieldUnit currentFieldUnit = facade.getFieldUnit(false);
       if(unitAssociationWrapper!=null){
            NodeList associatedUnits = unitAssociationWrapper.getAssociatedUnits();
            if(associatedUnits!=null){
                for(int m=0;m<associatedUnits.getLength();m++){
                    if(associatedUnits.item(m) instanceof Element){
                        state.reset();
                        String associationType = AbcdParseUtility.parseFirstTextContent(((Element) associatedUnits.item(m)).getElementsByTagName(state.getPrefix()+"AssociationType"));

                        Abcd206ImportParser.setUnitPropertiesXML((Element) associatedUnits.item(m), new Abcd206XMLFieldGetter(state.getDataHolder(), unitAssociationWrapper.getPrefix()), state);
                       // logger.debug("derived unit: " + state.getDerivedUnitBase().toString() + " associated unit: " +state.getDataHolder().getKindOfUnit() + ", " + state.getDataHolder().accessionNumber + ", " + state.getDataHolder().getRecordBasis() + ", " + state.getDataHolder().getUnitID());

                        handleSingleUnit(state, associatedUnits.item(m), false);

                        DerivedUnit associatedUnit = state.getDerivedUnitBase();
                        FieldUnit associatedFieldUnit = null;
                        java.util.Collection<FieldUnit> associatedFieldUnits = state.getCdmRepository().getOccurrenceService().getFieldUnits(associatedUnit.getUuid());
                        //ignore field unit if associated unit has more than one
                        if(associatedFieldUnits.size()>1){
                            state.getReport().addInfoMessage(String.format("%s has more than one field unit.", associatedUnit));
                        }
                        else if(associatedFieldUnits.size()==1){
                            associatedFieldUnit = associatedFieldUnits.iterator().next();
                        }
                        //parent-child relation:
                        if(associationType.contains("individual") || associationType.contains("culture") || associationType.contains("sample")){
                            DerivationEvent updatedDerivationEvent = DerivationEvent.NewSimpleInstance(currentUnit, associatedUnit, DerivationEventType.ACCESSIONING());
                            if(associatedFieldUnit!=null && associatedFieldUnit != currentFieldUnit){
                                associatedFieldUnit.removeDerivationEvent(updatedDerivationEvent);
                                state.getCdmRepository().getOccurrenceService().delete(associatedFieldUnit);
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
     * @param state
     * @param item
     */
    @Override
    public void handleSingleUnit(Abcd206ImportState state, Object itemObject){
        handleSingleUnit(state, itemObject, true);
    }


    @SuppressWarnings("rawtypes")
    public void handleSingleUnit(Abcd206ImportState state, Object itemObject, boolean handleAssociatedUnits) {
        Element item = (Element) itemObject;

        Abcd206ImportConfigurator config = state.getConfig();
        if (logger.isDebugEnabled()) {
            logger.info("handleSingleUnit "+state.getRef());
        }
        try {
            ICdmRepository cdmAppController = state.getConfig().getCdmAppController();
            if(cdmAppController==null){
                cdmAppController = this;
            }
            //check if unit already exists
            DerivedUnitFacade derivedUnitFacade = null;
            if(state.getConfig().isIgnoreImportOfExistingSpecimen()){
                SpecimenOrObservationBase<?> existingSpecimen = findExistingSpecimen(state.getDataHolder().getUnitID(), state);
                if(existingSpecimen!=null && existingSpecimen.isInstanceOf(DerivedUnit.class)){
                    DerivedUnit derivedUnit = HibernateProxyHelper.deproxy(existingSpecimen, DerivedUnit.class);
                    state.setDerivedUnitBase(derivedUnit);
                    derivedUnitFacade = DerivedUnitFacade.NewInstance(state.getDerivedUnitBase());
                    if (handleAssociatedUnits){
                        importAssociatedUnits(state, item, derivedUnitFacade);
                    }
                    state.getReport().addAlreadyExistingSpecimen(SpecimenImportUtility.getUnitID(derivedUnit, config), derivedUnit);
                    return;
                }
            }
            // TODO: implement overwrite/merge specimen
//            else if(state.getConfig().isOverwriteExistingSpecimens()){
//                Pager<SpecimenOrObservationBase> existingSpecimens = cdmAppController.getOccurrenceService().findByTitle(config);
//                if(!existingSpecimens.getRecords().isEmpty()){
//                    derivedUnitFacade = DerivedUnitFacade.NewInstance(derivedUnit);
//                    derivedUnitBase = derivedUnitFacade.innerDerivedUnit();
//                    fieldUnit = derivedUnitFacade.getFieldUnit(true);
//                }
//            }
            //import new specimen

            // import DNA unit
            if(state.getDataHolder().getKindOfUnit() !=null && state.getDataHolder().getKindOfUnit().equalsIgnoreCase("dna")){
                AbcdDnaParser dnaParser = new AbcdDnaParser(state.getPrefix(), state.getReport(), state.getCdmRepository());
                DnaSample dnaSample = dnaParser.parse(item, state);
                save(dnaSample, state);
                //set dna as derived unit to avoid creating an extra specimen for this dna sample (instead just the field unit will be created)
                state.setDerivedUnitBase(dnaSample);
                derivedUnitFacade = DerivedUnitFacade.NewInstance(state.getDerivedUnitBase());
            }
            else{
                // create facade
                derivedUnitFacade = getFacade(state);
                state.setDerivedUnitBase(derivedUnitFacade.innerDerivedUnit());

            }

            /**
             * GATHERING EVENT
             */

            //look for existing fieldUnit

            FieldUnit fieldUnit = state.getFieldUnit(state.getDataHolder().getFieldNumber());


                // gathering event
                UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(cdmAppController.getTermService(),
                        state.getDataHolder().locality, state.getDataHolder().languageIso, state.getDataHolder().longitude,
                        state.getDataHolder().latitude, state.getDataHolder().getGatheringElevationText(),
                        state.getDataHolder().getGatheringElevationMin(), state.getDataHolder().getGatheringElevationMax(),
                        state.getDataHolder().getGatheringElevationUnit(), state.getDataHolder().getGatheringDateText(),
                        state.getDataHolder().getGatheringNotes(), state.getDataHolder().getGatheringMethod(), state.getTransformer().getReferenceSystemByKey(
                                state.getDataHolder().getGatheringSpatialDatum()),
                         state.getConfig());

                unitsGatheringEvent.setGatheringDepth(state.getDataHolder().getGatheringDepthText(),state.getDataHolder().getGatheringDepthMin(), state.getDataHolder().getGatheringDepthMax(), state.getDataHolder().getGatheringDepthUnit());
                //unitsGatheringEvent.setHeight(heightText, heightMin, heightMax, heightUnit);
                if(state.getDataHolder().gatheringAgentsList.isEmpty()) {
                    unitsGatheringEvent.setCollector(state.getPersonStore().get(state.getDataHolder().gatheringAgentsText), config);
                }else{
                    unitsGatheringEvent.setCollector(state.getPersonStore().get(state.getDataHolder().gatheringAgentsList.toString()), config);
                }
                // count
                UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea();
                //  unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(), getTermService());
                unitsGatheringArea.setParams(state.getDataHolder().isocountry, state.getDataHolder().country, (state.getConfig()), cdmAppController.getTermService(), cdmAppController.getOccurrenceService(), cdmAppController.getVocabularyService());

                DefinedTermBase<?> areaCountry =  unitsGatheringArea.getCountry();

                // other areas
                unitsGatheringArea = new UnitsGatheringArea();
                //            unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(),getTermService());

                unitsGatheringArea.setAreas(state.getDataHolder().getNamedAreaList(),(state.getConfig()), cdmAppController.getTermService(), cdmAppController.getVocabularyService());

                ArrayList<DefinedTermBase> nas = unitsGatheringArea.getAreas();
                for (DefinedTermBase namedArea : nas) {
                    unitsGatheringEvent.addArea(namedArea);
                }

                // copy gathering event to facade
                GatheringEvent gatheringEvent = unitsGatheringEvent.getGatheringEvent();
                if (fieldUnit != null){
                    derivedUnitFacade.setFieldUnit(fieldUnit);
                }

                derivedUnitFacade.setLocality(gatheringEvent.getLocality());
                derivedUnitFacade.setExactLocation(gatheringEvent.getExactLocation());
                derivedUnitFacade.setCollector(gatheringEvent.getCollector());
                derivedUnitFacade.setCountry((NamedArea)areaCountry);
                derivedUnitFacade.setAbsoluteElevationText(gatheringEvent.getAbsoluteElevationText());
                derivedUnitFacade.setAbsoluteElevation(gatheringEvent.getAbsoluteElevation());
                derivedUnitFacade.setAbsoluteElevationMax(gatheringEvent.getAbsoluteElevationMax());
                derivedUnitFacade.setDistanceToGroundText(gatheringEvent.getDistanceToGroundText());
                derivedUnitFacade.setDistanceToGroundMax(gatheringEvent.getDistanceToGroundMax());
                derivedUnitFacade.setDistanceToGround(gatheringEvent.getDistanceToGround());
                derivedUnitFacade.setDistanceToWaterSurfaceText(gatheringEvent.getDistanceToWaterSurfaceText());
                derivedUnitFacade.setDistanceToWaterSurfaceMax(gatheringEvent.getDistanceToWaterSurfaceMax());
                derivedUnitFacade.setDistanceToWaterSurface(gatheringEvent.getDistanceToWaterSurface());
                derivedUnitFacade.setGatheringPeriod(gatheringEvent.getTimeperiod());
                derivedUnitFacade.setCollectingMethod(gatheringEvent.getCollectingMethod());

                for(DefinedTermBase<?> area:unitsGatheringArea.getAreas()){
                    derivedUnitFacade.addCollectingArea((NamedArea) area);
                }
                //            derivedUnitFacade.addCollectingAreas(unitsGatheringArea.getAreas());
                // TODO exsiccatum

                // add fieldNumber
                derivedUnitFacade.setFieldNumber(NB(state.getDataHolder().getFieldNumber()));
                save(unitsGatheringEvent.getLocality(), state);

            // add unitNotes
            if (state.getDataHolder().getUnitNotes() != null){
                derivedUnitFacade.addAnnotation(Annotation.NewDefaultLanguageInstance(NB(state.getDataHolder().getUnitNotes())));
            }



            // //add Multimedia URLs
            if (state.getDataHolder().getMultimediaObjects().size() != -1) {
                for (String multimediaObject : state.getDataHolder().getMultimediaObjects().keySet()) {
                    Media media;
                    try {
                        media = getImageMedia(multimediaObject, READ_MEDIA_DATA);
                        Map<String, String> attributes = state.getDataHolder().getMultimediaObjects().get(multimediaObject);
                        if (attributes.containsKey("Context")){
                            LanguageString description = LanguageString.NewInstance(attributes.get("Context"), Language.ENGLISH());
                            media.addDescription(description);
                        }
                        if (attributes.containsKey("Comment")){
                            LanguageString description = LanguageString.NewInstance(attributes.get("Comment"), Language.ENGLISH());
                            media.addDescription(description);
                        }
                        if (attributes.containsKey("Creators")){
                            String creators = attributes.get("Creators");
                            Person artist;
                            Team artistTeam;
                            String[] artists;
                            if (creators != null){
                                if (creators.contains("&")){
                                    artists = creators.split("&");
                                    artistTeam = new Team();
                                    for (String creator:artists){
                                        artist = Person.NewTitledInstance(creator);
                                        artistTeam.addTeamMember(artist);
                                    }
                                    media.setArtist(artistTeam);
                                } else{

                                    artist = Person.NewTitledInstance(creators);
                                    media.setArtist(artist);
                                }
                            }



                        }

                        derivedUnitFacade.addDerivedUnitMedia(media);
                        if(state.getConfig().isAddMediaAsMediaSpecimen()){
                            //add media also as specimen scan
                            MediaSpecimen mediaSpecimen = MediaSpecimen.NewInstance(SpecimenOrObservationType.StillImage);
                            mediaSpecimen.setMediaSpecimen(media);
                            //do it only once!!
                            DefinedTermBase specimenScanTerm = getTermService().load(SPECIMEN_SCAN_TERM);
                            if(specimenScanTerm instanceof DefinedTerm){
                                mediaSpecimen.setKindOfUnit((DefinedTerm) specimenScanTerm);
                            }
                            DerivationEvent derivationEvent = DerivationEvent.NewInstance(DerivationEventType.PREPARATION());
                            derivationEvent.addDerivative(mediaSpecimen);
                            derivedUnitFacade.innerDerivedUnit().addDerivationEvent(derivationEvent);
                        }

                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
            //multimedia for fieldUnit
            if (state.getDataHolder().getGatheringMultimediaObjects().size() != -1) {
                for (String multimediaObject : state.getDataHolder().getGatheringMultimediaObjects().keySet()) {
                    Media media;
                    try {
                        media = getImageMedia(multimediaObject, READ_MEDIA_DATA);
                        Map<String, String> attributes = state.getDataHolder().getGatheringMultimediaObjects().get(multimediaObject);
                        if (attributes.containsKey("Context")){
                            LanguageString description = LanguageString.NewInstance(attributes.get("Context"), Language.ENGLISH());
                            media.addDescription(description);
                        }
                        if (attributes.containsKey("Comment")){
                            LanguageString description = LanguageString.NewInstance(attributes.get("Comment"), Language.ENGLISH());
                            media.addDescription(description);
                        }
                        if (attributes.containsKey("Creators")){
                            String creators = attributes.get("Creators");
                            Person artist;
                            Team artistTeam;
                            String[] artists;
                            if (creators != null){
                                if (creators.contains("&")){
                                    artists = creators.split("&");
                                    artistTeam = new Team();
                                    for (String creator:artists){
                                        artist = Person.NewTitledInstance(creator);
                                        artistTeam.addTeamMember(artist);
                                    }
                                    media.setArtist(artistTeam);
                                } else{

                                    artist = Person.NewTitledInstance(creators);
                                    media.setArtist(artist);
                                }
                            }



                        }

                        derivedUnitFacade.addFieldObjectMedia(media);


                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }

            //			/*
            //			 * merge AND STORE DATA
            //			 */
            //			getTermService().saveOrUpdate(areaCountry);// TODO save area sooner
            //
            //			for (NamedArea area : otherAreas) {
            //				getTermService().saveOrUpdate(area);// merge it sooner (foreach area)
            //			}
//           save(derivedUnitFacade.getFieldUnit(false), state);
           if (derivedUnitFacade.getFieldUnit(false) != null){
               state.setFieldUnit(derivedUnitFacade.getFieldUnit(false));
           }

            // handle collection data
            setCollectionData(state, derivedUnitFacade);

            //Reference stuff
            SpecimenUserInteraction sui = config.getSpecimenUserInteraction();
            Map<String,OriginalSourceBase<?>> sourceMap = new HashMap<>();

            state.getDataHolder().setDocSources(new ArrayList<>());
            for (String[] fullReference : state.getDataHolder().getReferenceList()) {
                String strReference=fullReference[0];
                String citationDetail = fullReference[1];
                String citationURL = fullReference[2];

                if (!citationURL.isEmpty()) {
                    citationDetail+=", "+citationURL;
                }

                Reference reference;
                if(strReference.equals(state.getRef().getTitleCache())){
                    reference = state.getRef();
                }
                else{
                    reference = ReferenceFactory.newGeneric();
                    reference.setTitle(strReference);
                }


                save(reference, state);
                IdentifiableSource sour = getIdentifiableSource(reference, citationDetail);
                sour.getCitation().setUri(state.getActualAccessPoint());
                sour.setType(OriginalSourceType.PrimaryTaxonomicSource);
                try{
                    if (sour.getCitation() != null){
                        if(StringUtils.isNotBlank(sour.getCitationMicroReference())) {
                            state.getDataHolder().getDocSources().add(sour.getCitation().getTitleCache()+ "---"+sour.getCitationMicroReference());
                        } else {
                            state.getDataHolder().getDocSources().add(sour.getCitation().getTitleCache());
                        }
                    }
                }catch(Exception e){
                    logger.warn("oups");
                }
                derivedUnitFacade.addSource(sour);

            }
//            List<IdentifiableSource> issTmp = new ArrayList<IdentifiableSource>();//getCommonService().list(IdentifiableSource.class, null, null, null, null);
//            List<DescriptionElementSource> issTmp2 = new ArrayList<DescriptionElementSource>();//getCommonService().list(DescriptionElementSource.class, null, null, null, null);
//
//            Set<OriginalSourceBase> osbSet = new HashSet<OriginalSourceBase>();
//            if(issTmp2!=null) {
//                osbSet.addAll(issTmp2);
//            }
//            if(issTmp!=null) {
//                osbSet.addAll(issTmp);
//            }

            IdentifiableSource sour = getIdentifiableSource(state.getRef(),null);
            String idInSource = derivedUnitFacade.getAccessionNumber() != null? derivedUnitFacade.getAccessionNumber():derivedUnitFacade.getCatalogNumber();
            sour.getCitation().setUri(state.getActualAccessPoint());
            sour.setIdInSource(idInSource);
            try{
                if (sour.getCitation() != null){
                    if(StringUtils.isNotBlank(sour.getCitationMicroReference())) {
                        state.getDataHolder().getDocSources().add(sour.getCitation().getTitleCache()+ "---"+sour.getCitationMicroReference());
                    } else {
                        state.getDataHolder().getDocSources().add(sour.getCitation().getTitleCache());
                    }
                }
            }catch(Exception e){
                logger.warn("oups");
            }

           derivedUnitFacade.addSource(sour);
          // sourceMap.put(sour.getCitation().getTitleCache()+ "---"+sour.getCitationMicroReference(),sour);

//            if( state.getConfig().isInteractWithUser()){
//                List<OriginalSourceBase<?>>sources=null;
//                if(!state.isDerivedUnitSourcesSet()){
//                    sources= sui.askForSource(sourceMap, "the unit itself","",getReferenceService(), state.getDataHolder().getDocSources());
//                    state.setDerivedUnitSources(sources);
//                    state.setDerivedUnitSourcesSet(true);
//                }
//                else{
//                    sources=state.getDerivedUnitSources();
//                }
////                for (OriginalSourceBase<?> source:sources){
////                    if(source.isInstanceOf(IdentifiableSource.class)){
////                        if(sourceNotLinkedToElement(derivedUnitFacade,source)) {
////                            derivedUnitFacade.addSource((IdentifiableSource)source.clone());
////                        }
////                    }else{
////                        if(sourceNotLinkedToElement(derivedUnitFacade,sour)) {
////                            derivedUnitFacade.addSource(OriginalSourceType.Import,source.getCitation(),source.getCitationMicroReference(), ioName);
////                        }
////                    }
////                }
//            }else{
//                for (OriginalSourceBase<?> sr : sourceMap.values()){
//                    if(sr.isInstanceOf(IdentifiableSource.class)){
//                        if(sourceNotLinkedToElement(derivedUnitFacade,sr)) {
//                            derivedUnitFacade.addSource((IdentifiableSource)sr.clone());
//                        }
//                    }else{
//                        if(sourceNotLinkedToElement(derivedUnitFacade,sr)) {
//                            derivedUnitFacade.addSource(OriginalSourceType.Import,sr.getCitation(),sr.getCitationMicroReference(), ioName);
//                        }
//                    }
//                }
//            }

            save(state.getDerivedUnitBase(), state);

            if(logger.isDebugEnabled()) {
                logger.info("saved ABCD specimen ...");
            }

            // handle identifications
            handleIdentifications(state, derivedUnitFacade);

            //associatedUnits
            if (handleAssociatedUnits){
                importAssociatedUnits(state, item, derivedUnitFacade);
            }
            //siblings/ other children
            if (derivedUnitFacade.getType() != null && (derivedUnitFacade.getType().equals(SpecimenOrObservationType.LivingSpecimen) ||  derivedUnitFacade.getType().equals(SpecimenOrObservationType.TissueSample)  ||  derivedUnitFacade.getType().equals(SpecimenOrObservationType.OtherSpecimen)) &&state.getConfig().isGetSiblings()){
                getSiblings(state, item, derivedUnitFacade);
            }


        } catch (Exception e) {
            String message = "Error when reading record!";
            logger.warn(message);
            state.getReport().addException(message, e);
            e.printStackTrace();
            state.setUnsuccessfull();
        }

        return;
    }

    @Override
    protected void importAssociatedUnits(Abcd206ImportState state, Object itemObject, DerivedUnitFacade derivedUnitFacade) {

        Abcd206ImportConfigurator config = state.getConfig();
        //import associated units
        FieldUnit currentFieldUnit = derivedUnitFacade.innerFieldUnit();
        //TODO: push state (think of implementing stack architecture for state
        DerivedUnit currentUnit = state.getDerivedUnitBase();
        DerivationEvent currentDerivedFrom = currentUnit.getDerivedFrom();
        URI currentAccessPoint = state.getActualAccessPoint();
        String currentPrefix = state.getPrefix();
        Element item = null;
        if (itemObject instanceof Element){
            item = (Element)itemObject;
        }
        NodeList unitAssociationList = null;
        if (item != null){
            unitAssociationList = item.getElementsByTagName(currentPrefix+"UnitAssociation");
            for(int k=0;k<unitAssociationList.getLength();k++){
                if(unitAssociationList.item(k) instanceof Element){
                    Element unitAssociation = (Element)unitAssociationList.item(k);
                    UnitAssociationParser unitAssociationParser = new UnitAssociationParser(currentPrefix, state.getReport(), state.getCdmRepository());
                    UnitAssociationWrapper associationWrapper = unitAssociationParser.parse(unitAssociation);

                    state.setActualAccessPoint(associationWrapper.getAccesPoint());
                    if(associationWrapper!=null){
                        NodeList associatedUnits = associationWrapper.getAssociatedUnits();
                        if(associatedUnits!=null){
                            for(int m=0;m<associatedUnits.getLength();m++){
                                if(associatedUnits.item(m) instanceof Element){
                                    state.reset();
                                    state.setPrefix(associationWrapper.getPrefix());
                                    Abcd206ImportParser.setUnitPropertiesXML((Element) associatedUnits.item(m), new Abcd206XMLFieldGetter(state.getDataHolder(), state.getPrefix()), state);
                                    logger.debug("derived unit: " + state.getDerivedUnitBase().toString() + " associated unit: " +state.getDataHolder().getKindOfUnit() + ", " + state.getDataHolder().accessionNumber + ", " + state.getDataHolder().getRecordBasis() + ", " + state.getDataHolder().getUnitID());
                                    handleSingleUnit(state, associatedUnits.item(m), true);

                                    DerivedUnit associatedUnit = state.getDerivedUnitBase();
                                    FieldUnit associatedFieldUnit = null;
                                    java.util.Collection<FieldUnit> associatedFieldUnits = state.getCdmRepository().getOccurrenceService().getFieldUnits(associatedUnit.getUuid());
                                    //ignore field unit if associated unit has more than one
                                    if(associatedFieldUnits.size()>1){
                                        state.getReport().addInfoMessage(String.format("%s has more than one field unit.", associatedUnit));
                                    }
                                    else if(associatedFieldUnits.size()==1){
                                        associatedFieldUnit = associatedFieldUnits.iterator().next();
                                    }

                                    //attach current unit and associated unit depending on association type

                                    //parent-child relation:
                                    //copy derivation event and connect parent and sub derivative
//                                    if(associationWrapper.getAssociationType().contains("individual") || associationWrapper.getAssociationType().contains("culture") || associationWrapper.getAssociationType().contains("sample") || associationWrapper.getAssociationType().contains("same in situ")){
                                        if(currentDerivedFrom==null){
                                            state.getReport().addInfoMessage(String.format("No derivation event found for unit %s. Defaulting to ACCESSIONING event.",SpecimenImportUtility.getUnitID(currentUnit, config)));
                                            DerivationEvent.NewSimpleInstance(associatedUnit, currentUnit, DerivationEventType.ACCESSIONING());
                                        }
                                        else{
                                            DerivationEvent updatedDerivationEvent = DerivationEvent.NewSimpleInstance(associatedUnit, currentUnit, currentDerivedFrom.getType());
                                            updatedDerivationEvent.setActor(currentDerivedFrom.getActor());
                                            updatedDerivationEvent.setDescription(currentDerivedFrom.getDescription());
                                            updatedDerivationEvent.setInstitution(currentDerivedFrom.getInstitution());
                                            updatedDerivationEvent.setTimeperiod(currentDerivedFrom.getTimeperiod());

                                        }
                                        state.getReport().addDerivate(associatedUnit, currentUnit, config);
//                                    }
                                    //siblings relation
                                    //connect current unit to field unit of associated unit
//                                    else if(associationWrapper.getAssociationType().contains("population")|| associationWrapper.getAssociationType().contains("sample")){
//                                        //no associated field unit -> using current one
//                                        if(associatedFieldUnit==null){
//                                            if(currentFieldUnit!=null){
//                                                DerivationEvent.NewSimpleInstance(currentFieldUnit, associatedUnit, DerivationEventType.ACCESSIONING());
//                                            }
//                                        }
//                                        else{
//                                            if(currentDerivedFrom==null){
//                                                state.getReport().addInfoMessage("No derivation event found for unit "+SpecimenImportUtility.getUnitID(currentUnit, config)+". Defaulting to ACCESIONING event.");
//                                                DerivationEvent.NewSimpleInstance(associatedFieldUnit, currentUnit, DerivationEventType.ACCESSIONING());
//                                            }
//                                            if(currentDerivedFrom!=null && associatedFieldUnit==currentFieldUnit){
//                                                DerivationEvent updatedDerivationEvent = DerivationEvent.NewSimpleInstance(associatedFieldUnit, currentUnit, currentDerivedFrom.getType());
//                                                updatedDerivationEvent.setActor(currentDerivedFrom.getActor());
//                                                updatedDerivationEvent.setDescription(currentDerivedFrom.getDescription());
//                                                updatedDerivationEvent.setInstitution(currentDerivedFrom.getInstitution());
//                                                updatedDerivationEvent.setTimeperiod(currentDerivedFrom.getTimeperiod());
//                                            }
//                                        }
//                                    }

                                    //delete current field unit if replaced
                                    if(currentFieldUnit!=null && currentDerivedFrom!=null
                                            && currentFieldUnit.getDerivationEvents().size()==1  && currentFieldUnit.getDerivationEvents().contains(currentDerivedFrom) //making sure that the field unit
                                            && currentDerivedFrom.getDerivatives().size()==1 && currentDerivedFrom.getDerivatives().contains(currentUnit) //is not attached to other derived units
                                            && currentDerivedFrom!=currentUnit.getDerivedFrom() // <- derivation has been replaced and can be deleted
                                            ){
                                        currentFieldUnit.removeDerivationEvent(currentDerivedFrom);
                                        state.getCdmRepository().getOccurrenceService().delete(currentFieldUnit);
                                         }

                                    save(associatedUnit, state);
                                }
                            }
                        }
                    }
                }
            }
        }
        //TODO: pop state
        state.reset();
        state.setDerivedUnitBase(currentUnit);
        state.setActualAccessPoint(currentAccessPoint);
        state.setPrefix(currentPrefix);
    }




    /**
     * @param derivedUnitFacade
     * @param sour
     * @return
     */
    private boolean sourceNotLinkedToElement(DerivedUnitFacade derivedUnitFacade, OriginalSourceBase<?> source) {
        Set<IdentifiableSource> linkedSources = derivedUnitFacade.getSources();
        for (IdentifiableSource is:linkedSources){
            if (is.getCitation()!=null && source.getCitation()!=null &&
                    is.getCitation().getTitleCache().equalsIgnoreCase(source.getCitation().getTitleCache())){
                String isDetail =  is.getCitationMicroReference();
                if ((StringUtils.isBlank(isDetail) && StringUtils.isBlank(source.getCitationMicroReference()))
                        || (isDetail != null && isDetail.equalsIgnoreCase(source.getCitationMicroReference())) ) {
                    return false;
                }
            }
        }
        return true;
    }



    //    /**
    //     * @param reference
    //     * @param citationDetail
    //     * @return
    //     */
    //    private DescriptionElementSource getDescriptionSource(Reference reference, String citationDetail) {
    //
    //        List<OriginalSourceBase> issTmp2 = getCommonService().list(DescriptionElementSource.class, null, null, null, null);
    //
    //        try {
    //            for (OriginalSourceBase<?> osb:issTmp2){
    //                if (osb.getCitation().equals(reference) && osb.getCitationMicroReference().equalsIgnoreCase(citationDetail)) {
    //                    return (DescriptionElementSource) osb.clone();
    //                }
    //            }
    //        } catch (CloneNotSupportedException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        }
    //
    //        DescriptionElementSource sour = DescriptionElementSource.NewInstance(OriginalSourceType.Import,null,null, reference,citationDetail);
    //        return sour;
    //    }




    /**
     * setCollectionData : store the collection object into the
     * derivedUnitFacade
     *
     * @param state
     */
    protected void setCollectionData(Abcd206ImportState state, DerivedUnitFacade derivedUnitFacade) {
        Abcd206ImportConfigurator config = state.getConfig();
        SpecimenImportUtility.setUnitID(derivedUnitFacade.innerDerivedUnit(), state.getDataHolder().getUnitID(), config);
        if(!config.isMapUnitIdToAccessionNumber()){
            derivedUnitFacade.setAccessionNumber(NB(state.getDataHolder().accessionNumber));
        }
        // derivedUnitFacade.setCollectorsNumber(NB(state.getDataHolder().collectorsNumber));

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
     * @param state
     *
     * @return DerivedUnitFacade
     */
    @Override
    protected DerivedUnitFacade getFacade(Abcd206ImportState state) {
        if(logger.isDebugEnabled()) {
            logger.info("getFacade()");
        }
        SpecimenOrObservationType type = null;
        DefinedTerm kindOfUnit = null;

        // create specimen
        if (NB((state.getDataHolder()).getRecordBasis()) != null) {
            if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("living")>-1) {
                type = SpecimenOrObservationType.LivingSpecimen;
            }
            else if (state.getDataHolder().getRecordBasis().toLowerCase().startsWith("s") || state.getDataHolder().getRecordBasis().toLowerCase().indexOf("specimen")>-1) {// specimen
                type = SpecimenOrObservationType.PreservedSpecimen;
            }
            else if (state.getDataHolder().getRecordBasis().toLowerCase().startsWith("o") ||state.getDataHolder().getRecordBasis().toLowerCase().indexOf("observation")>-1 ) {
                type = SpecimenOrObservationType.Observation;
            }
            else if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("fossil")>-1){
                type = SpecimenOrObservationType.Fossil;
            }else if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("materialsample")>-1){
                type = SpecimenOrObservationType.OtherSpecimen;
            }
            else if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("sample")>-1){
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
            if (state.getDataHolder().getKindOfUnit().toLowerCase().indexOf("clone")>-1) {
                kindOfUnit = getKindOfUnit(state, null, "clone culture", "clone culture", "cc", null);
            }
            else if (state.getDataHolder().getKindOfUnit().toLowerCase().startsWith("live"))  {
                kindOfUnit = getKindOfUnit(state, null, "live sample", "live sample", "ls", null);
            }


            if (kindOfUnit == null) {
                logger.info("The kind of unit does not seem to be known: " + state.getDataHolder().getKindOfUnit());

            }
        } else {
            logger.info("The kind of unit is null");

        }
        DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(type);
        derivedUnitFacade.setFieldUnit(state.getFieldUnit(state.getDataHolder().getFieldNumber()));
        derivedUnitFacade.setKindOfUnit(kindOfUnit);
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
        //state.getDataHolder().gatheringAgents = "";

        abcdFieldGetter.getType(root);
        abcdFieldGetter.getGatheringPeople(root);
    }



    //    private void compareABCDtoCDM(URI urlFileName, List<String> knownElts, Abcd206XMLFieldGetter abcdFieldGetter) {
    //        try {
    //            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    //            DocumentBuilder constructeur = factory.newDocumentBuilder();
    //            URL url = urlFileName.toURL();
    //            Object o = url.getContent();
    //            InputStream is = (InputStream) o;
    //            Document document = constructeur.parse(is);
    //            Element root = document.getDocumentElement();
    //            abcdFieldGetter.traverse(root);
    //        }
    //        catch (ParserConfigurationException e){
    //            e.printStackTrace();
    //        }
    //        catch (SAXException e) {
    //            e.printStackTrace();
    //        }
    //        catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //        Set<String> elts = state.getDataHolder().allABCDelements.keySet();
    //        Iterator<String> it = elts.iterator();
    //        String elt;
    //        while (it.hasNext()) {
    //            elt = it.next();
    //            if (knownElts.indexOf(elt) == -1) {
    //                if(DEBUG) {
    //                    logger.info("Unmerged ABCD element: " + elt + " - "+ state.getDataHolder().allABCDelements.get(elt));
    //                }
    //            }
    //        }
    //    }

    /**
     * Load the list of names from the ABCD file and save them
     * @param state : the current ABCD import state
     * @param unitsList : the unit list from the ABCD file
     * @param abcdFieldGetter : the ABCD parser
     */
    private void prepareCollectors(Abcd206ImportState state, NodeList unitsList, Abcd206XMLFieldGetter abcdFieldGetter) {

        TeamOrPersonBase<?> teamOrPerson = null;
        Team team = null;
        //ImportHelper.setOriginalSource(teamOrPerson, state.getConfig().getSourceReference(), collector, "Collector");
        for (int i = 0; i < unitsList.getLength(); i++) {
            this.getCollectorsFromXML((Element) unitsList.item(i), abcdFieldGetter, state);
            if (!(state.getDataHolder().gatheringAgentsList.isEmpty())){
                if (state.getDataHolder().gatheringAgentsList.size() == 1){
                    teamOrPerson = parseAuthorString(state.getDataHolder().gatheringAgentsList.get(0));
                }else{
                    team = new Team();
                    for(String collector: state.getDataHolder().gatheringAgentsList){
                        teamOrPerson = parseAuthorString(collector);
                        if (teamOrPerson instanceof Person){
                            team.addTeamMember((Person)teamOrPerson);
                        }else{
                            for (Person person: ((Team)teamOrPerson).getTeamMembers()){
                                team.addTeamMember(person);
                            }
                        }
                    }
                }
                if (!state.getPersonStore().containsId(state.getDataHolder().gatheringAgentsList.toString())) {
                    state.getPersonStore().put(state.getDataHolder().gatheringAgentsList.toString(), teamOrPerson);
                    if (logger.isDebugEnabled()) { logger.debug("Stored author " + state.getDataHolder().gatheringAgentsList.toString());}
                    logger.warn("Not imported author with duplicated aut_id " + state.getDataHolder().gatheringAgentsList.toString() );
                }
            }
            if (!StringUtils.isBlank(state.getDataHolder().gatheringAgentsText) && state.getDataHolder().gatheringAgentsList.isEmpty()){
                teamOrPerson = parseAuthorString(state.getDataHolder().gatheringAgentsText);
            }

        }




//        List<String> collectorsU = new ArrayList<String>(new HashSet<String>(collectors));
//        List<String> teamsU = new ArrayList<String>(new HashSet<String>(teams));
//
//
//        //existing teams in DB
//        Map<String,Team> titleCacheTeam = new HashMap<String, Team>();
       // List<UuidAndTitleCache<Team>> hiberTeam = new ArrayList<UuidAndTitleCache<Team>>();//getAgentService().getTeamUuidAndTitleCache();

//        Set<UUID> uuids = new HashSet<UUID>();
//        for (UuidAndTitleCache<Team> hibernateT:hiberTeam){
//            uuids.add(hibernateT.getUuid());
//        }
//        if (!uuids.isEmpty()){
//            List<AgentBase> existingTeams = getAgentService().find(uuids);
//            for (AgentBase<?> existingP:existingTeams){
//                titleCacheTeam.put(existingP.getTitleCache(),CdmBase.deproxy(existingP,Team.class));
//            }
//        }


//        Map<String,UUID> teamMap = new HashMap<String, UUID>();
//        for (UuidAndTitleCache<Team> uuidt:hiberTeam){
//            teamMap.put(uuidt.getTitleCache(), uuidt.getUuid());
//        }

        //existing persons in DB
//        List<UuidAndTitleCache<Person>> hiberPersons = new ArrayList<UuidAndTitleCache<Person>>();//getAgentService().getPersonUuidAndTitleCache();
//        Map<String,Person> titleCachePerson = new HashMap<String, Person>();
//        uuids = new HashSet<UUID>();
//        for (UuidAndTitleCache<Person> hibernateP:hiberPersons){
//            uuids.add(hibernateP.getUuid());
//        }
//
//        if (!uuids.isEmpty()){
//            List<AgentBase> existingPersons = getAgentService().find(uuids);
//            for (AgentBase<?> existingP:existingPersons){
//                titleCachePerson.put(existingP.getTitleCache(),CdmBase.deproxy(existingP,Person.class));
//            }
//        }
//
//        Map<String,UUID> personMap = new HashMap<String, UUID>();
//        for (UuidAndTitleCache<Person> person:hiberPersons){
//            personMap.put(person.getTitleCache(), person.getUuid());
//        }
//
//        java.util.Collection<Person> personToadd = new ArrayList<Person>();
//        java.util.Collection<Team> teamToAdd = new ArrayList<Team>();
//
//        for (String collector:collectorsU){
//            Person p = Person.NewInstance();
//            p.setTitleCache(collector,true);
//            if (!personMap.containsKey(p.getTitleCache())){
//                personToadd.add(p);
//            }
//        }
//        for (String team:teamsU){
//            Team p = Team.NewInstance();
//            p.setTitleCache(team,true);
//            if (!teamMap.containsKey(p.getTitleCache())){
//                teamToAdd.add(p);
//            }
//        }
//
//        if(!personToadd.isEmpty()){
//            for (Person agent: personToadd){
//                save(agent, state);
//                titleCachePerson.put(agent.getTitleCache(),CdmBase.deproxy(agent, Person.class) );
//            }
//        }
//
//        Person ptmp ;
//        Map <String,Integer>teamdone = new HashMap<String, Integer>();
//        for (List<String> collteam: collectorinteams){
//            if (!teamdone.containsKey(StringUtils.join(collteam.toArray(),"-"))){
//                Team team = new Team();
//                boolean em =true;
//                for (String collector:collteam){
//                    ptmp = Person.NewInstance();
//                    ptmp.setTitleCache(collector,true);
//                    Person p2 = titleCachePerson.get(ptmp.getTitleCache());
//                    team.addTeamMember(p2);
//                    em=false;
//                }
//                if (!em) {
//                    teamToAdd.add(team);
//                }
//                teamdone.put(StringUtils.join(collteam.toArray(),"-"),0);
//            }
//        }
//
//        if(!teamToAdd.isEmpty()){
//            for (Team agent: teamToAdd){
//                save(agent, state);
//                titleCacheTeam.put(agent.getTitleCache(), CdmBase.deproxy( agent,Team.class) );
//            }
//        }

//        ((Abcd206ImportConfigurator) state.getConfig()).setTeams(titleCacheTeam);
//        ((Abcd206ImportConfigurator) state.getConfig()).setPersons(titleCachePerson);
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

}
