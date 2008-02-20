package eu.etaxonomy.cdm.remote.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.ResultSetPageSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.dto.TreeNode;

/*
 * Methods adopted from ws_method as 
 * found in http://dev.e-taxonomy.eu/svn/trunk/drupal/modules/cdm_dataportal/cdm_api.module
 */
public interface CdmService {

	public Class whatis(UUID uuid); 

	public NameTO getName(UUID uuid);// throws BusinessLogicException;  
	
	public TaxonTO getTaxon(UUID uuid); 
	
	/**
	 * @param q : name querystring
	 * @param sec
	 * @param higherTaxa
	 * @param matchAnywhere
	 * @param onlyAccepted
	 * @param pagesize
	 * @param page
	 * @return
	 */
	public ResultSetPageSTO<TaxonSTO> findTaxa(String q, UUID sec, Set<UUID> higherTaxa, boolean matchAnywhere, boolean onlyAccepted, int pagesize, int page);
	
	public ResultSetPageSTO<TreeNode> getParentTaxa(String beginsWith, boolean onlyAccepted);
	
	
}
