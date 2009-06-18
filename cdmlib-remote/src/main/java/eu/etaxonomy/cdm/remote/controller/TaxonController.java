// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.AnnotatableServiceBase;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;

/**
 * @author a.kohlbecker
 *
 */

@Controller
@RequestMapping(value = {"/*/taxon/*","/*/taxon/*/*", "/*/taxon/*/annotation"})
public class TaxonController extends AnnotatableController<TaxonBase, ITaxonService>
{
	public static final Logger logger = Logger.getLogger(TaxonController.class);
	
	public TaxonController(){
		super();
		setUuidParameterPattern("^/(?:[^/]+)/taxon/([^/?#&\\.]+).*");
		setInitializationStrategy(Arrays.asList(new String[]{"$","name.nomenclaturalReference"}));
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(ITaxonService service) {
		this.service = service;
	}
	
	@RequestMapping(value = "/*/taxon/*/accepted", method = RequestMethod.GET)
	public Set<TaxonBase> getAccepted(
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		TaxonBase tb = doGet(request, response);
		HashSet<TaxonBase> resultset = new HashSet<TaxonBase>();
		if(tb instanceof Taxon){
			//the taxon already is accepted
			//FIXME take the current view into account once views are implemented!!!
			resultset.add((Taxon)tb);
		} else {
			Synonym syn = (Synonym)tb;
			resultset.addAll(syn.getAcceptedTaxa());
		}
		return resultset;
	}
		

	
}
