/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.view;



import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;

import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.web.servlet.view.AbstractView;

import com.ibm.lsid.http.HTTPConstants;

import eu.etaxonomy.cdm.remote.dto.common.RemoteResponse;
import eu.etaxonomy.cdm.remote.dto.tdwg.BaseThing;
import eu.etaxonomy.remote.dto.rdf.Rdf;

/**
 * This class handles rdf views by mapping data objects to corresponding rdf objects, which are later
 * run through a marshaller to produce xml or json
 *
 * @author b.clarke,c.mathew
 * @version 1.0.0
 * @created 25-Nov-2012
 */

public class RdfView extends AbstractView {

	//FIXME : the standard marshaller as defined in remote.xml is not used in web service views so it
	//is commented aout for the moment.

//	private Marshaller marshaller;

	private Marshaller rdfMarshaller;

	private Mapper mapper;

//	private Map<Class<? extends CdmBase>,Class<? extends BaseThing>> classMap = new HashMap<Class<? extends CdmBase>,Class<? extends BaseThing>>();
	private Map<Class<? extends RemoteResponse>,Class<? extends BaseThing>> remoteClassMap = new HashMap<Class<? extends RemoteResponse>,Class<? extends BaseThing>>();

	private Integer expiresPlus;

    public enum Type{
        RDFXML("application/rdf+xml"),
        RDFJSON("application/rdf+json");

        private final String contentType;

        Type(String contentType){
            this.contentType = contentType;
        }

        public String getContentType(){
            return contentType;
        }
    }

    private Type type = Type.RDFXML;


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.View#getContentType()
     */
    @Override
    public String getContentType() {
        return type.getContentType();
    }

	public RdfView() {
//		classMap.put(Taxon.class, TaxonConcept.class);
//		classMap.put(Synonym.class, TaxonConcept.class);
//		classMap.put(TaxonDescription.class, SpeciesProfileModel.class);

		remoteClassMap.put(eu.etaxonomy.cdm.remote.dto.namecatalogue.NameInformation.class,
				eu.etaxonomy.cdm.remote.dto.cdm.NameInformationRdf.class);
	}

//	@Autowired
//	public void setMarshaller(Marshaller marshaller) {
//		this.marshaller = marshaller;
//	}

	@Autowired
	@Qualifier("rdfMarshaller")
	public void setRdfMarshaller(Marshaller rdfMarshaller) {
		this.rdfMarshaller = rdfMarshaller;
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
		    ZonedDateTime expires = ZonedDateTime.now();
	        response.setHeader(HTTPConstants.EXPIRES_HEADER, HTTPConstants.HTTP_DATE_FORMAT.format(expires.plusDays(expiresPlus).toLocalDate()));
	    }

		rdfMarshaller.marshal(buildRdf(model), new StreamResult(response.getOutputStream()));
	}


	public Rdf buildRdf(Map model) {
		Rdf rdf = new Rdf();
		for(Object object : model.values()) {
//		    if(object instanceof IdentifiableEntity) {
//		        IdentifiableEntity identifiableEntity = (IdentifiableEntity)object;
//		        Class clazz = classMap.get(identifiableEntity.getClass());
//		        if(clazz != null) {
//		          rdf.addThing((BaseThing)mapper.map(identifiableEntity, clazz));
//		        }
//		    } else
		    if(object instanceof Collection) {
		    	Collection c = (Collection)object;
		    	Iterator itr = c.iterator();
		    	while(itr.hasNext()) {
		    		Object obj = itr.next();
		    		Class clazz = remoteClassMap.get(obj.getClass());
		    		if(clazz != null) {
		    			rdf.addThing((BaseThing)mapper.map(obj, clazz));

		    		}
		    	}
		    }
		}
		return rdf;
	}

}
