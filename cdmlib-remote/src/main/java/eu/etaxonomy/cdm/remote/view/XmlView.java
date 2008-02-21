package eu.etaxonomy.cdm.remote.view;

import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.View;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class XmlView implements View {

	Log log = LogFactory.getLog(XmlView.class);
	
	public String getContentType() {
		return "text/xml";
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		// Retrieve data from model
		Object dto = model.values().toArray()[0];
		
		// Write the XML document to the reponse output stream
		OutputStream out = response.getOutputStream();
		XppDriver xpp = new XppDriver();
		XStream xstream = new XStream(xpp);
		/* 
		 * This disables object graph support and treats the object structure like a tree. 
		 * Duplicate references are treated as two seperate objects and circular references cause an exception. 
		 * */ 
		xstream.setMode(XStream.NO_REFERENCES);
		// serialize DTO into XML
		xstream.toXML(dto,out);
	}

}