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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.ext.occurrence.bioCase.BioCaseQueryServiceWrapper;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportBase;
import eu.etaxonomy.cdm.io.specimen.SpecimenUserInteraction;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.molecular.AbcdDnaParser;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
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
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;

/**
 * @author p.kelbert
 * @author p.plitzner
 * @created 20.10.2008
 */
@Component
public class Abcd206Import extends SpecimenImportBase<Abcd206ImportConfigurator, Abcd206ImportState> {

    private static final UUID SPECIMEN_SCAN_TERM = UUID.fromString("acda15be-c0e2-4ea8-8783-b9b0c4ad7f03");

    private static final Logger logger = Logger.getLogger(Abcd206Import.class);





    public Abcd206Import() {
        super();
    }

    @Override
    protected boolean doCheck(Abcd206ImportState state) {
        logger.warn("Checking not yet implemented for " + this.getClass().getSimpleName());
        return true;
    }


    @Override
    @SuppressWarnings("rawtypes")
    public void doInvoke(Abcd206ImportState state) {
        Abcd206ImportConfigurator config = state.getConfig();
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

                   response = queryService.query(config.getOccurenceQuery(), config.getSourceUri());

                }catch(Exception e){
                    logger.error("An error during ABCD import");
                }
            }
            SpecimenUserInteraction sui = ((Abcd206ImportConfigurator)state.getConfig()).getSpecimenUserInteraction();

            //init import reference
        //  List<Reference> references = getReferenceService().list(Reference.class, null, null, null, null);
            List<Reference> references = new ArrayList<Reference>();

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
                    String name = NB(((Abcd206ImportConfigurator) state.getConfig()).getSourceReferenceTitle());
                    for (Reference reference : references) {
                        if (! StringUtils.isBlank(reference.getTitleCache())) {
                            if (reference.getTitleCache().equalsIgnoreCase(name)) {
                                state.setRef(reference);
                            }
                        }
                    }
                    if (state.getRef() == null){
                        state.setRef(ReferenceFactory.newGeneric());
                        state.getRef().setTitle("ABCD classic");
                    }
                }
            //}
            save(state.getRef(), state);
            ((Abcd206ImportConfigurator) state.getConfig()).setSourceReference(state.getRef());

            if(((Abcd206ImportConfigurator) state.getConfig()).getClassificationUuid()!=null){
                //load classification from config if it exists
                state.setClassification(getClassificationService().load(((Abcd206ImportConfigurator) state.getConfig()).getClassificationUuid()));
            }
            if(state.getClassification()==null){//no existing classification was set in config
                List<Classification> classificationList = getClassificationService().list(Classification.class, null, null, null, null);
                //get classification via user interaction
                if (((Abcd206ImportConfigurator) state.getConfig()).isUseClassification() && ((Abcd206ImportConfigurator) state.getConfig()).isInteractWithUser()){
                    Map<String,Classification> classMap = new HashMap<String, Classification>();
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
                    String name = NB(((Abcd206ImportConfigurator) state.getConfig()).getClassificationName());
                    for (Classification classif : classificationList){
                        if (classif.getTitleCache() != null && classif.getTitleCache().equalsIgnoreCase(name)) {
                            state.setClassification(classif);
                        }
                    }
                    if (state.getClassification() == null){
                        state.setClassification(Classification.NewInstance(name, state.getRef(), Language.DEFAULT()));
                        //we do not need a default classification when creating an empty new one
                        state.setDefaultClassification(state.getClassification());
                        save(state.getDefaultClassification(), state);
                    }
                    save(state.getClassification(), state);
                }
            }

            if (response == null){
                response =(InputStream) ((Abcd206ImportConfigurator) state.getConfig()).getSource();
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

                state.setAssociationRefs(new ArrayList<OriginalSourceBase<?>>());
                state.setDescriptionRefs(new ArrayList<OriginalSourceBase<?>>());
                state.setDerivedUnitSources(new ArrayList<OriginalSourceBase<?>>());

                for (int i = 0; i < unitsList.getLength(); i++) {
                    if(state.getConfig().getProgressMonitor().isCanceled()){
                        break;
                    }

                    state.reset();

                    Element item = (Element) unitsList.item(i);
                    this.setUnitPropertiesXML( item, abcdFieldGetter, state);
                    updateProgress(state, "Importing data for unit "+state.getDataHolder().getUnitID()+" ("+i+"/"+unitsList.getLength()+")");

                    //import unit + field unit data
                    this.handleSingleUnit(state, item);

                }
                if(((Abcd206ImportConfigurator)state.getConfig()).isDeduplicateReferences()){
                    getReferenceService().deduplicate(Reference.class, null, null);
                }
                if(((Abcd206ImportConfigurator)state.getConfig()).isDeduplicateClassifications()){
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
     * Handle a single unit
     * @param state
     * @param item
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void handleSingleUnit(Abcd206ImportState state, Object itemObject) {
        Element item = (Element) itemObject;

        Abcd206ImportConfigurator config = state.getConfig();
        if (DEBUG) {
            logger.info("handleSingleUnit "+state.getRef());
        }
        try {
            ICdmApplicationConfiguration cdmAppController = state.getConfig().getCdmAppController();
            if(cdmAppController==null){
                cdmAppController = this;
            }
            //check if unit already exists
            DerivedUnitFacade derivedUnitFacade = null;
            if(((Abcd206ImportConfigurator)state.getConfig()).isIgnoreImportOfExistingSpecimens()){
                SpecimenOrObservationBase<?> existingSpecimen = findExistingSpecimen(state.getDataHolder().getUnitID(), state);
                if(existingSpecimen!=null && existingSpecimen.isInstanceOf(DerivedUnit.class)){
                    DerivedUnit derivedUnit = HibernateProxyHelper.deproxy(existingSpecimen, DerivedUnit.class);
                    state.setDerivedUnitBase(derivedUnit);
                    derivedUnitFacade = DerivedUnitFacade.NewInstance(state.getDerivedUnitBase());
                    importAssociatedUnits(state, item, derivedUnitFacade);
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
            // gathering event
            UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(cdmAppController.getTermService(),
                    state.getDataHolder().locality, state.getDataHolder().languageIso, state.getDataHolder().longitude,
                    state.getDataHolder().latitude, state.getDataHolder().getGatheringElevationText(),
                    state.getDataHolder().getGatheringElevationMin(), state.getDataHolder().getGatheringElevationMax(),
                    state.getDataHolder().getGatheringElevationUnit(), state.getDataHolder().getGatheringDateText(),
                    state.getDataHolder().getGatheringNotes(), state.getTransformer().getReferenceSystemByKey(
                            state.getDataHolder().getGatheringSpatialDatum()), state.getDataHolder().gatheringAgentList,
                    state.getDataHolder().gatheringTeamList, state.getConfig());

            // country
            UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea();
            //  unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(), getTermService());
            unitsGatheringArea.setParams(state.getDataHolder().isocountry, state.getDataHolder().country, (state.getConfig()), cdmAppController.getTermService(), cdmAppController.getOccurrenceService());

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
            derivedUnitFacade.setLocality(gatheringEvent.getLocality());
            derivedUnitFacade.setExactLocation(gatheringEvent.getExactLocation());
            derivedUnitFacade.setCollector(gatheringEvent.getCollector());
            derivedUnitFacade.setCountry((NamedArea)areaCountry);
            derivedUnitFacade.setAbsoluteElevationText(gatheringEvent.getAbsoluteElevationText());
            derivedUnitFacade.setAbsoluteElevation(gatheringEvent.getAbsoluteElevation());
            derivedUnitFacade.setAbsoluteElevationMax(gatheringEvent.getAbsoluteElevationMax());
            derivedUnitFacade.setGatheringPeriod(gatheringEvent.getTimeperiod());

            for(DefinedTermBase<?> area:unitsGatheringArea.getAreas()){
                derivedUnitFacade.addCollectingArea((NamedArea) area);
            }
            //            derivedUnitFacade.addCollectingAreas(unitsGatheringArea.getAreas());
            // TODO exsiccatum

            // add fieldNumber
            derivedUnitFacade.setFieldNumber(NB(state.getDataHolder().getFieldNumber()));

            // add unitNotes
            derivedUnitFacade.addAnnotation(Annotation.NewDefaultLanguageInstance(NB(state.getDataHolder().getUnitNotes())));

            // //add Multimedia URLs
            if (state.getDataHolder().getMultimediaObjects().size() != -1) {
                for (String multimediaObject : state.getDataHolder().getMultimediaObjects()) {
                    Media media;
                    try {
                        media = getImageMedia(multimediaObject, READ_MEDIA_DATA);
                        derivedUnitFacade.addDerivedUnitMedia(media);
                        if(((Abcd206ImportConfigurator)state.getConfig()).isAddMediaAsMediaSpecimen()){
                            //add media also as specimen scan
                            MediaSpecimen mediaSpecimen = MediaSpecimen.NewInstance(SpecimenOrObservationType.StillImage);
                            mediaSpecimen.setMediaSpecimen(media);
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

            //			/*
            //			 * merge AND STORE DATA
            //			 */
            //			getTermService().saveOrUpdate(areaCountry);// TODO save area sooner
            //
            //			for (NamedArea area : otherAreas) {
            //				getTermService().saveOrUpdate(area);// merge it sooner (foreach area)
            //			}

            save(unitsGatheringEvent.getLocality(), state);

            // handle collection data
            setCollectionData(state, derivedUnitFacade);

            //Reference stuff
            SpecimenUserInteraction sui = config.getSpecimenUserInteraction();
            Map<String,OriginalSourceBase<?>> sourceMap = new HashMap<String, OriginalSourceBase<?>>();

            state.getDataHolder().setDocSources(new ArrayList<String>());
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

                IdentifiableSource sour = getIdentifiableSource(reference,citationDetail);

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
                reference.addSource(sour);
                save(reference, state);
            }
            List<IdentifiableSource> issTmp = new ArrayList<IdentifiableSource>();//getCommonService().list(IdentifiableSource.class, null, null, null, null);
            List<DescriptionElementSource> issTmp2 = new ArrayList<DescriptionElementSource>();//getCommonService().list(DescriptionElementSource.class, null, null, null, null);

            Set<OriginalSourceBase> osbSet = new HashSet<OriginalSourceBase>();
            if(issTmp2!=null) {
                osbSet.addAll(issTmp2);
            }
            if(issTmp!=null) {
                osbSet.addAll(issTmp);
            }

            addToSourceMap(sourceMap, osbSet);

            if( ((Abcd206ImportConfigurator) state.getConfig()).isInteractWithUser()){
                List<OriginalSourceBase<?>>sources=null;
                if(!state.isDerivedUnitSourcesSet()){
                    sources= sui.askForSource(sourceMap, "the unit itself","",getReferenceService(), state.getDataHolder().getDocSources());
                    state.setDerivedUnitSources(sources);
                    state.setDerivedUnitSourcesSet(true);
                }
                else{
                    sources=state.getDerivedUnitSources();
                }
//                System.out.println("nb sources: "+sources.size());
//                System.out.println("derivedunitfacade : "+derivedUnitFacade.getTitleCache());
                for (OriginalSourceBase<?> sour:sources){
                    if(sour.isInstanceOf(IdentifiableSource.class)){
                        if(sourceNotLinkedToElement(derivedUnitFacade,sour)) {
//                            System.out.println("add source to derivedunitfacade1 "+derivedUnitFacade.getTitleCache());
                            derivedUnitFacade.addSource((IdentifiableSource)sour.clone());
                        }
                    }else{
                        if(sourceNotLinkedToElement(derivedUnitFacade,sour)) {
//                            System.out.println("add source to derivedunitfacade2 "+derivedUnitFacade.getTitleCache());
                            derivedUnitFacade.addSource(OriginalSourceType.Import,sour.getCitation(),sour.getCitationMicroReference(), ioName);
                        }
                    }
                }
            }else{
                for (OriginalSourceBase<?> sr : sourceMap.values()){
                    if(sr.isInstanceOf(IdentifiableSource.class)){
                        if(sourceNotLinkedToElement(derivedUnitFacade,sr)) {
//                            System.out.println("add source to derivedunitfacade3 "+derivedUnitFacade.getTitleCache());
                            derivedUnitFacade.addSource((IdentifiableSource)sr.clone());
                        }
                    }else{
                        if(sourceNotLinkedToElement(derivedUnitFacade,sr)) {
//                            System.out.println("add source to derivedunitfacade4 "+derivedUnitFacade.getTitleCache());
                            derivedUnitFacade.addSource(OriginalSourceType.Import,sr.getCitation(),sr.getCitationMicroReference(), ioName);
                        }
                    }
                }
            }

            save(state.getDerivedUnitBase(), state);

            if(DEBUG) {
                logger.info("saved ABCD specimen ...");
            }

            // handle identifications
            handleIdentifications(state, derivedUnitFacade);

            //associatedUnits
            importAssociatedUnits(state, item, derivedUnitFacade);



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
                    if(associationWrapper!=null){
                        NodeList associatedUnits = associationWrapper.getAssociatedUnits();
                        if(associatedUnits!=null){
                            for(int m=0;m<associatedUnits.getLength();m++){
                                if(associatedUnits.item(m) instanceof Element){
                                    state.reset();
                                    state.setPrefix(associationWrapper.getPrefix());
                                    this.setUnitPropertiesXML((Element) associatedUnits.item(m), new Abcd206XMLFieldGetter(state.getDataHolder(), state.getPrefix()), state);
                                    handleSingleUnit(state, associatedUnits.item(m));

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
                                    if(associationWrapper.getAssociationType().contains("individual")){
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
                                    }
                                    //siblings relation
                                    //connect current unit to field unit of associated unit
                                    else if(associationWrapper.getAssociationType().contains("population")){
                                        //no associated field unit -> using current one
                                        if(associatedFieldUnit==null){
                                            if(currentFieldUnit!=null){
                                                DerivationEvent.NewSimpleInstance(currentFieldUnit, associatedUnit, DerivationEventType.ACCESSIONING());
                                            }
                                        }
                                        else{
                                            if(currentDerivedFrom==null){
                                                state.getReport().addInfoMessage("No derivation event found for unit "+SpecimenImportUtility.getUnitID(currentUnit, config)+". Defaulting to ACCESIONING event.");
                                                DerivationEvent.NewSimpleInstance(associatedFieldUnit, currentUnit, DerivationEventType.ACCESSIONING());
                                            }
                                            if(currentDerivedFrom!=null && associatedFieldUnit!=currentFieldUnit){
                                                DerivationEvent updatedDerivationEvent = DerivationEvent.NewSimpleInstance(associatedFieldUnit, currentUnit, currentDerivedFrom.getType());
                                                updatedDerivationEvent.setActor(currentDerivedFrom.getActor());
                                                updatedDerivationEvent.setDescription(currentDerivedFrom.getDescription());
                                                updatedDerivationEvent.setInstitution(currentDerivedFrom.getInstitution());
                                                updatedDerivationEvent.setTimeperiod(currentDerivedFrom.getTimeperiod());
                                            }
                                        }
                                    }

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
        if(DEBUG) {
            logger.info("getFacade()");
        }
        SpecimenOrObservationType type = null;

        // create specimen
        if (NB((state.getDataHolder()).getRecordBasis()) != null) {
            if (state.getDataHolder().getRecordBasis().toLowerCase().startsWith("s") || state.getDataHolder().getRecordBasis().toLowerCase().indexOf("specimen")>-1) {// specimen
                type = SpecimenOrObservationType.PreservedSpecimen;
            }
            else if (state.getDataHolder().getRecordBasis().toLowerCase().startsWith("o") ||state.getDataHolder().getRecordBasis().toLowerCase().indexOf("observation")>-1 ) {
                type = SpecimenOrObservationType.Observation;
            }
            else if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("fossil")>-1){
                type = SpecimenOrObservationType.Fossil;
            }
            else if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("living")>-1) {
                type = SpecimenOrObservationType.LivingSpecimen;
            }
            if (type == null) {
                logger.info("The basis of record does not seem to be known: " + state.getDataHolder().getRecordBasis());
                type = SpecimenOrObservationType.DerivedUnit;
            }
        } else {
            logger.info("The basis of record is null");
            type = SpecimenOrObservationType.DerivedUnit;
        }
        DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(type);
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
        state.getDataHolder().gatheringAgentList = new ArrayList<String>();
        state.getDataHolder().gatheringTeamList = new ArrayList<String>();
        abcdFieldGetter.getType(root);
        abcdFieldGetter.getGatheringPeople(root);
    }

    /**
     * Store the unit's properties into variables Look which unit is the
     * preferred one Look what kind of name it is supposed to be, for the
     * parsing (Botanical, Zoological)
     * @param state
     *
     * @param racine: the root node for a single unit
     */
    private void setUnitPropertiesXML(Element root, Abcd206XMLFieldGetter abcdFieldGetter, Abcd206ImportState state) {
        try {
            NodeList group;

            group = root.getChildNodes();
            for (int i = 0; i < group.getLength(); i++) {
                if (group.item(i).getNodeName().equals(state.getPrefix() + "Identifications")) {
                    group = group.item(i).getChildNodes();
                    break;
                }
            }
            state.getDataHolder().setIdentificationList(new ArrayList<Identification>());
            state.getDataHolder().setStatusList(new ArrayList<SpecimenTypeDesignationStatus>());
            state.getDataHolder().setAtomisedIdentificationList(new ArrayList<HashMap<String, String>>());
            state.getDataHolder().setReferenceList(new ArrayList<String[]>());
            state.getDataHolder().setMultimediaObjects(new ArrayList<String>());

            abcdFieldGetter.getScientificNames(group);
            abcdFieldGetter.getType(root);

            if(DEBUG) {
                logger.info("this.identificationList "+state.getDataHolder().getIdentificationList().toString());
            }
            abcdFieldGetter.getIDs(root);
            abcdFieldGetter.getRecordBasis(root);
            abcdFieldGetter.getKindOfUnit(root);
            abcdFieldGetter.getMultimedia(root);
            abcdFieldGetter.getNumbers(root);
            abcdFieldGetter.getGeolocation(root, state);
            abcdFieldGetter.getGatheringPeople(root);
            abcdFieldGetter.getGatheringDate(root);
            abcdFieldGetter.getGatheringElevation(root);
            abcdFieldGetter.getGatheringNotes(root);
            abcdFieldGetter.getAssociatedUnitIds(root);
            abcdFieldGetter.getUnitNotes(root);
            boolean referencefound = abcdFieldGetter.getReferences(root);
            if (!referencefound) {
                String[]a = {state.getRef().getTitleCache(),"",""};
                state.getDataHolder().getReferenceList().add(a);
            }

        } catch (Exception e) {
            logger.info("Error occured while parsing XML file" + e);
            e.printStackTrace();
        }
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
        List<String> collectors = new ArrayList<String>();
        List<String> teams = new ArrayList<String>();
        List<List<String>> collectorinteams = new ArrayList<List<String>>();

        for (int i = 0; i < unitsList.getLength(); i++) {
            this.getCollectorsFromXML((Element) unitsList.item(i), abcdFieldGetter, state);
            for (String agent : state.getDataHolder().gatheringAgentList) {
                collectors.add(agent);
            }
            List<String> tmpTeam = new ArrayList<String>(new HashSet<String>(state.getDataHolder().gatheringTeamList));
            if(!tmpTeam.isEmpty()) {
                teams.add(StringUtils.join(tmpTeam.toArray()," & "));
            }
            for (String agent:tmpTeam) {
                collectors.add(agent);
            }
        }

        List<String> collectorsU = new ArrayList<String>(new HashSet<String>(collectors));
        List<String> teamsU = new ArrayList<String>(new HashSet<String>(teams));


        //existing teams in DB
        Map<String,Team> titleCacheTeam = new HashMap<String, Team>();
        List<UuidAndTitleCache<Team>> hiberTeam = new ArrayList<UuidAndTitleCache<Team>>();//getAgentService().getTeamUuidAndTitleCache();

        Set<UUID> uuids = new HashSet<UUID>();
        for (UuidAndTitleCache<Team> hibernateT:hiberTeam){
            uuids.add(hibernateT.getUuid());
        }
        if (!uuids.isEmpty()){
            List<AgentBase> existingTeams = getAgentService().find(uuids);
            for (AgentBase<?> existingP:existingTeams){
                titleCacheTeam.put(existingP.getTitleCache(),CdmBase.deproxy(existingP,Team.class));
            }
        }


        Map<String,UUID> teamMap = new HashMap<String, UUID>();
        for (UuidAndTitleCache<Team> uuidt:hiberTeam){
            teamMap.put(uuidt.getTitleCache(), uuidt.getUuid());
        }

        //existing persons in DB
        List<UuidAndTitleCache<Person>> hiberPersons = new ArrayList<UuidAndTitleCache<Person>>();//getAgentService().getPersonUuidAndTitleCache();
        Map<String,Person> titleCachePerson = new HashMap<String, Person>();
        uuids = new HashSet<UUID>();
        for (UuidAndTitleCache<Person> hibernateP:hiberPersons){
            uuids.add(hibernateP.getUuid());
        }

        if (!uuids.isEmpty()){
            List<AgentBase> existingPersons = getAgentService().find(uuids);
            for (AgentBase<?> existingP:existingPersons){
                titleCachePerson.put(existingP.getTitleCache(),CdmBase.deproxy(existingP,Person.class));
            }
        }

        Map<String,UUID> personMap = new HashMap<String, UUID>();
        for (UuidAndTitleCache<Person> person:hiberPersons){
            personMap.put(person.getTitleCache(), person.getUuid());
        }

        java.util.Collection<Person> personToadd = new ArrayList<Person>();
        java.util.Collection<Team> teamToAdd = new ArrayList<Team>();

        for (String collector:collectorsU){
            Person p = Person.NewInstance();
            p.setTitleCache(collector,true);
            if (!personMap.containsKey(p.getTitleCache())){
                personToadd.add(p);
            }
        }
        for (String team:teamsU){
            Team p = Team.NewInstance();
            p.setTitleCache(team,true);
            if (!teamMap.containsKey(p.getTitleCache())){
                teamToAdd.add(p);
            }
        }

        if(!personToadd.isEmpty()){
            for (Person agent: personToadd){
                save(agent, state);
                titleCachePerson.put(agent.getTitleCache(),CdmBase.deproxy(agent, Person.class) );
            }
        }

        Person ptmp ;
        Map <String,Integer>teamdone = new HashMap<String, Integer>();
        for (List<String> collteam: collectorinteams){
            if (!teamdone.containsKey(StringUtils.join(collteam.toArray(),"-"))){
                Team team = new Team();
                boolean em =true;
                for (String collector:collteam){
                    ptmp = Person.NewInstance();
                    ptmp.setTitleCache(collector,true);
                    Person p2 = titleCachePerson.get(ptmp.getTitleCache());
                    team.addTeamMember(p2);
                    em=false;
                }
                if (!em) {
                    teamToAdd.add(team);
                }
                teamdone.put(StringUtils.join(collteam.toArray(),"-"),0);
            }
        }

        if(!teamToAdd.isEmpty()){
            for (Team agent: teamToAdd){
                save(agent, state);
                titleCacheTeam.put(agent.getTitleCache(), CdmBase.deproxy( agent,Team.class) );
            }
        }

        ((Abcd206ImportConfigurator) state.getConfig()).setTeams(titleCacheTeam);
        ((Abcd206ImportConfigurator) state.getConfig()).setPersons(titleCachePerson);
    }

    @Override
    protected boolean isIgnore(Abcd206ImportState state) {
        return false;
    }



}