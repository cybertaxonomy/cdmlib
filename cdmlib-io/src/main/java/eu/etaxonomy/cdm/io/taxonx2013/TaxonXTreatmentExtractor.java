/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.taxonx2013;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.ITaxonNameBase;
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
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImplRegExBase;

/**
 * @author pkelbert
 * @date 2 avr. 2013
 *
 */
public class TaxonXTreatmentExtractor extends TaxonXExtractor{

    private static final String PUBLICATION_YEAR = "publicationYear";

	private static final Logger logger = Logger.getLogger(TaxonXTreatmentExtractor.class);

    private static final String notMarkedUp = "Not marked-up";
    private static final UUID proIbioTreeUUID = UUID.fromString("2c49f506-c7f7-44de-a8b9-2e695de3769c");
    private static final UUID OtherUUID = UUID.fromString("6465f8aa-2175-446f-807e-7163994b120f");
    private static final UUID NotMarkedUpUUID = UUID.fromString("796fe3a5-2c9c-4a89-b298-7598ca944063");
    private static final boolean skippQuestion = true;

    private final NomenclaturalCode nomenclaturalCode;
    private Classification classification;

    private  String treatmentMainName,originalTreatmentName;

    private final HashMap<String,Map<String,String>> namesMap = new HashMap<String, Map<String,String>>();


    private final Pattern keypattern = Pattern.compile("^(\\d+.*|-\\d+.*)");
    private final Pattern keypatternend = Pattern.compile("^.+?\\d$");

    private boolean maxRankRespected =false;
    private Map<String, Feature> featuresMap;

    private MyName currentMyName;

    private Reference sourceUrlRef;

    private String followingText;  //text element immediately following a tax:name in tax:nomenclature TODO move do state
    private String usedFollowingTextPrefix; //the part of the following text which has been used during taxon name creation

    private final TaxonXAddSources sourceHandler = new TaxonXAddSources();

    /**
     * @param nomenclaturalCode
     * @param classification
     * @param importer
     * @param configState
     */
    public TaxonXTreatmentExtractor(NomenclaturalCode nomenclaturalCode, Classification classification, TaxonXImport importer,
            TaxonXImportState configState,Map<String, Feature> featuresMap,  Reference urlSource) {
        this.nomenclaturalCode=nomenclaturalCode;
        this.classification = classification;
        this.importer=importer;
        this.state2=configState;
        this.featuresMap=featuresMap;
        this.sourceUrlRef =urlSource;
        prepareCollectors(configState, importer.getAgentService());
        this.sourceHandler.setSourceUrlRef(sourceUrlRef);
        this.sourceHandler.setImporter(importer);
        this.sourceHandler.setConfigState(configState);
    }

    /**
     * extracts all the treament information and save them
     * @param treatmentnode: the XML Node
     * @param tosave: the list of object to save into the CDM
     * @param refMods: the reference extracted from the MODS
     * @param sourceName: the URI of the document
     */
    @SuppressWarnings({ "rawtypes", "unused" })

