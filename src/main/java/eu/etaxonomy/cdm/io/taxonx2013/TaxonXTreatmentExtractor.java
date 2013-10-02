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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
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
public class TaxonXTreatmentExtractor extends TaxonXExtractor{

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
                            extractDistribution(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods);
                        }
                        else
                            if (multiple.equalsIgnoreCase("type status")){
                                extractDescriptionWithReference(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,"TypeStatus");
                            }
                            else
                                if (multiple.equalsIgnoreCase("vernacular name")){
                                    extractDescriptionWithReference(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,Feature.COMMON_NAME().getTitleCache());

                                }
                                else{
                                    extractSpecificFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,multiple);
                                }

            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("biology_ecology") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon, nametosave, refMods, Feature.BIOLOGY_ECOLOGY());
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("vernacularName") && maxRankRespected){
                extractDescriptionWithReference(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,Feature.COMMON_NAME().getTitleCache());
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
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("note") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods, Feature.DESCRIPTION());
            }

            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("distribution") && maxRankRespected){
                extractDistribution(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods);
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("etymology") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave,refMods,Feature.ETYMOLOGY());
            }

            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("materials_examined") && maxRankRespected){
                extractMaterials(children.item(i),acceptedTaxon, refMods, nametosave);
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:figure") && maxRankRespected){
                extractSpecificFeature(children.item(i),acceptedTaxon,defaultTaxon, nametosave, refMods, "figure");
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("other") && maxRankRespected){
                extractSpecificFeature(children.item(i),acceptedTaxon,defaultTaxon, nametosave, refMods, "table");
            }

            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("key") && maxRankRespected){
                //TODO IGNORE keys for the moment
                //extractKey(children.item(i),acceptedTaxon, nametosave,source, refMods);
                extractSpecificFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,"Keys - unparsed");
            }
            else{
                if (!children.item(i).getNodeName().equalsIgnoreCase("tax:pb")){
                    logger.info("ANOTHER KIND OF NODES: "+children.item(i).getNodeName()+", "+children.item(i).getAttributes());
                    if (children.item(i).getAttributes() !=null) {
                        logger.info(children.item(i).getAttributes().item(0));
                    }
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
        poly.addSource(OriginalSourceType.Import, null,null,refMods,null);
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

        MyName myname = new MyName();
        NomenclaturalStatusType statusType = null;

        try {
            myname = extractScientificName(taxons);
            if (!myname.getStatus().isEmpty()){
                try {
                    statusType = nomStatusString2NomStatus(myname.getStatus());
                } catch (UnknownCdmTypeException e) {
                    logger.warn("Problem with status");
                }
            }

        } catch (TransformerFactoryConfigurationError e1) {
            logger.warn(e1);
        } catch (TransformerException e1) {
            logger.warn(e1);
        }
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();

        nameToBeFilled = parser.parseFullName(myname.getName(), nomenclaturalCode, myname.getRank());
        if (nameToBeFilled.hasProblem() &&
                !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
            //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
            nameToBeFilled=solveNameProblem(myname.getOriginalName(), myname.getName(),parser);
        }

        nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave,statusType);

        //        importer.getNameService().saveOrUpdate(nametosave);
        Taxon t = importer.getTaxonService().findBestMatchingTaxon(nameToBeFilled.getTitleCache());

        boolean statusMatch=false;
        if(t !=null ){
            statusMatch=compareStatus(t, statusType);
        }
        if (t ==null || (t != null && !statusMatch)){
            if(statusType != null) {
                nameToBeFilled.addStatus(NomenclaturalStatus.NewInstance(statusType));
            }
            t= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
            if (t.getSec() == null) {
                t.setSec(refMods);
            }
            if(!configState.getConfig().doKeepOriginalSecundum()) {
                t.setSec(configState.getConfig().getSecundum());
                logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
            }
            t.addSource(OriginalSourceType.Import,null,null,refMods,null);

            boolean sourceExists=false;
            Set<IdentifiableSource> sources = t.getSources();
            for (IdentifiableSource src : sources){
                String micro = src.getCitationMicroReference();
                Reference r = src.getCitation();
                if (r.equals(refMods) && micro == null) {
                    sourceExists=true;
                }
            }
            if(!sourceExists) {
                t.addSource(OriginalSourceType.Import,null,null,refMods,null);
            }

            if (!myname.getIdentifier().isEmpty() && (myname.getIdentifier().length()>2)){
                setLSID(myname.getIdentifier(), t);
            }

            Taxon parentTaxon = askParent(t, classification);
            if (parentTaxon ==null){
                while (parentTaxon == null) {
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


    /**
     * @param taxons: the XML Nodegroup
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current accepted Taxon
     * @param refMods: the current reference extracted from the MODS
     *
     * @return Taxon object built
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TaxonNameBase getTaxonNameBaseFromXML(Node taxons, List<TaxonNameBase> nametosave, Reference<?> refMods) {
        //        logger.info("getTaxonFromXML");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);

        TaxonNameBase nameToBeFilled = null;

        MyName myName=new MyName();

        NomenclaturalStatusType statusType = null;
        try {
            myName = extractScientificName(taxons);
            if (!myName.getStatus().isEmpty()){
                try {
                    statusType = nomStatusString2NomStatus(myName.getStatus());
                } catch (UnknownCdmTypeException e) {
                    logger.warn("Problem with status");
                }
            }
        } catch (TransformerFactoryConfigurationError e1) {
            logger.warn(e1);
        } catch (TransformerException e1) {
            logger.warn(e1);
        }
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();

        nameToBeFilled = parser.parseFullName(myName.getName(), nomenclaturalCode, myName.getRank());
        if (nameToBeFilled.hasProblem() &&
                !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
            //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
            nameToBeFilled=solveNameProblem(myName.getOriginalName(), myName.getName(),parser);
        }

        nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave,statusType);
        return nameToBeFilled;

    }

    @SuppressWarnings("rawtypes")
    private TaxonNameBase getTaxonNameBase (TaxonNameBase name, List<TaxonNameBase> nametosave, NomenclaturalStatusType statusType){
        List<TaxonNameBase> names = importer.getNameService().list(TaxonNameBase.class, null, null, null, null);
        for (TaxonNameBase tb : names){
            if (tb.getTitleCache().equalsIgnoreCase(name.getTitleCache())) {
                boolean statusMatch=false;
                if(tb !=null ){
                    statusMatch=compareStatus(tb, statusType);
                }
                if (!statusMatch){
                    if(statusType != null) {
                        name.addStatus(NomenclaturalStatus.NewInstance(statusType));
                    }
                }else
                {
                    logger.info("TaxonNameBase FOUND"+name.getTitleCache());
                    return tb;
                }
            }
        }
        logger.info("TaxonNameBase NOT FOUND "+name.getTitleCache());
        System.out.println("add name "+name);
        nametosave.add(name);
        return name;

    }



    /**
     * @param tb
     * @param statusType
     * @return
     */
    private boolean compareStatus(TaxonNameBase tb, NomenclaturalStatusType statusType) {
        boolean statusMatch=false;
        //found one taxon
        Set<NomenclaturalStatus> status = tb.getStatus();
        if (statusType!=null && status.size()>0){ //the statusType is known for both taxon
            for (NomenclaturalStatus st:status){
                NomenclaturalStatusType stype = st.getType();
                if (stype.toString().equalsIgnoreCase(statusType.toString())) {
                    statusMatch=true;
                }
            }
        }
        else{
            if(statusType == null && status.size()==0) {//there is no statusType, we can assume it's the same
                statusMatch=true;
            }
        }
        return statusMatch;
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
/*<<<<<<< .courant
        boolean sourceExists=false;
        Set<IdentifiableSource> sources = t.getSources();
        for (IdentifiableSource src : sources){
            String micro = src.getCitationMicroReference();
            Reference r = src.getCitation();
            if (r.equals(refMods) && micro == null) {
                sourceExists=true;
            }
        }
        if(!sourceExists) {
            t.addSource(null,null,refMods,null);
        }
=======*/
        t.addSource(OriginalSourceType.Import,null,null,refMods,null);
        return t;
    }

    @SuppressWarnings("rawtypes")
    private void  extractDescriptionWithReference(Node typestatus, Taxon acceptedTaxon, Taxon defaultTaxon, List<TaxonNameBase> nametosave,
            Reference<?> refMods, String featureName) {
        System.out.println("extractDescriptionWithReference !");
        NodeList children = typestatus.getChildNodes();

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

        String r="";String s="";
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                s+=children.item(i).getTextContent().trim();
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:bibref")){
                r+= children.item(i).getTextContent().trim();
            }
            if (s.indexOf(r)>-1) {
                s=s.split(r)[0];
            }
        }
        Reference<?> currentref =  ReferenceFactory.newGeneric();
        if(!r.isEmpty()) {
            currentref.setTitle(r);
        } else {
            currentref=refMods;
        }
        setParticularDescription(s,acceptedTaxon,defaultTaxon, currentref, refMods,currentFeature);

    }

    /**
     * @param nametosave
     * @param distribution: the XML node group
     * @param acceptedTaxon: the current accepted Taxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param refMods: the current reference extracted from the MODS
     */
    @SuppressWarnings("rawtypes")
    private void extractDistribution(Node distribution, Taxon acceptedTaxon, Taxon defaultTaxon, List<TaxonNameBase> nametosave, Reference<?> refMods) {
        //        logger.info("DISTRIBUTION");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        NodeList children = distribution.getChildNodes();
        Map<Integer,List<MySpecimenOrObservation>> specimenOrObservations = new HashMap<Integer, List<MySpecimenOrObservation>>();
        Map<Integer,String> descriptionsFulltext = new HashMap<Integer,String>();

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList paragraph = children.item(i).getChildNodes();
                for (int j=0;j<paragraph.getLength();j++){
                    if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text")){
                        if(!paragraph.item(j).getTextContent().trim().isEmpty()) {
                            String s =paragraph.item(j).getTextContent().trim();
                            if (descriptionsFulltext.get(i) !=null){
                                s = descriptionsFulltext.get(i)+" "+s;
                            }
                            descriptionsFulltext.put(i, s);
                        }
                    }
                    else if (paragraph.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        String s =getTaxonNameBaseFromXML(paragraph.item(j),nametosave,refMods).toString().split("sec.")[0];
                        if (descriptionsFulltext.get(i) !=null){
                            s = descriptionsFulltext.get(i)+" "+s;
                        }
                        descriptionsFulltext.put(i, s);
                    }
                    else if (paragraph.item(j).getNodeName().equalsIgnoreCase("tax:collection_event")){
                        MySpecimenOrObservation specimenOrObservation = new MySpecimenOrObservation();
                        DerivedUnit derivedUnitBase = null;
                        specimenOrObservation = extractSpecimenOrObservation(paragraph.item(j), derivedUnitBase, SpecimenOrObservationType.DerivedUnit);
                        List<MySpecimenOrObservation> speObsList = specimenOrObservations.get(i);
                        if (speObsList == null) {
                            speObsList=new ArrayList<MySpecimenOrObservation>();
                        }
                        speObsList.add(specimenOrObservation);
                        specimenOrObservations.put(i,speObsList);

                        String s = specimenOrObservation.getDerivedUnitBase().toString();
                        if (descriptionsFulltext.get(i) !=null){
                            s = descriptionsFulltext.get(i)+" "+s;
                        }
                        descriptionsFulltext.put(i, s);
                    }

                }
            }
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
        DerivedUnit derivedUnitBase=null;
        String descr="";
        for (int k=0;k<=m;k++){
            if(specimenOrObservations.keySet().contains(k)){
                for (MySpecimenOrObservation soo:specimenOrObservations.get(k) ) {
                    derivedUnitBase = soo.getDerivedUnitBase();
                    descr=soo.getDescr();

/*<<<<<<< .courant
                    boolean sourceExists=false;
                    Set<IdentifiableSource> sources = derivedUnitBase.getSources();
                    for (IdentifiableSource src : sources){
                        String micro = src.getCitationMicroReference();
                        Reference r = src.getCitation();
                        if (r.equals(refMods) && micro == null) {
                            sourceExists=true;
                        }
                    }
                    if(!sourceExists) {
                        derivedUnitBase.addSource(null,null,refMods,null);
                    }
=======*/
                    derivedUnitBase.addSource(OriginalSourceType.Import, null,null,refMods,null);

                    importer.getOccurrenceService().saveOrUpdate(derivedUnitBase);

                    TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);
                    acceptedTaxon.addDescription(taxonDescription);


                    IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();

                    Feature feature=null;
                    feature = makeFeature(derivedUnitBase);
                    if(!StringUtils.isEmpty(descr)) {
                        derivedUnitBase.setTitleCache(descr, true);
                    }
                    indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
                    indAssociation.setFeature(feature);
                    indAssociation.addSource(OriginalSourceType.Import, null, null, refMods, null);

                   /* sourceExists=false;
                    Set<DescriptionElementSource> dsources = indAssociation.getSources();
                    for (DescriptionElementSource src : dsources){
                        String micro = src.getCitationMicroReference();
                        Reference r = src.getCitation();
                        if (r.equals(refMods) && micro == null) {
                            sourceExists=true;
                        }
                    }
                    if(!sourceExists) {
                        indAssociation.addSource(null, null, refMods, null);
                    }
                    */
                    indAssociation.addSource(OriginalSourceType.Import, null,null,refMods,null);

                    taxonDescription.addElement(indAssociation);
                    taxonDescription.setTaxon(acceptedTaxon);
                    taxonDescription.addSource(OriginalSourceType.Import, null,null,refMods,null);

                    /*sourceExists=false;
                    sources = taxonDescription.getSources();
                    for (IdentifiableSource src : sources){
                        String micro = src.getCitationMicroReference();
                        Reference r = src.getCitation();
                        if (r.equals(refMods) && micro == null) {
                            sourceExists=true;
                        }
                    }
                    if(!sourceExists) {
                        taxonDescription.addSource(OriginalSourceType.Import,null,null,refMods,null);
                    }*/
                    importer.getDescriptionService().saveOrUpdate(taxonDescription);
                    importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                    td.setDescribedSpecimenOrObservation(soo.getDerivedUnitBase());
                }
            }

            if (descriptionsFulltext.keySet().contains(k)){
                if (!descriptionsFulltext.get(k).isEmpty() && (descriptionsFulltext.get(k).startsWith("Hab.") || descriptionsFulltext.get(k).startsWith("Habitat"))){
                    setParticularDescription(descriptionsFulltext.get(k),acceptedTaxon,defaultTaxon, refMods, Feature.HABITAT());
                    break;
                }
                else{
                    TextData textData = TextData.NewInstance();

                    textData.setFeature(currentFeature);
                    textData.putText(Language.UNKNOWN_LANGUAGE(), descriptionsFulltext.get(k));
                    textData.addSource(OriginalSourceType.Import, null, null, refMods, null);

                    td.addElement(textData);
                }
            }


            if (descriptionsFulltext.keySet().contains(k) || specimenOrObservations.keySet().contains(k)){
/*<<<<<<< .courant
                boolean sourceExists=false;
                Set<IdentifiableSource> sources = td.getSources();
                for (IdentifiableSource src : sources){
                    String micro = src.getCitationMicroReference();
                    Reference r = src.getCitation();
                    if (r.equals(refMods) && micro == null) {
                        sourceExists=true;
                    }
                }
                if(!sourceExists) {
                    td.addSource(null,null,refMods,null);
                }
=======*/
                td.addSource(OriginalSourceType.Import, null,null,refMods,null);
                acceptedTaxon.addDescription(td);
                importer.getDescriptionService().saveOrUpdate(td);
                importer.getTaxonService().saveOrUpdate(acceptedTaxon);
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

        DerivedUnit derivedUnitBase=null;
        MySpecimenOrObservation myspecimenOrObservation = null;

        for (int i=0;i<children.getLength();i++){
            String rawAssociation="";
            boolean added=false;
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                events = children.item(i).getChildNodes();
                for(int k=0;k<events.getLength();k++){
                    if (events.item(k).getNodeName().equalsIgnoreCase("tax:name")){
                        String linkedTaxon = getTaxonNameBaseFromXML(events.item(k), nametosave,refMods).toString();//TODO NOT IMPLEMENTED IN THE CDM YET
                        rawAssociation+=linkedTaxon.split("sec")[0];
                    }
                    if (! events.item(k).getNodeName().equalsIgnoreCase("tax:name")
                            && !events.item(k).getNodeName().equalsIgnoreCase("tax:collection_event")){
                        rawAssociation+= events.item(k).getTextContent().trim();
                    }
                    if(events.item(k).getNodeName().equalsIgnoreCase("tax:collection_event")){
                        if (!containsDistinctLetters(rawAssociation.replaceAll(";",""))) {
                            rawAssociation="no description text";
                        }
                        added=true;
                        DerivedUnitFacade derivedUnitFacade = getFacade(rawAssociation.replaceAll(";",""),SpecimenOrObservationType.FieldUnit);
                        derivedUnitBase = derivedUnitFacade.innerDerivedUnit();
/*<<<<<<< .courant
                        System.out.println("derivedUnitBase: "+derivedUnitBase);

                        boolean sourceExists=false;
                        Set<IdentifiableSource> sources = derivedUnitBase.getSources();
                        for (IdentifiableSource src : sources){
                            String micro = src.getCitationMicroReference();
                            Reference r = src.getCitation();
                            if (r.equals(refMods) && micro == null) {
                                sourceExists=true;
                            }
                        }
                        if(!sourceExists) {
                            derivedUnitBase.addSource(null,null,refMods,null);
                        }

=======*/
                        derivedUnitBase.addSource(OriginalSourceType.Import, null,null,refMods,null);
                        importer.getOccurrenceService().saveOrUpdate(derivedUnitBase);

                        myspecimenOrObservation = extractSpecimenOrObservation(events.item(k),derivedUnitBase,SpecimenOrObservationType.FieldUnit);
                        derivedUnitBase = myspecimenOrObservation.getDerivedUnitBase();
                        descr=myspecimenOrObservation.getDescr();

/*<<<<<<< .courant
                        sourceExists=false;
                        sources = derivedUnitBase.getSources();
                        for (IdentifiableSource src : sources){
                            String micro = src.getCitationMicroReference();
                            Reference r = src.getCitation();
                            if (r.equals(refMods) && micro == null) {
                                sourceExists=true;
                            }
                        }
                        if(!sourceExists) {
                            derivedUnitBase.addSource(null,null,refMods,null);
                        }
=======*/
                        derivedUnitBase.addSource(OriginalSourceType.Import, null,null,refMods,null);

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
                        indAssociation.addSource(OriginalSourceType.Import,null, null, refMods, null);

                        /*sourceExists=false;
                        Set<DescriptionElementSource> dsources = indAssociation.getSources();
                        for (DescriptionElementSource src : dsources){
                            String micro = src.getCitationMicroReference();
                            Reference r = src.getCitation();
                            if (r.equals(refMods) && micro == null) {
                                sourceExists=true;
                            }
                        }
                        if(!sourceExists) {
                            indAssociation.addSource(null, null, refMods, null);
                        }
                        */

                        taxonDescription.addElement(indAssociation);
                        taxonDescription.setTaxon(acceptedTaxon);
                        taxonDescription.addSource(OriginalSourceType.Import,null,null,refMods,null);

                        /*sourceExists=false;
                        sources = taxonDescription.getSources();
                        for (IdentifiableSource src : sources){
                            String micro = src.getCitationMicroReference();
                            Reference r = src.getCitation();
                            if (r.equals(refMods) && micro == null) {
                                sourceExists=true;
                            }
                        }
                        if(!sourceExists) {
                            taxonDescription.addSource(OriginalSourceType.Import,null,null,refMods,null);
                        }*/
                        importer.getDescriptionService().saveOrUpdate(taxonDescription);
                        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                    }
                    if (!rawAssociation.isEmpty() && !added){
                        DerivedUnitFacade derivedUnitFacade = getFacade(rawAssociation.replaceAll(";",""),SpecimenOrObservationType.DerivedUnit);
                        derivedUnitBase = derivedUnitFacade.innerDerivedUnit();

                        TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);
                        acceptedTaxon.addDescription(taxonDescription);

                        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();

                        Feature feature = Feature.MATERIALS_EXAMINED();
                        if(!StringUtils.isEmpty(rawAssociation)) {
                            derivedUnitBase.setTitleCache(rawAssociation, true);
                        }
                        indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
                        indAssociation.setFeature(feature);
                        indAssociation.addSource(OriginalSourceType.Import, null, null, refMods, null);

                        /*boolean sourceExists=false;
                        Set<DescriptionElementSource> dsources = indAssociation.getSources();
                        for (DescriptionElementSource src : dsources){
                            String micro = src.getCitationMicroReference();
                            Reference r = src.getCitation();
                            if (r.equals(refMods) && micro == null) {
                                sourceExists=true;
                            }
                        }
                        if(!sourceExists) {
                            indAssociation.addSource(null, null, refMods, null);
                        }*/
                        taxonDescription.addElement(indAssociation);
                        taxonDescription.setTaxon(acceptedTaxon);
                        taxonDescription.addSource(OriginalSourceType.Import, null,null,refMods,null);

                        /*sourceExists=false;
                        Set<IdentifiableSource> sources = taxonDescription.getSources();
                        for (IdentifiableSource src : sources){
                            String micro = src.getCitationMicroReference();
                            Reference r = src.getCitation();
                            if (r.equals(refMods) && micro == null) {
                                sourceExists=true;
                            }
                        }
                        if(!sourceExists) {
                            taxonDescription.addSource(OriginalSourceType.Import,null,null,refMods,null);
                        }*/

                        importer.getDescriptionService().saveOrUpdate(taxonDescription);
                        importer.getTaxonService().saveOrUpdate(acceptedTaxon);

                        rawAssociation="";
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
    private String extractMaterialsDirect(Node materials, Taxon acceptedTaxon, Reference<?> refMods, String event) {
        //        logger.info("EXTRACTMATERIALS");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        String descr="";

        DerivedUnit derivedUnitBase=null;
        MySpecimenOrObservation myspecimenOrObservation = null;


        myspecimenOrObservation = extractSpecimenOrObservation(materials,derivedUnitBase, SpecimenOrObservationType.FieldUnit);
        derivedUnitBase = myspecimenOrObservation.getDerivedUnitBase();
        descr=myspecimenOrObservation.getDescr();

/*<<<<<<< .courant
        boolean sourceExists=false;
        Set<IdentifiableSource> sources = derivedUnitBase.getSources();
        for (IdentifiableSource src : sources){
            String micro = src.getCitationMicroReference();
            Reference r = src.getCitation();
            if (r.equals(refMods) && micro == null) {
                sourceExists=true;
            }
        }
        if(!sourceExists) {
            derivedUnitBase.addSource(null,null,refMods,null);
        }
=======*/
        derivedUnitBase.addSource(OriginalSourceType.Import, null,null,refMods,null);

        importer.getOccurrenceService().saveOrUpdate(derivedUnitBase);

        TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);
        acceptedTaxon.addDescription(taxonDescription);


        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();

        Feature feature=null;
        if (event.equalsIgnoreCase("collection")){
            feature = makeFeature(derivedUnitBase);
        }
        else{
            feature = Feature.MATERIALS_EXAMINED();
        }
        if(!StringUtils.isEmpty(descr)) {
            derivedUnitBase.setTitleCache(descr);
        }
        indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
        indAssociation.setFeature(feature);
        indAssociation.addSource(OriginalSourceType.Import, null, null, refMods, null);

       /* sourceExists=false;
        Set<DescriptionElementSource> dsources = indAssociation.getSources();
        for (DescriptionElementSource src : dsources){
            String micro = src.getCitationMicroReference();
            Reference r = src.getCitation();
            if (r.equals(refMods) && micro == null) {
                sourceExists=true;
            }
        }
        if(!sourceExists) {
            indAssociation.addSource(null, null, refMods, null);
        }
*/
        taxonDescription.addElement(indAssociation);
        taxonDescription.setTaxon(acceptedTaxon);
        taxonDescription.addSource(OriginalSourceType.Import, null,null,refMods,null);

      /*  sourceExists=false;
        sources = taxonDescription.getSources();
        for (IdentifiableSource src : sources){
            String micro = src.getCitationMicroReference();
            Reference r = src.getCitation();
            if (r.equals(refMods) && micro == null) {
                sourceExists=true;
            }
        }
        if(!sourceExists) {
            taxonDescription.addSource(OriginalSourceType.Import,null,null,refMods,null);
        }
*/
        importer.getDescriptionService().saveOrUpdate(taxonDescription);
        importer.getTaxonService().saveOrUpdate(acceptedTaxon);

        return derivedUnitBase.getTitleCache();

    }


    /**
     * @param description: the XML node group
     * @param acceptedTaxon: the current acceptedTaxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @param featureName: the feature name
     */
    private String extractSpecificFeature(Node description, Taxon acceptedTaxon, Taxon defaultTaxon,
            List<TaxonNameBase> nametosave, Reference<?> refMods, String featureName ) {
//        System.out.println("GRUUUUuu");
        NodeList children = description.getChildNodes();
        NodeList insideNodes ;
        NodeList trNodes;
        NodeList tdNodes;
        String descr ="";
        String localdescr="";
        List<String> blabla=null;
        List<String> text = new ArrayList<String>();

        String table="<table>";
        String head="";
        String line="";

        //        String fullContent = description.getTextContent();
        for (int i=0;i<children.getLength();i++){
            localdescr="";
            if (children.item(i).getNodeName().equalsIgnoreCase("#text") && !children.item(i).getTextContent().trim().isEmpty()){
                descr += children.item(i).getTextContent().trim();
            }
            //            if (children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
            //                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("other") &&
            //                    children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("table")){
            if (featureName.equalsIgnoreCase("table")){
                System.out.println("children.item(i).name: "+i+"-- "+children.item(i).getNodeName());
                if (children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                        children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("thead")){
                    head="<th>";
                    trNodes = children.item(i).getChildNodes();
                    for (int k=0;k<trNodes.getLength();k++){
                        System.out.println("NB ELEMENTS "+k +"("+trNodes.getLength()+")");
                        if (trNodes.item(k).getNodeName().equalsIgnoreCase("tax:div")
                                && trNodes.item(k).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("tr")){

                            System.out.println("hop");
                            line="<tr>";
                            tdNodes=trNodes.item(k).getChildNodes();
                            for (int l=0;l<tdNodes.getLength();l++){
                                if (tdNodes.item(l).getNodeName().equalsIgnoreCase("tax:p")){
                                    line+="<td>"+tdNodes.item(l).getTextContent()+"</td>";
                                }
                            }
                            line+="</tr>";
                            head+=line;
                        }
                    }
                    head+="</th>";
                    table+=head;
                    //                    }
                    line="<tr>";
                    if (children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("tr")){
                        line="<tr>";
                        tdNodes=children.item(i).getChildNodes();
                        for (int l=0;l<tdNodes.getLength();l++){
                            if (tdNodes.item(l).getNodeName().equalsIgnoreCase("tax:p")){
                                line+="<td>"+tdNodes.item(l).getTextContent()+"</td>";
                            }
                        }
                    }
                    line+="</tr>";
                    if (!line.equalsIgnoreCase("<tr></tr>")) {
                        table+=line;
                    }
                }
                if (children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                        children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("tr")){
                    line="<tr>";
                    trNodes = children.item(i).getChildNodes();
                    for (int k=0;k<trNodes.getLength();k++){
                        if (trNodes.item(k).getNodeName().equalsIgnoreCase("tax:p")){
                            line+="<td>"+trNodes.item(k).getTextContent()+"</td>";
                        }
                    }
                    line+="</tr>";
                    if(!line.equalsIgnoreCase("<tr></tr>")) {
                        table+=line;
                    }
                }
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                insideNodes=children.item(i).getChildNodes();
                blabla= new ArrayList<String>();
                for (int j=0;j<insideNodes.getLength();j++){
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        String linkedTaxon = getTaxonNameBaseFromXML(insideNodes.item(j), nametosave,refMods).toString();//TODO NOT IMPLEMENTED IN THE CDM YET
                        blabla.add(linkedTaxon.split("sec")[0]);
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
                text.add(StringUtils.join(blabla," "));
            }
        }

        table+="</table>";
        if (!table.equalsIgnoreCase("<table></table>")){
            System.out.println("TABLE : "+table);
            text.add(table);
        }

        if (text !=null && !text.isEmpty()) {
            return StringUtils.join(text," ");
        } else {
            return "";
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
        //        String localdescr;
        String descr="";
        NodeList insideNodes ;
        boolean collectionEvent = false;
        List<Node>collectionEvents = new ArrayList<Node>();

        NodeList children = paragraph.getChildNodes();

        for (int i=0;i<children.getLength();i++){
            //            localdescr="";
            if (children.item(i).getNodeName().equalsIgnoreCase("#text") && !children.item(i).getTextContent().trim().isEmpty()){
                descr += children.item(i).getTextContent().trim();
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                insideNodes=children.item(i).getChildNodes();
                List<String> blabla= new ArrayList<String>();
                for (int j=0;j<insideNodes.getLength();j++){
                    boolean nodeKnown = false;
                    System.out.println("insideNodes.item(j).getNodeName() : "+insideNodes.item(j).getNodeName());
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        String linkedTaxon = getTaxonNameBaseFromXML(insideNodes.item(j), nametosave,refMods).toString();//TODO NOT IMPLEMENTED IN THE CDM YET
                        blabla.add(linkedTaxon.split("sec")[0]);
                        nodeKnown=true;
                    }
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("#text")) {
                        if(!insideNodes.item(j).getTextContent().trim().isEmpty()){
                            blabla.add(insideNodes.item(j).getTextContent().trim());
                            //                            localdescr += insideNodes.item(j).getTextContent().trim();
                        }
                        nodeKnown=true;
                    }
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:bibref")) {
                        String ref = insideNodes.item(j).getTextContent().trim();
                        if (ref.endsWith(";")  && ((ref.length())>1)) {
                            ref=ref.substring(0, ref.length()-1)+".";
                        }
                        Reference<?> reference = ReferenceFactory.newGeneric();
                        reference.setTitleCache(ref, true);
                        blabla.add(reference.getTitleCache());
                        nodeKnown=true;
                    }
                    if  (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:figure")){
                        System.out.println("OUHOU");
                        String figure = extractSpecificFeature(insideNodes.item(j),acceptedTaxon,acceptedTaxon, nametosave, refMods, "figure");
                        blabla.add(figure);
                    }
                    if(insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:div") &&
                            insideNodes.item(j).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("other") &&
                            insideNodes.item(j).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("table")){
                        System.out.println("OUI?");
                        String table = extractSpecificFeature(insideNodes.item(j),acceptedTaxon,acceptedTaxon, nametosave, refMods, "table");
                        blabla.add(table);
                    }
                    if  (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:collection_event")) {
                        logger.warn("SEEMS TO BE COLLECTION EVENT INSIDE A "+feature.toString());
                        String titlecache  = extractMaterialsDirect(insideNodes.item(j), acceptedTaxon, refMods, "collection");
                        blabla.add(titlecache);
                        collectionEvent=true;
                        collectionEvents.add(insideNodes.item(j));
                        nodeKnown=true;
                    }
                    if (!nodeKnown && !insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:pb")) {
                        logger.info("Node not handled yet : "+insideNodes.item(j).getNodeName());
                    }

                }
                if (!blabla.isEmpty()) {
                    fullDescription.add(StringUtils.join(blabla," "));
                }
            }
            if  (children.item(i).getNodeName().equalsIgnoreCase("tax:figure")){
                String figure = extractSpecificFeature(children.item(i),acceptedTaxon,acceptedTaxon, nametosave, refMods, "figure");
                fullDescription.add(figure);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("other") &&
                    children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("table")){
                String table = extractSpecificFeature(children.item(i),acceptedTaxon,acceptedTaxon, nametosave, refMods, "table");
                fullDescription.add(table);
            }
        }
        //        if (collectionEvent) {
        //            logger.warn("SEEMS TO BE COLLECTION EVENT INSIDE A "+feature.toString());
        //            for (Node coll:collectionEvents){
        //                = extractMaterialsDirect(coll, acceptedTaxon, refMods, "collection");
        //            }
        //        }
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
        textData.addSource(OriginalSourceType.Import, null,null,refMods,null);

        textData.putText(Language.UNKNOWN_LANGUAGE(), descr+"<br/>");

        if(! descr.isEmpty() && (acceptedTaxon!=null)){
            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
            td.addElement(textData);
            td.addSource(OriginalSourceType.Import,null,null,refMods,null);
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
            td.addSource(OriginalSourceType.Import,null,null,refMods,null);
            importer.getDescriptionService().saveOrUpdate(td);
            importer.getTaxonService().saveOrUpdate(defaultTaxon);
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
    private void setParticularDescription(String descr, Taxon acceptedTaxon, Taxon defaultTaxon,Reference<?> currentRef, Reference<?> refMods, Feature currentFeature) {
        System.out.println("setParticularDescriptionSPecial "+currentFeature);
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        TextData textData = TextData.NewInstance();
        textData.setFeature(currentFeature);
        textData.addSource(OriginalSourceType.Import,null,null,refMods,null);

        textData.putText(Language.UNKNOWN_LANGUAGE(), descr+"<br/>");

        if(! descr.isEmpty() && (acceptedTaxon!=null)){
            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
            td.addElement(textData);
            td.addSource(OriginalSourceType.Import,null,null,refMods,null);
            if(currentRef != refMods) {
                td.addSource(OriginalSourceType.Import,null,null,currentRef,null);
            }
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
            if(currentRef != refMods) {
                td.addSource(OriginalSourceType.Import,null,null,refMods,null);
            }
            td.addSource(OriginalSourceType.Import,null,null,currentRef,null);
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
        List<MyName> names = new ArrayList<MyName>();

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList tmp = children.item(i).getChildNodes();
                //                String fullContent = children.item(i).getTextContent();
                for (int j=0; j< tmp.getLength();j++){
                    if(tmp.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        MyName myName;
                        try {
                            myName = extractScientificName(tmp.item(j));
                                names.add(myName);
                        } catch (TransformerFactoryConfigurationError e) {
                            logger.warn(e);
                        } catch (TransformerException e) {
                            logger.warn(e);
                        }

                    }
                }
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:name")){
                MyName myName;
                try {
                    myName = extractScientificName(children.item(i));
                    names.add(myName);
                } catch (TransformerFactoryConfigurationError e) {
                    logger.warn(e);
                } catch (TransformerException e) {
                    logger.warn(e);
                }

            }
        }
        NomenclaturalStatusType statusType = null;

        for(MyName name:names){
            System.out.println("HANDLE NAME "+name);

            statusType = null;

            if (!name.getStatus().isEmpty()){
                try {
                    statusType = nomStatusString2NomStatus(name.getStatus());
                } catch (UnknownCdmTypeException e) {
                    logger.warn("Problem with status");
                }
            }

            INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
            nameToBeFilled = parser.parseFullName(name.getName(), nomenclaturalCode, name.getRank());
            if (nameToBeFilled.hasProblem() &&
                    !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                nameToBeFilled = solveNameProblem(name.getOriginalName(), name.getName(), parser);
            }
            nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave,statusType);
            Synonym synonym = Synonym.NewInstance(nameToBeFilled, refMods);


            if (!name.getIdentifier().isEmpty() && (name.getIdentifier().length()>2)){
                setLSID(name.getIdentifier(), synonym);
            }

            acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
            System.out.println("SYNONYM");

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
     * handle cases where the bibref are inside <p> and outside
     */
    @SuppressWarnings({ "null", "unused" ,"rawtypes" })
    private Taxon extractReferences(Node refgroup, List<TaxonNameBase> nametosave, Taxon acceptedTaxon, Reference<?> refMods) {
        //        logger.info("extractReferences");
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        NodeList children = refgroup.getChildNodes();
        NonViralName<?> nameToBeFilled = null;
        if (nomenclaturalCode.equals(NomenclaturalCode.ICNCP)){
            nameToBeFilled = BotanicalName.NewInstance(null);
        }
        if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
            nameToBeFilled = ZoologicalName.NewInstance(null);
        }
        if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)){
            nameToBeFilled = BacterialName.NewInstance(null);
        }

        ReferenceBuilder refBuild = new ReferenceBuilder();
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:bibref")){
                String ref = children.item(i).getTextContent().trim();
                refBuild.builReference(ref, treatmentMainName, nomenclaturalCode,  acceptedTaxon, refMods);
                if (!refBuild.isFoundBibref()){
                    extractReferenceRawText(children.item(i).getChildNodes(), nameToBeFilled, nametosave, refMods,acceptedTaxon);
                }
            }

            if(children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList references = children.item(i).getChildNodes();
                for (int j=0;j<references.getLength();j++){
                    if(references.item(j).getNodeName().equalsIgnoreCase("tax:bibref")){
                        String ref = references.item(j).getTextContent().trim();
                        refBuild.builReference(ref, treatmentMainName,  nomenclaturalCode,  acceptedTaxon, refMods);
                    }
                }
                if (!refBuild.isFoundBibref()){
                    extractReferenceRawText(references, nameToBeFilled, nametosave, refMods, acceptedTaxon);
                }
            }
        }
        //        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;

    }

    /**
     * @param references
     * handle cases where the bibref are inside <p> and outside
     */
    @SuppressWarnings("rawtypes")
    private void extractReferenceRawText(NodeList references, NonViralName<?> nameToBeFilled, List<TaxonNameBase> nametosave,
            Reference<?> refMods, Taxon acceptedTaxon) {
        String refString="";
        NomenclaturalStatusType statusType = null;

        for (int j=0;j<references.getLength();j++){
            //no bibref tag inside
            MyName myName = new MyName();
            logger.info("references.item(j).getNodeName()"+references.item(j).getNodeName());
            if (references.item(j).getNodeName().equalsIgnoreCase("tax:name")){

                try {
                    myName = extractScientificName(references.item(j));
//                    if (myName.getNewName().isEmpty()) {
//                        name=myName.getOriginalName()+"---"+myName.getRank()+"---"+myName.getIdentifier()+"---"+myName.getStatus();
//                    } else {
//                        name=myName.getNewName()+"---"+myName.getRank()+"---"+myName.getIdentifier()+"---"+myName.getStatus();
//                    }
                } catch (TransformerFactoryConfigurationError e) {
                    logger.warn(e);
                } catch (TransformerException e) {
                    logger.warn(e);
                }

//                name=name.trim();
            }
            if (references.item(j).getNodeName().equalsIgnoreCase("#text")){
                refString = references.item(j).getTextContent().trim();
            }
            if(references.item(j).getNodeName().equalsIgnoreCase("#text") && !references.item(j).getTextContent().trim().isEmpty()){
//
                statusType = null;
                if (!myName.getStatus().isEmpty()){
                    try {
                        statusType = nomStatusString2NomStatus(myName.getStatus());
                    } catch (UnknownCdmTypeException e) {
                        logger.warn("Problem with status");
                    }
                }

                INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
                String fullLineRefName = references.item(j).getTextContent().trim();
                TaxonNameBase nameTBF = parser.parseFullName(fullLineRefName, nomenclaturalCode, Rank.UNKNOWN_RANK());
                if (nameTBF.hasProblem() &&
                        !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                    nameTBF=solveNameProblem(fullLineRefName, fullLineRefName,parser);
                }
                nameTBF = getTaxonNameBase(nameTBF,nametosave,statusType);
                Synonym synonym = Synonym.NewInstance(nameTBF, refMods);


                if (!myName.getIdentifier().isEmpty() && (myName.getIdentifier().length()>2)){
                    setLSID(myName.getIdentifier(), acceptedTaxon);
                }

                acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
                importer.getTaxonService().saveOrUpdate(acceptedTaxon);
            }


        if(!myName.getName().isEmpty()){
            logger.info("acceptedTaxon and name: *"+acceptedTaxon.getTitleCache()+"*, *"+myName.getName()+"*");
            if (acceptedTaxon.getTitleCache().split("sec")[0].trim().equalsIgnoreCase(myName.getName().trim())){
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


                if (!myName.getIdentifier().isEmpty() && (myName.getIdentifier().length()>2)){
                    setLSID(myName.getIdentifier(), acceptedTaxon);

                }

                acceptedTaxon.getName().setNomenclaturalReference(refS);
                importer.getTaxonService().saveOrUpdate(acceptedTaxon);
            }
            else{
                INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
                TaxonNameBase nameTBF = parser.parseFullName(myName.getName(), nomenclaturalCode, myName.getRank());
                if (nameTBF.hasProblem() &&
                        !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                    //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                    nameTBF=solveNameProblem(myName.getOriginalName(), myName.getName(),parser);
                }
                nameTBF = getTaxonNameBase(nameTBF,nametosave,statusType);
                Synonym synonym = Synonym.NewInstance(nameTBF, refMods);


                if (!myName.getIdentifier().isEmpty() && (myName.getIdentifier().length()>2)){
                    String id = myName.getIdentifier().split("__")[0];
                    String source = myName.getIdentifier().split("__")[1];
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
                        Reference<?> re = ReferenceFactory.newGeneric();
                        re.setTitle(source);

                        IdentifiableSource os = IdentifiableSource.NewInstance(OriginalSourceType.Import,null,null,re,null);
                        os.setIdInSource(id);
//
//                        os.setCitation(re);
                        synonym.addSource(os);
                    }
                }

                acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
                importer.getTaxonService().saveOrUpdate(acceptedTaxon);
            }
        }
    }
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
        if ((id.indexOf("lsid")<0) || !lsidok){
            //ADD ORIGINAL SOURCE ID
            IdentifiableSource os = IdentifiableSource.NewInstance(OriginalSourceType.Import);
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
        while (nameTBF.hasProblem() && (retry <1) && !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank))){
            String fullname =  getFullReference(name,nameTBF.getParsingProblems());
            if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)){
                nameTBF = BotanicalName.NewInstance(null);
            }
            if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                nameTBF = ZoologicalName.NewInstance(null);
            }
            if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)){
                nameTBF= BacterialName.NewInstance(null);
            }
            parser.parseReferencedName(nameTBF, fullname, rank, false);
            retry++;
        }
        if (retry == 1){
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

        //        String fullContent = nomenclatureNode.getTextContent();

        NomenclaturalStatusType statusType = null;
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:status")){
                String status = children.item(i).getTextContent().trim();
                if (!status.isEmpty()){
                    try {
                        statusType = nomStatusString2NomStatus(status);
                    } catch (UnknownCdmTypeException e) {
                        logger.warn("Problem with status");
                    }
                }
            }
        }
        for (int i=0;i<children.getLength();i++){

            if (children.item(i).getNodeName().equalsIgnoreCase("#text")) {
                freetext=children.item(i).getTextContent();
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:collection_event")) {
                System.out.println("COLLECTION EVENT INSIDE NOMENCLATURE");
                extractMaterialsDirect(children.item(i), acceptedTaxon, refMods, "collection");
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:name")){
                MyName myName = new MyName();
                try {
                    myName = extractScientificName(children.item(i));
                    treatmentMainName = myName.getNewName();
                    originalTreatmentName = myName.getOriginalName();

                } catch (TransformerFactoryConfigurationError e1) {
                    logger.warn(e1);
                } catch (TransformerException e1) {
                    logger.warn(e1);
                }

                if (myName.getRank().equals(Rank.UNKNOWN_RANK()) || myName.getRank().isLower(configState.getConfig().getMaxRank())){
                    maxRankRespected=true;
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)){
                        nameToBeFilled = BotanicalName.NewInstance(null);
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                        nameToBeFilled = ZoologicalName.NewInstance(null);
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)){
                        nameToBeFilled = BacterialName.NewInstance(null);
                    }
                    acceptedTaxon = importer.getTaxonService().findBestMatchingTaxon(treatmentMainName);
                    System.out.println("TreatmentName "+treatmentMainName+" - "+acceptedTaxon);


                    boolean statusMatch=false;
                    if(acceptedTaxon !=null ){
                        statusMatch=compareStatus(acceptedTaxon, statusType);
                    }
                    if (acceptedTaxon ==null || (acceptedTaxon != null && !statusMatch)){
                        System.out.println("devrait pas venir la");
                        nameToBeFilled = parser.parseFullName(treatmentMainName, nomenclaturalCode, null);
                        if (nameToBeFilled.hasProblem() &&
                                !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                            nameToBeFilled = solveNameProblem(originalTreatmentName,treatmentMainName,parser);
                        }
                        nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave,statusType);
                        if (!originalTreatmentName.isEmpty()) {
                            TaxonNameDescription td = TaxonNameDescription.NewInstance();
                            td.setTitleCache(originalTreatmentName);
                            nameToBeFilled.addDescription(td);
                        }
                        if(statusType != null) {
                            nameToBeFilled.addStatus(NomenclaturalStatus.NewInstance(statusType));
                        }
                        nameToBeFilled.addSource(OriginalSourceType.Import,null,null,refMods,null);
                        acceptedTaxon= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
                        if(!configState.getConfig().doKeepOriginalSecundum()) {
                            acceptedTaxon.setSec(configState.getConfig().getSecundum());
                            logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
                        }


                        if (!myName.getIdentifier().isEmpty() && (myName.getIdentifier().length()>2)){
                            boolean lsidok=false;
                            String id = myName.getIdentifier().split("__")[0];
                            String source = myName.getIdentifier().split("__")[1];
                            if (id.indexOf("lsid")>-1){
                                try {
                                    LSID lsid = new LSID(id);
                                    acceptedTaxon.setLsid(lsid);
                                    lsidok=true;
                                } catch (MalformedLSIDException e) {
                                    logger.warn("Malformed LSID");
                                }

                            }
                            if ((id.indexOf("lsid")<0) || !lsidok){
                                //TODO ADD ORIGINAL SOURCE ID
                                IdentifiableSource os = IdentifiableSource.NewInstance(OriginalSourceType.Import);
                                os.setIdInSource(id);
                                Reference<?> re = ReferenceFactory.newGeneric();
                                re.setTitle(source);
                                os.setCitation(re);
                                acceptedTaxon.addSource(os);
                            }
                        }
