/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.eflora.sapindaceae;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 *
 */
@Component
public class SapindaceaeTaxonImport  extends SapindaceaeImportBase implements ICdmIO<SapindaceaeImportState> {
	private static final Logger logger = Logger.getLogger(SapindaceaeTaxonImport.class);

	private static int modCount = 30000;
	
	public SapindaceaeTaxonImport(){
		super();
	}
	
	
	@Override
	public boolean doCheck(SapindaceaeImportState state){
		boolean result = true;
		return result;
	}
	
	//FIXME
	private static TaxonomicTree taxonomicTree;
	
	@Override
	public boolean doInvoke(SapindaceaeImportState state){
		logger.info("start make Taxa ...");
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		MapWrapper<TaxonNameBase<?,?>> taxonNameMap = (MapWrapper<TaxonNameBase<?,?>>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		
		ITaxonService taxonService = getTaxonService();

		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);

		Element elbody= getBodyElement(state.getConfig());
		
//		childName = "TaxonConcepts";
//		obligatory = false;
//		Element elTaxonConcepts = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
		
		String tcsElementName = "taxon";
//		List<Element> elTaxonList = elbody.getChildren(tcsElementName, tcsNamespace);
		List<Element> elTaxonList = elbody.getChildren();
		
		int i = 0;
		
		Set<String> unhandledTaxonChildrenNames = new HashSet<String>();
		//title, nomenclature, chromosomes, references, description, uses, notes, habitatecology, distribution, taxon, key
		
		Set<String> unhandledTitleClassess = new HashSet<String>();
		Set<String> unhandledNomeclatureChildren = new HashSet<String>();
		Set<String> unhandledDescriptionChildren = new HashSet<String>();
		
		taxonomicTree = TaxonomicTree.NewInstance("Sapindaceae");
		
		Taxon lastTaxon = null;
		
		
		//for each taxonConcept
		for (Element elTaxon : elTaxonList){
			if ((i++ % modCount) == 0 && i > 1){ logger.info("Taxa handled: " + (i-1));}
			if (! elTaxon.getName().equalsIgnoreCase("taxon")){
				logger.warn("body has element other than 'taxon'");
			}
			verifyNoAttribute(elTaxon);
			
			BotanicalName botanicalName = BotanicalName.NewInstance(Rank.SPECIES());
			Taxon taxon = Taxon.NewInstance(botanicalName, state.getConfig().getSourceReference());
			
			List<Element> children = elTaxon.getChildren();
			for (Element element : children){
				String elName = element.getName();
				
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
					handleUses(state, element, taxon);
				}else if(elName.equalsIgnoreCase("notes")){
					handleNotes(state, element, taxon);
				}else if(elName.equalsIgnoreCase("chromosomes")){
					handleChromosomes(state, element, taxon);
				}else if(elName.equalsIgnoreCase("taxon")){
					logger.warn("A taxon should not be part of a taxon");
				}else{
					unhandledTaxonChildrenNames.add(elName);
				}
				
			}
			handleTaxonRelation(state, taxon, lastTaxon);
			lastTaxon = taxon;
			taxaToSave.add(taxon);

//			testAdditionalElements(elTaxonConcept, elementList);
//			ImportHelper.setOriginalSource(taxonBase, config.getSourceReference(), strId, idNamespace);
//			taxonMap.put(strId, taxonBase);
			
		}
		logger.warn("There are taxa with attributes 'excluded' and 'dubious'");
		
