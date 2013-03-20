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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportBase;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
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
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 */
@Component
public class Abcd206Import extends SpecimenImportBase<Abcd206ImportConfigurator, Abcd206ImportState> {


    private final boolean DEBUG = false;

    private static final String SEC = "sec. ";
    private static final String PREFERRED = "_preferred_";
    private static final String CODE = "_code_";
    private static final String COLON = ":";
    private static final String SPLITTER = "--";

    private static final Logger logger = Logger.getLogger(Abcd206Import.class);
    private static String prefix = "";

    private Classification classification = null;
    private Reference<?> ref = null;

    private Abcd206ImportState abcdstate;
    private Abcd206DataHolder dataHolder;
    private DerivedUnitBase derivedUnitBase;

    private TransactionStatus tx;

    private Abcd206XMLFieldGetter abcdFileGetter;

    public Abcd206Import() {
        super();
    }

    @Override
    protected boolean doCheck(Abcd206ImportState state) {
        logger.warn("Checking not yet implemented for " + this.getClass().getSimpleName());
        this.abcdstate = state;
        return true;
    }

    /**
     * getClassification : get the classification declared in the ImportState
     *
     * @param state
     * @return
     */
    private void setClassification(Abcd206ImportState state) {
        if (classification == null) {
            String name = state.getConfig().getClassificationName();

            classification = Classification.NewInstance(name, ref, Language.DEFAULT());
            if (state.getConfig().getClassificationUuid() != null) {
                classification.setUuid(state.getConfig().getClassificationUuid());
            }
            getClassificationService().saveOrUpdate(classification);
            //            refreshTransaction();
        }
    }

    @Override
    public void doInvoke(Abcd206ImportState state) {
        abcdstate = state;
        tx = startTransaction();

        logger.info("INVOKE Specimen Import from ABCD2.06 XML ");
        URI sourceName = this.abcdstate.getConfig().getSource();
        NodeList unitsList = getUnitsNodeList(sourceName);

        ref = this.abcdstate.getConfig().getSourceReference();
        setClassification(abcdstate);

        if (unitsList != null) {
            String message = "nb units to insert: " + unitsList.getLength();
            logger.info(message);
            updateProgress(this.abcdstate, message);
            dataHolder = new Abcd206DataHolder();
            abcdFileGetter = new Abcd206XMLFieldGetter(dataHolder, prefix);

            prepareCollectors(unitsList,state);

            for (int i = 0; i < unitsList.getLength(); i++) {

                this.setUnitPropertiesXML((Element) unitsList.item(i));
                //                refreshTransaction();
                this.handleSingleUnit(i);

                // compare the ABCD elements added in to the CDM and the
                // unhandled ABCD elements
                //compareABCDtoCDM(sourceName, dataHolder.knownABCDelements);

                // reset the ABCD elements added in CDM
                // knownABCDelements = new ArrayList<String>();
                dataHolder.allABCDelements = new HashMap<String, String>();

                //                refreshTransaction();
            }
        }
        commitTransaction(tx);
        return;

    }


