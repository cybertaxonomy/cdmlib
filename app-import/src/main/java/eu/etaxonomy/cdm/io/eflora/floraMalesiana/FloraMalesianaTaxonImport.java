/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.eflora.floraMalesiana;

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

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.eflora.floraMalesiana.UnmatchedLeads.UnmatchedLeadsKey;
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
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.IReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;


/**
 * @author a.mueller
 *
 */
@Component
public class FloraMalesianaTaxonImport  extends FloraMalesianaImportBase implements ICdmIO<FloraMalesianaImportState> {
	private static final Logger logger = Logger.getLogger(FloraMalesianaTaxonImport.class);

	private static int modCount = 30000;
	
	public FloraMalesianaTaxonImport(){
		super();
	}
	
	
	@Override
	public boolean doCheck(FloraMalesianaImportState state){
		boolean result = true;
		return result;
	}
	
	@Override
	public boolean doInvoke(FloraMalesianaImportState state){
		logger.info("start make Taxa ...");
		
		//FIXME reset state
		state.putTree(null, null);
		UnmatchedLeads openKeys = state.getOpenKeys();
		if (openKeys == null){
			state.setOpenKeys(UnmatchedLeads.NewInstance());
		}
		
		TransactionStatus tx = startTransaction();
		
		//TODO generally do not store the reference object in the config
		ReferenceBase sourceReference = state.getConfig().getSourceReference();
		getReferenceService().saveOrUpdate(sourceReference);
		
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		ITaxonService taxonService = getTaxonService();
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
				verifyNoAttribute(elTaxon);

				BotanicalName botanicalName = BotanicalName.NewInstance(Rank.SPECIES());
				Taxon taxon = Taxon.NewInstance(botanicalName, state.getConfig().getSourceReference());
				
				List<Element> children = elTaxon.getChildren();
				for (Element element : children){
					handleTaxonElement(state, unhandledTitleClassess, unhandledNomeclatureChildren,	unhandledDescriptionChildren, taxon, element);
				}
				handleTaxonRelation(state, taxon, lastTaxon);
				lastTaxon = taxon;
				taxaToSave.add(taxon);
				state.getConfig().setLastTaxonUuid(lastTaxon.getUuid());
				
			} catch (Exception e) {
				logger.warn("Exception occurred in Sapindacea taxon import: " + e);
				e.printStackTrace();
			}
			
		}
		
		System.out.println(state.getOpenKeys().toString());
		logger.warn("There are taxa with attributes 'excluded' and 'dubious'");
		
		logger.info("Children for nomenclature are: " + unhandledNomeclatureChildren);
		logger.info("Children for description are: " + unhandledDescriptionChildren);
		logger.info("Children for homotypes are: " + unhandledHomotypeChildren);
		logger.info("Children for nom are: " + unhandledNomChildren);
		
		
		//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
		logger.info(i + " taxa handled. Saving ...");
		taxonService.save(taxaToSave);
		commitTransaction(tx);
		
		logger.info("end makeTaxa ...");
		logger.info("start makeKey ...");
