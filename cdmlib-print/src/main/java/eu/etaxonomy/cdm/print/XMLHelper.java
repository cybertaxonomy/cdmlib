/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;

/**
 * @author n.hoffmann
 * @since Apr 9, 2010
 * @version 1.0
 */
public class XMLHelper {
	private static final Logger logger = Logger.getLogger(XMLHelper.class);
	
	public enum EntityType{
		CLASSIFICATION("Classification"), 
		TAXON_NODE("TaxonNode"),
		EMPTY("");
		
		
		private String cdmClassName;
		
		EntityType(String cdmClassName){
			this.cdmClassName = cdmClassName;
		}
		
		public static EntityType getEntityType(String cdmClassName){
			for(EntityType entityType : values()){
				if(entityType.getCdmClassName().equals(cdmClassName)){
					return entityType;
				}
			}
			return EMPTY;
		}

		private String getCdmClassName() {
			return cdmClassName;
		}
	}
	
	public static final String ELEMENT_CLASS = "class";
	public static final String ELEMENT_UUID = "uuid";
	public static final String ELEMENT_TITLE_CACHE = "titleCache";

	public static EntityType getEntityType(Element element) {
		return EntityType.getEntityType(getClazz(element));
	}
	
	public static UUID getUuid(Element element) {
		String content = getContent(element, ELEMENT_UUID);
		
		UUID uuid = null;
		
		try{
			uuid = UUID.fromString(content);
		}catch(IllegalArgumentException e){
			throw new RuntimeException(e);
		}
		
		return uuid;
	}
	
	public static String getClazz(Element element) {
		return getContent(element, ELEMENT_CLASS);
	}
	
	public static String getTitleCache(Element element) {
		return getContent(element, ELEMENT_TITLE_CACHE);
	}
	
	public static String getTitleCacheOfTaxonNode(Element treeNodeElement) {
		if(EntityType.TAXON_NODE.equals(getEntityType(treeNodeElement))){
			Element element;
			try {
				element = (Element) XPath.selectSingleNode(treeNodeElement, "taxon/name");
				if(element != null){
					return getContent(element, ELEMENT_TITLE_CACHE);
				}else{
					logger.error("Could not find title cache for taxonNode/taxon/name path");
				}
				
			} catch (JDOMException e) {
				logger.error("Could not find title cache for taxonNode/taxon/name path", e);
			} catch (Exception e) {
				logger.error("Exception while trying to retrieve title cache for taxon", e);
			}			
		}
		return getTitleCache(treeNodeElement);
	}
	
	public static Element getTaxonFromTaxonNode(Element taxonNodeElement) {
		if(EntityType.TAXON_NODE.equals(getEntityType(taxonNodeElement))){
			Element element;
			try {
				element = (Element) XPath.selectSingleNode(taxonNodeElement, "taxon");
				return element;
			} catch (JDOMException e) {
				logger.error("Could not find title cache for taxonNode/taxon/name path", e);
			} catch (Exception e) {
				logger.error("Exception while trying to retrieve title cache for taxon", e);
			}			
		}
		return null;
	}

	/**
	 * Adds an element as a child to another element
	 * 
	 * @param elementToAdd
	 * @param parentElement
	 */
	public static void addContent(Element elementToAdd, Element parentElement){
		elementToAdd.detach();
		parentElement.addContent(elementToAdd);
	}
	
	/**
	 * Adds an element to the child element of another element using the given name to select the child element
	 * 
	 * @param elementToAdd
	 * @param childElementName
	 * @param parentElement
	 */
	public static void addContent(Element elementToAdd, String childElementName, Element parentElement){
				
		if(elementToAdd == null){
			logger.warn("Retrieved a null element. Not adding it.");
			return;
		}
		if(parentElement == null){
			throw new IllegalArgumentException("Target element may not be null");
		}
		
		// create element if it did not exist before
		if(parentElement.getChild(childElementName) == null){
			parentElement.addContent(new Element(childElementName));
		}
		
		Element elementToAddClone = (Element) elementToAdd.clone();
		elementToAddClone.detach();
		
		Element childElement = parentElement.getChild(childElementName);
		childElement.addContent(elementToAddClone);
	}
	
	/*********** PRIVATE METHODS ********************/
	
	/**
	 * Returns the content of a child of <code>this</code> element 
	 * with the given element name.
	 * 
	 * @param elementName the name of a child element
	 * @return a String represting the content of the XML element with the given name
	 */
	private static String getContent(Element element, String elementName){
		if(element == null){
			logger.error("Element is null");
			return "";
		}
		
		Element contentElement = element.getChild(elementName);
		if(contentElement != null){
			Text content = (Text) contentElement.getContent().iterator().next();
			return content.getText();
		}
		return null;
	}
}
