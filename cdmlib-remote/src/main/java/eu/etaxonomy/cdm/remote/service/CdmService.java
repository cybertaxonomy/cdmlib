package eu.etaxonomy.cdm.remote.service;

import java.util.List;
import java.util.UUID;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import org.codehaus.jra.Get;
import org.codehaus.jra.HttpResource;

import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;

/*
 * Methods adopted from ws_method as 
 * found in http://dev.e-taxonomy.eu/svn/trunk/drupal/modules/cdm_dataportal/cdm_api.module
 */
@WebService(targetNamespace = "http://cdm.etaxonomy.eu/remote")
public interface CdmService {

	@Get
    @HttpResource(location = "/whatis/{uuid}")
	public Class whatis(String uuid); 

	@Get
    @HttpResource(location = "/name/{uuid}")
	public NameTO getName(@WebParam(name="uuid") String uuid);// throws BusinessLogicException;  
	
	@Get
    @HttpResource(location = "/taxon/{uuid}")
	public TaxonTO getTaxon(String uuid); 
	
	@Get
    @HttpResource(location = "/taxa/{beginsWith}/{onlyAccepted}/{pagesize)/{page}")
	public List<TaxonSTO> listNames(
			@WebParam(name="beginsWith") String beginsWith, 
			@WebParam(name="onlyAccepted") boolean onlyAccepted, 
			@WebParam(name="pagesize") int pagesize, 
			@WebParam(name="page") int page);
	
	//TODO to be continued .....
	
	
}
