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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
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
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;

/**
 * @author pkelbert
 * @date 2 avr. 2013
 *
 */
public class TaxonXTreatmentExtractor extends TaxonXExtractor{

    private static final Logger logger = Logger.getLogger(TaxonXTreatmentExtractor.class);
    private final NomenclaturalCode nomenclaturalCode;
    private Classification classification;

    private  String treatmentMainName,originalTreatmentName;
    private final TaxonXImport importer;
    private final TaxonXImportState configState;

    private final Pattern keypattern = Pattern.compile("^(\\d+.*|-\\d+.*)");
    private final Pattern keypatternend = Pattern.compile("^.+?\\d$");

    /**
     * @param nomenclaturalCode
     * @param nameService
     * @param taxonService
     * @param referenceService
     * @param descriptionService
     * @param importer
     * @param configState
     * @param state
     */
    public TaxonXTreatmentExtractor(NomenclaturalCode nomenclaturalCode, Classification classification, TaxonXImport importer, TaxonXImportState configState) {
        this.nomenclaturalCode=nomenclaturalCode;
        this.classification = classification;
        this.importer=importer;
        this.configState=configState;
        prepareCollectors(configState, importer.getAgentService());
    }

    /**
     * @param item
     * @param tosave
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void extractTreatment(Node treatmentnode, List<Object> tosave) {
        logger.info("extractTreatment");
        List<TaxonNameBase> nametosave = new ArrayList<TaxonNameBase>();
        NodeList children = treatmentnode.getChildNodes();
        Taxon acceptedTaxon =null;
        Taxon defaultTaxon =null;

        boolean refgroup=false;
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group")) {
                refgroup=true;
            }
        }
        for (int i=0;i<children.getLength();i++){
            //            logger.info("i: "+i+", " +children.item(i).getNodeName());
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:nomenclature")){
                NodeList nomenclature = children.item(i).getChildNodes();
                boolean containsName=false;
                for(int k=0;k<nomenclature.getLength();k++){
                    if(nomenclature.item(k).getNodeName().equalsIgnoreCase("tax:name")){
                        containsName=true;
                        break;
                    }
                }
                if (containsName){
                    reloadClassification();
                    acceptedTaxon = extractNomenclature(children.item(i),nametosave);
                }
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group")){
                reloadClassification();
                extractReferences(children.item(i),nametosave,acceptedTaxon);

            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("multiple")){
                extractSynonyms(children.item(i),nametosave, acceptedTaxon);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("description")){
                extractDescription(children.item(i),acceptedTaxon,defaultTaxon);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("distribution")){
                extractDistribution(children.item(i),acceptedTaxon);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("materials_examined")){
                extractMaterials(children.item(i),acceptedTaxon);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("biology_ecology")){
                extractBioEco(children.item(i),acceptedTaxon);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("diagnosis")){
                extractDiagnosis(children.item(i),acceptedTaxon);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("key")){
                extractKey(children.item(i),acceptedTaxon, nametosave);
            }


        }
        logger.info("saveUpdateNames");
        importer.getNameService().saveOrUpdate(nametosave);
        logger.info("saveUpdateNames-ok");


    }

    /**
     * @param item
     * @param acceptedTaxon
     */
    private void extractKey(Node keys, Taxon acceptedTaxon,List<TaxonNameBase> nametosave) {
        NodeList children = keys.getChildNodes();
        String key="";
        PolytomousKey poly =  PolytomousKey.NewInstance();
        poly.addTaxonomicScope(acceptedTaxon);
        poly.setTitleCache("bloup");
        //        poly.addCoveredTaxon(acceptedTaxon);
        PolytomousKeyNode root = poly.getRoot();
        PolytomousKeyNode previous = null,tmpKey=null;
        Taxon taxonKey=null;
        List<PolytomousKeyNode> polyNodes = new ArrayList<PolytomousKeyNode>();

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList paragraph = children.item(i).getChildNodes();
                key="";
                taxonKey=null;
                for (int j=0;j<paragraph.getLength();j++){
                    if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text")){
                        if (! paragraph.item(j).getTextContent().trim().isEmpty()){
                            key+=paragraph.item(j).getTextContent().trim();
                            logger.info("KEY : "+j+"--"+key);
                        }
                    }
                    if(paragraph.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        taxonKey=getTaxonFromXML(paragraph.item(j),nametosave);
                    }
                }