//		invokeDoKey(state);
		logger.info("end makeKey ...");
		
		return success.getValue();
	}


	private Taxon getLastTaxon(FloraMalesianaImportState state) {
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
	private void handleTaxonElement(FloraMalesianaImportState state, Set<String> unhandledTitleClassess, Set<String> unhandledNomeclatureChildren, Set<String> unhandledDescriptionChildren, Taxon taxon, Element element) {
		String elName = element.getName();
		AnnotatableEntity lastEntity = null;
		
		
		if (elName.equalsIgnoreCase("title")){
			handleTitle(state, element, taxon, unhandledTitleClassess);
		}else if(elName.equalsIgnoreCase("nomenclature")){
			handleNomenclature(state, element, taxon, unhandledNomeclatureChildren);
		}else if(elName.equalsIgnoreCase("description")){
			handleDescription(state, element, taxon, unhandledDescriptionChildren);
		}else if(elName.equalsIgnoreCase("habitatecology")){
			handleEcology(state, element, taxon);
		}else if(elName.equalsIgnoreCase("distribution")){
			handleDistribution(state, element, taxon);
		}else if(elName.equalsIgnoreCase("uses")){
			lastEntity = handleUses(state, element, taxon);
		}else if(elName.equalsIgnoreCase("notes")){
			lastEntity = handleNotes(state, element, taxon);
		}else if(elName.equalsIgnoreCase("chromosomes")){
			handleChromosomes(state, element, taxon);
		}else if(elName.equalsIgnoreCase("key")){
			handleKeys(state, element, taxon);
		}else if(elName.equalsIgnoreCase("references")){
			handleReferences(state, element, taxon, lastEntity);
		}else if(elName.equalsIgnoreCase("taxon")){
			logger.warn("A taxon should not be part of a taxon");
		}else if(elName.equalsIgnoreCase("homotypes")){
			logger.warn("Homotypes should be included in the nomenclature flag but is child of taxon [XPath: body/taxon/homotypes]");
		}else{
			logger.warn("Unexpected child for taxon: " + elName);
		}
	}
	
	
	private void handleReferences(FloraMalesianaImportState state, Element elReferences, Taxon taxon, AnnotatableEntity lastEntity) {
		verifyNoAttribute(elReferences);
		verifyNoChildren(elReferences, true);
		logger.info("References need to be moved to their parent");
	}


	private void handleKeys(FloraMalesianaImportState state, Element elKey, Taxon taxon) {
		UnmatchedLeads openKeys = state.getOpenKeys();
		
		//title
		String title = makeKeyTitle(elKey);
		
//		//for testing only
//		if (! title.equalsIgnoreCase("KEY TO THE SPECIES")){
//			return;
//		}else{
//			logger.warn("Only species keys handled at the moment");
//		}
		
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
		System.out.println("Unmatched leads:" + openKeys.toString());
	}


	/**
	 * @param state
	 * @param elKey
	 * @param openKeys
	 * @param key
	 * @param elKeychoice
	 * @param taxon 
	 */
	private void handleKeyChoices(FloraMalesianaImportState state, UnmatchedLeads openKeys, PolytomousKey key, Element elKeychoice, Taxon taxon) {
		
		//char Attribute
		Feature feature = handleKeychoiceChar(state, elKeychoice);
		
		//lead
		List<FeatureNode> childNodes = handleKeychoiceLeads(state, key, elKeychoice, taxon, feature);
		
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
	private void handleKeychoiceNum(UnmatchedLeads openKeys, PolytomousKey key, Element elKeychoice, List<FeatureNode> childNodes) {
		Attribute numAttr = elKeychoice.getAttribute("num");
		String num = CdmUtils.removeTrailingDot(numAttr == null? null:numAttr.getValue());
		UnmatchedLeadsKey okk = UnmatchedLeadsKey.NewInstance(key, num);
		Set<FeatureNode> matchingNodes = openKeys.getNodes(okk);
		for (FeatureNode matchingNode : matchingNodes){
			for (FeatureNode childNode : childNodes){
				matchingNode.addChild(childNode);
			}
			openKeys.removeNode(okk, matchingNode);
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
	private List<FeatureNode> handleKeychoiceLeads(	FloraMalesianaImportState state, PolytomousKey key,	Element elKeychoice, Taxon taxon, Feature feature) {
		List<FeatureNode> childNodes = new ArrayList<FeatureNode>();
		List<Element> leads = elKeychoice.getChildren("lead");
		for(Element elLead : leads){
			FeatureNode childNode = handleLead(state, key, elLead, taxon, feature);
			childNodes.add(childNode);
		}
		return childNodes;
	}


	/**
	 * @param state
	 * @param elKeychoice
	 * @return
	 */
	private Feature handleKeychoiceChar(FloraMalesianaImportState state, Element elKeychoice) {
		Feature feature = null;
		Attribute charAttr = elKeychoice.getAttribute("char");
		if (charAttr != null){
			String charStr = charAttr.getValue();
			feature = getFeature(charStr, state);
			elKeychoice.removeAttribute("char");
		}
		return feature;
	}


	private FeatureNode handleLead(FloraMalesianaImportState state, PolytomousKey key, Element elLead, Taxon taxon, Feature feature) {
		FeatureNode node = FeatureNode.NewInstance();
		node.setFeature(feature);
		
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
	private String handleLeadText(Element elLead, FeatureNode node) {
		String text = elLead.getAttributeValue("text").trim();
		if (StringUtils.isBlank(text)){
			logger.warn("Empty text in lead");
		}
		elLead.removeAttribute("text");
		node.addQuestion(Representation.NewInstance(text, text, null, Language.DEFAULT()));
		return text;
	}


	/**
	 * @param state
	 * @param key
	 * @param elLead
	 * @param taxon
	 * @param node
	 */
	private void handleLeadGoto(FloraMalesianaImportState state, PolytomousKey key, Element elLead, Taxon taxon, FeatureNode node) {
		Attribute gotoAttr = elLead.getAttribute("goto");
		if (gotoAttr != null){
			String strGoto = gotoAttr.getValue().trim();
			UnmatchedLeadsKey gotoKey = null;
			if (isInternalNode(strGoto)){
				gotoKey = UnmatchedLeadsKey.NewInstance(key, strGoto);
			}else{
				String taxonKey = makeTaxonKey(strGoto, taxon);
				gotoKey = UnmatchedLeadsKey.NewInstance(taxonKey);
			}
			UnmatchedLeads openKeys = state.getOpenKeys();
			Set<FeatureNode> existingNodes = openKeys.getNodes(gotoKey);
			for (FeatureNode existingNode : existingNodes){
				node.addChild(existingNode);
			}
			openKeys.addKey(gotoKey, node);
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
			if (isInfraSpecificMarker(single)){
				String strSpeciesName = CdmBase.deproxy(taxon.getName(), NonViralName.class).getSpecificEpithet();
				split[i] = strGenusName + " " + strSpeciesName + " ";
			}
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
		}else if (single.length() == 0 || strGenusName.length() == 0){
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
	private void handleChromosomes(FloraMalesianaImportState state, Element element, Taxon taxon) {
		Feature chromosomeFeature = getFeature("chromosomes", state);
		verifyNoAttribute(element);
		verifyNoChildren(element);
		String value = element.getTextNormalize();
		value = replaceStart(value, "Chromosomes");
		String chromosomesPart = getChromosomesPart(value);
		String references = value.replace(chromosomesPart, "").trim();
		chromosomesPart = chromosomesPart.replace(":", "").trim();
		addDescriptionElement(taxon, chromosomesPart, chromosomeFeature, references);	
	}


	/**
	 * @param ref 
	 * @param string 
	 * @return
	 */
	private void makeOriginalSourceReferences(ISourceable sourcable, String splitter, String refAll) {
		String[] splits = refAll.split(splitter);
		for (String strRef: splits){
			ReferenceBase ref = ReferenceFactory.newGeneric();
			ref.setTitleCache(strRef, true);
			String refDetail = parseReference(ref);
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
	private Annotation handleNotes(FloraMalesianaImportState state, Element element, Taxon taxon) {
		verifyNoAttribute(element);
		//removeBr(element);
		verifyNoChildren(element, true);
		String value = element.getTextNormalize();
		value = replaceStart(value, "Notes");
		value = replaceStart(value, "Note");
		Annotation annotation = Annotation.NewInstance(value, AnnotationType.EDITORIAL(), Language.ENGLISH());
		taxon.addAnnotation(annotation);
		return annotation;
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
	private TextData handleUses(FloraMalesianaImportState state, Element element, Taxon taxon) {
		verifyNoAttribute(element);
		verifyNoChildren(element, true);
		String value = element.getTextNormalize();
		value = replaceStart(value, "Uses");
		Feature feature = Feature.USES();
		return addDescriptionElement(taxon, value, feature, null);
		
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param unhandledDescriptionChildren
	 */
	private void handleDistribution(FloraMalesianaImportState state, Element element, Taxon taxon) {
		verifyNoAttribute(element);
		verifyNoChildren(element, true);
		String value = element.getTextNormalize();
		value = replaceStart(value, "Distribution");
		Feature feature = Feature.DISTRIBUTION();
		//distribution parsing almost impossible as there is lots of freetext in the distribution tag
		addDescriptionElement(taxon, value, feature, null);
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param unhandledDescriptionChildren
	 */
	private void handleEcology(FloraMalesianaImportState state, Element elEcology, Taxon taxon) {
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
		addDescriptionElement(taxon, value, feature, null);
	}



	/**
	 * @param value
	 * @param replacementString
	 */
	private String replaceStart(String value, String replacementString) {
		if (value.startsWith(replacementString) ){
			value = value.substring(replacementString.length()).trim();
		}
		if (value.startsWith("-") ){
			value = value.substring("-".length()).trim();
		}
		return value;
	}



	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param unhandledNomeclatureChildren 
	 */
	private void handleNomenclature(FloraMalesianaImportState state, Element elNomenclature, Taxon taxon, Set<String> unhandledChildren) {
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



	private void handleNomenclatureNotes(FloraMalesianaImportState state, Element elNotes, Taxon taxon) {
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
	private void handleHomotypes(FloraMalesianaImportState state, Element elHomotypes, Taxon taxon) {
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
	private HomotypicalGroup handleNom(FloraMalesianaImportState state, Element elNom, Taxon taxon, HomotypicalGroup homotypicalGroup) {
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
					handleTypeRef(state, elNom, taxon);
				}else{
					logger.warn("Unhandled class value for nom: " + classValue);
				}
				
			}
		}
		
		List<Element> elements = elNom.getChildren();
		for (Element element : elements){
			if (element.getName().equals("name")){
				if (taxonBaseClassType == false){
					logger.warn("Name tag not allowed in non taxon nom tag");
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
	 */
	private void handleTypeRef(FloraMalesianaImportState state, Element elNom, Taxon taxon) {
		verifyNoChildren(elNom);
		String typeRef = elNom.getTextNormalize();
		typeRef = replaceStart(typeRef, "-");
		typeRef = replaceStart(typeRef, "—");
		typeRef = replaceStart(typeRef, "\u002d");
		typeRef = replaceStart(typeRef, "\u2013");
		
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
		}
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
		BotanicalName nameType = BotanicalName.NewInstance(null);
		nameType.setTitleCache(typeText);
		((NameTypeDesignation) typeDesignation).setTypeName(nameType);
		//TODO wie können NameTypes den Namen zugeordnet werden?
	}


	/**
	 * @param typeType
	 * @param typeText
	 * @param typeDesignation
	 */
	private void makeSpecimenTypeDesignation(StringBuffer typeType,
			String typeText, TypeDesignationBase typeDesignation) {
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
		specimen.setTitleCache(typeText);
		((SpecimenTypeDesignation) typeDesignation).setTypeSpecimen(specimen);
	}

	private TypeDesignationBase getTypeDesignationAndReference(StringBuffer typeType) {
		TypeDesignationBase result;
		ReferenceBase ref = parseTypeDesignationReference(typeType);
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


	private ReferenceBase parseTypeDesignationReference(StringBuffer typeType) {
		ReferenceBase result = null;
		String reBracketReference = "\\(.*\\)";
		Pattern patBracketReference = Pattern.compile(reBracketReference);
		Matcher matcher = patBracketReference.matcher(typeType);
		if (matcher.find()){
			String refString = matcher.group();
			int start = typeType.indexOf(refString);
			typeType.replace(start, start + refString.length(), "");
			refString = refString.replace("(", "").replace(")", "").trim();
			ReferenceBase ref = ReferenceFactory.newGeneric();
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
	private HomotypicalGroup handleNomTaxon(FloraMalesianaImportState state, Element elNom, Taxon taxon, HomotypicalGroup homotypicalGroup, boolean isSynonym) {
		NonViralName name = makeName(taxon, homotypicalGroup, isSynonym);
		String num = null;
		
		boolean hasGenusInfo = false;
		//first look for authors as author information is needed for references too
//		List<Element> elAuthors = XmlHelp.getAttributedChildListWithValue(elNom, "name", "class", "author");
//		handleNameAuthors(elAuthors, name, elNom);
//		
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
					handleNameAuthors(element, name, elNom);
				}else if (classValue.equalsIgnoreCase("paraut")){
					handleBasionymAuthor(state, element, name, false);
				}else if (classValue.equalsIgnoreCase("infrauthor") || classValue.equalsIgnoreCase("infraut")){
					handleInfrAuthor(state, element, name, true);
				}else if (classValue.equalsIgnoreCase("infrapar") || classValue.equalsIgnoreCase("infrpar") || classValue.equalsIgnoreCase("parauthor") ){
					handleBasionymAuthor(state, element, name, true);
				}else if (classValue.equalsIgnoreCase("infrepi")){
					handleInfrEpi(name, infraRank, value);
				}else if (classValue.equalsIgnoreCase("pub")){
					handleNomenclaturalReference(name, value);
				}else if (classValue.equalsIgnoreCase("usage")){
					handleNameUsage(taxon, name, value);
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
			}else{
				// child element is not "name"
				unhandledNomChildren.add(element.getName());
			}
		}
		
		//handle key
		if (! isSynonym){
			String taxonString = name.getNameCache();
			UnmatchedLeadsKey ulk = UnmatchedLeadsKey.NewInstance(num, taxonString);
			Set<FeatureNode> matchingNodes = state.getOpenKeys().getNodes(ulk);
			for (FeatureNode matchingNode : matchingNodes){
				state.getOpenKeys().removeNode(ulk, matchingNode);
				matchingNode.setTaxon(taxon);
			}
			if (matchingNodes.isEmpty() && num != null){
				logger.warn("Taxon has num but no matching nodes exist: " + num);
			}
		}
		
		//test nom element has no text
		if (StringUtils.isNotBlank(elNom.getTextNormalize().replace("—", "").replace("\u002d","").replace("\u2013", ""))){
			String strElNom = elNom.getTextNormalize();
			Character c = strElNom.charAt(0);
			System.out.println(CharUtils.unicodeEscaped(c));
			logger.warn("Nom tag has text: " + strElNom);
		}
		
		return name.getHomotypicalGroup();
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
	private void handleNameUsage(Taxon taxon, NonViralName name, String value) {
		ReferenceBase ref = ReferenceFactory.newGeneric();
		ref.setTitleCache(value, true);
		String microReference = parseReference(ref);
		TaxonDescription description = getDescription(taxon);
		TextData textData = TextData.NewInstance(Feature.CITATION());
		textData.addSource(null, null, ref, microReference, name, null);
		description.addElement(textData);
	}


	/**
	 * @param name
	 * @param value
	 */
	private void handleNomenclaturalReference(NonViralName name, String value) {
		ReferenceBase nomRef = ReferenceFactory.newGeneric();
		nomRef.setTitleCache(value, true);
		parseNomStatus(nomRef, name);
		String microReference = parseReference(nomRef);
		name.setNomenclaturalMicroReference(microReference);
		name.setNomenclaturalReference(nomRef);
		TeamOrPersonBase team = (TeamOrPersonBase)name.getCombinationAuthorTeam();
		if (team == null){
			logger.warn("Name has nom. ref. but no author team. Name: " + name.getTitleCache() + ", Nom.Ref.: " + value);
		}else{
			nomRef.setAuthorTeam(team);
		}
	}

	private void handleInfrAuthor(FloraMalesianaImportState state, Element elAuthor, NonViralName name, boolean overwrite) {
		String strAuthor = elAuthor.getValue().trim();
		if (strAuthor.endsWith(",")){
			strAuthor = strAuthor.substring(0, strAuthor.length() -1);
		}
		Team[] team = getTeam(strAuthor);
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
	private void handleBasionymAuthor(FloraMalesianaImportState state, Element element, NonViralName name, boolean overwrite) {
		String strAuthor = element.getValue().trim();
		Pattern reBasionymAuthor = Pattern.compile("^\\(.*\\)$");
		if (reBasionymAuthor.matcher(strAuthor).matches()){
			strAuthor = strAuthor.substring(1, strAuthor.length()-1);
		}else{
			logger.warn("Brackets are missing for original combination author " + strAuthor);
		}
		Team[] basionymTeam = getTeam(strAuthor);
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
	private void handleNameAuthors(Element elAuthor, NonViralName name, Element elNom) {
		if (name.getCombinationAuthorTeam() != null){
			logger.warn("Name already has a combination author. Name: " +  name.getTitleCache() + ", Author: " + elAuthor.getTextNormalize());
		}
		String strAuthor = elAuthor.getValue().trim();
		if (strAuthor.endsWith(",")){
			strAuthor = strAuthor.substring(0, strAuthor.length() -1);
		}
		Team[] team = getTeam(strAuthor);
		name.setCombinationAuthorTeam(team[0]);
		name.setExCombinationAuthorTeam(team[1]);
	}


	/**
	 * @param strAuthor
	 * @return
	 */
	private Team[] getTeam(String strAuthor) {
		Team[] result = new Team[2];
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
			result[0] = Team.NewInstance();
			result[0].setTitleCache(strBaseAuthor, true);
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


	/**
	 * @param result
	 * @param strBaseAuthor
	 */
	private Team getUuidTeam(String strBaseAuthor) {
		UUID uuidTeam = teamMap.get(strBaseAuthor);
		return CdmBase.deproxy(getAgentService().find(uuidTeam), Team.class);
	}


	private void handleDescription(FloraMalesianaImportState state, Element elDescription, Taxon taxon, Set<String> unhandledChildren) {
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
	private void handleChar(FloraMalesianaImportState state, Element element, Taxon taxon) {
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
					addDescriptionElement(taxon, value, feature, null);
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
	private TaxonDescription getDescription(Taxon taxon) {
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
	private Feature getFeature(String classValue, FloraMalesianaImportState state) {
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
	private void handleTitle(FloraMalesianaImportState state, Element element, Taxon taxon, Set<String> unhandledTitleClassess) {
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
		UUID uuidTitle = FloraMalesianaTransformer.uuidTitle;
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
	private void handleGenus(String value, TaxonNameBase taxonName) {
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
	private void handleTaxonRelation(FloraMalesianaImportState state, Taxon taxon, Taxon lastTaxon) {
		
		TaxonomicTree tree = getTree(state);
		if (lastTaxon == null){
			tree.addChildTaxon(taxon, null, null, null);
			return;
		}
		Rank thisRank = taxon.getName().getRank();
		Rank lastRank = lastTaxon.getName().getRank();
		if (lastTaxon.getTaxonNodes().size() > 0){
			TaxonNode lastNode = lastTaxon.getTaxonNodes().iterator().next();
			if (thisRank.isLower(lastRank )  ){
				//FIXME
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
	private TaxonomicTree getTree(FloraMalesianaImportState state) {
		TaxonomicTree result = state.getTree(null);
		if (result == null){
			UUID uuid = state.getConfig().getTaxonomicTreeUuid();
			if (uuid == null){
				logger.warn("No classification uuid is defined");
				result = getNewClassification(state);
			}else{
				result = getTaxonTreeService().getTaxonomicTreeByUuid(uuid);
				if (result == null){
					result = getNewClassification(state);
					result.setUuid(uuid);
				}
			}
			state.putTree(null, result);
		}
		return result;
	}


	private TaxonomicTree getNewClassification(FloraMalesianaImportState state) {
		TaxonomicTree result;
		result = TaxonomicTree.NewInstance(state.getConfig().getClassificationTitle());
		state.putTree(null, result);
		return result;
	}



	/**
	 * @param taxon
	 * @param value
	 * @param feature
	 * @return 
	 */
	private TextData addDescriptionElement(Taxon taxon, String value, Feature feature, String references) {
		TextData textData = TextData.NewInstance(feature);
		textData.putText(value, Language.ENGLISH());
		TaxonDescription description = getDescription(taxon);
		description.addElement(textData);
		if (references != null){
			makeOriginalSourceReferences(textData, ";", references);
		}
		return textData;
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
	private void verifyNoChildren(Element element) {
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
	
	

	static NonViralNameParserImpl parser = new NonViralNameParserImpl();
	private void parseNomStatus(ReferenceBase ref, NonViralName nonViralName) {
		String titleToParse = ref.getTitleCache();
		
		
		String noStatusTitle = parser.parseNomStatus(titleToParse, nonViralName);
		if (! noStatusTitle.equals(titleToParse)){
			ref.setTitleCache(noStatusTitle, true);
		}
	}

	
	private String parseReference(ReferenceBase ref){
		String detailResult = null;
		String titleToParse = ref.getTitleCache();
		if (titleToParse.startsWith(";") || titleToParse.startsWith(",")){
			titleToParse = titleToParse.substring(1).trim();
			ref.setTitleCache(titleToParse);
		}
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
			if (detailResult.endsWith(".")){
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
	protected boolean isIgnore(FloraMalesianaImportState state){
		return ! state.getConfig().isDoTaxa();
	}

}
