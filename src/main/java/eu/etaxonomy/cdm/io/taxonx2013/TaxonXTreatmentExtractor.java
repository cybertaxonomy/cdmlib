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
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
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

    private final HashMap<String,Map<String,String>> namesMap = new HashMap<String, Map<String,String>>();


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
    @SuppressWarnings({ "rawtypes", "unused" })
    protected void extractTreatment(Node treatmentnode, List<Object> tosave, Reference<?> refMods) {
        logger.info("extractTreatment");
        List<TaxonNameBase> nametosave = new ArrayList<TaxonNameBase>();
        NodeList children = treatmentnode.getChildNodes();
        Taxon acceptedTaxon =null;
        Taxon defaultTaxon =null;
        boolean refgroup=false;

        IdentifiableSource source =null;
        if (!refMods.getSources().isEmpty()) {
            source = refMods.getSources().iterator().next();
        }

        logger.info("SOURCE :"+source);
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group")) {
                refgroup=true;
            }
        }
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div")) {
                logger.info("NODE:"+children.item(i).getAttributes().getNamedItem("type").getNodeValue());
            }
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
                    acceptedTaxon = extractNomenclature(children.item(i),nametosave,refMods, source);
                }
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group")){
                reloadClassification();
                extractReferences(children.item(i),nametosave,acceptedTaxon,source);

            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("multiple")){
                extractSynonyms(children.item(i),nametosave, acceptedTaxon,refMods);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("description")){
                extractDescription(children.item(i),acceptedTaxon,defaultTaxon,source,nametosave, refMods);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("distribution")){
                extractDistribution(children.item(i),acceptedTaxon,defaultTaxon,refMods,source);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("materials_examined")){
                extractMaterials(children.item(i),acceptedTaxon,source, refMods);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("biology_ecology")){
                extractBiologyEcology(children.item(i),acceptedTaxon,defaultTaxon, source,nametosave, refMods);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("diagnosis")){
                extractDiagnosis(children.item(i),acceptedTaxon,defaultTaxon,source,nametosave, refMods);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("key")){
                extractKey(children.item(i),acceptedTaxon, nametosave,source, refMods);
            }

        }
        logger.info("saveUpdateNames");
        importer.getNameService().saveOrUpdate(nametosave);
        importer.getClassificationService().saveOrUpdate(classification);
        logger.info("saveUpdateNames-ok");


    }

    /**
     * @param item
     * @param acceptedTaxon
     */
    @SuppressWarnings("rawtypes")
    private void extractKey(Node keys, Taxon acceptedTaxon,List<TaxonNameBase> nametosave, IdentifiableSource modsSource, Reference<?> refMods) {
        NodeList children = keys.getChildNodes();
        String key="";
        PolytomousKey poly =  PolytomousKey.NewInstance();
        poly.addSource(modsSource);
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
                        taxonKey=getTaxonFromXML(paragraph.item(j),nametosave,acceptedTaxon,modsSource, refMods);
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Taxon getTaxonFromXML(Node taxons, List<TaxonNameBase> nametosave, Taxon acceptedTaxon,
            IdentifiableSource modsSource, Reference<?> refMods) {
        logger.info("getTaxonFromXML");
        logger.info("acceptedTaxon: "+acceptedTaxon);
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
            if (retry == 2){
                nameToBeFilled.setFullTitleCache(name, true);
                logger.info("FULL TITLE CACHE "+name);
            }
        }
        importer.getNameService().saveOrUpdate(nametosave);
        Taxon t = importer.getTaxonService().findBestMatchingTaxon(nameToBeFilled.getTitleCache());
        if (t ==null){
            t= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
            if (t.getSec() == null) {
                t.setSec(refMods);
            }
            t.addSource(modsSource);
            nametosave.add(nameToBeFilled);
            Taxon parentTaxon = askParent(t, classification);
            if (parentTaxon == null) {
                classification.addChildTaxon(t,(Reference<?>)nameToBeFilled.getNomenclaturalReference(),null,null);
            } else {
                classification.addParentChild(parentTaxon, t, refMods, null);
            }
        }

        return t;
    }



    /**
     * @param item
     * @param acceptedTaxon
     */
    private void extractDiagnosis(Node diagnosis, Taxon acceptedTaxon, Taxon defaultTaxon,IdentifiableSource modsSource,
            List<TaxonNameBase> nametosave, Reference<?> refMods) {
        logger.info("extractDiagnosis");
        logger.info("acceptedTaxon: "+acceptedTaxon);
        logger.info("defaultTaxon: "+defaultTaxon);
        NodeList children = diagnosis.getChildNodes();
        NodeList insideNodes ;
        String descr ="";
        String localdescr="";
        for (int i=0;i<children.getLength();i++){
            localdescr="";
            if (children.item(i).getNodeName().equalsIgnoreCase("#text") && !children.item(i).getTextContent().trim().isEmpty()){
                descr += children.item(i).getTextContent().trim();
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                insideNodes=children.item(i).getChildNodes();
                List<String> blabla= new ArrayList<String>();
                for (int j=0;j<insideNodes.getLength();j++){
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        Taxon linkedTaxon = getTaxonFromXML(insideNodes.item(j), nametosave,acceptedTaxon,modsSource, refMods);//TODO NOT IMPLEMENTED IN THE CDM YET
                        blabla.add(linkedTaxon.getTitleCache());
                    }
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("#text")) {

                        if(!insideNodes.item(j).getTextContent().trim().isEmpty()){
                            blabla.add(insideNodes.item(j).getTextContent().trim());
                            localdescr += insideNodes.item(j).getTextContent().trim();
                        }
                    }
                }
                if (!blabla.isEmpty()) {
                    logger.info("DESCRIPTION DIAGNOSIS "+blabla);
                    setParticularDescription(StringUtils.join(blabla," "),acceptedTaxon,defaultTaxon, modsSource,Feature.DIAGNOSIS());
                }
            }

        }


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
    @SuppressWarnings({ "unused", "rawtypes" })
    private Taxon getTaxon(TaxonNameBase taxonNameBase, Reference<?> refMods, IdentifiableSource modsSource) {
        Taxon t = new Taxon(taxonNameBase,null );
        if (t.getSec() == null) {
            t.setSec(refMods);
        }
        t.addSource(modsSource);
        return t;
    }

    /**
     * @param item
     */
    @SuppressWarnings("rawtypes")
    private void extractDistribution(Node distribution, Taxon acceptedTaxon, Taxon defaultTaxon, Reference<?> refMods,
            IdentifiableSource modsSource) {
        logger.info("DISTRIBUTION");
        logger.info("acceptedTaxon: "+acceptedTaxon);
        NodeList children = distribution.getChildNodes();
        Map<Integer,List<MySpecimenOrObservation>> specimenOrObservations = new HashMap<Integer, List<MySpecimenOrObservation>>();
        Map<Integer,String> descriptionsFulltext = new HashMap<Integer,String>();

        for (int i=0;i<children.getLength();i++){
            specimenOrObservations = new HashMap<Integer, List<MySpecimenOrObservation>>();
            descriptionsFulltext = new HashMap<Integer,String>();
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList paragraph = children.item(i).getChildNodes();
                for (int j=0;j<paragraph.getLength();j++){
                    if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text")){
                        if(!paragraph.item(j).getTextContent().trim().isEmpty()) {
                            descriptionsFulltext.put(i,paragraph.item(j).getTextContent().trim());
                        }
                    }
                    else if (paragraph.item(j).getNodeName().equalsIgnoreCase("tax:collection_event")){
                        MySpecimenOrObservation specimenOrObservation = new MySpecimenOrObservation();
                        DerivedUnitBase derivedUnitBase = null;
                        specimenOrObservation = extractSpecimenOrObservation(paragraph.item(j), derivedUnitBase);
                        List<MySpecimenOrObservation> speObsList = specimenOrObservations.get(i);
                        if (speObsList == null) {
                            speObsList=new ArrayList<MySpecimenOrObservation>();
                        }
                        speObsList.add(specimenOrObservation);
                        specimenOrObservations.put(i,speObsList);
                    }
                }


            }
            if (!descriptionsFulltext.isEmpty()) {
                logger.info("descriptionsFulltext: "+descriptionsFulltext);
            }
            if(!specimenOrObservations.isEmpty()) {
                logger.info("specimenOrObservations: "+specimenOrObservations);
            }

            int m=0;
            for (int k:descriptionsFulltext.keySet()) {
                if (k>m) {
                    m=k;
                }
            }
            for (int k:specimenOrObservations.keySet()) {
                if (k>m) {
                    m=k;
                }
            }


            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
            Feature currentFeature = Feature.DISTRIBUTION();
            for (int k=0;k<=m;k++){
                if (descriptionsFulltext.keySet().contains(k)){
                    if (!descriptionsFulltext.get(k).isEmpty() && (descriptionsFulltext.get(k).startsWith("Hab.") || descriptionsFulltext.get(k).startsWith("Habitat"))){
                        setParticularDescription(descriptionsFulltext.get(k),acceptedTaxon,defaultTaxon, modsSource, Feature.HABITAT());
                        logger.info("should break ! "+k);
                        break;
                    }
                    else{
                        TextData textData = TextData.NewInstance();

                        textData.setFeature(currentFeature);
                        textData.putText(Language.UNKNOWN_LANGUAGE(), descriptionsFulltext.get(k));
                        textData.addSource(null, null, refMods, null);

                        td.addElement(textData);
                    }
                }
                if(specimenOrObservations.keySet().contains(k)){
                    System.out.println("here with k "+k);
                    for (MySpecimenOrObservation soo:specimenOrObservations.get(k) ) {
                        td.addDescribedSpecimenOrObservation(soo.getDerivedUnitBase());
                    }
                }

                if (descriptionsFulltext.keySet().contains(k) || specimenOrObservations.keySet().contains(k)){
                    td.addSource(modsSource);
                    acceptedTaxon.addDescription(td);
                    importer.getDescriptionService().saveOrUpdate(td);
                    importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                }



            }
        }
    }





    /*
     * <tax:collection_event><tax:xmldata><dwc:Country>Argentina</dwc:Country>
     * <dwc:StateProvince>Chubut</dwc:StateProvince><dwc:Locality>Depto</dwc:Locality><dwc:Collector>Soriano</dwc:Collector>
     * </tax:xmldata>Exsiccata; Prov. Chubut : Depto Cushamen : Leleque , Soriano2350 ( BAA )</tax:collection_event>
     * */



    /**
     * @param item
     */
    @SuppressWarnings("rawtypes")
    private void extractMaterials(Node materials, Taxon acceptedTaxon, IdentifiableSource modsSource, Reference<?> modsRef) {
        logger.info("EXTRACTMATERIALS");
        logger.info("acceptedTaxon: "+acceptedTaxon);
        NodeList children = materials.getChildNodes();
        NodeList events = null;
        String descr="";

        DerivedUnitBase derivedUnitBase=null;
        MySpecimenOrObservation myspecimenOrObservation = null;

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                events = children.item(i).getChildNodes();
                for(int k=0;k<events.getLength();k++){

                    if(events.item(k).getNodeName().equalsIgnoreCase("tax:collection_event")){
                        myspecimenOrObservation = extractSpecimenOrObservation(events.item(k),derivedUnitBase);
                        derivedUnitBase = myspecimenOrObservation.getDerivedUnitBase();
                        descr=myspecimenOrObservation.getDescr();

                        derivedUnitBase.addSource(modsSource);

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
                        indAssociation.addSource(null, null, modsRef, null);

                        taxonDescription.addElement(indAssociation);
                        taxonDescription.setTaxon(acceptedTaxon);
                        taxonDescription.addSource(modsSource);

                        importer.getDescriptionService().saveOrUpdate(taxonDescription);
                        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                    }
                    else{
                        String rawAssociation = events.item(k).getTextContent().trim();
                        logger.info("RAW ASSOCIATION "+rawAssociation);
                        DerivedUnitFacade derivedUnitFacade = getFacade(rawAssociation);
                        derivedUnitBase = derivedUnitFacade.innerDerivedUnit();
                        derivedUnitBase.addSource(modsSource);
                        importer.getOccurrenceService().saveOrUpdate(derivedUnitBase);

                        TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);
                        acceptedTaxon.addDescription(taxonDescription);

                        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();

                        Feature feature = makeFeature(derivedUnitBase);
                        if(!StringUtils.isEmpty(rawAssociation)) {
                            derivedUnitBase.setTitleCache(rawAssociation, true);
                        }
                        indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
                        indAssociation.setFeature(feature);
                        indAssociation.addSource(null, null, modsRef, null);

                        taxonDescription.addElement(indAssociation);
                        taxonDescription.setTaxon(acceptedTaxon);
                        taxonDescription.addSource(modsSource);

                        importer.getDescriptionService().saveOrUpdate(taxonDescription);
                        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                    }
                }
            }
        }
    }


    private void extractBiologyEcology(Node description, Taxon acceptedTaxon, Taxon defaultTaxon, IdentifiableSource modsSource,
            List<TaxonNameBase> nametosave, Reference<?> refMods ) {
        logger.info("extractBiologyEcology");
        logger.info("acceptedTaxon: "+acceptedTaxon);
        logger.info("defaultTaxon: "+defaultTaxon);
        NodeList children = description.getChildNodes();
        NodeList insideNodes ;
        String descr ="";
        String localdescr="";
        for (int i=0;i<children.getLength();i++){
            localdescr="";
            if (children.item(i).getNodeName().equalsIgnoreCase("#text") && !children.item(i).getTextContent().trim().isEmpty()){
                descr += children.item(i).getTextContent().trim();
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                insideNodes=children.item(i).getChildNodes();
                List<String> blabla= new ArrayList<String>();
                for (int j=0;j<insideNodes.getLength();j++){
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        Taxon linkedTaxon = getTaxonFromXML(insideNodes.item(j), nametosave,acceptedTaxon,modsSource, refMods);//TODO NOT IMPLEMENTED IN THE CDM YET
                        blabla.add(linkedTaxon.getTitleCache());
                    }
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("#text")) {

                        if(!insideNodes.item(j).getTextContent().trim().isEmpty()){
                            blabla.add(insideNodes.item(j).getTextContent().trim());
                            localdescr += insideNodes.item(j).getTextContent().trim();
                        }
                    }
                }
                if (!blabla.isEmpty()) {
                    logger.info("DESCRIPTION BIOLOGYECOLOGY "+blabla);
                    setParticularDescription(StringUtils.join(blabla," "),acceptedTaxon,defaultTaxon, modsSource,Feature.BIOLOGY_ECOLOGY());
                }
            }

        }


    }
    /**
     * @param item
     * @param acceptedTaxon
     * @param defaultTaxon
     */
    private void extractDescription(Node description, Taxon acceptedTaxon, Taxon defaultTaxon, IdentifiableSource modsSource,
            List<TaxonNameBase> nametosave, Reference<?> refMods) {
        logger.info("extractDescription");
        logger.info("acceptedTaxon: "+acceptedTaxon);
        logger.info("defaultTaxon: "+defaultTaxon);
        NodeList children = description.getChildNodes();
        Feature currentFeature = Feature.DESCRIPTION();
        NodeList insideNodes ;
        String localdescr="";
        String descr ="";

        for (int i=0;i<children.getLength();i++){
            String tmpDesc = children.item(i).getTextContent().trim();
            if (!tmpDesc.isEmpty() && (tmpDesc.startsWith("Hab.") || tmpDesc.startsWith("Habitat"))){
                currentFeature=Feature.HABITAT();
            }
            localdescr="";
            if (children.item(i).getNodeName().equalsIgnoreCase("#text") && !children.item(i).getTextContent().trim().isEmpty()){
                descr += children.item(i).getTextContent().trim();
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                insideNodes=children.item(i).getChildNodes();
                List<String> blabla= new ArrayList<String>();
                for (int j=0;j<insideNodes.getLength();j++){
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        Taxon linkedTaxon = getTaxonFromXML(insideNodes.item(j), nametosave,acceptedTaxon,modsSource, refMods);//TODO NOT IMPLEMENTED IN THE CDM YET
                        blabla.add(linkedTaxon.getTitleCache());
                    }
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("#text")) {

                        if(!insideNodes.item(j).getTextContent().trim().isEmpty()){
                            blabla.add(insideNodes.item(j).getTextContent().trim());
                            localdescr += insideNodes.item(j).getTextContent().trim();
                        }
                    }
                }
                if (!blabla.isEmpty()) {
                    logger.info("DESCRIPTION DESCR "+blabla);
                    setParticularDescription(StringUtils.join(blabla," "),acceptedTaxon,defaultTaxon, modsSource,currentFeature);
                }
            }

        }

    }


    /**
     * @param descr
     * @param acceptedTaxon
     * @param defaultTaxon
     * @param modsSource
     * @param currentFeature
     * @return
     */
    private void setParticularDescription(String descr, Taxon acceptedTaxon, Taxon defaultTaxon, IdentifiableSource modsSource,Feature currentFeature) {
        logger.info("setParticularDescription "+currentFeature);
        logger.info("acceptedTaxon: "+acceptedTaxon);
        TextData textData = TextData.NewInstance();
        textData.setFeature(currentFeature);

        textData.putText(Language.UNKNOWN_LANGUAGE(), descr+"<br/>");

        if(! descr.isEmpty() && acceptedTaxon!=null){
            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
            td.addElement(textData);
            td.addSource(modsSource);
            acceptedTaxon.addDescription(td);
            importer.getDescriptionService().saveOrUpdate(td);
            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        }

        if(! descr.isEmpty() && acceptedTaxon == null && defaultTaxon != null){
            try{
                Taxon tmp =(Taxon) importer.getTaxonService().find(defaultTaxon.getUuid());
                if (tmp!=null) {
                    defaultTaxon=tmp;
                }else{
                    importer.getTaxonService().saveOrUpdate(defaultTaxon);
                }
            }catch(Exception e){
                logger.info("TAXON EXISTS"+defaultTaxon);
            }

            TaxonDescription td =importer.getTaxonDescription(defaultTaxon, false, true);
            defaultTaxon.addDescription(td);
            td.addElement(textData);
            td.addSource(modsSource);
            importer.getDescriptionService().saveOrUpdate(td);
            importer.getTaxonService().saveOrUpdate(defaultTaxon);
        }
    }



    /**
     * @param item
     * @param nametosave
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void extractSynonyms(Node synonnyms, List<TaxonNameBase> nametosave,Taxon acceptedTaxon, Reference<?> modsRef) {
        logger.info("extractSynonyms: "+acceptedTaxon);
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
                if (retry == 2){
                    nameToBeFilled.setFullTitleCache(name, true);
                    logger.info("FULL TITLE CACHE "+name);
                }
            }
            nametosave.add(nameToBeFilled);
            Synonym synonym = Synonym.NewInstance(nameToBeFilled, modsRef);

            acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        }

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

    /**
     * @param item
     * @param tosave
     */
    @SuppressWarnings({ "null", "unused" ,"rawtypes" })
    private Taxon extractReferences(Node refgroup, List<TaxonNameBase> tosave, Taxon acceptedTaxon, IdentifiableSource refSource) {
        logger.info("extractReferences");
        NodeList children = refgroup.getChildNodes();
        NonViralName<?> nameToBeFilled = null;
        boolean accepted=true;
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList references = children.item(i).getChildNodes();
                int nbRef=0;
                for (int j=0;j<references.getLength();j++){
                    if(references.item(j).getNodeName().equalsIgnoreCase("tax:bibref")){
                        String ref = references.item(j).getTextContent().trim();
                        if (ref.endsWith(";")  && (ref.length())>1) {
                            ref=ref.substring(0, ref.length()-1)+".";
                        }
                        if (ref.startsWith(treatmentMainName) && !ref.endsWith(treatmentMainName)) {
                            ref=ref.replace(treatmentMainName, "");
                            ref=ref.trim();
                            while (ref.startsWith(".") || ref.startsWith(",")) {
                                ref=ref.replace(".","").replace(",","").trim();
                            }
                        }

                        logger.info("Current reference :"+nbRef+", "+ref+", "+treatmentMainName+"--"+ref.indexOf(treatmentMainName));
                        Reference<?> reference = ReferenceFactory.newGeneric();
                        reference.setTitleCache(ref, true);

                        boolean makeEmpty = false;
                        Rank rank = null;
                        logger.info("TREATMENTMAINNAME : "+treatmentMainName);
                        logger.info("ref: "+ref);
                        if (nbRef==0) {
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
                            acceptedTaxon.getName().setNomenclaturalReference(reference);
                            nameToBeFilled.setNomenclaturalReference(reference);
                            acceptedTaxon.addSource(refSource);
                        }else{
                            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
                            acceptedTaxon.addDescription(td);
                            acceptedTaxon.addSource(refSource);

                            TextData textData = TextData.NewInstance(Feature.CITATION());

                            textData.addSource(null, null, reference, null, acceptedTaxon.getName(), ref);
                            td.addElement(textData);
                            td.addSource(refSource);

                            importer.getDescriptionService().saveOrUpdate(td);
                        }
                        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                        logger.warn("BWAAHHHH: "+nameToBeFilled.getParsingProblems()+", "+ref);
                        nbRef++;
                    }
                }
            }
        }
        //        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;

    }

    /**
     * @param item
     * @param tosave
     */
    @SuppressWarnings({ "rawtypes", "unused" })
    private Taxon extractNomenclature(Node nomenclatureNode,  List<TaxonNameBase> tosave, Reference<?> refMods, IdentifiableSource modsSource) {
        logger.info("extractNomenclature");
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
                    int retry=0;
                    Map<String,String> ato = namesMap.get(originalTreatmentName);
                    Rank rank = getRank(ato);
                    while (nameToBeFilled.hasProblem() && retry <3 && !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank))){
                        String fullname =  getFullReference(treatmentMainName,nameToBeFilled.getParsingProblems());
                        if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                            nameToBeFilled = BotanicalName.NewInstance(null);
                        }
                        if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                            nameToBeFilled = ZoologicalName.NewInstance(null);
                        }
                        parser.parseReferencedName(nameToBeFilled, fullname, rank, false);
                        retry++;
                    }
                    if (!originalTreatmentName.isEmpty()) {
                        TaxonNameDescription td = TaxonNameDescription.NewInstance();
                        td.setTitleCache(originalTreatmentName);
                        nameToBeFilled.addDescription(td);
                    }
                    nameToBeFilled.addSource(null, null, refMods, null);
                    tosave.add(nameToBeFilled);
                    acceptedTaxon= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
                    if (acceptedTaxon.getSec() == null) {
                        acceptedTaxon.setSec(refMods);
                    }
                    acceptedTaxon.addSource(modsSource);
                    importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                    Taxon parentTaxon = askParent(acceptedTaxon, classification);
                    if (parentTaxon == null) {
                        classification.addChildTaxon(acceptedTaxon,(Reference<?>)nameToBeFilled.getNomenclaturalReference(),null,null);
                    } else {
                        classification.addParentChild(parentTaxon, acceptedTaxon, refMods, null);
                    }
                }else{
                    Set<IdentifiableSource> sources = acceptedTaxon.getSources();
                    boolean sourcelinked=false;
                    for (IdentifiableSource source:sources){
                        if (source.getCitation().getTitle().equalsIgnoreCase(refMods.getTitleCache())) {
                            sourcelinked=true;
                        }
                    }
                    if (!sourcelinked){
                        acceptedTaxon.addSource(null, null, refMods, null);
                        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                    }

                }
            }
        }
        //        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;
    }



    /**
     * @param item
     * @param tosave
     */
    private String[] extractScientificName(Node name) {
        logger.info("extractScientificName");
        NodeList children = name.getChildNodes();
        String fullName = "";
        String newName="";
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        List<String> atomisedName= new ArrayList<String>();

        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:xmldata")){
                NodeList atom = children.item(i).getChildNodes();
                for (int k=0;k<atom.getLength();k++){
                    atomisedMap.put(atom.item(k).getNodeName().toLowerCase(),atom.item(k).getTextContent().trim());
                    atomisedName.add(atom.item(k).getTextContent().trim());
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
        if (fullName.trim().isEmpty()){
            fullName=StringUtils.join(atomisedName," ");
        }

        while(fullName.contains("  ")) {
            fullName=fullName.replace("  ", " ");
            logger.info("while");
        }

        logger.info("bla");
        namesMap.put(fullName,atomisedMap);
        String atomisedNameStr = StringUtils.join(atomisedName," ");
        while(atomisedNameStr.contains("  ")) {
            atomisedNameStr=atomisedNameStr.replace("  ", " ");
            logger.info("atomisedNameStr: "+atomisedNameStr);
        }
        atomisedNameStr=atomisedNameStr.trim();

        if (fullName != null){
            if (!fullName.equalsIgnoreCase(atomisedNameStr)) {
                newName=getScientificName(fullName,atomisedNameStr);
            } else {
                newName=fullName;
            }
        }
        String[] names = new String[2];
        names[0]=fullName;
        names[1]=newName;
        return names;

    }


}
