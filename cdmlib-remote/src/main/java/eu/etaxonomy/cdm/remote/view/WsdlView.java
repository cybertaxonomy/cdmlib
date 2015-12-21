// $Id$
package eu.etaxonomy.cdm.remote.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;

import org.springframework.web.servlet.view.xslt.XsltView;

//import org.springframework.web.servlet.view.xslt.AbstractXsltView;

/**
 * View class which returns the Source for serialization
 * @author ben
 * @see javax.xml.transform.Source
 */
public class WsdlView extends XsltView /*AbstractXsltView*/ {

    protected Source createXsltSource(Map model, String rootName, HttpServletRequest
	        request, HttpServletResponse response) throws Exception {
				return (Source) model.get("source");
	}

}
