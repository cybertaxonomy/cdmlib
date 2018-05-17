/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.print.out.IPublishOutputModule;

/**
 * Retrieves all necessary data from an {@link IXMLEntityFactory}.
 * 
 * @author n.hoffmann
 * @since Apr 8, 2010
 * @version 1.0
 */
public class XMLHarvester {
	private static final Logger logger = Logger.getLogger(XMLHarvester.class);
	
	private IXMLEntityFactory factory;

	private PublishConfigurator configurator;
	
	private List<SimplifiedFeatureNode> simplifiedFeatureTree;
	
	private IProgressMonitor progressMonitor;
	
	/**
	 * 
	 * @param configurator
	 */
	public XMLHarvester(PublishConfigurator configurator){
		this.configurator = configurator;
		this.progressMonitor = configurator.getProgressMonitor();
		this.factory = configurator.getFactory();
		
		Element featureTreeElement = factory.getFeatureTree(configurator.getFeatureTreeUuid());
		createSimplifiedFeatureTree(featureTreeElement);
	}
	
	private void createSimplifiedFeatureTree(Element featureTreeElement) {
		Element root = featureTreeElement.getChild("root");
		
		Element realRoot = factory.getFeatureNode(XMLHelper.getUuid(root));
		
		progressMonitor.subTask("Generating simplified Feature Tree.");
		simplifiedFeatureTree = featureTreeRecursive(realRoot);
		progressMonitor.worked(1);
		
		logger.info("Simplified FeeatureTree created");
	}
	
