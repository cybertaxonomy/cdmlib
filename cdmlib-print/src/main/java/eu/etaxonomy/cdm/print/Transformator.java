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
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.jdom.transform.XSLTransformException;

/**
 * Perform XSL transformations.
 * 
 * Note: This class provides access to the JAXP XSL transformer currently active. 
 * 
 * @author n.hoffmann
 * @created Apr 20, 2010
 * @version 1.0
 */
public class Transformator {

	private static final Logger logger = Logger
				.getLogger(Transformator.class);
	private Transformer transformer;
	

	public Transformator(InputStream stylesheet) throws XSLTransformException{
		if(stylesheet == null){
			throw new IllegalArgumentException("Stylsheet may not be null");
		}
		
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Templates templates = transformerFactory.newTemplates(new StreamSource(stylesheet));
            transformer = templates.newTransformer();
        }
        catch (TransformerException e) {
            throw new XSLTransformException("Could not construct XSLTransformer", e);
			
        }
	}
	
	
	public Transformator(URL stylesheet) throws XSLTransformException, IOException {
		this(stylesheet.openStream());
	}
	
    /**
     * Transforms the given document to an output document.
     *
     * @param  inputDoc            input document
     * @param  resolver			   entity resolver for the input document
     * @return                     transformed output document
     * @throws XSLTransformException       if there's a problem in the transformation
     */
    public Document transform(org.jdom.Document inputDocument) throws XSLTransformException {
    	JDOMSource source = new JDOMSource(inputDocument);
    	JDOMResult result = new JDOMResult();
        try {
        	logger.trace("Transforming input document: " + inputDocument);
        	
            transformer.transform(source, result);
            Document resultDocument = result.getDocument();
 
        	resultDocument.getContent();
        	return resultDocument;         
        }
    	catch (TransformerException e) {
    		logger.error(e);
            throw new XSLTransformException("Could not perform transformation", e);
        }
    }
}
