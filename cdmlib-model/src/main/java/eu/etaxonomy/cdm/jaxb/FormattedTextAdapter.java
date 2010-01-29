/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.jaxb;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class FormattedTextAdapter extends XmlAdapter<FormattedText,java.lang.String> {
	public static String[] NAMESPACE_PREFIXES = {"xmlns:common",
		                                         "xmlns:agent",
		                                         "xmlns:description",
		                                         "xmlns:location",
		                                         "xmlns:media",
		                                         "xmlns:molecular",
		                                         "xmlns:name",
		                                         "xmlns:occurence",
		                                         "xmlns:reference",
		                                         "xmlns:taxon",
		                                         "xmlns:view",
		                                         "xmlns:xsi"};

	@SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(FormattedTextAdapter.class);
	
	public FormattedText marshal(String string) throws Exception {
		if(string != null) {
			string = StringEscapeUtils.escapeXml(string);
			String documentString = "<?xml version=\"1.0\"?><text>"  + string + "</text>";
			//log.debug("Parsing " + documentString);
			FormattedText text = new FormattedText();
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder parser = factory.newDocumentBuilder();
		    Document document = parser.parse(new InputSource(new StringReader(documentString)));
		    NodeList childNodes = document.getDocumentElement().getChildNodes();
		    for(int i = 0; i < childNodes.getLength(); i++) {
		    	Node node = childNodes.item(i);
		    	if(node instanceof org.w3c.dom.Text ) {
		    		org.w3c.dom.Text textNode = (org.w3c.dom.Text) node;
		    		
		    		text.getContent().add(textNode.getTextContent());
		    	} else {
		    	    text.getContent().add(node);
		    	}
		    }
		    return text;
		}
		return null;
	}

	public String unmarshal(FormattedText text) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = factory.newDocumentBuilder().newDocument();
		DocumentFragment documentFragment = document.createDocumentFragment();
		
		for(Object object : text.getContent()) {
			if(object instanceof String) {
				String string = (String)object;
				documentFragment.appendChild(document.createTextNode(string));
			} else {
				Node node = (Node)object;
				NamedNodeMap attributes = node.getAttributes();
				for(String prefix : FormattedTextAdapter.NAMESPACE_PREFIXES) {
					try{
						attributes.removeNamedItem(prefix);
					} catch(DOMException de){
						if(de.code != DOMException.NOT_FOUND_ERR) {
							throw de;
						}
					}
					
				}

				documentFragment.appendChild(document.importNode(node,true));
			}

		}

		TransformerFactory transformerFactory  = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION , "yes");
		
		Source input = new DOMSource(documentFragment);
		StringWriter stringWriter = new StringWriter();
		Result output = new StreamResult(stringWriter);
		transformer.transform(input, output);
		String result = stringWriter.toString();
		result = StringEscapeUtils.unescapeXml(result);
		return result;
	}
}