    /*
     * Return the list of root nodes for an ABCD 2.06 XML file
     *
     * @param fileName: the file's location
     *
     * @return the list of root nodes ("Unit")
     */
    protected NodeList getUnitsNodeList(URI urlFileName) {
        NodeList unitList = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            URL url = urlFileName.toURL();
            Object o = url.getContent();
            InputStream is = (InputStream) o;
            Document document = builder.parse(is);
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

    /*
     * Stores the unit with its Gathering informations in the CDM
     */
    private void handleSingleUnit(int i) {
        logger.info("handleSingleUnit");

        try {
            updateProgress(this.abcdstate, "Importing data for unit: " + dataHolder.unitID);


            // create facade
            DerivedUnitFacade derivedUnitFacade = getFacade();
            derivedUnitBase = derivedUnitFacade.innerDerivedUnit();

            /**
             * GATHERING EVENT
             */

            // gathering event
            UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(getTermService(), dataHolder.locality, dataHolder.languageIso,
                    dataHolder.longitude, dataHolder.latitude, dataHolder.gatheringAgentList, dataHolder.gatheringTeamList,abcdstate.getConfig());

            // country
            UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea(dataHolder.isocountry, dataHolder.country, getOccurrenceService());
            NamedArea areaCountry = unitsGatheringArea.getArea();

            // other areas
            unitsGatheringArea = new UnitsGatheringArea(dataHolder.namedAreaList);
            ArrayList<NamedArea> nas = unitsGatheringArea.getAreas();
            for (NamedArea namedArea : nas) {
                unitsGatheringEvent.addArea(namedArea);
            }

            // copy gathering event to facade
            GatheringEvent gatheringEvent = unitsGatheringEvent.getGatheringEvent();
            derivedUnitFacade.setLocality(gatheringEvent.getLocality());
            derivedUnitFacade.setExactLocation(gatheringEvent.getExactLocation());
            derivedUnitFacade.setCollector(gatheringEvent.getCollector());
            derivedUnitFacade.setCountry(areaCountry);
            derivedUnitFacade.addCollectingAreas(unitsGatheringArea.getAreas());

            // TODO exsiccatum

            // add fieldNumber
            derivedUnitFacade.setFieldNumber(dataHolder.fieldNumber);

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

            /*
             * merge AND STORE DATA
             */
            getTermService().saveOrUpdate(areaCountry);// TODO save area sooner

            for (NamedArea area : nas) {
                getTermService().saveOrUpdate(area);// merge it sooner (foreach area)
            }
            getTermService().saveLanguageData(unitsGatheringEvent.getLocality());

            // handle collection data
            setCollectionData(this.abcdstate.getConfig(), derivedUnitFacade);


            getOccurrenceService().saveOrUpdate(derivedUnitBase);
            refreshTransaction();

            // handle identifications
            handleIdentifications(this.abcdstate.getConfig(), derivedUnitFacade);

            logger.info("saved ABCD specimen ...");

        } catch (Exception e) {
            logger.warn("Error when reading record!!");
            e.printStackTrace();
            this.abcdstate.setUnsuccessfull();
        }

        return;
    }

    /**
     * setCollectionData : store the collection object into the
     * derivedUnitFacade
     *
     * @param config
     */
    private void setCollectionData(Abcd206ImportConfigurator config,
            DerivedUnitFacade derivedUnitFacade) {
        // set catalogue number (unitID)
        derivedUnitFacade.setCatalogNumber(dataHolder.unitID);
        derivedUnitFacade.setAccessionNumber(dataHolder.accessionNumber);
        // derivedUnitFacade.setCollectorsNumber(dataHolder.collectorsNumber);

        /*
         * INSTITUTION & COLLECTION
         */
        // manage institution
        Institution institution = this.getInstitution(dataHolder.institutionCode, config);
        // manage collection
        Collection collection = this.getCollection(institution, config);
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
        DerivedUnitType type = null;

        // create specimen
        if (dataHolder.recordBasis != null) {
            if (dataHolder.recordBasis.toLowerCase().startsWith("s") || dataHolder.recordBasis.toLowerCase().contains("specimen")) {// specimen
                type = DerivedUnitType.Specimen;
            }
            if (dataHolder.recordBasis.toLowerCase().startsWith("o")) {
                type = DerivedUnitType.Observation;
            }
            if (dataHolder.recordBasis.toLowerCase().contains("fossil")) {
                type = DerivedUnitType.Fossil;
            }

            if (dataHolder.recordBasis.toLowerCase().startsWith("l")) {
                type = DerivedUnitType.LivingBeing;
            }
            if (type == null) {
                logger.info("The basis of record does not seem to be known: " + dataHolder.recordBasis);
                type = DerivedUnitType.DerivedUnit;
            }
            // TODO fossils?
        } else {
            logger.info("The basis of record is null");
            type = DerivedUnitType.DerivedUnit;
        }
        DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(type);
        return derivedUnitFacade;
    }

    private void getCollectorsFromXML(Element root) {
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
        abcdFileGetter.getType(root);
        abcdFileGetter.getGatheringPeople(root);
    }
    /**
     * Store the unit's properties into variables Look which unit is the
     * preferred one Look what kind of name it is supposed to be, for the
     * parsing (Botanical, Zoological)
     *
     * @param racine: the root node for a single unit
     */
    private void setUnitPropertiesXML(Element root) {
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
            dataHolder.referenceList = new ArrayList<String>();
            dataHolder.multimediaObjects = new ArrayList<String>();

            abcdFileGetter.getScientificNames(group);
            abcdFileGetter.getType(root);

            if(DEBUG) {
                logger.info("this.identificationList "+dataHolder.identificationList.toString());
            }
            abcdFileGetter.getIDs(root);
            abcdFileGetter.getRecordBasis(root);
            abcdFileGetter.getMultimedia(root);
            abcdFileGetter.getNumbers(root);
            abcdFileGetter.getGeolocation(root);
            abcdFileGetter.getGatheringPeople(root);
            boolean referencefound = abcdFileGetter.getReferences(root);
            if (!referencefound) {
                dataHolder.referenceList.add(ref.getTitleCache());
            }

        } catch (Exception e) {
            logger.info("Error occured while parsing XML file" + e);
        }
    }

