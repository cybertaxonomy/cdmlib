// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.taxonx2013;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;

/**
 * @author pkelbert
 * @date 2 avr. 2013
 *
 */
public class TaxonXExtractor {

    protected TaxonXImport importer;
    protected TaxonXImportState configState;


    public class MySpecimenOrObservation{
        String descr="";
        @SuppressWarnings("rawtypes")
        DerivedUnitBase derivedUnitBase=null;

        public String getDescr() {
            return descr;
        }
        public void setDescr(String descr) {
            this.descr = descr;
        }
        @SuppressWarnings("rawtypes")
        public DerivedUnitBase getDerivedUnitBase() {
            return derivedUnitBase;
        }
        @SuppressWarnings("rawtypes")
        public void setDerivedUnitBase(DerivedUnitBase derivedUnitBase) {
            this.derivedUnitBase = derivedUnitBase;
        }




    }

    /**
     * @param item
     * @return
     */
    @SuppressWarnings({ "unused", "null", "rawtypes" })
    protected MySpecimenOrObservation extractSpecimenOrObservation(Node specimenObservationNode, DerivedUnitBase derivedUnitBase) {
        String country=null;
        String locality=null;
        String stateprov=null;
        String collector=null;
        String fieldNumber=null;
        Double latitude=null,longitude=null;
        String descr="";
        boolean asso=false;
        NodeList eventContent =null;
        // create facade
        DerivedUnitFacade derivedUnitFacade = null;

        UnitsGatheringEvent unitsGatheringEvent;
        UnitsGatheringArea unitsGatheringArea;
        DefinedTermBase areaCountry;

        MySpecimenOrObservation specimenOrObservation = new MySpecimenOrObservation();

        NodeList xmldata= specimenObservationNode.getChildNodes();
        for (int n=0;n<xmldata.getLength();n++){
            eventContent=xmldata.item(n).getChildNodes();
            if (xmldata.item(n).getNodeName().equalsIgnoreCase("tax:xmldata")){
                asso=true;
                country=null;
                locality=null;
                stateprov=null;
                collector=null;
                fieldNumber=null;
                latitude=null;
                longitude=null;
                for (int j=0;j<eventContent.getLength();j++){
                    if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:country")){
                        country=eventContent.item(j).getTextContent().trim();
                    }
                    if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:locality")){
                        locality=eventContent.item(j).getTextContent().trim();
                    }
                    if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:stateprovince")){
                        stateprov=eventContent.item(j).getTextContent().trim();
                    }
                    if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:collector")){
                        collector=eventContent.item(j).getTextContent().trim();
                    }
                    if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:decimallongitude")){
                        String tmp = eventContent.item(j).getTextContent().trim();
                        try{longitude=Double.valueOf(tmp);}catch(Exception e){logger.warn("longitude is not a number");}
                    }
                    if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:decimallatitude")){
                        String tmp = eventContent.item(j).getTextContent().trim();
                        try{latitude=Double.valueOf(tmp);}catch(Exception e){logger.warn("latitude is not a number");}
                    }

                }
            }
            if(xmldata.item(n).getNodeName().equalsIgnoreCase("#text")){
                asso=true;
                descr=xmldata.item(n).getTextContent().trim();
                specimenOrObservation.setDescr(descr);
            }
            if(xmldata.item(n).getNodeName().equalsIgnoreCase("tax:p")){
                asso=true;
                descr=xmldata.item(n).getTextContent().trim();
                specimenOrObservation.setDescr(descr);
            }
        }
        if(asso){

            logger.info("DESCR: "+descr);
            derivedUnitFacade = getFacade(descr);
            derivedUnitBase = derivedUnitFacade.innerDerivedUnit();
            unitsGatheringEvent = new UnitsGatheringEvent(importer.getTermService(), locality,collector,longitude, latitude,
                    configState.getConfig(),importer.getAgentService());

            // country
            unitsGatheringArea = new UnitsGatheringArea();
            unitsGatheringArea.setConfig(configState.getConfig(),importer.getOccurrenceService(), importer.getTermService());
            unitsGatheringArea.setParams(null, country);

            areaCountry =  unitsGatheringArea.getCountry();

            //                         // other areas
            //                         unitsGatheringArea = new UnitsGatheringArea(namedAreaList,dataHolder.getTermService());
            //                         ArrayList<DefinedTermBase> nas = unitsGatheringArea.getAreas();
            //                         for (DefinedTermBase namedArea : nas) {
            //                             unitsGatheringEvent.addArea(namedArea);
            //                         }

            // copy gathering event to facade
            GatheringEvent gatheringEvent = unitsGatheringEvent.getGatheringEvent();
            derivedUnitFacade.setLocality(gatheringEvent.getLocality());
            derivedUnitFacade.setExactLocation(gatheringEvent.getExactLocation());
            derivedUnitFacade.setCollector(gatheringEvent.getCollector());
            derivedUnitFacade.setCountry((NamedArea)areaCountry);

            for(DefinedTermBase<?> area:unitsGatheringArea.getAreas()){
                derivedUnitFacade.addCollectingArea((NamedArea) area);
            }
            //                         derivedUnitFacade.addCollectingAreas(unitsGatheringArea.getAreas());

            // TODO exsiccatum

            // add fieldNumber
            if (fieldNumber != null) {
                derivedUnitFacade.setFieldNumber(fieldNumber);
            }
            specimenOrObservation.setDerivedUnitBase(derivedUnitBase);
        }
        return specimenOrObservation;
    }

    protected DerivedUnitFacade getFacade(String recordBasis) {
        logger.info("getFacade()");
        DerivedUnitType type = null;

        // create specimen
        if (recordBasis != null) {
            if (recordBasis.toLowerCase().startsWith("specimen") || recordBasis.toLowerCase().contains("specimen")) {// specimen
                type = DerivedUnitType.Specimen;
            }
            if (recordBasis.toLowerCase().startsWith("observation")) {
                type = DerivedUnitType.Observation;
            }
            if (recordBasis.toLowerCase().contains("fossil")) {
                type = DerivedUnitType.Fossil;
            }

            if (recordBasis.toLowerCase().startsWith("living")) {
                type = DerivedUnitType.LivingBeing;
            }
            if (type == null) {
                logger.info("The basis of record does not seem to be known: " + recordBasis);
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



    @SuppressWarnings("rawtypes")
    protected Feature makeFeature(SpecimenOrObservationBase unit) {
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


    protected final static String SPLITTER = ",";
    Logger logger = Logger.getLogger(getClass());

    protected  int askQuestion(String question){
        Scanner scan = new Scanner(System.in);
        logger.info(question);
        int index = scan.nextInt();
        return index;
    }


    /**
     * @param reftype
     * @return
     */
    protected Reference<?> getReferenceType(int reftype) {
        Reference<?> ref = null;
        switch (reftype) {
        case 1:
            ref = ReferenceFactory.newGeneric();
            break;
        case 2:
            IBook tmp= ReferenceFactory.newBook();
            ref = (Reference<?>)tmp;
            break;
        case 3:
            ref = ReferenceFactory.newArticle();
            break;
        case 4:
            IBookSection tmp2 = ReferenceFactory.newBookSection();
            ref = (Reference<?>)tmp2;
            break;
        case 5:
            ref = ReferenceFactory.newJournal();
            break;
        case 6:
            ref = ReferenceFactory.newPrintSeries();
            break;
        case 7:
            ref = ReferenceFactory.newThesis();
            break;
        default:
            break;
        }
        return ref;
    }
    /**
     * @param unitsList
     * @param state
     */
    protected void prepareCollectors(TaxonXImportState state,IAgentService agentService) {
        logger.info("PREPARE COLLECTORS");
        List<String> collectors = new ArrayList<String>();
        String tmp;
        List<String> collectorsU = new ArrayList<String>(new HashSet<String>(collectors));
        Set<UUID> uuids = new HashSet<UUID>();

        //existing persons in DB
        List<UuidAndTitleCache<Person>> hiberPersons = agentService.getPersonUuidAndTitleCache();
        Map<String,Person> titleCachePerson = new HashMap<String, Person>();
        uuids = new HashSet<UUID>();
        for (UuidAndTitleCache<Person> hibernateP:hiberPersons){
            uuids.add(hibernateP.getUuid());
        }

        if (!uuids.isEmpty()){
            List<AgentBase> existingPersons = agentService.find(uuids);
            for (AgentBase existingP:existingPersons){
                titleCachePerson.put(existingP.getTitleCache(),(Person) existingP);
            }
        }

        Map<String,UUID> personMap = new HashMap<String, UUID>();
        for (UuidAndTitleCache<Person> person:hiberPersons){
            personMap.put(person.getTitleCache(), person.getUuid());
        }

        java.util.Collection<AgentBase> personToadd = new ArrayList<AgentBase>();

        for (String collector:collectorsU){
            Person p = Person.NewInstance();
            p.setTitleCache(collector,true);
            if (!personMap.containsKey(p.getTitleCache())){
                personToadd.add(p);
            }
        }

        if(!personToadd.isEmpty()){
            Map<UUID, AgentBase> uuuidPerson = agentService.save(personToadd);
            for (UUID u:uuuidPerson.keySet()){
                titleCachePerson.put(uuuidPerson.get(u).getTitleCache(),(Person) uuuidPerson.get(u) );
            }
        }

        state.getConfig().setPersons(titleCachePerson);
    }

    /**
     * @param name
     * @return
     */
    protected String getFullReference(String name, List<ParserProblem> problems) {
        logger.info("getFullReference for "+ name);
        JFrame frame = new JFrame("I have a question");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String s = (String)JOptionPane.showInputDialog(
                frame,
                "Complete the reference '"+name+"' (use Euro+Med Checklist for Plants).\nThe current problem is "+StringUtils.join(problems,"--"),
                "Get full reference name",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                name);
        return s;
    }

    /**
     * @param name
     * @return
     */
    protected String getScientificName(String fullname,String atomised) {
        logger.info("getScientificName for "+ fullname);
        JFrame frame = new JFrame("I have a question");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String s = (String)JOptionPane.showInputDialog(
                frame,
                "The names in the free text and in the xml tags do not match : "+fullname+", or "+atomised,
                "Which name do I have to use? ",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                fullname);
        return s;
    }

    /**
     * @param t
     * @param classification
     * @return
     */
    protected Taxon askParent(Taxon taxon,Classification classification ) {
        logger.info("ask Parent ");
        Set<TaxonNode> allNodes = classification.getAllNodes();
        Map<String,Taxon> nodesMap = new HashMap<String, Taxon>();

        for (TaxonNode tn:allNodes){
            Taxon t = tn.getTaxon();
            nodesMap.put(t.getTitleCache(), t);
        }
        List<String> nodeList = new ArrayList<String>();
        nodeList.add("It is the root");
        for (String nl : nodesMap.keySet()) {
            nodeList.add(nl);
        }

        JFrame frame = new JFrame("I have a question");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String s = (String)JOptionPane.showInputDialog(
                frame,
                "What is the taxon parent for "+taxon.getTitleCache()+"?",
                " ",
                JOptionPane.PLAIN_MESSAGE,
                null,
                nodeList.toArray(),
                null);

        Taxon returnTaxon = nodesMap.get(s);
        return returnTaxon;
    }
}