/*<<<<<<< .courant
                        boolean sourceExists=false;
                        Set<IdentifiableSource> sources = acceptedTaxon.getSources();
                        for (IdentifiableSource src : sources){
                            String micro = src.getCitationMicroReference();
                            Reference r = src.getCitation();
                            if (r.equals(refMods)) {
                                sourceExists=true;
                            }
                        }
                        if(!sourceExists) {
                            acceptedTaxon.addSource(null,null,refMods,null);
                        }
=======*/

                        acceptedTaxon.addSource(OriginalSourceType.Import, null,null,refMods,null);
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
                            acceptedTaxon.addSource(OriginalSourceType.Import, null, null, refMods, null);
                        }
                        if (!sourcelinked || !configState.getConfig().doKeepOriginalSecundum()){

                            if (!myName.getIdentifier().isEmpty() && (myName.getIdentifier().length()>2)){
                                setLSID(myName.getIdentifier(), acceptedTaxon);
                            }
                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                        }
                    }
                }else{
                    maxRankRespected=false;
                }
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group") && maxRankRespected){
                reloadClassification();
                //extract the References within the document
                extractReferences(children.item(i),nametosave,acceptedTaxon,refMods);
            }

        }
        //        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;
    }

    /**
     * @return
     */
    private boolean compareStatus(Taxon t, NomenclaturalStatusType statusType) {
        boolean statusMatch=false;
        //found one taxon
        Set<NomenclaturalStatus> status = t.getName().getStatus();
        if (statusType!=null && status.size()>0){ //the statusType is known for both taxon
            for (NomenclaturalStatus st:status){
                NomenclaturalStatusType stype = st.getType();
                if (stype.toString().equalsIgnoreCase(statusType.toString())) {
                    statusMatch=true;
                }
            }
        }
        else{
            if(statusType == null && status.size()==0) {//there is no statusType, we can assume it's the same
                statusMatch=true;
            }
        }
        return statusMatch;
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
        logger.info("ADD TAXON: "+addTaxon);
        if (addTaxon == 0){
            Taxon tmp = askParent(acceptedTaxon, classification);
            if (tmp == null){
                s = askSetParent(s);
                r = askRank(s,rankListStr);

                NonViralName<?> nameToBeFilled = null;
                if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)){
                    nameToBeFilled = BotanicalName.NewInstance(null);
                }
                if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                    nameToBeFilled = ZoologicalName.NewInstance(null);
                }
                if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)){
                    nameToBeFilled = BacterialName.NewInstance(null);
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
            classification.addChildTaxon(acceptedTaxon, ref, null);
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
    private MyName extractScientificName(Node name) throws TransformerFactoryConfigurationError, TransformerException {
        //        System.out.println("extractScientificName");
        Rank rank = Rank.UNKNOWN_RANK();
        NodeList children = name.getChildNodes();
        String fullName = "";
        String newName="";
        String identifier="";
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        List<String> atomisedName= new ArrayList<String>();

        String rankStr = "";
        Rank tmpRank ;

        String status="";
        NomenclaturalStatusType statusType = null;
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:status") ||
                    (children.item(i).getNodeName().equalsIgnoreCase("tax:namePart") &&
                            children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("status"))){
                status = children.item(i).getTextContent().trim();
            }
        }

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
                    //                    if ((tmpRank != null) && (tmpRank.isLower(rank) || rank.equals(Rank.UNKNOWN_RANK()))) {
                    if (tmpRank != null){
                        rank=tmpRank;
                    }

                    atomisedMap.put(rankStr.toLowerCase(),atom.item(k).getTextContent().trim());
                    if (!atom.item(k).getNodeName().equalsIgnoreCase("dwc:taxonRank")) {
                        atomisedName.add(atom.item(k).getTextContent().trim());
                    }
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
        String[] names = new String[5];
        MyName myname = new MyName();
        myname.setOriginalName(fullName);
        myname.setNewName(newName);
        myname.setRank(rank);
        myname.setIdentifier(identifier);
        myname.setStatus(status);
        return myname;

    }

    /**
     * @param classification2
     */
    public void updateClassification(Classification classification2) {
        classification = classification2;
    }

    public class MyName {
        String originalName="";
        String newName="";
        Rank rank=Rank.UNKNOWN_RANK();
        String identifier="";
        String status="";

        public String getName(){
            if (newName.isEmpty()) {
                return originalName;
            } else {
                return newName;
            }

        }
        /**
         * @return the fullName
         */
        public String getOriginalName() {
            return originalName;
        }
        /**
         * @param fullName the fullName to set
         */
        public void setOriginalName(String fullName) {
            this.originalName = fullName;
        }
        /**
         * @return the newName
         */
        public String getNewName() {
            return newName;
        }
        /**
         * @param newName the newName to set
         */
        public void setNewName(String newName) {
            this.newName = newName;
        }
        /**
         * @return the rank
         */
        public Rank getRank() {
            return rank;
        }
        /**
         * @param rank the rank to set
         */
        public void setRank(Rank rank) {
            this.rank = rank;
        }
        /**
         * @return the idenfitiger
         */
        public String getIdentifier() {
            return identifier;
        }
        /**
         * @param idenfitiger the idenfitiger to set
         */
        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
        /**
         * @return the status
         */
        public String getStatus() {
            return status;
        }
        /**
         * @param status the status to set
         */
        public void setStatus(String status) {
            this.status = status;
        }



    }

}



