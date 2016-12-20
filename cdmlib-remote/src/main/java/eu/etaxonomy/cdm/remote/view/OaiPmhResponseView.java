package eu.etaxonomy.cdm.remote.view;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;

import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
	
	protected Mapper mapper;
	
	@Autowired
	@Qualifier("marshaller")
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	
	@Autowired
	public void setMapper(Mapper mapper) {
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
