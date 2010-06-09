/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.eflora.sapindaceae;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.springframework.stereotype.Component;

import com.sun.org.apache.xml.internal.security.encryption.Reference;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.XmlImportState;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
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
		logger.warn("Checking for Taxa not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
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
		String childName;
		boolean obligatory;
		String idNamespace = "taxon";

		Element elbody= getBodyElement(state.getConfig());
		Namespace tcsNamespace = null; //config.getTcsXmlNamespace();
		
//		childName = "TaxonConcepts";
//		obligatory = false;
//		Element elTaxonConcepts = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);
		
		String tcsElementName = "taxon";
//		List<Element> elTaxonList = elbody.getChildren(tcsElementName, tcsNamespace);
		List<Element> elTaxonList = elbody.getChildren();
		
		Set<String> synonymIdSet = null; //makeSynonymIds(elTaxonConceptList, success);
		//TODO make the same for the Assertions
		
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


//	/**
//	 * @param state
//	 * @param taxon
//	 * @param lastNode2
//	 * @return
//	 */
//	private TaxonNode findParentNode(SapindaceaeImportState state, Taxon taxon, TaxonNode lastParent) {
//		Rank thisRank = taxon.getName().getRank();
//		Rank lastRank = lastParent.getTaxon().getName().getRank();
//		TaxonNode lastNode = lastNode2.getTaxonNodes().iterator().next();
//		if (thisRank.equals(lastRank)){
//			return lastNode;
//		}else if (thisRank.isHigher(lastRank)){
//			return findParentNode(state, taxon, lastNode2);
//		}else{
//			throw new IllegalStateException("Last node should never be lower");
//		}
//
//	}


	/**
	 * @param state
	 * @return 
	 */
	private TaxonomicTree getTree(SapindaceaeImportState state) {
		//FIXME
		return taxonomicTree;
		
	}


	/**
	 * @param state
	 * @param element
	 * @param taxon
	 * @param unhandledNomeclatureChildren 
	 */
	private void handleNomenclature(SapindaceaeImportState state, Element elNomenclature, Taxon taxon, Set<String> unhandledChildren) {
		List<Attribute> attributes = elNomenclature.getAttributes();
		if (! attributes.isEmpty()){
			logger.warn("Nomenclature has unhandled attributes");
		}
		
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
		List<Attribute> attributes = elHomotypes.getAttributes();
		if (! attributes.isEmpty()){
			logger.warn("Homotypes has unhandled attributes");
		}
		
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
				}else{
					logger.warn("Unhandled class value for nom: " + classValue);
				}
				
			}
		}
		
		List<Element> elements = elNom.getChildren();
		for (Element element : elements){
			if (element.getName().equals("name")){
				//TODO
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
	private void handleAcceptedName(SapindaceaeImportState state, Element elNom, Taxon taxon) {
		NonViralName name = (NonViralName)taxon.getName();
		List<Element> elements = elNom.getChildren();
		for (Element element : elements){
			if (element.getName().equals("name")){
				String classValue = element.getAttributeValue("class");
				if (classValue.equalsIgnoreCase("genus")){
					name.setGenusOrUninomial(element.getValue());
				}else if (classValue.equalsIgnoreCase("epithet")){
					name.setSpecificEpithet(element.getValue());
				}else if (classValue.equalsIgnoreCase("author")){
					Team team = Team.NewInstance();
					team.setTitleCache(element.getValue(), true);
					name.setCombinationAuthorTeam(team);
				}else if (classValue.equalsIgnoreCase("pub")){
					ReferenceBase nomRef = ReferenceFactory.newGeneric();
					nomRef.setTitleCache(element.getValue(), true);
					name.setNomenclaturalReference(nomRef);
				}else if (classValue.equalsIgnoreCase("usage")){
					//TODO
				}else{
					logger.warn("Unhandled name class: " +  classValue);
				}
			}else{
				unhandledNomChildren.add(element.getName());
			}
		}

	}


	private void handleDescription(SapindaceaeImportState state, Element elDescription, Taxon taxon, Set<String> unhandledChildren) {
		List<Attribute> attributes = elDescription.getAttributes();
		if (! attributes.isEmpty()){
			logger.warn("Description has unhandled attributes");
		}
		
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
				Feature feature = getFeature(classValue);
				if (feature == null){
					logger.warn("Unhandled feature: " + classValue);
				}else{
					String value = element.getValue();
					TextData textData = TextData.NewInstance(feature);
					textData.putText(value, Language.ENGLISH());
					TaxonDescription description = getDescription(taxon);
					description.addElement(textData);
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
	 * @return
	 */
	private Feature getFeature(String classValue) {
		unhandledFeatureNames.add(classValue);
		// FIXME
		return Feature.DESCRIPTION();
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
					//TODO
					logger.warn("Non scientific names not yet implemented");
					success.setValue(false);
				}
			} catch (Exception e) {
				logger.warn("Value for scientific is not boolean");
			}
			String language = elName.getAttributeValue("language");
			//TODO
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
	
	
	/**
	 * @param elTaxonRelationships
	 * @param success
	 */
	private void makeTaxonRelationships(TaxonBase name, Element elTaxonRelationships, ResultWrapper<Boolean> success){
		//TaxonRelationships are handled in TcsXmlTaxonRelationsImport
		return;
	}
	

	
	private void makeSpecimenCircumscription(TaxonBase name, Element elSpecimenCircumscription, ResultWrapper<Boolean> success){
		if (elSpecimenCircumscription != null){
			logger.warn("makeProviderLink not yet implemented");
			success.setValue(false);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(SapindaceaeImportState state){
		return ! state.getConfig().isDoTaxa();
	}


}