		logger.info("Children for taxa are: " + unhandledTaxonChildrenNames);
		logger.info("Children for nomenclature are: " + unhandledNomeclatureChildren);
		logger.info("Children for description are: " + unhandledDescriptionChildren);
		logger.info("Children for homotypes are: " + unhandledHomotypeChildren);
		logger.info("Children for nom are: " + unhandledNomChildren);
		
		
		//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
		logger.info(i + " taxa handled. Saving ...");
		taxonService.save(taxaToSave);
		logger.info("end makeTaxa ...");
		return success.getValue();
	}
	
	
	/**
	 * @param state
	 * @param element
	 * @param taxon
	 */
	private void handleChromosomes(SapindaceaeImportState state, Element element, Taxon taxon) {
		Feature chromosomeFeature = getFeature("chromosomes", state);
		verifyNoAttribute(element);
		verifyNoChildren(element);
		String value = element.getTextNormalize();
		value = replaceStart(value, "Chromosomes");
		String chromosomesPart = getChromosomesPart(value);
		String references = value.replace(chromosomesPart + ":", "").trim();
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
		Pattern pattern = Pattern.compile("2n\\s*=\\s*d{1,2}:");
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
	private void handleNotes(SapindaceaeImportState state, Element element, Taxon taxon) {
		verifyNoAttribute(element);
		verifyNoChildren(element);
		String value = element.getTextNormalize();
		value = replaceStart(value, "Notes");
		value = replaceStart(value, "Note");
		Annotation annotation = Annotation.NewInstance(value, AnnotationType.EDITORIAL(), Language.ENGLISH());
		taxon.addAnnotation(annotation);
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 */
	private void handleUses(SapindaceaeImportState state, Element element, Taxon taxon) {
		verifyNoAttribute(element);
		verifyNoChildren(element);
		String value = element.getTextNormalize();
		value = replaceStart(value, "Uses");
		Feature feature = Feature.USES();
		addDescriptionElement(taxon, value, feature, null);
		
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param unhandledDescriptionChildren
	 */
	private void handleDistribution(SapindaceaeImportState state, Element element, Taxon taxon) {
		verifyNoAttribute(element);
		verifyNoChildren(element);
		String value = element.getTextNormalize();
		value = replaceStart(value, "Distribution");
		Feature feature = Feature.DISTRIBUTION();
		//TODO parse distribution
		addDescriptionElement(taxon, value, feature, null);
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param unhandledDescriptionChildren
	 */
	private void handleEcology(SapindaceaeImportState state, Element elEcology, Taxon taxon) {
		verifyNoAttribute(elEcology);
		verifyNoChildren(elEcology);
		String value = elEcology.getTextNormalize();
		value = replaceStart(value, "Habitat & Ecologie");
		value = replaceStart(value, "Habitat");
		Feature feature = Feature.ECOLOGY();
		addDescriptionElement(taxon, value, feature, null);
	}



	/**
	 * @param value
	 * @param replacementString
	 */
	private String replaceStart(String value, String replacementString) {
		if (value.startsWith(replacementString) ){
			value = value.substring(replacementString.length());
			value.trim();
		}
		return value;
	}



	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param unhandledNomeclatureChildren 
	 */
	private void handleNomenclature(SapindaceaeImportState state, Element elNomenclature, Taxon taxon, Set<String> unhandledChildren) {
		verifyNoAttribute(elNomenclature);
		
		List<Element> elements = elNomenclature.getChildren();
		for (Element element : elements){
			if (element.getName().equals("homotypes")){
				handleHomotypes(state, element, taxon);
			}else{
				unhandledChildren.add(element.getName());
			}
		}
		
	}



	private static Set<String> unhandledHomotypeChildren = new HashSet<String>();
	/**
	 * @param state
	 * @param element
	 * @param taxon
	 */
	private void handleHomotypes(SapindaceaeImportState state, Element elHomotypes, Taxon taxon) {
		verifyNoAttribute(elHomotypes);
		
		List<Element> elements = elHomotypes.getChildren();
		for (Element element : elements){
			if (element.getName().equals("nom")){
				handleNom(state, element, taxon);
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
	private void handleNom(SapindaceaeImportState state, Element elNom, Taxon taxon) {
		List<Attribute> attributes = elNom.getAttributes();
		for (Attribute attribute : attributes){
			if (! attribute.getName().equalsIgnoreCase("class")){
				logger.warn("Nom has unhandled attribute " +  attribute.getName());
			}else{
				String classValue = attribute.getValue();
				if (classValue.equalsIgnoreCase("acceptedname")){
					handleAcceptedName(state, elNom, taxon);
				}else if (classValue.equalsIgnoreCase("synonym")){
					handleSynonym(state, elNom, taxon);
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
				//TODO name
				//handleNom(state, element, taxon);
			}else{
				unhandledNomChildren.add(element.getName());
			}
		}
		
	}


	/**
	 * @param state
	 * @param elNom
	 * @param taxon
	 */
	private void handleTypeRef(SapindaceaeImportState state, Element elNom, Taxon taxon) {
		verifyNoChildren(elNom);
		String typeRef = elNom.getTextNormalize();
		typeRef = replaceStart(typeRef, "- Type:");
		SpecimenTypeDesignation type = SpecimenTypeDesignation.NewInstance();
		//TODO parse type specimen
		Specimen specimen = Specimen.NewInstance();
		specimen.setTitleCache(typeRef, true);
		type.setTypeSpecimen(specimen);
	}


	/**
	 * @param state
	 * @param elNom
	 * @param taxon
	 */
	boolean firstSynonym = true;
	private void handleSynonym(SapindaceaeImportState state, Element elNom, Taxon taxon) {
		if (firstSynonym == true){
			logger.warn("synonym not yet handled");
		}
		firstSynonym = false;
		
	}


	/**
	 * @param state
	 * @param elNom
	 * @param taxon
	 */
	//body/taxon/
	private void handleAcceptedName(SapindaceaeImportState state, Element elNom, Taxon taxon) {
		NonViralName name = (NonViralName)taxon.getName();
		boolean hasGenusInfo = false;
		//first look for authors as author information is needed for multiple elements
		List<Element> elAuthors = XmlHelp.getAttributedChildListWithValue(elNom, "name", "class", "author");
		handleNameAuthors(elAuthors, name, elNom);
		List<Element> elGenus = XmlHelp.getAttributedChildListWithValue(elNom, "name", "class", "genus");
		if (elGenus.size() > 0){
			hasGenusInfo = true;
		}
		
		
		List<Element> elements = elNom.getChildren();
		elements.removeAll(elAuthors);
		for (Element element : elements){
			if (element.getName().equals("name")){
				String classValue = element.getAttributeValue("class");
				String value = element.getValue().trim();
				if (classValue.equalsIgnoreCase("genus") || classValue.equalsIgnoreCase("family") ){
					name.setGenusOrUninomial(value);
				}else if (classValue.equalsIgnoreCase("subgenus")){
					name.setInfraGenericEpithet(value);
				} else if (classValue.equalsIgnoreCase("epithet")){
					if (hasGenusInfo == true){
						name.setSpecificEpithet(value);
					}else{
						handleInfraspecificEpithet(element, classValue, name);
					}
				}else if (classValue.equalsIgnoreCase("paraut")){
					handleBasionymAuthor(state, element, name);
				}else if (classValue.equalsIgnoreCase("pub")){
					ReferenceBase nomRef = ReferenceFactory.newGeneric();
					nomRef.setTitleCache(value, true);
					String microReference = parseReference(nomRef);
					name.setNomenclaturalMicroReference(microReference);
					name.setNomenclaturalReference(nomRef);
					nomRef.setAuthorTeam((TeamOrPersonBase)name.getCombinationAuthorTeam());
				}else if (classValue.equalsIgnoreCase("usage")){
					ReferenceBase ref = ReferenceFactory.newGeneric();
					ref.setTitleCache(value, true);
					String microReference = parseReference(ref);
					TaxonDescription description = getDescription(taxon);
					TextData textData = TextData.NewInstance(Feature.CITATION());
					textData.addSource(null, null, ref, microReference);
					description.addElement(textData);
				}else if (classValue.equalsIgnoreCase("num")){
					//TODO num
				}else if (classValue.equalsIgnoreCase("typification")){
					//TODO typification
				}else if (classValue.equalsIgnoreCase("author")){
					logger.warn("authors should have been removed already");
				}else{
					logger.warn("Unhandled name class: " +  classValue);
				}
			}else{
				unhandledNomChildren.add(element.getName());
			}
		}

	}


	/**
	 * @param element
	 * @param taxon
	 */
	private void handleInfraspecificEpithet(Element element, String attrValue, NonViralName name) {
		String value = element.getTextNormalize();
		if (value.indexOf("subsp.") != -1){
			//TODO genus and species epi
			name.setInfraSpecificEpithet(value);
			name.setRank(Rank.SUBSPECIES());
		}else{
			logger.warn("Unhandled infraspecific type: " + value);
		}
	}


	/**
	 * @param state
	 * @param element
	 * @param name
	 */
	private void handleBasionymAuthor(SapindaceaeImportState state, Element element, NonViralName name) {
		String strAuthor = element.getValue().trim();
		Pattern reBasionymAuthor = Pattern.compile("^\\(.*\\)$");
		if (reBasionymAuthor.matcher(strAuthor).matches()){
			strAuthor = strAuthor.substring(1, strAuthor.length()-1);
		}else{
			logger.warn("Brackets are missing for original combination author " + strAuthor);
		}
		Team basionymTeam = getTeam(strAuthor);
		name.setBasionymAuthorTeam(basionymTeam);
	}

	private Map<String, Team> teamMap = new HashMap<String, Team>();
	/**
	 * @param elAuthors
	 * @param name
	 * @param elNom 
	 */
	private void handleNameAuthors(List<Element> elAuthors, NonViralName name, Element elNom) {
		if (elAuthors.size() < 1){
			List<Element> pubList = XmlHelp.getAttributedChildListWithValue(elNom, "name", "class", "pub");
			if (pubList.size() > 0){
				logger.warn("No author exists but reference exists for " + name.getTitleCache());
			}else{
				logger.warn("No author exists for name " + name.getTitleCache());
			}
		}else if (elAuthors.size() > 1){
			logger.warn("more than 1 author elements exist");
		}else{
			Element elAuthor = elAuthors.get(0);
			String strAuthor = elAuthor.getValue().trim();
			if (strAuthor.endsWith(",")){
				strAuthor = strAuthor.substring(0, strAuthor.length() -1);
			}
			Team team = getTeam(strAuthor);
			name.setCombinationAuthorTeam(team);
		}
		
	}


	/**
	 * @param strAuthor
	 * @return
	 */
	private Team getTeam(String strAuthor) {
		Team team = teamMap.get(strAuthor);
		if (team == null){
			team = Team.NewInstance();
			team.setTitleCache(strAuthor, true);
		}
		return team;
	}


	private void handleDescription(SapindaceaeImportState state, Element elDescription, Taxon taxon, Set<String> unhandledChildren) {
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
	private void handleChar(SapindaceaeImportState state, Element element, Taxon taxon) {
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


	private static Set<String> unhandledFeatureNames = new HashSet<String>();
	/**
	 * @param classValue
	 * @param state 
	 * @return
	 * @throws UndefinedTransformerMethodException 
	 */
	private Feature getFeature(String classValue, SapindaceaeImportState state) {
		UUID uuid;
		try {
			uuid = state.getTransformer().getFeatureUuid(classValue);
			if (uuid == null){
				logger.info("Uuid is null for " + classValue);
			}
			Feature feature = getFeature(state, uuid, classValue, classValue, classValue);
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
	private void handleTitle(SapindaceaeImportState state, Element element, Taxon taxon, Set<String> unhandledTitleClassess) {
		// attributes
		List<Attribute> attributes = element.getAttributes();
		for (Attribute attribute : attributes){
			if (! attribute.getName().equalsIgnoreCase("class")){
				logger.warn("Title has unhandled attribute " +  attribute.getName());
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
		
	}


	/**
	 * @param value
	 * @param taxonNameBase 
	 */
	private void handleSubGenus(String value, TaxonNameBase taxonNameBase) {
		String name = value.replace("Subgenus", "").trim();
		((NonViralName)taxonNameBase).setInfraGenericEpithet(name);
	}

	private Pattern rexGenusAuthor = Pattern.compile("\\[.*\\]");
//	private Pattern rexGenusAuthor = Pattern.compile("SAPINDA");
	
	/**
	 * @param value
	 * @param taxonNameBase 
	 */
	private void handleGenus(String value, TaxonNameBase taxonName) {
		Matcher matcher = rexGenusAuthor.matcher(value);
		if (matcher.find()){
			String author = matcher.group();
			String genus = value.replace(author, "");
			author = author.substring(1, author.length() - 1);
			Team team = Team.NewInstance();
			team.setTitleCache(author, true);
			NonViralName nvn = (NonViralName)taxonName;
			nvn.setCombinationAuthorTeam(team);
			nvn.setGenusOrUninomial(genus);
		}else{
			logger.warn("No Author match for " + value);
		}
	}
	

	/**
	 * @param taxon
	 * @param lastTaxon
	 */
	private void handleTaxonRelation(SapindaceaeImportState state, Taxon taxon, Taxon lastTaxon) {
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
			}else if (thisRank.equals(lastRank)){
				lastNode.getParent().addChildTaxon(taxon, null, null, null);
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
	private TaxonomicTree getTree(SapindaceaeImportState state) {
		//FIXME
		return taxonomicTree;
		
	}



	/**
	 * @param taxon
	 * @param value
	 * @param feature
	 */
	private void addDescriptionElement(Taxon taxon, String value, Feature feature, String references) {
		TextData textData = TextData.NewInstance(feature);
		textData.putText(value, Language.ENGLISH());
		TaxonDescription description = getDescription(taxon);
		description.addElement(textData);
		if (references != null){
			makeOriginalSourceReferences(textData, ";", references);
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
	private void verifyNoChildren(Element element) {
		List<Element> children = element.getChildren();
		if (! children.isEmpty()){
			logger.warn(element.getName() + " has unhandled children");
		}
	}
	
	
	private String parseReference(ReferenceBase ref){
		String detailResult = null;
		String titleToParse = ref.getTitleCache();
		String reReference = "^\\.{1,}";
//		String reYear = "\\([1-2]{1}[0-9]{3}\\)";
		String reYear = "\\([1-2]{1}[0-9]{3}\\)";
		String reYearPeriod = reYear + "(\\-" + reYear + ")?";
		String reDetail = "\\.{1,10}$";
		
		//pattern for the whole string
		Pattern patReference = Pattern.compile(/*reReference +*/ reYearPeriod /*+ reDetail */);
		Matcher matcher = patReference.matcher(titleToParse);
		if (matcher.find()){
			//pattern for the year
			Pattern patYear = Pattern.compile(reYear);
			matcher = patYear.matcher(titleToParse);
			matcher.find();
			String year = matcher.group();
			int start = matcher.start();
			int end = matcher.end();
			//title and other information precedes the year part
			String title = titleToParse.substring(0, start).trim();
			//detail follows the year part
			String detail = titleToParse.substring(end).trim();
			TimePeriod datePublished = TimePeriod.NewInstance(Integer.valueOf(year.substring(1,5)));
			ref.setDatePublished(datePublished);
			ref.setTitle(title);
			detailResult = detail;
			ref.setProtectedTitleCache(false);
		}else{
			logger.warn("Could not parse reference: " +  titleToParse);
		}
		return detailResult;
		
	}

	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(SapindaceaeImportState state){
		return ! state.getConfig().isDoTaxa();
	}

//**************************** TCS OLD **************************************************	
	

	private boolean hasIsSynonymRelation(Element taxonConcept, Namespace rdfNamespace){
		boolean result = false;
		if (taxonConcept == null || ! "TaxonConcept".equalsIgnoreCase(taxonConcept.getName()) ){
			return false;
		}
		
		String elName = "relationshipCategory";
		Filter filter = new ElementFilter(elName, taxonConcept.getNamespace());
		Iterator<Element> relationshipCategories = taxonConcept.getDescendants(filter);
		while (relationshipCategories.hasNext()){
			Element relationshipCategory = relationshipCategories.next();
			Attribute resource = relationshipCategory.getAttribute("resource", rdfNamespace);
			String isSynonymFor = "http://rs.tdwg.org/ontology/voc/TaxonConcept#IsSynonymFor";
			if (resource != null && isSynonymFor.equalsIgnoreCase(resource.getValue()) ){
				return true;
			}
		}
		return result;
	}
	
	
	/**
	 * @param elTaxonRelationships
	 * @param success
	 */
	private TaxonNameBase<?, ?> makeName(Element elName, NomenclaturalCode code, MapWrapper<? extends TaxonNameBase<?,?>> objectMap, ResultWrapper<Boolean> success){
		TaxonNameBase<?, ?> result = null;
		if (elName != null){
			//scientific
			try {
				String strScientific = elName.getAttributeValue("scientific");
				boolean scientific = Boolean.valueOf(strScientific);
				if (! scientific){
					logger.warn("Non scientific names not yet implemented");
					success.setValue(false);
				}
			} catch (Exception e) {
				logger.warn("Value for scientific is not boolean");
			}
			String language = elName.getAttributeValue("language");
			//TODO language
			//Language
			if (language != null){
				logger.warn("language for name not yet implemented");	
			}
			Class<? extends IdentifiableEntity> clazz = (Class<? extends IdentifiableEntity>)NonViralName.class;
			if (code != null){
				clazz = code.getCdmClass();
			}
			result = (TaxonNameBase<?,?>)makeReferenceType (elName, clazz , objectMap, success);
			if(result == null){
				logger.warn("Name not found");
				success.setValue(false);
			}
		}else{
			logger.warn("Name element is null");
		}
		return result;
	}



}
