package eu.etaxonomy.cdm.remote.view;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.View;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

import eu.etaxonomy.cdm.remote.dto.DescriptionTO;
import eu.etaxonomy.cdm.remote.dto.DistributionSTO;
import eu.etaxonomy.cdm.remote.dto.FeatureSTO;
import eu.etaxonomy.cdm.remote.dto.HomotypicTaxonGroupSTO;
import eu.etaxonomy.cdm.remote.dto.MediaTO;
import eu.etaxonomy.cdm.remote.dto.NameRelationshipTO;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.NameTypeDesignationSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceTO;
import eu.etaxonomy.cdm.remote.dto.ResultSetPageSTO;
import eu.etaxonomy.cdm.remote.dto.RightsSTO;
import eu.etaxonomy.cdm.remote.dto.SpecimenSTO;
import eu.etaxonomy.cdm.remote.dto.SpecimenTypeDesignationSTO;
import eu.etaxonomy.cdm.remote.dto.SynonymRelationshipTO;
import eu.etaxonomy.cdm.remote.dto.TaggedText;
import eu.etaxonomy.cdm.remote.dto.TaxonRelationshipTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.dto.TreeNode;

public class XmlView extends BaseView implements View {
	Log log = LogFactory.getLog(XmlView.class);
	
	public String getContentType() {
		return "text/xml";
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {		
		// Retrieve data from model
		Object dto = getResponseData(model);
		// Write the XML document to the reponse output stream
		XppDriver xpp = new XppDriver();
		XStream xstream = new XStream(xpp);
		/* 
		 * This disables object graph support and treats the object structure like a tree. 
		 * Duplicate references are treated as two seperate objects and circular references cause an exception. 
		 * */ 
		xstream.setMode(XStream.NO_REFERENCES);
		// use simple element names for these DTO classes
		xstream.alias("TaggedText", TaggedText.class);
		xstream.alias("TreeNode", TreeNode.class);
		xstream.alias("DescriptionTO", DescriptionTO.class);
		xstream.alias("MediaTO", MediaTO.class);
		xstream.alias("NameRelationshipTO", NameRelationshipTO.class);
		xstream.alias("NameTO", NameTO.class);
		xstream.alias("ReferenceTO", ReferenceTO.class);
		xstream.alias("RightsTO", RightsSTO.class);
		xstream.alias("TaxonTO", TaxonTO.class);
		xstream.alias("SynonymRelationshipTO", SynonymRelationshipTO.class);
		xstream.alias("TaxonRelationshipTO", TaxonRelationshipTO.class);
		xstream.alias("DistributionSTO", DistributionSTO.class);
		xstream.alias("FeatureSTO", FeatureSTO.class);
		xstream.alias("HomotypicTaxonGroupSTO", HomotypicTaxonGroupSTO.class);
		xstream.alias("NameSTO", NameSTO.class);
		xstream.alias("NameTypeDesignationSTO", NameTypeDesignationSTO.class);
		xstream.alias("ReferenceSTO", ReferenceSTO.class);
		xstream.alias("ResultSetPageSTO", ResultSetPageSTO.class);
		xstream.alias("SpecimenSTO", SpecimenSTO.class);
		xstream.alias("SpecimenTypeDesignationSTO", SpecimenTypeDesignationSTO.class);
		xstream.alias("TaxonSTO", TaxonSTO.class);
		// serialize DTO into XML
		// TODO determine preferred charset from HTTP Accept-Charset header
		Writer out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		out.append("<?xml version='1.0'?>");
		xstream.toXML(dto, out);		
	}

}