    private Institution getInstitution(String institutionCode, Abcd206ImportConfigurator config) {
        Institution institution;
        List<Institution> institutions;
        try {
            if(DEBUG) {
                logger.info(dataHolder.institutionCode);
            }
            institutions = getAgentService().searchInstitutionByCode(dataHolder.institutionCode);
        } catch (Exception e) {
            institutions = new ArrayList<Institution>();
        }
        if (institutions.size() == 0 || !config.isReUseExistingMetadata()) {
            if(DEBUG) {
                logger.info("Institution (agent) unknown or not allowed to reuse existing metadata");
            }
            // create institution
            institution = Institution.NewInstance();
            institution.setCode(dataHolder.institutionCode);
        }
        else {
            if(DEBUG) {
                logger.info("Institution (agent) already in the db");
            }
            institution = institutions.get(0);
        }
        if(DEBUG) {
            logger.info("getinstitution " + institution.toString());
        }
        return institution;
    }

    /**
     * Look if the Collection does already exists
     * @param collectionCode: a string
     * @param institution: the current Institution
     * @param app
     * @return the Collection (existing or new)
     */
    private Collection getCollection(Institution institution, Abcd206ImportConfigurator config) {
        Collection collection = Collection.NewInstance();
        List<Collection> collections;
        try {
            collections = getCollectionService().searchByCode(dataHolder.collectionCode);
        } catch (Exception e) {
            collections = new ArrayList<Collection>();
        }
        if (collections.size() == 0 || !config.isReUseExistingMetadata()) {
            if(DEBUG) {
                logger.info("Collection not found or do not reuse existing metadata  " + dataHolder.collectionCode);
            }
            // create new collection
            collection.setCode(dataHolder.collectionCode);
            collection.setCodeStandard("GBIF");
            collection.setInstitute(institution);
        } else {
            boolean collectionFound = false;
            for (int i = 0; i < collections.size(); i++) {
                collection = collections.get(i);
                try {
                    if (collection.getInstitute().getCode().equalsIgnoreCase(institution.getCode())) {
                        // found a collection with the same code and the same institution
                        collectionFound = true;
                        break;
                    }
                } catch (NullPointerException e) {
                    //TODO: exception?
                }
            }
            if (!collectionFound) {
                collection.setCode(dataHolder.collectionCode);
                collection.setCodeStandard("GBIF");
                collection.setInstitute(institution);
            }

        }
        return collection;
    }

    /**
     * join DeterminationEvent to the Taxon Object
     * @param taxon current Taxon Object
     * @param preferredFlag preferred name, boolean
     * @param config current ABCD Import configurator
     */

