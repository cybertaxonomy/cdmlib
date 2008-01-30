package eu.etaxonomy.cdm.facade;

import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.dto.NameTO;
import eu.etaxonomy.cdm.dto.TaxonSTO;
import eu.etaxonomy.cdm.dto.TaxonTO;

/*
 * Methods adopted from ws_method as 
 * found in http://dev.e-taxonomy.eu/svn/trunk/drupal/modules/cdm_dataportal/cdm_api.module
 */
public interface IRemoteFacade {
	
	public NameTO getName(UUID uuid); 
	
	public TaxonTO getTaxon(UUID uuid); 
	
	public Class whatis(UUID uuid); 
	
	public List<TaxonSTO> listNames(String beginsWith, int page, boolean onlyAccepted, int pagesize);
	
	//TODO to be continued .....
	
	
}
