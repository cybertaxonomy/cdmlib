// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.print.out.IPublishOutputModule;

/**
 * Retrieves all necessary data from an {@link IXMLEntityFactory}.
 * 
 * @author n.hoffmann
 * @created Apr 8, 2010
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
			
			List<Element> childFeatureNodes = children.getChildren();
			
			for(Element childNode : childFeatureNodes){
				
				UUID uuid = XMLHelper.getUuid(childNode);
				Element featureNodeElement = factory.getFeatureNode(uuid);
				Element featureElement = factory.getFeatureForFeatureNode(uuid);
				featureElement.setName(featureElement.getName().toLowerCase(Locale.FRENCH));//We set it to French here but all the names are in English
				SimplifiedFeatureNode simplifiedFeatureNode = new SimplifiedFeatureNode(featureElement, featureTreeRecursive(featureNodeElement));				
				result.add(simplifiedFeatureNode);
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
						
			taxonNodeElement.detach();
			
			populateTreeNodeContainer(taxonNodeElement);
			
			root.addContent(taxonNodeElement);
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
				
				if (textWithRubbish.length() < 5) {
				logger.debug("textWithRubbish: " + textWithRubbish);
				return;
				}
				String cleanedText = textWithRubbish.substring(0, 4);
				node.setText(cleanedText);	
				
				Element parent = (Element) node.getParent().getParent();
				if(parent.getName().equals("citation")){
					Element parent2 = (Element) parent.getParent();
					parent2.setAttribute("sort", cleanedText);
				}
			}
		} catch (Exception e) {
			logger.error("Error trying to clean dat published field", e);
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
		
		progressMonitor.worked(1);
		
	}	
	
	private void populateTypeDesignations(Element fullTaxonElement) {
		
		Element nameElement = fullTaxonElement.getChild("name");
		
		List<Element> typeDesignations = factory.getTypeDesignations(nameElement);
		
		nameElement.removeChild("typeDesignations");
		
		for(Element typeDesignation: typeDesignations){
			XMLHelper.addContent(typeDesignation, "typeDesignations", nameElement);
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
		//logger.setLevel(Level.DEBUG);
		
		logger.debug("The taxonElement is " + XMLHelper.getUuid(taxonElement) + " name " + XMLHelper.getTitleCache(taxonElement));
				
		Element descriptions = new Element("descriptions");		
		Element features = new Element("features");		
	
		for(SimplifiedFeatureNode simplifiedFeatureNode : simplifiedFeatureTree){ 
			
			try {
				
				processFeatureNode(simplifiedFeatureNode, rawDescriptions, features);
				
				//UUID featureUuid = XMLHelper.getUuid(simplifiedFeatureNode.getFeatureElement());
				//String featureTitleCache = XMLHelper.getTitleCache(simplifiedFeatureNode.getFeatureElement());
				//logger.debug(" The feature uuid is " + featureUuid + " and name is " + featureTitleCache);
				
			
			} catch (JDOMException e) {
				logger.error(e);
			}
		}
		XMLHelper.addContent(features, descriptions);
		XMLHelper.addContent(descriptions, taxonElement);
	}
	
	private void processFeatureNode(SimplifiedFeatureNode featureNode, Object context, Element parentElement)  throws JDOMException{
		
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
			XMLHelper.addContent(feature, parentElement);
		}else if(featureNode.getChildren().size() > 0){
			
			Element featureElement = featureNode.getFeatureElement();
			Element featureElementClone = (Element) featureElement.clone();
			feature = (Element) featureElementClone.detach();
			
			XMLHelper.addContent(feature, parentElement); 
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
			XMLHelper.addContent(synonymyNode, "synonymy", taxonElement);
		}
	}
	
	
	
	/**
	 * TODO not implemented yet
	 * @param taxonElement
	 */
	private void populateImages(Element taxonElement){
		logger.warn("not implemented yet");
	}
}