    private void linkDeterminationEvent(Taxon taxon, boolean preferredFlag, Abcd206ImportConfigurator config, DerivedUnitFacade derivedFacade) {
        if(DEBUG) {
            logger.info("start linkdetermination with taxon:" + taxon.getUuid()+", "+taxon);
        }

        //        refreshTransaction();

        try {
            taxon = (Taxon) getTaxonService().find(taxon.getUuid());
        } catch (Exception e) {
            //          if(DEBUG) logger.info("taxon uptodate");
        }

        DeterminationEvent determinationEvent = DeterminationEvent.NewInstance();
        determinationEvent.setTaxon(taxon);
        determinationEvent.setPreferredFlag(preferredFlag);

        determinationEvent.setIdentifiedUnit(derivedUnitBase);

        derivedUnitBase.addDetermination(determinationEvent);
        //        refreshTransaction();

        try {
            if(DEBUG) {
                logger.info("NB TYPES INFO: "+ dataHolder.statusList.size());
            }
            for (SpecimenTypeDesignationStatus specimenTypeDesignationstatus : dataHolder.statusList) {
                if (specimenTypeDesignationstatus != null) {
                    if(DEBUG) {
                        logger.info("specimenTypeDesignationstatus :"+ specimenTypeDesignationstatus);
                    }
                    try {
                        taxon = (Taxon) getTaxonService().find(taxon.getUuid());

                    } catch (Exception e) {
                        // logger.info("taxon uptodate");
                    }
                    specimenTypeDesignationstatus = (SpecimenTypeDesignationStatus) getTermService().find(specimenTypeDesignationstatus.getUuid());
                    //Designation
                    TaxonNameBase<?,?> name = taxon.getName();
                    SpecimenTypeDesignation designation = SpecimenTypeDesignation.NewInstance();

                    designation.setTypeStatus(specimenTypeDesignationstatus);
                    designation.setTypeSpecimen(derivedUnitBase);
                    name.addTypeDesignation(designation, true);
                    refreshTransaction();

                }
            }
        } catch (Exception e) {
            logger.warn("PB addding SpecimenType " + e);
        }

        for (String strReference : dataHolder.referenceList) {
            Reference<?> reference = ReferenceFactory.newGeneric();
            reference.setTitleCache(strReference, true);
            getReferenceService().saveOrUpdate(reference);

            determinationEvent.addReference(reference);
        }

        getOccurrenceService().saveOrUpdate(derivedUnitBase);
        refreshTransaction();


        if (config.isDoCreateIndividualsAssociations()) {
            if(DEBUG) {
                logger.info("isDoCreateIndividualsAssociations");
            }


            makeIndividualsAssociation(taxon, determinationEvent);
            getOccurrenceService().saveOrUpdate(derivedUnitBase);
        }
    }

    private void makeIndividualsAssociation(Taxon taxon, DeterminationEvent determinationEvent) {
        try{
            taxon = (Taxon) getTaxonService().find(taxon.getUuid());
        }
        catch(Exception e){
            //logger.info("taxon uptodate");
        }

        TaxonDescription taxonDescription = getTaxonDescription(taxon, ref, false, true);
        taxonDescription.setTitleCache(ref.getTitleCache(), true);

        taxon.addDescription(taxonDescription);

        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();
        Feature feature = makeFeature(derivedUnitBase);
        indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
        indAssociation.setFeature(feature);

        for (Reference<?> citation : determinationEvent.getReferences()) {
            indAssociation.addSource(DescriptionElementSource.NewInstance(null, null, citation, null));
        }

        taxonDescription.addElement(indAssociation);
        taxonDescription.setTaxon(taxon);

        getDescriptionService().saveOrUpdate(taxonDescription);
        getTaxonService().saveOrUpdate(taxon);

    }

    private Feature makeFeature(SpecimenOrObservationBase unit) {
        if (unit.isInstanceOf(DerivedUnit.class)) {
            return Feature.INDIVIDUALS_ASSOCIATION();
        }
        else if (unit.isInstanceOf(FieldObservation.class) || unit.isInstanceOf(Observation.class)) {
            return Feature.OBSERVATION();
        }
        else if (unit.isInstanceOf(Fossil.class) || unit.isInstanceOf(LivingBeing.class) || unit.isInstanceOf(Specimen.class)) {
            return Feature.SPECIMEN();
        }
        logger.warn("No feature defined for derived unit class: " + unit.getClass().getSimpleName());
        return null;
    }

