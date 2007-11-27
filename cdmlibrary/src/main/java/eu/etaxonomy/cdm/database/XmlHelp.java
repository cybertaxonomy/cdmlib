package eu.etaxonomy.cdm.database;

import static eu.etaxonomy.cdm.database.XmlHelp.getFirstAttributedChild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
	/**
	 * Writes the Document doc to the specified file
	 * @param doc
	 * @param path
	 * @param fileName
	 * @return true, if no error
	 */
	static public boolean saveToXml(Document doc, String path, String fileName, Format format ){
		try {
			XMLOutputter out = new XMLOutputter(format);
			if (! fileName.endsWith(".xml")){
				fileName += ".xml";
			}
			FileOutputStream outFile = new FileOutputStream(path + "\\" + fileName); 
			out.output(doc, outFile);
			return true;
		} catch (IOException e) {
			logger.error("IOException in saveToXml()");
			return false;
		}
		
	}
	
	static public Element getFirstAttributedChild(Element parent, String elementName, String attributeName, String attributeValue){
		Namespace ns = parent.getNamespace();
		List<Element> elList = parent.getChildren(elementName, ns);
		for (Element el : elList){
			Attribute attr =  el.getAttribute(attributeName);
			if (attr != null && attr.getValue().equalsIgnoreCase(attributeValue)){
				return el;
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
	
	
	//returns the root Element in the File xmlFile
	static public  Element getRoot(File xmlFile){
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(xmlFile);
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
	
	
}
