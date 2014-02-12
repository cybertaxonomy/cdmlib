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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.ICdmApplicationDefaultConfiguration;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportBase;
import eu.etaxonomy.cdm.io.specimen.SpecimenUserInteraction;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
//=======
import eu.etaxonomy.cdm.model.common.LanguageString;
//<<<<<<< .courant
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
//>>>>>>> .fusion-droit.r19057
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author p.kelbert
 * @created 20.10.2008
 */
@Component
public class Abcd206Import extends SpecimenImportBase<Abcd206ImportConfigurator, Abcd206ImportState> {
    private static final Logger logger = Logger.getLogger(Abcd206Import.class);


    private final boolean DEBUG = true;

    private static final String PREFERRED = "_preferred_";
    private static final String CODE = "_code_";
    private static final String COLON = ":";
    private static final String SPLITTER = "--";





    private static String prefix = "";

    //TODO make all fields ABCD206ImportState variables
    private Classification classification = null;
    private Reference<?> ref = null;

    private Abcd206DataHolder dataHolder;
    private DerivedUnit derivedUnitBase;

    private List<OriginalSourceBase<?>> associationRefs = new ArrayList<OriginalSourceBase<?>>();
    boolean associationSourcesSet=false;
    private List<OriginalSourceBase<?>> descriptionRefs = new ArrayList<OriginalSourceBase<?>>();
    boolean descriptionSourcesSet=false;
    private List<OriginalSourceBase<?>> derivedUnitSources = new ArrayList<OriginalSourceBase<?>>();
    boolean derivedUnitSourcesSet=false;
    private boolean descriptionGroupSet = false;
    private TaxonDescription descriptionGroup = null;

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
        state.setTx(startTransaction());
        logger.info("INVOKE Specimen Import from ABCD2.06 XML ");

        SpecimenUserInteraction sui = state.getConfig().getSpecimenUserInteraction();

        List<Reference> references = getReferenceService().list(Reference.class, null, null, null, null);

        if (state.getConfig().isInteractWithUser()){
            Map<String,Reference> refMap = new HashMap<String, Reference>();
            for (Reference tree : references) {
                if (! StringUtils.isBlank(tree.getTitleCache())) {
                    refMap.put(tree.getTitleCache(),tree);
                }
            }
            ref = sui.askForReference(refMap);

            if (ref == null){
                String cla = sui.createNewReference();
                if (refMap.get(cla)!= null) {
                    ref = refMap.get(cla);
                } else {
                    ref = ReferenceFactory.newGeneric();
                    ref.setTitle(cla);
                }
            }
            else{
                ref = getReferenceService().find(ref.getUuid());
            }
        }else{
            if (ref==null){
                String name = NB(state.getConfig().getSourceReferenceTitle());
                for (Reference tree : references) {
                    if (! StringUtils.isBlank(tree.getTitleCache())) {
                        if (tree.getTitleCache().equalsIgnoreCase(name)) {
                            ref=tree;
//                            System.out.println("FIND SAME REFERENCE");
                        }
                    }
                }
                if (ref == null){
                    ref = ReferenceFactory.newGeneric();
                    ref.setTitle("ABCD classic");
                }
            }
        }
        save(ref, state);
        state.getConfig().setSourceReference(ref);

        List<Classification> classificationList = getClassificationService().list(Classification.class, null, null, null, null);
        if (state.getConfig().isUseClassification() && state.getConfig().isInteractWithUser()){
            Map<String,Classification> classMap = new HashMap<String, Classification>();
            for (Classification tree : classificationList) {
                if (! StringUtils.isBlank(tree.getTitleCache())) {
                    classMap.put(tree.getTitleCache(),tree);
                }
            }
            classification = sui.askForClassification(classMap);
            if (classification == null){
                String cla = sui.createNewClassification();
                if (classMap.get(cla)!= null) {
                    classification = classMap.get(cla);
                } else {
                    classification = Classification.NewInstance(cla, ref, Language.DEFAULT());
                }
            }
            save(classification, state);
        }
        else{
            if (classification == null) {
                String name = NB(state.getConfig().getClassificationName());
                for (Classification classif : classificationList){
                    if (classif.getTitleCache().equalsIgnoreCase(name) && classif.getCitation().equals(ref)) {
                        classification=classif;
//                        System.out.println("FIND SAME CLASSIF");
                    }
                }
                if (classification == null){
                    classification = Classification.NewInstance(name, ref, Language.DEFAULT());
                }
                //                if (state.getConfig().getClassificationUuid() != null) {
                //                    classification.setUuid(state.getConfig().getClassificationUuid());
                //                }
                save(classification, state);
            }
        }

        InputStream source = state.getConfig().getSource();
        NodeList unitsList = getUnitsNodeList(source);

