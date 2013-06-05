/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen.excel.in;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
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
 * @created 29.10.2008
 * @version 1.0
 *
 * @author pkelbert
 * @date 13 mars 2013
 *
 */
@Component
public class SpecimenSythesysExcelImport  extends CdmImportBase<SpecimenSynthesysExcelImportConfigurator, SpecimenSynthesysExcelImportState>  implements ICdmIO<SpecimenSynthesysExcelImportState> {

    private static final Logger logger = Logger.getLogger(SpecimenSythesysExcelImport.class);

    protected String fullScientificNameString;
    protected String nomenclatureCode;
    protected String institutionCode;
    protected String collectionCode;
    protected String unitID;
    protected String recordBasis;
    protected String accessionNumber;
    protected String fieldNumber;
    protected Double longitude;
    protected Double latitude;
    protected String locality;
    protected String languageIso = null;
    protected String country;
    protected String isocountry;
    protected int depth;
    protected int altitude;
    protected String gatheringYear;
    protected String gatheringMonth;
    protected String gatheringDay;
    protected String gatheringDate;
    protected String gatheringTeam;
    protected String gatheringAgent;
    protected String originalsource;

    private DerivedUnitBase derivedUnitBase;
    private Reference<?> ref = null;
    private TransactionStatus tx;
    private Classification classification = null;

    protected ArrayList<String> identificationList = new ArrayList<String>();
    protected ArrayList<String> multimediaObjects = new ArrayList<String>();
    private boolean keepAtomisedDate=true;

    boolean DEBUG =false;

    protected HSSFWorkbook hssfworkbook = null;

    public SpecimenSythesysExcelImport() {
        super();
    }

    /**
     * private HashMap that handle null values (missing keys)
     * return empty string instead of null
     * */
    public class MyHashMap<K,V> extends HashMap<K,V> {
        private static final long serialVersionUID = -6230407405666753405L;

        @SuppressWarnings("unchecked")
        @Override
        public V get(Object key) {
            Object a;
            if (containsKey(key)) {
                a= super.get(key);
            } else {
                a="";
            }
            if (a ==null || a.toString().equalsIgnoreCase("none")) {
                a="";
            }
            return (V) a;
        }
    }


    /**
     * getClassification : get the classification declared in the ImportState
     *
     * @param state
     * @return
     */
    private void setClassification(SpecimenSynthesysExcelImportState state) {
        if (classification == null){
            if (state.getConfig().getClassificationName()!=null && state.getConfig().getClassificationName().equalsIgnoreCase("chenopodium")) {
                classification = getClassificationService().find(UUID.fromString("2c2dc41c-9891-42cd-9cd5-8b28dfdd1b8a"));
                if (classification ==null){
                    String name = state.getConfig().getClassificationName();
                    classification = Classification.NewInstance(name, ref, Language.DEFAULT());
                    getClassificationService().saveOrUpdate(classification);
                }
            }
            else{
                String name = state.getConfig().getClassificationName();
                classification = Classification.NewInstance(name, ref, Language.DEFAULT());
                if (state.getConfig().getClassificationUuid() != null) {
                    classification.setUuid(state.getConfig().getClassificationUuid());
                }
                getClassificationService().saveOrUpdate(classification);
            }

        }
    }

    /**
     * refresh the hibernate transaction :
     * - commit the current queries
     * - get the reference and the classification and the derivedUnitBase back from the hibernate session
     * */
    private void refreshTransaction(){
        commitTransaction(tx);
        tx = startTransaction();
        ref = getReferenceService().find(ref.getUuid());
        classification = getClassificationService().find(classification.getUuid());
        if (derivedUnitBase != null){
        	derivedUnitBase = (DerivedUnitBase) getOccurrenceService().find(derivedUnitBase.getUuid());
        }
    }

