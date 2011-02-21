/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.eflora;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.eflora.UnmatchedLeads.UnmatchedLeadsKey;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;


/**
 * @author a.mueller
 *
 */
@Component
public class EfloraTaxonImport  extends EfloraImportBase implements ICdmIO<EfloraImportState> {
	private static final Logger logger = Logger.getLogger(EfloraTaxonImport.class);

	private static int modCount = 30000;
	private NonViralNameParserImpl parser = new NonViralNameParserImpl();

	public EfloraTaxonImport(){
		super();
	}
	
	
	@Override
	public boolean doCheck(EfloraImportState state){
		boolean result = true;
		return result;
	}
	
	//TODO make part of state, but state is renewed when invoking the import a second time 
	private UnmatchedLeads unmatchedLeads;
	
	@Override
	public boolean doInvoke(EfloraImportState state){
		logger.info("start make Taxa ...");
		
		//FIXME reset state
		state.putTree(null, null);
//		UnmatchedLeads unmatchedLeads = state.getOpenKeys();
		if (unmatchedLeads == null){
			unmatchedLeads = UnmatchedLeads.NewInstance();
		}
		state.setUnmatchedLeads(unmatchedLeads);
		
		TransactionStatus tx = startTransaction();
		unmatchedLeads.saveToSession(getPolytomousKeyService());
		
		
		//TODO generally do not store the reference object in the config
		Reference sourceReference = state.getConfig().getSourceReference();
		getReferenceService().saveOrUpdate(sourceReference);
		
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);

		Element elbody= getBodyElement(state.getConfig());
		List<Element> elTaxonList = elbody.getChildren();
		
		int i = 0;
		
		Set<String> unhandledTitleClassess = new HashSet<String>();
		Set<String> unhandledNomeclatureChildren = new HashSet<String>();
		Set<String> unhandledDescriptionChildren = new HashSet<String>();
		
		Taxon lastTaxon = getLastTaxon(state);
		
		//for each taxon
		for (Element elTaxon : elTaxonList){
			try {
				if ((i++ % modCount) == 0 && i > 1){ logger.info("Taxa handled: " + (i-1));}
				if (! elTaxon.getName().equalsIgnoreCase("taxon")){
					logger.warn("body has element other than 'taxon'");
				}
				
				BotanicalName botanicalName = BotanicalName.NewInstance(Rank.SPECIES());
				Taxon taxon = Taxon.NewInstance(botanicalName, state.getConfig().getSourceReference());
				
				handleTaxonAttributes(elTaxon, taxon, state);

				
				List<Element> children = elTaxon.getChildren();
				handleTaxonElement(state, unhandledTitleClassess, unhandledNomeclatureChildren,	unhandledDescriptionChildren, taxon, children);
				handleTaxonRelation(state, taxon, lastTaxon);
				lastTaxon = taxon;
				taxaToSave.add(taxon);
				state.getConfig().setLastTaxonUuid(lastTaxon.getUuid());
				
			} catch (Exception e) {
				logger.warn("Exception occurred in Sapindacea taxon import: " + e);
				e.printStackTrace();
			}
			
		}
		
		System.out.println(state.getUnmatchedLeads().toString());
		logger.warn("There are taxa with attributes 'excluded' and 'dubious'");
		
		logger.info("Children for nomenclature are: " + unhandledNomeclatureChildren);
		logger.info("Children for description are: " + unhandledDescriptionChildren);
		logger.info("Children for homotypes are: " + unhandledHomotypeChildren);
		logger.info("Children for nom are: " + unhandledNomChildren);
		
		
		//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
		logger.info(i + " taxa handled. Saving ...");
		getTaxonService().saveOrUpdate(taxaToSave);
		getFeatureTreeService().saveOrUpdateFeatureNodesAll(state.getFeatureNodesToSave());
		state.getFeatureNodesToSave().clear();
		commitTransaction(tx);
		
		logger.info("end makeTaxa ...");
		logger.info("start makeKey ...");
//		invokeDoKey(state);
		logger.info("end makeKey ...");
		
		return success.getValue();
	}


	private void handleTaxonAttributes(Element elTaxon, Taxon taxon, EfloraImportState state) {
		List<Attribute> attrList = elTaxon.getAttributes();
		for (Attribute attr : attrList){
			String attrName = attr.getName();
			String attrValue = attr.getValue();
			if ("class".equals(attrName)){
				if (attrValue.equalsIgnoreCase("dubious") || attrValue.equalsIgnoreCase("DUBIOUS GENUS") || attrValue.equalsIgnoreCase("DOUBTFUL SPECIES")  ){
					taxon.setDoubtful(true);
				}else{
					MarkerType markerType = getMarkerType(state, attrValue);
					if (markerType == null){
						logger.warn("Class attribute value for taxon not yet supported: " + attrValue);
					}else{
						taxon.addMarker(Marker.NewInstance(markerType, true));
					}
				}
			}else if ("num".equals(attrName)){
				logger.warn("num not yet supported");
			}else{
				logger.warn("Attribute " + attrName + " not yet supported for element taxon");
			}
		}

	}


	private Taxon getLastTaxon(EfloraImportState state) {
		if (state.getConfig().getLastTaxonUuid() == null){
			return null;
		}else{
			return (Taxon)getTaxonService().find(state.getConfig().getLastTaxonUuid());
		}
	}


