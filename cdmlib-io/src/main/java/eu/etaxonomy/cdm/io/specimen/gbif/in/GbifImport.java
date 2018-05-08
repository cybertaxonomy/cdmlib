/**
 * Copyright (C) 2016 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen.gbif.in;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.ext.occurrence.gbif.GbifQueryServiceWrapper;
import eu.etaxonomy.cdm.ext.occurrence.gbif.GbifResponse;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportBase;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportConfiguratorBase;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportStateBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author k.luther
 * @since 15.07.2016
 *
 */
@Component
public class GbifImport extends SpecimenImportBase<GbifImportConfigurator, SpecimenImportStateBase<SpecimenImportConfiguratorBase, SpecimenImportStateBase>> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(GbifImport.class);

    @Override
    protected boolean doCheck(SpecimenImportStateBase<SpecimenImportConfiguratorBase, SpecimenImportStateBase> state) {
        logger.warn("Checking not yet implemented for " + this.getClass().getSimpleName());
        return true;
    }






    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.specimen.SpecimenImportBase#doInvoke(eu.etaxonomy.cdm.io.common.ImportStateBase)
     */
    @Override
    protected void doInvoke(SpecimenImportStateBase state) {
       // GbifImportState gbifImportState = (GbifImportState)state;
        SpecimenImportConfiguratorBase config = state.getConfig();

            state.setTx(startTransaction());
            logger.info("INVOKE Specimen Import from Gbif webservice");
            Collection<GbifResponse> results = null;
            //init cd repository
            if(state.getCdmRepository()==null){
                state.setCdmRepository(this);
            }
            if (config.getOccurenceQuery() != null){
                 try {
                    results = new GbifQueryServiceWrapper().query(config.getOccurenceQuery());
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }
            if (results == null){
                logger.info("There were no results for the query: " + config.getOccurenceQuery().toString());
                return;
            }
            List<Reference> references = getReferenceService().listByReferenceTitle(Reference.class, state.getConfig().getSourceReferenceTitle(), MatchMode.LIKE, null, null, null, null, null);
            //List<Reference> references = new ArrayList<Reference>();
            if (state.getRef()==null){
                String name = NB(( state.getConfig()).getSourceReferenceTitle());
                for (Reference reference : references) {
                    if (! StringUtils.isBlank(reference.getTitleCache())) {
                        if (reference.getTitleCache().equalsIgnoreCase(name)) {
                            state.setRef(reference);
                        }
                    }
                }
                if (state.getRef() == null){
                    state.setRef(ReferenceFactory.newGeneric());
                    state.getRef().setTitle(state.getConfig().getSourceReferenceTitle() + " Test ");
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
                Map<String,Classification> classMap = new HashMap<String, Classification>();
                for (Classification tree : classificationList) {
                    if (! StringUtils.isBlank(tree.getTitleCache())) {
                        classMap.put(tree.getTitleCache(),tree);
                    }
                }

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

                }

            }
        }
        String message = "nb units to insert: " + results.size();
        logger.info(message);
        state.getConfig().getProgressMonitor().beginTask("Importing ABCD file", results.size());
        updateProgress(state, message);

        state.setDataHolder(new GbifDataHolder());
        state.getDataHolder().reset();

        for (GbifResponse response:results) {
            if(state.getConfig().getProgressMonitor().isCanceled()){
                break;
            }




            //this.setUnitPropertiesXML( item, abcdFieldGetter, state);
        //   updateProgress(state, "Importing data for unit "+state.getDataHolder().unitID+" ("+i+"/"+unitsList.getLength()+")");

            //import unit + field unit data
            this.handleSingleUnit(state, response);

        }


        commitTransaction(state.getTx());



    }

    @Override
    protected void importAssociatedUnits(
            SpecimenImportStateBase<SpecimenImportConfiguratorBase, SpecimenImportStateBase> state, Object item,
            DerivedUnitFacade derivedUnitFacade) {

        //import associated units
        FieldUnit currentFieldUnit = derivedUnitFacade.innerFieldUnit();
        //TODO: push state (think of implementing stack architecture for state
        DerivedUnit currentUnit = state.getDerivedUnitBase();
        if (currentUnit != null){
        DerivationEvent currentDerivedFrom = currentUnit.getDerivedFrom();
        }
      /*  NodeList unitAssociationList = item.getElementsByTagName(currentPrefix+"UnitAssociation");
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
                                        state.getReport().addInfoMessage(String.format("No derivation event found for unit %s. Defaulting to ACCESSIONING event.",AbcdImportUtility.getUnitID(currentUnit, config)));
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
                                            state.getReport().addInfoMessage("No derivation event found for unit "+AbcdImportUtility.getUnitID(currentUnit, config)+". Defaulting to ACCESIONING event.");
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
        }*/
        //TODO: pop state
        state.reset();
        state.setDerivedUnitBase(currentUnit);

    }


    @Override
    protected void handleSingleUnit(SpecimenImportStateBase<SpecimenImportConfiguratorBase, SpecimenImportStateBase> state,
            Object itemObject){
        GbifResponse item;
        if (itemObject instanceof GbifResponse){
            item = (GbifResponse) itemObject;
        } else{
            logger.error("For Gbif Import the item has to be of type GbifResponse.");
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.info("handleSingleUnit "+state.getRef());
        }

            ICdmRepository cdmAppController = state.getConfig().getCdmAppController();
            if(cdmAppController==null){
                cdmAppController = this;
            }
            //check if unit already exists
            DerivedUnitFacade derivedUnitFacade;
            derivedUnitFacade = item.getDerivedUnitFacade();
            state.setDerivedUnitBase(derivedUnitFacade.innerDerivedUnit());
            TaxonName bestMatchingName =  findBestMatchingNames(item, state);
            if (bestMatchingName == null){
                bestMatchingName = item.getScientificName();
            }
            if (bestMatchingName != null){
                Taxon taxon = getOrCreateTaxonForName(bestMatchingName, state);
                if (state.getConfig().isAddIndividualsAssociationsSuchAsSpecimenAndObservations()) {
                    //do not add IndividualsAssociation to non-preferred taxa
                    if(logger.isDebugEnabled()){
                        logger.info("isDoCreateIndividualsAssociations");
                    }
                    for (DeterminationEvent determinationEvent:derivedUnitFacade.getDeterminations()){
                        makeIndividualsAssociation(state, taxon, determinationEvent);
                    }

                    save(state.getDerivedUnitBase(), state);
                }
            }



            // handle collection data
            handleCollectionData(state, derivedUnitFacade);
            save(item.getDerivedUnitFacade().baseUnit(), state);
            save(item.getDerivedUnitFacade().getFieldUnit(false), state);
            importAssociatedUnits(state, item, derivedUnitFacade);
            /*
            if(state.getConfig().isIgnoreImportOfExistingSpecimens()){
                String[] tripleId = item.getTripleID();
                SpecimenOrObservationBase<?> existingSpecimen = findExistingSpecimen(tripleId[0], state);
                DerivedUnitFacade derivedUnitFacade;
                if(existingSpecimen!=null && existingSpecimen.isInstanceOf(DerivedUnit.class)){
                    DerivedUnit derivedUnit = HibernateProxyHelper.deproxy(existingSpecimen, DerivedUnit.class);
                    state.setDerivedUnitBase(derivedUnit);
                    derivedUnitFacade = item.getDerivedUnitFacade();
                    List<NonViralName> names = findExistingNames(item.getScientificName().getNameCache(), state);
                    if (!names.isEmpty()){
                        findBestMatchingName(names, item);
                    }
                    save(item.getDerivedUnitFacade().baseUnit(), state);
                    importAssociatedUnits(state, item, derivedUnitFacade);
                    state.getReport().addAlreadyExistingSpecimen(SpecimenImportUtility.getUnitID(derivedUnit, state.getConfig()), derivedUnit);
                    return;
                }
            }

            //import new specimen

            // import DNA unit
            //TODO!!!!
            if(state.getDataHolder().getKindOfUnit()!=null && state.getDataHolder().getKindOfUnit().equalsIgnoreCase("dna")){
                GbifDnaParser dnaParser = new GbifDnaParser(state.getPrefix(), state.getReport(), state.getCdmRepository());
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
         //   }

            /**
             * GATHERING EVENT

            // gathering event
            UnitsGatheringEvent<GbifImportConfigurator> unitsGatheringEvent =
                   /* new UnitsGatheringEvent<GbifImportConfigurator>(cdmAppController.getTermService(),
                    state.getDataHolder().locality, null, state.getDataHolder().decimalLongitude,
                    state.getDataHolder().decimalLatitude, state.getDataHolder().getGatheringElevationText(),
                    state.getDataHolder().getGatheringElevationMin(), state.getDataHolder().getGatheringElevationMax(),
                    state.getDataHolder().getGatheringElevationUnit(), state.getDataHolder().getGatheringDateText(),
                    state.getDataHolder().getGatheringNotes(), state.getTransformer().getReferenceSystemByKey(
                            state.getDataHolder().getGatheringSpatialDatum()), state.getDataHolder().gatheringAgentList,
                    state.getDataHolder().gatheringTeamList, state.getConfig());

            // country
            UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea();
            //  unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(), getTermService());
            unitsGatheringArea.setParams(state.getDataHolder().countryCode, state.getDataHolder().country, state.getConfig(), cdmAppController.getTermService(), cdmAppController.getOccurrenceService());

            DefinedTermBase<?> areaCountry =  unitsGatheringArea.getCountry();

            // other areas
            unitsGatheringArea = new UnitsGatheringArea();
            //            unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(),getTermService());
            unitsGatheringArea.setAreas(state.getDataHolder().getNamedAreaList(),state.getConfig(), cdmAppController.getTermService(), cdmAppController.getVocabularyService());
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
                        if(state.getConfig().isAddMediaAsMediaSpecimen()){
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

            //          /*
            //           * merge AND STORE DATA
            //
            //          getTermService().saveOrUpdate(areaCountry);// TODO save area sooner
            //
            //          for (NamedArea area : otherAreas) {
            //              getTermService().saveOrUpdate(area);// merge it sooner (foreach area)
            //          }

            save(unitsGatheringEvent.getLocality(), state);

            // handle collection data
            setCollectionData(state, derivedUnitFacade);

            //Reference stuff
           // SpecimenUserInteraction sui = state.getConfig().getSpecimenUserInteraction();
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

            if( state.getConfig().isInteractWithUser()){
                List<OriginalSourceBase<?>>sources=null;
                if(!state.isDerivedUnitSourcesSet()){
                    sources= sui.askForSource(sourceMap, "the unit itself","",getReferenceService(), state.getDataHolder().docSources);
                    state.setDerivedUnitSources(sources);
                    state.setDerivedUnitSourcesSet(true);
                }
                else{
                    sources=state.getDerivedUnitSources();
                }
    //          System.out.println("nb sources: "+sources.size());
    //          System.out.println("derivedunitfacade : "+derivedUnitFacade.getTitleCache());
                for (OriginalSourceBase<?> sour:sources){
                    if(sour.isInstanceOf(IdentifiableSource.class)){
                        if(sourceNotLinkedToElement(derivedUnitFacade,sour)) {
    //                      System.out.println("add source to derivedunitfacade1 "+derivedUnitFacade.getTitleCache());
                            derivedUnitFacade.addSource((IdentifiableSource)sour.clone());
                        }
                    }else{
                        if(sourceNotLinkedToElement(derivedUnitFacade,sour)) {
    //                      System.out.println("add source to derivedunitfacade2 "+derivedUnitFacade.getTitleCache());
                            derivedUnitFacade.addSource(OriginalSourceType.Import,sour.getCitation(),sour.getCitationMicroReference(), ioName);
                        }
                    }
                }
            }else{
                for (OriginalSourceBase<?> sr : sourceMap.values()){
                    if(sr.isInstanceOf(IdentifiableSource.class)){
                        if(sourceNotLinkedToElement(derivedUnitFacade,sr)) {
    //                      System.out.println("add source to derivedunitfacade3 "+derivedUnitFacade.getTitleCache());
                            derivedUnitFacade.addSource((IdentifiableSource)sr.clone());
                        }
                    }else{
                        if(sourceNotLinkedToElement(derivedUnitFacade,sr)) {
    //                      System.out.println("add source to derivedunitfacade4 "+derivedUnitFacade.getTitleCache());
                            derivedUnitFacade.addSource(OriginalSourceType.Import,sr.getCitation(),sr.getCitationMicroReference(), ioName);
                        }
                    }
                }
            }

            save(item, state);

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
    */
        return;
    }


    /**
     * @param state
     * @param derivedUnitFacade
     */
    private void handleCollectionData(
            SpecimenImportStateBase<SpecimenImportConfiguratorBase, SpecimenImportStateBase> state,
            DerivedUnitFacade derivedUnitFacade) {
       eu.etaxonomy.cdm.model.occurrence.Collection collection = derivedUnitFacade.getCollection();
       if (collection != null) {
           Institution institution = getInstitution(collection.getInstitute().getCode(), state);

           collection = getCollection(institution, collection.getCode(), state);
       }

    }


    /**
     * @param state
     * @param derivedUnitFacade
     */
    private void handleDeterminations(
            SpecimenImportStateBase<SpecimenImportConfiguratorBase, SpecimenImportStateBase> state,
            DerivedUnitFacade derivedUnitFacade) {
        SpecimenImportConfiguratorBase config = state.getConfig();


        String scientificName = "";
        boolean preferredFlag = false;

        if (state.getDataHolder().getNomenclatureCode() == ""){
            state.getDataHolder().setNomenclatureCode(config.getNomenclaturalCode().toString());
        }
        Set<DeterminationEvent> determinations =  derivedUnitFacade.getDeterminations();
        Iterator<DeterminationEvent> determinationIterator = determinations.iterator();
        DeterminationEvent event;
        Taxon taxon;
        TaxonName name ;
        while (determinationIterator.hasNext()) {
            event = determinationIterator.next();
            taxon = (Taxon)event.getTaxon();
            if (taxon == null){
                name = event.getTaxonName();
                if (!name.getTaxa().isEmpty()){
                    taxon = name.getTaxa().iterator().next();
                }
            }
            if (taxon != null){
                addTaxonNode(taxon, state,preferredFlag);
                linkDeterminationEvent(state, taxon, preferredFlag, derivedUnitFacade, null, null);
            }
        }

    }

    /**
     * @param names
     * @param item
     */
    private TaxonName findBestMatchingNames(GbifResponse item, SpecimenImportStateBase state) {
       //TODO
        if (item.getScientificName() != null){

           List<TaxonName> names = findExistingNames(item.getScientificName().getNameCache(), state);
           if (!names.isEmpty()){
               TaxonName result = names.get(0);
               Set<DeterminationEvent> detEvents = item.getDerivedUnitFacade().baseUnit().getDeterminations();
               for (DeterminationEvent event:detEvents){
                   if(event.getTaxonName().getNameCache().equals(result.getNameCache()) ){
                      event.setTaxonName(result);
                   } else{
                       names = findExistingNames(event.getTaxonName().getNameCache(), state);
                       if (!names.isEmpty()){
                           event.setTaxonName(names.get(0));
                       }
                   }
               }
               return result;
           }
        }
       return null;

    }



    /**
     * @param titleCache
     * @param state
     * @return
     */
    private List<TaxonName> findExistingNames(String nameCache, SpecimenImportStateBase state) {
        return getNameService().findNamesByNameCache(nameCache, MatchMode.LIKE, null);
    }



    @Override
    protected boolean isIgnore(SpecimenImportStateBase<SpecimenImportConfiguratorBase, SpecimenImportStateBase> state) {
        return false;
    }



    /*
     * "key": 1257570425,
"datasetKey": "7bd65a7a-f762-11e1-a439-00145eb45e9a",
"publishingOrgKey": "90fd6680-349f-11d8-aa2d-b8a03c50a862",
"publishingCountry": "US",
"protocol": "DWC_ARCHIVE",
"lastCrawled": "2016-06-06T11:11:35.800+0000",
"lastParsed": "2016-03-21T14:11:42.224+0000",
"extensions": { },
"basisOfRecord": "PRESERVED_SPECIMEN",
"individualCount": 1,
"taxonKey": 5338762,
"kingdomKey": 6,
"phylumKey": 7707728,
"classKey": 220,
"orderKey": 412,
"familyKey": 8798,
"genusKey": 2907867,
"speciesKey": 5338762,
"scientificName": "Mitchella repens L.",
"kingdom": "Plantae",
"phylum": "Tracheophyta",
"order": "Gentianales",
"family": "Rubiaceae",
"genus": "Mitchella",
"species": "Mitchella repens",
"genericName": "Mitchella",
"specificEpithet": "repens",
"taxonRank": "SPECIES",
"dateIdentified": "2005-12-31T23:00:00.000+0000",
"decimalLongitude": -98.70693,
"decimalLatitude": 20.77805,
"elevation": 1524.0,
"continent": "NORTH_AMERICA",
"stateProvince": "Hidalgo",
"year": 2006,
"month": 6,
"day": 11,
"eventDate": "2006-06-10T22:00:00.000+0000",
"issues": [

    "COORDINATE_ROUNDED",
    "GEODETIC_DATUM_ASSUMED_WGS84"

],
"lastInterpreted": "2016-04-17T13:34:52.325+0000",
"identifiers": [ ],
"facts": [ ],
"relations": [ ],
"geodeticDatum": "WGS84",
"class": "Magnoliopsida",
"countryCode": "MX",
"country": "Mexico",
"nomenclaturalStatus": "No opinion",
"rightsHolder": "Missouri Botanical Garden",
"identifier": "urn:catalog:MO:Tropicos:100217973",
"recordNumber": "Oberle 274",
"nomenclaturalCode": "ICNafp",
"county": "Metztitlán",
"locality": "Along trail downslope of road between Molango and Zacualtipan.",
"datasetName": "Tropicos",
"gbifID": "1257570425",
"collectionCode": "MO",
"occurrenceID": "urn:catalog:MO:Tropicos:100217973",
"type": "PhysicalObject",
"taxonID": "27902971",
"license": "http://creativecommons.org/licenses/by/4.0/legalcode",
"catalogNumber": "100217973",
"recordedBy": "Brad Oberle",
"institutionCode": "MO",
"ownerInstitutionCode": "MOBOT",
"bibliographicCitation": "http://www.tropicos.org/Specimen/100217973",
"identifiedBy": "B. Oberle",
"collectionID": "http://biocol.org/urn:lsid:biocol.org:col:15859
     *
     */



}