    /*
     * Store the unit's properties into variables
     * @param unit: the hashmap containing the splitted Excel line (Key=column name, value=value)
     */
    private void setUnitPropertiesExcel(MyHashMap<String,String> unit, String defaultAuthor){
        multimediaObjects = new ArrayList<String>();
        identificationList = new ArrayList<String>();
        gatheringTeam="";
        gatheringAgent="";

        String author = unit.get("author");
        if (author.isEmpty() && !defaultAuthor.isEmpty()) {
            author=defaultAuthor;
        }
        String taxonName = unit.get("taxonName");

        institutionCode = unit.get("institution");
        collectionCode = unit.get("collection");
        unitID = unit.get("unitID");
        recordBasis = unit.get("recordBasis");

        accessionNumber = null;

        try {longitude = Double.valueOf(unit.get("longitude"));
        } catch (Exception e) {longitude = 0.0;}

        try {latitude = Double.valueOf(unit.get("latitude"));
        } catch (Exception e) {latitude = 0.0;}

        country = unit.get("country");
        isocountry = unit.get("isoCountry");
        locality = unit.get("locality");

        fieldNumber = unit.get("field number");

        String url =unit.get("url");
        if (!url.isEmpty()) {
            multimediaObjects.add(url);
        }

        String coll =unit.get("collector");
        if (!coll.isEmpty()) {
            if (coll.indexOf("& al.")!=-1 || coll.indexOf("et al.") != -1 || coll.indexOf(" al. ")!=-1 || coll.indexOf("&") != -1 || coll.indexOf(" et ") != -1) {
                gatheringTeam = coll;
            } else{
                //single
                gatheringAgent = coll;
            }
        }

        identificationList.add(taxonName+" "+author);

        gatheringYear = unit.get("year");
        gatheringMonth = unit.get("month");
        gatheringDay = unit.get("day");
        gatheringDate = unit.get("date");

        originalsource = unit.get("originalsource");
    }


    private Institution getInstitution(SpecimenSynthesysExcelImportConfigurator config){
        Institution institution;
        List<Institution> institutions;
        try{
            institutions= getAgentService().searchInstitutionByCode(institutionCode);
        }catch(Exception e){
            institutions=new ArrayList<Institution>();
        }
        if (institutions.size() ==0 || !config.getReUseExistingMetadata()){
            logger.debug("Institution (agent) unknown or not allowed to reuse existing metadata");
            //create institution
            institution = Institution.NewInstance();
            institution.setCode(institutionCode);
        }
        else{
            logger.debug("Institution (agent) already in the db");
            institution = institutions.get(0);
        }
        return institution;
    }

    /*
     * Look if the Collection does already exists
     * @param collectionCode: a string
     * @param institution: the current Institution
     * @param app
     * @return the Collection (existing or new)
     */
    private Collection getCollection(Institution institution, SpecimenSynthesysExcelImportConfigurator config){
        Collection collection = Collection.NewInstance();
        List<Collection> collections;
        try{
            collections = getCollectionService().searchByCode(collectionCode);
        }catch(Exception e){
            collections=new ArrayList<Collection>();
        }
        if (collections.size() ==0 || !config.getReUseExistingMetadata()){
            logger.debug("Collection not found or do not reuse existing metadata  "+collectionCode);
            //create new collection
            collection.setCode(collectionCode);
            collection.setCodeStandard("GBIF");
            collection.setInstitute(institution);
        }
        else{
            boolean collectionFound=false;
            for (int i=0; i<collections.size(); i++){
                collection = collections.get(i);
                try {
                    if (collection.getInstitute().getCode().equalsIgnoreCase(institution.getCode())){
                        //found a collection with the same code and the same institution
                        collectionFound=true;
                    }
                } catch (NullPointerException e) {}
            }
            if (!collectionFound){
                collection.setCode(collectionCode);
                collection.setCodeStandard("GBIF");
                collection.setInstitute(institution);
            }

        }
        return collection;
    }