    private void refreshTransaction(){
        System.out.println("REFRESH");
        commitTransaction(tx);
        tx = startTransaction();
        ref = getReferenceService().find(ref.getUuid());
        classification = getClassificationService().find(classification.getUuid());
        try{
            derivedUnitBase = (DerivedUnitBase) getOccurrenceService().find(derivedUnitBase.getUuid());
        }
        catch(Exception e){
            //logger.warn("derivedunit up to date or not created yet");
        }
    }


    /**
     * getTaxon : search for an existing taxon in the database, for the same
     * reference
     *
     * @param config
     * @param scientificName
     * @param preferredFlag
     * @param i
     * @param taxonnametoinsert
     * @param preferredtaxonnametoinsert
     * @param taxonName
     * @return
     */
    private Taxon getTaxon(Abcd206ImportConfigurator config, String scientificName, int i, Rank rank, String nomenclature) {
        if (rank == null) {
            System.out.println("getTaxon "+scientificName);
        } else {
            System.out.println("getTaxon "+scientificName+", "+rank.generateTitle());
        }
        Taxon taxon = null;
        NonViralName<?> taxonName = null;

        if (config.isDoReUseTaxon()){
            List<TaxonBase> c = null;
            try {
                Taxon cc= getTaxonService().findBestMatchingTaxon(scientificName);
                if (cc != null && cc.getSec()!=null && cc.getSec().getTitleCache().equalsIgnoreCase(ref.getTitleCache())){
                    taxon=cc;
                }
                else{
                    c = getTaxonService().searchTaxaByName(scientificName, ref);
                    for (TaxonBase b : c) {
                        taxon = (Taxon) b;
                    }
                }
            } catch (Exception e) {
                logger.info("Searchtaxabyname failed" + e);
                taxon = null;
            }
        }
        if (!config.isDoReUseTaxon() || taxon == null){
            System.out.println("create new taxonName instance");
            if (config.isDoAutomaticParsing()){
                taxonName = parseScientificName(scientificName);
            }
            else{
                if (i>=0 && (dataHolder.atomisedIdentificationList != null || dataHolder.atomisedIdentificationList.size() > 0)) {
                    taxonName = setTaxonNameByType(dataHolder.atomisedIdentificationList.get(i), scientificName);
                } else {
                    taxonName=null;
                }
            }
            if(taxonName == null){
                taxonName = NonViralName.NewInstance(rank);
                taxonName.setFullTitleCache(scientificName,true);
                taxonName.setTitleCache(scientificName, true);
            }
            System.out.println("ADD NEW TAXON *"+taxonName.getRank()+"*"+taxonName);
            if (rank != null && (taxonName.getRank() ==null || taxonName.getRank().toString().trim().isEmpty())) {
                taxonName.setRank(rank);
            }
            getNameService().save(taxonName);
            taxon = Taxon.NewInstance(taxonName, ref); //sec set null
            getTaxonService().save(taxon);
            refreshTransaction();
           taxon= (Taxon) getTaxonService().find(taxon.getUuid());
        }

        System.out.println("taxon.getUuid() suite :"+taxon.getUuid());
        return taxon;
    }

