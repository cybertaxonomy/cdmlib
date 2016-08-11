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
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author p.kelbert
 * @author p.plitzner
 * @created 20.10.2008
 */
@Component
public class Abcd206Import extends SpecimenImportBase<Abcd206ImportConfigurator, Abcd206ImportState> {

    private static final UUID SPECIMEN_SCAN_TERM = UUID.fromString("acda15be-c0e2-4ea8-8783-b9b0c4ad7f03");

    private static final Logger logger = Logger.getLogger(Abcd206Import.class);



    private static final String COLON = ":";

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
                    updateProgress(state, "Importing data for unit "+state.getDataHolder().unitID+" ("+i+"/"+unitsList.getLength()+")");

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
            state.getReport().printReport(((Abcd206ImportConfigurator)state.getConfig()).getReportUri());
        }
        return;
    }

    /**
     * Handle a single unit
     * @param state
     * @param item
     */
    @SuppressWarnings("rawtypes")
    private void handleSingleUnit(Abcd206ImportState state, Element item) {
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
                SpecimenOrObservationBase<?> existingSpecimen = findExistingSpecimen(state.getDataHolder().unitID, state);
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
            if(state.getDataHolder().kindOfUnit!=null && state.getDataHolder().kindOfUnit.equalsIgnoreCase("dna")){
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
                    state.getDataHolder().latitude, state.getDataHolder().gatheringElevationText,
                    state.getDataHolder().gatheringElevationMin, state.getDataHolder().gatheringElevationMax,
                    state.getDataHolder().gatheringElevationUnit, state.getDataHolder().gatheringDateText,
                    state.getDataHolder().gatheringNotes, state.getTransformer().getReferenceSystemByKey(
                            state.getDataHolder().gatheringSpatialDatum), state.getDataHolder().gatheringAgentList,
                    state.getDataHolder().gatheringTeamList, state.getConfig());

            // country
            UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea();
            //  unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(), getTermService());
            unitsGatheringArea.setParams(state.getDataHolder().isocountry, state.getDataHolder().country, (state.getConfig()), cdmAppController.getTermService(), cdmAppController.getOccurrenceService());

            DefinedTermBase<?> areaCountry =  unitsGatheringArea.getCountry();

            // other areas
            unitsGatheringArea = new UnitsGatheringArea();
            //            unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(),getTermService());
            unitsGatheringArea.setAreas(state.getDataHolder().namedAreaList,(state.getConfig()), cdmAppController.getTermService(), cdmAppController.getVocabularyService());
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
            derivedUnitFacade.setFieldNumber(NB(state.getDataHolder().fieldNumber));

            // add unitNotes
            derivedUnitFacade.addAnnotation(Annotation.NewDefaultLanguageInstance(NB(state.getDataHolder().unitNotes)));

            // //add Multimedia URLs
            if (state.getDataHolder().multimediaObjects.size() != -1) {
                for (String multimediaObject : state.getDataHolder().multimediaObjects) {
                    Media media;
                    try {
                        media = getImageMedia(multimediaObject, READ_MEDIA_DATA);
                        derivedUnitFacade.addDerivedUnitMedia(media);
                        if(((Abcd206ImportConfigurator)state.getConfig()).isAddMediaAsMediaSpecimen()){
                            //add media also as specimen scan
                            MediaSpecimen mediaSpecimen = MediaSpecimen.NewInstance(SpecimenOrObservationType.Media);
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

            state.getDataHolder().docSources = new ArrayList<String>();
            for (String[] fullReference : state.getDataHolder().referenceList) {
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
                            state.getDataHolder().docSources.add(sour.getCitation().getTitleCache()+ "---"+sour.getCitationMicroReference());
                        } else {
                            state.getDataHolder().docSources.add(sour.getCitation().getTitleCache());
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
                    sources= sui.askForSource(sourceMap, "the unit itself","",getReferenceService(), state.getDataHolder().docSources);
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
        if (item instanceof Element){
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
                                    handleSingleUnit(state, (Element) associatedUnits.item(m));

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
     * @param sourceMap
     * @param osbSet
     */
    private void addToSourceMap(Map<String, OriginalSourceBase<?>> sourceMap, Set<OriginalSourceBase> osbSet) {
        for( OriginalSourceBase<?> osb:osbSet) {
            if(osb.getCitation()!=null && osb.getCitationMicroReference() !=null  && !osb.getCitationMicroReference().isEmpty()) {
                try{
                    sourceMap.put(osb.getCitation().getTitleCache()+ "---"+osb.getCitationMicroReference(),osb);
                }catch(NullPointerException e){logger.warn("null pointer problem (no ref?) with "+osb);}
            } else if(osb.getCitation()!=null){
                try{
                    sourceMap.put(osb.getCitation().getTitleCache(),osb);
                }catch(NullPointerException e){logger.warn("null pointer problem (no ref?) with "+osb);}
            }
        }
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

    /**
     * @param reference
     * @param citationDetail
     * @return
     */
    //FIXME this method is highly critical, because
    //  * it will have serious performance and memory problems with large databases
    //        (databases may easily have >1 Mio source records)
    //  * it does not make sense to search for existing sources and then clone them
    //    we need to search for existing references instead and use them (if exist)
    //    for our new source.
    private IdentifiableSource getIdentifiableSource(Reference reference, String citationDetail) {

      /*  List<IdentifiableSource> issTmp = getCommonService().list(IdentifiableSource.class, null, null, null, null);


        if (reference != null){
            try {
                for (OriginalSourceBase<?> osb: issTmp){
                    if (osb.getCitation() != null && osb.getCitation().getTitleCache().equalsIgnoreCase(reference.getTitleCache())){
                        String osbDetail = osb.getCitationMicroReference();
                        if ((StringUtils.isBlank(osbDetail) && StringUtils.isBlank(citationDetail))
                                || (osbDetail != null && osbDetail.equalsIgnoreCase(citationDetail)) ) {
//                            System.out.println("REFERENCE FOUND RETURN EXISTING SOURCE");
                            return (IdentifiableSource) osb.clone();
                        }
                    }
                }
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            } catch (Exception e1){
                e1.printStackTrace();
            }
        }
*/
        IdentifiableSource sour = IdentifiableSource.NewInstance(OriginalSourceType.Import,null,null, reference,citationDetail);
        return sour;
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
    private void setCollectionData(Abcd206ImportState state, DerivedUnitFacade derivedUnitFacade) {
        Abcd206ImportConfigurator config = state.getConfig();
        SpecimenImportUtility.setUnitID(derivedUnitFacade.innerDerivedUnit(), state.getDataHolder().unitID, config);
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
    private DerivedUnitFacade getFacade(Abcd206ImportState state) {
        if(DEBUG) {
            logger.info("getFacade()");
        }
        SpecimenOrObservationType type = null;

        // create specimen
        if (NB((state.getDataHolder().recordBasis)) != null) {
            if (state.getDataHolder().recordBasis.toLowerCase().startsWith("s") || state.getDataHolder().recordBasis.toLowerCase().indexOf("specimen")>-1) {// specimen
                type = SpecimenOrObservationType.PreservedSpecimen;
            }
            else if (state.getDataHolder().recordBasis.toLowerCase().startsWith("o") ||state.getDataHolder().recordBasis.toLowerCase().indexOf("observation")>-1 ) {
                type = SpecimenOrObservationType.Observation;
            }
            else if (state.getDataHolder().recordBasis.toLowerCase().indexOf("fossil")>-1){
                type = SpecimenOrObservationType.Fossil;
            }
            else if (state.getDataHolder().recordBasis.toLowerCase().indexOf("living")>-1) {
                type = SpecimenOrObservationType.LivingSpecimen;
            }
            if (type == null) {
                logger.info("The basis of record does not seem to be known: " + state.getDataHolder().recordBasis);
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
            state.getDataHolder().identificationList = new ArrayList<Identification>();
            state.getDataHolder().statusList = new ArrayList<SpecimenTypeDesignationStatus>();
            state.getDataHolder().setAtomisedIdentificationList(new ArrayList<HashMap<String, String>>());
            state.getDataHolder().referenceList = new ArrayList<String[]>();
            state.getDataHolder().multimediaObjects = new ArrayList<String>();

            abcdFieldGetter.getScientificNames(group);
            abcdFieldGetter.getType(root);

            if(DEBUG) {
                logger.info("this.identificationList "+state.getDataHolder().identificationList.toString());
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
                state.getDataHolder().referenceList.add(a);
            }

        } catch (Exception e) {
            logger.info("Error occured while parsing XML file" + e);
        }
    }

    /**
     * Look if the Institution does already exist
     * @param institutionCode: a string with the institutioncode
     * @param config : the configurator
     * @return the Institution (existing or new)
     */
    private Institution getInstitution(String institutionCode, Abcd206ImportState state) {
        Abcd206ImportConfigurator config = state.getConfig();
        Institution institution=null;
        List<Institution> institutions;
        try {
            institutions = getAgentService().list(Institution.class, null, null, null, null);
        } catch (Exception e) {
            institutions = new ArrayList<Institution>();
            logger.warn(e);
        }
        if (institutions.size() > 0 && config.isReUseExistingMetadata()) {
            for (Institution institut:institutions){
                try{
                    if (institut.getCode().equalsIgnoreCase(institutionCode)) {
                        institution=institut;
                    }
                }catch(Exception e){logger.warn("no institution code in the db");}
            }
        }
        if(DEBUG) {
            if(institution !=null) {
                logger.info("getinstitution " + institution.toString());
            }
        }
        if (institution == null){
            // create institution
            institution = Institution.NewInstance();
            institution.setCode(institutionCode);
            institution.setTitleCache(institutionCode, true);
        }
        save(institution, state);
        return institution;
    }

    /**
     * Look if the Collection does already exist
     * @param collectionCode
     * @param collectionCode: a string
     * @param config : the configurator
     * @return the Collection (existing or new)
     */
    private Collection getCollection(Institution institution, String collectionCode, Abcd206ImportState state) {
        Abcd206ImportConfigurator config = state.getConfig();
        Collection collection = null;
        List<Collection> collections;
        try {
            collections = getCollectionService().list(Collection.class, null, null, null, null);
        } catch (Exception e) {
            collections = new ArrayList<Collection>();
        }
        if (collections.size() > 0 && config.isReUseExistingMetadata()) {
            for (Collection coll:collections){
                if (coll.getCode() != null && coll.getInstitute() != null
                        && coll.getCode().equalsIgnoreCase(collectionCode) && coll.getInstitute().equals(institution)) {
                    collection = coll;
                    break;
                }
            }
        }

        if(collection == null){
            collection =Collection.NewInstance();
            collection.setCode(collectionCode);
            collection.setInstitute(institution);
            collection.setTitleCache(collectionCode);
        }
        save(collection, state);
        return collection;
    }


    /**
     * join DeterminationEvent to the Taxon Object
     * @param state : the ABCD import state
     * @param taxon: the current Taxon
     * @param preferredFlag :if the current name is preferred
     * @param derivedFacade : the derived Unit Facade
     */
    @SuppressWarnings("rawtypes")
    private void linkDeterminationEvent(Abcd206ImportState state, Taxon taxon, boolean preferredFlag,  DerivedUnitFacade derivedFacade) {
        Abcd206ImportConfigurator config = state.getConfig();
        if(DEBUG){
            logger.info("start linkdetermination with taxon:" + taxon.getUuid()+", "+taxon);
        }

        DeterminationEvent determinationEvent = DeterminationEvent.NewInstance();
        determinationEvent.setTaxonName(taxon.getName());
        determinationEvent.setPreferredFlag(preferredFlag);

        determinationEvent.setIdentifiedUnit(state.getDerivedUnitBase());
        state.getDerivedUnitBase().addDetermination(determinationEvent);

        if(DEBUG){
            logger.info("NB TYPES INFO: "+ state.getDataHolder().statusList.size());
        }
        for (SpecimenTypeDesignationStatus specimenTypeDesignationstatus : state.getDataHolder().statusList) {
            if (specimenTypeDesignationstatus != null) {
                if(DEBUG){
                    logger.info("specimenTypeDesignationstatus :"+ specimenTypeDesignationstatus);
                }

                ICdmApplicationConfiguration cdmAppController = config.getCdmAppController();
                if(cdmAppController == null){
                    cdmAppController = this;
                }
                specimenTypeDesignationstatus = (SpecimenTypeDesignationStatus) cdmAppController.getTermService().find(specimenTypeDesignationstatus.getUuid());
                //Designation
                TaxonNameBase<?,?> name = taxon.getName();
                SpecimenTypeDesignation designation = SpecimenTypeDesignation.NewInstance();

                designation.setTypeStatus(specimenTypeDesignationstatus);
                designation.setTypeSpecimen(state.getDerivedUnitBase());
                name.addTypeDesignation(designation, true);
            }
        }

        for (String[] fullReference : state.getDataHolder().referenceList) {


            String strReference=fullReference[0];
            String citationDetail = fullReference[1];
            String citationURL = fullReference[2];
            List<Reference> references = getReferenceService().listByTitle(Reference.class, "strReference", MatchMode.EXACT, null, null, null, null, null);

            if (!references.isEmpty()){
                Reference reference = null;
                for (Reference refe: references) {
                    if (refe.getTitleCache().equalsIgnoreCase(strReference)) {
                        reference =refe;
                        break;
                    }
                }
                if (reference ==null){
                    reference = ReferenceFactory.newGeneric();
                    reference.setTitleCache(strReference, true);
                    save(reference, state);
                }
                determinationEvent.addReference(reference);
            }
        }
        save(state.getDerivedUnitBase(), state);

        if (config.isAddIndividualsAssociationsSuchAsSpecimenAndObservations() && preferredFlag) {
            //do not add IndividualsAssociation to non-preferred taxa
            if(DEBUG){
                logger.info("isDoCreateIndividualsAssociations");
            }

            makeIndividualsAssociation(state, taxon, determinationEvent);

            save(state.getDerivedUnitBase(), state);
        }
    }

    /**
     * create and link each association (specimen, observation..) to the accepted taxon
     * @param state : the ABCD import state
     * @param taxon: the current Taxon
     * @param determinationEvent:the determinationevent
     */
    private void makeIndividualsAssociation(Abcd206ImportState state, Taxon taxon, DeterminationEvent determinationEvent) {
        Abcd206ImportConfigurator config = state.getConfig();
        SpecimenUserInteraction sui = config.getSpecimenUserInteraction();

        if (DEBUG) {
            logger.info("MAKE INDIVIDUALS ASSOCIATION");
        }

        TaxonDescription taxonDescription = null;
        Set<TaxonDescription> descriptions= taxon.getDescriptions();
        if (((Abcd206ImportConfigurator) state.getConfig()).isInteractWithUser()){
            if(!state.isDescriptionGroupSet()){
                taxonDescription = sui.askForDescriptionGroup(descriptions);
                state.setDescriptionGroup(taxonDescription);
                state.setDescriptionGroupSet(true);
            }else{
                taxonDescription=state.getDescriptionGroup();
            }
        } else {
            for (TaxonDescription description : descriptions){
                Set<IdentifiableSource> sources =  new HashSet<>();
                sources.addAll(description.getTaxon().getSources());
                sources.addAll(description.getSources());
                for (IdentifiableSource source:sources){
                    if(state.getRef().equals(source.getCitation())) {
                        taxonDescription = description;
                    }
                }
            }
        }
        if (taxonDescription == null){
            taxonDescription = TaxonDescription.NewInstance(taxon, false);
            if(sourceNotLinkedToElement(taxonDescription,state.getRef(),null)) {
                taxonDescription.addSource(OriginalSourceType.Import, null, null, state.getRef(), null);
            }
            state.setDescriptionGroup(taxonDescription);
            taxon.addDescription(taxonDescription);
        }

        //PREPARE REFERENCE QUESTIONS

        Map<String,OriginalSourceBase<?>> sourceMap = new HashMap<String, OriginalSourceBase<?>>();

        List<IdentifiableSource> issTmp = new ArrayList<>();//getCommonService().list(IdentifiableSource.class, null, null, null, null);
        List<DescriptionElementSource> issTmp2 = new ArrayList<>();//getCommonService().list(DescriptionElementSource.class, null, null, null, null);

        Set<OriginalSourceBase> osbSet = new HashSet<OriginalSourceBase>();
        if(issTmp2!=null) {
            osbSet.addAll(issTmp2);
        }
        if(issTmp!=null) {
            osbSet.addAll(issTmp);
        }


        addToSourceMap(sourceMap, osbSet);

        if (((Abcd206ImportConfigurator) state.getConfig()).isInteractWithUser()){
            List<OriginalSourceBase<?>> res = null;
            if(!state.isDescriptionSourcesSet()){
                res = sui.askForSource(sourceMap, "the description group ("+taxon+")",
                        "The current reference is "+state.getRef().getTitleCache(),getReferenceService(), state.getDataHolder().docSources);
                state.setDescriptionRefs(res);
                state.setDescriptionSourcesSet(true);
            }
            else{
                res=state.getDescriptionRefs();
            }
            if(res !=null) {
                for (OriginalSourceBase<?> sour:res){
                    if(sour.isInstanceOf(IdentifiableSource.class)){
                        try {
                            if(sourceNotLinkedToElement(taxonDescription,sour)) {
                                taxonDescription.addSource((IdentifiableSource)sour.clone());
                            }
                        } catch (CloneNotSupportedException e) {
                            logger.warn("no cloning?");
                        }
                    }else{
                        if(sourceNotLinkedToElement(taxonDescription,sour)) {
                            taxonDescription.addSource(OriginalSourceType.Import,null, null, sour.getCitation(),sour.getCitationMicroReference());
                        }
                    }
                }
            }
        }
        else {
            if(sourceNotLinkedToElement(taxonDescription,state.getRef(),null)) {
                taxonDescription.addSource(OriginalSourceType.Import,null, null, state.getRef(), null);
            }
        }
        state.setDescriptionGroup(taxonDescription);

        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();
        Feature feature = makeFeature(state.getDerivedUnitBase());
        indAssociation.setAssociatedSpecimenOrObservation(state.getDerivedUnitBase());
        indAssociation.setFeature(feature);

        if (((Abcd206ImportConfigurator) state.getConfig()).isInteractWithUser()){
            sourceMap = new HashMap<String, OriginalSourceBase<?>>();

            issTmp = getCommonService().list(IdentifiableSource.class, null, null, null, null);
            issTmp2 = getCommonService().list(DescriptionElementSource.class, null, null, null, null);

            osbSet = new HashSet<OriginalSourceBase>();
            if(issTmp2!=null) {
                osbSet.addAll(issTmp2);
            }
            if(issTmp!=null) {
                osbSet.addAll(issTmp);
            }


            addToSourceMap(sourceMap, osbSet);

            List<OriginalSourceBase<?>> sources =null;
            if(!state.isAssociationSourcesSet()) {
                sources = sui.askForSource(sourceMap,  "descriptive element (association) ",taxon.toString(),
                        getReferenceService(),state.getDataHolder().docSources);
                state.setAssociationRefs(sources);
                state.setAssociationSourcesSet(true);
            }
            else{
                sources=state.getAssociationRefs();
            }
            if(sources !=null) {
                for (OriginalSourceBase<?> source: sources) {
                    if(source !=null) {
                        if(source.isInstanceOf(DescriptionElementSource.class)){
                            try {
                                if(sourceNotLinkedToElement(indAssociation,source)) {
                                    indAssociation.addSource((DescriptionElementSource)source.clone());
                                }
                            } catch (CloneNotSupportedException e) {
                                logger.warn("clone forbidden?");
                            }
                        }else{
                            if(sourceNotLinkedToElement(indAssociation,source)) {
                                indAssociation.addSource(OriginalSourceType.Import,null, null, source.getCitation(),source.getCitationMicroReference());
                            }
                            try {
                                if(sourceNotLinkedToElement(state.getDerivedUnitBase(), source)) {
                                    state.getDerivedUnitBase().addSource((IdentifiableSource) source.clone());
                                }
                            } catch (CloneNotSupportedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }
        }else {
            if(sourceNotLinkedToElement(indAssociation,state.getRef(),null)) {
                indAssociation.addSource(OriginalSourceType.Import,null, null, state.getRef(), null);
            }
            if(sourceNotLinkedToElement(state.getDerivedUnitBase(), state.getRef(),null)) {
                state.getDerivedUnitBase().addSource(OriginalSourceType.Import,null, null, state.getRef(), null);
            }
            for (Reference citation : determinationEvent.getReferences()) {
                if(sourceNotLinkedToElement(indAssociation,citation,null))
                {
                    indAssociation.addSource(DescriptionElementSource.NewInstance(OriginalSourceType.Import, null, null, citation, null));
                }
                if(sourceNotLinkedToElement(state.getDerivedUnitBase(), state.getRef(),null)) {
                    state.getDerivedUnitBase().addSource(OriginalSourceType.Import,null, null, state.getRef(), null);
                }
            }
        }

        taxonDescription.addElement(indAssociation);

        save(taxonDescription, state);
        save(taxon, state);
        state.getReport().addDerivate(state.getDerivedUnitBase(), config);
        state.getReport().addIndividualAssociation(taxon, state.getDataHolder().unitID, state.getDerivedUnitBase());
    }

    /**
     * @param derivedUnitBase2
     * @param ref2
     * @param object
     * @return
     */
    private boolean sourceNotLinkedToElement(DerivedUnit derivedUnitBase2, Reference b, String d) {
        Set<IdentifiableSource> linkedSources = derivedUnitBase2.getSources();
        for (IdentifiableSource is:linkedSources){
            Reference a = is.getCitation();
            String c = is.getCitationMicroReference();

            boolean refMatch=false;
            boolean microMatch=false;

            try{
                if (a==null && b==null) {
                    refMatch=true;
                }
                if (a!=null && b!=null) {
                    if (a.getTitleCache().equalsIgnoreCase(b.getTitleCache())) {
                        refMatch=true;
                    }
                }
            }catch(Exception e){}


            try{
                if (c==null && d==null) {
                    microMatch=true;
                }
                if(c!=null && d!=null) {
                    if(c.equalsIgnoreCase(d)) {
                        microMatch=true;
                    }
                }
            }
            catch(Exception e){}

            if (microMatch && refMatch) {
                return false;
            }


        }
        return true;
    }

    /**
     * @param specimen
     * @param source
     * @return
     */
    private boolean sourceNotLinkedToElement(SpecimenOrObservationBase<?> specimen, OriginalSourceBase<?> source) {
        Set<IdentifiableSource> linkedSources = specimen.getSources();
        for (IdentifiableSource is:linkedSources){
            Reference a = is.getCitation();
            Reference b = source.getCitation();
            String c = is.getCitationMicroReference();
            String d = source.getCitationMicroReference();

            boolean refMatch=false;
            boolean microMatch=false;

            try{
                if (a==null && b==null) {
                    refMatch=true;
                }
                if (a!=null && b!=null) {
                    if (a.getTitleCache().equalsIgnoreCase(b.getTitleCache())) {
                        refMatch=true;
                    }
                }
            }catch(Exception e){}


            try{
                if (c==null && d==null) {
                    microMatch=true;
                }
                if(c!=null && d!=null) {
                    if(c.equalsIgnoreCase(d)) {
                        microMatch=true;
                    }
                }
            }
            catch(Exception e){}

            if (microMatch && refMatch) {
                return false;
            }


        }
        return true;
    }

    /**
     * @param indAssociation
     * @param ref2
     * @param object
     * @return
     */
    private boolean sourceNotLinkedToElement(IndividualsAssociation indAssociation, Reference a, String d) {
        Set<DescriptionElementSource> linkedSources = indAssociation.getSources();
        for (DescriptionElementSource is:linkedSources){
            Reference b = is.getCitation();
            String c = is.getCitationMicroReference();

            boolean refMatch=false;
            boolean microMatch=false;

            try{
                if (a==null && b==null) {
                    refMatch=true;
                }
                if (a!=null && b!=null) {
                    if (a.getTitleCache().equalsIgnoreCase(b.getTitleCache())) {
                        refMatch=true;
                    }
                }
            }catch(Exception e){}


            try{
                if (c==null && d==null) {
                    microMatch=true;
                }
                if(c!=null && d!=null) {
                    if(c.equalsIgnoreCase(d)) {
                        microMatch=true;
                    }
                }
            }
            catch(Exception e){}

            if (microMatch && refMatch) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param taxonDescription
     * @param ref2
     * @param object
     * @return
     */
    private boolean sourceNotLinkedToElement(TaxonDescription taxonDescription, Reference a, String d) {
        Set<IdentifiableSource> linkedSources = taxonDescription.getSources();
        for (IdentifiableSource is:linkedSources){
            Reference b = is.getCitation();
            String c = is.getCitationMicroReference();

            boolean refMatch=false;
            boolean microMatch=false;

            try{
                if (a==null && b==null) {
                    refMatch=true;
                }
                if (a!=null && b!=null) {
                    if (a.getTitleCache().equalsIgnoreCase(b.getTitleCache())) {
                        refMatch=true;
                    }
                }
            }catch(Exception e){}


            try{
                if (c==null && d==null) {
                    microMatch=true;
                }
                if(c!=null && d!=null) {
                    if(c.equalsIgnoreCase(d)) {
                        microMatch=true;
                    }
                }
            }
            catch(Exception e){}

            if (microMatch && refMatch) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param indAssociation
     * @param source
     * @return
     */
    private boolean sourceNotLinkedToElement(IndividualsAssociation indAssociation, OriginalSourceBase<?> source) {
        Set<DescriptionElementSource> linkedSources = indAssociation.getSources();
        for (DescriptionElementSource is:linkedSources){
            Reference a = is.getCitation();
            Reference b = source.getCitation();
            String c = is.getCitationMicroReference();
            String d = source.getCitationMicroReference();

            boolean refMatch=false;
            boolean microMatch=false;

            try{
                if (a==null && b==null) {
                    refMatch=true;
                }
                if (a!=null && b!=null) {
                    if (a.getTitleCache().equalsIgnoreCase(b.getTitleCache())) {
                        refMatch=true;
                    }
                }
            }catch(Exception e){}


            try{
                if (c==null && d==null) {
                    microMatch=true;
                }
                if(c!=null && d!=null) {
                    if(c.equalsIgnoreCase(d)) {
                        microMatch=true;
                    }
                }
            }
            catch(Exception e){}

            if (microMatch && refMatch) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param taxonDescription
     * @param sour
     * @return
     */
    private boolean sourceNotLinkedToElement(TaxonDescription taxonDescription, OriginalSourceBase<?> sour) {
        Set<IdentifiableSource> linkedSources = taxonDescription.getSources();
        for (IdentifiableSource is:linkedSources){
            Reference a = is.getCitation();
            Reference b = sour.getCitation();
            String c = is.getCitationMicroReference();
            String d = sour.getCitationMicroReference();

            boolean refMatch=false;
            boolean microMatch=false;

            try{
                if (a==null && b==null) {
                    refMatch=true;
                }
                if (a!=null && b!=null) {
                    if (a.getTitleCache().equalsIgnoreCase(b.getTitleCache())) {
                        refMatch=true;
                    }
                }
            }catch(Exception e){}


            try{
                if (c==null && d==null) {
                    microMatch=true;
                }
                if(c!=null && d!=null) {
                    if(c.equalsIgnoreCase(d)) {
                        microMatch=true;
                    }
                }
            }
            catch(Exception e){}

            if (microMatch && refMatch) {
                return false;
            }


        }
        return true;
    }

    /**
     * look for the Feature object (FieldObs, Specimen,...)
     * @param unit : a specimen or obersvation base
     * @return the corresponding Feature
     */
    private Feature makeFeature(SpecimenOrObservationBase<?> unit) {
        SpecimenOrObservationType type = unit.getRecordBasis();



        if (type.isFeatureObservation()){
            return Feature.OBSERVATION();
        }else if (type.isFeatureSpecimen()){
            return Feature.SPECIMEN();
        }else if (type == SpecimenOrObservationType.DerivedUnit){
            return Feature.OBSERVATION();
            //            return getFeature("Specimen or observation");
        }else{
            String message = "Unhandled record basis '%s' for defining individuals association feature type. Use default.";
            logger.warn(String.format(message, type.getMessage()));
            return Feature.OBSERVATION();
            //            return getFeature("Specimen or observation");

        }
    }

    /**
     * HandleIdentifications : get the scientific names present in the ABCD
     * document and store link them with the observation/specimen data
     * @param state: the current ABCD import state
     * @param derivedUnitFacade : the current derivedunitfacade
     */
    private void handleIdentifications(Abcd206ImportState state, DerivedUnitFacade derivedUnitFacade) {
        Abcd206ImportConfigurator config = state.getConfig();


        String scientificName = "";
        boolean preferredFlag = false;

        if (state.getDataHolder().getNomenclatureCode() == ""){
            state.getDataHolder().setNomenclatureCode(config.getNomenclaturalCode().toString());
        }

        for (int i = 0; i < state.getDataHolder().identificationList.size(); i++) {
            Identification identification = state.getDataHolder().identificationList.get(i);
            scientificName = identification.getScientificName().replaceAll(" et ", " & ");

            String preferred = identification.getPreferred();
            if (preferred.equals("1") || preferred.toLowerCase().indexOf("true") != -1 || state.getDataHolder().identificationList.size()==1) {
                preferredFlag = true;
            }
            else {
                preferredFlag = false;
            }

            if (identification.getCode().indexOf(':') != -1) {
                state.getDataHolder().setNomenclatureCode(identification.getCode().split(COLON)[1]);
            }
            else{
                state.getDataHolder().setNomenclatureCode(identification.getCode());
            }
            TaxonNameBase<?,?> taxonName = getOrCreateTaxonName(scientificName, null, preferredFlag, state, i);
            Taxon taxon = getOrCreateTaxonForName(taxonName, state);
            addTaxonNode(taxon, state,preferredFlag);
            linkDeterminationEvent(state, taxon, preferredFlag, derivedUnitFacade);
        }
    }






    private Taxon getOrCreateTaxonForName(TaxonNameBase<?, ?> taxonNameBase, Abcd206ImportState state){
        Set<Taxon> acceptedTaxa = taxonNameBase.getTaxa();
        if(acceptedTaxa.size()>0){
            Taxon firstAcceptedTaxon = acceptedTaxa.iterator().next();
            if(acceptedTaxa.size()>1){
                String message = "More than one accepted taxon was found for taxon name: "
                        + taxonNameBase.getTitleCache() + "!\n" + firstAcceptedTaxon + "was chosen for "+state.getDerivedUnitBase();
                state.getReport().addInfoMessage(message);
                logger.warn(message);
            }
            else{
                return firstAcceptedTaxon;
            }
        }
        else{
            Set<TaxonBase> taxonAndSynonyms = taxonNameBase.getTaxonBases();
            for (TaxonBase taxonBase : taxonAndSynonyms) {
                if(taxonBase.isInstanceOf(Synonym.class)){
                    Synonym synonym = HibernateProxyHelper.deproxy(taxonBase, Synonym.class);
                    Set<Taxon> acceptedTaxaOfSynonym = synonym.getAcceptedTaxa();
                    if(acceptedTaxaOfSynonym.size()!=1){
                        String message = "No accepted taxa could be found for taxon name: "
                                + taxonNameBase.getTitleCache()
                                + "!\nEither it is a pro parte synonym or has no accepted taxa";
                        state.getReport().addInfoMessage(message);
                        logger.warn(message);
                    }
                    else{
                        return acceptedTaxaOfSynonym.iterator().next();
                    }
                }
            }
        }
        Taxon taxon = Taxon.NewInstance(taxonNameBase, state.getRef());
        save(taxon, state);
        state.getReport().addTaxon(taxon);
        logger.info("Created new taxon "+ taxon);
        return taxon;
    }

    /**
     * @param taxon : a taxon to add as a node
     * @param state : the ABCD import state
     */
    private void addTaxonNode(Taxon taxon, Abcd206ImportState state, boolean preferredFlag) {
        Abcd206ImportConfigurator config = state.getConfig();
        logger.info("link taxon to a taxonNode "+taxon.getTitleCache());
        //only add nodes if not already existing in current classification or default classification

        //check if node exists in current classification
        //NOTE: we cannot use hasTaxonNodeInClassification() here because we are first creating it here
        if (!existsInClassification(taxon, state.getClassification())){
            if(config.isMoveNewTaxaToDefaultClassification()){
                //check if node exists in default classification
                if(!existsInClassification(taxon, state.getDefaultClassification())){
                    addParentTaxon(taxon, state, preferredFlag, state.getDefaultClassification());
                }
            }
            else {
                //add non-existing taxon to current classification
                addParentTaxon(taxon, state, preferredFlag, state.getClassification());
            }
        }
    }

    private boolean existsInClassification(Taxon taxon, Classification classification){
        boolean exist = false;
        Set<TaxonNode> allNodes = classification.getAllNodes();
        for (TaxonNode p : allNodes){
            try{
                if(p.getTaxon().equals(taxon)) {
                    exist = true;
                }
            }
            catch(Exception e){
                logger.warn("TaxonNode doesn't seem to have a taxon");
            }
        }
        return exist;
    }

    private boolean hasTaxonNodeInClassification(Taxon taxon, Classification classification){
        if(taxon.getTaxonNodes()!=null){
            for (TaxonNode node : taxon.getTaxonNodes()){
                if(node.getClassification().equals(classification)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Add the hierarchy for a Taxon(add higher taxa)
     * @param classification
     * @param taxon: a taxon to add as a node
     * @param state: the ABCD import state
     */
    private void addParentTaxon(Taxon taxon, Abcd206ImportState state, boolean preferredFlag, Classification classification){
        NonViralName<?>  nvname = CdmBase.deproxy(taxon.getName(), NonViralName.class);
        Rank rank = nvname.getRank();
        Taxon genus =null;
        Taxon subgenus =null;
        Taxon species = null;
        Taxon subspecies = null;
        Taxon parent = null;
        if(rank!=null){
            if (rank.isLower(Rank.GENUS() )){
                String genusOrUninomial = nvname.getGenusOrUninomial();
                TaxonNameBase<?,?> taxonName = getOrCreateTaxonName(genusOrUninomial, Rank.GENUS(), preferredFlag, state, -1);
                genus = getOrCreateTaxonForName(taxonName, state);
                if (preferredFlag) {
                    parent = linkParentChildNode(null, genus, classification, state);
                }

            }
            if (rank.isLower(Rank.SUBGENUS())){
                String prefix = nvname.getGenusOrUninomial();
                String name = nvname.getInfraGenericEpithet();
                if (name != null){
                    TaxonNameBase<?,?> taxonName = getOrCreateTaxonName(prefix+" "+name, Rank.SUBGENUS(), preferredFlag, state, -1);
                    subgenus = getOrCreateTaxonForName(taxonName, state);
                    if (preferredFlag) {
                        parent = linkParentChildNode(genus, subgenus, classification, state);
                    }            }
            }
            if (rank.isLower(Rank.SPECIES())){
                if (subgenus!=null){
                    String prefix = nvname.getGenusOrUninomial();
                    String name = nvname.getInfraGenericEpithet();
                    String spe = nvname.getSpecificEpithet();
                    if (spe != null){
                        TaxonNameBase<?,?> taxonName = getOrCreateTaxonName(prefix+" "+name+" "+spe, Rank.SPECIES(), preferredFlag, state, -1);
                        species = getOrCreateTaxonForName(taxonName, state);
                        if (preferredFlag) {
                            parent = linkParentChildNode(subgenus, species, classification, state);
                        }
                    }
                }
                else{
                    String prefix = nvname.getGenusOrUninomial();
                    String name = nvname.getSpecificEpithet();
                    if (name != null){
                        TaxonNameBase<?,?> taxonName = getOrCreateTaxonName(prefix+" "+name, Rank.SPECIES(), preferredFlag, state, -1);
                        species = getOrCreateTaxonForName(taxonName, state);
                        if (preferredFlag) {
                            parent = linkParentChildNode(genus, species, classification, state);
                        }
                    }
                }
            }
            if (rank.isLower(Rank.INFRASPECIES())){
                TaxonNameBase<?,?> taxonName = getOrCreateTaxonName(nvname.getFullTitleCache(), Rank.SUBSPECIES(), preferredFlag, state, -1);
                subspecies = getOrCreateTaxonForName(taxonName, state);
                if (preferredFlag) {
                    parent = linkParentChildNode(species, subspecies, classification, state);
                }
            }
        }
        if (preferredFlag && parent!=taxon) {
            linkParentChildNode(parent, taxon, classification, state);
        }
    }

    /**
     * Link a parent to a child and save it in the current classification
     * @param parent: the higher Taxon
     * @param child : the lower (or current) Taxon
     * return the Taxon from the new created Node
     * @param classification
     * @param state
     */
    private Taxon linkParentChildNode(Taxon parent, Taxon child, Classification classification, Abcd206ImportState state) {
        TaxonNode node =null;
        if (parent != null) {
            parent = (Taxon) getTaxonService().find(parent.getUuid());
            child = (Taxon) getTaxonService().find(child.getUuid());
            //here we do not have to check if the taxon nodes already exists
            //this is done by classification.addParentChild()
            //do not add child node if it already exists
            if(hasTaxonNodeInClassification(child, classification)){
                return child;
            }
            else{
                node = classification.addParentChild(parent, child, state.getRef(), "");
                save(classification, state);
            }
        }
        else {
            child = (Taxon) getTaxonService().find(child.getUuid());
            //do not add child node if it already exists
            if(hasTaxonNodeInClassification(child, classification)){
                return child;
            }
            else{
                node = classification.addChildTaxon(child, state.getRef(), null);
                save(classification, state);
            }
        }
        if(node!=null){
            state.getReport().addTaxonNode(node);
            return node.getTaxon();
        }
        String message = "Could not create taxon node for " +child;
        state.getReport().addInfoMessage(message);
        logger.warn(message);
        return null;
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

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.specimen.SpecimenImportBase#handleSingleUnit(eu.etaxonomy.cdm.io.specimen.SpecimenImportStateBase, java.lang.Object)
     */
    @Override
    protected void handleSingleUnit(Abcd206ImportState state, Object item) {
        // TODO Auto-generated method stub

    }

}