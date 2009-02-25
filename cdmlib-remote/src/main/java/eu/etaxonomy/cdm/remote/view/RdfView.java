package eu.etaxonomy.cdm.remote.view;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;

import net.sf.dozer.util.mapping.MapperIF;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.web.servlet.view.AbstractView;

import com.ibm.lsid.MetadataResponse;
import com.ibm.lsid.http.HTTPConstants;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.tdwg.BaseThing;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonConcept;
import eu.etaxonomy.remote.dto.rdf.Rdf;

/**
 * View class which takes a MetadataResponse and returns the Source for serialization
 * @author ben
 * @see javax.xml.transform.Source
 * @see com.ibm.lsid.MetadataResponse
 */
public class RdfView extends AbstractView {
	
	private Marshaller marshaller;
	
	private MapperIF mapper;
	
	private Map<Class<? extends CdmBase>,Class<? extends BaseThing>> classMap = new HashMap<Class<? extends CdmBase>,Class<? extends BaseThing>>();
	
	public RdfView() {
		classMap.put(Taxon.class, TaxonConcept.class);
	}
	
	@Autowired
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	
	@Autowired
	public void setMapper(MapperIF mapper) {
		this.mapper = mapper;
	}

	@Override
	protected void renderMergedOutputModel(Map model,HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		MetadataResponse metadataResponse = (MetadataResponse)model.get("metadataResponse");
		if(metadataResponse.getExpires() != null) {
		    response.setHeader(HTTPConstants.EXPIRES_HEADER, HTTPConstants.HTTP_DATE_FORMAT.format(metadataResponse.getExpires()));
		}
		
		CdmBase object = (CdmBase)metadataResponse.getValue();
		Class clazz = classMap.get(object.getClass());
		Rdf rdf = new Rdf();
		rdf.addThing((BaseThing)mapper.map(object, clazz));
		marshaller.marshal(rdf, new StreamResult(response.getOutputStream()));
	}

}
