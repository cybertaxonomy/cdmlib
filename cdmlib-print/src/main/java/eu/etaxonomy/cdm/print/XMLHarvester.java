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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import eu.etaxonomy.cdm.common.IProgressMonitor;
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
		this.factory = configurator.getFactory();
		this.progressMonitor = configurator.getProgressMonitor();
		
		Element featureTreeElement = factory.getFeatureTree(configurator.getFeatureTreeUuid());
		createSimplifiedFeatureTree(featureTreeElement);
	}
	
	private void createSimplifiedFeatureTree(Element featureTreeElement) {
		Element root = featureTreeElement.getChild("root");
		
		Element realRoot = factory.getFeatureNode(XMLHelper.getUuid(root));
		
		progressMonitor.subTask("Generating simplified Feature Tree.");
		simplifiedFeatureTree = featureTreeRecursive(realRoot);
		
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
				featureElement.setName(featureElement.getName().toLowerCase(Locale.ENGLISH));
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
				String cleanedText = textWithRubbish.substring(0, 4);
				node.setText(cleanedText);	
				
				Element parent = (Element) node.getParent().getParent();
				if(parent.getName().equals("citation")){
					Element parent2 = (Element) parent.getParent();
					parent2.setAttribute("sort", cleanedText);
				}
			}
		} catch (JDOMException e) {
			logger.error("Error tryong to clean dat published field");
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
		
		populateTypeDesignations(fullTaxonElement);
		
		// get descriptions
		if(configurator.isDoDescriptions()){
			populateDescriptions(fullTaxonElement);
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
		
		logger.info("populating branch");
		
		List<Element> childNodeElements = factory.getChildNodes(taxonNodeElement);
		
		for(Element childNodeElement : childNodeElements){
			logger.info("Creating content for child node");
			
			populateTreeNodeContainer(childNodeElement);			
			XMLHelper.addContent(childNodeElement, "childNodes", taxonNodeElement);
		}
	}
	
	/**
	 * Retrieves descriptions for the given taxonElement
	 * 
	 * @param taxonElement
	 */
	private void populateDescriptions(Element taxonElement){
		taxonElement.removeChild("descriptions");
		
		Element rawDescriptions = factory.getDescriptions(taxonElement);
		
		Element descriptions = new Element("descriptions");
		
		Element features = new Element("features");
		
		for(SimplifiedFeatureNode simplifiedFeatureNode : simplifiedFeatureTree){
			
			try {
				processFeatureNode(simplifiedFeatureNode, rawDescriptions, features);
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
		String featurePattern = "//feature[contains(uuid,'" + featureUuid + "')]";
		
		Element feature = (Element) XPath.selectSingleNode(context, featurePattern);
		
		if(feature != null){
			// get the parents of all feature elements with the current uuid
			List<Element> descriptionElementElements = XPath.selectNodes(context, featurePattern + "/..");
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
			processFeatureNode(childFeatureNode, context, feature);
		}
	}
	
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