    /*
     *
     * @param app
     * @param derivedThing
     * @param sec
     */
    private void setTaxonNameBase(SpecimenSynthesysExcelImportConfigurator config){
        NonViralName<?> taxonName = null;
        Taxon taxon = null;

        String scientificName="";
        boolean preferredFlag=false;

        for (int i = 0; i < identificationList.size(); i++) {
            String fullScientificNameString = identificationList.get(i);
            fullScientificNameString = fullScientificNameString.replaceAll(" et ", " & ");
            if (fullScientificNameString.indexOf("_preferred_") != -1){
                scientificName = fullScientificNameString.split("_preferred_")[0].trim();
                String pTmp = fullScientificNameString.split("_preferred_")[1].split("_code_")[0];
                if (pTmp == "1" || pTmp.toLowerCase().indexOf("true") != -1) {
                    preferredFlag=true;
                } else {
                    preferredFlag=false;
                }
            }else {
                scientificName = fullScientificNameString.trim();
            }

            if (fullScientificNameString.indexOf("_code_") != -1){
                nomenclatureCode = fullScientificNameString.split("_code_")[1].trim();
            }

            if (config.getDoReUseTaxon()){
                List<TaxonBase> c = null;
                try {
                    Taxon cc= getTaxonService().findBestMatchingTaxon(scientificName);
                    if (cc != null){
                        if ((cc.getSec() == null || cc.getSec().toString().isEmpty()) || (cc.getSec() != null && cc.getSec().getTitleCache().equalsIgnoreCase(ref.getTitleCache()))) {
                            if(cc.getSec() == null || cc.getSec().toString().isEmpty()){
                                cc.setSec(ref);
                                getTaxonService().saveOrUpdate(cc);
                            }
                            taxon=cc;
                        }
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
            if (!config.getDoReUseTaxon() || taxon == null){
                if (DEBUG) {
                    logger.info("create new taxonName instance");
                }
                if (config.getDoAutomaticParsing()){
                    taxonName = parseScientificName(scientificName);
                }
                else{
                    taxonName = NonViralName.NewInstance(Rank.UNKNOWN_RANK());
                    taxonName.setTitleCache(scientificName, true);
                }
                getNameService().save(taxonName);
                taxon = Taxon.NewInstance(taxonName, ref); //sec set null
                getTaxonService().save(taxon);
                //   refreshTransaction();
                taxon = (Taxon) getTaxonService().find(taxon.getUuid());
                taxon = addTaxonNode(taxon, config);
            }



            DeterminationEvent determinationEvent = DeterminationEvent.NewInstance();
            determinationEvent.setTaxon(getTaxonService().find(taxon.getUuid()));
            determinationEvent.setPreferredFlag(preferredFlag);

            determinationEvent.setIdentifiedUnit(derivedUnitBase);

            derivedUnitBase.addDetermination(determinationEvent);

            makeIndividualsAssociation(taxon,determinationEvent);

            getOccurrenceService().saveOrUpdate(derivedUnitBase);
        }

    }

    /**
     * @param taxon
     * @param taxonName
     * @param config
     * @return
     */
    private Taxon addTaxonNode(Taxon taxon, SpecimenSynthesysExcelImportConfigurator config) {
        if (DEBUG) {
            logger.info("link taxon to a taxonNode");
        }
        boolean exist = false;
        for (TaxonNode p : classification.getAllNodes()){
            if(p.getTaxon().equals(taxon)) {
                exist =true;
            }
        }
        if (!exist){
            taxon = (Taxon) getTaxonService().find(taxon.getUuid());
            classification.addChildTaxon(taxon, ref, "", null);
            getClassificationService().saveOrUpdate(classification);
            //            refreshTransaction();
        }
        return (Taxon) getTaxonService().find(taxon.getUuid());
    }

    private NonViralName<?> parseScientificName(String scientificName){
        if (DEBUG) {
            logger.debug("in parseScientificName");
        }
        NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
        NonViralName<?>taxonName = null;
        boolean problem=false;

        if (DEBUG) {
            logger.debug("nomenclature: "+nomenclatureCode);
        }

        if(nomenclatureCode == null){
            taxonName = NonViralName.NewInstance(Rank.UNKNOWN_RANK());
            taxonName.setTitleCache(scientificName, true);
            return taxonName;
        }

        if (nomenclatureCode.toString().equals("Zoological")){
            taxonName = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICZN,null);
            if (taxonName.hasProblem()) {
                problem=true;
            }
        }
        if (nomenclatureCode.toString().equals("Botanical")){
            taxonName  = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNAFP,null);
            if (taxonName.hasProblem()) {
                problem=true;
            }}
        if (nomenclatureCode.toString().equals("Bacterial")){
            taxonName = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNB, null);
            if (taxonName.hasProblem()) {
                problem=true;
            }
        }
        if (nomenclatureCode.toString().equals("Cultivar")){
            taxonName = nvnpi.parseFullName(scientificName,NomenclaturalCode.ICNCP, null);
            if (taxonName.hasProblem()) {
                problem=true;
            }
        }
        //      if (nomenclatureCode.toString().equals("Viral")){
        //      ViralName taxonName = (ViralName)nvnpi.parseFullName(scientificName,NomenclaturalCode.ICVCN(), null);
        //      if (taxonName.hasProblem())
        //      System.out.println("pb ICVCN");
        //      }
        //TODO: parsing of ViralNames?
        if(problem){
            taxonName = NonViralName.NewInstance(Rank.UNKNOWN_RANK());
            taxonName.setTitleCache(scientificName, true);
        }
        return taxonName;

    }

