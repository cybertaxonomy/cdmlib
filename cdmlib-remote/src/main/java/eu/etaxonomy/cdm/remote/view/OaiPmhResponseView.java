// $Id: RdfView.java 7281 2009-10-07 12:09:25Z ben.clark $
package eu.etaxonomy.cdm.remote.view;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;

import net.sf.dozer.util.mapping.MapperIF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.web.servlet.view.AbstractView;

import eu.etaxonomy.cdm.remote.dto.oaipmh.OAIPMH;

/**
 * View class which takes a MetadataResponse and returns the Source for serialization
 * @author ben
 * @see javax.xml.transform.Source
 * @see com.ibm.lsid.MetadataResponse
 */
public abstract class OaiPmhResponseView extends AbstractView {
	
	private Marshaller marshaller;
	
	protected MapperIF mapper;
	
	@Autowired
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	
	@Autowired
	public void setMapper(MapperIF mapper) {
		this.mapper = mapper;
	}
	
    protected abstract void constructResponse(OAIPMH oaiPmh,Map<String,Object> model);

    @Override
    protected void renderMergedOutputModel(Map<String,Object> model,HttpServletRequest request, HttpServletResponse response)
			throws Exception {
	    OAIPMH oaiPmh = new OAIPMH();
	    StringBuffer requestBuffer = request.getRequestURL();
	    if(!request.getParameterMap().isEmpty()) {
	    	int i = 0;
	    	Enumeration<String> parameterNames = request.getParameterNames();
	        while(parameterNames.hasMoreElements()) {
	        	String parameterName = parameterNames.nextElement();
	        	if(i == 0) {
	        		requestBuffer.append("?");
	        	} else {
	        		requestBuffer.append("&");
	        	}
	            requestBuffer.append(parameterName + "=" + request.getParameter(parameterName));
	        }
	    }
        model.put("request",requestBuffer.toString());
        constructResponse(oaiPmh,model);
		marshaller.marshal(oaiPmh, new StreamResult(response.getOutputStream()));
	}

}
