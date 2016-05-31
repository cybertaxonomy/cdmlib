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
import javax.swing.UIManager;
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
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
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
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;


/**
 * @author pkelbert
 * @date 2 avr. 2013
 *
 */
public class TaxonXExtractor {

    protected TaxonXImport importer;
    protected TaxonXImportState state2;
    private final Map<String,String> namesAsked = new HashMap<String, String>();
    private final Map<String,Rank>ranksAsked = new HashMap<String, Rank>();

    Logger logger = Logger.getLogger(TaxonXExtractor.class);

    public class ReferenceBuilder{
        private int nbRef=0;
        private boolean foundBibref=false;
        private final TaxonXAddSources sourceHandler;

        /**
         * @param sourceHandler
         */
        public ReferenceBuilder(TaxonXAddSources sourceHandler) {
            this.sourceHandler=sourceHandler;
        }

        /**
         * @return the foundBibref
         */
        public boolean isFoundBibref() {
            return foundBibref;
        }

        /**
         * @param foundBibref the foundBibref to set
         */
        public void setFoundBibref(boolean foundBibref) {
            this.foundBibref = foundBibref;
        }


        /**
         * @param ref
         * @param refMods
         */
        public void builReference(String mref, String treatmentMainName, NomenclaturalCode nomenclaturalCode,
                Taxon acceptedTaxon, Reference refMods) {
            // System.out.println("builReference "+mref);
            this.setFoundBibref(true);

            String ref= mref;
            if ( (ref.endsWith(";") ||ref.endsWith(",")  ) && ((ref.length())>1)) {
                ref=ref.substring(0, ref.length()-1)+".";
            }
            if (ref.startsWith(treatmentMainName) && !ref.endsWith(treatmentMainName)) {
                ref=ref.replace(treatmentMainName, "");
                ref=ref.trim();
                while (ref.startsWith(".") || ref.startsWith(",")) {
                    ref=ref.replace(".","").replace(",","").trim();
                }
            }

            //                        logger.info("Current reference :"+nbRef+", "+ref+", "+treatmentMainName+"--"+ref.indexOf(treatmentMainName));
            Reference reference = ReferenceFactory.newGeneric();
            reference.setTitleCache(ref, true);

            //only add the first one if there is no nomenclatural reference yet
            if (nbRef==0){
                if(acceptedTaxon.getName().getNomenclaturalReference()==null){
                    acceptedTaxon.getName().setNomenclaturalReference(reference);
                    sourceHandler.addSource(refMods, acceptedTaxon);
                }
            }
            //add all other references as Feature.Citation
            TaxonDescription taxonDescription =importer.getTaxonDescription(acceptedTaxon, false, true);
            acceptedTaxon.addDescription(taxonDescription);
            sourceHandler.addSource(refMods, acceptedTaxon);

            TextData textData = TextData.NewInstance(Feature.CITATION());
            Language language = Language.DEFAULT();
            textData.putText(language, ref);
            sourceHandler.addSource(reference, textData,acceptedTaxon.getName(),refMods);
            taxonDescription.addElement(textData);

            sourceHandler.addSource(refMods, taxonDescription);

            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
            //                        logger.warn("BWAAHHHH: "+nameToBeFilled.getParsingProblems()+", "+ref);
            nbRef++;

        }

    }

    public class MySpecimenOrObservation{
        String descr="";
        DerivedUnit derivedUnitBase=null;

        public String getDescr() {
            return descr;
        }
        public void setDescr(String descr) {
            this.descr = descr;
        }
        public DerivedUnit getDerivedUnitBase() {
            return derivedUnitBase;
        }
        public void setDerivedUnitBase(DerivedUnit derivedUnitBase) {
            this.derivedUnitBase = derivedUnitBase;
        }




    }

