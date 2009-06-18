// $Id: TaxonListController.java 5584 2009-04-09 10:04:18Z a.kohlbecker $
/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.impl.TaxonServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.kohlbecker
 * @date 20.03.2009
 */
//@Controller
@RequestMapping(value = {"/*/portal/taxon/"})
public class TaxonPortalListController extends BaseListController<TaxonBase, ITaxonService> {

	@Autowired
	private IReferenceService referenceService;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Override
	@Autowired
	public void setService(ITaxonService service) {
		this.service = service; 
	}
	
	public Pager<TaxonBase> doGet() {
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public Pager<IdentifiableEntity> doFind(
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "q", required = false) String query,
			@RequestParam(value = "doTaxa", required = false) Boolean doTaxa,
			@RequestParam(value = "doSynonyms", required = false) Boolean doSynonyms,
			@RequestParam(value = "doNamesWithoutTaxa", required = false) Boolean doNamesWithoutTaxa,
			@RequestParam(value = "secUuid", required = false) UUID secUuid) {
		
		if(page == null){ page = DEFAULT_PAGE;}
		if(pageSize == null){ pageSize = DEFAULT_PAGESIZE;}
			
		ITaxonServiceConfigurator config = new TaxonServiceConfiguratorImpl();
		config.setPageNumber(page);
		config.setPageSize(pageSize);
		config.setSearchString(query);
		config.setDoTaxa(doTaxa);
		config.setDoSynonyms(doSynonyms);
		config.setDoNamesWithoutTaxa(doNamesWithoutTaxa);
		config.setMatchMode(MatchMode.BEGINNING);
		if(secUuid != null){
			ReferenceBase sec = referenceService.findByUuid(secUuid);
			config.setSec(sec);
		}
			
		return (Pager<IdentifiableEntity>) service.findTaxaAndNames(config);
	}
}