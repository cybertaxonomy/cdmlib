package eu.etaxonomy.cdm.remote.service;

import java.util.List;
import java.util.UUID;

import javax.jws.WebService;

import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;

/*
 * Methods adopted from ws_method as 
 * found in http://dev.e-taxonomy.eu/svn/trunk/drupal/modules/cdm_dataportal/cdm_api.module
 */
@WebService
public interface CdmService {

	public NameTO getName(UUID uuid);// throws BusinessLogicException;  
	
	public TaxonTO getTaxon(UUID uuid); 
	
	public Class whatis(UUID uuid); 
	
	public List<TaxonSTO> listNames(String beginsWith, int page, boolean onlyAccepted, int pagesize);
	
	//TODO to be continued .....
	
	
}
