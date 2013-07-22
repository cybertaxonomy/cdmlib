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

import java.awt.Dimension;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
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
        DerivedUnit derivedUnitBase=null;

        public String getDescr() {
            return descr;
        }
        public void setDescr(String descr) {
            this.descr = descr;
        }
        @SuppressWarnings("rawtypes")
        public DerivedUnit getDerivedUnitBase() {
            return derivedUnitBase;
        }
        @SuppressWarnings("rawtypes")
        public void setDerivedUnitBase(DerivedUnit derivedUnitBase) {
            this.derivedUnitBase = derivedUnitBase;
        }




    }

    /**
     * @param item
     * @return
     */
    @SuppressWarnings({ "unused", "null", "rawtypes" })
    protected MySpecimenOrObservation extractSpecimenOrObservation(Node specimenObservationNode, DerivedUnit derivedUnitBase) {
        String country=null;
        String locality=null;
        String stateprov=null;
        String collector=null;
        String fieldNumber=null;
        Double latitude=null,longitude=null;
        TimePeriod tp =null;
        String day,month,year="";
        String descr="";
        String type="";
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
                day="";
                month="";
                year="";
                type="";
                for (int j=0;j<eventContent.getLength();j++){
                    if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:country")){
                        country=eventContent.item(j).getTextContent().trim();
                    }
                    else if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:locality")){
                        locality=eventContent.item(j).getTextContent().trim();
                    }
                    else  if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:stateprovince")){
                        stateprov=eventContent.item(j).getTextContent().trim();
                    }
                    else  if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:collector")){
                        collector=eventContent.item(j).getTextContent().trim();
                    }
                    else  if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:yearcollected")){
                        year=eventContent.item(j).getTextContent().trim();
                    }
                    else  if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:monthcollected")){
                        month=eventContent.item(j).getTextContent().trim();
                    }
                    else  if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:daycollected")){
                        day=eventContent.item(j).getTextContent().trim();
                    }
                    else  if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:decimallongitude")){
                        String tmp = eventContent.item(j).getTextContent().trim();
                        try{longitude=Double.valueOf(tmp);}catch(Exception e){logger.warn("longitude is not a number");}
                    }
                    else  if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:decimallatitude")){
                        String tmp = eventContent.item(j).getTextContent().trim();
                        try{latitude=Double.valueOf(tmp);}catch(Exception e){logger.warn("latitude is not a number");}
                    }else if(eventContent.item(j).getNodeName().equalsIgnoreCase("dwc:TypeStatus")){
                        type = eventContent.item(j).getTextContent().trim();
                    }
                    else {
                        logger.info("UNEXTRACTED FIELD FOR SPECIMEN "+eventContent.item(j).getNodeName()+", "+eventContent.item(j).getTextContent()) ;
                    }
                }
                if (!day.isEmpty() || !month.isEmpty() || !year.isEmpty()){
                    try{
                        if (!year.isEmpty()) {
                            tp = TimePeriod.NewInstance(Integer.parseInt(year));
                            if (!month.isEmpty()) {
                                tp.setStartMonth(Integer.parseInt(month));
                                if (!day.isEmpty()) {
                                    tp.setStartDay(Integer.parseInt(day));
                                }
                            }

                        }
                    }catch(Exception e){
                        logger.warn("Collection date error "+e);
                    }
                }
            }
            if(xmldata.item(n).getNodeName().equalsIgnoreCase("#text")){
                descr=xmldata.item(n).getTextContent().trim();
                if (descr.length()>1) {
                    specimenOrObservation.setDescr(descr);
                    asso=true;
                }
            }
            if(xmldata.item(n).getNodeName().equalsIgnoreCase("tax:p")){
                descr=xmldata.item(n).getTextContent().trim();
                if (descr.length()>1) {
                    specimenOrObservation.setDescr(descr);
                    asso=true;
                }
            }
        }
        if(asso && descr.length()>1){

            //            logger.info("DESCR: "+descr);
            if (!type.isEmpty()) {
                derivedUnitFacade = getFacade(type);
                SpecimenTypeDesignation designation = SpecimenTypeDesignation.NewInstance();
                SpecimenTypeDesignationStatus stds= getSpecimenTypeDesignationStatusByKey(type);
                if (stds !=null) {
                    stds = (SpecimenTypeDesignationStatus) importer.getTermService().find(stds.getUuid());
                }

                designation.setTypeStatus(stds);
                derivedUnitFacade.innerDerivedUnit().addSpecimenTypeDesignation(designation);

                derivedUnitBase = derivedUnitFacade.innerDerivedUnit();
                //                designation.setTypeSpecimen(derivedUnitBase);
                //                TaxonNameBase<?,?> name = taxon.getName();
                //                name.addTypeDesignation(designation, true);
            } else {
                derivedUnitFacade = getFacade(descr);
                derivedUnitBase = derivedUnitFacade.innerDerivedUnit();
            }

            unitsGatheringEvent = new UnitsGatheringEvent(importer.getTermService(), locality,collector,longitude, latitude,
                    configState.getConfig(),importer.getAgentService());
            
            if(tp!=null) {
                unitsGatheringEvent.setGatheringDate(tp);
            }

            // country
            unitsGatheringArea = new UnitsGatheringArea();
            unitsGatheringArea.setParams(null, country, configState.getConfig(), importer.getTermService(), importer.getOccurrenceService());

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


    private SpecimenTypeDesignationStatus getSpecimenTypeDesignationStatusByKey(
            String key) {
        if (key == null) {
            return null;
        } else if (key.matches("(?i)(T|Type)")) {
            return SpecimenTypeDesignationStatus.TYPE();
        } else if (key.matches("(?i)(HT|Holotype)")) {
            return SpecimenTypeDesignationStatus.HOLOTYPE();
        } else if (key.matches("(?i)(LT|Lectotype)")) {
            return SpecimenTypeDesignationStatus.LECTOTYPE();
        } else if (key.matches("(?i)(NT|Neotype)")) {
            return SpecimenTypeDesignationStatus.NEOTYPE();
        } else if (key.matches("(?i)(ST|Syntype)")) {
            return SpecimenTypeDesignationStatus.SYNTYPE();
        } else if (key.matches("(?i)(ET|Epitype)")) {
            return SpecimenTypeDesignationStatus.EPITYPE();
        } else if (key.matches("(?i)(IT|Isotype)")) {
            return SpecimenTypeDesignationStatus.ISOTYPE();
        } else if (key.matches("(?i)(ILT|Isolectotype)")) {
            return SpecimenTypeDesignationStatus.ISOLECTOTYPE();
        } else if (key.matches("(?i)(INT|Isoneotype)")) {
            return SpecimenTypeDesignationStatus.ISONEOTYPE();
        } else if (key.matches("(?i)(IET|Isoepitype)")) {
            return SpecimenTypeDesignationStatus.ISOEPITYPE();
        } else if (key.matches("(?i)(PT|Paratype)")) {
            return SpecimenTypeDesignationStatus.PARATYPE();
        } else if (key.matches("(?i)(PLT|Paralectotype)")) {
            return SpecimenTypeDesignationStatus.PARALECTOTYPE();
        } else if (key.matches("(?i)(PNT|Paraneotype)")) {
            return SpecimenTypeDesignationStatus.PARANEOTYPE();
        } else if (key.matches("(?i)(unsp.|Unspecified)")) {
            return SpecimenTypeDesignationStatus.UNSPECIFIC();
        } else if (key.matches("(?i)(2LT|Second Step Lectotype)")) {
            return SpecimenTypeDesignationStatus.SECOND_STEP_LECTOTYPE();
        } else if (key.matches("(?i)(2NT|Second Step Neotype)")) {
            return SpecimenTypeDesignationStatus.SECOND_STEP_NEOTYPE();
        } else if (key.matches("(?i)(OM|Original Material)")) {
            return SpecimenTypeDesignationStatus.ORIGINAL_MATERIAL();
        } else if (key.matches("(?i)(IcT|Iconotype)")) {
            return SpecimenTypeDesignationStatus.ICONOTYPE();
        } else if (key.matches("(?i)(PT|Phototype)")) {
            return SpecimenTypeDesignationStatus.PHOTOTYPE();
        } else if (key.matches("(?i)(IST|Isosyntype)")) {
            return SpecimenTypeDesignationStatus.ISOSYNTYPE();
        } else {
            return null;
        }
    }
    protected DerivedUnitFacade getFacade(String recordBasis) {
        //        logger.info("getFacade()");
    	SpecimenOrObservationType type = null;

        // create specimen
        if (recordBasis != null) {
            String recordBasisL = recordBasis.toLowerCase();
            if (recordBasisL.startsWith("specimen") || recordBasisL.contains("specimen") || recordBasisL.contains("type")) {// specimen
                type = SpecimenOrObservationType.PreservedSpecimen;
            }
            if (recordBasisL.startsWith("observation")) {
                type = SpecimenOrObservationType.Observation;
            }
            if (recordBasisL.contains("fossil")) {
                type = SpecimenOrObservationType.Fossil;
            }

            if (recordBasisL.startsWith("living")) {
                type = SpecimenOrObservationType.LivingSpecimen;
            }
            if (type == null) {
                logger.info("The basis of record does not seem to be known: *" + recordBasisL+"*");
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



    @SuppressWarnings("rawtypes")
    protected Feature makeFeature(SpecimenOrObservationBase unit) {
    	if (unit == null){
        	return null;
        }
        SpecimenOrObservationType type = unit.getRecordBasis();
    	
    	if (type.isFeatureObservation()){
        	return Feature.OBSERVATION();
        }else if (type.isPreservedSpecimen() || 
        		type == SpecimenOrObservationType.LivingSpecimen ||
        	    type == SpecimenOrObservationType.OtherSpecimen
        		){
        	return Feature.SPECIMEN();
        }else if (type == SpecimenOrObservationType.Unknown || 
        		type == SpecimenOrObservationType.DerivedUnit 
        		) {
            return Feature.INDIVIDUALS_ASSOCIATION();
        }
        logger.warn("No feature defined for derived unit class: "
                    + unit.getClass().getSimpleName());
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
        //        logger.info("PREPARE COLLECTORS");
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
                titleCachePerson.put(existingP.getTitleCache(),CdmBase.deproxy(existingP, Person.class));
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
                titleCachePerson.put(uuuidPerson.get(u).getTitleCache(), CdmBase.deproxy(uuuidPerson.get(u), Person.class));
            }
        }

        state.getConfig().setPersons(titleCachePerson);
    }

    /**
     * @param name
     * @return
     */
    protected String getFullReference(String name, List<ParserProblem> problems) {
        //        logger.info("getFullReference for "+ name);
        JTextArea textArea = new JTextArea("Complete the reference '"+name+"' (use Euro+Med Checklist for Plants).\nThe current problem is "+StringUtils.join(problems,"--"));
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 100 ) );

        //        JFrame frame = new JFrame("I have a question");
        //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String s = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
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
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     */
    protected String getScientificName(String fullname,String atomised,String classificationName, Node fullParagraph) throws TransformerFactoryConfigurationError, TransformerException {
        //        logger.info("getScientificName for "+ fullname);
        //        JFrame frame = new JFrame("I have a question");
        //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextArea textArea = new JTextArea("The names in the free text and in the xml tags do not match : "+fullname+
                ", or "+atomised+"\n"+formatNode(fullParagraph));
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 200 ) );
        String s = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                "Which name do I have to use? The current classification is "+classificationName,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                fullname);
        return s;
    }


    protected int askAddParent(String s){
    JTextArea textArea = new JTextArea("If you want to add a parent taxa for "+s+", click \"Yes\"." +
            " If it is a root for this classification, click \"No\" or \"Cancel\".");
    JScrollPane scrollPane = new JScrollPane(textArea);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    scrollPane.setPreferredSize( new Dimension( 700, 200 ) );

    int addTaxon = JOptionPane.showConfirmDialog(null,scrollPane);
    return addTaxon;
    }

    protected String askSetParent(String s){
        JTextArea textArea =  new JTextArea("What is the first taxon parent for "+s+"?\n"+
                "The rank will be asked later. ");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 200 ) );

        s = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                "",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);
        return s;
        }

    protected String askRank(String s, List<String> rankListStr){
        JTextArea  textArea = new JTextArea("What is the rank for "+s+"?");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 200 ) );

       String r = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                "",
                JOptionPane.PLAIN_MESSAGE,
                null,
                rankListStr.toArray(),
                null);
       return r;
    }

    /**
     * @param name
     * @return
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     */
    protected String askFeatureName(String paragraph){
        //        logger.info("getScientificName for "+ fullname);
        //        JFrame frame = new JFrame("I have a question");
        //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextArea textArea = new JTextArea("How should the feature be named? \n"+paragraph);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 200 ) );
        String s = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                "",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "Other");
        return s;
    }


    /**
     * @param name
     * @return
     */
    protected Rank askForRank(String fullname,Rank rank, NomenclaturalCode nomenclaturalCode) {
        //        logger.info("askForRank for "+ fullname+ ", "+rank);
        //        JFrame frame = new JFrame("I have a question");
        //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextArea textArea = new JTextArea("What is the correct rank for "+fullname+"?");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 600, 50 ) );

        List<Rank> rankList = new ArrayList<Rank>();
        rankList = importer.getTermService().listByTermClass(Rank.class, null, null, null, null);

        List<String> rankListStr = new ArrayList<String>();
        for (Rank r:rankList) {
            rankListStr.add(r.toString());
        }
        String s = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                "The rank extracted from the TaxonX file is "+rank.toString(),
                JOptionPane.PLAIN_MESSAGE,
                null,
                rankListStr.toArray(),
                rank.toString());

        Rank cR = null;
        try {
            cR = Rank.getRankByEnglishName(s,nomenclaturalCode,true);
        } catch (UnknownCdmTypeException e) {
            logger.warn("Unknown rank ?!"+s);
            logger.warn(e);
        }
        return cR;
    }

    /**
     * ask user to specify what kind of paragraph the current "multiple" section is
     * default possibilities are "synonyms","material examined","distribution","image caption","other"
     * could make sense to replace this list with the CDM-Feature list
     * if "other" is selected, a second pop-up will be prompted to ask user to specify a new Feature name.
     * @param fullParagraph : the current Node
     * @return the section name
     * */
    protected String askMultiple(Node fullParagraph){
        JTextArea textArea = new JTextArea("What category is it for this paragraph \n"+fullParagraph);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 600, 400 ) );

        String[] possiblities = {"synonyms","material examined","distribution","image caption","other"};


        String s = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                "",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possiblities,
                null);

        if (s.equalsIgnoreCase("other")) {
            try {
                s=askFeatureName(formatNode(fullParagraph));
            } catch (TransformerFactoryConfigurationError e) {
                logger.warn(e);
            } catch (TransformerException e) {
                logger.warn(e);
            }
        }
        return s;

    }



    /**
     * asks for the hierarchical parent, based on the current classification
     * @param taxon
     * @param classification
     * @return Taxon, the parent Taxon
     */
    protected Taxon askParent(Taxon taxon,Classification classification ) {
        //        logger.info("ask Parent "+taxon.getTitleCache());
        Set<TaxonNode> allNodes = classification.getAllNodes();
        Map<String,Taxon> nodesMap = new HashMap<String, Taxon>();

        for (TaxonNode tn:allNodes){
            Taxon t = tn.getTaxon();
            nodesMap.put(t.getTitleCache(), t);
        }
        List<String> nodeList = new ArrayList<String>();
        for (String nl : nodesMap.keySet()) {
            nodeList.add(nl+" - "+nodesMap.get(nl).getName().getRank());
        }
        Collections.sort(nodeList);
        nodeList.add(0, "Not here!");

        JFrame frame = new JFrame("I have a question");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String s = (String)JOptionPane.showInputDialog(
                frame,
                "What is the taxon parent for "+taxon.getTitleCache()+"?",
                "The current classification is "+classification.getTitleCache(),
                JOptionPane.PLAIN_MESSAGE,
                null,
                nodeList.toArray(),
                "Not here!");

        Taxon returnTaxon = nodesMap.get(s.split(" - ")[0]);
        //        logger.info("ask Parent returns "+s);
        return returnTaxon;
    }


    /**
     *
     * @param r: the rank as string (with dwc tags)
     * @return Rank : the Rank object corresponding to the current string
     *
     */
    protected Rank getRank(String r){
        if (r==null) {
            r=Rank.UNKNOWN_RANK().toString();
        }
        r=r.replace("dwcranks:", "");
        r =r.replace("dwc:","");
        //        logger.info("SEARCH RANK FOR "+r);

        Rank rank = Rank.UNKNOWN_RANK();
        if (r.equalsIgnoreCase("Superfamily")) {
            rank=Rank.SUPERFAMILY();
        }
        if (r.equalsIgnoreCase("Family")) {
            rank=Rank.FAMILY();
        }
        if (r.equalsIgnoreCase("Subfamily")) {
            rank=Rank.SUBFAMILY();
        }
        if (r.equalsIgnoreCase("Tribe")) {
            rank=Rank.TRIBE();
        }
        if (r.equalsIgnoreCase("Subtribe")) {
            rank=Rank.SUBTRIBE();
        }
        if (r.equalsIgnoreCase("Genus")) {
            rank=Rank.GENUS();
        }
        if (r.equalsIgnoreCase("Subgenus")) {
            rank=Rank.SUBGENUS();
        }
        if (r.equalsIgnoreCase("Section")) {
            rank=Rank.SECTION_BOTANY();
        }
        if (r.equalsIgnoreCase("Subsection")) {
            rank=Rank.SUBSECTION_BOTANY();
        }
        if (r.equalsIgnoreCase("Series")) {
            rank=Rank.SERIES();
        }
        if (r.equalsIgnoreCase("Subseries")) {
            rank=Rank.SUBSERIES();
        }
        if (r.equalsIgnoreCase("Species")) {
            rank=Rank.SPECIES();
        }
        if (r.equalsIgnoreCase("Subspecies")) {
            rank=Rank.SUBSPECIES();
        }
        if (r.equalsIgnoreCase("Variety")) {
            rank=Rank.VARIETY();
        }
        if (r.equalsIgnoreCase("Subvariety")) {
            rank=Rank.SUBVARIETY();
        }
        if (r.equalsIgnoreCase("Form")) {
            rank=Rank.FORM();
        }
        if (r.equalsIgnoreCase("Subform")) {
            rank=Rank.SUBFORM();
        }

        return rank;
    }


    /**
     * @param ato: atomised taxon name data
     * @return rank present in the xmldata fields
     */
    protected Rank getRank(Map<String, String> ato) {
        Rank rank=Rank.UNKNOWN_RANK();

        if (ato == null) {
            return rank;
        }
        if (ato.containsKey("dwc:family")){
            rank=Rank.FAMILY();
        }
        if (ato.containsKey("dwc:tribe") || ato.containsKey("dwcranks:tribe")){
            rank=Rank.TRIBE();
        }
        if (ato.containsKey("dwc:genus")) {
            rank= Rank.GENUS();
        }
        if (ato.containsKey("dwc:subgenus")) {
            rank= Rank.SUBGENUS();
        }
        if (ato.containsKey("dwc:specificepithet") || ato.containsKey("dwc:species")) {
            rank= Rank.SPECIES();
        }
        if (ato.containsKey("dwc:infraspecificepithet")) {
            rank= Rank.INFRASPECIES();
        }
        //popUp(rank.getTitleCache());
        return rank;
    }

    /**
     * Format a XML node for a clean (screen) output with tags
     * @param Node : the node to format
     * @return String : the XML section formated for a screen output
     * */

    protected String formatNode(Node node) throws TransformerFactoryConfigurationError, TransformerException{
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(node);
        transformer.transform(source, result);
        String xmlString = result.getWriter().toString();
        return xmlString;
    }


}