    /**
     * HandleIdentifications : get the scientific names present in the ABCD
     * document and store link them with the observation/specimen data
     *
     * @param config
     */
    private void handleIdentifications(Abcd206ImportConfigurator config,
            DerivedUnitFacade derivedUnitFacade) {

        String fullScientificNameString;
        Taxon taxon = null;

        Rank.GENUS();
        Rank.FAMILY();

        String scientificName = "";
        boolean preferredFlag = false;
        boolean onepreferred = false;

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
                    onepreferred = true;
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

            taxon = getTaxon(config, scientificName,Integer.parseInt(index),dataHolder.nomenclatureCode);
            addTaxonNode(taxon, config,dataHolder.nomenclatureCode);
            taxon = (Taxon) getTaxonService().find(taxon.getUuid());

            linkDeterminationEvent(taxon, preferredFlag, config, derivedUnitFacade);
            //            refreshTransaction();
        }
        refreshTransaction();
        taxon = (Taxon) getTaxonService().find(taxon.getUuid());
    }




    /**
     * @param config
     * @param scientificName
     * @param parseInt
     * @return
     */
    private Taxon getTaxon(Abcd206ImportConfigurator config, String scientificName, int index, String nomenclature) {
        return getTaxon(config, scientificName, index, null, nomenclature);
    }

    /**
     * @param taxon
     * @param config
     * @return
     */
    private void addTaxonNode(Taxon taxon, Abcd206ImportConfigurator config, String nomenclature) {
        logger.info("link taxon to a taxonNode");
        boolean exist = false;
        for (TaxonNode p : classification.getAllNodes()){
            if(p.getTaxon().equals(taxon)) {
                exist =true;
            }
        }
        if (!exist){
            addParentTaxon(taxon, config,nomenclature);
        }
        refreshTransaction();
    }

    private void addParentTaxon(Taxon taxon, Abcd206ImportConfigurator config,String nomenclature){
            System.out.println("addParentTaxon "+taxon.getTitleCache());

        NonViralName<?>  nvname = CdmBase.deproxy(taxon.getName(), NonViralName.class);
        Rank rank = nvname.getRank();

        Taxon genus =null;
        Taxon subgenus =null;
        Taxon species = null;
        Taxon subspecies = null;
        if (rank.isLower(Rank.GENUS() )){
            String prefix = nvname.getGenusOrUninomial();
            genus = getTaxon(config, prefix, -1, Rank.GENUS(),nomenclature);
            saveOrUpdateClassification(null, genus);
        }
        if (rank.isLower(Rank.SUBGENUS())){
            String prefix = nvname.getGenusOrUninomial();
            String name = nvname.getInfraGenericEpithet();
            if (name != null){
                subgenus = getTaxon(config, prefix+" "+name, -1, Rank.SUBGENUS(),nomenclature);
                saveOrUpdateClassification(genus, subgenus);
            }
        }
        if (rank.isLower(Rank.SPECIES())){
            if (subgenus!=null){
                String prefix = nvname.getGenusOrUninomial();
                String name = nvname.getInfraGenericEpithet();
                String spe = nvname.getSpecificEpithet();
                if (spe != null){
                    species = getTaxon(config, prefix+" "+name+" "+spe, -1, Rank.SPECIES(),nomenclature);
                    saveOrUpdateClassification(subgenus, species);
                }
            }
            else{
                String prefix = nvname.getGenusOrUninomial();
                String name = nvname.getSpecificEpithet();
                if (name != null){
                    species = getTaxon(config, prefix+" "+name, -1, Rank.SPECIES(),nomenclature);
                    saveOrUpdateClassification(genus, species);
                }
            }
        }
        if (rank.isInfraSpecific()){
                subspecies = getTaxon(config, nvname.getFullTitleCache(), -1, Rank.SUBSPECIES(),nomenclature);
                saveOrUpdateClassification(species, subspecies);
        }
    }


    /**
     * @param currentTaxon
     * @param taxon
     */
    private void saveOrUpdateClassification(Taxon parent, Taxon child) {
        System.out.println("ADD CLASSIFICATION parent child "+parent+"," +child);
        if (parent != null) {
            parent = (Taxon) getTaxonService().find(parent.getUuid());
            child = (Taxon) getTaxonService().find(child.getUuid());
            classification.addParentChild(parent, child, ref, "");
        }
        if (parent == null) {
            child = (Taxon) getTaxonService().find(child.getUuid());
            classification.addChildTaxon(child, ref, "", null);
        }
        getClassificationService().saveOrUpdate(classification);
    }

    private NonViralName<?> parseScientificName(String scientificName) {
        NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
        NonViralName<?> taxonName = null;
        boolean problem = false;

        if(DEBUG) {
            System.out.println("parseScientificName " + dataHolder.nomenclatureCode.toString());
        }

        if (dataHolder.nomenclatureCode.toString().equals("Zoological") || dataHolder.nomenclatureCode.toString().contains("ICZN")) {
            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICZN, null);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        if (dataHolder.nomenclatureCode.toString().equals("Botanical") || dataHolder.nomenclatureCode.toString().contains("ICBN")) {
            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICBN, null);
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
            System.out.println("Parsing with problem in parseScientificName " + scientificName);
            return null;
        }
        return taxonName;

    }

    private NonViralName<?> setTaxonNameByType(
            HashMap<String, String> atomisedMap, String fullName) {
        boolean problem = false;
        if(DEBUG) {
            logger.info("settaxonnamebytype " + dataHolder.nomenclatureCode.toString());
        }

        if (dataHolder.nomenclatureCode.equals("Zoological")) {
            NonViralName<ZoologicalName> taxonName = ZoologicalName.NewInstance(null);
            taxonName.setFullTitleCache(fullName, true);
            taxonName.setGenusOrUninomial(getFromMap(atomisedMap, "Genus"));
            taxonName.setInfraGenericEpithet(getFromMap(atomisedMap, "SubGenus"));
            taxonName.setSpecificEpithet(getFromMap(atomisedMap,"SpeciesEpithet"));
            taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,"SubspeciesEpithet"));

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
            taxonName.setGenusOrUninomial(getFromMap(atomisedMap, "Genus"));
            taxonName.setInfraGenericEpithet(getFromMap(atomisedMap, "FirstEpithet"));
            taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap, "InfraSpeEpithet"));
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
            taxonName.setInfraGenericEpithet(getFromMap(atomisedMap, "SubGenus"));
            taxonName.setSpecificEpithet(getFromMap(atomisedMap, "Species"));
            taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap, "SubspeciesEpithet"));

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

    private void compareABCDtoCDM(URI urlFileName, ArrayList<String> knownElts) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructeur = factory.newDocumentBuilder();
            URL url = urlFileName.toURL();
            Object o = url.getContent();
            InputStream is = (InputStream) o;
            Document document = constructeur.parse(is);
            Element root = document.getDocumentElement();
            abcdFileGetter.traverse(root);
        }
        catch (ParserConfigurationException e){
            e.printStackTrace();
        }
        catch (SAXException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Set<String> elts = dataHolder.allABCDelements.keySet();
        Iterator<String> it = elts.iterator();
        String elt;
        while (it.hasNext()) {
            elt = it.next();
            if (knownElts.indexOf(elt) == -1) {
                if(DEBUG) {
                    logger.info("Unmerged ABCD element: " + elt + " - "+ dataHolder.allABCDelements.get(elt));
                }
            }
        }
    }

    @Override
    protected boolean isIgnore(Abcd206ImportState state) {
        return false;
    }

    /**
     * @param unitsList
     * @param state
     */
    private void prepareCollectors(NodeList unitsList, Abcd206ImportState state) {
        List<String> collectors = new ArrayList<String>();
        List<String> teams = new ArrayList<String>();
        List<List<String>> collectorinteams = new ArrayList<List<String>>();
        String tmp;

        for (int i = 0; i < unitsList.getLength(); i++) {
            this.getCollectorsFromXML((Element) unitsList.item(i));
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
            for (AgentBase existingP:existingTeams){
                titleCacheTeam.put(existingP.getTitleCache(),(Team) existingP);
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
            for (AgentBase existingP:existingPersons){
                titleCachePerson.put(existingP.getTitleCache(),(Person) existingP);
            }
        }

        Map<String,UUID> personMap = new HashMap<String, UUID>();
        for (UuidAndTitleCache<Person> person:hiberPersons){
            personMap.put(person.getTitleCache(), person.getUuid());
        }

        java.util.Collection<AgentBase> personToadd = new ArrayList<AgentBase>();
        java.util.Collection<AgentBase> teamToAdd = new ArrayList<AgentBase>();

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
            Map<UUID, AgentBase> uuuidPerson = getAgentService().save(personToadd);
            for (UUID u:uuuidPerson.keySet()){
                titleCachePerson.put(uuuidPerson.get(u).getTitleCache(),(Person) uuuidPerson.get(u) );
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
            Map<UUID, AgentBase> uuuidTeam =  getAgentService().save(teamToAdd);
            for (UUID u:uuuidTeam.keySet()){
                titleCacheTeam.put(uuuidTeam.get(u).getTitleCache(), (Team) uuuidTeam.get(u) );
            }
        }

        state.getConfig().setTeams(titleCacheTeam);
        state.getConfig().setPersons(titleCachePerson);
    }

}