    private DerivedUnitFacade getFacade() {
        // logger.info("GETFACADE");
        /**
         * SPECIMEN OR OBSERVATION OR LIVING
         */
        // DerivedUnitBase derivedThing = null;
        DerivedUnitType type = null;

        // create specimen
        if (recordBasis != null) {
            String rec = recordBasis.toLowerCase();
            if (rec.contains("specimen") || rec.startsWith("s")) {// specimen
                type = DerivedUnitType.Specimen;
            }
            if (rec.contains("observat") || rec.startsWith("o")) {
                type = DerivedUnitType.Observation;
            }
            if (rec.contains("fossil") || rec.startsWith("f") ){
                type = DerivedUnitType.Fossil;
            }

            if (rec.contains("living") || rec.startsWith("l")) {
                type = DerivedUnitType.LivingBeing;
            }
            if (type == null) {
                if(DEBUG) {
                    logger.info("The basis of record does not seem to be known: "   + recordBasis);
                }
                type = DerivedUnitType.DerivedUnit;
            }
        } else {
            logger.info("The basis of record is null");
            type = DerivedUnitType.DerivedUnit;
        }
        DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(type);
        return derivedUnitFacade;
    }

    /*
     * Store the unit with it's Gathering informations in the CDM
     */
    public boolean start(SpecimenSynthesysExcelImportConfigurator config){
        boolean result = true;

        //        refreshTransaction();
        try {

            /**
             * SPECIMEN OR OBSERVATION OR LIVING
             */
            DerivedUnitFacade derivedUnitFacade = getFacade();
            derivedUnitBase = derivedUnitFacade.innerDerivedUnit();

            //set catalogue number (unitID)
            derivedUnitFacade.setCatalogNumber(unitID);
            derivedUnitFacade.setAccessionNumber(accessionNumber);

            if (!originalsource.isEmpty()){
                Reference<?> reference = ReferenceFactory.newGeneric();
                reference.setTitleCache(originalsource, true);
                derivedUnitBase.addSource(originalsource, "", reference, "");
            }
            /**
             * INSTITUTION & COLLECTION
             */
            //manage institution
            Institution institution = getInstitution(config);
            //manage collection
            Collection collection = getCollection(institution, config);
            //link specimen & collection
            derivedUnitFacade.setCollection(collection);

            /**
             * GATHERING EVENT
             */

            // gathering event
            UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(getTermService(), locality, languageIso,
                    longitude, latitude, gatheringAgent, gatheringTeam,config);

            // country
            UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea(isocountry, country, this);
            NamedArea areaCountry = unitsGatheringArea.getArea();



            // copy gathering event to facade
            GatheringEvent gatheringEvent = unitsGatheringEvent.getGatheringEvent();

            //join gatheringEvent to fieldObservation
            derivedUnitFacade.setGatheringEvent(gatheringEvent);

            derivedUnitFacade.setLocality(gatheringEvent.getLocality());
            derivedUnitFacade.setExactLocation(gatheringEvent.getExactLocation());
            //derivedUnitFacade.setCollector(gatheringEvent.getCollector());
            derivedUnitFacade.setCountry(areaCountry);
            derivedUnitFacade.addCollectingAreas(unitsGatheringArea.getAreas());


            /*
             * merge AND STORE DATA
             */
            getTermService().saveOrUpdate(areaCountry);// TODO save area sooner
            getTermService().saveLanguageData(unitsGatheringEvent.getLocality());



            //add gathering date
            if (keepAtomisedDate && (!gatheringYear.isEmpty() || !gatheringMonth.isEmpty() || !gatheringDay.isEmpty())){
                Calendar calendar =  Calendar.getInstance();
                if (!gatheringYear.isEmpty()) {
                    TimePeriod tp = TimePeriod.NewInstance(Integer.parseInt(gatheringYear));
                    if (!gatheringMonth.isEmpty()) {
                        tp.setStartMonth(Integer.parseInt(gatheringMonth));
                        if (!gatheringDay.isEmpty()) {
                            tp.setStartDay(Integer.parseInt(gatheringDay));
                        }
                    }
                    unitsGatheringEvent.setGatheringDate(tp);
                }

            }else{
                if (!gatheringDate.isEmpty()){
                    TimePeriod tp = TimePeriod.parseString(gatheringDate);
                    unitsGatheringEvent.setGatheringDate(tp);
                }
            }


            //add fieldNumber
            derivedUnitFacade.setFieldNumber(NB(fieldNumber));

            //add Multimedia URLs
            if(multimediaObjects.size()>0){
                for (String multimediaObject : multimediaObjects) {
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

            getOccurrenceService().saveOrUpdate(derivedUnitBase);

            setTaxonNameBase(config);

            //         refreshTransaction();
            if (DEBUG) {
                logger.info("saved new specimen ...");
            }

        } catch (Exception e) {
            logger.warn("Error when reading record!!");
            e.printStackTrace();
            result = false;
        }
        if (DEBUG) {
            logger.info("commit done");
        }
        //app.close();
        return result;
    }


    private Feature makeFeature(SpecimenOrObservationBase unit) {
        if (unit.isInstanceOf(DerivedUnit.class)) {
            return Feature.INDIVIDUALS_ASSOCIATION();
        } else if (unit.isInstanceOf(FieldObservation.class)
                || unit.isInstanceOf(Observation.class)) {
            return Feature.OBSERVATION();
        } else if (unit.isInstanceOf(Fossil.class)
                || unit.isInstanceOf(LivingBeing.class)
                || unit.isInstanceOf(Specimen.class)) {
            return Feature.SPECIMEN();
        }
        if (DEBUG) {
            logger.warn("No feature defined for derived unit class: "
                    + unit.getClass().getSimpleName());
        }
        return null;
    }

    private void makeIndividualsAssociation(Taxon taxon, DeterminationEvent determinationEvent) {
        //        try{
        //            taxon = (Taxon) getTaxonService().find(taxon.getUuid());
        //        }
        //        catch(Exception e){
        //            //logger.info("taxon uptodate");
        //        }

        TaxonDescription taxonDescription=null;
        if (taxon.getTitleCache().contains("Chenopodium vulvaria L.")) {
            taxonDescription = (TaxonDescription) getDescriptionService().find(UUID.fromString("a60074e7-979b-41fd-ad4d-d391db474ac4"));
        }
        if(!taxon.getTitleCache().contains("Chenopodium vulvaria L.") || taxonDescription ==null) {
            taxonDescription = getTaxonDescription(taxon, ref, false, true);
            taxonDescription.setTitleCache(ref.getTitleCache(), true);
        }


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


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
     */
    @Override
    protected boolean isIgnore(SpecimenSynthesysExcelImportState state) {
        return false;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.specimen.SpecimenIoBase#doInvoke(eu.etaxonomy.cdm.io.specimen.abcd206.SpecimenImportState)
     */
    @Override
    protected void doInvoke(SpecimenSynthesysExcelImportState state) {
        boolean success = true;
        if (state.getConfig().doAskForDate()) {
            keepAtomisedDate = askQuestion("Gathering dates can be stored in either atomised fieds (day month year) or in a concatenated field."+
                    "\nWhich value do you want to store?\nPress 1 for the atomised, press 2 for the concatenated field, and then press enter.");
        }

        tx = startTransaction();

        ref = state.getConfig().getTaxonReference();
        if (ref == null){
            ref = state.getConfig().getSourceReference();
        }
        getReferenceService().saveOrUpdate(ref);
        setClassification(state);

        refreshTransaction();

        URI source = state.getConfig().getSource();
        ArrayList<HashMap<String,String>> unitsList = null;
        try{
            unitsList = ExcelUtils.parseXLS(source);
        } catch(FileNotFoundException e){
            String message = "File not found: " + source;
            warnProgress(state, message, e);
            logger.error(message);
        }

        logger.info("unitslist : "+unitsList.size());
        if (unitsList != null){
            //load collectors in the database
            prepareCollectors(unitsList,state);
            HashMap<String,String> unit=null;
            MyHashMap<String,String> myunit;
            for (int i=0; i<unitsList.size();i++){
                if (i%100==0) {
                    logger.info("NbUnit prepared: "+i);
                    refreshTransaction();
                }
                unit = unitsList.get(i);
                myunit=new MyHashMap<String, String>();
                for (String key :unit.keySet()) {
                    myunit.put(key, unit.get(key));
                }
                //FIXME do this via state
                setUnitPropertiesExcel(myunit, state.getConfig().getDefaultAuthor());//and then invoke
                success &= start(state.getConfig());
            }
        }
        if (success == false){
            state.setUnsuccessfull();
        }
        commitTransaction(tx);
        return;
    }


    /**
     * @param unitsList
     * @param state
     */
    private void prepareCollectors(ArrayList<HashMap<String, String>> unitsList, SpecimenSynthesysExcelImportState state) {
        List<String> collectors = new ArrayList<String>();
        List<String> teams = new ArrayList<String>();
        List<List<String>> collectorinteams = new ArrayList<List<String>>();
        String tmp;
        for (HashMap<String,String> unit:unitsList){
            tmp=null;
            tmp = unit.get("collector");
            if (tmp != null && !tmp.isEmpty()) {
                if (tmp.indexOf("et al.") != -1 || tmp.indexOf(" al. ")!=-1 || tmp.indexOf("& al.")!=-1 ) {
                    if (!tmp.trim().isEmpty()) {
                        teams.add(tmp.trim());
                    }
                } else{
                    if(tmp.indexOf(" et ")!=-1 || tmp.indexOf("&")!=-1){
                        List<String> collteam = new ArrayList<String>();
                        String[] tmp1 = tmp.split(" et ");
                        for (String elt:tmp1){
                            String tmp2[] = elt.split("&");
                            for (String elt2:tmp2) {
                                if (!elt2.trim().isEmpty()) {
                                    collectors.add(elt2.trim());
                                    collteam.add(elt2.trim());
                                }
                            }
                        }
                        if (collteam.size()>0) {
                            collectorinteams.add(new ArrayList<String>(new HashSet<String>(collteam)));
                        }
                    }
                    else
                        if (!tmp.trim().isEmpty() && !tmp.toString().trim().equalsIgnoreCase("none")) {
                            collectors.add(tmp.trim());
                        }
                }
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
        Map<UUID, AgentBase> uuuidPerson = getAgentService().save(personToadd);
        for (UUID u:uuuidPerson.keySet()){
            titleCachePerson.put(uuuidPerson.get(u).getTitleCache(),(Person) uuuidPerson.get(u) );
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


        Map<UUID, AgentBase> uuuidTeam =  getAgentService().save(teamToAdd);

        for (UUID u:uuuidTeam.keySet()){
            titleCacheTeam.put(uuuidTeam.get(u).getTitleCache(), (Team) uuuidTeam.get(u) );
        }

        state.getConfig().setTeams(titleCacheTeam);
        state.getConfig().setPersons(titleCachePerson);
    }

    private boolean askQuestion(String question){
        Scanner scan = new Scanner(System.in);
        System.out.println(question);
        int index = scan.nextInt();
        if (index == 1) {
            return true;
        } else {
            return false;
        }
    }




    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
     */
    @Override
    protected boolean doCheck(SpecimenSynthesysExcelImportState state) {
        logger.warn("Validation not yet implemented for " + getClass().getSimpleName());
        return true;
    }


}