    /**
     * @param item
     * @return
     */
    @SuppressWarnings({ "unused", "rawtypes" })
    protected MySpecimenOrObservation extractSpecimenOrObservation(Node specimenObservationNode, DerivedUnit derivedUnitBase,
            SpecimenOrObservationType defaultAssociation, TaxonNameBase<?,?> typifiableName) {
        String country=null;
        String locality=null;
        String stateprov=null;
        String collector=null;
        String fieldNumber=null;
        Double latitude=null,longitude=null;
        TimePeriod tp =null;
        String day,month,year="";
        String descr="not available";
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
                    }else if(eventContent.item(j).getNodeName().equalsIgnoreCase("#text") && StringUtils.isBlank(eventContent.item(j).getTextContent())){
                        //do nothing
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
                descr=xmldata.item(n).getTextContent().replaceAll(";","").trim();
                if (descr.length()>1 && containsDistinctLetters(descr)) {
                    specimenOrObservation.setDescr(descr);
                    asso=true;
                }
            }
            if(xmldata.item(n).getNodeName().equalsIgnoreCase("tax:p")){
                descr=xmldata.item(n).getTextContent().replaceAll(";","").trim();
                if (descr.length()>1 && containsDistinctLetters(descr)) {
                    specimenOrObservation.setDescr(descr);
                    asso=true;
                }
            }
        }
        //        if(asso && descr.length()>1){

        //            logger.info("DESCR: "+descr);
        if (!type.isEmpty()) {
            if (!containsDistinctLetters(type)) {
                type="no description text";
            }
            derivedUnitFacade = getFacade(type.replaceAll(";",""), defaultAssociation);
            SpecimenTypeDesignation designation = SpecimenTypeDesignation.NewInstance();

            if (typifiableName != null){
            	typifiableName.addTypeDesignation(designation, true);
            }else{
            	logger.warn("No typifiable name available");
            }
            SpecimenTypeDesignationStatus stds= getSpecimenTypeDesignationStatusByKey(type);
            if (stds !=null) {
                stds = (SpecimenTypeDesignationStatus) importer.getTermService().find(stds.getUuid());
            }

            designation.setTypeStatus(stds);
            derivedUnitFacade.innerDerivedUnit().addSpecimenTypeDesignation(designation);

            derivedUnitBase = derivedUnitFacade.innerDerivedUnit();
            // System.out.println("derivedUnitBase: "+derivedUnitBase);
            //                designation.setTypeSpecimen(derivedUnitBase);
            //                TaxonNameBase<?,?> name = taxon.getName();
            //                name.addTypeDesignation(designation, true);
        } else {
            if (!containsDistinctLetters(descr.replaceAll(";",""))) {
                descr="no description text";
            }

            derivedUnitFacade = getFacade(descr.replaceAll(";",""), defaultAssociation);
            derivedUnitBase = derivedUnitFacade.innerDerivedUnit();
            // System.out.println("derivedUnitBase2: "+derivedUnitBase);
        }

        unitsGatheringEvent = new UnitsGatheringEvent(importer.getTermService(), locality,collector,longitude, latitude,
                state2.getConfig(),importer.getAgentService());

        if(tp!=null) {
            unitsGatheringEvent.setGatheringDate(tp);
        }

        // country
        unitsGatheringArea = new UnitsGatheringArea();
        unitsGatheringArea.setParams(null, country, state2.getConfig(), importer.getTermService(), importer.getOccurrenceService());
        //TODO other areas
        if (StringUtils.isNotBlank(stateprov)){
        	Map<String, String> namedAreas = new HashMap<String, String>();
        	namedAreas.put(stateprov, null);
            unitsGatheringArea.setAreaNames(namedAreas, state2.getConfig(), importer.getTermService(), importer.getVocabularyService());
        }

        areaCountry =  unitsGatheringArea.getCountry();

        //                         // other areas
        //                         unitsGatheringArea = new UnitsGatheringArea(namedAreaList,dataHolder.getTermService());
        //                         ArrayList<DefinedTermBase> nas = unitsGatheringArea.getAreas();
        //                         for (DefinedTermBase namedArea : nas) {
        //                             unitsGatheringEvent.addArea(namedArea);
        //                         }

        // copy gathering event to facade
        GatheringEvent gatheringEvent = unitsGatheringEvent.getGatheringEvent();
        derivedUnitFacade.setGatheringEvent(gatheringEvent);
        derivedUnitFacade.setLocality(gatheringEvent.getLocality());
        derivedUnitFacade.setExactLocation(gatheringEvent.getExactLocation());
        derivedUnitFacade.setCollector(gatheringEvent.getCollector());
        derivedUnitFacade.setCountry((NamedArea)areaCountry);

        for(DefinedTermBase<?> area:unitsGatheringArea.getAreas()){
            derivedUnitFacade.addCollectingArea((NamedArea) area);
        }
        //                         derivedUnitFacade.addCollectingAreas(unitsGatheringArea.getAreas());

        // add fieldNumber
        if (fieldNumber != null) {
            derivedUnitFacade.setFieldNumber(fieldNumber);
        }
        specimenOrObservation.setDerivedUnitBase(derivedUnitBase);
        //        }
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
    protected DerivedUnitFacade getFacade(String recordBasis, SpecimenOrObservationType defaultAssoc) {
        // System.out.println("getFacade() for "+recordBasis+", defaultassociation: "+defaultAssoc);
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
                type = defaultAssoc;
            }
            // TODO fossils?
        } else {
            logger.info("The basis of record is null");
            type = defaultAssoc;
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
    protected Reference getReferenceWithType(int reftype) {
        Reference ref = null;
        switch (reftype) {
        case 1:
            ref = ReferenceFactory.newGeneric();
            break;
        case 2:
            IBook tmp= ReferenceFactory.newBook();
            ref = (Reference)tmp;
            break;
        case 3:
            ref = ReferenceFactory.newArticle();
            break;
        case 4:
            IBookSection tmp2 = ReferenceFactory.newBookSection();
            ref = (Reference)tmp2;
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
        JTextArea textArea = new JTextArea("Complete the reference or the name '"+name+"'.\nThe current problem is "+StringUtils.join(problems,"--"));
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 70 ) );

        //        JFrame frame = new JFrame("I have a question");
        //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String s = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                "Get full reference or name",
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
    protected String askWhichScientificName(String fullname,String atomised,String classificationName, Node fullParagraph) throws TransformerFactoryConfigurationError, TransformerException {
        //        logger.info("getScientificName for "+ fullname);
        //        JFrame frame = new JFrame("I have a question");
        //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String k = fullname+"_"+atomised;

        String defaultN = "";
        if (atomised.length()>fullname.length()) {
            defaultN=atomised;
        } else {
            defaultN=fullname;
        }

        if (namesAsked.containsKey(k)){
            return namesAsked.get(k);
        }
        else{
            //activate it for ants because a lot of markup is incomplete
            if (classificationName.indexOf("Ants")>-1) {
                return defaultN;
            }

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
                    defaultN);
            namesAsked.put(k, s);
            return s;
        }
    }


    protected int askAddParent(String s){
        //        boolean hack=true;
        //        if (hack) {
        //            return 1;
        //        }
        JTextArea textArea = new JTextArea("If you want to add a parent taxa for "+s+", click \"Yes\"." +
                " If it is a root for this classification, click \"No\" or \"Cancel\".");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 600, 70 ) );

        Object[] options = { UIManager.getString("OptionPane.yesButtonText"),
                UIManager.getString("OptionPane.noButtonText")};


        int addTaxon = JOptionPane.showOptionDialog(null,
                scrollPane,
                "",
                JOptionPane.YES_NO_OPTION,
                0,
                null,
                options,
                options[1]);
        return addTaxon;
    }

    protected String askSetParent(String s){
        JTextArea textArea =  new JTextArea("What is the first taxon parent for "+s+"?\n"+
                "The rank will be asked later. ");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 200 ) );

        String s2 = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                "",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                s);
        return s2;
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
     * @param taxonnamebase2
     * @param bestMatchingTaxon
     * @param refMods
     * @param similarityAuthor
     * @return
     */
    protected boolean askIfReuseBestMatchingTaxon(NonViralName<?> taxonnamebase2, Taxon bestMatchingTaxon, Reference refMods, double similarityScore, double similarityAuthor) {
        Object[] options = { UIManager.getString("OptionPane.yesButtonText"),
                UIManager.getString("OptionPane.noButtonText")};

        if (similarityScore<0.66 &&  similarityAuthor<0.5) {
            return false;
            //            System.out.println("should say NO");
        }

        boolean sameSource=false;
        boolean noRef=false;

        String sec = refMods.getTitleCache();
        String secBest = "";
        try{
            secBest=bestMatchingTaxon.getSec().getTitleCache();
        }
        catch(NullPointerException e){
            logger.warn("no sec - ignore");
        }

        if (secBest.isEmpty()) {
            noRef=true;
        }

        Object defaultOption=options[1];
        if(sec.equalsIgnoreCase(secBest)
                //                ||                taxonnamebase2.getTitleCache().split("sec.")[0].trim().equalsIgnoreCase(bestMatchingTaxon.getTitleCache().split("sec.")[0].trim())
                ) {
            //System.out.println(sec+" and "+secBest);
            sameSource=true;
            //-1 <=> no author
            if (similarityScore>0.65 && (similarityAuthor==-1 || similarityAuthor>0.8)) {
                defaultOption=options[0];
            } else {
                defaultOption=options[1];
            }
        } else {
            if (similarityScore>0.65 && similarityAuthor>0.8) {
                if(similarityScore==1 ) {
                    return true;
                }
                defaultOption=options[0];
            } else {
                defaultOption=options[1];
            }
        }

        String sourcesStr="";

        Set<IdentifiableSource> sources = bestMatchingTaxon.getSources();
        for (IdentifiableSource src:sources){
            try{
                String srcSec=src.getCitation().getTitleCache();
                if(!srcSec.isEmpty()){
                    sourcesStr+="\n "+srcSec;
                    if (srcSec.equalsIgnoreCase(sec)){
                        sameSource=true;
                        if (similarityScore>0.65 && similarityAuthor>0.8) {
                            defaultOption=options[0];
                        } else {
                            defaultOption=options[1];
                        }
                    }
                }
            }catch(Exception e){
                logger.warn("the source reference is maybe null, just ignore it.");
            }
        }

        if (sameSource && similarityScore>0.9999 && (similarityAuthor==-1 || similarityAuthor>0.8)) {
            return true;
        }
        if(similarityScore<0.66) {
            defaultOption=options[1];
        }

        //        //only activate it if you know the data you are importing (ok for Chenopodium)
        if(defaultOption==options[1]) {
            return false;
        }

        JTextArea textArea =null;
        if (!sourcesStr.isEmpty()) {
            textArea = new JTextArea("Does "+taxonnamebase2.toString()+" correspond to "
                    + bestMatchingTaxon.toString()+" ?\n Click \"Yes\". if it does, click \"No\" if it does not."
                    + "\n The current sources are:"+ sourcesStr);
        } else {
            textArea = new JTextArea("Does "+taxonnamebase2.toString()+" correspond to "
                    + bestMatchingTaxon.toString()+" ?\n Click \"Yes\". if it does, click \"No\" if it does not.");
        }
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 600, 70 ) );

        int addTaxon = JOptionPane.showOptionDialog(null,
                scrollPane,
                refMods.toString(),
                JOptionPane.YES_NO_OPTION,
                0,
                null,
                options,
                defaultOption);
        if(addTaxon==1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @param fullLineRefName
     * @return
     */
    protected int askIfNameContained(String fullLineRefName) {

        JTextArea textArea = new JTextArea("Is a scientific name contained in this sentence ? Type 0 if contains a name, 1 if it's only a reference. Press 2 if it's to be ignored \n"+fullLineRefName);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 600, 400 ) );

        String s = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                "",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "0");
        return Integer.valueOf(s);
    }


    /**
     * @param name
     * @return
     */
    protected Rank askForRank(String fullname,Rank rank, NomenclaturalCode nomenclaturalCode) {
        //        logger.info("askForRank for "+ fullname+ ", "+rank);
        //        JFrame frame = new JFrame("I have a question");
        //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (ranksAsked.containsKey(fullname)){
            return ranksAsked.get(fullname);
        }
        else{
            boolean np=false;
            int npi=0;
            Rank cR = null;

            while (!np && npi<2)
            {


                JTextArea textArea = new JTextArea("What is the correct rank for "+fullname+"?");
                JScrollPane scrollPane = new JScrollPane(textArea);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                scrollPane.setPreferredSize( new Dimension( 600, 50 ) );

                List<Rank> rankList = new ArrayList<Rank>();
                rankList = importer.getTermService().list(Rank.class, null, null, null, null);

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


                try {
                    npi++;
                    cR = Rank.getRankByEnglishName(s,nomenclaturalCode,true);
                    np=true;
                } catch (UnknownCdmTypeException e) {
                    logger.warn("Unknown rank ?!"+s);
                    logger.warn(e);
                }
            }
            ranksAsked.put(fullname,cR);
            return cR;

        }
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
        String fp = "";
        try {
            fp = formatNode(fullParagraph);
        } catch (TransformerFactoryConfigurationError e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (TransformerException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        JTextArea textArea = new JTextArea("What category is it for this paragraph \n"+fp);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 600, 400 ) );

        String[] possiblities = {"synonyms","material examined","distribution","image caption","Other","vernacular name","type status","new category"};


        String s = (String)JOptionPane.showInputDialog(
                null,
                scrollPane,
                "",
                JOptionPane.PLAIN_MESSAGE,
                null,
                possiblities,
                "Other");

        if (s.equalsIgnoreCase("new category")) {
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
        // System.out.println("ASK PARENT "+classification);
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

        Rank rank = Rank.UNKNOWN_RANK();
        if (r.equalsIgnoreCase("Superfamily")) {
            rank=Rank.SUPERFAMILY();
        }
        else if (r.equalsIgnoreCase("Family")) {
            rank=Rank.FAMILY();
        }
        else if (r.equalsIgnoreCase("Subfamily")) {
            rank=Rank.SUBFAMILY();
        }
        else if (r.equalsIgnoreCase("Tribe")) {
            rank=Rank.TRIBE();
        }
        else if (r.equalsIgnoreCase("Subtribe")) {
            rank=Rank.SUBTRIBE();
        }
        else if (r.equalsIgnoreCase("Genus")) {
            rank=Rank.GENUS();
        }
        else if (r.equalsIgnoreCase("Subgenus")) {
            rank=Rank.SUBGENUS();
        }
        else if (r.equalsIgnoreCase("Section")) {
            rank=Rank.SECTION_BOTANY();
        }
        else if (r.equalsIgnoreCase("Subsection")) {
            rank=Rank.SUBSECTION_BOTANY();
        }
        else if (r.equalsIgnoreCase("Series")) {
            rank=Rank.SERIES();
        }
        else if (r.equalsIgnoreCase("Subseries")) {
            rank=Rank.SUBSERIES();
        }
        else if (r.equalsIgnoreCase("Species")) {
            rank=Rank.SPECIES();
        }
        else if (r.equalsIgnoreCase("Subspecies")) {
            rank=Rank.SUBSPECIES();
        }
        else if (r.equalsIgnoreCase("Variety") || r.equalsIgnoreCase("varietyEpithet")) {
            rank=Rank.VARIETY();
        }
        else if (r.equalsIgnoreCase("Subvariety")) {
            rank=Rank.SUBVARIETY();
        }
        else if (r.equalsIgnoreCase("Form")) {
            rank=Rank.FORM();
        }
        else if (r.equalsIgnoreCase("Subform")) {
            rank=Rank.SUBFORM();
        }else if (r.equalsIgnoreCase("higher")) {
//            rank=Rank.SUPRAGENERICTAXON();
        	logger.warn("handling of 'higher' rank still unclear");
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
        if (ato.containsKey("dwcranks:varietyepithet")) {
            rank=Rank.VARIETY();
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

    protected boolean containsDistinctLetters(String word){
        Set<Character> dl = new HashSet<Character>();
        for (char a: word.toCharArray()) {
            dl.add(a);
        }
        if(dl.size()>1 && word.indexOf("no description text")==-1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Tries to match the status string against any new name status
     * and returns the status if it matches. Returns <code>null</code> otherwise.
     * @param status
     * @return
     */
    protected String newNameStatus(String status){
    	String pattern = "(" + "((sp|spec|gen|comb|)\\.\\s*nov.)" +
    				"|(new\\s*(species|combination))" +
    				"|(n\\.\\s*sp\\.)" +
    				"|(sp\\.\\s*n\\.)" +
    				")";
    	if (status.trim().matches(pattern)){
    		//FIXME
    		return null;
//    		return status;
    	}else{
    		return null;
    	}
    }


    /** Creates an cdm-NomenclaturalCode by the tcs NomenclaturalCode
     */
    protected NomenclaturalStatusType nomStatusString2NomStatus (String nomStatus) throws UnknownCdmTypeException{

        if (nomStatus == null){ return null;
        }else if ("Valid".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.VALID();

        }else if ("Alternative".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ALTERNATIVE();
        }else if ("nom. altern.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ALTERNATIVE();

        }else if ("Ambiguous".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.AMBIGUOUS();

        }else if ("Doubtful".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.DOUBTFUL();

        }else if ("Confusum".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONFUSUM();

        }else if ("Illegitimate".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ILLEGITIMATE();
        }else if ("nom. illeg.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ILLEGITIMATE();

        }else if ("Superfluous".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.SUPERFLUOUS();
        }else if ("nom. superfl.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.SUPERFLUOUS();

        }else if ("Rejected".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.REJECTED();
        }else if ("nom. rej.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.REJECTED();

        }else if ("Utique Rejected".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.UTIQUE_REJECTED();

        }else if ("Conserved Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONSERVED_PROP();

        }else if ("Orthography Conserved Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP();

        }else if ("Legitimate".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.LEGITIMATE();

        }else if ("Novum".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NOVUM();
        }else if ("nom. nov.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NOVUM();

        }else if ("Utique Rejected Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.UTIQUE_REJECTED_PROP();

        }else if ("Orthography Conserved".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED();

        }else if ("Rejected Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.REJECTED_PROP();

        }else if ("Conserved".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONSERVED();
        }else if ("nom. cons.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONSERVED();

        }else if ("Sanctioned".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.SANCTIONED();

        }else if ("Invalid".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.INVALID();
        }else if ("nom. inval.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.INVALID();

        }else if ("Nudum".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NUDUM();
        }else if ("nom. nud.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NUDUM();

        }else if ("Combination Invalid".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.COMBINATION_INVALID();

        }else if ("Provisional".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.PROVISIONAL();
        }else if ("nom. provis.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.PROVISIONAL();
        }
        else {
            throw new UnknownCdmTypeException("Unknown Nomenclatural status type " + nomStatus);
        }
    }


    //TypeDesignation
    protected  SpecimenTypeDesignationStatus typeStatusId2TypeStatus (int typeStatusId)  throws UnknownCdmTypeException{
        switch (typeStatusId){
        case 0: return null;
        case 1: return SpecimenTypeDesignationStatus.HOLOTYPE();
        case 2: return SpecimenTypeDesignationStatus.LECTOTYPE();
        case 3: return SpecimenTypeDesignationStatus.NEOTYPE();
        case 4: return SpecimenTypeDesignationStatus.EPITYPE();
        case 5: return SpecimenTypeDesignationStatus.ISOLECTOTYPE();
        case 6: return SpecimenTypeDesignationStatus.ISONEOTYPE();
        case 7: return SpecimenTypeDesignationStatus.ISOTYPE();
        case 8: return SpecimenTypeDesignationStatus.PARANEOTYPE();
        case 9: return SpecimenTypeDesignationStatus.PARATYPE();
        case 10: return SpecimenTypeDesignationStatus.SECOND_STEP_LECTOTYPE();
        case 11: return SpecimenTypeDesignationStatus.SECOND_STEP_NEOTYPE();
        case 12: return SpecimenTypeDesignationStatus.SYNTYPE();
        case 21: return SpecimenTypeDesignationStatus.ICONOTYPE();
        case 22: return SpecimenTypeDesignationStatus.PHOTOTYPE();
        default: {
            throw new UnknownCdmTypeException("Unknown TypeDesignationStatus (id=" + Integer.valueOf(typeStatusId).toString() + ")");
        }
        }
    }


}


