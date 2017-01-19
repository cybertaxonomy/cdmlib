/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XmlHelp {
	private static final Logger logger = Logger.getLogger(XmlHelp.class);

	public final static Format prettyFormat = Format.getPrettyFormat(); 
	/**
	 * Writes the Document doc to the specified file
	 * @param doc
	 * @param path
	 * @param fileName
	 * @return true, if no error
	 * 
	 * TODO throw the FileNotFoundException and handle in the calling method. That is more likely the place where you can do 
	 * something about the problem
	 */
	static public boolean saveToXml(Document doc, String path, String fileName, Format format ){
		try {
			if (! fileName.endsWith(".xml")){
				fileName += ".xml";
			}
			FileOutputStream outFile = new FileOutputStream(path + File.separator + fileName); 
			return saveToXml(doc, outFile, format);
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException in saveToXml()");
			return false;
		}
			
	}
	
	/**
	 * Writes the Document doc to the specified file
	 * @param doc
	 * @param path
	 * @param fileName
	 * @return true, if no error
	 * 
	 * TODO throw the IOException and handle in the calling method. That is more likely the place where you can do 
	 * something about the problem
	 */
	static public boolean saveToXml(Document doc, OutputStream outStream, Format format ){
		try {
			XMLOutputter out = new XMLOutputter(format);
			out.output(doc, outStream);
			return true;
		} catch (IOException e) {
			logger.error("IOException in saveToXml()");
			return false;
		}	
	}
	
	static public Element getFirstAttributedChild(Element parent, String elementName, String attributeName, String attributeValue){
		Namespace ns = parent.getNamespace();
		
		List<Element> elList = getChildren(parent, elementName, ns);
		for (Element el : elList){
			Attribute attr =  el.getAttribute(attributeName);
			if (attr != null && attr.getValue().equalsIgnoreCase(attributeValue)){
				return el;
			}
		}
		return null;
	}
	
	static public List<Element> getAttributedChildList(Element parent, String elementName, String attributeName){
		List<Element> resultList = new ArrayList<Element>();
		Namespace ns = parent.getNamespace();
		List<Element> elList = getChildren(parent, elementName, ns);
		for (Element el : elList){
			Attribute attr =  el.getAttribute(attributeName);
			if (attr != null){
				resultList.add(el);
			}
		}
		return resultList;
	}
	
	/**
	 * Returns a list of children with the given element name and with a given attribute name and 
	 * a given value for this attribute.<BR>
	 * The value comparison is case insensitive.
	 * @param parent
	 * @param elementName
	 * @param attributeName
	 * @param value
	 * @return
	 */
	static public List<Element> getAttributedChildListWithValue(Element parent, String elementName, String attributeName, String value){
		List<Element> resultList = new ArrayList<Element>();
		Namespace ns = parent.getNamespace();
		List<Element> elList = getChildren(parent, elementName, ns);
		for (Element el : elList){
			Attribute attr =  el.getAttribute(attributeName);
			if (attr != null){
				if (attr.getValue().equalsIgnoreCase(value)){
					resultList.add(el);
				}
			}
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	private static List<Element> getChildren(Element parent, String elementName,Namespace ns) {
		return parent.getChildren(elementName, ns);
	}
	
	public static String getChildAttributeValue(Element element, String childElementName, Namespace childElementNamespace, String childAttributeName, Namespace childAttributeNamespace){
		Element child = element.getChild(childElementName, childElementNamespace);
		if (child == null){
			return null;
		}
		Attribute childAttribute = child.getAttribute(childAttributeName, childAttributeNamespace);
		if (childAttribute == null){
			return null;
		}
		return childAttribute.getValue();
	}
	
	public static String getChildContent(Element element, String childElementName, Namespace childElementNamespace, String childAttributeName, Namespace childAttributeNamespace){
		Element child = element.getChild(childElementName, childElementNamespace);
		if (child == null){
			return null;
		}
		List childContent = child.getContent();
		if (childContent.isEmpty()){
			return null;
		}
		for (Object content:childContent){
			if (content instanceof Element){
				Element contentEl = (Element)content;
				if (contentEl.getName().equals(childAttributeName)){
					return contentEl.getText();
				}
			}
		}
		return null;
	}

	/**
	 * @param parent
	 * @param elementName
	 * @param attributeName
	 * @param attributeValue
	 * @return
	 */
	static public Element getOrAddChild(Element parent, String elementName, String attributeName, String attributeValue){
		Element result = null;
		if (parent != null){ 
			if (attributeName != null){
				result = getFirstAttributedChild(parent, elementName, attributeName, attributeValue);
			}else{
				result = parent.getChild(elementName, parent.getNamespace());
			}
			if (result == null){
				result  = new Element(elementName, parent.getNamespace());
				if (attributeName != null){
					Attribute attr = new Attribute(attributeName, attributeValue);
					result.setAttribute(attr);
				}
			}
			if (result.getParent()== null){
				parent.addContent(result);
			}
		}
		return result;
	}
	
	static public Element insertXmlRefProperty(Element parent, String strName, String strValue){
		Namespace ns = parent.getNamespace();
		Element property = new Element("property", ns);
		Attribute name = new Attribute("name", strName);
		property.setAttribute(name);
		Attribute value = new Attribute("value", strValue);
		property.setAttribute(value);
		parent.addContent(property);
		return  property;
	}
	
	static public Element insertXmlValueProperty(Element parent, String strName, String strValue){
		Namespace ns = parent.getNamespace();
		Element property = new Element("property", ns);
		Attribute name = new Attribute("name", strName);
		property.setAttribute(name);
		Attribute value = new Attribute("value", strValue);
		property.setAttribute(value);
		parent.addContent(property);
		return  property;
	}
	
	
	static public Element insertXmlBean(Element parent, String strId, String strClass){
		Namespace ns = parent.getNamespace();
		Element bean = new Element("bean", ns);
		Attribute id = new Attribute("id", strId);
		bean.setAttribute(id);
		Attribute clazz = new Attribute("class", strClass);
		bean.setAttribute(clazz);
		parent.addContent(bean);
		return  bean;
	}
	

	/**
	 * returns the root Element in the File xmlFile
	 * @param xmlInput
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	static public  Element getRoot(InputStream xmlInput) throws JDOMException, IOException{
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(xmlInput);
		Element root = doc.getRootElement();
		return root;
	}
	
	/**
	 * returns the root Element in the File xmlFile
	 * 
	 * @param xmlInput
	 * @param elementName
	 * @return
	 * TODO throw the JDOMException and the IOException and handle in the calling method. That is more likely the place where you can do 
	 * something about the problem
	 */
	static public  Element getRoot(InputStream xmlInput, String elementName){
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(xmlInput);
			Element root = doc.getRootElement();
			if (root.getName() != elementName){
				return null;
			}else{
				return root;
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	/**
	 * returns the root Element in the File xmlFile
	 * @param xmlInput
	 * @return
	 * 
	 * TODO throw the IOException and handle in the calling method. That is more likely the place where you can do 
	 * something about the problem
	 */
	static public  Element getBeansRoot(InputStream xmlInput){
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(xmlInput);
			Element root = doc.getRootElement();
			if (root.getName() != "beans"){
				return null;
			}else{
				return root;
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	/**
	 * Gets the child element and tests if there is no other child element exists having the same name.
	 * The result is returned as a pair of thd child element and a boolean value that indicates if the 
	 * elements cardinality was correct. <BR>
	 * If there is more then one child element with the child element name 
	 * or if there is no such element and obligatory is <code>true</code> the second part of the result is <code>false</code>
	 * Otherwise it is <code>true</code>.
	 * @param parentElement the parent element
	 * @param childName name of the child element
	 * @param nsChild the namespace for the child element
	 * @param obligatory if <code>true</code>, return value is only <code>true</code> if exactly 1 child element with
	 * the given name exists
	 * @return
	 */
	static public DoubleResult<Element, Boolean> getSingleChildElement(Element parentElement, String childName, Namespace nsChild, boolean obligatory){
		DoubleResult<Element, Boolean> result = new DoubleResult<Element, Boolean>();
		result.setSecondResult(false);
		
		if (parentElement == null){
			logger.warn("Parent element is null");
			return result;
		}
		List<Element> elList = getChildren(parentElement, childName, nsChild);
		if (elList.size() > 1){
			logger.error("Multiple '" + childName + "' elements.");
			return result;		
		}else if (elList.size() == 0){
			logger.info("There is no '" + childName + "' element");
			if (! obligatory){
				result.setSecondResult(true);
			}
			return result;
		}
		Element childElement = elList.get(0);		
		result.setFirstResult(childElement);
		result.setSecondResult(true);
		return result;
	}
	
	static public Element getSingleChildElement(ResultWrapper<Boolean> success, Element parentElement, String childName, Namespace nsChild, boolean obligatory){
		
		if (parentElement == null){
			logger.warn("Parent element is null");
			success.setValue(false);
			return null;
		}
		List<Element> elList = getChildren(parentElement, childName, nsChild);
		if (elList.size() > 1){
			logger.error("Multiple '" + childName + "' elements.");
			success.setValue(false);
			return null;	
		}else if (elList.size() == 0){
			elList = getChildren(parentElement, childName, null);
			logger.info("There is no '" + childName + "' element");
			if (obligatory){
				success.setValue(false);
			}
			return null;
		}
		
		Element childElement = elList.get(0);		
		return childElement;
	}	

	static public List<Element> getMultipleChildElement(Element parentElement, String childName, Namespace nsChild, boolean obligatory){
		
		if (parentElement == null){
			logger.warn("Parent element is null");
			return null;
		}
		
		List<Element> elList = getChildren(parentElement, childName.trim(), nsChild);
		
		if (elList.size() == 0){
			logger.info("There is no '" + childName + "' element");
			return null;
		}
		return elList;
	}

	public static String getChildContentAttributeValue(Element element,
			String childAttributeName, Namespace taxonNameNamespace, String childElementName,
			Namespace childElementNamespace) {
		Element child = element.getChild(childAttributeName, taxonNameNamespace);
		if (child == null){
			return null;
		}
		List childContent = child.getContent();
		if (childContent.isEmpty()){
			return null;
		}
		for (Object content:childContent){
			if (content instanceof Element){
				Element contentEl = (Element)content;
				if (contentEl == null){
					return null;
				}
				if (!(contentEl.getAttributeValue(childElementName) == null)){
					Attribute at = contentEl.getAttribute("ressource");
					if (at != null)return at.getValue();
				}
			}
		}
		return null;
	}	
	
}