    protected void extractTreatment(Node treatmentnode, Reference refMods, URI sourceName) {        logger.info("extractTreatment");
        List<TaxonNameBase> namesToSave = new ArrayList<TaxonNameBase>();
        NodeList children = treatmentnode.getChildNodes();
        Taxon acceptedTaxon =null;
        boolean hasRefgroup=false;

        //needed?
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group")) {
            	hasRefgroup=true;
            }
        }

        for (int i=0;i<children.getLength();i++){
        	Node child = children.item(i);
    		acceptedTaxon = handleSingleNode(refMods, sourceName, namesToSave, child, acceptedTaxon);
        }
        //        logger.info("saveUpdateNames");
        if (maxRankRespected){
            importer.getNameService().saveOrUpdate(namesToSave);
            importer.getClassificationService().saveOrUpdate(classification);
            //logger.info("saveUpdateNames-ok");
        }

        buildFeatureTree();
    }

	private Taxon handleSingleNode(Reference refMods, URI sourceName,
			List<TaxonNameBase> namesToSave, Node child, Taxon acceptedTaxon) {
		Taxon defaultTaxon =null;

		String nodeName = child.getNodeName();
		if (nodeName.equalsIgnoreCase("tax:nomenclature")){
		    NodeList nomenclatureChildren = child.getChildNodes();
		    boolean containsName = false;
		    for(int k=0; k<nomenclatureChildren.getLength(); k++){
		        if(nomenclatureChildren.item(k).getNodeName().equalsIgnoreCase("tax:name")){
		            containsName=true;
		            break;
		        }
		    }
		    if (containsName){
		        reloadClassification();
		        //extract "main" the scientific name
		        try{
		            acceptedTaxon = extractNomenclature(child, namesToSave, refMods);
		        }catch(ClassCastException e){
		        	//FIXME exception handling
		        	e.printStackTrace();
		        }
		        //                    System.out.println("acceptedTaxon : "+acceptedTaxon);
		    }
		}else if (nodeName.equalsIgnoreCase("tax:ref_group") && maxRankRespected){
		    reloadClassification();
		    //extract the References within the document
		    extractReferences(child, namesToSave ,acceptedTaxon,refMods);
		}else if (nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("multiple") && maxRankRespected){
		    File file = new File(TaxonXImport.LOG_FOLDER + "multipleTaxonX.txt");
		    FileWriter writer;
		    try {
		        writer = new FileWriter(file ,true);
		        writer.write(sourceName+"\n");
		        writer.flush();
		        writer.close();
		    } catch (IOException e1) {
		        // TODO Auto-generated catch block
		        logger.error(e1.getMessage());
		    }
		    //                String multiple = askMultiple(children.item(i));
		    String multiple = "Other";
		    if (multiple.equalsIgnoreCase("other")) {
		        extractSpecificFeatureNotStructured(child,acceptedTaxon, defaultTaxon,namesToSave, refMods,multiple);
		    }else if (multiple.equalsIgnoreCase("synonyms")) {
		        try{
		            extractSynonyms(child,acceptedTaxon, refMods, null);
		        }catch(NullPointerException e){
		            logger.warn("the accepted taxon is maybe null");
		        }
		    }else if(multiple.equalsIgnoreCase("material examined")){
		    	extractMaterials(child, acceptedTaxon, refMods, namesToSave);
		    }else if (multiple.equalsIgnoreCase("distribution")){
		    	extractDistribution(child, acceptedTaxon, defaultTaxon, namesToSave, refMods);
		    }else if (multiple.equalsIgnoreCase("type status")){
		    	extractDescriptionWithReference(child, acceptedTaxon, defaultTaxon,refMods, "TypeStatus");
		    }else if (multiple.equalsIgnoreCase("vernacular name")){
		    	extractDescriptionWithReference(child, acceptedTaxon, defaultTaxon,refMods, Feature.COMMON_NAME().getTitleCache());
		    }else{
		    	extractSpecificFeature(child,acceptedTaxon,defaultTaxon,namesToSave, refMods,multiple);
		    }
		}
		else if(nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("biology_ecology") && maxRankRespected){
		    extractFeature(child,acceptedTaxon,defaultTaxon, namesToSave, refMods, Feature.BIOLOGY_ECOLOGY());
		}
		else if(nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("vernacularName") && maxRankRespected){
		    extractDescriptionWithReference(child, acceptedTaxon,defaultTaxon,refMods, Feature.COMMON_NAME().getTitleCache());
		}
		else if(nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("description") && maxRankRespected){
		    extractFeature(child,acceptedTaxon,defaultTaxon,namesToSave, refMods, Feature.DESCRIPTION());
		}
		else if(nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("diagnosis") && maxRankRespected){
		    extractFeature(child,acceptedTaxon,defaultTaxon,namesToSave, refMods,Feature.DIAGNOSIS());
		}
		else if(nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("discussion") && maxRankRespected){
		    extractFeature(child,acceptedTaxon,defaultTaxon,namesToSave, refMods, Feature.DISCUSSION());
		}
		else if(nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("note") && maxRankRespected){
		    extractFeature(child,acceptedTaxon,defaultTaxon,namesToSave, refMods, Feature.DESCRIPTION());
		}
		else if(nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("distribution") && maxRankRespected){
		    extractDistribution(child,acceptedTaxon,defaultTaxon,namesToSave, refMods);
		}
		else if(nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("etymology") && maxRankRespected){
		    extractFeature(child,acceptedTaxon,defaultTaxon,namesToSave,refMods,Feature.ETYMOLOGY());
		}
		else if(nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("materials_examined") && maxRankRespected){
		    extractMaterials(child,acceptedTaxon, refMods, namesToSave);
		}
		else if(nodeName.equalsIgnoreCase("tax:figure") && maxRankRespected){
		    extractSpecificFeature(child,acceptedTaxon,defaultTaxon, namesToSave, refMods, "Figure");
		}
		else if(nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("Other") && maxRankRespected){
		    extractSpecificFeature(child, acceptedTaxon,defaultTaxon, namesToSave, refMods, "table");
		}else if(nodeName.equalsIgnoreCase("tax:div") &&
				child.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("key") && maxRankRespected){
		    //TODO IGNORE keys for the moment
		    //extractKey(children.item(i),acceptedTaxon, nameToSave,source, refMods);
		    extractSpecificFeatureNotStructured(child,acceptedTaxon,defaultTaxon,namesToSave, refMods,"Keys - unparsed");
		}
		else{
		    if (! nodeName.equalsIgnoreCase("tax:pb")){
		        //logger.info("ANOTHER KIND OF NODES: "+children.item(i).getNodeName()+", "+children.item(i).getAttributes());
		        if (child.getAttributes() !=null) {
		            logger.info("First Attribute: " + child.getAttributes().item(0));
		        }
		        extractSpecificFeatureNotStructured(child,acceptedTaxon,defaultTaxon,namesToSave, refMods, notMarkedUp);
		    }else{
		    	//FIXME
		    	logger.warn("Unhandled");
		    }
		}
		return acceptedTaxon;
	}


    protected Map<String,Feature> getFeaturesUsed(){
        return featuresMap;
    }
    /**
     *
     */
    private void buildFeatureTree() {
        logger.info("buildFeatureTree");
        FeatureTree proibiospheretree = importer.getFeatureTreeService().find(proIbioTreeUUID);
        if (proibiospheretree == null){
            List<FeatureTree> trees = importer.getFeatureTreeService().list(FeatureTree.class, null, null, null, null);
            if (trees.size()==1) {
                FeatureTree ft = trees.get(0);
                if (featuresMap==null) {
                    featuresMap=new HashMap<String, Feature>();
                }
                for (Feature feature: ft.getDistinctFeatures()){
                    if(feature!=null) {
                        featuresMap.put(feature.getTitleCache(), feature);
                    }
                }
            }
            proibiospheretree = FeatureTree.NewInstance();
            proibiospheretree.setUuid(proIbioTreeUUID);
        }
        //        FeatureNode root = proibiospheretree.getRoot();
        FeatureNode root2 = proibiospheretree.getRoot();
        if (root2 != null){
            int nbChildren = root2.getChildCount()-1;
            while (nbChildren>-1){
                try{
                    root2.removeChild(nbChildren);
                }catch(Exception e){logger.warn("Can't remove child from FeatureTree "+e);}
                nbChildren --;
            }

        }

        for (Feature feature:featuresMap.values()) {
            root2.addChild(FeatureNode.NewInstance(feature));
        }
        importer.getFeatureTreeService().saveOrUpdate(proibiospheretree);

    }


    /**
     * @param keys
     * @param acceptedTaxon: the current acceptedTaxon
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     */
    /*   @SuppressWarnings("rawtypes")
    private void extractKey(Node keys, Taxon acceptedTaxon,List<TaxonNameBase> nametosave, Reference refMods) {
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        NodeList children = keys.getChildNodes();
        String key="";
        PolytomousKey poly =  PolytomousKey.NewInstance();
        poly.addSource(OriginalSourceType.Import, null,null,refMods,null);
        poly.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
        poly.addTaxonomicScope(acceptedTaxon);
        poly.setTitleCache("bloup", true);
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
*/


    /**
     * @param taxons: the XML Nodegroup
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current accepted Taxon
     * @param refMods: the current reference extracted from the MODS
     *
     * @return Taxon object built
     */
    @SuppressWarnings({ "rawtypes", "unused" })
    private TaxonNameBase getTaxonNameBaseFromXML(Node taxons, List<TaxonNameBase> nametosave, Reference refMods, boolean isSynonym) {
        //        logger.info("getTaxonFromXML");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        logger.info("getTaxonNameBaseFromXML");
        TaxonNameBase nameToBeFilled = null;

        currentMyName=new MyName(isSynonym);

        NomenclaturalStatusType statusType = null;
        try {
        	String followingText = null;  //needs to be checked if following text is possible
            currentMyName = extractScientificName(taxons,refMods, null);
        } catch (TransformerFactoryConfigurationError e1) {
            logger.warn(e1);
        } catch (TransformerException e1) {
            logger.warn(e1);
        }
        /* INonViralNameParser parser = NonViralNameParserImpl.NewInstance();

        nameToBeFilled = parser.parseFullName(currentMyName.getName(), nomenclaturalCode, currentMyName.getRank());
        if (nameToBeFilled.hasProblem() &&
                !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
            //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
            addProblemNameToFile(currentMyName.getName(),"",nomenclaturalCode,currentMyName.getRank());
            nameToBeFilled=solveNameProblem(currentMyName.getOriginalName(), currentMyName.getName(),parser,currentMyName.getAuthor(), currentMyName.getRank());
        }

        nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave,statusType);
         */
        nameToBeFilled = currentMyName.getTaxonNameBase();
        return nameToBeFilled;

    }


    /**
     *
     */
    private void reloadClassification() {
        logger.info("reloadClassification");
        Classification cl = importer.getClassificationService().find(classification.getUuid());
        if (cl != null){
            classification = cl;
        }else{
            importer.getClassificationService().saveOrUpdate(classification);
            classification = importer.getClassificationService().find(classification.getUuid());
        }
    }

    //    /**
    //     * Create a Taxon for the current NameBase, based on the current reference
    //     * @param taxonNameBase
    //     * @param refMods: the current reference extracted from the MODS
    //     * @return Taxon
    //     */
    //    @SuppressWarnings({ "unused", "rawtypes" })
    //    private Taxon getTaxon(TaxonNameBase taxonNameBase, Reference refMods) {
    //        Taxon t = new Taxon(taxonNameBase,null );
    //        if (!configState.getConfig().doKeepOriginalSecundum() || (t.getSec() == null)) {
    //            t.setSec(configState.getConfig().getSecundum());
    //            logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
    //        }
    //        /*<<<<<<< .courant
    //        boolean sourceExists=false;
    //        Set<IdentifiableSource> sources = t.getSources();
    //        for (IdentifiableSource src : sources){
    //            String micro = src.getCitationMicroReference();
    //            Reference r = src.getCitation();
    //            if (r.equals(refMods) && micro == null) {
    //                sourceExists=true;
    //            }
    //        }
    //        if(!sourceExists) {
    //            t.addSource(null,null,refMods,null);
    //        }
    //=======*/
    //        t.addSource(OriginalSourceType.Import,null,null,refMods,null);
    //        t.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
    //        return t;
    //    }

    private void  extractDescriptionWithReference(Node typestatus, Taxon acceptedTaxon, Taxon defaultTaxon, Reference refMods,
            String featureName) {
        //        System.out.println("extractDescriptionWithReference !");
        logger.info("extractDescriptionWithReference");
        NodeList children = typestatus.getChildNodes();

        Feature currentFeature=getFeatureObjectFromString(featureName);

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

        Reference currentref =  ReferenceFactory.newGeneric();
        if(!r.isEmpty()) {
            currentref.setTitleCache(r, true);
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
    private void extractDistribution(Node distribution, Taxon acceptedTaxon, Taxon defaultTaxon, List<TaxonNameBase> nametosave, Reference refMods) {
        logger.info("extractDistribution");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        NodeList children = distribution.getChildNodes();
        Map<Integer,List<MySpecimenOrObservation>> specimenOrObservations = new HashMap<Integer, List<MySpecimenOrObservation>>();
        Map<Integer,String> descriptionsFulltext = new HashMap<Integer,String>();

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList paragraph = children.item(i).getChildNodes();
                for (int j=0;j<paragraph.getLength();j++){
                    if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text")){
                        extractText(descriptionsFulltext, i, paragraph.item(j));
                    }
                    else if (paragraph.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        extractInLine(nametosave, refMods, descriptionsFulltext, i,paragraph.item(j));
                    }
                    else if (paragraph.item(j).getNodeName().equalsIgnoreCase("tax:collection_event")){
                        MySpecimenOrObservation specimenOrObservation = new MySpecimenOrObservation();
                        DerivedUnit derivedUnitBase = null;
                        specimenOrObservation = extractSpecimenOrObservation(paragraph.item(j), derivedUnitBase, SpecimenOrObservationType.DerivedUnit, null);
                        extractTextFromSpecimenOrObservation(specimenOrObservations, descriptionsFulltext, i, specimenOrObservation);
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


        if(acceptedTaxon!=null){
            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
            Feature currentFeature = Feature.DISTRIBUTION();
            //        DerivedUnit derivedUnitBase=null;
            //        String descr="";
            for (int k=0;k<=m;k++){
                if(specimenOrObservations.keySet().contains(k)){
                    for (MySpecimenOrObservation soo:specimenOrObservations.get(k) ) {
                        handleAssociation(acceptedTaxon, refMods, td, soo);
                    }
                }

                if (descriptionsFulltext.keySet().contains(k)){
                    if (!stringIsEmpty(descriptionsFulltext.get(k).trim()) && (descriptionsFulltext.get(k).startsWith("Hab.") || descriptionsFulltext.get(k).startsWith("Habitat"))){
                        setParticularDescription(descriptionsFulltext.get(k),acceptedTaxon,defaultTaxon, refMods, Feature.HABITAT());
                        break;
                    }
                    else{
                        handleTextData(refMods, descriptionsFulltext, td, currentFeature, k);
                    }
                }

                if (descriptionsFulltext.keySet().contains(k) || specimenOrObservations.keySet().contains(k)){
                    acceptedTaxon.addDescription(td);
                    sourceHandler.addAndSaveSource(refMods, td, null);
                    importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                }
            }
        }
    }

    /**
     * @param refMods
     * @param descriptionsFulltext
     * @param td
     * @param currentFeature
     * @param k
     */
    private void handleTextData(Reference refMods, Map<Integer, String> descriptionsFulltext, TaxonDescription td,
            Feature currentFeature, int k) {
        //logger.info("handleTextData");
        TextData textData = TextData.NewInstance();
        textData.setFeature(currentFeature);
        textData.putText(Language.UNKNOWN_LANGUAGE(), descriptionsFulltext.get(k));
        sourceHandler.addSource(refMods, textData);
        td.addElement(textData);
    }

    /**
     * @param acceptedTaxon
     * @param refMods
     * @param td
     * @param soo
     */
    private void handleAssociation(Taxon acceptedTaxon, Reference refMods, TaxonDescription td, MySpecimenOrObservation soo) {
        logger.info("handleAssociation");
        String descr=soo.getDescr();
        DerivedUnit derivedUnitBase = soo.getDerivedUnitBase();

        sourceHandler.addAndSaveSource(refMods, derivedUnitBase);

        TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);

        Feature feature=null;
        feature = makeFeature(derivedUnitBase);
        if(!StringUtils.isEmpty(descr)) {
            derivedUnitBase.setTitleCache(descr, true);
        }

        IndividualsAssociation indAssociation = createIndividualAssociation(refMods, derivedUnitBase, feature);

        taxonDescription.addElement(indAssociation);
        sourceHandler.addAndSaveSource(refMods, taxonDescription,null);
        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        td.setDescribedSpecimenOrObservation(soo.getDerivedUnitBase());
    }

    /**
     * create an individualAssociation
     * @param refMods
     * @param derivedUnitBase
     * @param feature
     * @return
     */
    private IndividualsAssociation createIndividualAssociation(Reference refMods, DerivedUnit derivedUnitBase,
            Feature feature) {
        logger.info("createIndividualAssociation");
        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();
        indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
        indAssociation.setFeature(feature);
        indAssociation = sourceHandler.addSource(refMods, indAssociation);
        return indAssociation;
    }

    /**
     * @param specimenOrObservations
     * @param descriptionsFulltext
     * @param i
     * @param specimenOrObservation
     */
    private void extractTextFromSpecimenOrObservation(Map<Integer, List<MySpecimenOrObservation>> specimenOrObservations,
            Map<Integer, String> descriptionsFulltext, int i, MySpecimenOrObservation specimenOrObservation) {
        logger.info("extractTextFromSpecimenOrObservation");
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

    /**
     * Extract the text with the inline link to a taxon
     * @param nametosave
     * @param refMods
     * @param descriptionsFulltext
     * @param i
     * @param paragraph
     */
    @SuppressWarnings("rawtypes")
    private void extractInLine(List<TaxonNameBase> nametosave, Reference refMods, Map<Integer, String> descriptionsFulltext,
            int i, Node paragraph) {
        //logger.info("extractInLine");
        String inLine=getInlineTextForName(nametosave, refMods, paragraph);
        if (descriptionsFulltext.get(i) !=null){
            inLine = descriptionsFulltext.get(i)+inLine;
        }
        descriptionsFulltext.put(i, inLine);
    }

    /**
     * Extract the raw text from a Node
     * @param descriptionsFulltext
     * @param node
     * @param j
     */
    private void extractText(Map<Integer, String> descriptionsFulltext, int i, Node node) {
        //logger.info("extractText");
        if(!node.getTextContent().trim().isEmpty()) {
            String s =node.getTextContent().trim();
            if (descriptionsFulltext.get(i) !=null){
                s = descriptionsFulltext.get(i)+" "+s;
            }
            descriptionsFulltext.put(i, s);
        }
    }


    /**
     * @param materials: the XML node group
     * @param acceptedTaxon: the current accepted Taxon
     * @param refMods: the current reference extracted from the MODS
     */
    @SuppressWarnings("rawtypes")
    private void extractMaterials(Node materials, Taxon acceptedTaxon, Reference refMods,List<TaxonNameBase> nametosave) {
        logger.info("EXTRACTMATERIALS");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        NodeList children = materials.getChildNodes();
        NodeList events = null;
        //        String descr="";


        for (int i=0;i<children.getLength();i++){
            String rawAssociation="";
            boolean added=false;
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                events = children.item(i).getChildNodes();
                for(int k=0;k<events.getLength();k++){
                    if (events.item(k).getNodeName().equalsIgnoreCase("tax:name")){
                        String inLine= getInlineTextForName(nametosave, refMods, events.item(k));
                        if(!inLine.isEmpty()) {
                            rawAssociation+=inLine;
                        }
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
                        handleDerivedUnitFacadeAndBase(acceptedTaxon, refMods, events.item(k), rawAssociation);
                    }
                    if (!rawAssociation.isEmpty() && !added){

                        Feature feature = Feature.MATERIALS_EXAMINED();
                        featuresMap.put(feature.getTitleCache(),feature);

                        TextData textData = createTextData(rawAssociation, refMods, feature);

                        if(! rawAssociation.isEmpty() && (acceptedTaxon!=null)){
                            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
                            td.addElement(textData);
                            acceptedTaxon.addDescription(td);
                            sourceHandler.addAndSaveSource(refMods, td, null);
                        }
                        //                        DerivedUnitFacade derivedUnitFacade = getFacade(rawAssociation.replaceAll(";",""),SpecimenOrObservationType.DerivedUnit);
                        //                        derivedUnitBase = derivedUnitFacade.innerDerivedUnit();
                        //
                        //                        TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);
                        //                        acceptedTaxon.addDescription(taxonDescription);
                        //
                        //                        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();
                        //
                        //                        Feature feature = Feature.MATERIALS_EXAMINED();
                        //                        featuresMap.put(feature.getTitleCache(),feature);
                        //                        if(!StringUtils.isEmpty(rawAssociation)) {
                        //                            derivedUnitBase.setTitleCache(rawAssociation, true);
                        //                        }
                        //                        indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
                        //                        indAssociation.setFeature(feature);
                        //                        indAssociation.addSource(OriginalSourceType.Import, null, null, refMods, null);
                        //
                        //                        /*boolean sourceExists=false;
                        //                        Set<DescriptionElementSource> dsources = indAssociation.getSources();
                        //                        for (DescriptionElementSource src : dsources){
                        //                            String micro = src.getCitationMicroReference();
                        //                            Reference r = src.getCitation();
                        //                            if (r.equals(refMods) && micro == null) {
                        //                                sourceExists=true;
                        //                            }
                        //                        }
                        //                        if(!sourceExists) {
                        //                            indAssociation.addSource(null, null, refMods, null);
                        //                        }*/
                        //                        taxonDescription.addElement(indAssociation);
                        //                        taxonDescription.setTaxon(acceptedTaxon);
                        //                        taxonDescription.addSource(OriginalSourceType.Import, null,null,refMods,null);
                        //
                        //                        /*sourceExists=false;
                        //                        Set<IdentifiableSource> sources = taxonDescription.getSources();
                        //                        for (IdentifiableSource src : sources){
                        //                            String micro = src.getCitationMicroReference();
                        //                            Reference r = src.getCitation();
                        //                            if (r.equals(refMods) && micro == null) {
                        //                                sourceExists=true;
                        //                            }
                        //                        }
                        //                        if(!sourceExists) {
                        //                            taxonDescription.addSource(OriginalSourceType.Import,null,null,refMods,null);
                        //                        }*/
                        //
                        //                        importer.getDescriptionService().saveOrUpdate(taxonDescription);
                        importer.getTaxonService().saveOrUpdate(acceptedTaxon);

                        rawAssociation="";
                    }
                }
            }
        }
    }

    /**
     * @param acceptedTaxon
     * @param refMods
     * @param events
     * @param rawAssociation
     * @param k
     */
    private void handleDerivedUnitFacadeAndBase(Taxon acceptedTaxon, Reference refMods, Node event,
            String rawAssociation) {
        logger.info("handleDerivedUnitFacadeAndBase");
        String descr;
        DerivedUnit derivedUnitBase;
        MySpecimenOrObservation myspecimenOrObservation;
        DerivedUnitFacade derivedUnitFacade = getFacade(rawAssociation.replaceAll(";",""),SpecimenOrObservationType.DerivedUnit);
        derivedUnitBase = derivedUnitFacade.innerDerivedUnit();

        sourceHandler.addAndSaveSource(refMods, derivedUnitBase);

        //TODO this may not always be correct, ask user
        TaxonNameBase<?,?> typifiableName = acceptedTaxon != null ?  acceptedTaxon.getName() : null;
        myspecimenOrObservation = extractSpecimenOrObservation(event,derivedUnitBase,SpecimenOrObservationType.DerivedUnit, typifiableName);
        derivedUnitBase = myspecimenOrObservation.getDerivedUnitBase();
        descr=myspecimenOrObservation.getDescr();

        sourceHandler.addAndSaveSource(refMods, derivedUnitBase);

        TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);

        Feature feature = makeFeature(derivedUnitBase);
        featuresMap.put(feature.getTitleCache(),feature);
        if(!StringUtils.isEmpty(descr)) {
            derivedUnitBase.setTitleCache(descr, true);
        }

        IndividualsAssociation indAssociation = createIndividualAssociation(refMods, derivedUnitBase, feature);

        taxonDescription.addElement(indAssociation);
        sourceHandler.addAndSaveSource(refMods, taxonDescription,null);
        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
    }



    /**
     * @param currentName
     * @param materials: the XML node group
     * @param acceptedTaxon: the current accepted Taxon
     * @param refMods: the current reference extracted from the MODS
     */
    private String extractMaterialsDirect(Node materials, Taxon acceptedTaxon, Reference refMods, String event, TaxonNameBase<?,?> currentName) {
        logger.info("extractMaterialsDirect");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        String descr="";

        DerivedUnit derivedUnitBase=null;
        MySpecimenOrObservation myspecimenOrObservation = extractSpecimenOrObservation(materials,derivedUnitBase, SpecimenOrObservationType.DerivedUnit, currentName);
        derivedUnitBase = myspecimenOrObservation.getDerivedUnitBase();

        sourceHandler.addAndSaveSource(refMods, derivedUnitBase);

        TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);

        Feature feature=null;
        if (event.equalsIgnoreCase("collection")){
            feature = makeFeature(derivedUnitBase);
        }
        else{
            feature = Feature.MATERIALS_EXAMINED();
        }
        featuresMap.put(feature.getTitleCache(),  feature);

        descr=myspecimenOrObservation.getDescr();
        if(!StringUtils.isEmpty(descr)) {
            derivedUnitBase.setTitleCache(descr, true);
        }

        IndividualsAssociation indAssociation = createIndividualAssociation(refMods, derivedUnitBase, feature);

        taxonDescription.addElement(indAssociation);
        sourceHandler.addAndSaveSource(refMods, taxonDescription,null);
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
    @SuppressWarnings({ "rawtypes"})
    private String extractSpecificFeature(Node description, Taxon acceptedTaxon, Taxon defaultTaxon,
            List<TaxonNameBase> nametosave, Reference refMods, String featureName ) {
        logger.info("extractSpecificFeature "+featureName);
        //        System.out.println("GRUUUUuu");
        NodeList children = description.getChildNodes();
        NodeList insideNodes ;
        NodeList trNodes;
        //        String descr ="";
        String localdescr="";
        List<String> blabla=null;
        List<String> text = new ArrayList<String>();

        String table="<table>";
        String head="";
        String line="";

        Feature currentFeature=getFeatureObjectFromString(featureName);

        //        String fullContent = description.getTextContent();
        for (int i=0;i<children.getLength();i++){
            //            localdescr="";
            if (children.item(i).getNodeName().equalsIgnoreCase("#text") && !children.item(i).getTextContent().trim().isEmpty()){
                text.add(children.item(i).getTextContent().trim());
            }
            if (featureName.equalsIgnoreCase("table")){
                if (children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                        children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("thead")){
                    head = extractTableHead(children.item(i));
                    table+=head;
                    line = extractTableLine(children.item(i));
                    if (!line.equalsIgnoreCase("<tr></tr>")) {
                        table+=line;
                    }
                }
                if (children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                        children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("tr")){
                    line = extractTableLineWithColumn(children.item(i).getChildNodes());
                    if(!line.equalsIgnoreCase("<tr></tr>")) {
                        table+=line;
                    }
                }
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                insideNodes=children.item(i).getChildNodes();
                blabla= new ArrayList<String>();
                for (int j=0;j<insideNodes.getLength();j++){
                    Node insideNode = insideNodes.item(j);
                	if (insideNode.getNodeName().equalsIgnoreCase("tax:name")){
                        String inlinetext = getInlineTextForName(nametosave, refMods, insideNode);
                        if (!inlinetext.isEmpty()) {
                            blabla.add(inlinetext);
                        }
                    }
                    else if (insideNode.getNodeName().equalsIgnoreCase("#text")) {
                        if(!insideNode.getTextContent().trim().isEmpty()){
                            blabla.add(insideNode.getTextContent().trim());
                            //                            localdescr += insideNodes.item(j).getTextContent().trim();
                        }
                    }
                }
                if (!blabla.isEmpty()) {
                    String blaStr = StringUtils.join(blabla," ").trim();
                    if(!stringIsEmpty(blaStr)) {
                        setParticularDescription(blaStr,acceptedTaxon,defaultTaxon, refMods,currentFeature);
                        text.add(blaStr);
                    }
                }

            }
            if (children.item(i).getNodeName().equalsIgnoreCase("#text")){
                if(!children.item(i).getTextContent().trim().isEmpty()){
                    localdescr = children.item(i).getTextContent().trim();
                    if(!stringIsEmpty(localdescr)) {
                        setParticularDescription(localdescr,acceptedTaxon,defaultTaxon, refMods,currentFeature);
                    }
                }
            }
        }

        table+="</table>";
        if (!table.equalsIgnoreCase("<table></table>")){
            //            System.out.println("TABLE : "+table);
            text.add(table);
        }

        if (text !=null && !text.isEmpty()) {
            return StringUtils.join(text," ");
        } else {
            return "";
        }

    }

    /**
     * @param children
     * @param i
     * @return
     */
    private String extractTableLine(Node child) {
        //logger.info("extractTableLine");
        String line;
        line="<tr>";
        if (child.getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("tr")){
            line = extractTableLineWithColumn(child.getChildNodes());
        }
        line+="</tr>";
        return line;
    }

    /**
     * @param children
     * @param i
     * @return
     */
    private String extractTableHead(Node child) {
        //logger.info("extractTableHead");
        String head;
        String line;
        head="<th>";
        NodeList trNodes = child.getChildNodes();
        for (int k=0;k<trNodes.getLength();k++){
            if (trNodes.item(k).getNodeName().equalsIgnoreCase("tax:div")
                    && trNodes.item(k).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("tr")){
                line = extractTableLineWithColumn(trNodes.item(k).getChildNodes());
                head+=line;
            }
        }
        head+="</th>";
        return head;
    }

    /**
     * build a html table line, with td columns
     * @param tdNodes
     * @return an html coded line
     */
    private String extractTableLineWithColumn(NodeList tdNodes) {
        //logger.info("extractTableLineWithColumn");
        String line;
        line="<tr>";
        for (int l=0;l<tdNodes.getLength();l++){
            if (tdNodes.item(l).getNodeName().equalsIgnoreCase("tax:p")){
                line+="<td>"+tdNodes.item(l).getTextContent()+"</td>";
            }
        }
        line+="</tr>";
        return line;
    }

    /**
     * @param description: the XML node group
     * @param acceptedTaxon: the current acceptedTaxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @param featureName: the feature name
     */
    @SuppressWarnings({ "unused", "rawtypes" })
    private String extractSpecificFeatureNotStructured(Node description, Taxon acceptedTaxon, Taxon defaultTaxon,
            List<TaxonNameBase> nameToSave, Reference refMods, String featureName ) {
        logger.info("extractSpecificFeatureNotStructured " + featureName);
        NodeList children = description.getChildNodes();
        NodeList insideNodes ;
        List<String> blabla= new ArrayList<String>();


        Feature currentFeature = getFeatureObjectFromString(featureName);

        String fullContent = description.getTextContent();
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                insideNodes=children.item(i).getChildNodes();
                for (int j=0;j<insideNodes.getLength();j++){
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        String inlineText =getInlineTextForName(nameToSave, refMods, insideNodes.item(j));
                        if(!inlineText.isEmpty()) {
                            blabla.add(inlineText);
                        }
                    }
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("#text")) {
                        if(!insideNodes.item(j).getTextContent().trim().isEmpty()){
                            blabla.add(insideNodes.item(j).getTextContent().trim());
                        }
                    }
                }
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("#text")){
                if(!children.item(i).getTextContent().trim().isEmpty()){
                    String localdescr = children.item(i).getTextContent().trim();
                    if(!localdescr.isEmpty())
                    {
                        blabla.add(localdescr);
                    }
                }
            }
        }

        if (blabla !=null && !blabla.isEmpty()) {
            String blaStr = StringUtils.join(blabla," ").trim();
            if (! stringIsEmpty(blaStr)) {
                setParticularDescription(blaStr,acceptedTaxon,defaultTaxon, refMods,currentFeature);
                return blaStr;
            } else {
                return "";
            }
        } else {
            return "";
        }

    }

    /**
     * @param blaStr
     * @return
     */
    private boolean stringIsEmpty(String blaStr) {
        if (blaStr.matches("(\\.|,|;|\\.-)?")){
        	return true;
        }else{
        	return false;
        }
    }

    /**
     * @param nametosave
     * @param refMods
     * @param insideNodes
     * @param blabla
     * @param j
     */
    @SuppressWarnings({ "rawtypes" })
    private String getInlineTextForName(List<TaxonNameBase> nametosave, Reference refMods, Node insideNode) {
        if (true){
        	NodeList children = insideNode.getChildNodes();
        	String result = "";
            for (int i=0;i<children.getLength();i++){
            	Node nameChild = children.item(i);
                if(nameChild.getNodeName().equalsIgnoreCase("#text")){
                	result += nameChild.getTextContent();
                }else{
                	//do nothing
                }
            }
        	return result.replace("\n", "").trim();
        }else{
	    	TaxonNameBase tnb = getTaxonNameBaseFromXML(insideNode, nametosave,refMods,false);
	        //                        Taxon tax = getTaxonFromTxonNameBase(tnb, refMods);
	        Taxon tax = currentMyName.getTaxon();
	        if(tnb !=null && tax != null){
	            String linkedTaxon = tnb.getTitleCache().split("sec")[0];//TODO NOT IMPLEMENTED IN THE CDM YET
	            return "<cdm:taxon uuid='"+tax.getUuid()+"'>"+linkedTaxon+"</cdm:taxon>";
	        }else if (tnb != null && tax == null){
	        	//TODO
	        	return "<cdm:taxonName uuid='" + tnb.getUuid() +"'>" + tnb.getTitleCache().split("sec")[0]  +"</cdm:taxonName>";
	        }else{
	        	logger.warn("Inline text has no content yet");
	        }
	        return "";
        }
    }

    /**
     * @param featureName
     * @return
     */
    @SuppressWarnings("rawtypes")
    private Feature getFeatureObjectFromString(String featureName) {
        logger.info("getFeatureObjectFromString");
        List<Feature> features = importer.getTermService().list(Feature.class, null,null,null,null);
        Feature currentFeature=null;
        for (Feature feature: features){
            String tmpF = feature.getTitleCache();
            if (tmpF.equalsIgnoreCase(featureName)) {
                currentFeature=feature;
                //                System.out.println("currentFeatureFromList "+currentFeature.getUuid());
            }
        }
        if (currentFeature == null) {
            currentFeature=Feature.NewInstance(featureName, featureName, featureName);
            if(featureName.equalsIgnoreCase("Other")){
                currentFeature.setUuid(OtherUUID);
            }
            if(featureName.equalsIgnoreCase(notMarkedUp)){
                currentFeature.setUuid(NotMarkedUpUUID);
            }
            importer.getTermService().saveOrUpdate(currentFeature);
        }
        return currentFeature;
    }




    /**
     * @param children: the XML node group
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current acceptedTaxon
     * @param refMods: the current reference extracted from the MODS
     * @param fullContent :the parsed XML content
     * @return a list of description (text)
     */
    @SuppressWarnings({ "unused", "rawtypes" })
    private List<String> parseParagraph(List<TaxonNameBase> namesToSave, Taxon acceptedTaxon, Reference refMods, Node paragraph, Feature feature){
        logger.info("parseParagraph "+feature.toString());
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
                    //    System.out.println("insideNodes.item(j).getNodeName() : "+insideNodes.item(j).getNodeName());
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        String inlineText = getInlineTextForName(namesToSave, refMods, insideNodes.item(j));
                        if (!inlineText.isEmpty()) {
                            blabla.add(inlineText);
                        }
                        nodeKnown=true;
                    }
                    else if (insideNodes.item(j).getNodeName().equalsIgnoreCase("#text")) {
                        if(!insideNodes.item(j).getTextContent().trim().isEmpty()){
                            blabla.add(insideNodes.item(j).getTextContent().trim());
                            // localdescr += insideNodes.item(j).getTextContent().trim();
                        }
                        nodeKnown=true;
                    }
                    else if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:bibref")) {
                        String ref = insideNodes.item(j).getTextContent().trim();
                        if (ref.endsWith(";")  && ((ref.length())>1)) {
                            ref=ref.substring(0, ref.length()-1)+".";
                        }
                        Reference reference = ReferenceFactory.newGeneric();
                        reference.setTitleCache(ref, true);
                        blabla.add(reference.getTitleCache());
                        nodeKnown=true;
                    }
                    else if  (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:figure")){
                        String figure = extractSpecificFeature(insideNodes.item(j),acceptedTaxon,acceptedTaxon, namesToSave, refMods, "figure");
                        blabla.add(figure);
                    }
                    else if(insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:div") &&
                            insideNodes.item(j).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("Other") &&
                            insideNodes.item(j).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("table")){
                        String table = extractSpecificFeature(insideNodes.item(j),acceptedTaxon,acceptedTaxon, namesToSave, refMods, "table");
                        blabla.add(table);
                    }
                    else if  (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:collection_event")) {
                        //                        logger.warn("SEEMS TO BE COLLECTION EVENT INSIDE A "+feature.toString());
                        String titlecache  = extractMaterialsDirect(insideNodes.item(j), acceptedTaxon, refMods, "collection", null);
                        blabla.add(titlecache);
                        collectionEvent=true;
                        collectionEvents.add(insideNodes.item(j));
                        nodeKnown=true;
                    }else{
                    	logger.warn("node not handled yet: " + insideNodes.item(j).getNodeName());
                    }

                }
                if (!StringUtils.isBlank(StringUtils.join(blabla," "))) {
                    fullDescription.add(StringUtils.join(blabla," "));
                }
            }
            if  (children.item(i).getNodeName().equalsIgnoreCase("tax:figure")){
                String figure = extractSpecificFeature(children.item(i),acceptedTaxon,acceptedTaxon, namesToSave, refMods, "Figure");
                fullDescription.add(figure);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("Other") &&
                    children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("table")){
                String table = extractSpecificFeature(children.item(i),acceptedTaxon,acceptedTaxon, namesToSave, refMods, "table");
                fullDescription.add(table);
            }
        }

        if( !stringIsEmpty(descr.trim())){
            Feature currentFeature= getNotMarkedUpFeatureObject();
            setParticularDescription(descr.trim(),acceptedTaxon,acceptedTaxon, refMods,currentFeature);
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
    @SuppressWarnings("rawtypes")
    private void extractFeature(Node description, Taxon acceptedTaxon, Taxon defaultTaxon, List<TaxonNameBase> namesToSave, Reference refMods, Feature feature){
        logger.info("EXTRACT FEATURE "+feature.toString());
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
        List<String> fullDescription= parseParagraph( namesToSave, acceptedTaxon, refMods, description,feature);

        //        System.out.println("Feature : "+feature.toString()+", "+fullDescription.toString());
        if (!fullDescription.isEmpty() &&!stringIsEmpty(StringUtils.join(fullDescription,"\n").trim())) {
            setParticularDescription(StringUtils.join(fullDescription,"\n").trim(),acceptedTaxon,defaultTaxon, refMods,feature);
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
    private void setParticularDescription(String descr, Taxon acceptedTaxon, Taxon defaultTaxon, Reference refMods, Feature currentFeature) {
        logger.info("setParticularDescription " + currentFeature.getTitleCache()+", \n blabla : "+descr);

        //remove redundant feature title
        String featureStr = currentFeature.getTitleCache();
        if (!descr.isEmpty() && descr.toLowerCase().startsWith(featureStr.toLowerCase())){
        	descr = descr.replaceAll("(?i)" + featureStr + "\\.\\s*", "");
        }


        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
        featuresMap.put(currentFeature.getTitleCache(),currentFeature);

        TextData textData = createTextData(descr, refMods, currentFeature);

        if(acceptedTaxon!=null){
            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
            td.addElement(textData);
            acceptedTaxon.addDescription(td);

            sourceHandler.addAndSaveSource(refMods, td, null);
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
            sourceHandler.addAndSaveSource(refMods, td, null);
            importer.getTaxonService().saveOrUpdate(defaultTaxon);
        }
    }

    /**
     * @param descr
     * @param refMods
     * @param currentFeature
     * @return
     */
    private TextData createTextData(String descr, Reference refMods, Feature currentFeature) {
        //logger.info("createTextData");
        TextData textData = TextData.NewInstance();
        textData.setFeature(currentFeature);
        sourceHandler.addSource(refMods, textData);

        textData.putText(Language.UNKNOWN_LANGUAGE(), descr);
        return textData;
    }



    /**
     * @param descr: the XML Nodegroup to parse
     * @param acceptedTaxon: the current acceptedTaxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param refMods: the current reference extracted from the MODS
     * @param currentFeature: the feature name
     * @return
     */
    private void setParticularDescription(String descr, Taxon acceptedTaxon, Taxon defaultTaxon,Reference currentRef, Reference refMods, Feature currentFeature) {
        //        System.out.println("setParticularDescriptionSPecial "+currentFeature);
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        logger.info("setParticularDescription");
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        featuresMap.put(currentFeature.getTitleCache(),currentFeature);
        TextData textData = createTextData(descr, refMods, currentFeature);

        if(! descr.isEmpty() && (acceptedTaxon!=null)){
            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
            td.addElement(textData);
            acceptedTaxon.addDescription(td);

            sourceHandler.addAndSaveSource(refMods, td, currentRef);
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
            sourceHandler.addAndSaveSource(currentRef, td,currentRef);
            importer.getTaxonService().saveOrUpdate(defaultTaxon);
        }
    }



    /**
     * @param synonyms: the XML Nodegroup to parse
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current acceptedTaxon
     * @param refMods: the current reference extracted from the MODS
     */
    @SuppressWarnings({ "rawtypes" })
    private void extractSynonyms(Node synonymsNode, Taxon acceptedTaxon,Reference refMods, String followingText) {
        logger.info("extractSynonyms");
        //System.out.println("extractSynonyms for: "+acceptedTaxon);
        Taxon ttmp = (Taxon) importer.getTaxonService().find(acceptedTaxon.getUuid());
        if (ttmp != null) {
            acceptedTaxon = CdmBase.deproxy(ttmp,Taxon.class);
        }
        else{
            acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
        }
        NodeList children = synonymsNode.getChildNodes();
        List<MyName> names = new ArrayList<MyName>();

        if(synonymsNode.getNodeName().equalsIgnoreCase("tax:name")){
            try {
            	MyName myName = extractScientificNameSynonym(synonymsNode, refMods, followingText);
                names.add(myName);
            } catch (TransformerFactoryConfigurationError e) {
                logger.warn(e);
            } catch (TransformerException e) {
                logger.warn(e);
            }
        }


        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList tmp = children.item(i).getChildNodes();
                //                String fullContent = children.item(i).getTextContent();
                for (int j=0; j< tmp.getLength();j++){
                    if(tmp.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        try {
                        	MyName myName = extractScientificNameSynonym(tmp.item(j),refMods, followingText);
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
                try {
                	MyName myName = extractScientificNameSynonym(children.item(i),refMods, followingText);
                    names.add(myName);
                } catch (TransformerFactoryConfigurationError e) {
                    logger.warn(e);
                } catch (TransformerException e) {
                    logger.warn(e);
                }

            }
        }

        for(MyName name:names){
        	TaxonNameBase nameToBeFilled = name.getTaxonNameBase();
            Synonym synonym = name.getSyno();
            addFollowingTextToName(nameToBeFilled, followingText);

            /* INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
            nameToBeFilled = parser.parseFullName(name.getName(), nomenclaturalCode, name.getRank());
            if (nameToBeFilled.hasProblem() &&
                    !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                addProblemNameToFile(name.getName(),"",nomenclaturalCode,name.getRank());
                nameToBeFilled = solveNameProblem(name.getOriginalName(), name.getName(), parser,name.getAuthor(), name.getRank());
            }
            nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave,statusType);
             */
            if (!name.getIdentifier().isEmpty() && (name.getIdentifier().length()>2)){
                setLSID(name.getIdentifier(), synonym);
            }

            Set<Synonym> synonymsSet= acceptedTaxon.getSynonyms();
            boolean synoExist = false;
            for (Synonym syn: synonymsSet){

                boolean a =syn.getName().equals(synonym.getName());
                boolean b = syn.getSec().equals(synonym.getSec());
                if (a && b) {
                    synoExist=true;
                }
            }
            if (!synonymsSet.contains(synonym) && ! (synoExist)) {
                sourceHandler.addSource(refMods, synonym);
                acceptedTaxon.addSynonym(synonym, SynonymType.SYNONYM_OF());
            }
        }
        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
    }


    private boolean addFollowingTextToName(TaxonNameBase nameToBeFilled, String followingText) {
    	if (nameToBeFilled != null && StringUtils.isNotBlank(followingText)){
    		if (! followingText.matches("\\d\\.?")){

	    		if (followingText.startsWith(",")){
	    			followingText = followingText.substring(1).trim();
	    		}
	    		nameToBeFilled.setFullTitleCache(nameToBeFilled.getFullTitleCache()+ "," +followingText , true);
    		}
    		return true;
    	}
    	return false;

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
    @SuppressWarnings({ "rawtypes" })
    private Taxon extractReferences(Node refgroup, List<TaxonNameBase> nametosave, Taxon acceptedTaxon, Reference refMods) {
        logger.info("extractReferences");
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        NodeList children = refgroup.getChildNodes();
        NonViralName<?> nameToBeFilled = getNonViralNameAccNomenclature();

        ReferenceBuilder refBuild = new ReferenceBuilder(sourceHandler);
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:bibref")){
                String ref = children.item(i).getTextContent().trim();
                refBuild.builReference(ref, treatmentMainName, nomenclaturalCode,  acceptedTaxon, refMods);
                if (!refBuild.isFoundBibref()){
                    extractReferenceRawText(children.item(i).getChildNodes(), nameToBeFilled, refMods, acceptedTaxon);
                }
            }

            if(children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList references = children.item(i).getChildNodes();
                String descr="";
                for (int j=0;j<references.getLength();j++){
                    if(references.item(j).getNodeName().equalsIgnoreCase("tax:bibref")){
                        String ref = references.item(j).getTextContent().trim();
                        refBuild.builReference(ref, treatmentMainName,  nomenclaturalCode,  acceptedTaxon, refMods);
                    }
                    else
                        if (references.item(j).getNodeName().equalsIgnoreCase("#text")
                                && !references.item(j).getTextContent().trim().isEmpty()){
                            descr += references.item(j).getTextContent().trim();
                        }

                }
                if (!refBuild.isFoundBibref()){
                    //if it's not tagged, put it as row information.
                    //                    extractReferenceRawText(references, nameToBeFilled, nametosave, refMods, acceptedTaxon);
                    //then put it as a not markup feature if not empty
                    if (!stringIsEmpty(descr.trim())){
                        Feature currentFeature= getNotMarkedUpFeatureObject();
                        setParticularDescription(descr.trim(),acceptedTaxon,acceptedTaxon, refMods,currentFeature);
                    }
                }
            }
        }
        //        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;

    }

    /**
     * get the non viral name according to the current nomenclature
     * @return
     */

    private NonViralName<?> getNonViralNameAccNomenclature() {
    	return (NonViralName<?>)nomenclaturalCode.getNewTaxonNameInstance(null);
    }

    /**
     * @return the feature object for the category "not marked up"
     */
    private Feature getNotMarkedUpFeatureObject() {
    	// FIXME use getFeature(uuid ....)
        logger.info("getNotMarkedUpFeatureObject");
        Feature currentFeature = (Feature)importer.getTermService().find(NotMarkedUpUUID);
        if (currentFeature == null) {
            currentFeature=Feature.NewInstance(notMarkedUp, notMarkedUp, notMarkedUp);
            currentFeature.setUuid(NotMarkedUpUUID);
            //TODO use userDefined Feature Vocabulary
            Feature.DISTRIBUTION().getVocabulary().addTerm(currentFeature);
//            importer.getTermService().saveOrUpdate(currentFeature);
            importer.getVocabularyService().saveOrUpdate(currentFeature.getVocabulary());
        }
        return currentFeature;
    }

    /**
     * @param references
     * handle cases where the bibref are inside <p> and outside
     */
    @SuppressWarnings("rawtypes")
    private void extractReferenceRawText(NodeList references, NonViralName<?> nameToBeFilled, Reference refMods,
            Taxon acceptedTaxon) {
        logger.info("extractReferenceRawText");
        String refString="";
        currentMyName= new MyName(true);
        for (int j=0;j<references.getLength();j++){
            acceptedTaxon=CdmBase.deproxy(acceptedTaxon, Taxon.class);
            //no bibref tag inside
            //            System.out.println("references.item(j).getNodeName()"+references.item(j).getNodeName());
            if (references.item(j).getNodeName().equalsIgnoreCase("tax:name")){

                try {
                	String followingText = null;  //needs to be checked if follText is possible
                	//TODO create or not create?
                    currentMyName = extractScientificName(references.item(j), refMods, followingText);
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
               if (!currentMyName.getStatus().isEmpty()){
            	   String nomNovStatus = this.newNameStatus(currentMyName.getStatus());
	               	if (nomNovStatus != null){
	               		nameToBeFilled.setAppendedPhrase(nomNovStatus);
	               	}else{
	            	   try {
	                        NomenclaturalStatusType  statusType = nomStatusString2NomStatus(currentMyName.getStatus());
                            nameToBeFilled.addStatus(NomenclaturalStatus.NewInstance(statusType));
	                    } catch (UnknownCdmTypeException e) {
	                        addProblematicStatusToFile(currentMyName.getStatus());
	                        logger.warn("Problem with status");
	                    }
	               	}
                }

                String fullLineRefName = references.item(j).getTextContent().trim();
                int nameOrRefOrOther=2;
                nameOrRefOrOther=askIfNameContained(fullLineRefName);
                if (nameOrRefOrOther==0){
                    TaxonNameBase nameTBF = currentMyName.getTaxonNameBase();
                    Synonym synonym = Synonym.NewInstance(nameTBF, refMods);

                    Set<Synonym> synonymsSet= acceptedTaxon.getSynonyms();
                    //                    System.out.println(synonym.getName()+" -- "+synonym.getSec());
                    boolean synoExist = false;
                    for (Synonym syn: synonymsSet){
                        //                        System.out.println(syn.getName()+" -- "+syn.getSec());
                        boolean a =syn.getName().equals(synonym.getName());
                        boolean b = syn.getSec().equals(synonym.getSec());
                        if (a && b) {
                            synoExist=true;
                        }
                    }
                    if (!synonymsSet.contains(synonym) && ! (synoExist)) {
                        sourceHandler.addSource(refMods, synonym);

                        acceptedTaxon.addSynonym(synonym, SynonymType.SYNONYM_OF());
                    }
                }

                if (nameOrRefOrOther==1){
                    Reference re = ReferenceFactory.newGeneric();
                    re.setTitleCache(fullLineRefName, true);

                    /* TaxonNameBase nameTBF = parser.parseFullName(currentMyName.getName(), nomenclaturalCode, currentMyName.getRank());
                    if (nameTBF.hasProblem() &&
                            !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                        addProblemNameToFile(currentMyName.getName(),"",nomenclaturalCode,currentMyName.getRank());
                        nameTBF=solveNameProblem(currentMyName.getName(), currentMyName.getName(),parser,currentMyName.getAuthor(), currentMyName.getRank());
                    }
                    nameTBF = getTaxonNameBase(nameTBF,nametosave,statusType);
                     */
                    TaxonNameBase nameTBF = currentMyName.getTaxonNameBase();
                    Synonym synonym = Synonym.NewInstance(nameTBF, re);

                    Set<Synonym> synonymsSet= acceptedTaxon.getSynonyms();
                    //                    System.out.println(synonym.getName()+" -- "+synonym.getSec());
                    boolean synoExist = false;
                    for (Synonym syn: synonymsSet){
                        //                        System.out.println(syn.getName()+" -- "+syn.getSec());
                        boolean a =syn.getName().equals(synonym.getName());
                        boolean b = syn.getSec().equals(synonym.getSec());
                        if (a && b) {
                            synoExist=true;
                        }
                    }
                    if (!synonymsSet.contains(synonym) && ! (synoExist)) {
                        sourceHandler.addSource(refMods, synonym);

                        acceptedTaxon.addSynonym(synonym, SynonymType.SYNONYM_OF());
                    }

                }


                if (!currentMyName.getIdentifier().isEmpty() && (currentMyName.getIdentifier().length()>2)){
                    setLSID(currentMyName.getIdentifier(), acceptedTaxon);
                }
            }

            if(!currentMyName.getName().isEmpty()){
                //logger.info("acceptedTaxon and name: *"+acceptedTaxon.getTitleCache()+"*, *"+currentMyName.getName()+"*");
                if (acceptedTaxon.getTitleCache().split("sec")[0].trim().equalsIgnoreCase(currentMyName.getName().trim())){
                    Reference refS = ReferenceFactory.newGeneric();
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


                    if (!currentMyName.getIdentifier().isEmpty() && (currentMyName.getIdentifier().length()>2)){
                        setLSID(currentMyName.getIdentifier(), acceptedTaxon);

                    }

                    acceptedTaxon.getName().setNomenclaturalReference(refS);
                }else{
                    TaxonNameBase nameTBF = currentMyName.getTaxonNameBase();
                    Synonym synonym = null;
                    if (! currentMyName.getStatus().isEmpty()){
                    	String nomNovStatus = this.newNameStatus(currentMyName.getStatus());
                    	if (nomNovStatus != null){
                    		nameToBeFilled.setAppendedPhrase(nomNovStatus);
                    	}else{
	                    	try {
	                            NomenclaturalStatusType statusType = nomStatusString2NomStatus(currentMyName.getStatus());
	                            nameToBeFilled.addStatus(NomenclaturalStatus.NewInstance(statusType));
	                            synonym = Synonym.NewInstance(nameTBF, refMods);
	                        } catch (UnknownCdmTypeException e) {
	                            addProblematicStatusToFile(currentMyName.getStatus());
	                            logger.warn("Problem with status");
	                            synonym = Synonym.NewInstance(nameTBF, refMods);
	                            synonym.setAppendedPhrase(currentMyName.getStatus());
	                        }
                    	}
                    }else{
                        synonym =  Synonym.NewInstance(nameTBF, refMods);
                    }


                    if (!currentMyName.getIdentifier().isEmpty() && (currentMyName.getIdentifier().length()>2)){
                        setLSID(currentMyName.getIdentifier(), synonym);
                    }

                    Set<Synonym> synonymsSet= acceptedTaxon.getSynonyms();
                    //                    System.out.println(synonym.getName()+" -- "+synonym.getSec());
                    boolean synoExist = false;
                    for (Synonym syn: synonymsSet){
                        //                        System.out.println(syn.getName()+" -- "+syn.getSec());
                        boolean a =syn.getName().equals(synonym.getName());
                        boolean b = syn.getSec().equals(synonym.getSec());
                        if (a && b) {
                            synoExist=true;
                        }
                    }
                    if (!synonymsSet.contains(synonym) && ! (synoExist)) {
                        sourceHandler.addSource(refMods, synonym);

                        acceptedTaxon.addSynonym(synonym, SynonymType.SYNONYM_OF());
                    }
                }
            }
            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        }
    }



    /**
     * @param identifier
     * @param acceptedTaxon
     */
    @SuppressWarnings("rawtypes")
    private void setLSID(String identifier, TaxonBase<?> taxon) {
        //logger.info("setLSID");
        //        boolean lsidok=false;
        String id = identifier.split("__")[0];
        String source = identifier.split("__")[1];
        if (id.indexOf("lsid")>-1){
            try {
                LSID lsid = new LSID(id);
                taxon.setLsid(lsid);
                //                lsidok=true;
            } catch (MalformedLSIDException e) {
                logger.warn("Malformed LSID");
            }

        }

        //logger.info("search reference for LSID");
        //  if ((id.indexOf("lsid")<0) || !lsidok){
        //ADD ORIGINAL SOURCE ID EVEN IF LSID
        Reference re = null;
        Pager<Reference> references = importer.getReferenceService().findByTitle(Reference.class, source, MatchMode.EXACT, null, 1, null, null, null);
        if( references !=null && references.getCount()>0){
            re=references.getRecords().get(0);
        }
        //logger.info("search reference for LSID-end");
        if(re == null){
            re = ReferenceFactory.newGeneric();
            re.setTitleCache(source, true);
            importer.getReferenceService().saveOrUpdate(re);
        }
        re=CdmBase.deproxy(re, Reference.class);

        //logger.info("search source for LSID");
        Set<IdentifiableSource> sources = taxon.getSources();
        boolean lsidinsource=false;
        boolean urlinsource=false;
        for (IdentifiableSource src:sources){
            if (id.equalsIgnoreCase(src.getIdInSource()) && re.getTitleCache().equals(src.getCitation().getTitleCache())) {
                lsidinsource=true;
            }
            if (src.getIdInSource() == null && re.getTitleCache().equals(sourceUrlRef.getTitleCache())) {
                urlinsource=true;
            }
        }
        if(!lsidinsource) {
            taxon.addSource(OriginalSourceType.Import, id,null,re,null);
        }
        if(!urlinsource)
        {
            sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
            taxon.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
            // }
        }

    }

    /**
     * try to solve a parsing problem for a scientific name
     * @param original : the name from the OCR document
     * @param name : the tagged version
     * @param parser
     * @return the corrected TaxonNameBase
     */
    /*   @SuppressWarnings({ "unchecked", "rawtypes" })
    private TaxonNameBase<?,?> solveNameProblem(String original, String name, INonViralNameParser parser, String author, Rank rank) {
        Map<String,String> ato = namesMap.get(original);
        if (ato == null) {
            ato = namesMap.get(original+" "+author);
        }


        if (ato == null && rank.equals(Rank.UNKNOWN_RANK())){
            rank=askForRank(original, Rank.UNKNOWN_RANK(), nomenclaturalCode);
        }
        if (ato != null && rank.equals(Rank.UNKNOWN_RANK())){
            rank = getRank(ato);
        }
        //        TaxonNameBase<?,?> nameTBF = parser.parseFullName(name, nomenclaturalCode, rank);
        TaxonNameBase<?,?> nameTBF = parser.parseSimpleName(name, nomenclaturalCode, rank);
        //                logger.info("RANK: "+rank);
        int retry=0;
        List<ParserProblem> problems = nameTBF.getParsingProblems();
        for (ParserProblem pb:problems) {
            System.out.println(pb.toString());
        }
        while (nameTBF.hasProblem() && (retry <1) && !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank))){
            addProblemNameToFile(name,author,nomenclaturalCode,rank);
            String fullname=name;
            if(! skippQuestion) {
                fullname =  getFullReference(name,nameTBF.getParsingProblems());
            }
            if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)){
                nameTBF = TaxonNameBase.NewBotanicalInstance(null);
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
            if(author != null){
                if (name.indexOf(author)>-1) {
                    nameTBF = parser.parseSimpleName(name.substring(0, name.indexOf(author)), nomenclaturalCode, rank);
                } else {
                    nameTBF = parser.parseSimpleName(name, nomenclaturalCode, rank);
                }
                if (nameTBF.hasProblem()){
                    if (name.indexOf(author)>-1) {
                        addProblemNameToFile(name.substring(0, name.indexOf(author)),author,nomenclaturalCode,rank);
                    } else {
                        addProblemNameToFile(name,author,nomenclaturalCode,rank);
                    }
                    //                    System.out.println("TBF still has problems "+nameTBF.hasProblem());
                    problems = nameTBF.getParsingProblems();
                    for (ParserProblem pb:problems) {
                        System.out.println(pb.toString());
                    }
                    nameTBF.setFullTitleCache(name, true);
                }else{
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)) {
                        ((BotanicalName) nameTBF).setAuthorshipCache(currentMyName.getAuthor());
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)) {
                        ((ZoologicalName) nameTBF).setAuthorshipCache(currentMyName.getAuthor());
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)) {
                        ((BacterialName) nameTBF).setAuthorshipCache(currentMyName.getAuthor());
                    }
                }
                //                    logger.info("FULL TITLE CACHE "+name);
            }else{
                nameTBF.setFullTitleCache(name, true);
            }
        }
        return nameTBF;
    }

     */

    /**
     * @param nomenclatureNode: the XML nodes
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @return
     */
    @SuppressWarnings({ "rawtypes" })
    private Taxon extractNomenclature(Node nomenclatureNode,  List<TaxonNameBase> nametosave, Reference refMods) throws ClassCastException{
        refMods=CdmBase.deproxy(refMods, Reference.class);

        logger.info("extractNomenclature");
        NodeList children = nomenclatureNode.getChildNodes();
        String freetext="";
        Taxon acceptedTaxon = null;
        //   INonViralNameParser parser = NonViralNameParserImpl.NewInstance();

        //        String fullContent = nomenclatureNode.getTextContent();

        NomenclaturalStatusType statusType = null;
        String newNameStatus = null;
        //TODO
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:status")){
                String status = children.item(i).getTextContent().trim();

                if (!status.isEmpty()){
                	if (newNameStatus(status) != null){
                		newNameStatus = newNameStatus(status);
                    }else{
	                    try {
	                        statusType = nomStatusString2NomStatus(status);
	                    } catch (UnknownCdmTypeException e) {
	//                    	nomNovStatus;
	                    	addProblematicStatusToFile(status);
	                        logger.warn("Problem with status: " + status);
	                    }
                    }
                }
            }
        }

        boolean containsSynonyms=false;
        boolean wasSynonym = false;
        usedFollowingTextPrefix = null;  //reset

        for (int i=0; i<children.getLength(); i++){
        	Node childNode = children.item(i);
        	String childName = childNode.getNodeName();


        	//following text
        	followingText = null;
        	if ( i + 1 < children.getLength()){
            	Node followingTextNode = children.item(i +1);
            	if (followingTextNode.getNodeName().equals("#text") && !followingTextNode.getTextContent().matches("\\s*") ){
            		followingText = followingTextNode.getTextContent();
            	}
        	}

        	//traverse nodes
            if (childName.equalsIgnoreCase("#text")) {
                freetext = childNode.getTextContent().trim();
                if (usedFollowingTextPrefix != null && freetext.startsWith(usedFollowingTextPrefix)){
                	freetext = freetext.substring(usedFollowingTextPrefix.length());
                }
                usedFollowingTextPrefix = null;  //reset
            }else if (childName.equalsIgnoreCase("tax:collection_event")) {
                //                System.out.println("COLLECTION EVENT INSIDE NOMENCLATURE");
                extractMaterialsDirect(childNode, acceptedTaxon, refMods, "collection", currentMyName.getTaxonNameBase());
            }else if(childName.equalsIgnoreCase("tax:name")){
            	NonViralName<?> nameToBeFilled;
                //System.out.println("HANDLE FIRST NAME OF THE LIST");
                if(!containsSynonyms){
                	wasSynonym = false;

                	//System.out.println("I : "+i);
                    currentMyName = new MyName(false);
                    try {
                        currentMyName = extractScientificName(childNode, refMods, followingText);
                        treatmentMainName = currentMyName.getNewName();
                        originalTreatmentName = currentMyName.getOriginalName();

                    } catch (TransformerFactoryConfigurationError e1) {
                        throw new RuntimeException(e1);
                    } catch (TransformerException e1) {
                    	throw new RuntimeException(e1);
                    }

                    if (currentMyName.getRank().equals(Rank.UNKNOWN_RANK()) || currentMyName.getRank().isLower(state2.getConfig().getMaxRank()) || currentMyName.getRank().equals(state2.getConfig().getMaxRank())){
                        maxRankRespected=true;

                        nameToBeFilled=currentMyName.getTaxonNameBase();

                        //   acceptedTaxon = importer.getTaxonService().findBestMatchingTaxon(treatmentMainName);
                        acceptedTaxon=currentMyName.getTaxon();
                        //System.out.println("TreatmentName "+treatmentMainName+" - "+acceptedTaxon);


                        boolean statusMatch=false;
                        if(acceptedTaxon !=null ){
                            acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
                            statusMatch=compareStatus(acceptedTaxon, statusType);
                            //System.out.println("statusMatch: "+statusMatch);
                        }
                        if (acceptedTaxon ==null || (acceptedTaxon != null && !statusMatch)){

                            nameToBeFilled=currentMyName.getTaxonNameBase();
                            if (nameToBeFilled != null){
                                if (!originalTreatmentName.isEmpty()) {
                                    TaxonNameDescription td = TaxonNameDescription.NewInstance();
                                    td.setTitleCache(originalTreatmentName, true);
                                    nameToBeFilled.addDescription(td);
                                }

                                if(statusType != null) {
                                    nameToBeFilled.addStatus(NomenclaturalStatus.NewInstance(statusType));
                                }
                                if(newNameStatus != null){
                                	nameToBeFilled.setAppendedPhrase(newNameStatus);
                                }
                                sourceHandler.addSource(refMods, nameToBeFilled);

                                if (nameToBeFilled.getNomenclaturalReference() == null) {
                                    acceptedTaxon= Taxon.NewInstance(nameToBeFilled,refMods);
                                    //System.out.println("NEW ACCEPTED HERE "+nameToBeFilled);
                                }
                                else {
                                    acceptedTaxon= Taxon.NewInstance(nameToBeFilled,(Reference) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
                                    //System.out.println("NEW ACCEPTED HERE2 "+nameToBeFilled);
                                }

                                sourceHandler.addSource(refMods, acceptedTaxon);

                                if(!state2.getConfig().doKeepOriginalSecundum()) {
                                    acceptedTaxon.setSec(state2.getConfig().getSecundum());
                                    //logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
                                    //System.out.println("SET SECUNDUM "+configState.getConfig().getSecundum());
                                }

                                if (!currentMyName.getIdentifier().isEmpty() && (currentMyName.getIdentifier().length()>2)){
                                    setLSID(currentMyName.getIdentifier(), acceptedTaxon);
                                }


                                importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                                acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
                            }

                        }else{
                            acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
                            Set<IdentifiableSource> sources = acceptedTaxon.getSources();
                            boolean sourcelinked=false;
                            for (IdentifiableSource source:sources){
                                if (source.getCitation().getTitleCache().equalsIgnoreCase(refMods.getTitleCache())) {
                                    sourcelinked=true;
                                }
                            }
                            if (!state2.getConfig().doKeepOriginalSecundum()) {
                                acceptedTaxon.setSec(state2.getConfig().getSecundum());
                                //logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
                                //System.out.println("SET SECUNDUM "+configState.getConfig().getSecundum());
                            }
                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);

                            if (!sourcelinked){
                                sourceHandler.addSource(refMods, acceptedTaxon);
                            }
                            if (!sourcelinked || !state2.getConfig().doKeepOriginalSecundum()){

                                if (!currentMyName.getIdentifier().isEmpty() && (currentMyName.getIdentifier().length()>2)){
                                    //FIXME are these identifiers really related to taxa, not names? Exiting LSIDs come from Zoobank, urn:lsid:biosci.ohio-state.edu:osuc_concepts:134826 (Ants)
                                	setLSID(currentMyName.getIdentifier(), acceptedTaxon);
                                }
                                importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                            }
                        }
                    }else{
                        maxRankRespected=false;
                    }
                    containsSynonyms=true;  //all folowing names are handled as synonyms
                }else{
                    try{
                        extractSynonyms(childNode, acceptedTaxon, refMods, followingText);
                        wasSynonym = true;

                    }catch(NullPointerException e){
                        logger.warn("null pointer exception, the accepted taxon might be null");
                    }
                }
                containsSynonyms=true;
            }else if (childName.equalsIgnoreCase("tax:ref_group") && maxRankRespected){
                reloadClassification();
                //extract the References within the document
                extractReferences(childNode,nametosave,acceptedTaxon,refMods);
            }else if (childName.equalsIgnoreCase("tax:bibref")){
            	logger.warn(childName + " still preliminary");

            	NonViralName<?> currentName = currentMyName == null ? null : currentMyName.getTaxonNameBase();
            	boolean handled = addFollowingTextToName (currentName, childNode.getTextContent() );
            	if (! handled){
            		setParticularDescription(freetext.trim(), acceptedTaxon,acceptedTaxon, refMods, getNotMarkedUpFeatureObject());
            	}
            }else{
            	logger.warn(childName + " not yet handled");
            }
            if(!stringIsEmpty(freetext.trim())) {;
                if (! freetext.matches("\\d\\.?")){
                	NonViralName<?> currentName = currentMyName == null ? null : currentMyName.getTaxonNameBase();
                	boolean handled = false;
                	if (currentName != null && !wasSynonym){
                		handled = addFollowingTextToName (currentName, childNode.getTextContent() );
                	}
                	if (! handled){
                		setParticularDescription(freetext.trim(), acceptedTaxon,acceptedTaxon, refMods, getNotMarkedUpFeatureObject());
                	}
                }

                 freetext = "";
            }

        }
        //importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;
    }




	/**
     * @return
     */

    private boolean compareStatus(TaxonBase<?> t, NomenclaturalStatusType statusType) {
        //logger.info("compareStatus");
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
    /*  private Taxon createParent(Taxon acceptedTaxon, Reference ref) {
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
        if(!skippQuestion){
            int addTaxon = askAddParent(s);
            logger.info("ADD TAXON: "+addTaxon);
            if (addTaxon == 0 ){
                Taxon tmp = askParent(acceptedTaxon, classification);
                if (tmp == null){
                    s = askSetParent(s);
                    r = askRank(s,rankListStr);

                    TaxonNameBase<?,?> nameToBeFilled = null;
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)){
                        nameToBeFilled = TaxonNameBase.NewBotanicalInstance(null);
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                        nameToBeFilled = ZoologicalName.NewInstance(null);
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)){
                        nameToBeFilled = BacterialName.NewInstance(null);
                    }
                    nameToBeFilled.setTitleCache(s, true);
                    nameToBeFilled.setRank(getRank(r), true);

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
        } else{
            classification.addChildTaxon(acceptedTaxon, ref, null);
            tax=acceptedTaxon;
        }
        //        logger.info("RETURN: "+tax );
        return tax;

    }

     */


    private MyName  extractScientificNameSynonym(Node name, Reference refMods, String followingText) throws TransformerFactoryConfigurationError, TransformerException {
        //System.out.println("extractScientificNameSynonym");
        logger.info("extractScientificNameSynonym");
        String[] rankListToPrint_tmp ={"dwc:genus","dwc:specificepithet","dwc:species","dwc:subspecies", "dwc:infraspecificepithet","dwc:scientificnameauthorship"};
        List<String> rankListToPrint = new ArrayList<String>();
        for (String r : rankListToPrint_tmp) {
            rankListToPrint.add(r.toLowerCase());
        }

        Rank rank = Rank.UNKNOWN_RANK();
        NodeList children = name.getChildNodes();
        String originalName="";
        String fullName = "";
        String newName="";
        String identifier="";
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        List<String> atomisedName= new ArrayList<String>();

        String rankStr = "";
        Rank tmpRank ;

        String status= extractStatus(children);

        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:xmldata")){
                NodeList atom = children.item(i).getChildNodes();
                for (int k=0;k<atom.getLength();k++){
                    identifier = extractIdentifier(identifier, atom.item(k));
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
                }
                addAtomisedNamesToMap(rankListToPrint, rank, atomisedName, atom);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("#text") && !StringUtils.isBlank(children.item(i).getTextContent())){
                //                logger.info("name non atomised: "+children.item(i).getTextContent());
                fullName = children.item(i).getTextContent().trim();
                //                logger.info("fullname: "+fullName);
            }
        }
        originalName=fullName;
        fullName = cleanName(fullName, atomisedName);
        namesMap.put(fullName,atomisedMap);

        String atomisedNameStr = getAtomisedNameStr(atomisedName);

        if (fullName != null){
            //            System.out.println("fullname: "+fullName);
            //            System.out.println("atomised: "+atomisedNameStr);
            if (!fullName.equalsIgnoreCase(atomisedNameStr)) {
                if (skippQuestion){
                    //                    String defaultN = "";
                    if (atomisedNameStr.length()>fullName.length()) {
                        newName=atomisedNameStr;
                    } else {
                        if (fullName.length()>atomisedNameStr.length() && (rank.isLower(Rank.SPECIES()) && fullName.length()>2 && !fullName.substring(0, 1).equals("."))) {
                            newName=askWhichScientificName(fullName,atomisedNameStr,classification.getTitleCache(),name);
                        } else {
                            newName=fullName;
                        }
                    }
                } else {
                    newName=askWhichScientificName(fullName,atomisedNameStr,classification.getTitleCache(),name);
                }
            } else {
                newName=fullName;
            }
        }
        //not really needed
        //        rank = askForRank(newName, rank, nomenclaturalCode);
        //        System.out.println("atomised: "+atomisedMap.toString());

        //        String[] names = new String[5];
        MyName myname = new MyName(true);

        //System.out.println("Handle "+newName+ "(rank: "+rank+")");
        //        System.out.println(atomisedMap.keySet());
        fullName = extractAuthorFromNames(rank, fullName, atomisedMap, myname);
        myname.setOriginalName(fullName);
        myname.setNewName(newName);
        myname.setRank(rank);
        myname.setIdentifier(identifier);
        myname.setStatus(status);
        myname.setSource(refMods);

        //        boolean higherAdded=false;


        boolean parseNameManually=false;
        INonViralNameParser<?> parser = NonViralNameParserImpl.NewInstance();
        ITaxonNameBase  nameToBeFilledTest ;

        //if selected the atomised version
        if(newName==atomisedNameStr){
            nameToBeFilledTest = parseWithExtension(parser, atomisedNameStr, rank, followingText, atomisedMap);
            if (nameToBeFilledTest.hasProblem()){
                addProblemNameToFile("ato",atomisedNameStr,nomenclaturalCode,rank, nameToBeFilledTest.getParsingProblems().toString());
                nameToBeFilledTest = parser.parseFullName(fullName, nomenclaturalCode, rank);
                if (nameToBeFilledTest.hasProblem()){
                    addProblemNameToFile("full",fullName,nomenclaturalCode,rank, nameToBeFilledTest.getParsingProblems().toString());
                    parseNameManually=true;
                }
            }
        }else{
            nameToBeFilledTest = parseWithExtension(parser, atomisedNameStr, rank, followingText, atomisedMap);
            if (nameToBeFilledTest.hasProblem()){
                addProblemNameToFile("fullversion",fullName, nomenclaturalCode,rank, nameToBeFilledTest.getParsingProblems().toString());
                nameToBeFilledTest = parser.parseFullName(fullName, nomenclaturalCode,rank);
                parseNameManually=true;
                if(!originalName.equalsIgnoreCase(atomisedNameStr)) {
                    addNameDifferenceToFile(originalName,atomisedNameStr);
                }
            }
        }

        if(parseNameManually){
            //System.out.println("DO IT MANUALLY");
        	if (this.state2.getConfig().isUseOldUnparsedSynonymExtraction()){
                createUnparsedSynonym(rank, newName, atomisedMap, myname);
        	}else{
        		createUnparsedSynonymNew(rank, newName, atomisedMap, myname, refMods);;
        	}
        } else{
            //System.out.println("AUTOMATIC!");
            //            createAtomisedTaxonString(newName, atomisedMap, myname);
            myname.setParsedName(nameToBeFilledTest);
            myname.buildTaxon();
        }
        //System.out.println("RETURN SYNONYM "+myname.getSyno().toString());
        return myname;
    }


	/**
     * @param name
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     * @return a list of possible names
     */
    @SuppressWarnings({"rawtypes" })
    private MyName extractScientificName(Node name, Reference refMods, String followingText) throws TransformerFactoryConfigurationError, TransformerException {
        logger.info("extractScientificName");

        String[] rankListToPrintLowerCase_tmp ={"dwc:genus","dwc:specificepithet","dwc:species","dwc:subspecies", "dwc:infraspecificepithet","dwc:scientificnameauthorship"};
        List<String> rankListToPrint = Arrays.asList(rankListToPrintLowerCase_tmp);

        Rank rank = Rank.UNKNOWN_RANK();
        NodeList children = name.getChildNodes();
        String originalName = "";
        String fullName = "";
        String newName = "";
        String identifier = "";
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        List<String> atomisedNameList= new ArrayList<String>();

        String status= extractStatus(children);

        for (int i=0;i<children.getLength();i++){
        	Node nameChild = children.item(i);
            if(nameChild.getNodeName().equalsIgnoreCase("tax:xmldata")){
                NodeList xmlDataChildren = nameChild.getChildNodes();
                for (int k=0;k<xmlDataChildren.getLength();k++){
                	Node xmlDataChild = xmlDataChildren.item(k);
                    identifier = extractIdentifier(identifier, xmlDataChild);
                    String rankStr = xmlDataChild.getNodeName().toLowerCase();
                    if (rankStr.equalsIgnoreCase("dwc:taxonRank")) {
                        rankStr=xmlDataChild.getTextContent().trim();
                        Rank tmpRank = getRank(rankStr);
                        if (tmpRank != null){
                            rank=tmpRank;
                        }
                    }
                    //                    if ((tmpRank != null) && (tmpRank.isLower(rank) || rank.equals(Rank.UNKNOWN_RANK()))) {

                    atomisedMap.put(rankStr.toLowerCase(),xmlDataChild.getTextContent().trim());
                }
                addAtomisedNamesToMap(rankListToPrint, rank, atomisedNameList, xmlDataChildren);
            }
            else if(nameChild.getNodeName().equalsIgnoreCase("#text") && ! nameChild.getTextContent().matches("\\s*")){
                //                logger.info("name non atomised: "+children.item(i).getTextContent());
                fullName = nameChild.getTextContent().trim();
                //                logger.info("fullname: "+fullName);
            }
        }
        originalName=fullName;
        fullName = cleanName(fullName, atomisedNameList);
        namesMap.put(fullName,atomisedMap);

        String atomisedNameStr = getAtomisedNameStr(atomisedNameList);

        if (fullName != null){
            if (!fullName.equalsIgnoreCase(atomisedNameStr)) {
                if (skippQuestion){
                    if (atomisedNameStr.length()>fullName.length()) {
                        newName = atomisedNameStr;
                    } else {
                        if (fullName.length()>atomisedNameStr.length() && (rank.isLower(Rank.SPECIES()) && fullName.length()>2 && !fullName.substring(0, 1).equals("."))) {
                            newName = askWhichScientificName(fullName, atomisedNameStr, classification.getTitleCache(), name);
                        } else {
                            newName = fullName;
                        }
                    }
                } else {
                    newName=askWhichScientificName(fullName, atomisedNameStr, classification.getTitleCache(), name);
                }
            } else {
                newName=fullName;
            }
        }
        //not really needed
        //        rank = askForRank(newName, rank, nomenclaturalCode);
        //        System.out.println("atomised: "+atomisedMap.toString());

        //        String[] names = new String[5];
        MyName myname = new MyName(false);

        //System.out.println("\n\nBUILD "+newName+ "(rank: "+rank+")");
        //        System.out.println(atomisedMap.keySet());
        fullName = extractAuthorFromNames(rank, fullName, atomisedMap, myname);
        myname.setOriginalName(fullName);
        myname.setNewName(newName);

        myname.setRank(rank);
        myname.setIdentifier(identifier);
        myname.setStatus(status);
        myname.setSource(refMods);

        //        boolean higherAdded=false;


        boolean parseNameManually=false;
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
        ITaxonNameBase  nameToBeFilledTest = null;

        //if selected the atomised version
        if(newName==atomisedNameStr){
            nameToBeFilledTest = parseWithExtension(parser, atomisedNameStr, rank, followingText, atomisedMap);
            if (nameToBeFilledTest.hasProblem()){
        	    addProblemNameToFile("ato",atomisedNameStr,nomenclaturalCode,rank, nameToBeFilledTest.getParsingProblems().toString());
                nameToBeFilledTest = parser.parseFullName(fullName, nomenclaturalCode,rank);
                if (nameToBeFilledTest.hasProblem()){
                    addProblemNameToFile("full",fullName,nomenclaturalCode,rank, nameToBeFilledTest.getParsingProblems().toString());
                    parseNameManually=true;
                }
            }
        }else{
            nameToBeFilledTest = parseWithExtension(parser, fullName , rank, followingText, atomisedMap);
            if (nameToBeFilledTest.hasProblem()){
                addProblemNameToFile("fullversion",fullName,nomenclaturalCode,rank, nameToBeFilledTest.getParsingProblems().toString());
                nameToBeFilledTest = parser.parseFullName(fullName, nomenclaturalCode,rank);
                parseNameManually=true;
                if(!originalName.equalsIgnoreCase(atomisedNameStr)) {
                    addNameDifferenceToFile(originalName,atomisedNameStr);
                }
            }
        }

        //System.out.println("parseNameManually: "+parseNameManually);
        if(parseNameManually){
            createAtomisedTaxon(rank, newName, atomisedMap, myname);
        }
        else{
            createAtomisedTaxonString(newName, atomisedMap, myname);
            myname.setParsedName(nameToBeFilledTest);
            //TODO correct handling of createIfNotExists
           	myname.buildTaxon();
        }
        return myname;

    }

    private ITaxonNameBase parseWithExtension(INonViralNameParser parser, String atomisedNameStr, Rank rank, String followingText, HashMap<String, String> atomisedMap) {
    	Object[] nameExtensionResult = getPossibleExtension(followingText, atomisedMap, nomenclaturalCode);

    	ITaxonNameBase name = parser.parseFullName(atomisedNameStr, nomenclaturalCode, rank);
    	if (nameExtensionResult != null && nameExtensionResult[0] != null){
    		String ext = (String)nameExtensionResult[0];
    		ITaxonNameBase extName =parser.parseFullName(atomisedNameStr + " " + ext, nomenclaturalCode, rank);
    		if (! extName.hasProblem()){
    			name = extName;
    			this.usedFollowingTextPrefix = ext;
    			//TODO do we need to fill the atomisedMap at all?
    			if ((Boolean)(nameExtensionResult[1])){
    				//TODO
    			}
    			if ((Boolean)(nameExtensionResult[2])){
    				//TODO BasionymYear etc.
    				Integer origYear = ((ZoologicalName)name).getPublicationYear();
    				if (origYear != null){
        				atomisedMap.put(PUBLICATION_YEAR, origYear.toString());
    				}
    			}
    		}
    	}
		return name;
	}

	private Object[] getPossibleExtension(String followingText, HashMap<String, String> atomisedMap, NomenclaturalCode nomenclaturalCode) {
		if (StringUtils.isBlank(followingText)){
			return null;
		}

    	boolean includeAuthor = true;
    	boolean includeYear = false;
		if (atomisedMap.containsKey("dwc:scientificnameauthorship")){
			includeAuthor = false;
		}
    	if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
    		includeYear = true;
    	}
    	String patternStr = "";
    	if (includeAuthor){
    		patternStr += NonViralNameParserImplRegExBase.capitalWord;
    	}
    	if (includeYear){
    		patternStr += "\\s*(,|\\s+)\\s*" + "(17|18|19|20)" + "\\d{2}" ;
    	}
    	String match = null;
    	if (! patternStr.isEmpty()){
    		Pattern pattern = Pattern.compile("^" + patternStr);
    		Matcher matcher = pattern.matcher(followingText.trim());
    		if (matcher.find()){
    			match = matcher.group();
    		}
    	}

		return new Object[]{match, includeAuthor, includeYear};
	}

	/**
     * @param atomisedName
     * @return
     */
    private String getAtomisedNameStr(List<String> atomisedName) {
        //logger.info("getAtomisedNameStr");
        String atomisedNameStr = StringUtils.join(atomisedName," ");
        while(atomisedNameStr.contains("  ")) {
            atomisedNameStr=atomisedNameStr.replace("  ", " ");
        }
        atomisedNameStr=atomisedNameStr.trim();
        return atomisedNameStr;
    }

    /**
     * @param children
     * @param status
     * @return
     */
    private String extractStatus(NodeList children) {
        logger.info("extractStatus");
        String status="";
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:status") ||
                    (children.item(i).getNodeName().equalsIgnoreCase("tax:namePart") &&
                            children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("status"))){
                status = children.item(i).getTextContent().trim();
            }
        }
        return status;
    }

    /**
     * @param identifier
     * @param atom
     * @param k
     * @return
     */
    private String extractIdentifier(String identifier, Node atom) {
        //logger.info("extractIdentifier");
        if (atom.getNodeName().equalsIgnoreCase("tax:xid")){
            try{
                identifier = atom.getAttributes().getNamedItem("identifier").getNodeValue();
            }catch(Exception e){
                System.out.println("pb with identifier, maybe empty");
            }
            try{
                identifier+="__"+atom.getAttributes().getNamedItem("source").getNodeValue();
            }catch(Exception e){
                System.out.println("pb with identifier, maybe empty");
            }
        }
        return identifier;
    }

    /**
     * @param rankListToPrint
     * @param rank
     * @param atomisedName
     * @param atom
     */
    private void addAtomisedNamesToMap(List<String> rankListToPrint, Rank rank, List<String> atomisedName, NodeList atom) {
        logger.info("addAtomisedNamesToMap");
        for (int k=0;k<atom.getLength();k++){
        	Node node = atom.item(k);
        	String nodeName = node.getNodeName();
            if (! nodeName.equalsIgnoreCase("dwc:taxonRank") ) {  //rank has been handled in higher method
                if (nodeName.equalsIgnoreCase("dwc:subgenus") || nodeName.equalsIgnoreCase("dwcranks:subgenus")) {
                    atomisedName.add("("+ node.getTextContent().trim()+")");
                } else if(nodeName.equalsIgnoreCase("dwcranks:varietyepithet") || nodeName.equalsIgnoreCase("dwc:Subspecies") || nodeName.equalsIgnoreCase("dwc:infraspecificepithet")) {
                       	if(nodeName.equalsIgnoreCase("dwcranks:varietyepithet")){
                            atomisedName.add("var. "+node.getTextContent().trim());
                        }else if(nodeName.equalsIgnoreCase("dwc:Subspecies") || nodeName.equalsIgnoreCase("dwc:infraspecificepithet")) {
                            atomisedName.add("subsp. "+atom.item(k).getTextContent().trim());
                        }
                } else if(rankListToPrint.contains(nodeName.toLowerCase())) {
                    atomisedName.add(node.getTextContent().trim());
                } else{
                    if (rank.isHigher(Rank.GENUS()) && (nodeName.indexOf("dwcranks:")>-1 || nodeName.indexOf("dwc:Family")>-1)) {
                        atomisedName.add(node.getTextContent().trim());
                    }else if (nodeName.equals("#text")){
                    	String text = node.getTextContent();
                    	if (StringUtils.isNotBlank(text)){
                    		//TODO handle text
                    		logger.warn("name xmldata contains text. This is unhandled");
                    	}
                    }else if (nodeName.matches("(?i)(dwc:Kingdom|dwc:Class|dwc:Order|dwc:Family)")){
                    	//we currently do not use higher ranks information
                    }else{
                    	//TODO handle unhandled node
                    	logger.warn("Unhandled node: " + nodeName);
                    }
                }
            }
        }
    }

    /**
     * @param fullName
     * @param atomisedName
     * @return
     */
    private String cleanName(String name, List<String> atomisedName) {
        //logger.info("cleanName");
        String fullName =name;
        if (fullName != null){
            fullName = fullName.replace("( ", "(");
            fullName = fullName.replace(" )",")");

            if (fullName.trim().isEmpty()){
                fullName=StringUtils.join(atomisedName," ");
            }

            while(fullName.contains("  ")) {
                fullName=fullName.replace("  ", " ");
                //            logger.info("while");
            }
            fullName=fullName.trim();
        }
        return fullName;
    }

    /**
     * @param rank
     * @param fullName
     * @param atomisedMap
     * @param myname
     * @return
     */
    private String extractAuthorFromNames(Rank rank, String name, HashMap<String, String> atomisedMap, MyName myname) {
        logger.info("extractAuthorFromNames");
        String fullName=name;
        if (atomisedMap.get("dwc:scientificnameauthorship") == null && fullName!=null){
            //            System.out.println("rank : "+rank.toString());
            if(rank.isHigher(Rank.SPECIES())){
                try{
                    String author=null;
                    if(atomisedMap.get("dwcranks:subgenus") != null) {
                        author = fullName.split(atomisedMap.get("dwcranks:subgenus"))[1].trim();
                    }
                    if(atomisedMap.get("dwc:subgenus") != null) {
                        author = fullName.split(atomisedMap.get("dwc:subgenus"))[1].trim();
                    }
                    if(author == null) {
                        if(atomisedMap.get("dwc:genus") != null) {
                            author = fullName.split(atomisedMap.get("dwc:genus"))[1].trim();
                        }
                    }
                    if(author != null){
                        fullName = fullName.substring(0, fullName.indexOf(author));
                        author=author.replaceAll(",","").trim();
                        myname.setAuthor(author);
                    }
                }catch(Exception e){
                    //could not extract the author
                }
            }
            if(rank.equals(Rank.SPECIES())){
                try{
                    String author=null;
                    if(author == null) {
                        if(atomisedMap.get("dwc:species") != null) {
                            String[] t = fullName.split(atomisedMap.get("dwc:species"));
                            //                            System.out.println("NB ELEMENTS "+t.length +"fullName "+fullName+", "+atomisedMap.get("dwc:species"));
                            author = fullName.split(atomisedMap.get("dwc:species"))[1].trim();
                            //                            System.out.println("AUTEUR "+author);
                        }
                    }
                    if(author != null){
                        fullName = fullName.substring(0, fullName.indexOf(author));
                        author=author.replaceAll(",","").trim();
                        myname.setAuthor(author);
                    }
                }catch(Exception e){
                    //could not extract the author
                }
            }
        }else{
            myname.setAuthor(atomisedMap.get("dwc:scientificnameauthorship"));
        }
        return fullName;
    }

    /**
     * @param newName
     * @param atomisedMap
     * @param myname
     */
    private void createAtomisedTaxonString(String newName, HashMap<String, String> atomisedMap, MyName myname) {
        logger.info("createAtomisedTaxonString "+atomisedMap);
        if(atomisedMap.get("dwc:family") != null && checkRankValidForImport(Rank.FAMILY())){
            myname.setFamilyStr(atomisedMap.get("dwc:family"));
        }
        if(atomisedMap.get("dwcranks:subfamily") != null  && checkRankValidForImport(Rank.SUBFAMILY())){
            myname.setSubfamilyStr(atomisedMap.get("dwcranks:subfamily"));
        }
        if(atomisedMap.get("dwcranks:tribe") != null && checkRankValidForImport(Rank.TRIBE())){
            myname.setTribeStr(atomisedMap.get("dwcranks:tribe"));
        }
        if(atomisedMap.get("dwcranks:subtribe") != null && checkRankValidForImport(Rank.SUBTRIBE())){
            myname.setSubtribeStr(atomisedMap.get("dwcranks:subtribe"));
        }
        if(atomisedMap.get("dwc:genus") != null && checkRankValidForImport(Rank.GENUS())){
            myname.setGenusStr(atomisedMap.get("dwc:genus"));
        }
        if(atomisedMap.get("dwcranks:subgenus") != null && checkRankValidForImport(Rank.SUBGENUS())){
            myname.setSubgenusStr(atomisedMap.get("dwcranks:subgenus"));
        }
        if(atomisedMap.get("dwc:subgenus") != null && checkRankValidForImport(Rank.SUBGENUS())){
            myname.setSubgenusStr(atomisedMap.get("dwc:subgenus"));
        }
        if(atomisedMap.get("dwc:species") != null && checkRankValidForImport(Rank.SPECIES())){
            String n=newName;
            if(atomisedMap.get("dwc:infraspecificepithet") != null) {
                n=newName.split(atomisedMap.get("dwc:infraspecificepithet"))[0];
                n=n.replace("subsp.","");
            }
            if(atomisedMap.get("dwc:subspecies") != null) {
                n=newName.split(atomisedMap.get("dwc:subspecies"))[0];
                n=n.replace("subsp.","");
            }
            if(atomisedMap.get("dwcranks:varietyepithet") != null) {
                n=newName.split(atomisedMap.get("dwcranks:varietyepithet"))[0];
                n=n.replace("var.","");
                n=n.replace("v.","");
            }
            if(atomisedMap.get("dwcranks:formepithet") != null) {
                //TODO
                System.out.println("TODO FORMA");
                n=newName.split(atomisedMap.get("dwcranks:formepithet"))[0];
                n=n.replace("forma","");
            }
            n=n.trim();
            String author = myname.getAuthor();
            if(n.split(" ").length>2){

                String n2=n.split(" ")[0]+" "+n.split(" ")[1];
                String a= "";
                try{
                    a=n.split(n2)[1].trim();
                }catch(Exception e){
                    logger.info("no author in "+n+"?");}

                myname.setAuthor(a);
                //System.out.println("FINDCREATESPECIES --"+n2+"--"+n+"**"+a+"##");
                n=n2;

            }

            myname.setSpeciesStr(atomisedMap.get("dwc:species"));
            myname.setAuthor(author);
        }
        if(atomisedMap.get("dwc:subspecies") != null && checkRankValidForImport(Rank.SUBSPECIES())){
            myname.setSubspeciesStr(atomisedMap.get("dwc:subspecies"));
        }
        if(atomisedMap.get("dwc:infraspecificepithet") != null && checkRankValidForImport(Rank.SUBSPECIES())){
            myname.setSubspeciesStr(atomisedMap.get("dwc:infraspecificepithet"));
        }
        if(atomisedMap.get("dwcranks:varietyepithet") != null && checkRankValidForImport(Rank.VARIETY())){
            myname.setVarietyStr(atomisedMap.get("dwcranks:varietyepithet"));
        }
        if(atomisedMap.get("dwcranks:formepithet") != null && checkRankValidForImport(Rank.FORM())){
            myname.setFormStr(atomisedMap.get("dwcranks:formepithet"));
        }
        if (atomisedMap.get(PUBLICATION_YEAR) != null){
        	myname.setPublicationYear(Integer.valueOf(atomisedMap.get(PUBLICATION_YEAR)));
        }
    }

    /**
     * @see #createUnparsedSynonymNew(Rank, String, HashMap, MyName)
     * @param rank
     * @param newName
     * @param atomisedMap
     * @param myname
     */
    private void createUnparsedSynonym(Rank rank, String newName, HashMap<String, String> atomisedMap, MyName myname) {
        logger.info("createSynonym");
        //System.out.println("createsynonym");
        if(rank.equals(Rank.UNKNOWN_RANK())){
            myname.setNotParsableTaxon(newName);
        }else{
	        if(atomisedMap.get("dwc:family") != null && checkRankValidForImport(Rank.FAMILY()) && rank.equals(Rank.FAMILY())){
	            myname.setFamily(myname.findOrCreateTaxon(atomisedMap.get("dwc:family"),newName, Rank.FAMILY(),rank));
	        }
	        if(atomisedMap.get("dwcranks:subfamily") != null  && checkRankValidForImport(Rank.SUBFAMILY()) && rank.equals(Rank.SUBFAMILY())){
	            myname.setSubfamily(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:subfamily"), newName,Rank.SUBFAMILY(),rank));
	        }
	        if(atomisedMap.get("dwcranks:tribe") != null && checkRankValidForImport(Rank.TRIBE()) && rank.equals(Rank.TRIBE())){
	            myname.setTribe(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:tribe"),newName, Rank.TRIBE(),rank));
	        }
	        if(atomisedMap.get("dwcranks:subtribe") != null && checkRankValidForImport(Rank.SUBTRIBE()) && rank.equals(Rank.SUBTRIBE())){
	            myname.setSubtribe(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:subtribe"),newName, Rank.SUBTRIBE(),rank));
	        }
	        if(atomisedMap.get("dwc:genus") != null && checkRankValidForImport(Rank.GENUS()) && rank.equals(Rank.GENUS())){
	            myname.setGenus(myname.findOrCreateTaxon(atomisedMap.get("dwc:genus"),newName, Rank.GENUS(),rank));
	        }
	        if(atomisedMap.get("dwcranks:subgenus") != null && checkRankValidForImport(Rank.SUBGENUS()) && rank.equals(Rank.SUBGENUS())){
	            myname.setSubgenus(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:subgenus"),newName, Rank.SUBGENUS(),rank));
	        }
	        if(atomisedMap.get("dwc:subgenus") != null && checkRankValidForImport(Rank.SUBGENUS()) && rank.equals(Rank.SUBGENUS())){
	            myname.setSubgenus(myname.findOrCreateTaxon(atomisedMap.get("dwc:subgenus"),newName, Rank.SUBGENUS(),rank));
	        }
	        if(atomisedMap.get("dwc:species") != null && checkRankValidForImport(Rank.SPECIES()) && rank.equals(Rank.SPECIES())){
	            String n=newName;
	            if(atomisedMap.get("dwc:infraspecificepithet") != null) {
	                n=newName.split(atomisedMap.get("dwc:infraspecificepithet"))[0];
	                n=n.replace("subsp.","");
	            }
	            if(atomisedMap.get("dwc:subspecies") != null) {
	                n=newName.split(atomisedMap.get("dwc:subspecies"))[0];
	                n=n.replace("subsp.","");
	            }
	            if(atomisedMap.get("dwcranks:varietyepithet") != null) {
	                n=newName.split(atomisedMap.get("dwcranks:varietyepithet"))[0];
	                n=n.replace("var.","");
	                n=n.replace("v.","");
	            }
	            if(atomisedMap.get("dwcranks:formepithet") != null) {
	                //TODO
	                //System.out.println("TODO FORMA");
	                n=newName.split(atomisedMap.get("dwcranks:formepithet"))[0];
	                n=n.replace("forma","");
	            }
	            n=n.trim();
	            String author = myname.getAuthor();
	            if(n.split(" ").length>2){

	                String n2=n.split(" ")[0]+" "+n.split(" ")[1];
	                String a="";
	                try{
	                    a= n.split(n2)[1].trim();
	                }catch(Exception e){logger.info("no author in "+n);}
	                myname.setAuthor(a);
	                //System.out.println("FINDCREATESPECIES --"+n2+"--"+n+"**"+a+"##");
	                n=n2;

	            }
	            Taxon species = myname.findOrCreateTaxon(atomisedMap.get("dwc:species"),n, Rank.SPECIES(),rank);
	            myname.setSpecies(species);
	            myname.setAuthor(author);
	        }
	        if(atomisedMap.get("dwc:subspecies") != null && checkRankValidForImport(Rank.SUBSPECIES()) && rank.equals(Rank.SUBSPECIES())){
	            myname.setSubspecies(myname.findOrCreateTaxon(atomisedMap.get("dwc:subspecies"), newName,Rank.SUBSPECIES(),rank));
	        }
	        if(atomisedMap.get("dwc:infraspecificepithet") != null && checkRankValidForImport(Rank.SUBSPECIES()) && rank.equals(Rank.SUBSPECIES())){
	            myname.setSubspecies(myname.findOrCreateTaxon(atomisedMap.get("dwc:infraspecificepithet"),newName, Rank.SUBSPECIES(),rank));
	        }
	        if(atomisedMap.get("dwcranks:varietyepithet") != null && checkRankValidForImport(Rank.VARIETY()) && rank.equals(Rank.VARIETY())){
	            myname.setVariety(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:varietyepithet"),newName, Rank.VARIETY(),rank));
	        }
	        if(atomisedMap.get("dwcranks:formepithet") != null && checkRankValidForImport(Rank.FORM()) && rank.equals(Rank.FORM())){
	            myname.setForm(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:formepithet"), newName,Rank.FORM(),rank));
	        }
        }

    }


    /**
     * @param refMods
     * @see #createUnparsedSynonym(Rank, String, HashMap, MyName)
     * the original TaxonXImport extracted Synonyms by creating acc Taxa with partial names
     * I (AM) do not understand this but don't want to destroy code which maybe works in some cases) there
     * I created this switch for old
     * for Spiders the new version is preferred
     */
    private void createUnparsedSynonymNew(Rank rank, String newName, HashMap<String, String> atomisedMap, MyName myname, Reference refMods) {
        logger.info("createSynonym");

        NonViralName<?> nameToBeFilled = this.getNonViralNameAccNomenclature();
        //System.out.println("createsynonym");
        if(rank.equals(Rank.UNKNOWN_RANK())){
            //TODO
        	myname.setNotParsableTaxon(newName);

        	nameToBeFilled.setTitleCache(newName, true);
        }else{
        	if(atomisedMap.get("dwc:genus") != null ){
    			nameToBeFilled.setGenusOrUninomial(atomisedMap.get("dwc:genus"));
	        }
        	if (rank.isSupraGeneric()){
        		if (atomisedMap.get("dwcranks:subtribe") != null ){
    	        	nameToBeFilled.setGenusOrUninomial(atomisedMap.get("dwcranks:subtribe"));
    	        }else if (atomisedMap.get("dwcranks:subtribe") != null ){
    	        	nameToBeFilled.setGenusOrUninomial(atomisedMap.get("dwcranks:subtribe"));
    	        }else if (atomisedMap.get("dwcranks:tribe") != null ){
    	        	nameToBeFilled.setGenusOrUninomial(atomisedMap.get("dwcranks:tribe"));
    	        }else if (atomisedMap.get("dwcranks:subfamily") != null ){
    	        	nameToBeFilled.setGenusOrUninomial(atomisedMap.get("dwcranks:subfamily"));
    	        }else if (atomisedMap.get("dwc:family") != null ){
    	        	nameToBeFilled.setGenusOrUninomial(atomisedMap.get("dwc:family"));
        	    }else{
        	    	logger.warn("Supra generic rank not yet handled or atomisation not available");
        	    }
        	}
        	if (atomisedMap.get("dwcranks:subgenus") != null){
        		nameToBeFilled.setInfraGenericEpithet(atomisedMap.get("dwcranks:subgenus"));
        	}
        	if (atomisedMap.get("dwc:subgenus") != null){
        		nameToBeFilled.setInfraGenericEpithet(atomisedMap.get("dwc:subgenus"));
        	}
        	if (atomisedMap.get("dwc:species") != null){
        		nameToBeFilled.setSpecificEpithet(atomisedMap.get("dwc:species"));
        	}
        	if (atomisedMap.get("dwcranks:formepithet") != null){
        		nameToBeFilled.setInfraSpecificEpithet(atomisedMap.get("dwcranks:formepithet"));
        	}else if (atomisedMap.get("dwcranks:varietyepithet") != null){
        		nameToBeFilled.setInfraSpecificEpithet(atomisedMap.get("dwcranks:varietyepithet"));
        	}else if (atomisedMap.get("dwc:infraspecificepithet") != null){
        		nameToBeFilled.setInfraSpecificEpithet(atomisedMap.get("dwc:infraspecificepithet"));
        	}else if (atomisedMap.get("dwc:subspecies") != null){
        		nameToBeFilled.setInfraSpecificEpithet(atomisedMap.get("dwc:subspecies"));
        	}
            Reference sec = sourceUrlRef;
            if(!state2.getConfig().doKeepOriginalSecundum()){
                sec = state2.getConfig().getSecundum();
            }
        	Synonym syn = Synonym.NewInstance(nameToBeFilled, sec);
//        	sourceHandler.addSource(refMods, syn);
        	myname.setSyno(syn);
        	myname.setSynonym(true);
        }
	}

    /**
     * @param rank
     * @param newName
     * @param atomisedMap
     * @param myname
     */
    private void createAtomisedTaxon(Rank rank, String newName, HashMap<String, String> atomisedMap, MyName myname) {
        logger.info("createAtomisedTaxon "+atomisedMap);
        if(rank.equals(Rank.UNKNOWN_RANK())){
            myname.setNotParsableTaxon(newName);
        }
        else{
            if(atomisedMap.get("dwc:family") != null && checkRankValidForImport(Rank.FAMILY())){
                myname.setFamily(myname.findOrCreateTaxon(atomisedMap.get("dwc:family"),newName, Rank.FAMILY(),rank));
            }
            if(atomisedMap.get("dwcranks:subfamily") != null  && checkRankValidForImport(Rank.SUBFAMILY())){
                myname.setSubfamily(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:subfamily"), newName,Rank.SUBFAMILY(),rank));
            }
            if(atomisedMap.get("dwcranks:tribe") != null && checkRankValidForImport(Rank.TRIBE())){
                myname.setTribe(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:tribe"),newName, Rank.TRIBE(),rank));
            }
            if(atomisedMap.get("dwcranks:subtribe") != null && checkRankValidForImport(Rank.SUBTRIBE())){
                myname.setSubtribe(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:subtribe"),newName, Rank.SUBTRIBE(),rank));
            }
            if(atomisedMap.get("dwc:genus") != null && checkRankValidForImport(Rank.GENUS())){
                myname.setGenus(myname.findOrCreateTaxon(atomisedMap.get("dwc:genus"),newName, Rank.GENUS(),rank));
            }
            if(atomisedMap.get("dwcranks:subgenus") != null && checkRankValidForImport(Rank.SUBGENUS())){
                myname.setSubgenus(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:subgenus"),newName, Rank.SUBGENUS(),rank));
            }
            if(atomisedMap.get("dwc:subgenus") != null && checkRankValidForImport(Rank.SUBGENUS())){
                myname.setSubgenus(myname.findOrCreateTaxon(atomisedMap.get("dwc:subgenus"),newName, Rank.SUBGENUS(),rank));
            }
            if(atomisedMap.get("dwc:species") != null && checkRankValidForImport(Rank.SPECIES())){
                String n=newName;
                if(atomisedMap.get("dwc:infraspecificepithet") != null) {
                    n=newName.split(atomisedMap.get("dwc:infraspecificepithet"))[0];
                    n=n.replace("subsp.","");
                }
                if(atomisedMap.get("dwc:subspecies") != null) {
                    n=newName.split(atomisedMap.get("dwc:subspecies"))[0];
                    n=n.replace("subsp.","");
                }
                if(atomisedMap.get("dwcranks:varietyepithet") != null) {
                    n=newName.split(atomisedMap.get("dwcranks:varietyepithet"))[0];
                    n=n.replace("var.","");
                    n=n.replace("v.","");
                }
                if(atomisedMap.get("dwcranks:formepithet") != null) {
                    //TODO
                    //System.out.println("TODO FORMA");
                    n=newName.split(atomisedMap.get("dwcranks:formepithet"))[0];
                    n=n.replace("forma","");
                }
                n=n.trim();
                String author = myname.getAuthor();
                if(n.split(" ").length>2){
                    String n2=n.split(" ")[0]+" "+n.split(" ")[1];
                    String a="";
                    try{
                        a= n.split(n2)[1].trim();
                    }catch(Exception e){logger.info("no author  in "+n);}
                    myname.setAuthor(a);
                    //System.out.println("FINDCREATESPECIES --"+n2+"--"+n+"**"+a+"##");
                    n=n2;

                }

                myname.setSpecies(myname.findOrCreateTaxon(atomisedMap.get("dwc:species"),n, Rank.SPECIES(),rank));
                myname.setAuthor(author);
            }
            if(atomisedMap.get("dwc:subspecies") != null && checkRankValidForImport(Rank.SUBSPECIES())){
                myname.setSubspecies(myname.findOrCreateTaxon(atomisedMap.get("dwc:subspecies"), newName,Rank.SUBSPECIES(),rank));
            }
            if(atomisedMap.get("dwc:infraspecificepithet") != null && checkRankValidForImport(Rank.SUBSPECIES())){
                myname.setSubspecies(myname.findOrCreateTaxon(atomisedMap.get("dwc:infraspecificepithet"),newName, Rank.SUBSPECIES(),rank));
            }
            if(atomisedMap.get("dwcranks:varietyepithet") != null && checkRankValidForImport(Rank.VARIETY())){
                myname.setVariety(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:varietyepithet"),newName, Rank.VARIETY(),rank));
            }
            if(atomisedMap.get("dwcranks:formepithet") != null && checkRankValidForImport(Rank.FORM())){
                myname.setForm(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:formepithet"), newName,Rank.FORM(),rank));
            }
        }
    }

    /**
     * @return
     */
    private boolean checkRankValidForImport(Rank currentRank) {
        //logger.info("checkRankValidForImport");
        return currentRank.isLower(state2.getConfig().getMaxRank()) || currentRank.equals(state2.getConfig().getMaxRank());
    }



    /**
     * @param classification2
     */
    public void updateClassification(Classification classification2) {
        //logger.info("updateClassification");
        classification = classification2;
    }

    /**
     * @param tnb
     * cast the current taxonnamebase into a botanical name or zoological or bacterial name
     * if errors, cast into a classis nonviralname
     * @param taxonnamebase2
     */
    @SuppressWarnings("rawtypes")
    public NonViralName<?> castTaxonNameBase(TaxonNameBase tnb, NonViralName<?> nvn) {

    	//logger.info("castTaxonNameBase");
        NonViralName<?> taxonnamebase2 = nvn;
        if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)) {
            try{
                taxonnamebase2=(BotanicalName) tnb;
            }catch(Exception e){
                taxonnamebase2= (NonViralName<?>) tnb;
            }
        }
        if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)) {
            try{
                taxonnamebase2=(ZoologicalName) tnb;
            }catch(Exception e){
                taxonnamebase2= (NonViralName<?>) tnb;
            }
        }
        if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)) {
            try{
                taxonnamebase2=(BacterialName) tnb;
            }catch(Exception e){
                taxonnamebase2= (NonViralName<?>) tnb;
            }
        }
        return taxonnamebase2;
    }

    /**
     * @param tnb
     * cast the current taxonnamebase into a botanical name or zoological or bacterial name
     * if errors, cast into a classis nonviralname
     * @param taxonnamebase2
     */
    @SuppressWarnings("rawtypes")
    public NonViralName<?> castTaxonNameBase(TaxonNameBase tnb) {
        //logger.info("castTaxonNameBase2");
        NonViralName<?> taxonnamebase2 = null;
        tnb=CdmBase.deproxy(tnb, TaxonNameBase.class);
        if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)) {
            try{
                taxonnamebase2=(BotanicalName) tnb;
            }catch(Exception e){
                taxonnamebase2= (NonViralName<?>) tnb;
            }
        }
        if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)) {
            try{
                taxonnamebase2=(ZoologicalName) tnb;
            }catch(Exception e){
                taxonnamebase2= (NonViralName<?>) tnb;
            }
        }
        if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)) {
            try{
                taxonnamebase2=(BacterialName) tnb;
            }catch(Exception e){
                taxonnamebase2= (NonViralName<?>) tnb;
            }
        }
        return taxonnamebase2;
    }

    public class MyName {
        /**
         * @param isSynonym
         */
        public MyName(boolean isSynonym) {
            super();
            this.isSynonym = isSynonym;
        }

        String originalName="";
        String newName="";
        Rank rank=Rank.UNKNOWN_RANK();
        String identifier="";
        String status="";
        String author=null;

        NonViralName<?> taxonNameBase;

        Reference refMods ;

        Taxon family,subfamily,tribe,subtribe,genus,subgenus,species,subspecies, variety,form;
        NonViralName<?> familyName, subfamilyName, tribeName,subtribeName,genusName,subgenusName,speciesName,subspeciesName;
        String familyStr, subfamilyStr, tribeStr,subtribeStr,genusStr,subgenusStr,speciesStr,subspeciesStr,formStr,varietyStr;
        Integer publicationYear;


		Taxon higherTaxa;
        Rank higherRank;
        private Taxon taxon;
        private Synonym syno;

        /**
         * @return the syno
         */
        public Synonym getSyno() {
            return syno;
        }

        @Override
        public String toString(){
            List<String> tot=new ArrayList<String>();
            String[] n= {familyStr, subfamilyStr, tribeStr,subtribeStr,genusStr,subgenusStr,speciesStr,subspeciesStr,formStr,varietyStr};
            for (String elt:n){
                if (!StringUtils.isEmpty(elt)) {
                    tot.add(elt);
                } else {
                    tot.add("*");
                }
            }
            return StringUtils.join(tot," ");
        }
        /**
         * @param syno the syno to set
         */
        public void setSyno(Synonym syno) {
            this.syno = syno;
        }

        boolean isSynonym=false;

        /**
         * @return the isSynonym
         */
        public boolean isSynonym() {
            return isSynonym;
        }

        /**
         * @param isSynonym the isSynonym to set
         */
        public void setSynonym(boolean isSynonym) {
            this.isSynonym = isSynonym;
        }

        public void setSource(Reference re){
            refMods=re;
        }

        /**
         * @param string
         */
        public void setFormStr(String string) {
            this.formStr=string;

        }
        /**
         * @param string
         */
        public void setVarietyStr(String string) {
            this.varietyStr=string;

        }
        /**
         * @param string
         */
        public void setSubspeciesStr(String string) {
            this.subspeciesStr=string;

        }
        /**
         * @param string
         */
        public void setSpeciesStr(String string) {
            this.speciesStr=string;

        }
        /**
         * @param string
         */
        public void setSubgenusStr(String string) {
            this.subgenusStr=string;

        }
        /**
         * @param string
         */
        public void setGenusStr(String string) {
            this.genusStr=string;

        }
        /**
         * @param string
         */
        public void setSubtribeStr(String string) {
            this.subtribeStr=string;

        }
        /**
         * @param string
         */
        public void setTribeStr(String string) {
            this.tribeStr=string;

        }
        /**
         * @param string
         */
        public void setSubfamilyStr(String string) {
            this.subfamilyStr=string;

        }
        /**
         * @param string
         */
        public void setFamilyStr(String string) {
            this.familyStr=string;

        }
        /**
         * @return the familyStr
         */
        public String getFamilyStr() {
            return familyStr;
        }
        /**
         * @return the subfamilyStr
         */
        public String getSubfamilyStr() {
            return subfamilyStr;
        }
        /**
         * @return the tribeStr
         */
        public String getTribeStr() {
            return tribeStr;
        }
        /**
         * @return the subtribeStr
         */
        public String getSubtribeStr() {
            return subtribeStr;
        }
        /**
         * @return the genusStr
         */
        public String getGenusStr() {
            return genusStr;
        }
        /**
         * @return the subgenusStr
         */
        public String getSubgenusStr() {
            return subgenusStr;
        }
        /**
         * @return the speciesStr
         */
        public String getSpeciesStr() {
            return speciesStr;
        }
        /**
         * @return the subspeciesStr
         */
        public String getSubspeciesStr() {
            return subspeciesStr;
        }
        /**
         * @return the formStr
         */
        public String getFormStr() {
            return formStr;
        }
        /**
         * @return the varietyStr
         */
        public String getVarietyStr() {
            return varietyStr;
        }

        public Integer getPublicationYear() {
			return publicationYear;
		}

		public void setPublicationYear(Integer publicationYear) {
			this.publicationYear = publicationYear;
		}

        /**
         * @param newName2
         */
        public void setNotParsableTaxon(String newName2) {
            //takes too much time
            //            List<TaxonBase> tmpList = importer.getTaxonService().list(Taxon.class, 0, 0, null, null);

            NomenclaturalStatusType statusType = null;
            if (!getStatus().isEmpty()){
                try {
                    statusType = nomStatusString2NomStatus(getStatus());
                } catch (UnknownCdmTypeException e) {
                    addProblematicStatusToFile(getStatus());
                    logger.warn("Problem with status");
                }
            }
            List<TaxonBase> tmpList = new ArrayList<>();

            Pager<TaxonBase> taxontest = importer.getTaxonService().findByTitle(TaxonBase.class, newName2, MatchMode.BEGINNING, null, null, null, null, null);
            tmpList.addAll(taxontest.getRecords());

            //logger.info("tmpList returned: "+tmpList.size());


            INonViralName identicName = null;
            boolean foundIdentic=false;
            TaxonBase<?> tmpTaxonBase=null;
            //            Taxon tmpPartial=null;
            for (TaxonBase<?> tmpb:tmpList){
                if(tmpb !=null){
                    TaxonNameBase<?,?> tnb =  tmpb.getName();
                    Rank crank=null;
                    if (tnb != null){
                        if (tnb.getTitleCache().split("sec.")[0].trim().equalsIgnoreCase(newName2) ){
                            crank =tnb.getRank();
                            if (crank !=null && rank !=null){
                                if (crank.equals(rank)){
                                	identicName = tnb;
                                	if (isSynonym && tmpb.isInstanceOf(Synonym.class) || !isSynonym && tmpb.isInstanceOf(Taxon.class)){
                                		foundIdentic=true;
                                		tmpTaxonBase=tmpb;
                               			break;
                                	}
                                }
                            }
                        }
                    }
                }
            }
            boolean statusMatch=false;
            boolean appendedMatch=false;
            if(tmpTaxonBase !=null && foundIdentic){
                statusMatch=compareStatus(tmpTaxonBase, statusType);
                if (!getStatus().isEmpty() && ! (tmpTaxonBase.getAppendedPhrase() == null)) {
                    appendedMatch=tmpTaxonBase.getAppendedPhrase().equals(getStatus());
                }
                if (getStatus().isEmpty() && tmpTaxonBase.getAppendedPhrase() == null) {
                    appendedMatch=true;
                }

            }
            if ((tmpTaxonBase == null || !foundIdentic) ||  (tmpTaxonBase != null && !statusMatch) ||  (tmpTaxonBase != null && !appendedMatch && !statusMatch)){

            	INonViralName tnb;
            	if (identicName == null){
            		tnb = getNonViralNameAccNomenclature();
            		tnb.setRank(rank);

	                if(statusType != null) {
	                    tnb.addStatus(NomenclaturalStatus.NewInstance(statusType));
	                }
	                if(StringUtils.isNotBlank(getStatus())) {
	                    tnb.setAppendedPhrase(getStatus());
	                }
	                tnb.setTitleCache(newName2,true);
	                tmpTaxonBase = findMatchingTaxon(tnb,refMods);
	            }else{
            		tnb = identicName;
            	}

                if(tmpTaxonBase==null){
                    tmpTaxonBase = isSynonym ? Synonym.NewInstance(tnb, refMods) : Taxon.NewInstance(tnb, refMods);
                    if(!state2.getConfig().doKeepOriginalSecundum()) {
                        tmpTaxonBase.setSec(state2.getConfig().getSecundum());
                    }
                    //tmptaxonbase.setSec(refMods);
                    if(!isSynonym) {
                        classification.addChildTaxon((Taxon)tmpTaxonBase, null, null);
                        sourceHandler.addSource(refMods, (Taxon)tmpTaxonBase);
                    }
                }
            }

            tmpTaxonBase = CdmBase.deproxy(tmpTaxonBase, TaxonBase.class);
            if (author != null) {
                if (!getIdentifier().isEmpty() && (getIdentifier().length()>2)){
                    setLSID(getIdentifier(), tmpTaxonBase);
                    importer.getTaxonService().saveOrUpdate(tmpTaxonBase);
                    tmpTaxonBase = CdmBase.deproxy(tmpTaxonBase, TaxonBase.class);
                }
            }
            TaxonNameBase<?,?> tnb = CdmBase.deproxy(tmpTaxonBase.getName(), TaxonNameBase.class);

            if(!isSynonym) {
                this.taxon=(Taxon)tmpTaxonBase;
            } else {
                if (tmpTaxonBase instanceof Taxon){
                	logger.warn("Incorrect status");
                }
            	this.syno=(Synonym)tmpTaxonBase;
            }

            taxonNameBase = castTaxonNameBase(tnb, taxonNameBase);

        }

        /**
         *
         */
        public void buildTaxon() {
            //System.out.println("BUILD TAXON");
            logger.info("buildTaxon");
            NomenclaturalStatusType statusType = null;
            if (!getStatus().isEmpty()){
            	status = getStatus();
            	String newNameStatus = newNameStatus(status);
            	if (newNameStatus != null){
            		taxonNameBase.setAppendedPhrase(newNameStatus);
            	}else{
            		try {
            			statusType = nomStatusString2NomStatus(getStatus());
            			taxonNameBase.addStatus(NomenclaturalStatus.NewInstance(statusType));
            		} catch (UnknownCdmTypeException e) {
            			addProblematicStatusToFile(getStatus());
            			logger.warn("Problem with status");
            		}
            	}
            }
            importer.getNameService().save(taxonNameBase);

            TaxonBase<?> tmpTaxonBase;
            if (!isSynonym) {
                tmpTaxonBase =Taxon.NewInstance(taxonNameBase, refMods); //sec set null
            }
            else {
                tmpTaxonBase =Synonym.NewInstance(taxonNameBase, refMods); //sec set null
            }
            boolean exist = false;
            if (!isSynonym){
	            for (TaxonNode node : classification.getAllNodes()){
	                try{
	                	Taxon nodeTaxon = node.getTaxon();
	                	boolean titleMatches = nodeTaxon.getTitleCache().equalsIgnoreCase(tmpTaxonBase.getTitleCache());
	                	boolean nomStatusMatches = compareStatus(node.getTaxon(), statusType);
	                	boolean nodeNameReplaceable = checkNodeNameReplaceable(nodeTaxon, tmpTaxonBase);
	                    if(titleMatches && nomStatusMatches) {
	                    	if (!isSynonym) {
	                    		tmpTaxonBase=CdmBase.deproxy(nodeTaxon, TaxonBase.class);
	                            exist =true;
	                        } else {
	                            logger.info("Found the same name but from another type (taxon/synonym)");
	                            TaxonNameBase<?,?> existingTnb = getTaxon().getName();
                                tmpTaxonBase = Synonym.NewInstance(existingTnb, refMods);
                                importer.getTaxonService().saveOrUpdate(tmpTaxonBase);
                                exist =true;
                            }
	                    }else if (nodeNameReplaceable){
	                    	nodeTaxon.setName(tmpTaxonBase.getName());
	                    	tmpTaxonBase = nodeTaxon;
	                    	exist = true;
	                    }
	                }catch(NullPointerException n){logger.warn(" A taxon is either null or its titlecache is null - ignore it?");}
	            }
            }
            if (!exist){

                boolean insertAsExisting =false;
                List<Taxon> existingTaxons=new ArrayList<Taxon>();
                try {
                    existingTaxons = getMatchingTaxa(taxonNameBase);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                double similarityScore=0.0;
                double similarityAuthor=-1;
                String author1="";
                String author2="";
                String t1="";
                String t2="";
                for (Taxon bestMatchingTaxon : existingTaxons){
                    //System.out.println("tnbase "+taxonnamebase.getTitleCache());
                    //System.out.println("bestex "+bestMatchingTaxon.getTitleCache());
                    if(taxonNameBase.getAuthorshipCache()!=null) {
                    	author1=taxonNameBase.getAuthorshipCache();
                    }
                    try {
                        if(castTaxonNameBase(bestMatchingTaxon.getName()).getAuthorshipCache()!=null) {
                            author2=castTaxonNameBase(bestMatchingTaxon.getName()).getAuthorshipCache();
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        t1=taxonNameBase.getTitleCache();
                        if (author1!=null && !StringUtils.isEmpty(author1)) {
                            t1=t1.split(Pattern.quote(author1))[0];
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        t2=bestMatchingTaxon.getTitleCache().split("sec.")[0].trim();
                        if (author2!=null && !StringUtils.isEmpty(author2)) {
                            t2=t2.split(Pattern.quote(author2))[0];
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    similarityScore=similarity(t1.trim(), t2.trim());
                    //System.out.println("taxonscore "+similarityScore);
                    similarityAuthor=similarity(author1.trim(), author2.trim());
                    //System.out.println("authorscore "+similarityAuthor);
                    insertAsExisting = compareAndCheckTaxon(taxonNameBase, refMods, similarityScore, bestMatchingTaxon, similarityAuthor);
                    if(insertAsExisting) {
                        tmpTaxonBase=bestMatchingTaxon;
                        break;
                    }
                }
                if ( !insertAsExisting ){
                    if(!state2.getConfig().doKeepOriginalSecundum()) {
                        tmpTaxonBase.setSec(state2.getConfig().getSecundum());
                    }

                    //                    tmptaxonbase.setSec(refMods);
                    if (taxonNameBase.getRank().equals(state2.getConfig().getMaxRank())) {
                        //System.out.println("****************************"+tmptaxonbase);
                        if (!isSynonym) {
                            classification.addChildTaxon((Taxon)tmpTaxonBase, refMods, null);
                        }
                    } else{
                        hierarchy = new HashMap<Rank, Taxon>();
                        //System.out.println("LOOK FOR PARENT "+taxonnamebase.toString()+", "+tmptaxonbase.toString());
                        if (!isSynonym){
                            lookForParentNode(taxonNameBase,(Taxon)tmpTaxonBase, refMods,this);
                            //System.out.println("HIERARCHY "+hierarchy);
                            Taxon parent = buildHierarchy();
                            if(!taxonExistsInClassification(parent,(Taxon)tmpTaxonBase)){
                                if(parent !=null) {
                                    classification.addParentChild(parent, (Taxon)tmpTaxonBase, refMods, null);
                                } else {
                                    classification.addChildTaxon((Taxon)tmpTaxonBase, refMods, null);
                                }
                                importer.getClassificationService().saveOrUpdate(classification);
                            }
                        }
                        //                        Set<TaxonNode> nodeList = classification.getAllNodes();
                        //                        for(TaxonNode tn:nodeList) {
                        //                            System.out.println(tn.getTaxon());
                        //                        }
                    }
                }
                importer.getClassificationService().saveOrUpdate(classification);
                 if(isSynonym) {
                    try{
                        Synonym castTest=CdmBase.deproxy(tmpTaxonBase, Synonym.class);
                    }catch(Exception e){
                        TaxonNameBase<?,?> existingTnb = tmpTaxonBase.getName();
                        Synonym castTest = Synonym.NewInstance(existingTnb, refMods);
                        importer.getTaxonService().saveOrUpdate(castTest);
                        tmpTaxonBase=CdmBase.deproxy(castTest, Synonym.class);
                    }
                }
            }
            if(!isSynonym) {
                taxon=CdmBase.deproxy(tmpTaxonBase, Taxon.class);
            } else {
                syno=CdmBase.deproxy(tmpTaxonBase, Synonym.class);
            }

        }

		private boolean checkNodeNameReplaceable(Taxon nodeTaxon, TaxonBase<?> newTaxon) {
			//TODO preliminary check
			if (newTaxon.isInstanceOf(Synonym.class)){
				return false;
			}
			NonViralName<?> nodeName = CdmBase.deproxy(nodeTaxon.getName(), NonViralName.class);
			NonViralName<?> newName = CdmBase.deproxy(newTaxon.getName(), NonViralName.class);
			if (nodeTaxon.getName() == null ||  newName == null){
				return false;
			}
			if (nodeTaxon.getDescriptions().size() > 0 || nodeName.getDescriptions().size() > 0 || nodeName.getTypeDesignations().size() > 0 ){
				return false;
			}
			boolean compare = true;
			for (NomenclaturalStatus status : newName.getStatus() ){
				compare &= compareStatus(nodeTaxon, status.getType());
			}
			if (! compare){
				return false;
			}

			if (nodeName.getNameCache() != null && nodeName.getNameCache().equals(newName.getNameCache())){
				if (nodeName.getNameCache().equals(nodeName.getTitleCache())){
					if (newName.getNameCache().length() < newName.getTitleCache().length()){
						logger.warn("We still need to check, if node was automatically created via hierarchy creation: " + nodeName.getNameCache());
						return true;
					}
				}
			}

			return false;
		}

		/**
         *
         */
        private Taxon buildHierarchy() {
            logger.info("buildHierarchy");
            Taxon higherTaxon = null;
            //add the maxRank as a root
            if(hierarchy.containsKey(state2.getConfig().getMaxRank())){
                Taxon ct=hierarchy.get(state2.getConfig().getMaxRank());
                if(!taxonExistsInClassification(higherTaxon, ct)) {
                   classification.addChildTaxon(ct, refMods, null);
                }
                higherTaxon = hierarchy.get(state2.getConfig().getMaxRank());
                //                return higherTaxon;
            }
            //add the relation to the highertaxon, except if the current rank to add IS the maxRank

            //TODO higher Ranks

            if(hierarchy.containsKey(Rank.FAMILY()) && !state2.getConfig().getMaxRank().equals(Rank.FAMILY())){
                higherTaxon=saveAndGetHigherTaxon(Rank.FAMILY(),higherTaxon);
            }
            if(hierarchy.containsKey(Rank.SUBFAMILY()) && !state2.getConfig().getMaxRank().equals(Rank.SUBFAMILY())){
                higherTaxon=saveAndGetHigherTaxon(Rank.SUBFAMILY(),higherTaxon);
            }
            if(hierarchy.containsKey(Rank.TRIBE())&& !state2.getConfig().getMaxRank().equals(Rank.TRIBE())){
                higherTaxon=saveAndGetHigherTaxon(Rank.TRIBE(),higherTaxon);
            }
            if(hierarchy.containsKey(Rank.SUBTRIBE())&& !state2.getConfig().getMaxRank().equals(Rank.SUBTRIBE())){
                higherTaxon=saveAndGetHigherTaxon(Rank.SUBTRIBE(),higherTaxon);
            }
            if(hierarchy.containsKey(Rank.GENUS())&& !state2.getConfig().getMaxRank().equals(Rank.SUBGENUS())){
                higherTaxon=saveAndGetHigherTaxon(Rank.GENUS(),higherTaxon);
            }
            if(hierarchy.containsKey(Rank.SUBGENUS())&& !state2.getConfig().getMaxRank().equals(Rank.SUBGENUS())){
                higherTaxon=saveAndGetHigherTaxon(Rank.SUBGENUS(),higherTaxon);
            }
            importer.getClassificationService().saveOrUpdate(classification);
            return higherTaxon;
        }

        private Taxon saveAndGetHigherTaxon(Rank r, Taxon higherTaxon){
            Taxon ct=hierarchy.get(r);
            if(!taxonExistsInClassification(higherTaxon,ct )) {
                if(higherTaxon != null && ct!=null) {
                    classification.addParentChild(higherTaxon, ct, refMods, null);
                } else
                    if(higherTaxon == null && ct !=null) {
                        classification.addChildTaxon(ct, refMods, null);
                }
            }
            return ct;
        }

        private boolean taxonExistsInClassification(Taxon parent, Taxon child){
            logger.info("taxonExistsInClassification");
            //            System.out.println("LOOK IF TAXA EXIST "+parent+", "+child);
            boolean found=false;
            if(parent !=null){
                for (TaxonNode p : classification.getAllNodes()){
                    if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                        for (TaxonNode c : p.getChildNodes()) {
                            if (c.getTaxon().getTitleCache().equalsIgnoreCase(child.getTitleCache())) {
                                found=true;
                                break;
                            }
                        }
                    }
                }
            }
            else{
                for (TaxonNode p : classification.getAllNodes()){
                    if(p.getTaxon().getTitleCache().equalsIgnoreCase(child.getTitleCache())) {
                        found=true;
                        break;
                    }
                }
            }
            //            System.out.println("LOOK IF TAXA EXIST? "+found);
            return found;
        }
        /**
         * @param nameToBeFilledTest
         */
        @SuppressWarnings("rawtypes")
        public void setParsedName(ITaxonNameBase nameToBeFilledTest) {
            this.taxonNameBase = (NonViralName<?>) nameToBeFilledTest;

        }
        //variety dwcranks:varietyEpithet
        /**
         * @return the author
         */
        public String getAuthor() {
            return author;
        }
        /**
         * @return
         */
        public Taxon getTaxon() {
            return taxon;
        }
        /**
         * @return
         */
        public NonViralName<?> getTaxonNameBase() {
            return taxonNameBase;
        }

        /**
         * @param findOrCreateTaxon
         */
        public void setForm(Taxon form) {
            this.form=form;

        }
        /**
         * @param findOrCreateTaxon
         */
        public void setVariety(Taxon variety) {
            this.variety=variety;

        }
        /**
         * @param string
         * @return
         */
        @SuppressWarnings("rawtypes")
        public Taxon findOrCreateTaxon(String partialname,String fullname, Rank rank, Rank globalrank) {
            logger.info("findOrCreateTaxon");
            sourceUrlRef=CdmBase.deproxy(sourceUrlRef, Reference.class);
            //takes too much time
            //            List<TaxonBase> tmpList = importer.getTaxonService().list(Taxon.class, 0, 0, null, null);
            //            logger.info("tmpList returned: "+tmpList.size());

            NomenclaturalStatusType statusType = null;
            if (!getStatus().isEmpty()){
                try {
                    statusType = nomStatusString2NomStatus(getStatus());
                } catch (UnknownCdmTypeException e) {
                    addProblematicStatusToFile(getStatus());
                    logger.warn("Problem with status");
                }
            }

            List<TaxonBase> tmpListFiltered = new ArrayList<TaxonBase>();

            Pager<TaxonBase> taxontest = importer.getTaxonService().findByTitle(TaxonBase.class, fullname, MatchMode.BEGINNING, null, null, null, null, null);

            tmpListFiltered.addAll(taxontest.getRecords());
            taxontest = importer.getTaxonService().findByTitle(TaxonBase.class, partialname, MatchMode.BEGINNING, null, null, null, null, null);
            tmpListFiltered.addAll(taxontest.getRecords());

            //logger.info("tmpListFiltered returned: "+tmpListFiltered.size());

            boolean nameCorrected=false;
            if (fullname.indexOf(partialname)<0) {
                nameCorrected=true;
            }

            boolean foundIdentic=false;
            Taxon tmp=null;
            for (TaxonBase tmpb:tmpListFiltered){
                if(tmpb !=null){
                    TaxonNameBase tnb =  tmpb.getName();
                    Rank crank=null;
                    if (tnb != null){
                         if(globalrank.equals(rank) || (globalrank.isLower(Rank.SPECIES()) && rank.equals(Rank.SPECIES()))){
                            if (tnb.getTitleCache().split("sec.")[0].trim().equalsIgnoreCase(fullname) ){
                                crank =tnb.getRank();
                                if (crank !=null && rank !=null){
                                    if (crank.equals(rank)){
                                        foundIdentic=true;
                                        try{
                                            tmp=(Taxon)tmpb;
                                            break;
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            if(nameCorrected){ //for corrected names such as Anochetus -- A. blf-pat
                                if (tnb.getTitleCache().split("sec.")[0].trim().equalsIgnoreCase(partialname) ){
                                    crank =tnb.getRank();
                                    if (crank !=null && rank !=null){
                                        if (crank.equals(rank)){
                                            foundIdentic=true;
                                            try{
                                                tmp=(Taxon)tmpb;
                                                break;
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            if (tnb.getTitleCache().split("sec.")[0].trim().equalsIgnoreCase(partialname) ){
                                crank =tnb.getRank();
                                if (crank !=null && rank !=null){
                                    if (crank.equals(rank)){
                                        foundIdentic=true;
                                        try{
                                            tmp=(Taxon)tmpb;
                                            break;
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            boolean statusMatch=false;
            boolean appendedMatch=false;
            if(tmp !=null && foundIdentic){
                statusMatch=compareStatus(tmp, statusType);
                if (!getStatus().isEmpty() && ! (tmp.getAppendedPhrase() == null)) {
                    appendedMatch=tmp.getAppendedPhrase().equals(getStatus());
                }
                if (getStatus().isEmpty() && tmp.getAppendedPhrase() == null) {
                    appendedMatch=true;
                }

            }
            if ((tmp == null || !foundIdentic) ||  (tmp != null && !statusMatch) ||  (tmp != null && !appendedMatch && !statusMatch)){

                NonViralName<?> tnb = getNonViralNameAccNomenclature();
                tnb.setRank(rank);

                if(statusType != null) {
                    tnb.addStatus(NomenclaturalStatus.NewInstance(statusType));
                }
                if(StringUtils.isNotBlank(getStatus())) {
                    tnb.setAppendedPhrase(getStatus());
                }

                if(rank.equals(Rank.UNKNOWN_RANK())){
                    tnb.setTitleCache(fullname, true);
                    //                    tnb.setGenusOrUninomial(fullname);
                }
                if(rank.isHigher(Rank.GENUS())) {
                    tnb.setGenusOrUninomial(partialname);
                }

                if(rank.isHigher(Rank.SPECIES())) {
                    tnb.setTitleCache(partialname, true);
                }

                if (rank.equals(globalrank) && author != null) {

                    tnb.setCombinationAuthorship(findOrCreateAuthor(author));
                    if (getIdentifier() !=null && !getIdentifier().isEmpty()){
                        Taxon taxonLSID = getTaxonByLSID(getIdentifier());
                        if (taxonLSID !=null) {
                            tmp=taxonLSID;
                        }
                    }
                }

                if(tmp == null){
                    if (rank.equals(Rank.FAMILY())) {
                        tmp = buildFamily(tnb);
                    }
                    if (rank.equals(Rank.SUBFAMILY())) {
                        tmp = buildSubfamily(tnb);
                    }
                    if (rank.equals(Rank.TRIBE())) {
                        tmp = buildTribe(tnb);
                    }
                    if (rank.equals(Rank.SUBTRIBE())) {
                        tmp = buildSubtribe(tnb);
                    }
                    if (rank.equals(Rank.GENUS())) {
                        tmp = buildGenus(partialname, tnb);
                    }

                    if (rank.equals(Rank.SUBGENUS())) {
                        tmp = buildSubgenus(partialname, tnb);
                    }
                    if (rank.equals(Rank.SPECIES())) {
                        tmp = buildSpecies(partialname, tnb);
                    }

                    if (rank.equals(Rank.SUBSPECIES())) {
                        tmp = buildSubspecies(partialname, tnb);
                    }

                    if (rank.equals(Rank.VARIETY())) {
                        tmp = buildVariety(fullname, partialname, tnb);
                    }

                    if (rank.equals(Rank.FORM())) {
                        tmp = buildForm(fullname, partialname, tnb);
                    }
                    if (tmp != null){
                    	TaxonXTreatmentExtractor.this.sourceHandler.addSource(refMods, tmp);
                    }

                    importer.getClassificationService().saveOrUpdate(classification);
                }

            }

            tmp = CdmBase.deproxy(tmp, Taxon.class);
            if (rank.equals(globalrank) && author != null) {
                if (!getIdentifier().isEmpty() && (getIdentifier().length()>2)){
                    setLSID(getIdentifier(), tmp);
                    importer.getTaxonService().saveOrUpdate(tmp);
                    tmp = CdmBase.deproxy(tmp, Taxon.class);
                }
            }

            this.taxon=tmp;

            return tmp;
        }

        /**
         * @param tnb
         * @return
         */
        private Taxon buildSubfamily(NonViralName<?> tnb) {
            Taxon tmp;
            //            tnb.generateTitle();
            tmp = findMatchingTaxon(tnb,refMods);
            if(tmp ==null){
                tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                if(!state2.getConfig().doKeepOriginalSecundum()) {
                    tmp.setSec(state2.getConfig().getSecundum());
                }
                //                tmp.setSec(refMods);
                //                sourceHandler.addSource(refMods, tmp);
                if(family != null) {
                    classification.addParentChild(family, tmp, null, null);
                    higherRank=Rank.FAMILY();
                    higherTaxa=family;
                } else {
                    //System.out.println("ADDCHILDTAXON SUBFAMILY "+tmp);
                    classification.addChildTaxon(tmp, null, null);
                }
            }
            return tmp;
        }
        /**
         * @param tnb
         * @return
         */
        private Taxon buildFamily(NonViralName<?> tnb) {
            Taxon tmp;
            //            tnb.generateTitle();
            tmp = findMatchingTaxon(tnb,refMods);
            if(tmp ==null){
                tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                if(!state2.getConfig().doKeepOriginalSecundum()) {
                    tmp.setSec(state2.getConfig().getSecundum());
                }
                //                tmp.setSec(refMods);
                //sourceHandler.addSource(refMods, tmp);
                //System.out.println("ADDCHILDTAXON FAMILY "+tmp);
                classification.addChildTaxon(tmp, null, null);
            }
            return tmp;
        }
        /**
         * @param fullname
         * @param tnb
         * @return
         */
        private Taxon buildForm(String fullname, String partialname, NonViralName<?> tnb) {
            if (genusName !=null) {
                tnb.setGenusOrUninomial(genusName.getGenusOrUninomial());
            }
            if (subgenusName !=null) {
                tnb.setInfraGenericEpithet(subgenusName.getInfraGenericEpithet());
            }
            if(speciesName !=null) {
                tnb.setSpecificEpithet(speciesName.getSpecificEpithet());
            }
            if(subspeciesName != null) {
                tnb.setInfraSpecificEpithet(subspeciesName.getInfraSpecificEpithet());
            }
            if(partialname!= null) {
                tnb.setInfraSpecificEpithet(partialname);
            }
             //TODO how to save form??
            tnb.setTitleCache(fullname, true);
            Taxon tmp = findMatchingTaxon(tnb,refMods);
            if(tmp ==null){
                tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                if(!state2.getConfig().doKeepOriginalSecundum()) {
                    tmp.setSec(state2.getConfig().getSecundum());
                }
                //                tmp.setSec(refMods);
                //sourceHandler.addSource(refMods, tmp);
                if (subspecies !=null) {
                    classification.addParentChild(subspecies, tmp, null, null);
                    higherRank=Rank.SUBSPECIES();
                    higherTaxa=subspecies;
                } else {
                    if (species !=null) {
                        classification.addParentChild(species, tmp, null, null);
                        higherRank=Rank.SPECIES();
                        higherTaxa=species;
                    }
                    else{
                        //                        System.out.println("ADDCHILDTAXON FORM "+tmp);
                        classification.addChildTaxon(tmp, null, null);
                    }
                }
            }
            return tmp;
        }
        /**
         * @param fullname
         * @param tnb
         * @return
         */
        private Taxon buildVariety(String fullname, String partialname, NonViralName<?> tnb) {
            Taxon tmp;
            if (genusName !=null) {
                tnb.setGenusOrUninomial(genusName.getGenusOrUninomial());
            }
            if (subgenusName !=null) {
                tnb.setInfraGenericEpithet(subgenusName.getInfraGenericEpithet());
            }
            if(speciesName !=null) {
                tnb.setSpecificEpithet(speciesName.getSpecificEpithet());
            }
            if(subspeciesName != null) {
                tnb.setInfraSpecificEpithet(subspeciesName.getSpecificEpithet());
            }
            if(partialname != null) {
                tnb.setInfraSpecificEpithet(partialname);
            }
            //TODO how to save variety?
            tnb.setTitleCache(fullname, true);
            tmp = findMatchingTaxon(tnb,refMods);
            if(tmp ==null){
                tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                if(!state2.getConfig().doKeepOriginalSecundum()) {
                    tmp.setSec(state2.getConfig().getSecundum());
                }
                //                tmp.setSec(refMods);
                //sourceHandler.addSource(refMods, tmp);
                if (subspecies !=null) {
                    classification.addParentChild(subspecies, tmp, null, null);
                    higherRank=Rank.SUBSPECIES();
                    higherTaxa=subspecies;
                } else {
                    if(species !=null) {
                        classification.addParentChild(species, tmp, null, null);
                        higherRank=Rank.SPECIES();
                        higherTaxa=species;
                    }
                    else{
                        //System.out.println("ADDCHILDTAXON VARIETY "+tmp);
                        classification.addChildTaxon(tmp, null, null);
                    }
                }
            }
            return tmp;
        }
        /**
         * @param partialname
         * @param tnb
         * @return
         */
        private Taxon buildSubspecies(String partialname, NonViralName<?> tnb) {
            if (genusName !=null) {
                tnb.setGenusOrUninomial(genusName.getGenusOrUninomial());
            }
            if (subgenusName !=null) {
                //                            System.out.println("SUB:"+subgenusName.getInfraGenericEpithet());
                tnb.setInfraGenericEpithet(subgenusName.getInfraGenericEpithet());
            }
            if(speciesName !=null) {
                //                            System.out.println("SPE:"+speciesName.getSpecificEpithet());
                tnb.setSpecificEpithet(speciesName.getSpecificEpithet());
            }
            tnb.setInfraSpecificEpithet(partialname);
            Taxon tmp = findMatchingTaxon(tnb,refMods);
            if(tmp ==null){
                tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                if(!state2.getConfig().doKeepOriginalSecundum())
                 {
                    tmp.setSec(state2.getConfig().getSecundum());
                //                tmp.setSec(refMods);
                //sourceHandler.addSource(refMods, tmp);
                }

                if(species != null) {
                    classification.addParentChild(species, tmp, null, null);
                    higherRank=Rank.SPECIES();
                    higherTaxa=species;
                }
                else{
                    //System.out.println("ADDCHILDTAXON SUBSPECIES "+tmp);
                    classification.addChildTaxon(tmp, null, null);
                }
            }
            return tmp;
        }
        /**
         * @param partialname
         * @param tnb
         * @return
         */
        private Taxon buildSpecies(String partialname, NonViralName<?> tnb) {
            if (genusName !=null) {
                tnb.setGenusOrUninomial(genusName.getGenusOrUninomial());
            }
            if (subgenusName !=null) {
                tnb.setInfraGenericEpithet(subgenusName.getInfraGenericEpithet());
            }
            tnb.setSpecificEpithet(partialname.toLowerCase());
            Taxon tmp = findMatchingTaxon(tnb,refMods);
            if(tmp ==null){
                tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                if(!state2.getConfig().doKeepOriginalSecundum()) {
                    tmp.setSec(state2.getConfig().getSecundum());
                }
                //                tmp.setSec(refMods);
                //sourceHandler.addSource(refMods, tmp);
                if (subgenus !=null) {
                    classification.addParentChild(subgenus, tmp, null, null);
                    higherRank=Rank.SUBGENUS();
                    higherTaxa=subgenus;
                } else {
                    if (genus !=null) {
                        classification.addParentChild(genus, tmp, null, null);
                        higherRank=Rank.GENUS();
                        higherTaxa=genus;
                    }
                    else{
                        //System.out.println("ADDCHILDTAXON SPECIES "+tmp);
                        classification.addChildTaxon(tmp, null, null);
                    }
                }
            }
            return tmp;
        }
        /**
         * @param partialname
         * @param tnb
         * @return
         */
        private Taxon buildSubgenus(String partialname, NonViralName<?> tnb) {
            tnb.setInfraGenericEpithet(partialname);
            if (genusName !=null) {
                tnb.setGenusOrUninomial(genusName.getGenusOrUninomial());
            }
            Taxon tmp = findMatchingTaxon(tnb,refMods);
            if(tmp ==null){
                tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                if(!state2.getConfig().doKeepOriginalSecundum()) {
                    tmp.setSec(state2.getConfig().getSecundum());
                }
                //                tmp.setSec(refMods);
                //sourceHandler.addSource(refMods, tmp);
                if(genus != null) {
                    classification.addParentChild(genus, tmp, null, null);
                    higherRank=Rank.GENUS();
                    higherTaxa=genus;
                } else{
                    //System.out.println("ADDCHILDTAXON SUBGENUS "+tmp);
                    classification.addChildTaxon(tmp, null, null);
                }
            }
            return tmp;
        }
        /**
         * @param partialname
         * @param tnb
         * @return
         */
        private Taxon buildGenus(String partialname, NonViralName<?> tnb) {
            Taxon tmp;
            tnb.setGenusOrUninomial(partialname);


            tmp = findMatchingTaxon(tnb,refMods);
            if(tmp ==null){
                tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                if(!state2.getConfig().doKeepOriginalSecundum())
                 {
                    tmp.setSec(state2.getConfig().getSecundum());
                //                tmp.setSec(refMods);
                //sourceHandler.addSource(refMods, tmp);
                }

                if(subtribe != null) {
                    classification.addParentChild(subtribe, tmp, null, null);
                    higherRank=Rank.SUBTRIBE();
                    higherTaxa=subtribe;
                } else{
                    if(tribe !=null) {
                        classification.addParentChild(tribe, tmp, null, null);
                        higherRank=Rank.TRIBE();
                        higherTaxa=tribe;
                    } else{
                        if(subfamily !=null) {
                            classification.addParentChild(subfamily, tmp, null, null);
                            higherRank=Rank.SUBFAMILY();
                            higherTaxa=subfamily;
                        } else
                            if(family !=null) {
                                classification.addParentChild(family, tmp, null, null);
                                higherRank=Rank.FAMILY();
                                higherTaxa=family;
                            }
                            else{
                                //System.out.println("ADDCHILDTAXON GENUS "+tmp);
                                classification.addChildTaxon(tmp, null, null);
                            }
                    }
                }
            }
            return tmp;
        }

        /**
         * @param tnb
         * @return
         */
        private Taxon buildSubtribe(NonViralName<?> tnb) {
            Taxon tmp = findMatchingTaxon(tnb,refMods);
            if(tmp==null){
                tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                if(!state2.getConfig().doKeepOriginalSecundum()) {
                    tmp.setSec(state2.getConfig().getSecundum());
                }
                //                tmp.setSec(refMods);
                //sourceHandler.addSource(refMods, tmp);
                if(tribe != null) {
                    classification.addParentChild(tribe, tmp, null, null);
                    higherRank=Rank.TRIBE();
                    higherTaxa=tribe;
                } else{
                    //System.out.println("ADDCHILDTAXON SUBTRIBE "+tmp);
                    classification.addChildTaxon(tmp, null, null);
                }
            }
            return tmp;
        }
        /**
         * @param tnb
         * @return
         */
        private Taxon buildTribe(NonViralName<?> tnb) {
            Taxon tmp = findMatchingTaxon(tnb,refMods);
            if(tmp==null){
                tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                if(!state2.getConfig().doKeepOriginalSecundum()) {
                    tmp.setSec(state2.getConfig().getSecundum());
                }
                //                tmp.setSec(refMods);
                //sourceHandler.addSource(refMods, tmp);
                if (subfamily !=null) {
                    classification.addParentChild(subfamily, tmp, null, null);
                    higherRank=Rank.SUBFAMILY();
                    higherTaxa=subfamily;
                } else {
                    if(family != null) {
                        classification.addParentChild(family, tmp, null, null);
                        higherRank=Rank.FAMILY();
                        higherTaxa=family;
                    }
                    else{
                        //System.out.println("ADDCHILDTAXON TRIBE "+tmp);
                        classification.addChildTaxon(tmp, null, null);
                    }
                }
            }
            return tmp;
        }

        /**
         * @param identifier2
         * @return
         */
        @SuppressWarnings("rawtypes")
        private Taxon getTaxonByLSID(String identifier) {
            //logger.info("getTaxonByLSID");
            //            boolean lsidok=false;
            String id = identifier.split("__")[0];
            //            String source = identifier.split("__")[1];
            LSID lsid = null;
            if (id.indexOf("lsid")>-1){
                try {
                    lsid = new LSID(id);
                    //                    lsidok=true;
                } catch (MalformedLSIDException e) {
                    logger.warn("Malformed LSID");
                }
            }
            if (lsid !=null){
                List<Taxon> taxa = importer.getTaxonService().list(Taxon.class, 0, 0, null, null);
                LSID currentlsid=null;
                for (Taxon t:taxa){
                    currentlsid = t.getLsid();
                    if (currentlsid !=null){
                        if (currentlsid.getLsid().equals(lsid.getLsid())){
                            try{
                                return t;
                            }
                            catch(Exception e){logger.warn("Exception occurred while comparing LSIDs "+e );}
                        }
                    }
                }
            }
            return null;
        }
        /**
         * @param author2
         * @return
         */
        @SuppressWarnings("rawtypes")
        private Person findOrCreateAuthor(String author2) {
            //logger.info("findOrCreateAuthor");
            List<UuidAndTitleCache<Person>> hiberPersons = importer.getAgentService().getPersonUuidAndTitleCache();
            for (UuidAndTitleCache<Person> hibernateP:hiberPersons){
                if(hibernateP.getTitleCache().equals(author2)) {
                    AgentBase existing = importer.getAgentService().find(hibernateP.getUuid());
                    return CdmBase.deproxy(existing, Person.class);
                }
            }
            Person p = Person.NewInstance();
            p.setTitleCache(author2,true);
            importer.getAgentService().saveOrUpdate(p);
            return CdmBase.deproxy(p, Person.class);
        }
        /**
         * @param author the author to set
         */
        public void setAuthor(String author) {
            this.author = author;
        }

        /**
         * @return the higherTaxa
         */
        public Taxon getHigherTaxa() {
            return higherTaxa;
        }
        /**
         * @param higherTaxa the higherTaxa to set
         */
        public void setHigherTaxa(Taxon higherTaxa) {
            this.higherTaxa = higherTaxa;
        }
        /**
         * @return the higherRank
         */
        public Rank getHigherRank() {
            return higherRank;
        }
        /**
         * @param higherRank the higherRank to set
         */
        public void setHigherRank(Rank higherRank) {
            this.higherRank = higherRank;
        }
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
            if (status == null) {
                return "";
            }
            return status;
        }
        /**
         * @param status the status to set
         */
        public void setStatus(String status) {
            this.status = status;
        }
        /**
         * @return the family
         */
        public Taxon getFamily() {
            return family;
        }
        /**
         * @param family the family to set
         */
        @SuppressWarnings("rawtypes")
        public void setFamily(Taxon family) {
            this.family = family;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(family.getName(), TaxonNameBase.class);
            familyName = castTaxonNameBase(taxonNameBase,familyName);
        }
        /**
         * @return the subfamily
         */
        public Taxon getSubfamily() {
            return subfamily;
        }
        /**
         * @param subfamily the subfamily to set
         */
        @SuppressWarnings("rawtypes")
        public void setSubfamily(Taxon subfamily) {
            this.subfamily = subfamily;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(subfamily.getName(), TaxonNameBase.class);
            subfamilyName = castTaxonNameBase(taxonNameBase,subfamilyName);
        }
        /**
         * @return the tribe
         */
        public Taxon getTribe() {
            return tribe;
        }
        /**
         * @param tribe the tribe to set
         */
        @SuppressWarnings("rawtypes")
        public void setTribe(Taxon tribe) {
            this.tribe = tribe;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(tribe.getName(), TaxonNameBase.class);
            tribeName = castTaxonNameBase(taxonNameBase,tribeName);
        }
        /**
         * @return the subtribe
         */
        public Taxon getSubtribe() {
            return subtribe;
        }
        /**
         * @param subtribe the subtribe to set
         */
        @SuppressWarnings("rawtypes")
        public void setSubtribe(Taxon subtribe) {
            this.subtribe = subtribe;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(subtribe.getName(), TaxonNameBase.class);
            subtribeName =castTaxonNameBase(taxonNameBase,subtribeName);
        }
        /**
         * @return the genus
         */
        public Taxon getGenus() {
            return genus;
        }
        /**
         * @param genus the genus to set
         */
        @SuppressWarnings("rawtypes")
        public void setGenus(Taxon genus) {
            if (genus != null){
	        	this.genus = genus;
	            TaxonNameBase taxonNameBase = CdmBase.deproxy(genus.getName(), TaxonNameBase.class);
	            genusName = castTaxonNameBase(taxonNameBase,genusName);
            }
        }
        /**
         * @return the subgenus
         */
        public Taxon getSubgenus() {
            return subgenus;
        }
        /**
         * @param subgenus the subgenus to set
         */
        @SuppressWarnings("rawtypes")
        public void setSubgenus(Taxon subgenus) {
            this.subgenus = subgenus;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(subgenus.getName(), TaxonNameBase.class);
            subgenusName = castTaxonNameBase(taxonNameBase,subgenusName);
        }
        /**
         * @return the species
         */
        public Taxon getSpecies() {
            return species;
        }
        /**
         * @param species the species to set
         */
        public void setSpecies(Taxon species) {
        	if (species != null){
	            this.species = species;
	            @SuppressWarnings("rawtypes")
	            TaxonNameBase taxonNameBase = CdmBase.deproxy(species.getName(), TaxonNameBase.class);
	            speciesName = castTaxonNameBase(taxonNameBase,speciesName);
        	}
        }
        /**
         * @return the subspecies
         */
        public Taxon getSubspecies() {
            return subspecies;
        }
        /**
         * @param subspecies the subspecies to set
         */
        @SuppressWarnings("rawtypes")
        public void setSubspecies(Taxon subspecies) {
            this.subspecies = subspecies;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(subspecies.getName(), TaxonNameBase.class);
            subspeciesName = castTaxonNameBase(taxonNameBase,subspeciesName);

        }



    }


    /**
     * @param status
     */
    private void addProblematicStatusToFile(String status) {
        try{
            FileWriter fstream = new FileWriter(TaxonXImport.LOG_FOLDER + "StatusUnknown_"+classification.getTitleCache()+".txt",true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(status+"\n");
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }



    /**
     * @param tnb
     * @return
     */
    private Taxon findMatchingTaxon(INonViralName tnb, Reference refMods) {
        logger.info("findMatchingTaxon");
        Taxon tmp=null;

        refMods=CdmBase.deproxy(refMods, Reference.class);
        boolean insertAsExisting =false;
        List<Taxon> existingTaxa = new ArrayList<Taxon>();
        try {
            existingTaxa = getMatchingTaxa(tnb);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        double similarityScore=0.0;
        double similarityAuthor=-1;
        String author1="";
        String author2="";
        String t1="";
        String t2="";
        for (Taxon bestMatchingTaxon : existingTaxa){
            if (!existingTaxa.isEmpty() && state2.getConfig().isInteractWithUser() && !insertAsExisting) {
                //                System.out.println("tnb "+tnb.getTitleCache());
                //                System.out.println("ext "+bestMatchingTaxon.getTitleCache());
                try {
                    if(tnb.getAuthorshipCache()!=null) {
                        author1=tnb.getAuthorshipCache();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    if(castTaxonNameBase(bestMatchingTaxon.getName()).getAuthorshipCache()!=null) {
                        author2=castTaxonNameBase(bestMatchingTaxon.getName()).getAuthorshipCache();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    t1=tnb.getTitleCache().split("sec.")[0].trim();
                    if (author1!=null && !StringUtils.isEmpty(author1)) {
                        t1=t1.split(Pattern.quote(author1))[0];
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    t2=bestMatchingTaxon.getTitleCache().split("sec.")[0].trim();
                    if (author2!=null && !StringUtils.isEmpty(author2)) {
                        t2=t2.split(Pattern.quote(author2))[0];
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                similarityScore=similarity(t1.trim(), t2.trim());
                //                System.out.println("taxascore: "+similarityScore);
                similarityAuthor=similarity(author1.trim(), author2.trim());
                //                System.out.println("authorscore: "+similarityAuthor);
                insertAsExisting = compareAndCheckTaxon(tnb, refMods, similarityScore, bestMatchingTaxon,similarityAuthor);
            }
            if(insertAsExisting) {
                //System.out.println("KEEP "+bestMatchingTaxon.toString());
                tmp=bestMatchingTaxon;
                sourceHandler.addSource(refMods, tmp);
                return tmp;
            }
        }
        return tmp;
    }


    /**
     * @param tnb
     * @param refMods
     * @param similarityScore
     * @param bestMatchingTaxon
     * @param similarityAuthor
     * @return
     */
    private boolean compareAndCheckTaxon(INonViralName tnb, Reference refMods, double similarityScore,
            Taxon bestMatchingTaxon, double similarityAuthor) {
        //logger.info("compareAndCheckTaxon");
        boolean insertAsExisting;
        //        if (tnb.getTitleCache().split("sec.")[0].equalsIgnoreCase("Chenopodium") && bestMatchingTaxon.getTitleCache().split("sec.")[0].indexOf("Chenopodium album")>-1) {
        //            insertAsExisting=false;
        //        } else{
        //a small hack/automatisation for Chenopodium only
        if (tnb.getTitleCache().split("sec.")[0].trim().equalsIgnoreCase("Chenopodium") &&
                bestMatchingTaxon.getTitleCache().split("sec.")[0].indexOf("Chenopodium L.")>-1) {
            insertAsExisting=true;
        } else {
            insertAsExisting=askIfReuseBestMatchingTaxon(tnb, bestMatchingTaxon, refMods, similarityScore,similarityAuthor);
        }
        //        }

        logDecision(tnb, bestMatchingTaxon, insertAsExisting, refMods);
        return insertAsExisting;
    }

    /**
     * @return
     */
    @SuppressWarnings("rawtypes")
    private List<Taxon> getMatchingTaxa(ITaxonNameBase tnb) {
        //logger.info("getMatchingTaxon");
    	if (tnb.getTitleCache() == null){
    		tnb.setTitleCache(tnb.toString(), tnb.isProtectedTitleCache());
    	}

        Pager<TaxonBase> pager=importer.getTaxonService().findByTitle(TaxonBase.class, tnb.getTitleCache().split("sec.")[0].trim(), MatchMode.BEGINNING, null, null, null, null, null);
        List<TaxonBase>records = pager.getRecords();

        List<Taxon> existingTaxons = new ArrayList<Taxon>();
        for (TaxonBase r:records){
            try{
                Taxon bestMatchingTaxon = (Taxon)r;
                //                System.out.println("best: "+bestMatchingTaxon.getTitleCache());
                if(compareTaxonNameLength(bestMatchingTaxon.getTitleCache().split(".sec")[0],tnb.getTitleCache().split(".sec")[0])) {
                    existingTaxons.add(bestMatchingTaxon);
                }
            }catch(ClassCastException e){logger.warn("classcast exception, might be a synonym, ignore it");}
        }
        Taxon bmt = importer.getTaxonService().findBestMatchingTaxon(tnb.getTitleCache());
        if (!existingTaxons.contains(bmt) && bmt!=null) {
            if(compareTaxonNameLength(bmt.getTitleCache().split(".sec")[0],tnb.getTitleCache().split(".sec")[0])) {
                existingTaxons.add(bmt);
            }
        }
        return existingTaxons;
    }

    /**
     * Check if the found Taxon can reasonnably be the same
     * example: with and without author should match, but the subspecies should not be suggested for a genus
     * */
    private boolean compareTaxonNameLength(String f, String o){
        //logger.info("compareTaxonNameLength");
        boolean lengthOk=false;
        int sizeF = f.length();
        int sizeO = o.length();
        if (sizeO>=sizeF) {
            lengthOk=true;
        }
        if(sizeF>sizeO) {
            if (sizeF-sizeO>10) {
                lengthOk=false;
            } else {
                lengthOk=true;
            }
        }

        //        System.out.println(lengthOk+": compare "+f+" ("+f.length()+") and "+o+" ("+o.length()+")");
        return lengthOk;
    }

    private double similarity(String s1, String s2) {
        //logger.info("similarity");
        //System.out.println("similarity *"+s1+"* vs. *"+s2+"*");
        if(!StringUtils.isEmpty(s1) && !StringUtils.isEmpty(s2)){
            String l1=s1.toLowerCase().trim();
            String l2=s2.toLowerCase().trim();
            if (l1.length() < l2.length()) { // s1 should always be bigger
                String swap = l1; l1 = l2; l2 = swap;
            }
            int bigLen = l1.length();
            if (bigLen == 0) { return 1.0; /* both strings are zero length */ }
            return (bigLen - computeEditDistance(l1, l2)) / (double) bigLen;
        }
        else{
            if(s1!=null && s2!=null){
                if (s1.equalsIgnoreCase(s2)) {
                    return 1;
                }
            }
            return -1;
        }
    }

    private int computeEditDistance(String s1, String s2) {
        //logger.info("computeEditDistance");
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }

    Map<Rank, Taxon> hierarchy = new HashMap<Rank, Taxon>();
    /**
     * @param taxonNameBase
     */
    @SuppressWarnings("rawtypes")
    public void lookForParentNode(NonViralName<?> taxonNameBase, Taxon tax, Reference ref, MyName myName) {
        logger.info("lookForParentNode "+taxonNameBase.getTitleCache()+" for "+myName.toString());
        //System.out.println("LOOK FOR PARENT NODE "+taxonnamebase.toString()+"; "+tax.toString()+"; "+taxonnamebase.getRank());
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
        if (taxonNameBase.getRank().equals(Rank.FORM())){
            handleFormHierarchy(ref, myName, parser);
        }
        else if (taxonNameBase.getRank().equals(Rank.VARIETY())){
            handleVarietyHierarchy(ref, myName, parser);
        }
        else if (taxonNameBase.getRank().equals(Rank.SUBSPECIES())){
            handleSubSpeciesHierarchy(ref, myName, parser);
        }
        else if (taxonNameBase.getRank().equals(Rank.SPECIES())){
            handleSpeciesHierarchy(ref, myName, parser);
        }
        else if (taxonNameBase.getRank().equals(Rank.SUBGENUS())){
            handleSubgenusHierarchy(ref, myName, parser);
        }

        if (taxonNameBase.getRank().equals(Rank.GENUS())){
            handleGenusHierarchy(ref, myName, parser);
        }
        if (taxonNameBase.getRank().equals(Rank.SUBTRIBE())){
            handleSubtribeHierarchy(ref, myName, parser);
        }
        if (taxonNameBase.getRank().equals(Rank.TRIBE())){
            handleTribeHierarchy(ref, myName, parser);
        }

        if (taxonNameBase.getRank().equals(Rank.SUBFAMILY())){
            handleSubfamilyHierarchy(ref, myName, parser);
        }
    }

    /**
     * @param ref
     * @param myName
     * @param parser
     */
    private void handleSubfamilyHierarchy(Reference ref, MyName myName, INonViralNameParser<?> parser) {
        System.out.println("handleSubfamilyHierarchy");
        String parentStr = myName.getFamilyStr();
        Rank r = Rank.FAMILY();
        if(parentStr!=null){

            Taxon parent = null;
            Pager<TaxonBase> taxontest = importer.getTaxonService().findByTitle(TaxonBase.class, parentStr, MatchMode.BEGINNING, null, null, null, null, null);
            for(TaxonBase tb:taxontest.getRecords()){
                try {
                    if (tb.getName().getRank().equals(r)) {
                        parent=CdmBase.deproxy(tb, Taxon.class);
                    }
                    break;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(parent == null) {
                NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
                Taxon tmp = findMatchingTaxon(parentNameName,ref);
                if(tmp ==null)
                {
                    parent=Taxon.NewInstance(parentNameName, ref);
                    importer.getTaxonService().save(parent);
                    parent = CdmBase.deproxy(parent, Taxon.class);
                } else {
                    parent=tmp;
                }
                lookForParentNode(parentNameName, parent, ref,myName);

            }
            hierarchy.put(r,parent);
        }
    }

    /**
     * @param ref
     * @param myName
     * @param parser
     */
    private void handleTribeHierarchy(Reference ref, MyName myName, INonViralNameParser<?> parser) {
        String parentStr = myName.getSubfamilyStr();
        Rank r = Rank.SUBFAMILY();
        if (parentStr == null){
            parentStr = myName.getFamilyStr();
            r = Rank.FAMILY();
        }
        if(parentStr!=null){
            NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
            Taxon parent = Taxon.NewInstance(parentNameName, ref); //sec set null
            //                    importer.getTaxonService().save(parent);
            //                    parent = CdmBase.deproxy(parent, Taxon.class);

            boolean parentDoesNotExists = true;
            for (TaxonNode p : classification.getAllNodes()){
                if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                    parentDoesNotExists = false;
                    parent=CdmBase.deproxy(p.getTaxon(),    Taxon.class);
                    break;
                }
            }
            //                if(parentDoesNotExists) {
            //                    importer.getTaxonService().save(parent);
            //                    parent = CdmBase.deproxy(parent, Taxon.class);
            //                    lookForParentNode(parentNameName, parent, ref,myName);
            //                }
            if(parentDoesNotExists) {
                Taxon tmp = findMatchingTaxon(parentNameName,ref);
                if(tmp ==null)
                {
                    parent=Taxon.NewInstance(parentNameName, ref);
                    importer.getTaxonService().save(parent);
                    parent = CdmBase.deproxy(parent, Taxon.class);
                } else {
                    parent=tmp;
                }
                lookForParentNode(parentNameName, parent, ref,myName);

            }
            hierarchy.put(r,parent);
        }
    }

    /**
     * @param ref
     * @param myName
     * @param parser
     */
    private void handleSubtribeHierarchy(Reference ref, MyName myName, INonViralNameParser<?> parser) {
        String parentStr = myName.getTribeStr();
        Rank r = Rank.TRIBE();
        if (parentStr == null){
            parentStr = myName.getSubfamilyStr();
            r = Rank.SUBFAMILY();
        }
        if (parentStr == null){
            parentStr = myName.getFamilyStr();
            r = Rank.FAMILY();
        }
        if(parentStr!=null){
            NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
            Taxon parent = Taxon.NewInstance(parentNameName, ref); //sec set null
            //                    importer.getTaxonService().save(parent);
            //                    parent = CdmBase.deproxy(parent, Taxon.class);

            boolean parentDoesNotExists = true;
            for (TaxonNode p : classification.getAllNodes()){
                if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                    parentDoesNotExists = false;
                    parent=CdmBase.deproxy(p.getTaxon(),    Taxon.class);

                    break;
                }
            }
            //                if(parentDoesNotExists) {
            //                    importer.getTaxonService().save(parent);
            //                    parent = CdmBase.deproxy(parent, Taxon.class);
            //                    lookForParentNode(parentNameName, parent, ref,myName);
            //                }
            if(parentDoesNotExists) {
                Taxon tmp = findMatchingTaxon(parentNameName,ref);
                if(tmp ==null)
                {
                    parent=Taxon.NewInstance(parentNameName, ref);
                    importer.getTaxonService().save(parent);
                    parent = CdmBase.deproxy(parent, Taxon.class);
                } else {
                    parent=tmp;
                }
                lookForParentNode(parentNameName, parent, ref,myName);

            }
            hierarchy.put(r,parent);
        }
    }

    /**
     * @param ref
     * @param myName
     * @param parser
     */
    private void handleGenusHierarchy(Reference ref, MyName myName, INonViralNameParser<?> parser) {
        String parentStr = myName.getSubtribeStr();
        Rank r = Rank.SUBTRIBE();
        if (parentStr == null){
            parentStr = myName.getTribeStr();
            r = Rank.TRIBE();
        }
        if (parentStr == null){
            parentStr = myName.getSubfamilyStr();
            r = Rank.SUBFAMILY();
        }
        if (parentStr == null){
            parentStr = myName.getFamilyStr();
            r = Rank.FAMILY();
        }
        if(parentStr!=null){
            NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
            Taxon parent = Taxon.NewInstance(parentNameName, ref); //sec set null
            //                    importer.getTaxonService().save(parent);
            //                    parent = CdmBase.deproxy(parent, Taxon.class);

            boolean parentDoesNotExist = true;
            for (TaxonNode p : classification.getAllNodes()){
                if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                    //                        System.out.println(p.getTaxon().getUuid());
                    //                        System.out.println(parent.getUuid());
                    parentDoesNotExist = false;
                    parent=CdmBase.deproxy(p.getTaxon(),    Taxon.class);
                    break;
                }
            }
            //                if(parentDoesNotExists) {
            //                    importer.getTaxonService().save(parent);
            //                    parent = CdmBase.deproxy(parent, Taxon.class);
            //                    lookForParentNode(parentNameName, parent, ref,myName);
            //                }
            if(parentDoesNotExist) {
                Taxon tmp = findMatchingTaxon(parentNameName,ref);
                if(tmp ==null){

                    parent=Taxon.NewInstance(parentNameName, ref);
                    importer.getTaxonService().save(parent);
                    parent = CdmBase.deproxy(parent, Taxon.class);
                } else {
                    parent=tmp;
                }
                lookForParentNode(parentNameName, parent, ref,myName);

            }
            hierarchy.put(r,parent);
        }
    }

    /**
     * @param ref
     * @param myName
     * @param parser
     */
    private void handleSubgenusHierarchy(Reference ref, MyName myName, INonViralNameParser<?> parser) {
        String parentStr = myName.getGenusStr();
        Rank r = Rank.GENUS();

        if(parentStr==null){
            parentStr = myName.getSubtribeStr();
            r = Rank.SUBTRIBE();
        }
        if (parentStr == null){
            parentStr = myName.getTribeStr();
            r = Rank.TRIBE();
        }
        if (parentStr == null){
            parentStr = myName.getSubfamilyStr();
            r = Rank.SUBFAMILY();
        }
        if (parentStr == null){
            parentStr = myName.getFamilyStr();
            r = Rank.FAMILY();
        }
        if(parentStr!=null){
            NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
            Taxon parent = Taxon.NewInstance(parentNameName, ref); //sec set null
            //                    importer.getTaxonService().save(parent);
            //                    parent = CdmBase.deproxy(parent, Taxon.class);

            boolean parentDoesNotExists = true;
            for (TaxonNode p : classification.getAllNodes()){
                if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                    //                        System.out.println(p.getTaxon().getUuid());
                    //                        System.out.println(parent.getUuid());
                    parentDoesNotExists = false;
                    parent=CdmBase.deproxy(p.getTaxon(),    Taxon.class);
                    break;
                }
            }
            //                if(parentDoesNotExists) {
            //                    importer.getTaxonService().save(parent);
            //                    parent = CdmBase.deproxy(parent, Taxon.class);
            //                    lookForParentNode(parentNameName, parent, ref,myName);
            //                }
            if(parentDoesNotExists) {
                Taxon tmp = findMatchingTaxon(parentNameName,ref);
                if(tmp ==null)
                {
                    parent=Taxon.NewInstance(parentNameName, ref);
                    importer.getTaxonService().save(parent);
                    parent = CdmBase.deproxy(parent, Taxon.class);
                } else {
                    parent=tmp;
                }
                lookForParentNode(parentNameName, parent, ref,myName);

            }
            hierarchy.put(r,parent);
        }
    }

    /**
     * @param ref
     * @param myName
     * @param parser
     */
    private void handleSpeciesHierarchy(Reference ref, MyName myName, INonViralNameParser<?> parser) {
        String parentStr = myName.getSubgenusStr();
        Rank r = Rank.SUBGENUS();

        if(parentStr==null){
            parentStr = myName.getGenusStr();
            r = Rank.GENUS();
        }

        if(parentStr==null){
            parentStr = myName.getSubtribeStr();
            r = Rank.SUBTRIBE();
        }
        if (parentStr == null){
            parentStr = myName.getTribeStr();
            r = Rank.TRIBE();
        }
        if (parentStr == null){
            parentStr = myName.getSubfamilyStr();
            r = Rank.SUBFAMILY();
        }
        if (parentStr == null){
            parentStr = myName.getFamilyStr();
            r = Rank.FAMILY();
        }
        if(parentStr!=null){
            Taxon parent = handleParentName(ref, myName, parser, parentStr, r);
            //System.out.println("PUT IN HIERARCHY "+r+", "+parent);
            hierarchy.put(r,parent);
        }
    }

    /**
     * @param ref
     * @param myName
     * @param parser
     */
    private void handleSubSpeciesHierarchy(Reference ref, MyName myName, INonViralNameParser<?> parser) {
        String parentStr = myName.getSpeciesStr();
        Rank r = Rank.SPECIES();


        if(parentStr==null){
            parentStr = myName.getSubgenusStr();
            r = Rank.SUBGENUS();
        }

        if(parentStr==null){
            parentStr = myName.getGenusStr();
            r = Rank.GENUS();
        }

        if(parentStr==null){
            parentStr = myName.getSubtribeStr();
            r = Rank.SUBTRIBE();
        }
        if (parentStr == null){
            parentStr = myName.getTribeStr();
            r = Rank.TRIBE();
        }
        if (parentStr == null){
            parentStr = myName.getSubfamilyStr();
            r = Rank.SUBFAMILY();
        }
        if (parentStr == null){
            parentStr = myName.getFamilyStr();
            r = Rank.FAMILY();
        }
        if(parentStr!=null){
            Taxon parent = handleParentName(ref, myName, parser, parentStr, r);
            //System.out.println("PUT IN HIERARCHY "+r+", "+parent);
            hierarchy.put(r,parent);
        }
    }


    /**
     * @param ref
     * @param myName
     * @param parser
     */
    private void handleFormHierarchy(Reference ref, MyName myName, INonViralNameParser<?> parser) {
        String parentStr = myName.getSubspeciesStr();
        Rank r = Rank.SUBSPECIES();


        if(parentStr==null){
            parentStr = myName.getSpeciesStr();
            r = Rank.SPECIES();
        }

        if(parentStr==null){
            parentStr = myName.getSubgenusStr();
            r = Rank.SUBGENUS();
        }

        if(parentStr==null){
            parentStr = myName.getGenusStr();
            r = Rank.GENUS();
        }

        if(parentStr==null){
            parentStr = myName.getSubtribeStr();
            r = Rank.SUBTRIBE();
        }
        if (parentStr == null){
            parentStr = myName.getTribeStr();
            r = Rank.TRIBE();
        }
        if (parentStr == null){
            parentStr = myName.getSubfamilyStr();
            r = Rank.SUBFAMILY();
        }
        if (parentStr == null){
            parentStr = myName.getFamilyStr();
            r = Rank.FAMILY();
        }
        if(parentStr!=null){
            Taxon parent = handleParentName(ref, myName, parser, parentStr, r);
            //System.out.println("PUT IN HIERARCHY "+r+", "+parent);
            hierarchy.put(r,parent);
        }
    }

    /**
     * @param ref
     * @param myName
     * @param parser
     */
    private void handleVarietyHierarchy(Reference ref, MyName myName, INonViralNameParser<?> parser) {
        String parentStr = myName.getSubspeciesStr();
        Rank r = Rank.SUBSPECIES();

        if(parentStr==null){
            parentStr = myName.getSpeciesStr();
            r = Rank.SPECIES();
        }

        if(parentStr==null){
            parentStr = myName.getSubgenusStr();
            r = Rank.SUBGENUS();
        }

        if(parentStr==null){
            parentStr = myName.getGenusStr();
            r = Rank.GENUS();
        }

        if(parentStr==null){
            parentStr = myName.getSubtribeStr();
            r = Rank.SUBTRIBE();
        }
        if (parentStr == null){
            parentStr = myName.getTribeStr();
            r = Rank.TRIBE();
        }
        if (parentStr == null){
            parentStr = myName.getSubfamilyStr();
            r = Rank.SUBFAMILY();
        }
        if (parentStr == null){
            parentStr = myName.getFamilyStr();
            r = Rank.FAMILY();
        }
        if(parentStr!=null){
            Taxon parent = handleParentName(ref, myName, parser, parentStr, r);
            //System.out.println("PUT IN HIERARCHY "+r+", "+parent);
            hierarchy.put(r,parent);
        }
    }

    /**
     * @param ref
     * @param myName
     * @param parser
     * @param parentStr
     * @param r
     * @return
     */
    private Taxon handleParentName(Reference ref, MyName myName, INonViralNameParser<?> parser, String parentStr, Rank r) {
        NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
        Taxon parent = Taxon.NewInstance(parentNameName, ref); //sec set null
        //                    importer.getTaxonService().save(parent);
        //                    parent = CdmBase.deproxy(parent, Taxon.class);

        boolean parentDoesNotExists = true;
        for (TaxonNode p : classification.getAllNodes()){
            if(p.getTaxon().getTitleCache().split("sec.")[0].trim().equalsIgnoreCase(parent.getTitleCache().split("sec.")[0].trim())) {
                //                        System.out.println(p.getTaxon().getUuid());
                //                        System.out.println(parent.getUuid());
                parentDoesNotExists = false;
                parent=CdmBase.deproxy(p.getTaxon(),    Taxon.class);
                break;
            }
        }
        if(parentDoesNotExists) {
            Taxon tmp = findMatchingTaxon(parentNameName,ref);
            //                    System.out.println("FOUND PARENT "+tmp.toString()+" for "+parentNameName.toString());
            if(tmp ==null){

                parent=Taxon.NewInstance(parentNameName, ref);
                importer.getTaxonService().save(parent);

            } else {
                parent=tmp;
            }
            lookForParentNode(parentNameName, parent, ref,myName);

        }
        return parent;
    }

    private void addNameDifferenceToFile(String originalname, String atomisedname){
        try{
            FileWriter fstream = new FileWriter(TaxonXImport.LOG_FOLDER + "NamesDifferent_"+classification.getTitleCache()+".txt",true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(originalname+" (original) versus "+replaceNull(atomisedname)+" (atomised) \n");
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }
    /**
     * @param name
     * @param author
     * @param nomenclaturalCode2
     * @param rank
     */
    private void addProblemNameToFile(String name, String author, NomenclaturalCode nomenclaturalCode2, Rank rank) {
        try{
            FileWriter fstream = new FileWriter(TaxonXImport.LOG_FOLDER + "NameNotParsed.txt",true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(name+"\t"+replaceNull(author)+"\t"+replaceNull(nomenclaturalCode2)+"\t"+replaceNull(rank)+"\n");
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }


    /**
     * @param tnb
     * @param bestMatchingTaxon
     * @param insertAsExisting
     * @param refMods
     */
    private void logDecision(INonViralName tnb, Taxon bestMatchingTaxon, boolean insertAsExisting, Reference refMods) {
        try{
            FileWriter fstream = new FileWriter(TaxonXImport.LOG_FOLDER + "Decisions_"+classification.toString()+".txt", true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(tnb.getTitleCache() + " sec. " + refMods + "\t" + bestMatchingTaxon.getTitleCache() + "\t" + insertAsExisting + "\n");
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }


    @SuppressWarnings("unused")
    private String replaceNull(Object in){
        if (in == null) {
            return "";
        }
        if (in.getClass().equals(NomenclaturalCode.class)) {
            return ((NomenclaturalCode)in).getTitleCache();
        }
        return in.toString();
    }

    /**
     * @param fullName
     * @param nomenclaturalCode2
     * @param rank
     */
    private void addProblemNameToFile(String type, String name, NomenclaturalCode nomenclaturalCode2, Rank rank, String problems) {
        try{
            FileWriter fstream = new FileWriter(TaxonXImport.LOG_FOLDER + "NameNotParsed_"+classification.getTitleCache()+".txt",true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(type+"\t"+name+"\t"+replaceNull(nomenclaturalCode2)+"\t"+replaceNull(rank)+"\t"+problems+"\n");
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }

}



