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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonName;
import eu.etaxonomy.cdm.remote.editor.RankPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

/**
 * TODO write controller documentation
 * 
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/name/*", "/name/{uuid}"})
public class NameController extends AnnotatableController<TaxonNameBase, INameService>
{
	
	private static final List<String> TYPEDESIGNATION_INIT_STRATEGY = Arrays.asList(new String []{
			"typeStatus.representations",
			"typifiedNames.titleCache",
			"typeSpecimen.titleCache",
			"typeName.titleCache",
			"citation",
			"citation.authorTeam.$",
	});
	
	private static final List<String> NAME_CACHE_INIT_STRATEGY = Arrays.asList(new String []{
			"titleCache"
	});
	
	public NameController(){
		super();
		setUuidParameterPattern("^/name/([^/?#&\\.]+).*");
		setInitializationStrategy(Arrays.asList(new String[]{"$"}));
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(INameService service) {
		this.service = service;
	}
	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
	}
	
	/**
     * Get the list of {@link TypeDesignationBase}s of the 
	 * {@link TaxonNameBase} instance identified by the <code>{name-uuid}</code>.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;name&#x002F;{name-uuid}&#x002F;typeDesignations</b>
	 * 
	 * @param request
	 * @param response
	 * @return a List of {@link TypeDesignationBase} entities which are initialized
	 *         using the {@link #TYPEDESIGNATION_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"*/typeDesignations"},
			method = RequestMethod.GET)
	public List<TypeDesignationBase> doGetNameTypeDesignations(HttpServletRequest request, HttpServletResponse response)throws IOException {
		logger.info("doGetTypeDesignations()" + request.getServletPath());
		TaxonNameBase tnb = getCdmBase(request, response, null, TaxonNameBase.class);
		Pager<TypeDesignationBase> p = service.getTypeDesignations(tnb, null, null, null, TYPEDESIGNATION_INIT_STRATEGY);
		return p.getRecords();
	}
	
	@RequestMapping(
			value = {"*/nameCache"},
			method = RequestMethod.GET)
	public List<String> doGetNameCache(HttpServletRequest request, HttpServletResponse response)throws IOException {
		TaxonNameBase tnb = getCdmBase(request, response, NAME_CACHE_INIT_STRATEGY, TaxonNameBase.class);
		NonViralName nvn = (NonViralName) tnb;
		String nameCacheString = nvn.getNameCache();
		List result = new ArrayList<String>();
		result.add(nameCacheString);
		return result;
	}
	
}