                    if (keypattern.matcher(key).matches()){
                        tmpKey = PolytomousKeyNode.NewInstance(key);
                        if (taxonKey!=null) {
                            tmpKey.setTaxon(taxonKey);
                        }
                        polyNodes.add(tmpKey);
                        if (previous == null) {
                            root.addChild(tmpKey);
                        } else {
                            previous.addChild(tmpKey);
                        }
                    }else{
                        if (!key.isEmpty()){
                            tmpKey=PolytomousKeyNode.NewInstance(key);
                            if (taxonKey!=null) {
                                tmpKey.setTaxon(taxonKey);
                            }
                            polyNodes.add(tmpKey);
                            if (keypatternend.matcher(key).matches()) {
                                root.addChild(tmpKey);
                                previous=tmpKey;
                            } else{
                                previous.addChild(tmpKey);
                            }

                        }
                    }


            }
        }
        importer.getPolytomousKeyNodeService().saveOrUpdate(polyNodes);
        importer.getPolytomousKeyService().saveOrUpdate(poly);

    }

    /**
     * @param item
     * @return
     */
    private Taxon getTaxonFromXML(Node taxons, List<TaxonNameBase> nametosave) {
        TaxonNameBase nameToBeFilled = null;
        String name;

        String[] enames = extractScientificName(taxons);
        if (enames[1].isEmpty()) {
            name=enames[0];
        } else {
            name=enames[1];
        }
        String original=enames[0];
        Rank rank = null;
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();

        nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);
        if (nameToBeFilled.hasProblem() &&
                !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
            //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
            Map<String,String> ato = namesMap.get(original);

            rank = getRank(ato);
            nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);
            logger.info("RANK: "+rank);
            int retry=0;
            while (nameToBeFilled.hasProblem() && retry <3 && !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank))){
                String fullname =  getFullReference(name,nameToBeFilled.getParsingProblems());
                if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                    nameToBeFilled = BotanicalName.NewInstance(null);
                }
                if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                    nameToBeFilled = ZoologicalName.NewInstance(null);
                }
                parser.parseReferencedName(nameToBeFilled, fullname, rank, false);
                retry++;
            }
            if (retry == 2 && !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank))){
                nameToBeFilled.setFullTitleCache(name, true);
                logger.info("FULL TITLE CACHE "+name);
            }
        }
        nametosave.add(nameToBeFilled);
        Taxon t = importer.getTaxonService().findBestMatchingTaxon(nameToBeFilled.getTitleCache());
        if (t ==null){
            logger.info("Did not find anything similar to "+nameToBeFilled.getTitleCache());
            t= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
            classification.addChildTaxon(t,(Reference<?>)nameToBeFilled.getNomenclaturalReference(),"",null);
            importer.getClassificationService().saveOrUpdate(classification);
        }

        return t;
    }

    /**
     * @param item
     * @param acceptedTaxon
     */
    private void extractDiagnosis(Node item, Taxon acceptedTaxon) {
        // TODO Auto-generated method stub

    }

    /**
     * @param item
     * @param acceptedTaxon
     */
    private void extractBioEco(Node item, Taxon acceptedTaxon) {
        // TODO Auto-generated method stub

    }

    /**
     *
     */
    private void reloadClassification() {
        Classification cl = importer.getClassificationService().find(classification.getUuid());
        if (cl != null){
            classification=cl;
        }else{
            importer.getClassificationService().saveOrUpdate(classification);
            classification = importer.getClassificationService().find(classification.getUuid());
        }

    }

    /**
     * @param taxonNameBase
     * @return
     */
    private Taxon getTaxon(TaxonNameBase taxonNameBase) {
        return new Taxon(taxonNameBase,null );
    }

    /**
     * @param item
     */
    private void extractDistribution(Node distribution, Taxon acceptedTaxon) {
        NodeList children = distribution.getChildNodes();

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                boolean first=true;
                NodeList paragraph = children.item(i).getChildNodes();
                for (int j=0;j<paragraph.getLength();j++){
                    if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text") && first){
                        logger.info(paragraph.item(j).getTextContent());
                        first =false;
                    }
                    else if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text") && !first){
                    }
                }
            }
            String descr = children.item(i).getTextContent().trim();
            if(! descr.isEmpty()){
                TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
                Feature currentFeature = Feature.DISTRIBUTION();
                TextData descBase = TextData.NewInstance();

                descBase.setFeature(currentFeature);
                descBase.putText(Language.UNKNOWN_LANGUAGE(), descr);
                td.addElement(descBase);
                acceptedTaxon.addDescription(td);
                importer.getTaxonService().saveOrUpdate(acceptedTaxon);
            }
        }

    }

    private DerivedUnitFacade getFacade(String recordBasis) {
        logger.info("getFacade()");
        DerivedUnitType type = null;

        // create specimen
        if (recordBasis != null) {
            if (recordBasis.toLowerCase().startsWith("s") || recordBasis.toLowerCase().contains("specimen")) {// specimen
                type = DerivedUnitType.Specimen;
            }
            if (recordBasis.toLowerCase().startsWith("o")) {
                type = DerivedUnitType.Observation;
            }
            if (recordBasis.toLowerCase().contains("fossil")) {
                type = DerivedUnitType.Fossil;
            }

            if (recordBasis.toLowerCase().startsWith("l")) {
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

    /*
     * <tax:collection_event><tax:xmldata><dwc:Country>Argentina</dwc:Country>
     * <dwc:StateProvince>Chubut</dwc:StateProvince><dwc:Locality>Depto</dwc:Locality><dwc:Collector>Soriano</dwc:Collector>
     * </tax:xmldata>Exsiccata; Prov. Chubut : Depto Cushamen : Leleque , Soriano2350 ( BAA )</tax:collection_event>
     * */



    /**
     * @param item
     */
    private void extractMaterials(Node materials, Taxon acceptedTaxon) {
        logger.info("EXTRACTMATERIALS");
        NodeList children = materials.getChildNodes();
        NodeList events = null;
        NodeList xmldata=null;
        NodeList eventContent =null;
        UnitsGatheringEvent unitsGatheringEvent;
        UnitsGatheringArea unitsGatheringArea;

        // create facade
        DerivedUnitFacade derivedUnitFacade = null;
        DerivedUnitBase derivedUnitBase=null;

        String country=null;
        String locality=null;
        String stateprov=null;
        String collector=null;
        String fieldNumber=null;
        Double latitude=null,longitude=null;
        String descr="";
        boolean asso=false;

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                events = children.item(i).getChildNodes();
                for(int k=0;k<events.getLength();k++){
                    if(events.item(k).getNodeName().equalsIgnoreCase("tax:collection_event")){
                        asso=false;
                        descr="";
                        xmldata=events.item(k).getChildNodes();
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
                                        System.out.println("COUNTRY: "+country);
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

                            DefinedTermBase areaCountry =  unitsGatheringArea.getCountry();

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

                            importer.getOccurrenceService().saveOrUpdate(derivedUnitBase);

                            TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);
                            acceptedTaxon.addDescription(taxonDescription);


                            IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();

                            Feature feature = makeFeature(derivedUnitBase);
                            if(!StringUtils.isEmpty(descr)) {
                                derivedUnitBase.setTitleCache(descr, true);
                            }
                            indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
                            indAssociation.setFeature(feature);

                            taxonDescription.addElement(indAssociation);
                            taxonDescription.setTaxon(acceptedTaxon);

                            importer.getDescriptionService().saveOrUpdate(taxonDescription);
                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);


                        }
                    }
                }
            }
        }
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
    /**
     * @param item
     * @param acceptedTaxon
     * @param defaultTaxon
     */
    private void extractDescription(Node description, Taxon acceptedTaxon, Taxon defaultTaxon) {
        logger.info("acceptedTaxon: "+acceptedTaxon);
        logger.info("defaultTaxon: "+defaultTaxon);
        NodeList children = description.getChildNodes();
        for (int i=0;i<children.getLength();i++){
            String descr = children.item(i).getTextContent().trim();
            if(! descr.isEmpty() && acceptedTaxon!=null){
                TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
                Feature currentFeature = Feature.DESCRIPTION();
                TextData descBase = TextData.NewInstance();

                descBase.setFeature(currentFeature);
                descBase.putText(Language.UNKNOWN_LANGUAGE(), descr);
                td.addElement(descBase);
                acceptedTaxon.addDescription(td);
                importer.getTaxonService().saveOrUpdate(acceptedTaxon);
            }

            if(! descr.isEmpty() && acceptedTaxon == null && defaultTaxon != null){
                try{
                    Taxon tmp =(Taxon) importer.getTaxonService().find(defaultTaxon.getUuid());
                    if (tmp!=null) {
                        defaultTaxon=tmp;
                    }else{
                        logger.warn("TAXON???" +defaultTaxon.getUuid()+","+defaultTaxon.getTitleCache());
                        importer.getTaxonService().saveOrUpdate(defaultTaxon);
                        defaultTaxon =(Taxon) importer.getTaxonService().find(defaultTaxon.getUuid());
                    }
                }catch(Exception e){
                    logger.warn("TAXON EXISTS"+defaultTaxon);
                    logger.info("TAXON EXISTS"+defaultTaxon);
                }

                TaxonDescription td =importer.getTaxonDescription(defaultTaxon, false, true);
                defaultTaxon.addDescription(td);
                Feature currentFeature = Feature.DESCRIPTION();
                TextData descBase = TextData.NewInstance();

                descBase.setFeature(currentFeature);
                descBase.putText(Language.UNKNOWN_LANGUAGE(), descr);
                td.addElement(descBase);

                importer.getDescriptionService().saveOrUpdate(td);
                importer.getTaxonService().saveOrUpdate(defaultTaxon);
            }
        }

    }

    /**
     * @param item
     * @param nametosave
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void extractSynonyms(Node synonnyms, List<TaxonNameBase> nametosave,Taxon acceptedTaxon) {
        logger.info("EXTRACT Synonyms : "+acceptedTaxon);
        Taxon ttmp = (Taxon) importer.getTaxonService().find(acceptedTaxon.getUuid());
        if (ttmp != null) {
            acceptedTaxon = ttmp;
        }
        NodeList children = synonnyms.getChildNodes();
        TaxonNameBase nameToBeFilled = null;
        List<String> names = new ArrayList<String>();
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList tmp = children.item(i).getChildNodes();
                for (int j=0; j< tmp.getLength();j++){
                    if(tmp.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        String[] enames = extractScientificName(tmp.item(j));
                        if (enames[1].isEmpty()) {
                            names.add(enames[0]);
                        } else {
                            names.add(enames[1]);
                        }
                    }
                }
            }
        }
        for(String name:names){
            logger.info("HANDLE NAME "+name);
            String original = name;
            //            if (name.equalsIgnoreCase("Blitum L. (1753)")) {
            //                name="Blitum L., Sp. Pl.: 4. 1753";
            //            }
            //            if (name.equalsIgnoreCase("Roubieva Moq. (1834)")) {
            //                name = "Roubevia Moq. in Ann. Sci. Nat., Bot., ser. 2 1: 289. 1834";
            //            }
            //            if (name.equalsIgnoreCase("Teloxys Moq. (1834)")) {
            //                name = "Teloxys teloxys Moq. in Ann. Sci. Nat., Bot., ser. 2 1: 289. 1834";
            //            }
            //            if (name.equalsIgnoreCase("Ceratoides Gagnebin (1755)")){
            //                name ="Ceratoides Gagnebin in Acta Helvetica, Physico-Mathematico-Anatomico-Botanico-Medica 2: 59. 1755.";
            //            }
            Rank rank = null;
            INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
            nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);
            if (nameToBeFilled.hasProblem() &&
                    !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                Map<String,String> ato = namesMap.get(original);

                rank = getRank(ato);
                nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);
                logger.info("RANK: "+rank);
                int retry=0;
                while (nameToBeFilled.hasProblem() && retry <3 && !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank))){
                    String fullname =  getFullReference(name,nameToBeFilled.getParsingProblems());
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                        nameToBeFilled = BotanicalName.NewInstance(null);
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                        nameToBeFilled = ZoologicalName.NewInstance(null);
                    }
                    parser.parseReferencedName(nameToBeFilled, fullname, rank, false);
                    retry++;
                }
                if (retry == 2 && !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank))){
                    nameToBeFilled.setFullTitleCache(name, true);
                    logger.info("FULL TITLE CACHE "+name);
                }
            }
            nametosave.add(nameToBeFilled);
            Synonym synonym = Synonym.NewInstance(nameToBeFilled, null);

            acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        }

    }

    /**
     * @param name
     * @return
     */
    private String getFullReference(String name, List<ParserProblem> problems) {
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
    private String getScientificName(String fullname,String atomised) {
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
     * @param ato: atomised taxon name data
     * @return rank present in the xmldata fields
     */
    private Rank getRank(Map<String, String> ato) {
        Rank rank=null;

        if (ato.containsKey("dwc:family")){
            rank=Rank.FAMILY();
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

    private void popUp(String message){
        JOptionPane.showMessageDialog(null, message);
    }

    /**
     * @param item
     * @param tosave
     */
    @SuppressWarnings({ "unchecked", "null" })
    private Taxon extractReferences(Node refgroup, List<TaxonNameBase> tosave, Taxon acceptedTaxon) {
        NodeList children = refgroup.getChildNodes();
        NonViralName<?> nameToBeFilled = null;
        boolean accepted=true;
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList references = children.item(i).getChildNodes();
                for (int j=0;j<references.getLength();j++){
                    if(references.item(j).getNodeName().equalsIgnoreCase("tax:bibref")){
                        String ref = references.item(j).getTextContent().trim();
                        if (ref.endsWith(";")  && (ref.length())>1) {
                            ref=ref.substring(0, ref.length()-1)+".";
                        }
                        logger.info("Current reference :"+ref+", "+treatmentMainName+"--"+ref.indexOf(treatmentMainName));


                        boolean makeEmpty = false;
                        Rank rank = null;
                        logger.info("TREATMENTMAINNAME : "+treatmentMainName);
                        logger.info("ref: "+ref);
                        //                        if(ref.indexOf(treatmentMainName) == -1 ){
                        //                            ref=treatmentMainName+" "+ref;
                        //                            accepted=false;
                        //                        }
                        //                        else{
                        //                            accepted=true;
                        //                        }
                        if (j==0) {
                            accepted=true;
                        } else {
                            accepted=false;
                        }

                        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
                        if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                            nameToBeFilled = BotanicalName.NewInstance(null);
                        }
                        if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                            nameToBeFilled = ZoologicalName.NewInstance(null);
                        }
                        if (accepted){
                            nameToBeFilled = (NonViralName<?>) parser.parseFullName(ref, nomenclaturalCode, rank);
                        }else{
                            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
                            acceptedTaxon.addDescription(td);
                            TextData descBase = TextData.NewInstance(Feature.CITATION());
                            Reference<?> reference = ReferenceFactory.newGeneric();
                            reference.setTitleCache(ref, true);

                            descBase.addSource("", "", reference, "", acceptedTaxon.getName(), ref);
                            td.addElement(descBase);

                            importer.getDescriptionService().saveOrUpdate(td);
                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);


                        }
                        logger.warn("BWAAHHHH: "+nameToBeFilled.getParsingProblems()+", "+ref);
                        //
                        //
                        //
                        ////                        parser.parseReferencedName(nameToBeFilled, ref, rank, makeEmpty);
                        //                        int retry=0;
                        ////                        if (nameToBeFilled.hasProblem() &&
                        ////                                !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ){
                        //
                        //                                                    while (nameToBeFilled.hasProblem() &&
                        //                                                            !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) &&
                        //                                                            retry<3){
                        //                                                        logger.info("PROBLEM: "+nameToBeFilled.getParsingProblems());
                        //                                                        String fullname =  getFullReference(references.item(j).getTextContent().trim(),nameToBeFilled.getParsingProblems());
                        //                                                        if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                        //                                                            nameToBeFilled = BotanicalName.NewInstance(null);
                        //                                                        }
                        //                                                        if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                        //                                                            nameToBeFilled = ZoologicalName.NewInstance(null);
                        //                                                        }
                        //                                                        parser.parseReferencedName(nameToBeFilled, fullname, rank, false);
                        //                                                        retry++;
                        //                                                    }
                        //                                                    if (nameToBeFilled.hasProblem() &&
                        //                                                            !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) &&
                        //                                                            retry ==2 ){
                        //                                                        logger.info("FULL TITLE CACHEREF "+ref);
                        //                            nameToBeFilled.setFullTitleCache(ref, true);
                        //                        }
                        //                        treatmentMainName =nameToBeFilled.getNameCache();
                        //                        tosave.add(nameToBeFilled);
                        //                        if (accepted) {
                        ////                            acceptedTaxon = importer.getTaxonService().findBestMatchingTaxon(nameToBeFilled.getFullTitleCache());
                        ////                            if (acceptedTaxon ==null){
                        ////                                acceptedTaxon= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
                        ////                                classification.addChildTaxon(acceptedTaxon,(Reference<?>)nameToBeFilled.getNomenclaturalReference(),"",null);
                        ////                            }
                        ////                            else{logger.info("ACCEPTED EXISTS!!!");}
                        //                            // acceptedTaxon =  new Taxon(acceptedTaxon);
                        //                        }else{
                        //                            Taxon tmp= importer.getTaxonService().findBestMatchingTaxon(nameToBeFilled.getFullTitleCache());
                        //                            if (tmp ==null){
                        //                                tmp= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
                        //                                classification.addChildTaxon(tmp,(Reference<?>) nameToBeFilled.getNomenclaturalReference() ,"",null);
                        //                            }
                        //                            else{logger.info("TAXON EXISTS!!!!");}
                        //                        }
                    }
                }
            }
        }
        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;

    }

    /**
     * @param item
     * @param tosave
     */
    private Taxon extractNomenclature(Node nomenclatureNode,  List<TaxonNameBase> tosave) {
        NodeList children = nomenclatureNode.getChildNodes();
        String freetext;
        NonViralName<?> nameToBeFilled = null;
        Taxon acceptedTaxon = null;
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("#text")) {
                freetext=children.item(i).getTextContent();
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:name")){
                String[]names = extractScientificName(children.item(i));
                treatmentMainName = names[1];
                originalTreatmentName = names[0];


                if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                    nameToBeFilled = BotanicalName.NewInstance(null);
                }
                if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                    nameToBeFilled = ZoologicalName.NewInstance(null);
                }

                acceptedTaxon = importer.getTaxonService().findBestMatchingTaxon(treatmentMainName);
                if (acceptedTaxon ==null){
                    nameToBeFilled = (NonViralName<?>) parser.parseFullName(treatmentMainName, nomenclaturalCode, null);
                    if (!originalTreatmentName.isEmpty()) {
                        TaxonNameDescription td = TaxonNameDescription.NewInstance();
                        td.setTitleCache(originalTreatmentName);
                        nameToBeFilled.addDescription(td);
                    }
                    tosave.add(nameToBeFilled);
                    acceptedTaxon= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
                    classification.addChildTaxon(acceptedTaxon,(Reference<?>)nameToBeFilled.getNomenclaturalReference(),"",null);
                }
            }
        }
        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;
    }


    HashMap<String,Map<String,String>> namesMap = new HashMap<String, Map<String,String>>();

    /**
     * @param item
     * @param tosave
     */
    private String[] extractScientificName(Node name) {
        NodeList children = name.getChildNodes();
        String fullName = "";
        String newName="";
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        String atomisedName="";
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:xmldata")){
                NodeList atom = children.item(i).getChildNodes();
                for (int k=0;k<atom.getLength();k++){
                    atomisedMap.put(atom.item(k).getNodeName().toLowerCase(),atom.item(k).getTextContent().trim());
                    atomisedName+=" "+atom.item(k).getTextContent().trim();
                    //                    logger.info(atom.item(k).getNodeName().toLowerCase()+":"+atom.item(k).getTextContent().trim());
                }
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("#text") && !StringUtils.isBlank(children.item(i).getTextContent())){
                logger.info("name non atomised: "+children.item(i).getTextContent());
                fullName = children.item(i).getTextContent().trim();
                logger.info("fullname: "+fullName);
            }
        }
        if (fullName != null){
            fullName = fullName.replace("( ", "(");
            fullName = fullName.replace(" )",")");
        }
        namesMap.put(fullName,atomisedMap);
        while(atomisedName.contains("  ")) {
            atomisedName=atomisedName.replace("  ", " ");
        }
        atomisedName = atomisedName.trim();
        while(fullName.contains("  ")) {
            fullName=fullName.replace("  ", " ");
        }
        if (!fullName.equalsIgnoreCase(atomisedName)) {
            newName=getScientificName(fullName, atomisedName);
        }
        String[] names = new String[2];
        names[0]=fullName;
        names[1]=newName;
        return names;

    }


}