        if (unitsList != null) {
            String message = "nb units to insert: " + unitsList.getLength();
            logger.info(message);
            updateProgress(state, message);

            dataHolder = new Abcd206DataHolder();

            Abcd206XMLFieldGetter abcdFieldGetter = new Abcd206XMLFieldGetter(dataHolder, prefix);

            prepareCollectors(state, unitsList, abcdFieldGetter);

            associationRefs = new ArrayList<OriginalSourceBase<?>>();
            descriptionRefs = new ArrayList<OriginalSourceBase<?>>();
            derivedUnitSources = new ArrayList<OriginalSourceBase<?>>();

            for (int i = 0; i < unitsList.getLength(); i++) {
                System.out.println("------------------------------------------------------------------------------------------");

                this.setUnitPropertiesXML( (Element) unitsList.item(i), abcdFieldGetter);
                //				refreshTransaction(state);
                this.handleSingleUnit(state);

                // compare the ABCD elements added in to the CDM and the
                // unhandled ABCD elements
                //compareABCDtoCDM(sourceName, dataHolder.knownABCDelements, abcdFieldGetter);

                // reset the ABCD elements added in CDM
                // knownABCDelements = new ArrayList<String>();
                dataHolder.allABCDelements = new HashMap<String, String>();
            }
            getReferenceService().deduplicate(Reference.class, null, null);
            getClassificationService().deduplicate(Classification.class, null, null);
        }
        commitTransaction(state.getTx());
        return;

    }


    protected NodeList getUnitsNodeList(URI source) {
        try {
            InputStream is = UriUtils.getInputStream(source);
            return getUnitsNodeList(is);
        } catch (Exception e) {
            logger.warn(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the list of root nodes for an ABCD 2.06 XML file
     * @param fileName: the file's location
     * @return the list of root nodes ("Unit")
     */
    protected NodeList getUnitsNodeList(InputStream inputStream) {
        NodeList unitList = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(inputStream);
            Element root = document.getDocumentElement();
            unitList = root.getElementsByTagName("Unit");
            if (unitList.getLength() == 0) {
                unitList = root.getElementsByTagName("abcd:Unit");
                prefix = "abcd:";
            }
        } catch (Exception e) {
            logger.warn(e);
        }
        return unitList;
    }

    /**
     * Handle a single unit
     * @param state
     */
    @SuppressWarnings("rawtypes")
    private void handleSingleUnit(Abcd206ImportState state) {
        if (DEBUG) {
            logger.info("handleSingleUnit "+ref);
        }
        try {
            updateProgress(state, "Importing data for unit: " + dataHolder.unitID);

            // create facade
            DerivedUnitFacade derivedUnitFacade = getFacade();
            derivedUnitBase = derivedUnitFacade.innerDerivedUnit();

            /**
             * GATHERING EVENT
             */
            // gathering event
            UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(getTermService(), dataHolder.locality, dataHolder.languageIso,
                    dataHolder.longitude, dataHolder.latitude, dataHolder.gatheringAgentList, dataHolder.gatheringTeamList,state.getConfig());

            // country
            UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea();
            //  unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(), getTermService());
            unitsGatheringArea.setParams(dataHolder.isocountry, dataHolder.country, state.getConfig(), getTermService(), getOccurrenceService());

            DefinedTermBase<?> areaCountry =  unitsGatheringArea.getCountry();

            // other areas
            unitsGatheringArea = new UnitsGatheringArea();
            //            unitsGatheringArea.setConfig(state.getConfig(),getOccurrenceService(),getTermService());
            unitsGatheringArea.setAreas(dataHolder.namedAreaList,state.getConfig(), getTermService());
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

            for(DefinedTermBase<?> area:unitsGatheringArea.getAreas()){
                derivedUnitFacade.addCollectingArea((NamedArea) area);
            }
            //            derivedUnitFacade.addCollectingAreas(unitsGatheringArea.getAreas());
            // TODO exsiccatum

            // add fieldNumber
            derivedUnitFacade.setFieldNumber(NB(dataHolder.fieldNumber));

            // //add Multimedia URLs
            if (dataHolder.multimediaObjects.size() != -1) {
                for (String multimediaObject : dataHolder.multimediaObjects) {
                    Media media;
                    try {
                        media = getImageMedia(multimediaObject, READ_MEDIA_DATA, false);
                        derivedUnitFacade.addDerivedUnitMedia(media);
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
            SpecimenUserInteraction sui = state.getConfig().getSpecimenUserInteraction();
            Map<String,OriginalSourceBase<?>> sourceMap = new HashMap<String, OriginalSourceBase<?>>();

            dataHolder.docSources = new ArrayList<String>();
            for (String[] fullReference : dataHolder.referenceList) {
                String strReference=fullReference[0];
                String citationDetail = fullReference[1];
                String citationURL = fullReference[2];

                if (!citationURL.isEmpty()) {
                    citationDetail+=", "+citationURL;
                }

                Reference<?> reference = ReferenceFactory.newGeneric();
                reference.setTitleCache(strReference);

                IdentifiableSource sour = getIdentifiableSource(reference,citationDetail);

                try{
                    if (sour.getCitation() != null){
                        if(StringUtils.isNotBlank(sour.getCitationMicroReference())) {
                            dataHolder.docSources.add(sour.getCitation().getTitleCache()+ "---"+sour.getCitationMicroReference());
                        } else {
                            dataHolder.docSources.add(sour.getCitation().getTitleCache());
                        }
                    }
                }catch(Exception e){
                    logger.warn("oups");
                }
                reference.addSource(sour);
                getReferenceService().saveOrUpdate(reference);
            }


            List<OriginalSourceBase> issTmp = getCommonService().list(IdentifiableSource.class, null, null, null, null);
            List<OriginalSourceBase> issTmp2 = getCommonService().list(DescriptionElementSource.class, null, null, null, null);

            Set<OriginalSourceBase> osbSet = new HashSet<OriginalSourceBase>();
            if(issTmp2!=null) {
                osbSet.addAll(issTmp2);
            }
            if(issTmp!=null) {
                osbSet.addAll(issTmp);
            }


            for( OriginalSourceBase<?> osb:osbSet) {
                if(osb.getCitationMicroReference() !=null  && !osb.getCitationMicroReference().isEmpty()) {
                    try{
                        sourceMap.put(osb.getCitation().getTitleCache()+ "---"+osb.getCitationMicroReference(),osb);
                    }catch(NullPointerException e){logger.warn("null pointer problem (no ref?) with "+osb);}
                } else{
                    try{
                        sourceMap.put(osb.getCitation().getTitleCache(),osb);
                    }catch(NullPointerException e){logger.warn("null pointer problem (no ref?) with "+osb);}
                }
            }

            if( state.getConfig().isInteractWithUser()){
                List<OriginalSourceBase<?>>sources=null;
                if(!derivedUnitSourcesSet){
                    sources= sui.askForSource(sourceMap, "the unit itself","",getReferenceService(), dataHolder.docSources);
                    derivedUnitSources=sources;
                    derivedUnitSourcesSet=true;
                }
                else{
                    sources=derivedUnitSources;
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

            getOccurrenceService().saveOrUpdate(derivedUnitBase);

            //=======
            save(derivedUnitBase, state);

            // handle identifications
            handleIdentifications(state, derivedUnitFacade);

            if(DEBUG) {
                logger.info("saved ABCD specimen ...");
            }

        } catch (Exception e) {
            logger.warn("Error when reading record!!");
            e.printStackTrace();
            state.setUnsuccessfull();
        }

        return;
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
    private IdentifiableSource getIdentifiableSource(Reference<?> reference, String citationDetail) {

        List<OriginalSourceBase> issTmp = getCommonService().list(IdentifiableSource.class, null, null, null, null);


        if (reference != null){
            try {
                for (OriginalSourceBase<?> osb: issTmp){
                    if (osb.getCitation() != null && reference!=null && osb.getCitation().getTitleCache().equalsIgnoreCase(reference.getTitleCache())){
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

        IdentifiableSource sour = IdentifiableSource.NewInstance(OriginalSourceType.Import,null,null, reference,citationDetail);
        return sour;
    }

    //    /**
    //     * @param reference
    //     * @param citationDetail
    //     * @return
    //     */
    //    private DescriptionElementSource getDescriptionSource(Reference<?> reference, String citationDetail) {
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
     * Very fast and dirty implementation to allow handling of transient objects as described in
     * https://dev.e-taxonomy.eu/trac/ticket/3726
     *
     * Not yet complete.
     *
     * @param cdmBase
     * @param state
     */
    private void save(CdmBase cdmBase, Abcd206ImportState state) {
        ICdmApplicationDefaultConfiguration cdmRepository = state.getConfig().getCdmAppController();
        if (cdmRepository == null){
            cdmRepository = this;
        }

        if (cdmBase.isInstanceOf(LanguageString.class)){
            cdmRepository.getTermService().saveLanguageData(CdmBase.deproxy(cdmBase, LanguageString.class));
        }else if (cdmBase.isInstanceOf(SpecimenOrObservationBase.class)){
            cdmRepository.getOccurrenceService().saveOrUpdate(CdmBase.deproxy(cdmBase, SpecimenOrObservationBase.class));
        }else if (cdmBase.isInstanceOf(Reference.class)){
            cdmRepository.getReferenceService().saveOrUpdate(CdmBase.deproxy(cdmBase, Reference.class));
        }else if (cdmBase.isInstanceOf(Classification.class)){
            cdmRepository.getClassificationService().saveOrUpdate(CdmBase.deproxy(cdmBase, Classification.class));
        }else if (cdmBase.isInstanceOf(AgentBase.class)){
            cdmRepository.getAgentService().saveOrUpdate(CdmBase.deproxy(cdmBase, AgentBase.class));
        }else if (cdmBase.isInstanceOf(Collection.class)){
            cdmRepository.getCollectionService().saveOrUpdate(CdmBase.deproxy(cdmBase, Collection.class));
        }else if (cdmBase.isInstanceOf(DescriptionBase.class)){
            cdmRepository.getDescriptionService().saveOrUpdate(CdmBase.deproxy(cdmBase, DescriptionBase.class));
        }else if (cdmBase.isInstanceOf(TaxonBase.class)){
            cdmRepository.getTaxonService().saveOrUpdate(CdmBase.deproxy(cdmBase, TaxonBase.class));
        }else if (cdmBase.isInstanceOf(TaxonNameBase.class)){
            cdmRepository.getNameService().saveOrUpdate(CdmBase.deproxy(cdmBase, TaxonNameBase.class));
        }else{
            throw new IllegalArgumentException("Class not supported in save method: " + CdmBase.deproxy(cdmBase, CdmBase.class).getClass().getSimpleName());
        }

    }

    /**
     * setCollectionData : store the collection object into the
     * derivedUnitFacade
     *
     * @param state
     */
    private void setCollectionData(Abcd206ImportState state, DerivedUnitFacade derivedUnitFacade) {
        // set catalogue number (unitID)
        derivedUnitFacade.setCatalogNumber(NB(dataHolder.unitID));
        derivedUnitFacade.setAccessionNumber(NB(dataHolder.accessionNumber));
        // derivedUnitFacade.setCollectorsNumber(NB(dataHolder.collectorsNumber));

        /*
         * INSTITUTION & COLLECTION
         */
        // manage institution
        Institution institution = this.getInstitution(NB(dataHolder.institutionCode), state);
        // manage collection
        Collection collection = this.getCollection(institution, NB(dataHolder.collectionCode), state);
        // link specimen & collection
        derivedUnitFacade.setCollection(collection);
    }

    /**
     * getFacade : get the DerivedUnitFacade based on the recordBasis
     *
     * @return DerivedUnitFacade
     */
    private DerivedUnitFacade getFacade() {
        if(DEBUG) {
            logger.info("getFacade()");
        }
        SpecimenOrObservationType type = null;

        // create specimen
        if (NB((dataHolder.recordBasis)) != null) {
            if (dataHolder.recordBasis.toLowerCase().startsWith("s") || dataHolder.recordBasis.toLowerCase().indexOf("specimen")>-1) {// specimen
                type = SpecimenOrObservationType.PreservedSpecimen;
            }
            if (dataHolder.recordBasis.toLowerCase().startsWith("o") ||dataHolder.recordBasis.toLowerCase().indexOf("observation")>-1 ) {
                type = SpecimenOrObservationType.Observation;
            }
            if (dataHolder.recordBasis.toLowerCase().indexOf("fossil")>-1){
                type = SpecimenOrObservationType.Fossil;
            }
            if (dataHolder.recordBasis.toLowerCase().indexOf("living")>-1) {
                type = SpecimenOrObservationType.LivingSpecimen;
            }
            if (type == null) {
                logger.info("The basis of record does not seem to be known: " + dataHolder.recordBasis);
                type = SpecimenOrObservationType.DerivedUnit;
            }
            // TODO fossils?
        } else {
            logger.info("The basis of record is null");
            type = SpecimenOrObservationType.DerivedUnit;
        }
        DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(type);
        return derivedUnitFacade;
    }

    private void getCollectorsFromXML(Element root, Abcd206XMLFieldGetter abcdFieldGetter) {
        NodeList group;

        group = root.getChildNodes();
        for (int i = 0; i < group.getLength(); i++) {
            if (group.item(i).getNodeName().equals(prefix + "Identifications")) {
                group = group.item(i).getChildNodes();
                break;
            }
        }
        dataHolder.gatheringAgentList = new ArrayList<String>();
        dataHolder.gatheringTeamList = new ArrayList<String>();
        abcdFieldGetter.getType(root);
        abcdFieldGetter.getGatheringPeople(root);
    }

    /**
     * Store the unit's properties into variables Look which unit is the
     * preferred one Look what kind of name it is supposed to be, for the
     * parsing (Botanical, Zoological)
     *
     * @param racine: the root node for a single unit
     */
    private void setUnitPropertiesXML(Element root, Abcd206XMLFieldGetter abcdFieldGetter) {
        try {
            NodeList group;

            group = root.getChildNodes();
            for (int i = 0; i < group.getLength(); i++) {
                if (group.item(i).getNodeName().equals(prefix + "Identifications")) {
                    group = group.item(i).getChildNodes();
                    break;
                }
            }
            dataHolder.identificationList = new ArrayList<String>();
            dataHolder.statusList = new ArrayList<SpecimenTypeDesignationStatus>();
            dataHolder.atomisedIdentificationList = new ArrayList<HashMap<String, String>>();
            dataHolder.referenceList = new ArrayList<String[]>();
            dataHolder.multimediaObjects = new ArrayList<String>();

            abcdFieldGetter.getScientificNames(group);
            abcdFieldGetter.getType(root);

            if(DEBUG) {
                logger.info("this.identificationList "+dataHolder.identificationList.toString());
            }
            abcdFieldGetter.getIDs(root);
            abcdFieldGetter.getRecordBasis(root);
            abcdFieldGetter.getMultimedia(root);
            abcdFieldGetter.getNumbers(root);
            abcdFieldGetter.getGeolocation(root);
            abcdFieldGetter.getGatheringPeople(root);
            boolean referencefound = abcdFieldGetter.getReferences(root);
            if (!referencefound) {
                String[]a = {ref.getTitleCache(),"",""};
                dataHolder.referenceList.add(a);
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
    @SuppressWarnings("rawtypes")
    private Institution getInstitution(String institutionCode, Abcd206ImportState state) {
        Institution institution=null;
        List<AgentBase> institutions;
        try {
            institutions = getAgentService().list(Institution.class, null, null, null, null);
        } catch (Exception e) {
            institutions = new ArrayList<AgentBase>();
            logger.warn(e);
        }
        if (institutions.size() > 0 && state.getConfig().isReUseExistingMetadata()) {
            for (AgentBase inst:institutions){
                Institution institut = (Institution)inst;
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
            institution.setTitleCache(institutionCode);
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
        Collection collection = null;
        List<Collection> collections;
        try {
            collections = getCollectionService().list(Collection.class, null, null, null, null);
        } catch (Exception e) {
            collections = new ArrayList<Collection>();
        }
        if (collections.size() > 0 && state.getConfig().isReUseExistingMetadata()) {
            for (Collection coll:collections){
                if (coll.getInstitute() != null) {
                    if (coll.getCode().equalsIgnoreCase(collectionCode) && coll.getInstitute().equals(institution)) {
                        collection=coll;
                    }
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
        determinationEvent.setTaxon(taxon);
        determinationEvent.setPreferredFlag(preferredFlag);

        determinationEvent.setIdentifiedUnit(derivedUnitBase);
        derivedUnitBase.addDetermination(determinationEvent);

        try {
            if(DEBUG){
                logger.info("NB TYPES INFO: "+ dataHolder.statusList.size());
            }
            for (SpecimenTypeDesignationStatus specimenTypeDesignationstatus : dataHolder.statusList) {
                if (specimenTypeDesignationstatus != null) {
                    if(DEBUG){
                        logger.info("specimenTypeDesignationstatus :"+ specimenTypeDesignationstatus);
                    }

                    specimenTypeDesignationstatus = (SpecimenTypeDesignationStatus) getTermService().find(specimenTypeDesignationstatus.getUuid());
                    //Designation
                    TaxonNameBase<?,?> name = taxon.getName();
                    SpecimenTypeDesignation designation = SpecimenTypeDesignation.NewInstance();

                    designation.setTypeStatus(specimenTypeDesignationstatus);
                    designation.setTypeSpecimen(derivedUnitBase);
                    name.addTypeDesignation(designation, true);
                }
            }
        } catch (Exception e) {
            logger.warn("PB addding SpecimenType " + e);
        }

        for (String[] fullReference : dataHolder.referenceList) {
            try{
//                System.out.println(fullReference);
                List<Reference> references = getReferenceService().list(Reference.class, null, null, null, null);

                String strReference=fullReference[0];
                String citationDetail = fullReference[1];
                String citationURL = fullReference[2];

                if (isNotBlank(strReference)){
                    Reference<?> reference = null;
                    for (Reference<?> refe: references) {
                        if (refe.getTitleCache().equalsIgnoreCase(strReference)) {
                            reference =refe;
                        }
                    }
                    if (reference ==null){
                        reference = ReferenceFactory.newGeneric();
                        /*<<<<<<< .courant
                    reference.setTitleCache(strReference);
                    System.out.println("reference hasproblem2 "+reference.hasProblem());
                    IdentifiableSource sour = IdentifiableSource.NewInstance(reference,citationDetail);
                    getReferenceService().saveOrUpdate(sour.getCitation());
=======*/
                        reference.setTitleCache(strReference, true);
                        save(reference, state);
                    }
                    determinationEvent.addReference(reference);
                }
            }catch(Exception e){logger.warn("pv getReferenceList "+e);}
        }
        save(derivedUnitBase, state);

        if (config.isAddIndividualsAssociationsSuchAsSpecimenAndObservations()) {
            if(DEBUG){
                logger.info("isDoCreateIndividualsAssociations");
            }

            makeIndividualsAssociation(state, taxon, determinationEvent);
            save(derivedUnitBase, state);
        }
    }

    /**
     * create and link each association (specimen, observation..) to the accepted taxon
     * @param state : the ABCD import state
     * @param taxon: the current Taxon
     * @param determinationEvent:the determinationevent
     */
    @SuppressWarnings("unused")
    private void makeIndividualsAssociation(Abcd206ImportState state, Taxon taxon, DeterminationEvent determinationEvent) {
        SpecimenUserInteraction sui = state.getConfig().getSpecimenUserInteraction();

        if (DEBUG) {
            System.out.println("MAKE INDIVIDUALS ASSOCIATION");
        }

        TaxonDescription taxonDescription = null;
        Set<TaxonDescription> descriptions= taxon.getDescriptions();
        if (state.getConfig().isInteractWithUser()){
            if(!descriptionGroupSet){
                taxonDescription = sui.askForDescriptionGroup(descriptions);
                descriptionGroup=taxonDescription;
                descriptionGroupSet=true;
            }else{
                taxonDescription=descriptionGroup;
            }
        } else {
            for (TaxonDescription description : descriptions){
                Set<IdentifiableSource> sources =  description.getTaxon().getSources();
                sources.addAll(description.getSources());
                for (IdentifiableSource source:sources){
                    if(ref.equals(source.getCitation())) {
                        taxonDescription = description;
                    }
                }
            }
        }
        if (taxonDescription == null){
            taxonDescription = TaxonDescription.NewInstance(taxon, false);
            if(sourceNotLinkedToElement(taxonDescription,ref,null)) {
                taxonDescription.addSource(OriginalSourceType.Import, null, null, ref, null);
            }
            descriptionGroup=taxonDescription;
            taxon.addDescription(taxonDescription);
        }

        //PREPARE REFERENCE QUESTIONS

        Map<String,OriginalSourceBase<?>> sourceMap = new HashMap<String, OriginalSourceBase<?>>();

        List<OriginalSourceBase> issTmp = getCommonService().list(IdentifiableSource.class, null, null, null, null);
        List<OriginalSourceBase> issTmp2 = getCommonService().list(DescriptionElementSource.class, null, null, null, null);

        Set<OriginalSourceBase> osbSet = new HashSet<OriginalSourceBase>();
        if(issTmp2!=null) {
            osbSet.addAll(issTmp2);
        }
        if(issTmp!=null) {
            osbSet.addAll(issTmp);
        }


        for( OriginalSourceBase<?> osb:osbSet) {
            if(osb.getCitationMicroReference() !=null && !osb.getCitationMicroReference().isEmpty()) {
                try{
                    sourceMap.put(osb.getCitation().getTitleCache()+ "---"+osb.getCitationMicroReference(),osb);
                }catch(NullPointerException e){logger.warn("null pointer problem (no ref?) with "+osb);}
            } else{
                try{
                    sourceMap.put(osb.getCitation().getTitleCache(),osb);
                }catch(NullPointerException e){logger.warn("null pointer problem (no ref?) with "+osb);}
            }
        }

        if (state.getConfig().isInteractWithUser()){
            List<OriginalSourceBase<?>> res = null;
            if(!descriptionSourcesSet){
                res = sui.askForSource(sourceMap, "the description group ("+taxon+")",
                        "The current reference is "+ref.getTitleCache(),getReferenceService(), dataHolder.docSources);
                descriptionRefs=res;
                descriptionSourcesSet=true;
            }
            else{
                res=descriptionRefs;
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
            if(sourceNotLinkedToElement(taxonDescription,ref,null)) {
                taxonDescription.addSource(OriginalSourceType.Import,null, null, ref, null);
            }
        }
        descriptionGroup=taxonDescription;

        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();
        Feature feature = makeFeature(derivedUnitBase);
        indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
        indAssociation.setFeature(feature);

        //<<<<<<< .courant
        if (state.getConfig().isInteractWithUser()){
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


            for( OriginalSourceBase<?> osb:osbSet) {
                if(osb.getCitationMicroReference() !=null && !osb.getCitationMicroReference().isEmpty()) {
                    try{
                        sourceMap.put(osb.getCitation().getTitleCache()+ "---"+osb.getCitationMicroReference(),osb);
                    }catch(NullPointerException e){logger.warn("null pointer problem (no ref?) with "+osb);}
                } else{
                    try{
                        sourceMap.put(osb.getCitation().getTitleCache(),osb);
                    }catch(NullPointerException e){logger.warn("null pointer problem (no ref?) with "+osb);}
                }
            }

            List<OriginalSourceBase<?>> sources =null;
            if(!associationSourcesSet) {
                sources = sui.askForSource(sourceMap,  "descriptive element (association) ",taxon.toString(),
                        getReferenceService(),dataHolder.docSources);
                associationRefs=sources;
                associationSourcesSet=true;
            }
            else{
                sources=associationRefs;
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
                                if(sourceNotLinkedToElement(derivedUnitBase, source)) {
                                    derivedUnitBase.addSource((IdentifiableSource) source.clone());
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
            if(sourceNotLinkedToElement(indAssociation,ref,null)) {
                indAssociation.addSource(OriginalSourceType.Import,null, null, ref, null);
            }
            if(sourceNotLinkedToElement(derivedUnitBase, ref,null)) {
                derivedUnitBase.addSource(OriginalSourceType.Import,null, null, ref, null);
            }
            for (Reference<?> citation : determinationEvent.getReferences()) {
                if(sourceNotLinkedToElement(indAssociation,citation,null))
                {
                    indAssociation.addSource(DescriptionElementSource.NewInstance(OriginalSourceType.Import, null, null, citation, null));
                }
                if(sourceNotLinkedToElement(derivedUnitBase, ref,null)) {
                    derivedUnitBase.addSource(OriginalSourceType.Import,null, null, ref, null);
                }
            }
        }

        taxonDescription.addElement(indAssociation);
        taxonDescription.setTaxon(taxon);

        save(taxonDescription, state);
        save(taxon, state);
    }



    /**
     * @param derivedUnitBase2
     * @param ref2
     * @param object
     * @return
     */
    private boolean sourceNotLinkedToElement(DerivedUnit derivedUnitBase2, Reference<?> b, String d) {
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
     * @param derivedUnitBase2
     * @param source
     * @return
     */
    private boolean sourceNotLinkedToElement(DerivedUnit derivedUnitBase2, OriginalSourceBase<?> source) {
        Set<IdentifiableSource> linkedSources = derivedUnitBase2.getSources();
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
    private boolean sourceNotLinkedToElement(IndividualsAssociation indAssociation, Reference<?> a, String d) {
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
    private boolean sourceNotLinkedToElement(TaxonDescription taxonDescription, Reference<?> a, String d) {
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

    private Feature getFeature(String featureName){
        List<DefinedTermBase> features = getTermService().list(Feature.class, null,null,null,null);
        Feature currentFeature=null;
        for (DefinedTermBase feature: features){
            String tmpF = ((Feature)feature).getTitleCache();
            if (tmpF.equalsIgnoreCase(featureName)) {
                currentFeature=(Feature)feature;
            }
        }
        if (currentFeature == null) {
            currentFeature=Feature.NewInstance(featureName, featureName, featureName);
            getTermService().saveOrUpdate(currentFeature);
        }
        return currentFeature;
    }

    /**
     *  getTaxon : search for an existing taxon in the database
     * @param state : the ABCD import state
     * @param scientificName : the name (string)
     * @param i : the current unit position in the abcd file
     * @param rank : the rank for the taxon
     * @return a Taxon
     */
    @SuppressWarnings("rawtypes")
    private Taxon getTaxon(Abcd206ImportState state, String scientificName, int i, Rank rank) {
//        System.out.println("GETTAXON "+scientificName);
        Abcd206ImportConfigurator config = state.getConfig();
        Taxon taxon = null;
        NonViralName<?> taxonName = null;

        SpecimenUserInteraction sui = state.getConfig().getSpecimenUserInteraction();

//        System.out.println("config.isReuseExistingTaxaWhenPossible() :"+config.isReuseExistingTaxaWhenPossible());
        if (config.isReuseExistingTaxaWhenPossible()){
            List<TaxonBase> c = null;
            try {
                List<TaxonBase> taxonbaseList = getTaxonService().listByTitle(Taxon.class, scientificName+" sec", MatchMode.BEGINNING, null, null, null, null, null);
                if (taxonbaseList.size()>0){
                    if(config.isInteractWithUser() && config.isAllowReuseOtherClassifications()){
                        taxon = sui.askWhereToFixData(scientificName,taxonbaseList, classification);
                    } else {
                        taxon = sui.lookForTaxaIntoCurrentClassification(taxonbaseList, classification);
                    }
                }
                else{
                    c = getTaxonService().searchTaxaByName(scientificName, ref);
                    if(config.isInteractWithUser() && config.isAllowReuseOtherClassifications()){
                        taxon = sui.askWhereToFixData(scientificName,c, classification);
                    }
                    else{
                        taxon = sui.lookForTaxaIntoCurrentClassification(c, classification);
                    }
                }
            } catch (Exception e) {
                logger.info("Searchtaxabyname failed" + e);
                taxon = null;
            }
        }
        if (!config.isReuseExistingTaxaWhenPossible() || taxon == null){
//            System.out.println("create new taxonName instance "+i+", "+config.isParseNameAutomatically());
            if (config.isParseNameAutomatically()){
                taxonName = parseScientificName(scientificName);
            }
            else{
                if (i>=0 && (dataHolder.atomisedIdentificationList != null || dataHolder.atomisedIdentificationList.size() > 0)) {
                    taxonName = setTaxonNameByType(dataHolder.atomisedIdentificationList.get(i), scientificName);
                } else {
                    taxonName=null;
                }
            }
            //            if (taxonName != null) {
            //                System.out.println(taxonName.getTitleCache());
            //            } else {
            //                System.out.println("taxonname: "+taxonName);
            //            }
            if(taxonName == null){
                taxonName = NonViralName.NewInstance(rank);
                taxonName.setFullTitleCache(scientificName,true);
                taxonName.setTitleCache(scientificName, true);
            }
//            System.out.println("ADD NEW TAXON *"+taxonName.getRank()+"*"+taxonName.getTitleCache());
            if (rank != null && (taxonName.getRank() ==null || taxonName.getRank().toString().trim().isEmpty())) {
                taxonName.setRank(rank);
            }
            save(taxonName, state);
            taxon = Taxon.NewInstance(taxonName, ref); //sec set null
            save(taxon, state);
        }
        return taxon;
    }







    /**
     * HandleIdentifications : get the scientific names present in the ABCD
     * document and store link them with the observation/specimen data
     * @param state: the current ABCD import state
     * @param derivedUnitFacade : the current derivedunitfacade
     */
    private void handleIdentifications(Abcd206ImportState state, DerivedUnitFacade derivedUnitFacade) {
//        System.out.println("The reference from handleidentification "+ref);
        Abcd206ImportConfigurator config = state.getConfig();

        String fullScientificNameString;
        Taxon taxon = null;
        Rank.GENUS();
        Rank.FAMILY();

        String scientificName = "";
        boolean preferredFlag = false;

        List<String> scientificNames = new ArrayList<String>();
        if (dataHolder.nomenclatureCode == ""){
            dataHolder.nomenclatureCode = config.getNomenclaturalCode().toString();
        }

        for (int i = 0; i < dataHolder.identificationList.size(); i++) {

            fullScientificNameString = dataHolder.identificationList.get(i);
            fullScientificNameString = fullScientificNameString.replaceAll(" et ", " & ");

            if (fullScientificNameString.indexOf(PREFERRED) != -1) {
                scientificName = fullScientificNameString.split(PREFERRED)[0];
                String pTmp = fullScientificNameString.split(PREFERRED)[1].split(CODE)[0];
                if (pTmp.equals("1") || pTmp.toLowerCase().indexOf("true") != -1) {
                    preferredFlag = true;
                }
                else {
                    preferredFlag = false;
                }
            }
            else {
                scientificName = fullScientificNameString;
            }
            if(DEBUG) {
                logger.info("fullscientificname " + fullScientificNameString + ", *" + dataHolder.nomenclatureCode + "*");
            }
            if (fullScientificNameString.indexOf(CODE) != -1) {
                if (fullScientificNameString.indexOf(':') != -1) {
                    dataHolder.nomenclatureCode = fullScientificNameString.split(CODE)[1].split(COLON)[1];
                }
                else{
                    dataHolder.nomenclatureCode = fullScientificNameString.split(CODE)[1];
                }
            }
            scientificNames.add(scientificName+SPLITTER+preferredFlag+SPLITTER+i);
        }
        for (String name:scientificNames) {
            scientificName = name.split(SPLITTER)[0];
            String pref = name.split(SPLITTER)[1];
            String index = name.split(SPLITTER)[2];
            if (pref.equalsIgnoreCase("true") || scientificNames.size()==1) {
                preferredFlag = true;
            } else {
                preferredFlag =false;
            }
            taxon = getTaxon(state, scientificName,Integer.parseInt(index),null);
            addTaxonNode(taxon, state,preferredFlag);
            linkDeterminationEvent(state, taxon, preferredFlag, derivedUnitFacade);
        }
    }

    /**
     * @param taxon : a taxon to add as a node
     * @param state : the ABCD import state
     */
    private void addTaxonNode(Taxon taxon, Abcd206ImportState state, boolean preferredFlag) {
        logger.info("link taxon to a taxonNode "+taxon.getTitleCache());
        boolean exist = false;
        for (TaxonNode p : classification.getAllNodes()){
            try{
                if(p.getTaxon().equals(taxon)) {
                    exist =true;
                }
            }catch(Exception e){logger.warn("TaxonNode does'nt seem to have a taxon");}
        }
        if (!exist){
            addParentTaxon(taxon, state, preferredFlag);
        }
    }

    /**
     * Add the hierarchy for a Taxon(add higher taxa)
     * @param taxon: a taxon to add as a node
     * @param state: the ABCD import state
     */
    private void addParentTaxon(Taxon taxon, Abcd206ImportState state, boolean preferredFlag){
//        System.out.println("addParentTaxon " + taxon.getTitleCache());

        NonViralName<?>  nvname = CdmBase.deproxy(taxon.getName(), NonViralName.class);
        Rank rank = nvname.getRank();
        Taxon genus =null;
        Taxon subgenus =null;
        Taxon species = null;
        Taxon subspecies = null;
        Taxon parent = null;
        if (rank.isLower(Rank.GENUS() )){
            String prefix = nvname.getGenusOrUninomial();
            genus = getTaxon(state, prefix, -1, Rank.GENUS());
            if (preferredFlag) {
                parent = saveOrUpdateClassification(null, genus, state);
            }

        }
        if (rank.isLower(Rank.SUBGENUS())){
            String prefix = nvname.getGenusOrUninomial();
            String name = nvname.getInfraGenericEpithet();
            if (name != null){
                subgenus = getTaxon(state, prefix+" "+name, -1, Rank.SUBGENUS());
                if (preferredFlag) {
                    parent = saveOrUpdateClassification(genus, subgenus, state);
                }            }
        }
        if (rank.isLower(Rank.SPECIES())){
            if (subgenus!=null){
                String prefix = nvname.getGenusOrUninomial();
                String name = nvname.getInfraGenericEpithet();
                String spe = nvname.getSpecificEpithet();
                if (spe != null){
                    species = getTaxon(state, prefix+" "+name+" "+spe, -1, Rank.SPECIES());
                    if (preferredFlag) {
                        parent = 	saveOrUpdateClassification(subgenus, species, state);
                    }
                }
            }
            else{
                String prefix = nvname.getGenusOrUninomial();
                String name = nvname.getSpecificEpithet();
                if (name != null){
                    species = getTaxon(state, prefix+" "+name, -1, Rank.SPECIES());
                    if (preferredFlag) {
                        parent = 	saveOrUpdateClassification(genus, species, state);
                    }
                }
            }
        }
        if (rank.isInfraSpecific()){
            subspecies = getTaxon(state, nvname.getFullTitleCache(), -1, Rank.SUBSPECIES());
            if (preferredFlag) {
                parent = 	saveOrUpdateClassification(species, subspecies, state);
            }
        }
        if (preferredFlag) {
            saveOrUpdateClassification(parent, taxon, state);
        }
    }

    /**
     * Link a parent to a child and save it in the current classification
     * @param parent: the higher Taxon
     * @param child : the lower (or current) Taxon
     * return the Taxon from the new created Node
     * @param state
     */
    private Taxon saveOrUpdateClassification(Taxon parent, Taxon child, Abcd206ImportState state) {
//        System.out.println("ADD CLASSIFICATION parent child "+parent+"," +child);
        TaxonNode node =null;
        if (parent != null) {
            parent = (Taxon) getTaxonService().find(parent.getUuid());
            child = (Taxon) getTaxonService().find(child.getUuid());
            node = classification.addParentChild(parent, child, ref, "");
        }
        if (parent == null) {
            child = (Taxon) getTaxonService().find(child.getUuid());
            node =classification.addChildTaxon(child, ref, null);
        }
        save(classification, state);
        return node.getTaxon();
    }

    /**
     * Parse automatically the scientific name
     * @param scientificName: the scientific name to parse
     * @return a parsed name
     */
    private NonViralName<?> parseScientificName(String scientificName) {
        NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
        NonViralName<?> taxonName = null;
        boolean problem = false;

        if(DEBUG){
            logger.info("parseScientificName " + dataHolder.nomenclatureCode.toString());
        }

        if (dataHolder.nomenclatureCode.toString().equals("Zoological") || dataHolder.nomenclatureCode.toString().contains("ICZN")) {
            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICZN, null);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        if (dataHolder.nomenclatureCode.toString().equals("Botanical") || dataHolder.nomenclatureCode.toString().contains("ICBN")) {
            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNAFP, null);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        if (dataHolder.nomenclatureCode.toString().equals("Bacterial") || dataHolder.nomenclatureCode.toString().contains("ICBN")) {
            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNB, null);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        if (dataHolder.nomenclatureCode.toString().equals("Cultivar") || dataHolder.nomenclatureCode.toString().contains("ICNCP")) {
            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNCP, null);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        if (problem) {
            logger.info("Parsing with problem in parseScientificName " + scientificName);
            return null;
        }
        return taxonName;

    }

    /**
     * Create the name without automatic parsing, either because it failed, or because the user deactivated it.
     * The name is built upon the ABCD fields
     * @param atomisedMap : the ABCD atomised fields
     * @param fullName : the full scientific name
     * @return the corresponding Botanical or Zoological or... name
     */
    private NonViralName<?> setTaxonNameByType(
            HashMap<String, String> atomisedMap, String fullName) {
        boolean problem = false;
        if(DEBUG) {
            logger.info("settaxonnamebytype " + dataHolder.nomenclatureCode.toString());
        }

        if (dataHolder.nomenclatureCode.equals("Zoological")) {
            NonViralName<ZoologicalName> taxonName = ZoologicalName.NewInstance(null);
            taxonName.setFullTitleCache(fullName, true);
            taxonName.setGenusOrUninomial(NB(getFromMap(atomisedMap, "Genus")));
            taxonName.setInfraGenericEpithet(NB(getFromMap(atomisedMap, "SubGenus")));
            taxonName.setSpecificEpithet(NB(getFromMap(atomisedMap,"SpeciesEpithet")));
            taxonName.setInfraSpecificEpithet(NB(getFromMap(atomisedMap,"SubspeciesEpithet")));

            if (taxonName.getGenusOrUninomial() != null){
                taxonName.setRank(Rank.GENUS());
            }

            if (taxonName.getInfraGenericEpithet() != null){
                taxonName.setRank(Rank.SUBGENUS());
            }

            if (taxonName.getSpecificEpithet() != null){
                taxonName.setRank(Rank.SPECIES());
            }

            if (taxonName.getInfraSpecificEpithet() != null){
                taxonName.setRank(Rank.SUBSPECIES());
            }

            Team team = null;
            if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
                team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"), true);
            }
            else {
                if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
                    team = Team.NewInstance();
                    team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamAndYear"), true);
                }
            }
            if (team != null) {
                taxonName.setBasionymAuthorTeam(team);
            }
            else {
                if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"));
                }
                else if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeamAndYear"));
                }
            }
            if (getFromMap(atomisedMap, "CombinationAuthorTeamAndYear") != null) {
                team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "CombinationAuthorTeamAndYear"), true);
                taxonName.setCombinationAuthorTeam(team);
            }
            if (taxonName.hasProblem()) {
                logger.info("pb ICZN");
                problem = true;
            }
            else {
                return taxonName;
            }
        }
        else if (dataHolder.nomenclatureCode.equals("Botanical")) {
            BotanicalName taxonName = (BotanicalName) parseScientificName(fullName);
            if (taxonName != null){
                return taxonName;
            }
            else{
                taxonName = BotanicalName.NewInstance(null);
            }
            taxonName.setFullTitleCache(fullName, true);
            taxonName.setGenusOrUninomial(NB(getFromMap(atomisedMap, "Genus")));
            taxonName.setInfraGenericEpithet(NB(getFromMap(atomisedMap, "FirstEpithet")));
            taxonName.setInfraSpecificEpithet(NB(getFromMap(atomisedMap, "InfraSpeEpithet")));
            try {
                taxonName.setRank(Rank.getRankByName(getFromMap(atomisedMap, "Rank")));
            } catch (Exception e) {
                if (taxonName.getGenusOrUninomial() != null){
                    taxonName.setRank(Rank.GENUS());
                }
                else if (taxonName.getInfraGenericEpithet() != null){
                    taxonName.setRank(Rank.SUBGENUS());
                }
                else if (taxonName.getSpecificEpithet() != null){
                    taxonName.setRank(Rank.SPECIES());
                }
                else if (taxonName.getInfraSpecificEpithet() != null){
                    taxonName.setRank(Rank.SUBSPECIES());
                }
            }
            Team team = null;
            if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
                team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"), true);
                taxonName.setBasionymAuthorTeam(team);
            }
            if (getFromMap(atomisedMap, "AuthorTeam") != null) {
                team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeam"), true);
                taxonName.setCombinationAuthorTeam(team);
            }
            if (team == null) {
                if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"));
                }
                else if (getFromMap(atomisedMap, "AuthorTeam") != null) {
                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeam"));
                }
            }
            if (getFromMap(atomisedMap, "CombinationAuthorTeamAndYear") != null) {
                team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "CombinationAuthorTeamAndYear"), true);
                taxonName.setCombinationAuthorTeam(team);
            }
            if (taxonName.hasProblem()) {
                logger.info("pb ICBN");
                problem = true;
            }
            else {
                return taxonName;
            }
        }
        else if (dataHolder.nomenclatureCode.equals("Bacterial")) {
            NonViralName<BacterialName> taxonName = BacterialName.NewInstance(null);
            taxonName.setFullTitleCache(fullName, true);
            taxonName.setGenusOrUninomial(getFromMap(atomisedMap, "Genus"));
            taxonName.setInfraGenericEpithet(NB(getFromMap(atomisedMap, "SubGenus")));
            taxonName.setSpecificEpithet(NB(getFromMap(atomisedMap, "Species")));
            taxonName.setInfraSpecificEpithet(NB(getFromMap(atomisedMap, "SubspeciesEpithet")));

            if (taxonName.getGenusOrUninomial() != null){
                taxonName.setRank(Rank.GENUS());
            }
            else if (taxonName.getInfraGenericEpithet() != null){
                taxonName.setRank(Rank.SUBGENUS());
            }
            else if (taxonName.getSpecificEpithet() != null){
                taxonName.setRank(Rank.SPECIES());
            }
            else if (taxonName.getInfraSpecificEpithet() != null){
                taxonName.setRank(Rank.SUBSPECIES());
            }

            if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
                Team team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamAndYear"), true);
                taxonName.setCombinationAuthorTeam(team);
            }
            if (getFromMap(atomisedMap, "ParentheticalAuthorTeamAndYear") != null) {
                Team team = Team.NewInstance();
                team.setTitleCache(getFromMap(atomisedMap, "ParentheticalAuthorTeamAndYear"), true);
                taxonName.setBasionymAuthorTeam(team);
            }
            if (taxonName.hasProblem()) {
                logger.info("pb ICNB");
                problem = true;
            }
            else {
                return taxonName;
            }
        }
        else if (dataHolder.nomenclatureCode.equals("Cultivar")) {
            CultivarPlantName taxonName = CultivarPlantName.NewInstance(null);

            if (taxonName.hasProblem()) {
                logger.info("pb ICNCP");
                problem = true;
            }
            else {
                return taxonName;
            }
            return taxonName;
        }

        if (problem) {
            logger.info("Problem im setTaxonNameByType ");
            NonViralName<?> taxonName = NonViralName.NewInstance(null);
            taxonName.setFullTitleCache(fullName, true);
            return taxonName;
        }
        NonViralName<?> tn = NonViralName.NewInstance(null);
        return tn;
    }


    /**
     * Get a formated string from a hashmap
     * @param atomisedMap
     * @param key
     * @return
     */
    private String getFromMap(HashMap<String, String> atomisedMap, String key) {
        String value = null;
        if (atomisedMap.containsKey(key)) {
            value = atomisedMap.get(key);
        }

        try {
            if (value != null && key.matches(".*Year.*")) {
                value = value.trim();
                if (value.matches("[a-z A-Z ]*[0-9]{4}$")) {
                    String tmp = value.split("[0-9]{4}$")[0];
                    int year = Integer.parseInt(value.split(tmp)[1]);
                    if (year >= 1752) {
                        value = tmp;
                    }
                    else {
                        value = null;
                    }
                }
                else {
                    value = null;
                }
            }
        }
        catch (Exception e) {
            value = null;
        }
        return value;
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
    //        Set<String> elts = dataHolder.allABCDelements.keySet();
    //        Iterator<String> it = elts.iterator();
    //        String elt;
    //        while (it.hasNext()) {
    //            elt = it.next();
    //            if (knownElts.indexOf(elt) == -1) {
    //                if(DEBUG) {
    //                    logger.info("Unmerged ABCD element: " + elt + " - "+ dataHolder.allABCDelements.get(elt));
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
            this.getCollectorsFromXML((Element) unitsList.item(i), abcdFieldGetter);
            for (String agent : dataHolder.gatheringAgentList) {
                collectors.add(agent);
            }
            List<String> tmpTeam = new ArrayList<String>(new HashSet<String>(dataHolder.gatheringTeamList));
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
        List<UuidAndTitleCache<Team>> hiberTeam = getAgentService().getTeamUuidAndTitleCache();

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
        List<UuidAndTitleCache<Person>> hiberPersons = getAgentService().getPersonUuidAndTitleCache();
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

        state.getConfig().setTeams(titleCacheTeam);
        state.getConfig().setPersons(titleCachePerson);
    }

    @Override
    protected boolean isIgnore(Abcd206ImportState state) {
        return false;
    }


}
