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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
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
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
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
     * @param sourceName
     */
    @SuppressWarnings({ "rawtypes", "unused" })
    protected void extractTreatment(Node treatmentnode, List<Object> tosave, Reference<?> refMods, URI sourceName) {
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
        //        if (source==null){
        //            source=IdentifiableSource.NewInstance(refMods, null);
        //        }
        //        importer.getCommonService().saveOrUpdate(source);

        logger.info("SOURCE :"+source);
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group")) {
                refgroup=true;
            }
        }
        for (int i=0;i<children.getLength();i++){
            //            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div")) {
            //                logger.info("NODE:"+children.item(i).getAttributes().getNamedItem("type").getNodeValue());
            //            }
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
                    acceptedTaxon = extractNomenclature(children.item(i),nametosave,refMods,source);
                }
            }
            else if (children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group")){
                reloadClassification();
                extractReferences(children.item(i),nametosave,acceptedTaxon,source,nametosave, refMods);

            }
            else if (children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("multiple")){
                File file = new File("/home/pkelbert/Bureau/multipleTaxonX.txt");
                FileWriter writer;
                try {
                    writer = new FileWriter(file ,true);
                    writer.write(sourceName+"\n");
                    writer.flush();
                    writer.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                String multiple = askMultiple(children.item(i).getTextContent());
                if (multiple.equalsIgnoreCase("synonyms")) {
                    extractSynonyms(children.item(i),nametosave, acceptedTaxon,refMods);
                }
                else
                    if(multiple.equalsIgnoreCase("material examined")){
                        extractMaterials(children.item(i),acceptedTaxon,source, refMods);
                    }
                    else
                        if (multiple.equalsIgnoreCase("distribution")){
                            extractDistribution(children.item(i),acceptedTaxon,defaultTaxon,refMods,source);
                        }
                        else {
                            extractSpecificFeature(children.item(i),acceptedTaxon,defaultTaxon,source,nametosave, refMods,multiple);
                        }
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("biology_ecology")){
                extractBiologyEcology(children.item(i),acceptedTaxon,defaultTaxon, source,nametosave, refMods);
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("description")){
                extractDescription(children.item(i),acceptedTaxon,defaultTaxon,source,nametosave, refMods);
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("diagnosis")){
                extractDiagnosis(children.item(i),acceptedTaxon,defaultTaxon,source,nametosave, refMods);
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("discussion")){
                extractDiscussion(children.item(i),acceptedTaxon,defaultTaxon,source,nametosave, refMods);
            }

            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("distribution")){
                extractDistribution(children.item(i),acceptedTaxon,defaultTaxon,refMods,source);
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("etymology")){
                extractEtymology(children.item(i),acceptedTaxon,defaultTaxon,source,nametosave, refMods);
            }

            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("materials_examined")){
                extractMaterials(children.item(i),acceptedTaxon,source, refMods);
            }


            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("key")){
                //TODO IGNORE keys for the moment
                //extractKey(children.item(i),acceptedTaxon, nametosave,source, refMods);
                extractSpecificFeature(children.item(i),acceptedTaxon,defaultTaxon,source,nametosave, refMods,"Keys - unparsed");
            }
            else{
                logger.info("ANOTHER KIND OF NODES : "+children.item(i).getNodeName()+", "+children.item(i).getAttributes());
                if (children.item(i).getAttributes() !=null) {
                    logger.info(children.item(i).getAttributes().item(0));
                }
            }

        }
        //        logger.info("saveUpdateNames");
        importer.getNameService().saveOrUpdate(nametosave);
        importer.getClassificationService().saveOrUpdate(classification);
        logger.info("saveUpdateNames-ok");


    }

    /**
     * @param item
     * @param acceptedTaxon
     * @param defaultTaxon
     * @param source
     * @param nametosave
     * @param refMods
     */
    private void extractDiscussion(Node discussion, Taxon acceptedTaxon, Taxon defaultTaxon, IdentifiableSource modsSource,
            List<TaxonNameBase> nametosave, Reference<?> refMods ) {

        NodeList children = discussion.getChildNodes();
        String fullContent = discussion.getTextContent();

        List<String> fullDescription= parseParagraph(children, nametosave, acceptedTaxon, modsSource, refMods, fullContent);

        if (!fullDescription.isEmpty()) {
            setParticularDescription(StringUtils.join(fullDescription,"<br/>"),acceptedTaxon,defaultTaxon, modsSource,Feature.DISCUSSION());
        }

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

        String fullContent = keys.getTextContent();
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList paragraph = children.item(i).getChildNodes();
                key="";
                taxonKey=null;
                for (int j=0;j<paragraph.getLength();j++){
                    if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text")){
                        if (! paragraph.item(j).getTextContent().trim().isEmpty()){
                            key+=paragraph.item(j).getTextContent().trim();
                            //                            logger.info("KEY : "+j+"--"+key);
                        }
                    }
                    if(paragraph.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        taxonKey=getTaxonFromXML(paragraph.item(j),nametosave,acceptedTaxon,modsSource, refMods, fullContent);
                    }
                }
                //                logger.info("keypattern.matcher(key).matches(): "+keypattern.matcher(key).matches());
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
            IdentifiableSource modsSource, Reference<?> refMods, String fullContent) {
        //        logger.info("getTaxonFromXML");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        TaxonNameBase nameToBeFilled = null;
        String name;

        String[] enames = extractScientificName(taxons,fullContent);
        if (enames[1].isEmpty()) {
            name=enames[0];
        } else {
            name=enames[1];
        }

        String original=enames[0];
        Rank rank;
        try {
            rank = Rank.getRankByName(enames[2]);
        } catch (UnknownCdmTypeException e) {
            logger.warn("Rank problem!");
            rank=null;
        }
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();

        nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);
        if (nameToBeFilled.hasProblem() &&
                !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
            //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
            nameToBeFilled=solveNameProblem(original, name,parser);

        }
        importer.getNameService().saveOrUpdate(nametosave);
        Taxon t = importer.getTaxonService().findBestMatchingTaxon(nameToBeFilled.getTitleCache());
        if (t ==null || (t!=null && t.getSec() != refMods)){
            //            logger.info("BestTaxonService not the best or null");
            t= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
            if (t.getSec() == null) {
                t.setSec(refMods);
            }
            t.addSource(modsSource);
            nametosave.add(nameToBeFilled);
            Taxon parentTaxon = askParent(t, classification);
            if (parentTaxon ==null){
                while (parentTaxon == null) {
                    //                    logger.info("PARENT MISSING");
                    parentTaxon = createParent(t, refMods);
                    classification.addParentChild(parentTaxon, t, refMods, null);
                }
            }else{
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
        //        logger.info("extractDiagnosis");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        //        logger.info("defaultTaxon: "+defaultTaxon);
        NodeList children = diagnosis.getChildNodes();

        String fullContent = diagnosis.getTextContent();
        List<String> fullDescription= parseParagraph(children, nametosave, acceptedTaxon, modsSource, refMods, fullContent);

        if (!fullDescription.isEmpty()) {
            setParticularDescription(StringUtils.join(fullDescription,"<br/>"),acceptedTaxon,defaultTaxon, modsSource,Feature.DIAGNOSIS());
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
        //        logger.info("DISTRIBUTION");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
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
            //            if (!descriptionsFulltext.isEmpty()) {
            //                logger.info("descriptionsFulltext: "+descriptionsFulltext);
            //            }
            //            if(!specimenOrObservations.isEmpty()) {
            //                logger.info("specimenOrObservations: "+specimenOrObservations);
            //            }

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
        //        logger.info("EXTRACTMATERIALS");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
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
                        if (rawAssociation.length()>1){
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
    }


    private void extractSpecificFeature(Node description, Taxon acceptedTaxon, Taxon defaultTaxon, IdentifiableSource modsSource,
            List<TaxonNameBase> nametosave, Reference<?> refMods, String featureName ) {
        NodeList children = description.getChildNodes();
        NodeList insideNodes ;
        String descr ="";
        String localdescr="";

        String fullContent = description.getTextContent();
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
                        Taxon linkedTaxon = getTaxonFromXML(insideNodes.item(j), nametosave,acceptedTaxon,modsSource, refMods,fullContent);//TODO NOT IMPLEMENTED IN THE CDM YET
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
                    List<DefinedTermBase> features = importer.getTermService().list(Feature.class, null,null,null,null);
                    Feature currentFeature=null;
                    for (DefinedTermBase feature: features){
                        String tmpF = ((Feature)feature).getTitleCache();
                        if (tmpF.equalsIgnoreCase(featureName)) {
                            currentFeature=(Feature)feature;
                        }
                    }
                    if (currentFeature == null) {
                        currentFeature=Feature.NewInstance(featureName, featureName, featureName);
                        importer.getTermService().saveOrUpdate(currentFeature);
                    }
                    setParticularDescription(StringUtils.join(blabla," "),acceptedTaxon,defaultTaxon, modsSource,currentFeature);
                }
            }

        }

    }
    private void extractBiologyEcology(Node description, Taxon acceptedTaxon, Taxon defaultTaxon, IdentifiableSource modsSource,
            List<TaxonNameBase> nametosave, Reference<?> refMods ) {
        //        logger.info("extractBiologyEcology");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        //        logger.info("defaultTaxon: "+defaultTaxon);
        NodeList children = description.getChildNodes();
        String fullContent = description.getTextContent();
        List<String> fullDescription= parseParagraph(children, nametosave, acceptedTaxon, modsSource, refMods, fullContent);

        if (!fullDescription.isEmpty()) {
            setParticularDescription(StringUtils.join(fullDescription,"<br/>"),acceptedTaxon,defaultTaxon, modsSource,Feature.BIOLOGY_ECOLOGY());
        }


    }

    private List<String> parseParagraph(NodeList children, List<TaxonNameBase> nametosave, Taxon acceptedTaxon, IdentifiableSource modsSource, Reference<?> refMods, String fullContent){
        List<String> fullDescription=  new ArrayList<String>();
        String localdescr;
        String descr="";
        NodeList insideNodes ;


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
                        Taxon linkedTaxon = getTaxonFromXML(insideNodes.item(j), nametosave,acceptedTaxon,modsSource, refMods,fullContent);//TODO NOT IMPLEMENTED IN THE CDM YET
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
                        fullDescription.add(StringUtils.join(blabla," "));
                }
            }

        }

        return fullDescription;
    }

    private void extractEtymology(Node description, Taxon acceptedTaxon, Taxon defaultTaxon, IdentifiableSource modsSource,
            List<TaxonNameBase> nametosave, Reference<?> refMods ) {
        //        logger.info("extractBiologyEcology");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        //        logger.info("defaultTaxon: "+defaultTaxon);
        NodeList children = description.getChildNodes();
        NodeList insideNodes ;
        String descr ="";
        String localdescr="";

        String fullContent = description.getTextContent();
        List<String> fullDescription= parseParagraph(children, nametosave, acceptedTaxon, modsSource, refMods, fullContent);


        if (!fullDescription.isEmpty()) {
            setParticularDescription(StringUtils.join(fullDescription,"<br/>"),acceptedTaxon,defaultTaxon, modsSource,Feature.ETYMOLOGY());
        }


    }
    /**
     * @param item
     * @param acceptedTaxon
     * @param defaultTaxon
     */
    private void extractDescription(Node description, Taxon acceptedTaxon, Taxon defaultTaxon, IdentifiableSource modsSource,
            List<TaxonNameBase> nametosave, Reference<?> refMods) {
        //        logger.info("extractDescription");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        //        logger.info("defaultTaxon: "+defaultTaxon);
        NodeList children = description.getChildNodes();
        String fullContent = description.getTextContent();
        List<String> fullDescription= parseParagraph(children, nametosave, acceptedTaxon, modsSource, refMods, fullContent);

        if (!fullDescription.isEmpty()) {
            setParticularDescription(StringUtils.join(fullDescription,"<br/>"),acceptedTaxon,defaultTaxon, modsSource,Feature.DESCRIPTION());
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
        //        logger.info("setParticularDescription "+currentFeature);
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
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
                logger.debug("TAXON EXISTS"+defaultTaxon);
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
        //        logger.info("extractSynonyms: "+acceptedTaxon);
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
                String fullContent = children.item(i).getTextContent();
                for (int j=0; j< tmp.getLength();j++){
                    if(tmp.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        String[] enames = extractScientificName(tmp.item(j),fullContent);
                        if (enames[1].isEmpty()) {
                            names.add(enames[0]+"---"+enames[2]);
                        } else {
                            names.add(enames[1]+"---"+enames[2]);
                        }
                    }
                }
            }
        }
        for(String name:names){
            //            logger.info("HANDLE NAME "+name);
            Rank rank;
            try {
                rank = Rank.getRankByName(name.split("---")[1]);
            } catch (UnknownCdmTypeException e) {
                logger.warn("Rank problem!");
                rank=null;
            }
            name = name.split("---")[0];
            String original = name;

            INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
            nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);
            if (nameToBeFilled.hasProblem() &&
                    !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                nameToBeFilled = solveNameProblem(original, name, parser);
            }
            nametosave.add(nameToBeFilled);
            Synonym synonym = Synonym.NewInstance(nameToBeFilled, modsRef);

            acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        }

    }





    /**
     * @param item
     * @param tosave
     * @param refMods
     * @param nametosave
     */
    @SuppressWarnings({ "null", "unused" ,"rawtypes" })
    private Taxon extractReferences(Node refgroup, List<TaxonNameBase> tosave, Taxon acceptedTaxon, IdentifiableSource refSource, List<TaxonNameBase> nametosave, Reference<?> refMods) {
        //        logger.info("extractReferences");
        NodeList children = refgroup.getChildNodes();
        NonViralName<?> nameToBeFilled = null;
        boolean accepted=true;
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList references = children.item(i).getChildNodes();
                int nbRef=0;
                boolean foundBibref=false;
                for (int j=0;j<references.getLength();j++){
                    if(references.item(j).getNodeName().equalsIgnoreCase("tax:bibref")){
                        foundBibref=true;
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

                        //                        logger.info("Current reference :"+nbRef+", "+ref+", "+treatmentMainName+"--"+ref.indexOf(treatmentMainName));
                        Reference<?> reference = ReferenceFactory.newGeneric();
                        reference.setTitleCache(ref, true);

                        boolean makeEmpty = false;
                        //                        Rank rank = null;
                        //                        logger.info("TREATMENTMAINNAME : "+treatmentMainName);
                        //                        logger.info("ref: "+ref);
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
                        //                        logger.warn("BWAAHHHH: "+nameToBeFilled.getParsingProblems()+", "+ref);
                        nbRef++;
                    }
                }
                if (!foundBibref){
                    String refString="";
                    String name="";
                    for (int j=0;j<references.getLength();j++){
                        //no bibref tag inside
                        logger.info("references.item(j).getNodeName()"+references.item(j).getNodeName());
                        if (references.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                            String[] enames =  extractScientificName(references.item(j), references.item(j).getTextContent());
                            if (enames[1].isEmpty()) {
                                name=enames[0]+"---"+enames[2];
                            } else {
                                name=enames[1]+"---"+enames[2];
                            }
                            name=name.trim();
                        }
                        if (references.item(j).getNodeName().equalsIgnoreCase("#text")){
                            refString = references.item(j).getTextContent().trim();
                        }
                        if(references.item(j).getNodeName().equalsIgnoreCase("#text") && name.isEmpty() && !references.item(j).getTextContent().trim().isEmpty()){
                            INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
                            String fullLineRefName = references.item(j).getTextContent().trim();
                            TaxonNameBase nameTBF = parser.parseFullName(fullLineRefName, nomenclaturalCode, Rank.UNKNOWN_RANK());
                            if (nameTBF.hasProblem() &&
                                    !(nameTBF.getParsingProblems().size()==1 && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                                nameTBF=solveNameProblem(fullLineRefName, fullLineRefName,parser);
                            }
                            nametosave.add(nameTBF);
                            Synonym synonym = Synonym.NewInstance(nameTBF, refMods);

                            acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                        }
                    }

                    if(!name.isEmpty()){
                        logger.info("acceptedTaxon and name: *"+acceptedTaxon.getTitleCache()+"*, *"+name+"*");
                        if (acceptedTaxon.getTitleCache().split("sec")[0].trim().equalsIgnoreCase(name.split("---")[0].trim())){

                            Reference<?> refS = ReferenceFactory.newGeneric();
                            refS.setTitleCache(refString, true);
                            //                            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
                            //                            acceptedTaxon.addDescription(td);
                            //                            acceptedTaxon.addSource(refSource);
                            //
                            //                            TextData textData = TextData.NewInstance(Feature.CITATION());
                            //
                            //                            textData.addSource(null, null, refS, null);
                            //                            td.addElement(textData);
                            //                            td.addSource(refSource);
                            //                            importer.getDescriptionService().saveOrUpdate(td);
                            acceptedTaxon.getName().setNomenclaturalReference(refS);
                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                        }
                        else{
                            Rank rank;
                            try {
                                rank = Rank.getRankByName(name.split("---")[1]);
                            } catch (Exception e) {
                                logger.warn("Rank or name problem!");
                                rank=null;
                            }
                            name = name.split("---")[0].trim() + refString;
                            String original = name;

                            INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
                            TaxonNameBase nameTBF = parser.parseFullName(name, nomenclaturalCode, rank);
                            if (nameTBF.hasProblem() &&
                                    !(nameTBF.getParsingProblems().size()==1 && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                                //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                                nameTBF=solveNameProblem(original, name,parser);

                            }
                            nametosave.add(nameTBF);
                            Synonym synonym = Synonym.NewInstance(nameTBF, refMods);

                            acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                        }
                    }
                }
            }
        }
        //        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;

    }

    /**
     * @param original
     * @param name
     * @param parser
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private TaxonNameBase<?,?> solveNameProblem(String original, String name, INonViralNameParser parser) {
        Map<String,String> ato = namesMap.get(original);
        Rank rank=Rank.UNKNOWN_RANK();

        if (ato == null){
            rank=askForRank(original, Rank.UNKNOWN_RANK(), nomenclaturalCode);
        }else{
            rank = getRank(ato);
        }
        TaxonNameBase<?,?> nameTBF = parser.parseFullName(name, nomenclaturalCode, rank);
        //                logger.info("RANK: "+rank);
        int retry=0;
        while (nameTBF.hasProblem() && retry <3 && !(nameTBF.getParsingProblems().size()==1 && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank))){
            String fullname =  getFullReference(name,nameTBF.getParsingProblems());
            if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                nameTBF = BotanicalName.NewInstance(null);
            }
            if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                nameTBF = ZoologicalName.NewInstance(null);
            }
            parser.parseReferencedName(nameTBF, fullname, rank, false);
            retry++;
        }
        if (retry == 2){
            nameTBF.setFullTitleCache(name, true);
            //                    logger.info("FULL TITLE CACHE "+name);
        }
        return nameTBF;
    }

    /**
     * @param item
     * @param tosave
     */
    @SuppressWarnings({ "rawtypes", "unused" })
    private Taxon extractNomenclature(Node nomenclatureNode,  List<TaxonNameBase> tosave, Reference<?> refMods, IdentifiableSource modsSource) {
        //        logger.info("extractNomenclature");
        NodeList children = nomenclatureNode.getChildNodes();
        String freetext;
        TaxonNameBase nameToBeFilled = null;
        Taxon acceptedTaxon = null;
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();


        String fullContent = nomenclatureNode.getTextContent();
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("#text")) {
                freetext=children.item(i).getTextContent();
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:name")){
                String[]names = extractScientificName(children.item(i),fullContent);
                treatmentMainName = names[1];
                originalTreatmentName = names[0];
                Rank rank;
                try {
                    rank = Rank.getRankByName(names[2]);
                } catch (UnknownCdmTypeException e) {
                    logger.warn("Rank problem!");
                    rank=null;
                }

                if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                    nameToBeFilled = BotanicalName.NewInstance(null);
                }
                if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                    nameToBeFilled = ZoologicalName.NewInstance(null);
                }

                acceptedTaxon = importer.getTaxonService().findBestMatchingTaxon(treatmentMainName);
                if (acceptedTaxon ==null   || (acceptedTaxon!=null && acceptedTaxon.getSec() != refMods)){
                    nameToBeFilled = parser.parseFullName(treatmentMainName, nomenclaturalCode, null);
                    if (nameToBeFilled.hasProblem() &&
                            !(nameToBeFilled.getParsingProblems().size()==1 && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                        nameToBeFilled = solveNameProblem(originalTreatmentName,treatmentMainName,parser);
                    }
                    if (!originalTreatmentName.isEmpty()) {
                        TaxonNameDescription td = TaxonNameDescription.NewInstance();
                        td.setTitleCache(originalTreatmentName);
                        nameToBeFilled.addDescription(td);
                    }
                    nameToBeFilled.addSource(modsSource);
                    tosave.add(nameToBeFilled);
                    acceptedTaxon= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
                    if (acceptedTaxon.getSec() == null) {
                        acceptedTaxon.setSec(refMods);
                    }
                    acceptedTaxon.addSource(modsSource);
                    importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                    Taxon parentTaxon = askParent(acceptedTaxon, classification);
                    if (parentTaxon ==null){
                        while (parentTaxon == null) {
                            parentTaxon = createParent(acceptedTaxon, refMods);
                            classification.addParentChild(parentTaxon, acceptedTaxon, refMods, null);
                        }
                    }else{
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
     * @param acceptedTaxon
     * @return
     */
    private Taxon createParent(Taxon acceptedTaxon, Reference<?> ref) {
        JFrame frame = new JFrame("I have a question");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        List<Rank> rankList = new ArrayList<Rank>();
        rankList = importer.getTermService().listByTermClass(Rank.class, null, null, null, null);

        List<String> rankListStr = new ArrayList<String>();
        for (Rank r:rankList) {
            rankListStr.add(r.toString());
        }
        String r="";
        String s = acceptedTaxon.getTitleCache();
        Taxon tax = null;

        int addTaxon = JOptionPane.showConfirmDialog(frame, "If you want to add a parent taxa for "+s+", click \"Yes\". If it is a root for this classification, click \"No\" or \"Cancel\".");
        //        logger.info("ADD TAXON: "+addTaxon);
        if (addTaxon == 0){
            Taxon tmp = askParent(acceptedTaxon, classification);
            if (tmp == null){
                s = (String)JOptionPane.showInputDialog(
                        frame,
                        "What is the first taxon parent for "+s+"?",
                        "The rank will be asked later. ",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        null);


                r = (String)JOptionPane.showInputDialog(
                        frame,
                        "1What is the rank for "+s+"?",
                        "",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        rankListStr.toArray(),
                        null);

                NonViralName<?> nameToBeFilled = null;
                if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                    nameToBeFilled = BotanicalName.NewInstance(null);
                }
                if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                    nameToBeFilled = ZoologicalName.NewInstance(null);
                }
                nameToBeFilled.setTitleCache(s, true);
                nameToBeFilled.setRank(getRank(r));

                tax = Taxon.NewInstance(nameToBeFilled, ref);
            }
            else{
                tax=tmp;
            }

            createParent(tax, ref);
            //            logger.info("add parent child "+tax.getTitleCache()+", "+acceptedTaxon.getTitleCache());
            classification.addParentChild(tax, acceptedTaxon, ref, null);
        }
        else{
            //            r = (String)JOptionPane.showInputDialog(
            //                    frame,
            //                    "2What is the rank for "+s+"? "+r,
            //                    "",
            //                    JOptionPane.PLAIN_MESSAGE,
            //                    null,
            //                    rankList.toArray(),
            //                    null);

            //            logger.info("add child taxon "+acceptedTaxon);
            classification.addChildTaxon(acceptedTaxon, ref, null, null);
            tax=acceptedTaxon;
        }


        //        logger.info("RETURN : "+tax );
        return tax;

    }



    /**
     * @param item
     * @param tosave
     */
    private String[] extractScientificName(Node name, String fullContent) {
        //        logger.info("extractScientificName");
        Rank rank = Rank.UNKNOWN_RANK();
        NodeList children = name.getChildNodes();
        String fullName = "";
        String newName="";
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        List<String> atomisedName= new ArrayList<String>();

        String rankStr = "";
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:xmldata")){
                NodeList atom = children.item(i).getChildNodes();
                for (int k=0;k<atom.getLength();k++){
                    rankStr = atom.item(k).getNodeName().toLowerCase();
                    //                    logger.info("RANKSTR:*"+rankStr+"*");
                    if (rankStr.equalsIgnoreCase("dwc:taxonRank")) {
                        rankStr=atom.item(k).getTextContent().trim();
                    }
                    Rank tmpRank = null;
                    if (!rankStr.equalsIgnoreCase("#text")) {
                        tmpRank = getRank(rankStr);
                    }
                    if (tmpRank != null && (tmpRank.isLower(rank) || rank.equals(Rank.UNKNOWN_RANK()))) {
                        rank=tmpRank;
                    }

                    atomisedMap.put(rankStr,atom.item(k).getTextContent().trim());
                    atomisedName.add(atom.item(k).getTextContent().trim());
                }
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("#text") && !StringUtils.isBlank(children.item(i).getTextContent())){
                //                logger.info("name non atomised: "+children.item(i).getTextContent());
                fullName = children.item(i).getTextContent().trim();
                //                logger.info("fullname: "+fullName);
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
            //            logger.info("while");
        }

        namesMap.put(fullName,atomisedMap);
        String atomisedNameStr = StringUtils.join(atomisedName," ");
        while(atomisedNameStr.contains("  ")) {
            atomisedNameStr=atomisedNameStr.replace("  ", " ");
            //            logger.info("atomisedNameStr: "+atomisedNameStr);
        }
        atomisedNameStr=atomisedNameStr.trim();

        if (fullName != null){
            if (!fullName.equalsIgnoreCase(atomisedNameStr)) {
                newName=getScientificName(fullName,atomisedNameStr,classification.getTitleCache(),fullContent);
            } else {
                newName=fullName;
            }
        }
        rank = askForRank(newName, rank, nomenclaturalCode);
        String[] names = new String[3];
        names[0]=fullName;
        names[1]=newName;
        names[2]=rank.toString();
        return names;

    }

    /**
     * @param classification2
     */
    public void updateClassification(Classification classification2) {
        this.classification = classification2;

    }


}