	private List<SimplifiedFeatureNode> featureTreeRecursive(Element featureNode){
		List<SimplifiedFeatureNode> result = new ArrayList<SimplifiedFeatureNode>();

		if(featureNode != null){
			Element children = featureNode.getChild("children");

			if (children != null){
				List<Element> childFeatureNodes = children.getChildren();

				for(Element childNode : childFeatureNodes){

					UUID uuid = XMLHelper.getUuid(childNode);
					Element featureNodeElement = factory.getFeatureNode(uuid);
					Element featureElement = factory.getFeatureForFeatureNode(uuid);


					try {
						Element featureTitleCache = (Element) XPath.selectSingleNode(featureElement, "//Feature/titleCache");

						logger.info("The featureNode uuid is " + uuid);
						logger.info("The feature element name is " + featureTitleCache.getValue());
						logger.info("The feature title cache text french is " + featureTitleCache.getText().toLowerCase(Locale.FRENCH));
						logger.info("The feature title cache value french is " + featureTitleCache.getValue().toLowerCase(Locale.FRENCH));

						featureTitleCache.setText(featureTitleCache.getText().toLowerCase(Locale.FRENCH));

					} catch (JDOMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					////We set it to French here but this isn't the correct place the Feature/titleCache
					featureElement.setName(featureElement.getName().toLowerCase(Locale.FRENCH));
					SimplifiedFeatureNode simplifiedFeatureNode = new SimplifiedFeatureNode(featureElement, featureTreeRecursive(featureNodeElement));				
					result.add(simplifiedFeatureNode);
				}
			}
		}

		return result;
	}
	
	private class SimplifiedFeatureNode{
		private Element featureElement;
		private List<SimplifiedFeatureNode> children;
		
		public SimplifiedFeatureNode(Element featureElement, List<SimplifiedFeatureNode> children){
			this.featureElement = featureElement;
			this.children = children;
		}

		/**
		 * @return the uuid
		 */
		public Element getFeatureElement() {
			return featureElement;
		}

		/**
		 * @return the children
		 */
		public List<SimplifiedFeatureNode> getChildren() {
			return children;
		}
	}

	/**
	 * Commences harvesting the given {@link List} of taxonNodeElements
	 * 
	 * @param taxonNodeElements
	 * @return a {@link Document} containing the necessary XML needed by the {@link IPublishOutputModule IPublishOutputModules}
	 */
	public Document harvest(List<Element> taxonNodeElements){
		
		Element root = new Element(IXMLElements.ROOT);
		
		for(Element taxonNodeElement : taxonNodeElements){
			
			logger.warn("Adding taxonNodeElement " + taxonNodeElement.getChildText("uuid")); 
			
			//temporarily filter c15e12c1-6118-4929-aed0-b0cc90f5ab22 as it's causing a lazyInitializationException
			if (!taxonNodeElement.getChildText("uuid").equals("c15e12c1-6118-4929-aed0-b0cc90f5ab22")) {
			taxonNodeElement.detach();
			
			populateTreeNodeContainer(taxonNodeElement);
			
			root.addContent(taxonNodeElement);
			}
			
		}
		
		
		Document result = new Document();
		
		result.addContent(root);
		
		cleanDateFields(result);
		
		
		return result;
	}
	
	/**
	 * FIXME
	 * This is a hack to circumvent problems with the serialized version of
	 * datePublished objects. Remove this once this was fixed in the library
	 * @param element the context 
	 */
	@Deprecated
	private void cleanDateFields(Object context) {
		String path = "//datePublished/start";
		
		try {
			List<Element> nodes = XPath.selectNodes(context, path);
			
			for(Element node : nodes){
				
				String textWithRubbish = node.getText() ;
				
				if (textWithRubbish.length() > 5) {
				String cleanedText = textWithRubbish.substring(0, 4);
				node.setText(cleanedText);	
				} 
				/*else {
					
					Element parentOfParent = (Element) node.getParent().getParent().getParent();
					
					if (parentOfParent.getName().equals("inReference")) {
					List<Element> parentNodes = XPath.selectNodes(parentOfParent, "//nomenclaturalReference/titleCache");					
					for(Element parentNode : parentNodes){
					logger.error("Problem with date for node  with titleCache: " + parentNode.getText());
					}
					}
				}*/
				
				/*Element parent = (Element) node.getParent().getParent();

				if(parent.getName().equals("citation")){
					Element parent2 = (Element) parent.getParent();
					parent2.setAttribute("sort", cleanedText);
				}*/
			}
		} catch (Exception e) {
			logger.error("Error trying to clean date published field", e);
		}
	}

	/**
	 * Get all additional content that is not included in taxon node initialization
	 * 
	 * @param container
	 */
	private void populateTreeNodeContainer(Element taxonNodeElement){
		
		// get the taxon from the generic service to have the uuid for further processing
		Element taxonElement = factory.getTaxonForTaxonNode(taxonNodeElement);
		
		progressMonitor.subTask("Gathering data for taxon: " + XMLHelper.getTitleCache(taxonElement));
		
		// get initialized accepted taxon
		// TODO right now we are getting that from the portal service but should consider to use the generic service
		// as the portal service is more likely to change
		Element fullTaxonElement = factory.getAcceptedTaxonElement(taxonElement);
		
		//populateTypeDesignations(fullTaxonElement);
	
		// get descriptions
		if(configurator.isDoDescriptions()){
			populateDescriptions(fullTaxonElement);
		}
		
		// get polytomous key
		
		if(configurator.isDoPolytomousKey()){
			populatePolytomousKey(fullTaxonElement);
		}
		
		// get synonym
		if(configurator.isDoSynonymy()){
			populateSynonyms(fullTaxonElement);
		}
		
		// get media
		if(configurator.isDoImages()){
			populateImages(fullTaxonElement);
		}
		
		// add taxon element to the node element
		XMLHelper.addContent(fullTaxonElement, taxonNodeElement);
		
		// get taxonomically included taxa
		if(configurator.isDoPublishEntireBranches()){
			populateChildren(taxonNodeElement);
		}
		
		try {
			populateReferences(fullTaxonElement);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		populateTypeDesignations(fullTaxonElement);
		
		progressMonitor.worked(1);
		
	}	
	
	// the name isn't populated in the taxonNode http://dev.e-taxonomy.eu/cdmserver/flora_central_africa/taxonNode/de808dae-e50a-42f2-a4da-bd12f2c2faaf/taxon.json
	// but can get the name from http://dev.e-taxonomy.eu/cdmserver/flora_central_africa/portal/taxon/8f6d5498-1f4b-420f-a1ae-3f0ed9406bb1.json
	private void populateTypeDesignations(Element fullTaxonElement) {
		
		Element nameElement = fullTaxonElement.getChild("name");
		Element uuidElement = fullTaxonElement.getChild("uuid");
		
		List<Element> typeDesignations = factory.getTypeDesignations(nameElement);
		
		nameElement.removeChild("typeDesignations");
		
		for(Element typeDesignation: typeDesignations){
			XMLHelper.addContent(typeDesignation, "typeDesignations", nameElement);
		}
	}
	
	private void populateReferences(Element fullTaxonElement) throws JDOMException {

		//get the references from the taxonElement
		//String referencePattern = "//name/nomenclaturalReference";
		String referencePattern = "/Taxon/name/nomenclaturalReference";

		//but there could be many references
		Element referenceElement = (Element) XPath.selectSingleNode(fullTaxonElement, referencePattern); //Mon 1st july do we get the /Taxon/name/nomenclaturalReference from the taxon node - is this working
		//List<Element> descriptionElementElements = XPath.selectNodes(context, featurePattern + "/..");

		List<Element> elementList = null;

		if(referenceElement != null){  //the referencePattern was found in the taxonElement

			List<Element> refs = factory.getReferences(referenceElement);//getReferences

			fullTaxonElement.removeChild("nomenclaturalReference");//remove the references

			for(Element ref: refs){
				XMLHelper.addContent(ref, "nomenclaturalReference", fullTaxonElement);
			}
		}		

	}

	/**
	 * Populates all child nodes of the given taxonNodeElement
	 * 
	 * @param container
	 */
	private void populateChildren(Element taxonNodeElement){
		
		logger.setLevel(Level.INFO);
		logger.info("populating branch");
		
		List<Element> childNodeElements = factory.getChildNodes(taxonNodeElement);
		
		for(Element childNodeElement : childNodeElements){
			
			populateTreeNodeContainer(childNodeElement);			
			XMLHelper.addContent(childNodeElement, "childNodes", taxonNodeElement);
		}
	}
	
	private void populatePolytomousKey(Element taxonElement){		
		logger.setLevel(Level.INFO);
		logger.info("populating Polytomous key");
		logger.info("populating Polytomous key taxonElement " + XMLHelper.getUuid(taxonElement) + " name " + XMLHelper.getTitleCache(taxonElement));
				
		//List<Element> polytomousKey = factory.getPolytomousKey(taxonElement);
		Element polytomousKey = factory.getPolytomousKey(taxonElement);
		XMLHelper.addContent(polytomousKey, "key", taxonElement);
			
		/*for(Element keyRow : polytomousKey){
			XMLHelper.addContent(keyRow, "key", taxonElement);
		}*/
		
	}
	
	/**
	 * Retrieves descriptions for the given taxonElement and adds them to a SimplifiedFeatureNode
	 * 
	 * @param taxonElement
	 */
	private void populateDescriptions(Element taxonElement){
		taxonElement.removeChild("descriptions");
		
		Element rawDescriptions = factory.getDescriptions(taxonElement);
		
		Element featureTitleCache;
		try {
			//featureTitleCache = (Element) XPath.selectSingleNode(rawDescriptions, "//feature/representation_L10n");			
			List descs = XPath.selectNodes(rawDescriptions, "//feature/representation_L10n");			
			for(Object des : descs){
				logger.info("The descriptions //feature/representation_L10n is " + ((Element) des).getValue());
			}			
			
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

		//logger.setLevel(Level.DEBUG);
		
		logger.debug("The taxonElement is " + XMLHelper.getUuid(taxonElement) + " name " + XMLHelper.getTitleCache(taxonElement));
				
		Element descriptionsElement = new Element("descriptions");		
		Element featuresElement = new Element("features");		
	
		for(SimplifiedFeatureNode simplifiedFeatureNode : simplifiedFeatureTree){ 
			
			try {
				
				processFeatureNode(simplifiedFeatureNode, rawDescriptions, featuresElement);
				
				//UUID featureUuid = XMLHelper.getUuid(simplifiedFeatureNode.getFeatureElement());
				//String featureTitleCache = XMLHelper.getTitleCache(simplifiedFeatureNode.getFeatureElement());
				//logger.debug(" The feature uuid is " + featureUuid + " and name is " + featureTitleCache);
				
			
			} catch (JDOMException e) {
				logger.error(e);
			}
		}
		XMLHelper.addContent(featuresElement, descriptionsElement);
		XMLHelper.addContent(descriptionsElement, taxonElement);
	}
	
	private void processFeatureNode(SimplifiedFeatureNode featureNode, Object context, Element parentFeatureElement)  throws JDOMException{
		
		// gets the feature elements with the current feature uuid	
		UUID featureUuid = XMLHelper.getUuid(featureNode.getFeatureElement());	

		String featurePatternold = "//feature[contains(uuid,'" + featureUuid + "')]";
		//String featurePattern = "/ArrayList[1]/e[1]/elements[1]/e[1]/feature/uuid[.='" + featureUuid + "']";
		//Xpath is now more specific so that only the feature associated with a particular Taxon and not the
		//Taxon's parent or children are selected.
		//Alternative would be to ensure the context object only contains descriptions for the Taxon element of interest
		//Need to look at the taxonPortalController.doGetDescriptions to change this
		String featurePattern = "/ArrayList[1]/e/elements/e/feature[contains(uuid,'" + featureUuid + "')]";
		
		Element feature = (Element) XPath.selectSingleNode(context, featurePattern);
		
		if(feature != null){  //the featurePattern was found in the raw descriptions data
						
			List<Element> descriptionElementElements = XPath.selectNodes(context, featurePattern + "/..");
			
			logger.debug("No of desc elements " +  descriptionElementElements.size() + " featureUUID " + featureUuid + " feature type is " + XMLHelper.getTitleCache(featureNode.getFeatureElement()));
			// add matching description elements as children to this feature element
			for(Element descriptionElementElement : descriptionElementElements){
				
				descriptionElementElement.removeChild("feature"); 
				descriptionElementElement.setName("descriptionelement");
				XMLHelper.addContent(descriptionElementElement, "descriptionelements", feature);
				
			}
			XMLHelper.addContent(feature, parentFeatureElement);
		}else if(featureNode.getChildren().size() > 0){
			
			Element featureElement = featureNode.getFeatureElement();
			Element featureElementClone = (Element) featureElement.clone();
			feature = (Element) featureElementClone.detach();
			
			XMLHelper.addContent(feature, parentFeatureElement); 
		}		
		
		// recurse into children
		for(SimplifiedFeatureNode childFeatureNode : featureNode.getChildren()){
			
			logger.debug("No of featureNode children " + featureNode.getChildren().size());//always 10
			
			UUID childFeatureUuid = XMLHelper.getUuid(childFeatureNode.getFeatureElement());
			String childFeatureTitleCache = XMLHelper.getTitleCache(childFeatureNode.getFeatureElement());
			logger.debug(" The feature is " + childFeatureUuid + " name " + childFeatureTitleCache);

			//9 features in each simplifiedFeatureNode but some of the feature elements are null for a particular featureTitleCache,
			//e.g. Ecology, Description - Description has child features
			
			//creates the second level features i.e. descriptions/features/feature/feature for the description
			processFeatureNode(childFeatureNode, context, feature);
		}
	}
	
	
	/*private Element processDescriptionsRecursive(Object context, SimplifiedFeatureNode simplifiedFeatureNode) throws JDOMException{
		// gets the feature elements with the current uuid
		String featurePattern = "//feature[contains(uuid,'" + simplifiedFeatureNode.getUuid() + "')]";
		
		Element feature = (Element) XPath.selectSingleNode(context, featurePattern);
		
		if(feature != null){
			// recurse into children
			for(SimplifiedFeatureNode childFeatureNode : simplifiedFeatureNode.getChildren()){
				Element childFeatureWithElements = processDescriptionsRecursive(context, childFeatureNode);
				XMLHelper.addContent(childFeatureWithElements, "features", feature);
			}
			
			// get the parents of all feature elements with the current uuid
			List<Element> descriptionElementElements = XPath.selectNodes(context, featurePattern + "/..");
			
			// add matching description elements as children to this feature element
			for(Element descriptionElementElement : descriptionElementElements){
				descriptionElementElement.removeChild("feature");
				descriptionElementElement.setName("descriptionelement");
				XMLHelper.addContent(descriptionElementElement, "descriptionelements", feature);
			}
		}
		
		return feature;
	}*/
	
	/**
	 * Retrieves the synonymy for the given taxonElement 
	 * 
	 * @param taxonElement
	 */
	private void populateSynonyms(Element taxonElement){
		List<Element> synonymy = factory.getSynonymy(taxonElement);		
		
		for(Element synonymyNode : synonymy){
			
			List<Element> children = synonymyNode.getChildren("e");
			
			for(Element child : children){
				
				List<Element> children2 = child.getChildren("e");
				
				for(Element child2 : children2){
					
					if (child2.getChild("name") != null) {
						populateTypeDesignations(child2);// pass in the name of the synonym from synonymy/e/e/name
						//populateImages(child2);
						}
					
				}
			}
			
			XMLHelper.addContent(synonymyNode, "synonymy", taxonElement);
		}
	}
	
	
	
	/**
	 * 
	 * @param taxonElement
	 */
	private void populateImages(Element taxonElement){
		
		factory.getMedia(taxonElement);
		logger.warn("Populating images");
		//Element nameElement = fullTaxonElement.getChild("name");
		//Element uuidElement = fullTaxonElement.getChild("uuid");
		
		List<Element> mediaElements = factory.getMedia(taxonElement);
		
		//nameElement.removeChild("typeDesignations");
		
		for(Element media: mediaElements){
			XMLHelper.addContent(media, "media", taxonElement);
		}
	}
}
