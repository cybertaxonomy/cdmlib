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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Abstract implementation of an IXMLEntityFactory
 * 
 * @see IXMLEntityFactory
 * 
 * @author n.hoffmann
 * @created Jul 16, 2010
 * @version 1.0
 */
public abstract class XmlEntityFactoryBase implements IXMLEntityFactory {
	private static final Logger logger = Logger
			.getLogger(XmlEntityFactoryBase.class);

	/**
	 * A list of XML element names that are used to store collections
	 */
	private static final List<String> COLLECTION_ELEMENTS = Arrays.asList(new String[]{"arraylist", "hashtable", "hashset", "persistentset"});
	
	private static final String MODEL_AND_VIEW = "ModelAndView";
	
	/**
	 * The SAXBuilder used to parse the results from cdmlib-remote API calls
	 */
	protected SAXBuilder builder = new SAXBuilder();
		
	/**
	 * This method will transform the response of a CDM REST request into a list of
	 * XMLEntityContainer objects for convenient handling.
	 * 
	 * @param rootElement
	 * @return
	 */
	protected List<Element> processElementList(Element rootElement) {
		logger.trace("Processing element list: " + rootElement);
		
		List<Element> result = new ArrayList<Element>();
		
		if(rootElement == null){
			return result;
		}
		
		if(rootElement.getName().equals(MODEL_AND_VIEW)){
			Element model = rootElement.getChild("model");
			rootElement = (Element) model.getChildren().get(0);
			
			logger.warn("ModelAndView detected");
			
		}
		
		String rootElementName = rootElement.getName().toLowerCase(Locale.ENGLISH);
		if(! COLLECTION_ELEMENTS.contains(rootElementName)){
			logger.error("Given element is not of a processable collection type. RootElementName: " + rootElementName);
			return result;
		}
		
		
		for(Object child : rootElement.getChildren()){
			if(child instanceof Element){
				Element childElement = (Element) ((Element)child).clone();
				
				String processedElementName = XMLHelper.getClazz(childElement);
				
				childElement.detach();
				childElement.setName(processedElementName);
				childElement.removeAttribute("class");
				
				result.add(childElement);
			}
		}
		
		return result;
	}
}