//	private void invokeDoKey(SapindaceaeImportState state) {
//		TransactionStatus tx = startTransaction();
//		
//		Set<FeatureNode> nodesToSave = new HashSet<FeatureNode>();
//		ITaxonService taxonService = getTaxonService();
//		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
//
//		Element elbody= getBodyElement(state.getConfig());
//		List<Element> elTaxonList = elbody.getChildren();
//		
//		int i = 0;
//		
//		//for each taxon
//		for (Element elTaxon : elTaxonList){
//			if ((i++ % modCount) == 0 && i > 1){ logger.info("Taxa handled: " + (i-1));}
//			if (! elTaxon.getName().equalsIgnoreCase("taxon")){
//				continue;
//			}
//			
//			List<Element> children = elTaxon.getChildren("key");
//			for (Element element : children){
//				handleKeys(state, element, null);
//			}
//			nodesToSave.add(taxon);
//
//		}
//		
//	}


	// body/taxon/*
	private void handleTaxonElement(EfloraImportState state, Set<String> unhandledTitleClassess, Set<String> unhandledNomeclatureChildren, Set<String> unhandledDescriptionChildren, Taxon taxon, List<Element> children) {
		AnnotatableEntity lastEntity = null;
		for (Element element : children){
			String elName = element.getName();
			
			if (elName.equalsIgnoreCase("title")){
				handleTitle(state, element, taxon, unhandledTitleClassess);
				lastEntity = null;
			}else if(elName.equalsIgnoreCase("nomenclature")){
				handleNomenclature(state, element, taxon, unhandledNomeclatureChildren);
				lastEntity = null;
			}else if(elName.equalsIgnoreCase("description")){
				handleDescription(state, element, taxon, unhandledDescriptionChildren);
				lastEntity = null;
			}else if(elName.equalsIgnoreCase("habitatecology")){
				lastEntity = handleEcology(state, element, taxon);
			}else if(elName.equalsIgnoreCase("distribution")){
				lastEntity = handleDistribution(state, element, taxon);
			}else if(elName.equalsIgnoreCase("uses")){
				lastEntity = handleUses(state, element, taxon);
			}else if(elName.equalsIgnoreCase("notes")){
				lastEntity = handleTaxonNotes(state, element, taxon);
			}else if(elName.equalsIgnoreCase("chromosomes")){
				lastEntity = handleChromosomes(state, element, taxon);
			}else if(elName.equalsIgnoreCase("vernacularnames")){
				handleVernaculars(state, element, taxon);
			}else if(elName.equalsIgnoreCase("key")){
				lastEntity = handleKeys(state, element, taxon);
			}else if(elName.equalsIgnoreCase("references")){
				handleReferences(state, element, taxon, lastEntity);
				lastEntity = null;
			}else if(elName.equalsIgnoreCase("taxon")){
				logger.warn("A taxon should not be part of a taxon");
			}else if(elName.equalsIgnoreCase("homotypes")){
				logger.warn("Homotypes should be included in the nomenclature flag but is child of taxon [XPath: body/taxon/homotypes]");
			}else{
				logger.warn("Unexpected child for taxon: " + elName);
			}
		}
	}
	
	
	private void handleVernaculars(EfloraImportState state, Element elVernacular, Taxon taxon) {
		verifyNoAttribute(elVernacular);
		verifyNoChildren(elVernacular, false);
		String value = elVernacular.getTextNormalize();
		Feature feature = Feature.COMMON_NAME();
		value = replaceStart(value, "Noms vernaculaires");
		String[] dialects = value.split(";");
		for (String singleDialect : dialects){
			handleSingleDialect(taxon, singleDialect, feature, state);
		}
		return;
	}


	private void handleSingleDialect(Taxon taxon, String singleDialect, Feature feature, EfloraImportState state) {
		singleDialect = singleDialect.trim();
		TaxonDescription description = getDescription(taxon);
		String reDialect = "\\(dial\\.\\s.*\\)";
//		String reDialect = "\\(.*\\)";
		Pattern patDialect = Pattern.compile(reDialect);
		Matcher matcher = patDialect.matcher(singleDialect);
		if (matcher.find()){
			String dialect = singleDialect.substring(matcher.start(), matcher.end());
			dialect = dialect.replace("(dial. ", "").replace(")", "");
			
			Language language = null;
			try {
				language = this.getLanguage(state, state.getTransformer().getLanguageUuid(dialect), dialect, dialect, dialect);
			} catch (UndefinedTransformerMethodException e) {
				logger.error(e.getMessage());
			}
			
			String commonNames = singleDialect.substring(0, matcher.start());
			String[] splitNames = commonNames.split(",");
			for (String commonNameString : splitNames){
				commonNameString = commonNameString.trim();
				CommonTaxonName commonName = CommonTaxonName.NewInstance(commonNameString, language);
				description.addElement(commonName);
			}
		}else{
			logger.warn("No dialect match: " +  singleDialect);
		}
	}


	private void handleReferences(EfloraImportState state, Element elReferences, Taxon taxon, AnnotatableEntity lastEntity) {
		verifyNoAttribute(elReferences);
		verifyNoChildren(elReferences, true);
		String refString = elReferences.getTextNormalize(); 
		if (lastEntity == null){
			logger.warn("No last entity defined: " + refString);
			return;
		}
		
		Annotation annotation = Annotation.NewInstance(refString, AnnotationType.EDITORIAL(), Language.DEFAULT());
		lastEntity.addAnnotation(annotation);
	}


	private PolytomousKey handleKeys(EfloraImportState state, Element elKey, Taxon taxon) {
		UnmatchedLeads openKeys = state.getUnmatchedLeads();
		
		//title
		String title = makeKeyTitle(elKey);
		
		//key
		PolytomousKey key = PolytomousKey.NewTitledInstance(title);
		
		//TODO add covered taxa etc.
		verifyNoAttribute(elKey);
		
		//notes
		makeKeyNotes(elKey, key);
		
		//keycouplets
		List<Element> keychoices = new ArrayList<Element>();
		keychoices.addAll(elKey.getChildren("keycouplet"));
		keychoices.addAll(elKey.getChildren("keychoice"));
		
		
		for (Element elKeychoice : keychoices){
			handleKeyChoices(state, openKeys, key, elKeychoice, taxon);
			elKey.removeContent(elKeychoice);
		}
		
		//
		verifyNoChildren(elKey);
		logger.info("Unmatched leads after key handling:" + openKeys.toString());
		

		if (state.getConfig().isDoPrintKeys()){
			key.print(System.err);
		}
		getPolytomousKeyService().save(key);
		return key;
	}


	/**
	 * @param state
	 * @param elKey
	 * @param openKeys
	 * @param key
	 * @param elKeychoice
	 * @param taxon 
	 */
	private void handleKeyChoices(EfloraImportState state, UnmatchedLeads openKeys, PolytomousKey key, Element elKeychoice, Taxon taxon) {
		
		//char Attribute
		//TODO it's still unclear if char is a feature and needs to be a new attribute 
		//or if it is handled as question. Therefore both cases are handled but feature
		//is finally not yet set
		KeyStatement question = handleKeychoiceChar(state, elKeychoice);
		Feature feature = handleKeychoiceCharAsFeature(state, elKeychoice);
		
		//lead
		List<PolytomousKeyNode> childNodes = handleKeychoiceLeads(state, key, elKeychoice, taxon, question, feature);
		
		//num -> match with unmatched leads
		handleKeychoiceNum(openKeys, key, elKeychoice, childNodes);

		//others
		verifyNoAttribute(elKeychoice);
	}


	/**
	 * @param openKeys
	 * @param key
	 * @param elKeychoice
	 * @param childNodes
	 */
	private void handleKeychoiceNum(UnmatchedLeads openKeys, PolytomousKey key, Element elKeychoice, List<PolytomousKeyNode> childNodes) {
		Attribute numAttr = elKeychoice.getAttribute("num");
		String num = CdmUtils.removeTrailingDot(numAttr == null? "":numAttr.getValue());
		UnmatchedLeadsKey okk = UnmatchedLeadsKey.NewInstance(key, num);
		Set<PolytomousKeyNode> matchingNodes = openKeys.getNodes(okk);
		for (PolytomousKeyNode matchingNode : matchingNodes){
			for (PolytomousKeyNode childNode : childNodes){
				matchingNode.addChild(childNode);
			}
			openKeys.removeNode(okk, matchingNode);
		}
		if (matchingNodes.isEmpty()){
			for (PolytomousKeyNode childNode : childNodes){
				key.getRoot().addChild(childNode);
			}
		}
		
		elKeychoice.removeAttribute("num");
	}


	/**
	 * @param state
	 * @param key
	 * @param elKeychoice
	 * @param taxon
	 * @param feature
	 * @return
	 */
	private List<PolytomousKeyNode> handleKeychoiceLeads(	EfloraImportState state, PolytomousKey key,	Element elKeychoice, Taxon taxon, KeyStatement question, Feature feature) {
		List<PolytomousKeyNode> childNodes = new ArrayList<PolytomousKeyNode>();
		List<Element> leads = elKeychoice.getChildren("lead");
		for(Element elLead : leads){
			PolytomousKeyNode childNode = handleLead(state, key, elLead, taxon, question, feature);
			childNodes.add(childNode);
		}
		return childNodes;
	}


	/**
	 * @param state
	 * @param elKeychoice
	 * @return
	 */
	private KeyStatement handleKeychoiceChar(EfloraImportState state, Element elKeychoice) {
		KeyStatement statement = null;
		Attribute charAttr = elKeychoice.getAttribute("char");
		if (charAttr != null){
			String charStr = charAttr.getValue();
			if (StringUtils.isNotBlank(charStr)){
				statement = KeyStatement.NewInstance(charStr);
			}
			elKeychoice.removeAttribute("char");
		}
		return statement;
	}
	
	/**
	 * @param state
	 * @param elKeychoice
	 * @return
	 */
	private Feature handleKeychoiceCharAsFeature(EfloraImportState state, Element elKeychoice) {
		Feature feature = null;
		Attribute charAttr = elKeychoice.getAttribute("char");
		if (charAttr != null){
			String charStr = charAttr.getValue();
			feature = getFeature(charStr, state);
			elKeychoice.removeAttribute("char");
		}
		return feature;
	}


	private PolytomousKeyNode handleLead(EfloraImportState state, PolytomousKey key, Element elLead, Taxon taxon, KeyStatement question, Feature feature) {
		PolytomousKeyNode node = PolytomousKeyNode.NewInstance();
		//TODO the char attribute in the keychoice is more a feature than a question
		//needs to be discussed on model side
		node.setQuestion(question);
//		node.setFeature(feature);
		
		//text
		String text = handleLeadText(elLead, node);
		
		//num
		handleLeadNum(elLead, text);
		
		//goto
		handleLeadGoto(state, key, elLead, taxon, node);
		
		//others
		verifyNoAttribute(elLead);
		
		return node;
	}


	/**
	 * @param elLead
	 * @param node
	 * @return
	 */
	private String handleLeadText(Element elLead, PolytomousKeyNode node) {
		String text = elLead.getAttributeValue("text").trim();
		if (StringUtils.isBlank(text)){
			logger.warn("Empty text in lead");
		}
		elLead.removeAttribute("text");
		KeyStatement statement = KeyStatement.NewInstance(text);
		node.setStatement(statement);
		return text;
	}


	/**
	 * @param state
	 * @param key
	 * @param elLead
	 * @param taxon
	 * @param node
	 */
	private void handleLeadGoto(EfloraImportState state, PolytomousKey key, Element elLead, Taxon taxon, PolytomousKeyNode node) {
		Attribute gotoAttr = elLead.getAttribute("goto");
		if (gotoAttr != null){
			String strGoto = gotoAttr.getValue().trim();
			//create key
			UnmatchedLeadsKey gotoKey = null;
			if (isInternalNode(strGoto)){
				gotoKey = UnmatchedLeadsKey.NewInstance(key, strGoto);
			}else{
				String taxonKey = makeTaxonKey(strGoto, taxon);
				gotoKey = UnmatchedLeadsKey.NewInstance(taxonKey);
			}
			//
			UnmatchedLeads openKeys = state.getUnmatchedLeads();
			if (gotoKey.isInnerLead()){
				Set<PolytomousKeyNode> existingNodes = openKeys.getNodes(gotoKey);
				for (PolytomousKeyNode existingNode : existingNodes){
					node.addChild(existingNode);
				}
			}
			openKeys.addKey(gotoKey, node);
			//remove attribute (need for consistency check)
			elLead.removeAttribute("goto");
		}else{
			logger.warn("lead has no goto attribute");
		}
	}


	/**
	 * @param elLead
	 * @param text
	 */
	private void handleLeadNum(Element elLead, String text) {
		Attribute numAttr = elLead.getAttribute("num");
		if (numAttr != null){
			//TODO num
			String num = numAttr.getValue();
			elLead.removeAttribute("num");
		}else{
			logger.info("Keychoice has no num attribute: " + text);
		}
	}


	private String makeTaxonKey(String strGoto, Taxon taxon) {
		String result = "";
		if (strGoto == null){
			return "";
		}
		String strGenusName = CdmBase.deproxy(taxon.getName(), NonViralName.class).getGenusOrUninomial();
		strGoto = strGoto.replaceAll("\\([^\\(\\)]*\\)", "");  //replace all brackets
		strGoto = strGoto.replaceAll("\\s+", " "); //replace multiple whitespaces by exactly one whitespace
		
		strGoto = strGoto.trim();  
		String[] split = strGoto.split("\\s");
		for (int i = 0; i<split.length; i++){
			String single = split[i];
			if (isGenusAbbrev(single, strGenusName)){
				split[i] = strGenusName;
			}
//			if (isInfraSpecificMarker(single)){
//				String strSpeciesName = CdmBase.deproxy(taxon.getName(), NonViralName.class).getSpecificEpithet();
//				split[i] = strGenusName + " " + strSpeciesName + " ";
//			}
			result = (result + " " + split[i]).trim();
		}
		return result;
	}


	private boolean isInfraSpecificMarker(String single) {
		try {
			if (Rank.getRankByAbbreviation(single).isInfraSpecific()){
				return true;
			}
		} catch (UnknownCdmTypeException e) {
			return false;
		}
		return false;
	}


	private boolean isGenusAbbrev(String single, String strGenusName) {
		if (! single.matches("[A-Z]\\.?")) {
			return false;
		}else if (single.length() == 0 || strGenusName == null || strGenusName.length() == 0){
			return false; 
		}else{
			return single.charAt(0) == strGenusName.charAt(0);
		}
	}


	private boolean isInternalNode(String strGoto) {
		return CdmUtils.isNumeric(strGoto);
	}


	private void makeKeyNotes(Element keyElement, PolytomousKey key) {
		Element elNotes = keyElement.getChild("notes");
		if (elNotes != null){
			keyElement.removeContent(elNotes);
			String notes = elNotes.getTextNormalize();
			if (StringUtils.isNotBlank(notes)){
				key.addAnnotation(Annotation.NewInstance(notes, AnnotationType.EDITORIAL(), Language.DEFAULT()));
			}
		}
	}


	private String makeKeyTitle(Element keyElement) {
		String title = "- no title - ";
		Attribute titleAttr = keyElement.getAttribute("title");
		keyElement.removeAttribute(titleAttr);
		if (titleAttr == null){
			Element elTitle = keyElement.getChild("keytitle");
			keyElement.removeContent(elTitle);
			if (elTitle != null){
				title = elTitle.getTextNormalize();
			}
		}else{
			title = titleAttr.getValue();
		}
		return title;
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 */
	private TextData handleChromosomes(EfloraImportState state, Element element, Taxon taxon) {
		Feature chromosomeFeature = getFeature("chromosomes", state);
		verifyNoAttribute(element);
		verifyNoChildren(element);
		String value = element.getTextNormalize();
		value = replaceStart(value, "Chromosomes");
		String chromosomesPart = getChromosomesPart(value);
		String references = value.replace(chromosomesPart, "").trim();
		chromosomesPart = chromosomesPart.replace(":", "").trim();
		return addDescriptionElement(state, taxon, chromosomesPart, chromosomeFeature, references);	
	}


	/**
	 * @param ref 
	 * @param string 
	 * @return
	 */
	private void makeOriginalSourceReferences(ISourceable sourcable, String splitter, String refAll) {
		String[] splits = refAll.split(splitter);
		for (String strRef: splits){
			Reference ref = ReferenceFactory.newGeneric();
			ref.setTitleCache(strRef, true);
			String refDetail = parseReferenceYearAndDetail(ref);
			sourcable.addSource(null, null, ref, refDetail);
		}
		
		
//TODO use regex instead
/*		String detailResult = null;
		String titleToParse = ref.getTitleCache();
		String reReference = "^\\.{1,}";
//		String reYear = "\\([1-2]{1}[0-9]{3}\\)";
		String reYear = "\\([1-2]{1}[0-9]{3}\\)";
		String reYearPeriod = reYear + "(-" + reYear + ")+";
		String reDetail = "\\.{1,10}$";
*/		
	}


	/**
	 * @param value
	 * @return
	 */
	private String getChromosomesPart(String str) {
		Pattern pattern = Pattern.compile("2n\\s*=\\s*\\d{1,2}:");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()){
			return matcher.group(0);
		}else{
			logger.warn("Chromosomes could not be parsed: " + str);
		}
		return str;
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 */
	private TextData handleTaxonNotes(EfloraImportState state, Element element, Taxon taxon) {
		TextData result = null;
		verifyNoChildren(element, true);
		//verifyNoAttribute(element);
		List<Attribute> attributes = element.getAttributes();
		for (Attribute attribute : attributes){
			if (! attribute.getName().equalsIgnoreCase("class")){
				logger.warn("Char has unhandled attribute " +  attribute.getName());
			}else{
				String classValue = attribute.getValue();
				result = handleDescriptiveElement(state, element, taxon, classValue);
			}
		}
		//if no class attribute exists, handle as note
		if (attributes.isEmpty()){
			result = handleDescriptiveElement(state, element, taxon, "Note");
		}

		//Annotation annotation = Annotation.NewInstance(value, AnnotationType.EDITORIAL(), Language.ENGLISH());
		//taxon.addAnnotation(annotation);
		return result; //annotation;
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param result
	 * @param attribute
	 * @return
	 */
	private TextData handleDescriptiveElement(EfloraImportState state, Element element, Taxon taxon, String classValue) {
		TextData result = null;
		Feature feature = getFeature(classValue, state);
		if (feature == null){
			logger.warn("Unhandled feature: " + classValue);
		}else{
			String value = element.getValue();
			value = replaceStart(value, "Notes");
			value = replaceStart(value, "Note");
			result = addDescriptionElement(state, taxon, value, feature, null);
		}
		return result;
	}


	private void removeBr(Element element) {
		element.removeChildren("Br");
		element.removeChildren("br");
		element.removeChildren("BR");
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 */
	private TextData handleUses(EfloraImportState state, Element element, Taxon taxon) {
		verifyNoAttribute(element);
		verifyNoChildren(element, true);
		String value = element.getTextNormalize();
		value = replaceStart(value, "Uses");
		Feature feature = Feature.USES();
		return addDescriptionElement(state, taxon, value, feature, null);
		
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param unhandledDescriptionChildren
	 */
	private DescriptionElementBase handleDistribution(EfloraImportState state, Element element, Taxon taxon) {
		verifyNoAttribute(element);
		verifyNoChildren(element, true);
		String value = element.getTextNormalize();
		value = replaceStart(value, "Distribution");
		Feature feature = Feature.DISTRIBUTION();
		//distribution parsing almost impossible as there is lots of freetext in the distribution tag
		return addDescriptionElement(state, taxon, value, feature, null);
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param unhandledDescriptionChildren
	 */
	private TextData handleEcology(EfloraImportState state, Element elEcology, Taxon taxon) {
		verifyNoAttribute(elEcology);
		verifyNoChildren(elEcology, true);
		String value = elEcology.getTextNormalize();
		Feature feature = Feature.ECOLOGY();
		if (value.startsWith("Habitat & Ecology")){
			feature = getFeature("Habitat & Ecology", state);
			value = replaceStart(value, "Habitat & Ecology");
		}else if (value.startsWith("Habitat")){
			value = replaceStart(value, "Habitat");
			feature = getFeature("Habitat", state);
		}
		return addDescriptionElement(state, taxon, value, feature, null);
	}



	/**
	 * @param value
	 * @param replacementString
	 */
	private String replaceStart(String value, String replacementString) {
		if (value.startsWith(replacementString) ){
			value = value.substring(replacementString.length()).trim();
		}
		while (value.startsWith("-") || value.startsWith("–") ){
			value = value.substring("-".length()).trim();
		}
		return value;
	}


	/**
	 * @param value
	 * @param replacementString
	 */
	protected String removeTrailing(String value, String replacementString) {
		if (value == null){
			return null;
		}
		if (value.endsWith(replacementString) ){
			value = value.substring(0, value.length() - replacementString.length()).trim();
		}
		return value;
	}

	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param unhandledNomeclatureChildren 
	 */
	private void handleNomenclature(EfloraImportState state, Element elNomenclature, Taxon taxon, Set<String> unhandledChildren) {
		verifyNoAttribute(elNomenclature);
		
		List<Element> elements = elNomenclature.getChildren();
		for (Element element : elements){
			if (element.getName().equals("homotypes")){
				handleHomotypes(state, element, taxon);
			}else if (element.getName().equals("notes")){
				handleNomenclatureNotes(state, element, taxon);
			}else{
				unhandledChildren.add(element.getName());
			}
		}
		
	}



	private void handleNomenclatureNotes(EfloraImportState state, Element elNotes, Taxon taxon) {
		verifyNoAttribute(elNotes);
		verifyNoChildren(elNotes);
		String notesText = elNotes.getTextNormalize();
		Annotation annotation = Annotation.NewInstance(notesText, AnnotationType.EDITORIAL(), Language.DEFAULT());
		taxon.addAnnotation(annotation);
	}



	private static Set<String> unhandledHomotypeChildren = new HashSet<String>();
	/**
	 * @param state
	 * @param element
	 * @param taxon
	 */
	private void handleHomotypes(EfloraImportState state, Element elHomotypes, Taxon taxon) {
		verifyNoAttribute(elHomotypes);
		
		List<Element> elements = elHomotypes.getChildren();
		HomotypicalGroup homotypicalGroup = null;
		for (Element element : elements){
			if (element.getName().equals("nom")){
				homotypicalGroup = handleNom(state, element, taxon, homotypicalGroup);
			}else{
				unhandledHomotypeChildren.add(element.getName());
			}
		}
		
	}

	private static Set<String> unhandledNomChildren = new HashSet<String>();

	/**
	 * @param state
	 * @param element
	 * @param taxon
	 */
	private HomotypicalGroup handleNom(EfloraImportState state, Element elNom, Taxon taxon, HomotypicalGroup homotypicalGroup) {
		List<Attribute> attributes = elNom.getAttributes();
		
		boolean taxonBaseClassType = false;
		for (Attribute attribute : attributes){
			if (! attribute.getName().equalsIgnoreCase("class")){
				logger.warn("Nom has unhandled attribute " +  attribute.getName());
			}else{
				String classValue = attribute.getValue();
				if (classValue.equalsIgnoreCase("acceptedname")){
					homotypicalGroup = handleNomTaxon(state, elNom, taxon,homotypicalGroup, false);
					taxonBaseClassType = true;
				}else if (classValue.equalsIgnoreCase("synonym")){
					homotypicalGroup = handleNomTaxon(state, elNom, taxon, homotypicalGroup, true);
					taxonBaseClassType = true;
				}else if (classValue.equalsIgnoreCase("typeref")){
					handleTypeRef(state, elNom, taxon, homotypicalGroup);
				}else{
					logger.warn("Unhandled class value for nom: " + classValue);
				}
				
			}
		}
		
		List<Element> elements = elNom.getChildren();
		for (Element element : elements){
			if (element.getName().equals("name") || element.getName().equals("homonym") ){
				if (taxonBaseClassType == false){
					logger.warn("Name or homonym tag not allowed in non taxon nom tag");
				}
			}else{
				unhandledNomChildren.add(element.getName());
			}
		}
		
		return homotypicalGroup;
		
	}

	/**
	 * @param state
	 * @param elNom
	 * @param taxon
	 * @param homotypicalGroup 
	 */
	protected void handleTypeRef(EfloraImportState state, Element elNom, Taxon taxon, HomotypicalGroup homotypicalGroup) {
		verifyNoChildren(elNom);
		String typeRef = elNom.getTextNormalize();
		typeRef = removeStartingTypeRefMinus(typeRef);
		
		String[] split = typeRef.split(":");
		if (split.length < 2){
			logger.warn("typeRef has no ':' : " + typeRef);
		}else if (split.length > 2){
			logger.warn("typeRef has more than 1 ':' : " + typeRef);
		}else{
			StringBuffer typeType = new StringBuffer(split[0]);
			String typeText = split[1].trim();
			TypeDesignationBase typeDesignation = getTypeDesignationAndReference(typeType);
			
			//Name Type Desitnations
			if (typeDesignation instanceof NameTypeDesignation){
				makeNameTypeDesignations(typeType, typeText, typeDesignation);
			}
			//SpecimenTypeDesignations
			else if (typeDesignation instanceof SpecimenTypeDesignation){
				makeSpecimenTypeDesignation(typeType, typeText, typeDesignation);
			}else{
				logger.error("Unhandled type designation class" + typeDesignation.getClass().getName());
			}
			for (TaxonNameBase name : homotypicalGroup.getTypifiedNames()){
				name.addTypeDesignation(typeDesignation, true);
			}
		}
	}


	/**
	 * @param typeRef
	 * @return
	 */
	protected String removeStartingTypeRefMinus(String typeRef) {
		typeRef = replaceStart(typeRef, "-");
		typeRef = replaceStart(typeRef, "—");
		typeRef = replaceStart(typeRef, "\u002d");
		typeRef = replaceStart(typeRef, "\u2013");
		typeRef = replaceStart(typeRef, "--");
		return typeRef;
	}

	/**
	 * @param typeType
	 * @param typeText
	 * @param typeDesignation
	 */
	private void makeNameTypeDesignations(StringBuffer typeType, String typeText, TypeDesignationBase typeDesignation) {
		if (typeType.toString().trim().equalsIgnoreCase("Type")){
			//do nothing
		}else if (typeType.toString().trim().equalsIgnoreCase("Lectotype")){
			typeDesignation.setTypeStatus(SpecimenTypeDesignationStatus.LECTOTYPE());
		}else if (typeType.toString().trim().equalsIgnoreCase("Syntype")){
			typeDesignation.setTypeStatus(SpecimenTypeDesignationStatus.SYNTYPE());
		}else{
			logger.warn("Unhandled type string: " + typeType + "(" + CharUtils.unicodeEscaped(typeType.charAt(0)) + ")");
		}
		//clean
		typeText = cleanNameType(typeText);
		//create name
		BotanicalName nameType = (BotanicalName)parser.parseFullName(typeText, NomenclaturalCode.ICBN, Rank.SPECIES());
		((NameTypeDesignation) typeDesignation).setTypeName(nameType);
		//TODO wie können NameTypes den Namen zugeordnet werden? -  wird aber vom Portal via NameCache matching gemacht
	}


	private String cleanNameType(String typeText) {
		String result;
		String[] split = typeText.split("\\[.*\\].?");
		result = split[0];
		return result;
	}


	/**
	 * @param typeType
	 * @param typeText
	 * @param typeDesignation
	 */
	protected void makeSpecimenTypeDesignation(StringBuffer typeType, String typeText, TypeDesignationBase typeDesignation) {
		if (typeType.toString().trim().equalsIgnoreCase("Type")){
			//do nothing
		}else if (typeType.toString().trim().equalsIgnoreCase("Neotype") || typeType.toString().trim().equalsIgnoreCase("Neotypes")){
			typeDesignation.setTypeStatus(SpecimenTypeDesignationStatus.NEOTYPE());
		}else if (typeType.toString().trim().equalsIgnoreCase("Syntype") || typeType.toString().trim().equalsIgnoreCase("Syntypes")){
			typeDesignation.setTypeStatus(SpecimenTypeDesignationStatus.SYNTYPE());
		}else if (typeType.toString().trim().equalsIgnoreCase("Lectotype")){
			typeDesignation.setTypeStatus(SpecimenTypeDesignationStatus.LECTOTYPE());
		}else if (typeType.toString().trim().equalsIgnoreCase("Paratype")){
			typeDesignation.setTypeStatus(SpecimenTypeDesignationStatus.PARATYPE());
		}else{
			logger.warn("Unhandled type string: " + typeType);
		}
		Specimen specimen = Specimen.NewInstance();
		if (typeText.length() > 255){
			specimen.setTitleCache(typeText.substring(0, 252) + "...", true);
		}else{
			specimen.setTitleCache(typeText, true);
		}
		specimen.addDefinition(typeText, Language.ENGLISH());
		((SpecimenTypeDesignation) typeDesignation).setTypeSpecimen(specimen);
	}

	private TypeDesignationBase getTypeDesignationAndReference(StringBuffer typeType) {
		TypeDesignationBase result;
		Reference ref = parseTypeDesignationReference(typeType);
		if (typeType.indexOf(" species")>-1 || typeType.indexOf("genus")>-1){
			if (typeType.indexOf(" species")>-1 ){
				result = NameTypeDesignation.NewInstance();
				int start = typeType.indexOf(" species");
				typeType.replace(start, start + " species".length(), "");
			}else {
				result = NameTypeDesignation.NewInstance();
				int start = typeType.indexOf(" genus");
				typeType.replace(start, start + " genus".length(), "");
			}
		}else{
			result = SpecimenTypeDesignation.NewInstance();
		}
		result.setCitation(ref);
		return result;
	}


	private Reference parseTypeDesignationReference(StringBuffer typeType) {
		Reference result = null;
		String reBracketReference = "\\(.*\\)";
		Pattern patBracketReference = Pattern.compile(reBracketReference);
		Matcher matcher = patBracketReference.matcher(typeType);
		if (matcher.find()){
			String refString = matcher.group();
			int start = typeType.indexOf(refString);
			typeType.replace(start, start + refString.length(), "");
			refString = refString.replace("(", "").replace(")", "").trim();
			Reference ref = ReferenceFactory.newGeneric();
			ref.setTitleCache(refString, true);
			result = ref;
		}
		return result;
	}


	/**
	 * @param state
	 * @param elNom
	 * @param taxon
	 */
	//body/taxon/
	private HomotypicalGroup handleNomTaxon(EfloraImportState state, Element elNom, Taxon taxon, HomotypicalGroup homotypicalGroup, boolean isSynonym) {
		NonViralName name = makeName(taxon, homotypicalGroup, isSynonym);
		String num = null;
		
		boolean hasGenusInfo = false;
		TeamOrPersonBase lastTeam = null;
		
		//genus
		List<Element> elGenus = XmlHelp.getAttributedChildListWithValue(elNom, "name", "class", "genus");
		if (elGenus.size() > 0){
			hasGenusInfo = true;
		}else{
			logger.debug ("No Synonym Genus");
		}
		//infra rank -> needed to handle authors correctly
		List<Element> elInfraRank = XmlHelp.getAttributedChildListWithValue(elNom, "name", "class", "infrank");
		Rank infraRank = null;
		infraRank = handleInfRank(name, elInfraRank, infraRank);
		
		//get left over elements
		List<Element> elements = elNom.getChildren();
		elements.removeAll(elInfraRank);
		
		for (Element element : elements){
			if (element.getName().equals("name")){
				String classValue = element.getAttributeValue("class");
				String value = element.getValue().trim();
				if (classValue.equalsIgnoreCase("genus") || classValue.equalsIgnoreCase("family") ){
					name.setGenusOrUninomial(value);
				}else if (classValue.equalsIgnoreCase("family") ){
					name.setGenusOrUninomial(value);
					name.setRank(Rank.FAMILY());
				}else if (classValue.equalsIgnoreCase("subgenus")){
					//name.setInfraGenericEpithet(value);
					name.setNameCache(value.replace(":", "").trim());
					name.setRank(Rank.SUBGENUS());
				}else if (classValue.equalsIgnoreCase("epithet") ){
					if (hasGenusInfo == true){
						name.setSpecificEpithet(value);
					}else{
						handleInfraspecificEpithet(element, classValue, name);
					}
				}else if (classValue.equalsIgnoreCase("author")){
					handleNameAuthors(element, name);
				}else if (classValue.equalsIgnoreCase("paraut")){
					handleBasionymAuthor(state, element, name, false);
				}else if (classValue.equalsIgnoreCase("infrauthor") || classValue.equalsIgnoreCase("infraut")){
					handleInfrAuthor(state, element, name, true);
				}else if (classValue.equalsIgnoreCase("infrapar") || classValue.equalsIgnoreCase("infrpar") || classValue.equalsIgnoreCase("parauthor") ){
					handleBasionymAuthor(state, element, name, true);
				}else if (classValue.equalsIgnoreCase("infrepi")){
					handleInfrEpi(name, infraRank, value);
				}else if (classValue.equalsIgnoreCase("pub")){
					lastTeam = handleNomenclaturalReference(name, value);
				}else if (classValue.equalsIgnoreCase("usage")){
					lastTeam = handleNameUsage(taxon, name, value, lastTeam);
				}else if (classValue.equalsIgnoreCase("note")){
					handleNameNote(name, value);
				}else if (classValue.equalsIgnoreCase("num")){
					if (num != null){
						logger.warn("Duplicate num: " + value);
					}else{
						num = value;
					}
					if (isSynonym == true){
						logger.warn("Synonym should not have a num");
					}
				}else if (classValue.equalsIgnoreCase("typification")){
					logger.warn("Typification should not be a nom class");
				}else{
					logger.warn("Unhandled name class: " +  classValue);
				}
			}else if(element.getName().equals("homonym")){
				handleHomonym(state, element, name);
			}else{
				// child element is not "name"
				unhandledNomChildren.add(element.getName());
			}
		}
		
		//handle key
		if (! isSynonym){
			String taxonString = name.getNameCache();
			//try to find matching lead nodes 
			UnmatchedLeadsKey leadsKey = UnmatchedLeadsKey.NewInstance(num, taxonString);
			Set<PolytomousKeyNode> matchingNodes = handleMatchingNodes(state, taxon, leadsKey);
			//same without using the num
			if (num != null){
				UnmatchedLeadsKey noNumLeadsKey = UnmatchedLeadsKey.NewInstance("", taxonString);
				handleMatchingNodes(state, taxon, noNumLeadsKey);
			}
			if (matchingNodes.isEmpty() && num != null){
				logger.warn("Taxon has num but no matching nodes exist: " + num+ ", Key: " + leadsKey.toString());
			}
		}
		
		//test nom element has no text
		if (StringUtils.isNotBlank(elNom.getTextNormalize().replace("—", "").replace("\u002d","").replace("\u2013", ""))){
			String strElNom = elNom.getTextNormalize();
			if ("?".equals(strElNom)){
				handleQuestionMark(name, taxon);
			}
//			Character c = strElNom.charAt(0);
			//System.out.println(CharUtils.unicodeEscaped(c));
			logger.warn("Nom tag has text: " + strElNom);
		}
		
		return name.getHomotypicalGroup();
	}


	private void handleQuestionMark(NonViralName name, Taxon taxon) {
		int count = name.getTaxonBases().size();
		if (count != 1){
			logger.warn("Name has " + count + " taxa. This is not handled for question mark");
		}else{
			TaxonBase taxonBase = (TaxonBase)name.getTaxonBases().iterator().next();
			taxonBase.setDoubtful(true);
		}
	}


	//merge with handleNomTaxon	
	private void handleHomonym(EfloraImportState state, Element elHomonym, NonViralName upperName) {
		verifyNoAttribute(elHomonym);
		
		//hommonym name
		BotanicalName homonymName = BotanicalName.NewInstance(upperName.getRank());
		homonymName.setGenusOrUninomial(upperName.getGenusOrUninomial());
		homonymName.setInfraGenericEpithet(upperName.getInfraGenericEpithet());
		homonymName.setSpecificEpithet(upperName.getSpecificEpithet());
		homonymName.setInfraSpecificEpithet(upperName.getInfraSpecificEpithet());

		for (Element elName : (List<Element>)elHomonym.getChildren("name")){
			String classValue = elName.getAttributeValue("class");
			String value = elName.getValue().trim();
			if (classValue.equalsIgnoreCase("genus") ){
				homonymName.setGenusOrUninomial(value);
			}else if (classValue.equalsIgnoreCase("epithet") ){
				homonymName.setSpecificEpithet(value);
			}else if (classValue.equalsIgnoreCase("author")){
				handleNameAuthors(elName, homonymName);
			}else if (classValue.equalsIgnoreCase("paraut")){
				handleBasionymAuthor(state, elName, homonymName, true);
			}else if (classValue.equalsIgnoreCase("pub")){
				handleNomenclaturalReference(homonymName, value);
			}else if (classValue.equalsIgnoreCase("note")){
				handleNameNote(homonymName, value);
			}else{
				logger.warn("Unhandled class value: " + classValue);
			}
		}
		//TODO verify other information
		

		//rel
		boolean homonymIsLater = false;
		NameRelationshipType relType = NameRelationshipType.LATER_HOMONYM();
		if (upperName.getNomenclaturalReference() != null && homonymName.getNomenclaturalReference() != null){
			TimePeriod homonymYear = homonymName.getNomenclaturalReference().getDatePublished();
			TimePeriod nameYear = upperName.getNomenclaturalReference().getDatePublished();
			homonymIsLater = homonymYear.getStart().compareTo(nameYear.getStart())  > 0;
		}else{
			if (upperName.getNomenclaturalReference() == null){
				logger.warn("Homonym parent does not have a nomenclatural reference or year: " + upperName.getTitleCache());
			}
			if (homonymName.getNomenclaturalReference() == null){
				logger.warn("Homonym does not have a nomenclatural reference or year: " + homonymName.getTitleCache());
			}
		}
		if (homonymIsLater){
			homonymName.addRelationshipToName(upperName, relType, null);
		}else{
			upperName.addRelationshipToName(homonymName, relType, null);
		}
		
	}


	/**
	 * @param state
	 * @param taxon
	 * @param leadsKey
	 * @return
	 */
	private Set<PolytomousKeyNode> handleMatchingNodes(EfloraImportState state, Taxon taxon, UnmatchedLeadsKey leadsKey) {
		Set<PolytomousKeyNode> matchingNodes = state.getUnmatchedLeads().getNodes(leadsKey);
		for (PolytomousKeyNode matchingNode : matchingNodes){
			state.getUnmatchedLeads().removeNode(leadsKey, matchingNode);
			matchingNode.setTaxon(taxon);
			state.getPolytomousKeyNodesToSave().add(matchingNode);
		}
		return matchingNodes;
	}


	private void handleNameNote(NonViralName name, String value) {
		logger.warn("Name note: " + value + ". Available in portal?");
		Annotation annotation = Annotation.NewInstance(value, AnnotationType.EDITORIAL(), Language.DEFAULT());
		name.addAnnotation(annotation);
	}


	/**
	 * @param taxon
	 * @param name
	 * @param value
	 */
	protected TeamOrPersonBase handleNameUsage(Taxon taxon, NonViralName name, String referenceTitle, TeamOrPersonBase lastTeam) {
		Reference ref = ReferenceFactory.newGeneric();
		referenceTitle = removeStartingSymbols(referenceTitle, ref);
		
		ref.setTitleCache(referenceTitle, true);
		String microReference = parseReferenceYearAndDetail(ref);
		TeamOrPersonBase team = getReferenceAuthor(ref);
		parseReferenceType(ref);
		if (team == null){
			team = lastTeam;
		}
		ref.setAuthorTeam(team);
		
		TaxonDescription description = getDescription(taxon);
		TextData textData = TextData.NewInstance(Feature.CITATION());
		textData.addSource(null, null, ref, microReference, name, null);
		description.addElement(textData);
		return team;
	}


	/**
	 * @param referenceTitle
	 * @param ref
	 * @return
	 */
	private String removeStartingSymbols(String referenceTitle,	Reference ref) {
		if (referenceTitle.startsWith(";") || referenceTitle.startsWith(",") || referenceTitle.startsWith(":")){
			referenceTitle = referenceTitle.substring(1).trim();
			ref.setTitleCache(referenceTitle);
		}
		return referenceTitle;
	}


	private void parseReferenceType(Reference ref) {
		String title = ref.getTitle();
		if (title == null){
			return;
		}
		title = title.trim();
		//no in reference
		if (! title.startsWith("in ")){
			ref.setType(ReferenceType.Book);
			return;
		}
		
		title = title.substring(3);
		//in reference
		//no ,
		if (title.indexOf(",") == -1){
			ref.setType(ReferenceType.Article);
			IJournal journal = ReferenceFactory.newJournal();
			journal.setTitle(title);
			ref.setTitle(null);
			ref.setInJournal(journal);
			//return;
		}else{
			//,-references
			ref.setType(ReferenceType.BookSection);
			String[] split = (title).split(",\\s*[A-Z]");
			if (split.length <= 1){
				logger.warn("Can not fully decide what reference type. Guess it is a book section: " + title );
			}
			IBook book = ReferenceFactory.newBook();
			Team bookTeam = Team.NewTitledInstance(split[0].trim(), split[0].trim());
			try {
				title = title.substring(split[0].length() + 1).trim();
			} catch (Exception e) {
				logger.error("ERROR occurred when trying to split title: " +  title + "; split[0]: + " + split[0]);
			}
			book.setTitle(title);
			book.setAuthorTeam(bookTeam);
			book.setDatePublished(ref.getDatePublished());
			ref.setTitle(null);
			ref.setInBook(book);
		}		
	}


	protected Team getReferenceAuthor (Reference ref) {
		boolean isCache = false;
		String referenceTitle = ref.getTitle();
		if (referenceTitle == null){
			isCache = true;
			referenceTitle = ref.getTitleCache();
		}
		//in references
		String[] split = (" " + referenceTitle).split(" in ");
		if (split.length > 1){
			if (StringUtils.isNotBlank(split[0])){
				//' in ' is within the reference string, take the preceding string as the team
				Team team = Team.NewTitledInstance(split[0].trim(), split[0].trim());
				if (! isCache){
					ref.setTitle("in " + split[1]);
				}
				return team;
			}else{
				//string starts with in therefore no author is given
				return null;
			}
		}
		//no ,-reference
		split = referenceTitle.split(",");
		if (split.length < 2){
			//no author is given
			return null;
		}
		
		//,-references
		split = (referenceTitle).split(",\\s*[A-Z]");
		if (split.length > 1){
			Team team = Team.NewTitledInstance(split[0].trim(), split[0].trim());
			if (! isCache){
				ref.setTitle(referenceTitle.substring(split[0].length()+1).trim());
			}
			return team;
		}else{
			logger.warn("Can't decide if a usage has an author: " + referenceTitle );
			return null;
		}
	}


	/**
	 * Replaced by <homonym> tag but still in use for exceptions
	 * @param detail
	 * @param name
	 * @return
	 */
	protected String parseHomonym(String detail, NonViralName name) {
		String result;
		if (detail == null){
			return detail;
		}

		
		//non RE
		String reNon = "(\\s|,)non\\s";
		Pattern patReference = Pattern.compile(reNon);
		Matcher matcher = patReference.matcher(detail);
		if (matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			
			if (detail != null){
				logger.warn("Unhandled non part: " + detail.substring(start));
				return detail;
			}
			
			result = detail.substring(0, start);

			//homonym string
			String homonymString = detail.substring(end);
			
			//hommonym name
			BotanicalName homonymName = BotanicalName.NewInstance(name.getRank());
			homonymName.setGenusOrUninomial(name.getGenusOrUninomial());
			homonymName.setInfraGenericEpithet(name.getInfraGenericEpithet());
			homonymName.setSpecificEpithet(name.getSpecificEpithet());
			homonymName.setInfraSpecificEpithet(name.getInfraSpecificEpithet());
			Reference homonymNomRef = ReferenceFactory.newGeneric();
			homonymNomRef.setTitleCache(homonymString);
			String homonymNomRefDetail = parseReferenceYearAndDetail(homonymNomRef);
			homonymName.setNomenclaturalMicroReference(homonymNomRefDetail);
			String authorTitle = homonymNomRef.getTitleCache();
			Team team = Team.NewTitledInstance(authorTitle, authorTitle);
			homonymNomRef.setAuthorTeam(team);
			homonymNomRef.setTitle("");
			homonymNomRef.setProtectedTitleCache(false);
			
			//rel
			boolean homonymIsLater = false;
			NameRelationshipType relType = NameRelationshipType.LATER_HOMONYM();
			TimePeriod homonymYear = homonymNomRef.getDatePublished();
			if (name.getNomenclaturalReference() != null){
				TimePeriod nameYear = name.getNomenclaturalReference().getDatePublished();
				homonymIsLater = homonymYear.getStart().compareTo(nameYear.getStart())  > 0;
			}else{
				logger.warn("Classification name has no nomenclatural reference");
			}
			if (homonymIsLater){
				homonymName.addRelationshipToName(name, relType, null);
			}else{
				name.addRelationshipToName(homonymName, relType, null);
			}
			
		}else{
			return detail;
		}
		return result;
	}


	/**
	 * @Xpath body/taxon/nomenclature/homotypes/nom/name[@class="pub"]
	 * @param name
	 * @param value
	 */
	protected TeamOrPersonBase handleNomenclaturalReference(NonViralName name, String value) {
		Reference nomRef = ReferenceFactory.newGeneric();
		nomRef.setTitleCache(value, true);
		parseNomStatus(nomRef, name);
		String microReference = parseReferenceYearAndDetail(nomRef);
		name.setNomenclaturalReference(nomRef);
		microReference = parseHomonym(microReference, name);
		name.setNomenclaturalMicroReference(microReference);
		TeamOrPersonBase  team = (TeamOrPersonBase)name.getCombinationAuthorTeam();
		if (team == null){
			logger.warn("Name has nom. ref. but no author team. Name: " + name.getTitleCache() + ", Nom.Ref.: " + value);
		}else{
			nomRef.setAuthorTeam(team);
		}
		return team;
	}

	private void handleInfrAuthor(EfloraImportState state, Element elAuthor, NonViralName name, boolean overwrite) {
		String strAuthor = elAuthor.getValue().trim();
		if (strAuthor.endsWith(",")){
			strAuthor = strAuthor.substring(0, strAuthor.length() -1);
		}
		TeamOrPersonBase[] team = getTeam(strAuthor);
		if (name.getCombinationAuthorTeam() != null && overwrite == false){
			logger.warn("Try to write combination author for a name that already has a combination author. Neglected.");
		}else{
			name.setCombinationAuthorTeam(team[0]);
			name.setExCombinationAuthorTeam(team[1]);
		}
		
		
	}


	/**
	 * Sets the names rank according to the infrank value
	 * @param name
	 * @param elements
	 * @param elInfraRank
	 * @param infraRank
	 * @return
	 */
	private Rank handleInfRank(NonViralName name, List<Element> elInfraRank, Rank infraRank) {
		if (elInfraRank.size() == 1){
			String strRank = elInfraRank.get(0).getTextNormalize();
			try {
				infraRank = Rank.getRankByNameOrAbbreviation(strRank);
			} catch (UnknownCdmTypeException e) {
				try{
					infraRank = Rank.getRankByNameOrAbbreviation(strRank + ".");
				} catch (UnknownCdmTypeException e2) {
					logger.warn("Unknown infrank " + strRank + ". Set infraRank to (null).");
				}
			}
		}else if (elInfraRank.size() > 1){
			logger.warn ("There is more than 1 infrank");
		}
		if (infraRank != null){
			name.setRank(infraRank);
		}
		return infraRank;
	}


	private void handleInfrEpi(NonViralName name, Rank infraRank, String value) {
		if (infraRank != null && infraRank.isInfraSpecific()){
			name.setInfraSpecificEpithet(value);
			if (CdmUtils.isCapital(value)){
				logger.warn("Infraspecific epithet starts with a capital letter: " + value);
			}
		}else if (infraRank != null && infraRank.isInfraGeneric()){
			name.setInfraGenericEpithet(value);
			if (! CdmUtils.isCapital(value)){
				logger.warn("Infrageneric epithet does not start with a capital letter: " + value);
			}
		}else{
			logger.warn("Infrepi could not be handled: " + value);
		}
	}



	/**
	 * Returns the (empty) with the correct homotypical group depending on the taxon status
	 * @param taxon
	 * @param homotypicalGroup
	 * @param isSynonym
	 * @return
	 */
	private NonViralName makeName(Taxon taxon,HomotypicalGroup homotypicalGroup, boolean isSynonym) {
		NonViralName name;
		if (isSynonym){
			name = BotanicalName.NewInstance(Rank.SPECIES(), homotypicalGroup);
			SynonymRelationshipType synonymType = SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF();
			if (taxon.getHomotypicGroup().equals(homotypicalGroup)){
				synonymType = SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF();
			}
			taxon.addSynonymName(name, synonymType);
		}else{
			name = (NonViralName)taxon.getName();
		}
		return name;
	}


	/**
	 * @param element
	 * @param taxon
	 */
	private void handleInfraspecificEpithet(Element element, String attrValue, NonViralName name) {
		String value = element.getTextNormalize();
		if (value.indexOf("subsp.") != -1){
			//TODO genus and species epi
			String infrEpi = value.substring(value.indexOf("subsp.") + 6).trim();
			name.setInfraSpecificEpithet(infrEpi);
			name.setRank(Rank.SUBSPECIES());
		}else if (value.indexOf("var.") != -1){
			//TODO genus and species epi
			String infrEpi = value.substring(value.indexOf("var.") + 4).trim();
			name.setInfraSpecificEpithet(infrEpi);
			name.setRank(Rank.VARIETY());
		}else{
			logger.warn("Unhandled infraspecific type: " + value);
		}
	}


	/**
	 * @param state
	 * @param element
	 * @param name
	 */
	private void handleBasionymAuthor(EfloraImportState state, Element elBasionymAuthor, NonViralName name, boolean overwrite) {
		String strAuthor = elBasionymAuthor.getValue().trim();
		Pattern reBasionymAuthor = Pattern.compile("^\\(.*\\)$");
		if (reBasionymAuthor.matcher(strAuthor).matches()){
			strAuthor = strAuthor.substring(1, strAuthor.length()-1);
		}else{
			logger.warn("Brackets are missing for original combination author " + strAuthor);
		}
		TeamOrPersonBase[] basionymTeam = getTeam(strAuthor);
		if (name.getBasionymAuthorTeam() != null && overwrite == false){
			logger.warn("Try to write basionym author for a name that already has a basionym author. Neglected.");
		}else{
			name.setBasionymAuthorTeam(basionymTeam[0]);
			name.setExBasionymAuthorTeam(basionymTeam[1]);

		}
	}

	private Map<String, UUID> teamMap = new HashMap<String, UUID>();
	/**
	 * @param elAuthors
	 * @param name
	 * @param elNom 
	 */
	private void handleNameAuthors(Element elAuthor, NonViralName name) {
		if (name.getCombinationAuthorTeam() != null){
			logger.warn("Name already has a combination author. Name: " +  name.getTitleCache() + ", Author: " + elAuthor.getTextNormalize());
		}
		String strAuthor = elAuthor.getValue().trim();
		if (strAuthor.endsWith(",")){
			strAuthor = strAuthor.substring(0, strAuthor.length() -1);
		}
		if (strAuthor.indexOf("(") > -1 || strAuthor.indexOf(")") > -1){
			logger.warn("Author has brackets. Basionym authors should be handled in separate tags: " + strAuthor);
		}
		TeamOrPersonBase[] team = getTeam(strAuthor);
		name.setCombinationAuthorTeam(team[0]);
		name.setExCombinationAuthorTeam(team[1]);
	}


	/**
	 * @param strAuthor
	 * @return
	 */
	private TeamOrPersonBase[] getTeam(String strAuthor) {
		TeamOrPersonBase[] result = new TeamOrPersonBase[2];
		String[] split = strAuthor.split(" ex ");
		String strBaseAuthor = null;
		String strExAuthor = null;
		
		if (split.length == 2){
			strBaseAuthor = split[1]; 
			strExAuthor = split[0];	
		}else if (split.length == 1){
			strBaseAuthor = split[0];
		}else{
			logger.warn("Could not parse (ex) author: " + strAuthor);
		}
		result[0] = getUuidTeam(strBaseAuthor);
		if (result[0] == null){
			result[0] = parseSingleTeam(strBaseAuthor);
			teamMap.put(strBaseAuthor, result[0].getUuid());
		}
		if (strExAuthor != null){
			result[1] = getUuidTeam(strExAuthor);
			if (result[1] == null){
				result[1] = Team.NewInstance();
				result[1].setTitleCache(strExAuthor, true);
				teamMap.put(strExAuthor, result[1].getUuid());
			}
		
		}	
		return result;
	}


	protected TeamOrPersonBase parseSingleTeam(String strBaseAuthor) {
		TeamOrPersonBase result;
		String[] split = strBaseAuthor.split("&");
		if (split.length > 1){
			result = Team.NewInstance();
			for (String personString : split){
				Person person = makePerson(personString);
				((Team)result).addTeamMember(person);
			}
		}else{
			result = makePerson(strBaseAuthor.trim());
		}
		return result;
	}


	/**
	 * @param personString
	 * @return
	 */
	private Person makePerson(String personString) {
		personString = personString.trim();
		Person person = Person.NewTitledInstance(personString);
		person.setNomenclaturalTitle(personString);
		return person;
	}


	/**
	 * @param result
	 * @param strBaseAuthor
	 */
	private TeamOrPersonBase getUuidTeam(String strBaseAuthor) {
		UUID uuidTeam = teamMap.get(strBaseAuthor);
		return CdmBase.deproxy(getAgentService().find(uuidTeam), TeamOrPersonBase.class);
	}


	private void handleDescription(EfloraImportState state, Element elDescription, Taxon taxon, Set<String> unhandledChildren) {
		verifyNoAttribute(elDescription);
		
		List<Element> elements = elDescription.getChildren();
		for (Element element : elements){
			if (element.getName().equalsIgnoreCase("char")){
				handleChar(state, element, taxon);
			}else{
				logger.warn("Unhandled description child: " + element.getName());
			}
		}
		
	}
	
	
	/**
	 * @param state
	 * @param element
	 * @param taxon
	 */
	private void handleChar(EfloraImportState state, Element element, Taxon taxon) {
		List<Attribute> attributes = element.getAttributes();
		for (Attribute attribute : attributes){
			if (! attribute.getName().equalsIgnoreCase("class")){
				logger.warn("Char has unhandled attribute " +  attribute.getName());
			}else{
				String classValue = attribute.getValue();
				Feature feature = getFeature(classValue, state);
				if (feature == null){
					logger.warn("Unhandled feature: " + classValue);
				}else{
					String value = element.getValue();
					addDescriptionElement(state, taxon, value, feature, null);
				}
				
			}
		}
		
		List<Element> elements = element.getChildren();
		if (! elements.isEmpty()){
			logger.warn("Char has unhandled children");
		}
	}


	/**
	 * @param taxon
	 * @return
	 */
	protected TaxonDescription getDescription(Taxon taxon) {
		for (TaxonDescription description : taxon.getDescriptions()){
			if (! description.isImageGallery()){
				return description;
			}
		}
		TaxonDescription newDescription = TaxonDescription.NewInstance(taxon);
		return newDescription;
	}


	/**
	 * @param classValue
	 * @param state 
	 * @return
	 * @throws UndefinedTransformerMethodException 
	 */
	private Feature getFeature(String classValue, EfloraImportState state) {
		UUID uuid;
		try {
			uuid = state.getTransformer().getFeatureUuid(classValue);
			if (uuid == null){
				logger.info("Uuid is null for " + classValue);
			}
			String featureText = StringUtils.capitalize(classValue);
			Feature feature = getFeature(state, uuid, featureText, featureText, classValue);
			if (feature == null){
				throw new NullPointerException(classValue + " not recognized as a feature");
			}
			return feature;
		} catch (Exception e) {
			logger.warn("Could not create feature for " + classValue + ": " + e.getMessage()) ;
			return Feature.UNKNOWN();
		}
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param unhandledTitleClassess 
	 */
	private void handleTitle(EfloraImportState state, Element element, Taxon taxon, Set<String> unhandledTitleClassess) {
		// attributes
		List<Attribute> attributes = element.getAttributes();
		for (Attribute attribute : attributes){
			if (! attribute.getName().equalsIgnoreCase("class") ){
				if (! attribute.getName().equalsIgnoreCase("num")){
					logger.warn("Title has unhandled attribute " +  attribute.getName());
				}else{
					//TODO num attribute in taxon
				}
			}else{
				String classValue = attribute.getValue();
				try {
					Rank rank;
					try {
						rank = Rank.getRankByNameOrAbbreviation(classValue);
					} catch (Exception e) {
						//TODO nc
						rank = Rank.getRankByEnglishName(classValue, NomenclaturalCode.ICBN, false);
					}
					taxon.getName().setRank(rank);
					if (rank.equals(Rank.FAMILY()) || rank.equals(Rank.GENUS())){
						handleGenus(element.getValue(), taxon.getName());
					}else if (rank.equals(Rank.SUBGENUS())){
						handleSubGenus(element.getValue(), taxon.getName());
					}else if (rank.equals(Rank.SECTION_BOTANY())){
						handleSection(element.getValue(), taxon.getName());
					}else if (rank.equals(Rank.SPECIES())){
						handleSpecies(element.getValue(), taxon.getName());
					}else if (rank.equals(Rank.SUBSPECIES())){
						handleSubSpecies(element.getValue(), taxon.getName());
					}else if (rank.equals(Rank.VARIETY())){
						handleVariety(element.getValue(), taxon.getName());
					}else{
						logger.warn("Unhandled rank: " + rank.getLabel());
					}
				} catch (UnknownCdmTypeException e) {
					logger.warn("Unknown rank " + classValue);
					unhandledTitleClassess.add(classValue);
				}
			}
		}
		List<Element> elements = element.getChildren();
		if (! elements.isEmpty()){
			logger.warn("Title has unexpected children");
		}
		UUID uuidTitle = EfloraTransformer.uuidTitle;
		ExtensionType titleExtension = this.getExtensionType(state, uuidTitle, "title", "title", "title");
		taxon.addExtension(element.getTextNormalize(), titleExtension);
		
	}


	/**
	 * @param value
	 * @param taxonNameBase 
	 */
	private void handleSubGenus(String value, TaxonNameBase taxonNameBase) {
		String name = value.replace("Subgenus", "").trim();
		((NonViralName)taxonNameBase).setInfraGenericEpithet(name);
	}
	
	/**
	 * @param value
	 * @param taxonNameBase 
	 */
	private void handleSection(String value, TaxonNameBase taxonNameBase) {
		String name = value.replace("Section", "").trim();
		((NonViralName)taxonNameBase).setInfraGenericEpithet(name);
	}
	
	/**
	 * @param value
	 * @param taxonNameBase 
	 */
	private void handleSpecies(String value, TaxonNameBase taxonNameBase) {
		//do nothing
	}
	
	/**
	 * @param value
	 * @param taxonNameBase 
	 */
	private void handleVariety(String value, TaxonNameBase taxonNameBase) {
		//do nothing
	}
	
	/**
	 * @param value
	 * @param taxonNameBase 
	 */
	private void handleSubSpecies(String value, TaxonNameBase taxonNameBase) {
		//do nothing
	}

	
	private Pattern rexGenusAuthor = Pattern.compile("(\\[|\\().*(\\]|\\))");
	
	/**
	 * @param value
	 * @param taxonNameBase 
	 */
	protected void handleGenus(String value, TaxonNameBase taxonName) {
		Matcher matcher = rexGenusAuthor.matcher(value);
		if (matcher.find()){
			String author = matcher.group();
//			String genus = value.replace(author, "");
			author = author.substring(1, author.length() - 1);
			Team team = Team.NewInstance();
			team.setTitleCache(author, true);
			Credit credit = Credit.NewInstance(team, null);
			taxonName.addCredit(credit);
//			NonViralName nvn = (NonViralName)taxonName;
//			nvn.setCombinationAuthorTeam(team);
//			nvn.setGenusOrUninomial(genus);
		}else{
			logger.info("No Author match for " + value);
		}
	}
	

	/**
	 * @param taxon
	 * @param lastTaxon
	 */
	private void handleTaxonRelation(EfloraImportState state, Taxon taxon, Taxon lastTaxon) {
		
		Classification tree = getTree(state);
		if (lastTaxon == null){
			tree.addChildTaxon(taxon, null, null, null);
			return;
		}
		Rank thisRank = taxon.getName().getRank();
		Rank lastRank = lastTaxon.getName().getRank();
		if (lastTaxon.getTaxonNodes().size() > 0){
			TaxonNode lastNode = lastTaxon.getTaxonNodes().iterator().next();
			if (thisRank.isLower(lastRank )  ){
				lastNode.addChildTaxon(taxon, null, null, null);
				fillMissingEpithetsForTaxa(lastTaxon, taxon);
			}else if (thisRank.equals(lastRank)){
				TaxonNode parent = lastNode.getParent();
				if (parent != null){
					parent.addChildTaxon(taxon, null, null, null);
					fillMissingEpithetsForTaxa(parent.getTaxon(), taxon);
				}else{
					tree.addChildTaxon(taxon, null, null, null);
				}
			}else if (thisRank.isHigher(lastRank)){
				handleTaxonRelation(state, taxon, lastNode.getParent().getTaxon());
//				TaxonNode parentNode = handleTaxonRelation(state, taxon, lastNode.getParent().getTaxon());
//				parentNode.addChildTaxon(taxon, null, null, null);
			}
		}else{
			logger.warn("Last taxon has no node");
		}
	}



	/**
	 * @param state
	 * @return 
	 */
	private Classification getTree(EfloraImportState state) {
		Classification result = state.getTree(null);
		if (result == null){
			UUID uuid = state.getConfig().getClassificationUuid();
			if (uuid == null){
				logger.warn("No classification uuid is defined");
				result = getNewClassification(state);
			}else{
				result = getClassificationService().getClassificationByUuid(uuid);
				if (result == null){
					result = getNewClassification(state);
					result.setUuid(uuid);
				}
			}
			state.putTree(null, result);
		}
		return result;
	}


	private Classification getNewClassification(EfloraImportState state) {
		Classification result;
		result = Classification.NewInstance(state.getConfig().getClassificationTitle());
		state.putTree(null, result);
		return result;
	}


	/**
	 * @param state 
	 * @param taxon
	 * @param value
	 * @param feature
	 * @return 
	 */
	private TextData addDescriptionElement(EfloraImportState state, Taxon taxon, String value, Feature feature, String references) {
		TextData textData = TextData.NewInstance(feature);
		Language textLanguage = getDefaultLanguage(state);
		textData.putText(textLanguage, value);
		TaxonDescription description = getDescription(taxon);
		description.addElement(textData);
		if (references != null){
			makeOriginalSourceReferences(textData, ";", references);
		}
		return textData;
	}

	private Language getDefaultLanguage(EfloraImportState state) {
		UUID defaultLanguageUuid = state.getConfig().getDefaultLanguageUuid();
		if (defaultLanguageUuid != null){
			Language result = state.getDefaultLanguage();
			if (result == null || ! result.getUuid().equals(defaultLanguageUuid)){
				result = (Language)getTermService().find(defaultLanguageUuid);
				state.setDefaultLanguage(result);
				if (result == null){
					logger.warn("Default language for " + defaultLanguageUuid +  " does not exist.");
				}
			}
			return result;
		}else{
			return Language.DEFAULT();
		}
	}


	/**
	 * @param elNomenclature
	 */
	private void verifyNoAttribute(Element element) {
		List<Attribute> attributes = element.getAttributes();
		if (! attributes.isEmpty()){
			logger.warn(element.getName() + " has unhandled attributes: " + attributes.get(0).getValue() + "..." );
		}
	}
	
	/**
	 * @param elNomenclature
	 */
	protected void verifyNoChildren(Element element) {
		verifyNoChildren(element, false);
	}
	
	/**
	 * @param elNomenclature
	 */
	private void verifyNoChildren(Element element, boolean ignoreLineBreak) {
		List<Element> children = element.getChildren();
		if (! children.isEmpty()){
			if (ignoreLineBreak == true){
				for (Element child : children){
					if (! child.getName().equalsIgnoreCase("BR")){
						logger.warn(element.getName() + " has unhandled child: " + child.getName());
					}
				}
			}else{
				logger.warn(element.getName() + " has unhandled children");
			}
		}
	}
	
	

	/**
	 * Parses the nomenclatural status from the references titleCache. If a nomenclatural status
	 * exists it is added to the name and the nom. status part of the references title cache is 
	 * removed. Requires protected title cache.
	 * @param ref
	 * @param nonViralName
	 */
	protected void parseNomStatus(Reference ref, NonViralName nonViralName) {
		String titleToParse = ref.getTitleCache();
		
		String noStatusTitle = parser.parseNomStatus(titleToParse, nonViralName);
		if (! noStatusTitle.equals(titleToParse)){
			ref.setTitleCache(noStatusTitle, true);
		}
	}

	
	/**
	 * Extracts the date published part and returns micro reference
	 * @param ref
	 * @return
	 */
	private String parseReferenceYearAndDetail(Reference ref){
		String detailResult = null;
		String titleToParse = ref.getTitleCache();
		titleToParse = removeStartingSymbols(titleToParse, ref);
		String reReference = "^\\.{1,}";
//		String reYear = "\\([1-2]{1}[0-9]{3}\\)";
		String oneMonth = "(Feb.|Dec.|March|June|July)";
		String reYear = oneMonth + "?\\s?[1-2]\\s?[0-9]\\s?[0-9]\\s?[0-9]\\s?";
		String secondYear = "(\\s?[1-2]\\s?[0-9])?\\s?[0-9]\\s?[0-9]\\s?";
		
		String reYearPeriod = "\\(" + reYear + "(\\-" + secondYear + ")?\\)";
		String reDetail = "\\.{1,10}$";
		
		//pattern for the whole string
		Pattern patReference = Pattern.compile(/*reReference +*/ reYearPeriod /*+ reDetail */);
		Matcher matcher = patReference.matcher(titleToParse);
		if (matcher.find()){
			int start = matcher.start();
			int end = matcher.end();
			
			//title and other information precedes the year part
			String title = titleToParse.substring(0, start).trim();
			//detail follows the year part
			String detail = titleToParse.substring(end).trim();
			
			//time period
			String strPeriod = matcher.group().trim();
			strPeriod = strPeriod.substring(1, strPeriod.length()-1);   //remove brackets
			Pattern patStartMonth = Pattern.compile("^" + oneMonth);
			matcher = patStartMonth.matcher(strPeriod);
			strPeriod = strPeriod.replace(" ", "");
			Integer startMonth = null;
			if (matcher.find()){
				end = matcher.end();
				strPeriod = strPeriod.substring(0, end) + " " + strPeriod.substring(end);
				startMonth = getMonth(strPeriod.substring(0, end));
			}
			
			TimePeriod datePublished = TimePeriod.parseString(strPeriod);
			if (startMonth != null){
				datePublished.setStartMonth(startMonth);
			}
			ref.setDatePublished(datePublished);
			ref.setTitle(title);
			detailResult = CdmUtils.removeTrailingDot(detail);
			if (detailResult.endsWith(".") || detailResult.endsWith(";") || detailResult.endsWith(",")  ){
				detailResult = detailResult.substring(0, detailResult.length() -1);
			}
			ref.setProtectedTitleCache(false);
		}else{
			logger.warn("Could not parse reference: " +  titleToParse);
		}
		return detailResult;
		
	}

	
	
	private Integer getMonth(String month) {
		if (month.startsWith("Jan")){
			return 1;
		}else if (month.startsWith("Feb")){
			return 2;
		}else if (month.startsWith("Mar")){
			return 3;
		}else if (month.startsWith("Apr")){
			return 4;
		}else if (month.startsWith("May")){
			return 5;
		}else if (month.startsWith("Jun")){
			return 6;
		}else if (month.startsWith("Jul")){
			return 7;
		}else if (month.startsWith("Aug")){
			return 8;
		}else if (month.startsWith("Sep")){
			return 9;
		}else if (month.startsWith("Oct")){
			return 10;
		}else if (month.startsWith("Nov")){
			return 11;
		}else if (month.startsWith("Dec")){
			return 12;
		}else{
			logger.warn("Month not yet supported: " + month);
			return null;
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(EfloraImportState state){
		return ! state.getConfig().isDoTaxa();
	}

}
