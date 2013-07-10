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

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
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
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;

/**
 * @author pkelbert
 * @date 2 avr. 2013
 *
 */
/**
 * @author pkelbert
 * @date 17 juin 2013
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

    private boolean maxRankRespected =false;

    /**
     * @param nomenclaturalCode
     * @param classification
     * @param importer
     * @param configState
     */
    public TaxonXTreatmentExtractor(NomenclaturalCode nomenclaturalCode, Classification classification, TaxonXImport importer,
            TaxonXImportState configState) {
        this.nomenclaturalCode=nomenclaturalCode;
        this.classification = classification;
        this.importer=importer;
        this.configState=configState;
        prepareCollectors(configState, importer.getAgentService());
    }

    /**
     * extracts all the treament information and save them
     * @param treatmentnode: the XML Node
     * @param tosave: the list of object to save into the CDM
     * @param refMods: the reference extracted from the MODS
     * @param sourceName: the URI of the document
     */
    @SuppressWarnings({ "rawtypes", "unused" })
    protected void extractTreatment(Node treatmentnode, List<Object> tosave, Reference<?> refMods, URI sourceName) {
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
                    //extract "main" the scientific name
                    acceptedTaxon = extractNomenclature(children.item(i),nametosave,refMods);
                }
            }
            else if (children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group") && maxRankRespected){
                reloadClassification();
                //extract the References within the document
                extractReferences(children.item(i),nametosave,acceptedTaxon,refMods);
            }
            else if (children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("multiple") && maxRankRespected){
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
                String multiple = askMultiple(children.item(i));
                if (multiple.equalsIgnoreCase("synonyms")) {
                    extractSynonyms(children.item(i),nametosave, acceptedTaxon,refMods);
                }
                else
                    if(multiple.equalsIgnoreCase("material examined")){
                        extractMaterials(children.item(i),acceptedTaxon, refMods, nametosave);
                    }
                    else
                        if (multiple.equalsIgnoreCase("distribution")){
                            extractDistribution(children.item(i),acceptedTaxon,defaultTaxon,refMods);
                        }
                        else {
                            extractSpecificFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,multiple);
                        }
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("biology_ecology") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon, nametosave, refMods, Feature.BIOLOGY_ECOLOGY());
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("description") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods, Feature.DESCRIPTION());
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("diagnosis") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,Feature.DIAGNOSIS());
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("discussion") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods, Feature.DISCUSSION());
            }

            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("distribution") && maxRankRespected){
                extractDistribution(children.item(i),acceptedTaxon,defaultTaxon,refMods);
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("etymology") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave,refMods,Feature.ETYMOLOGY());
            }

            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("materials_examined") && maxRankRespected){
                extractMaterials(children.item(i),acceptedTaxon, refMods, nametosave);
            }

            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("key") && maxRankRespected){
                //TODO IGNORE keys for the moment
                //extractKey(children.item(i),acceptedTaxon, nametosave,source, refMods);
                extractSpecificFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,"Keys - unparsed");
            }
            else{
                logger.info("ANOTHER KIND OF NODES: "+children.item(i).getNodeName()+", "+children.item(i).getAttributes());
                if (children.item(i).getAttributes() !=null) {
                    logger.info(children.item(i).getAttributes().item(0));
                }
            }
        }
        //        logger.info("saveUpdateNames");
        if (maxRankRespected){
            importer.getNameService().saveOrUpdate(nametosave);
            importer.getClassificationService().saveOrUpdate(classification);
            logger.info("saveUpdateNames-ok");
        }
    }


    /**
     * @param keys
     * @param acceptedTaxon: the current acceptedTaxon
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     */
    @SuppressWarnings("rawtypes")
    private void extractKey(Node keys, Taxon acceptedTaxon,List<TaxonNameBase> nametosave, Reference<?> refMods) {
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        NodeList children = keys.getChildNodes();
        String key="";
        PolytomousKey poly =  PolytomousKey.NewInstance();
        poly.addSource(null,null,refMods,null);
        poly.addTaxonomicScope(acceptedTaxon);
        poly.setTitleCache("bloup");
        //        poly.addCoveredTaxon(acceptedTaxon);
        PolytomousKeyNode root = poly.getRoot();
        PolytomousKeyNode previous = null,tmpKey=null;
        Taxon taxonKey=null;
        List<PolytomousKeyNode> polyNodes = new ArrayList<PolytomousKeyNode>();

        //        String fullContent = keys.getTextContent();
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList paragraph = children.item(i).getChildNodes();
                key="";
                taxonKey=null;
                for (int j=0;j<paragraph.getLength();j++){
                    if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text")){
                        if (! paragraph.item(j).getTextContent().trim().isEmpty()){
                            key+=paragraph.item(j).getTextContent().trim();
                            //                            logger.info("KEY: "+j+"--"+key);
                        }
                    }
                    if(paragraph.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        taxonKey=getTaxonFromXML(paragraph.item(j),nametosave,refMods);
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
     * @param taxons: the XML Nodegroup
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current accepted Taxon
     * @param refMods: the current reference extracted from the MODS
     *
     * @return Taxon object built
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Taxon getTaxonFromXML(Node taxons, List<TaxonNameBase> nametosave, Reference<?> refMods) {
        //        logger.info("getTaxonFromXML");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);

        TaxonNameBase nameToBeFilled = null;
        String name="";

        String[] enames = null;
        Rank rank = Rank.UNKNOWN_RANK();
        String original="";
        String identifier="";

        try {
            enames = extractScientificName(taxons);
            if (enames[1].isEmpty()) {
                name=enames[0];
            } else {
                name=enames[1];
            }
            original=enames[0];
            rank = Rank.getRankByName(enames[2]);
            identifier = enames[3];
        } catch (TransformerFactoryConfigurationError e1) {
            logger.warn(e1);
        } catch (TransformerException e1) {
            logger.warn(e1);
        } catch (UnknownCdmTypeException e) {
            logger.warn("Rank problem!"+enames[2]);
            rank=Rank.UNKNOWN_RANK();
        }
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();

        nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);
        if (nameToBeFilled.hasProblem() &&
                !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
            //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
            nameToBeFilled=solveNameProblem(original, name,parser);
        }

        nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave);

        //        importer.getNameService().saveOrUpdate(nametosave);
        Taxon t = importer.getTaxonService().findBestMatchingTaxon(nameToBeFilled.getTitleCache());
        if (t ==null){
            //            logger.info("BestTaxonService not the best or null");
            t= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
            if (t.getSec() == null) {
                t.setSec(refMods);
            }
            if(!configState.getConfig().doKeepOriginalSecundum()) {
                t.setSec(configState.getConfig().getSecundum());
                logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
            }
            t.addSource(null,null,refMods,null);

            if (!identifier.isEmpty() && identifier.length()>2){
                setLSID(identifier, t);
            }

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
        else{
            t = CdmBase.deproxy(t, Taxon.class);
        }
        if (!configState.getConfig().doKeepOriginalSecundum()) {
            t.setSec(configState.getConfig().getSecundum());
            logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
        }
        return t;
    }

    @SuppressWarnings("rawtypes")
    private TaxonNameBase getTaxonNameBase (TaxonNameBase name, List<TaxonNameBase> nametosave){
        List<TaxonNameBase> names = importer.getNameService().list(TaxonNameBase.class, null, null, null, null);
        for (TaxonNameBase tb : names){
            if (tb.getTitleCache().equalsIgnoreCase(name.getTitleCache())) {
                logger.info("TaxonNameBase FOUND"+name.getTitleCache());
                return tb;
            }
        }
        logger.info("TaxonNameBase NOT FOUND "+name.getTitleCache());
        nametosave.add(name);
        return name;

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
     * Create a Taxon for the current NameBase, based on the current reference
     * @param taxonNameBase
     * @param refMods: the current reference extracted from the MODS
     * @return Taxon
     */
    @SuppressWarnings({ "unused", "rawtypes" })
    private Taxon getTaxon(TaxonNameBase taxonNameBase, Reference<?> refMods) {
        Taxon t = new Taxon(taxonNameBase,null );
        if (!configState.getConfig().doKeepOriginalSecundum() || (t.getSec() == null)) {
            t.setSec(configState.getConfig().getSecundum());
            logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
        }
        t.addSource(null,null,refMods,null);
        return t;
    }

    /**
     * @param distribution: the XML node group
     * @param acceptedTaxon: the current accepted Taxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param refMods: the current reference extracted from the MODS
     */
    @SuppressWarnings("rawtypes")
    private void extractDistribution(Node distribution, Taxon acceptedTaxon, Taxon defaultTaxon, Reference<?> refMods) {
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
                        setParticularDescription(descriptionsFulltext.get(k),acceptedTaxon,defaultTaxon, refMods, Feature.HABITAT());
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
                    td.addSource(null,null,refMods,null);
                    acceptedTaxon.addDescription(td);
                    importer.getDescriptionService().saveOrUpdate(td);
                    importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                }



            }
        }
    }


    /**
     * @param materials: the XML node group
     * @param acceptedTaxon: the current accepted Taxon
     * @param refMods: the current reference extracted from the MODS
     */
    @SuppressWarnings("rawtypes")
    private void extractMaterials(Node materials, Taxon acceptedTaxon, Reference<?> refMods,List<TaxonNameBase> nametosave) {
        //        logger.info("EXTRACTMATERIALS");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        NodeList children = materials.getChildNodes();
        NodeList events = null;
        String descr="";

        DerivedUnitBase derivedUnitBase=null;
        MySpecimenOrObservation myspecimenOrObservation = null;

        for (int i=0;i<children.getLength();i++){
            String rawAssociation="";
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                events = children.item(i).getChildNodes();
                for(int k=0;k<events.getLength();k++){

                    if(events.item(k).getNodeName().equalsIgnoreCase("tax:collection_event")){
                        myspecimenOrObservation = extractSpecimenOrObservation(events.item(k),derivedUnitBase);
                        derivedUnitBase = myspecimenOrObservation.getDerivedUnitBase();
                        descr=myspecimenOrObservation.getDescr();

                        derivedUnitBase.addSource(null,null,refMods,null);

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
                        indAssociation.addSource(null, null, refMods, null);

                        taxonDescription.addElement(indAssociation);
                        taxonDescription.setTaxon(acceptedTaxon);
                        taxonDescription.addSource(null,null,refMods,null);

                        importer.getDescriptionService().saveOrUpdate(taxonDescription);
                        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                    }
                    else{

                        if (events.item(k).getNodeName().equalsIgnoreCase("tax:name")){
                           Taxon linkedTaxon = getTaxonFromXML(events.item(k), nametosave,refMods);//TODO NOT IMPLEMENTED IN THE CDM YET
                           rawAssociation+=linkedTaxon.getTitleCache();
                        } else {
                            rawAssociation+= events.item(k).getTextContent().trim();
                        }
                        if (rawAssociation.length()>1){
                            DerivedUnitFacade derivedUnitFacade = getFacade(rawAssociation);
                            derivedUnitBase = derivedUnitFacade.innerDerivedUnit();
                            derivedUnitBase.addSource(null,null,refMods,null);
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
                            indAssociation.addSource(null, null, refMods, null);

                            taxonDescription.addElement(indAssociation);
                            taxonDescription.setTaxon(acceptedTaxon);
                            taxonDescription.addSource(null,null,refMods,null);

                            importer.getDescriptionService().saveOrUpdate(taxonDescription);
                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param materials: the XML node group
     * @param acceptedTaxon: the current accepted Taxon
     * @param refMods: the current reference extracted from the MODS
     */
    @SuppressWarnings("rawtypes")
    private void extractMaterialsDirect(Node materials, Taxon acceptedTaxon, Reference<?> refMods) {
        //        logger.info("EXTRACTMATERIALS");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        String descr="";

        DerivedUnitBase derivedUnitBase=null;
        MySpecimenOrObservation myspecimenOrObservation = null;

        myspecimenOrObservation = extractSpecimenOrObservation(materials,derivedUnitBase);
        derivedUnitBase = myspecimenOrObservation.getDerivedUnitBase();
        descr=myspecimenOrObservation.getDescr();

        derivedUnitBase.addSource(null,null,refMods,null);

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
        indAssociation.addSource(null, null, refMods, null);

        taxonDescription.addElement(indAssociation);
        taxonDescription.setTaxon(acceptedTaxon);
        taxonDescription.addSource(null,null,refMods,null);

        importer.getDescriptionService().saveOrUpdate(taxonDescription);
        importer.getTaxonService().saveOrUpdate(acceptedTaxon);

    }


    /**
     * @param description: the XML node group
     * @param acceptedTaxon: the current acceptedTaxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @param featureName: the feature name
     */
    private void extractSpecificFeature(Node description, Taxon acceptedTaxon, Taxon defaultTaxon,
            List<TaxonNameBase> nametosave, Reference<?> refMods, String featureName ) {
        NodeList children = description.getChildNodes();
        NodeList insideNodes ;
        String descr ="";
        String localdescr="";

        //        String fullContent = description.getTextContent();
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
                        Taxon linkedTaxon = getTaxonFromXML(insideNodes.item(j), nametosave,refMods);//TODO NOT IMPLEMENTED IN THE CDM YET
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
                    setParticularDescription(StringUtils.join(blabla," "),acceptedTaxon,defaultTaxon, refMods,currentFeature);
                }
            }

        }

    }




    /**
     * @param children: the XML node group
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current acceptedTaxon
     * @param refMods: the current reference extracted from the MODS
     * @param fullContent :the parsed XML content
     * @return a list of description (text)
     */
    @SuppressWarnings("unused")
    private List<String> parseParagraph(List<TaxonNameBase> nametosave, Taxon acceptedTaxon, Reference<?> refMods, Node paragraph, Feature feature){
        List<String> fullDescription=  new ArrayList<String>();
        String localdescr;
        String descr="";
        NodeList insideNodes ;
        boolean collectionEvent = false;
        List<Node>collectionEvents = new ArrayList<Node>();

        NodeList children = paragraph.getChildNodes();

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
                        Taxon linkedTaxon = getTaxonFromXML(insideNodes.item(j), nametosave,refMods);//TODO NOT IMPLEMENTED IN THE CDM YET
                        blabla.add(linkedTaxon.getTitleCache());
                    }
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("#text")) {

                        if(!insideNodes.item(j).getTextContent().trim().isEmpty()){
                            blabla.add(insideNodes.item(j).getTextContent().trim());
                            localdescr += insideNodes.item(j).getTextContent().trim();
                        }
                    }
                    if  (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:collection_event")) {
                        collectionEvent=true;
                        collectionEvents.add(insideNodes.item(j));
                    }

                }
                if (!blabla.isEmpty()) {
                    fullDescription.add(StringUtils.join(blabla," "));
                }
            }
        }
        if (collectionEvent) {
            logger.warn("SEEMS TO BE COLLECTION EVENT INSIDE A "+feature.toString());
            for (Node coll:collectionEvents){
                extractMaterialsDirect(coll, acceptedTaxon, refMods);
            }
        }
        return fullDescription;
    }


    /**
     * @param description: the XML node group
     * @param acceptedTaxon: the current acceptedTaxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @param feature: the feature to link the data with
     */
    private void extractFeature(Node description, Taxon acceptedTaxon, Taxon defaultTaxon, List<TaxonNameBase> nametosave, Reference<?> refMods, Feature feature){
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
        List<String> fullDescription= parseParagraph( nametosave, acceptedTaxon, refMods, description,feature);

        if (!fullDescription.isEmpty()) {
            setParticularDescription(StringUtils.join(fullDescription,"<br/>"),acceptedTaxon,defaultTaxon, refMods,feature);
        }

    }


    /**
     * @param descr: the XML Nodegroup to parse
     * @param acceptedTaxon: the current acceptedTaxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param refMods: the current reference extracted from the MODS
     * @param currentFeature: the feature name
     * @return
     */
    private void setParticularDescription(String descr, Taxon acceptedTaxon, Taxon defaultTaxon, Reference<?> refMods, Feature currentFeature) {
        //        logger.info("setParticularDescription "+currentFeature);
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        TextData textData = TextData.NewInstance();
        textData.setFeature(currentFeature);

        textData.putText(Language.UNKNOWN_LANGUAGE(), descr+"<br/>");

        if(! descr.isEmpty() && (acceptedTaxon!=null)){
            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
            td.addElement(textData);
            td.addSource(null,null,refMods,null);
            acceptedTaxon.addDescription(td);
            importer.getDescriptionService().saveOrUpdate(td);
            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        }

        if(! descr.isEmpty() && (acceptedTaxon == null) && (defaultTaxon != null)){
            try{
                Taxon tmp =(Taxon) importer.getTaxonService().find(defaultTaxon.getUuid());
                if (tmp!=null) {
                    defaultTaxon=CdmBase.deproxy(tmp,Taxon.class);
                }else{
                    importer.getTaxonService().saveOrUpdate(defaultTaxon);
                }
            }catch(Exception e){
                logger.debug("TAXON EXISTS"+defaultTaxon);
            }

            TaxonDescription td =importer.getTaxonDescription(defaultTaxon, false, true);
            defaultTaxon.addDescription(td);
            td.addElement(textData);
            td.addSource(null,null,refMods,null);
            importer.getDescriptionService().saveOrUpdate(td);
            importer.getTaxonService().saveOrUpdate(defaultTaxon);
        }
    }



    /**
     * @param synonyms: the XML Nodegroup to parse
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current acceptedTaxon
     * @param refMods: the current reference extracted from the MODS
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void extractSynonyms(Node synonyms, List<TaxonNameBase> nametosave,Taxon acceptedTaxon, Reference<?> refMods) {
        //        logger.info("extractSynonyms: "+acceptedTaxon);
        Taxon ttmp = (Taxon) importer.getTaxonService().find(acceptedTaxon.getUuid());
        if (ttmp != null) {
            acceptedTaxon = CdmBase.deproxy(ttmp,Taxon.class);
        }
        else{
            acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
        }
        NodeList children = synonyms.getChildNodes();
        TaxonNameBase nameToBeFilled = null;
        List<String> names = new ArrayList<String>();

        String identifier="";

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList tmp = children.item(i).getChildNodes();
                //                String fullContent = children.item(i).getTextContent();
                for (int j=0; j< tmp.getLength();j++){
                    if(tmp.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        String[] enames;
                        try {
                            enames = extractScientificName(tmp.item(j));
                            if (enames[1].isEmpty()) {
                                names.add(enames[0]+"---"+enames[2]+"---"+enames[3]);
                            } else {
                                names.add(enames[1]+"---"+enames[2]+"---"+enames[3]);
                            }
                        } catch (TransformerFactoryConfigurationError e) {
                            logger.warn(e);
                        } catch (TransformerException e) {
                            logger.warn(e);
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
            identifier = name.split("---")[2];
            String original = name;

            INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
            nameToBeFilled = parser.parseFullName(name, nomenclaturalCode, rank);
            if (nameToBeFilled.hasProblem() &&
                    !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                nameToBeFilled = solveNameProblem(original, name, parser);
            }
            nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave);
            Synonym synonym = Synonym.NewInstance(nameToBeFilled, refMods);


            if (!identifier.isEmpty() && identifier.length()>2){
                setLSID(identifier, synonym);
            }

            acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        }

    }





    /**
     * @param refgroup: the XML nodes
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current acceptedTaxon
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @return the acceptedTaxon (why?)
     */
    @SuppressWarnings({ "null", "unused" ,"rawtypes" })
    private Taxon extractReferences(Node refgroup, List<TaxonNameBase> nametosave, Taxon acceptedTaxon, Reference<?> refMods) {
        //        logger.info("extractReferences");
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

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
                        if (ref.endsWith(";")  && ((ref.length())>1)) {
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
                        //                        logger.info("TREATMENTMAINNAME: "+treatmentMainName);
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
                            acceptedTaxon.addSource(null,null,refMods,null);
                        }else{
                            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
                            acceptedTaxon.addDescription(td);
                            acceptedTaxon.addSource(null,null,refMods,null);

                            TextData textData = TextData.NewInstance(Feature.CITATION());

                            textData.addSource(null, null, reference, null, acceptedTaxon.getName(), ref);
                            td.addElement(textData);
                            td.addSource(null,null,refMods,null);

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
                    String identifier="";
                    for (int j=0;j<references.getLength();j++){
                        //no bibref tag inside
                        logger.info("references.item(j).getNodeName()"+references.item(j).getNodeName());
                        if (references.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                            String[] enames;
                            try {
                                enames = extractScientificName(references.item(j));
                                if (enames[1].isEmpty()) {
                                    name=enames[0]+"---"+enames[2]+"---"+enames[3];
                                } else {
                                    name=enames[1]+"---"+enames[2]+"---"+enames[3];
                                }
                            } catch (TransformerFactoryConfigurationError e) {
                                logger.warn(e);
                            } catch (TransformerException e) {
                                logger.warn(e);
                            }

                            name=name.trim();
                        }
                        if (references.item(j).getNodeName().equalsIgnoreCase("#text")){
                            refString = references.item(j).getTextContent().trim();
                        }
                        if(references.item(j).getNodeName().equalsIgnoreCase("#text") && name.isEmpty() && !references.item(j).getTextContent().trim().isEmpty()){
                            identifier = name.split("---")[3];

                            INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
                            String fullLineRefName = references.item(j).getTextContent().trim();
                            TaxonNameBase nameTBF = parser.parseFullName(fullLineRefName, nomenclaturalCode, Rank.UNKNOWN_RANK());
                            if (nameTBF.hasProblem() &&
                                    !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                                nameTBF=solveNameProblem(fullLineRefName, fullLineRefName,parser);
                            }
                            nameTBF = getTaxonNameBase(nameTBF,nametosave);
                            Synonym synonym = Synonym.NewInstance(nameTBF, refMods);


                            if (!identifier.isEmpty() && identifier.length()>2){
                                setLSID(identifier, acceptedTaxon);
                            }

                            acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                        }
                    }

                    if(!name.isEmpty()){
                        logger.info("acceptedTaxon and name: *"+acceptedTaxon.getTitleCache()+"*, *"+name+"*");
                        if (acceptedTaxon.getTitleCache().split("sec")[0].trim().equalsIgnoreCase(name.split("---")[0].trim())){
                            identifier = name.split("---")[3];
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


                            if (!identifier.isEmpty() && identifier.length()>2){
                                setLSID(identifier, acceptedTaxon);

                            }

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
                            identifier = name.split("---")[3];

                            INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
                            TaxonNameBase nameTBF = parser.parseFullName(name, nomenclaturalCode, rank);
                            if (nameTBF.hasProblem() &&
                                    !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                                //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                                nameTBF=solveNameProblem(original, name,parser);
                            }
                            nameTBF = getTaxonNameBase(nameTBF,nametosave);
                            Synonym synonym = Synonym.NewInstance(nameTBF, refMods);


                            if (!identifier.isEmpty() && identifier.length()>2){
                                String id = identifier.split("__")[0];
                                String source = identifier.split("__")[1];
                                if (id.indexOf("lsid")>-1){
                                    try {
                                        LSID lsid = new LSID(id);
                                        synonym.setLsid(lsid);
                                    } catch (MalformedLSIDException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }

                                }
                                else{
                                    //TODO ADD ORIGINAL SOURCE ID
                                    IdentifiableSource os = IdentifiableSource.NewInstance();
                                    os.setIdInSource(id);
                                    Reference<?> re = ReferenceFactory.newGeneric();
                                    re.setTitle(source);
                                    os.setCitation(re);
                                    synonym.addSource(os);
                                }
                            }

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
     * @param identifier
     * @param acceptedTaxon
     */
    private void setLSID(String identifier, TaxonBase<?> taxon) {
        boolean lsidok=false;
        String id = identifier.split("__")[0];
        String source = identifier.split("__")[1];
        if (id.indexOf("lsid")>-1){
            try {
                LSID lsid = new LSID(id);
                taxon.setLsid(lsid);
                lsidok=true;
            } catch (MalformedLSIDException e) {
               logger.warn("Malformed LSID");
            }

        }
        if (id.indexOf("lsid")<0 || !lsidok){
            //ADD ORIGINAL SOURCE ID
            IdentifiableSource os = IdentifiableSource.NewInstance();
            os.setIdInSource(id);
            Reference<?> re = ReferenceFactory.newGeneric();
            re.setTitle(source);
            os.setCitation(re);
            taxon.addSource(os);
        }

    }

    /**
     * try to solve a parsing problem for a scientific name
     * @param original : the name from the OCR document
     * @param name : the tagged version
     * @param parser
     * @return the corrected TaxonNameBase
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
        while (nameTBF.hasProblem() && (retry <3) && !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank))){
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
     * @param nomenclatureNode: the XML nodes
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unused" })
    private Taxon extractNomenclature(Node nomenclatureNode,  List<TaxonNameBase> nametosave, Reference<?> refMods) {
        //        logger.info("extractNomenclature");
        NodeList children = nomenclatureNode.getChildNodes();
        String freetext;
        TaxonNameBase nameToBeFilled = null;
        Taxon acceptedTaxon = null;
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
        String identifier="";

        Rank rank = Rank.UNKNOWN_RANK();
        //        String fullContent = nomenclatureNode.getTextContent();
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("#text")) {
                freetext=children.item(i).getTextContent();
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:collection_event")) {
                System.out.println("COLLECTION EVENT INSIDE NOMENCLATURE");
                extractMaterialsDirect(children.item(i), acceptedTaxon, refMods);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:name")){
                String[] names;
                try {
                    names = extractScientificName(children.item(i));
                    treatmentMainName = names[1];
                    originalTreatmentName = names[0];
                    rank = Rank.getRankByName(names[2]);
                    identifier=names[3];

                } catch (TransformerFactoryConfigurationError e1) {
                    logger.warn(e1);
                } catch (TransformerException e1) {
                    logger.warn(e1);
                } catch (UnknownCdmTypeException e) {
                    logger.warn(e);
                }

                if (rank.equals(Rank.UNKNOWN_RANK()) || rank.isLower(configState.getConfig().getMaxRank())){
                    maxRankRespected=true;
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                        nameToBeFilled = BotanicalName.NewInstance(null);
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                        nameToBeFilled = ZoologicalName.NewInstance(null);
                    }

                    acceptedTaxon = importer.getTaxonService().findBestMatchingTaxon(treatmentMainName);
                    if (acceptedTaxon ==null ){
                        nameToBeFilled = parser.parseFullName(treatmentMainName, nomenclaturalCode, null);
                        if (nameToBeFilled.hasProblem() &&
                                !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                            nameToBeFilled = solveNameProblem(originalTreatmentName,treatmentMainName,parser);
                        }
                        nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave);
                        if (!originalTreatmentName.isEmpty()) {
                            TaxonNameDescription td = TaxonNameDescription.NewInstance();
                            td.setTitleCache(originalTreatmentName);
                            nameToBeFilled.addDescription(td);
                        }
                        nameToBeFilled.addSource(null,null,refMods,null);
                        acceptedTaxon= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
                        if(!configState.getConfig().doKeepOriginalSecundum()) {
                            acceptedTaxon.setSec(configState.getConfig().getSecundum());
                            logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
                        }


                        if (!identifier.isEmpty() && identifier.length()>2){
                            boolean lsidok=false;
                            String id = identifier.split("__")[0];
                            String source = identifier.split("__")[1];
                            if (id.indexOf("lsid")>-1){
                                try {
                                    LSID lsid = new LSID(id);
                                    acceptedTaxon.setLsid(lsid);
                                    lsidok=true;
                                } catch (MalformedLSIDException e) {
                                   logger.warn("Malformed LSID");
                                }

                            }
                            if (id.indexOf("lsid")<0 || !lsidok){
                                //TODO ADD ORIGINAL SOURCE ID
                                IdentifiableSource os = IdentifiableSource.NewInstance();
                                os.setIdInSource(id);
                                Reference<?> re = ReferenceFactory.newGeneric();
                                re.setTitle(source);
                                os.setCitation(re);
                                acceptedTaxon.addSource(os);
                            }
                        }

                        acceptedTaxon.addSource(null,null,refMods,null);
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
                        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
                        Set<IdentifiableSource> sources = acceptedTaxon.getSources();
                        boolean sourcelinked=false;
                        for (IdentifiableSource source:sources){
                            if (source.getCitation().getTitle().equalsIgnoreCase(refMods.getTitleCache())) {
                                sourcelinked=true;
                            }
                        }
                        if (!configState.getConfig().doKeepOriginalSecundum()) {
                            acceptedTaxon.setSec(configState.getConfig().getSecundum());
                            logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
                        }
                        if (!sourcelinked){
                            acceptedTaxon.addSource(null, null, refMods, null);
                        }
                        if (!sourcelinked || !configState.getConfig().doKeepOriginalSecundum()){

                            if (!identifier.isEmpty() && identifier.length()>2){
                                setLSID(identifier, acceptedTaxon);
                            }
                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                        }
                    }
                }else{
                    maxRankRespected=false;
                }
            }
        }
        //        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;
    }

    /**
     * @param acceptedTaxon: the current acceptedTaxon
     * @param ref: the current reference extracted from the MODS
     * @return the parent for the current accepted taxon
     */
    private Taxon createParent(Taxon acceptedTaxon, Reference<?> ref) {
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        List<Rank> rankList = new ArrayList<Rank>();
        rankList = importer.getTermService().listByTermClass(Rank.class, null, null, null, null);

        List<String> rankListStr = new ArrayList<String>();
        for (Rank r:rankList) {
            rankListStr.add(r.toString());
        }
        String r="";
        String s = acceptedTaxon.getTitleCache();
        Taxon tax = null;

        int addTaxon = askAddParent(s);
        //        logger.info("ADD TAXON: "+addTaxon);
        if (addTaxon == 0){
            Taxon tmp = askParent(acceptedTaxon, classification);
            if (tmp == null){
                s = askSetParent(s);
                r = askRank(s,rankListStr);

                NonViralName<?> nameToBeFilled = null;
                if (nomenclaturalCode.equals(NomenclaturalCode.ICBN)){
                    nameToBeFilled = BotanicalName.NewInstance(null);
                }
                if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                    nameToBeFilled = ZoologicalName.NewInstance(null);
                }
                nameToBeFilled.setTitleCache(s);
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
            classification.addChildTaxon(acceptedTaxon, ref, null, null);
            tax=acceptedTaxon;
        }
        //        logger.info("RETURN: "+tax );
        return tax;

    }



    /**
     * @param name
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     * @return a list of possible names
     */
    private String[] extractScientificName(Node name) throws TransformerFactoryConfigurationError, TransformerException {
        System.out.println("extractScientificName");
        Rank rank = Rank.UNKNOWN_RANK();
        NodeList children = name.getChildNodes();
        String fullName = "";
        String newName="";
        String identifier="";
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        List<String> atomisedName= new ArrayList<String>();

        String rankStr = "";
        Rank tmpRank ;
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:xmldata")){
                NodeList atom = children.item(i).getChildNodes();
                for (int k=0;k<atom.getLength();k++){
                    if (atom.item(k).getNodeName().equalsIgnoreCase("tax:xid")){
                        try{
                        identifier = atom.item(k).getAttributes().getNamedItem("identifier").getNodeValue();
                        }catch(Exception e){
                            System.out.println("pb with identifier, maybe empty");
                            }
                        try{
                            identifier+="__"+atom.item(k).getAttributes().getNamedItem("source").getNodeValue();
                        }catch(Exception e){
                            System.out.println("pb with identifier, maybe empty");
                            }
                    }
                    tmpRank = null;
                    rankStr = atom.item(k).getNodeName().toLowerCase();
                    //                    logger.info("RANKSTR:*"+rankStr+"*");
                    if (rankStr.equalsIgnoreCase("dwc:taxonRank")) {
                        rankStr=atom.item(k).getTextContent().trim();
                        tmpRank = getRank(rankStr);
                    }
                    if ((tmpRank != null) && (tmpRank.isLower(rank) || rank.equals(Rank.UNKNOWN_RANK()))) {
                        rank=tmpRank;
                    }

                    atomisedMap.put(rankStr.toLowerCase(),atom.item(k).getTextContent().trim());
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
                newName=getScientificName(fullName,atomisedNameStr,classification.getTitleCache(),name);
            } else {
                newName=fullName;
            }
        }
        rank = askForRank(newName, rank, nomenclaturalCode);
        String[] names = new String[4];
        names[0]=fullName;
        names[1]=newName;
        names[2]=rank.toString();
        names[3]=identifier;
        return names;

    }

    /**
     * @param classification2
     */
    public void updateClassification(Classification classification2) {
        this.classification = classification2;

    }


}
