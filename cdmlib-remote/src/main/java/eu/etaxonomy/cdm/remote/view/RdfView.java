// $Id$
package eu.etaxonomy.cdm.remote.view;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;

import org.dozer.Mapper;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.web.servlet.view.AbstractView;

import com.ibm.lsid.http.HTTPConstants;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.tdwg.BaseThing;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.SpeciesProfileModel;
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
	
	private Mapper mapper;
	
	private Map<Class<? extends CdmBase>,Class<? extends BaseThing>> classMap = new HashMap<Class<? extends CdmBase>,Class<? extends BaseThing>>();
	
	private Integer expiresPlus;
	
	public RdfView() {
		classMap.put(Taxon.class, TaxonConcept.class);
		classMap.put(Synonym.class, TaxonConcept.class);
		classMap.put(TaxonDescription.class, SpeciesProfileModel.class);
	}
	
	@Autowired
	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	
	@Autowired
	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}
	
	public void setExpiresPlus(Integer expiresPlus) {
		this.expiresPlus = expiresPlus;
	}

	@Override
	protected void renderMergedOutputModel(Map model,HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if(expiresPlus != null) {
		    DateTime expires = new DateTime();
	        response.setHeader(HTTPConstants.EXPIRES_HEADER, HTTPConstants.HTTP_DATE_FORMAT.format(expires.plusDays(expiresPlus).toDate()));
	    }
		
		Rdf rdf = new Rdf();
		for(Object object : model.values()) {
		    if(object instanceof IdentifiableEntity) {
		        IdentifiableEntity identifiableEntity = (IdentifiableEntity)object;
		        Class clazz = classMap.get(identifiableEntity.getClass());
		        if(clazz != null) {
		          rdf.addThing((BaseThing)mapper.map(identifiableEntity, clazz));
		        }
		    }
		}
		
		marshaller.marshal(rdf, new StreamResult(response.getOutputStream()));
	}

}
