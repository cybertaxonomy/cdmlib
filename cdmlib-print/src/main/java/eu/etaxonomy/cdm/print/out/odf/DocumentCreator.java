package eu.etaxonomy.cdm.print.out.odf;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;
import org.odftoolkit.odfdom.pkg.OdfFileDom;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author n.hoffmann
 * @since Apr 20, 2010
 * @version 1.0
 */
public class DocumentCreator {

	private static final Logger logger = Logger
			.getLogger(DocumentCreator.class);
	
	private OdfTextDocument outputDocument;
	private OdfFileDom contentDom; // the document object model for content.xml
	private OdfFileDom stylesDom; // the document object model for styles.xml

	// the office:automatic-styles element in content.xml
	private OdfOfficeAutomaticStyles contentAutoStyles;

	// the office:styles element in styles.xml
	private StylesFactory stylesFactory;

	// the office:text element in the content.xml file
	private OfficeTextElement officeText;

	public OdfTextDocument create(Document document) throws JDOMException {
		if (setupOutputDocument()) {

			cleanOutDocument();

			stylesFactory = new StylesFactory(outputDocument);

			DOMOutputter domOutputter = new DOMOutputter();
			org.w3c.dom.Document output;
			output = domOutputter.output(document);
			
			Node firstChild = output.getFirstChild();
			
			org.w3c.dom.Document officeDocument = officeText.getOwnerDocument();
			
			
			Node node = officeDocument.importNode(firstChild, true);
			NodeList childNodes = node.getChildNodes();
			
			for (int i = 0; i < childNodes.getLength(); i++){
				officeText.appendChild(childNodes.item(i));
			}
			
			return outputDocument;
		}
		return null;
	}

	private boolean setupOutputDocument() {

		try {
			outputDocument = OdfTextDocument.newTextDocument();
			contentDom = outputDocument.getContentDom();
			stylesDom = outputDocument.getStylesDom();
			
			officeText = outputDocument.getContentRoot();
			
			contentAutoStyles = contentDom.getOrCreateAutomaticStyles();

			return true;
		} catch (Exception e) {
			logger.error("Unable to create output document.", e);
			outputDocument = null;
		}
		return false;
	}

	

	/*
	 * The default document has some content in it already (in the case of a
	 * text document, a <text:p>. Clean out all the old stuff.
	 */
	void cleanOutDocument() {
		Node childNode;

		childNode = officeText.getFirstChild();
		while (childNode != null) {
			officeText.removeChild(childNode);
			childNode = officeText.getFirstChild();
		}
	}
}
