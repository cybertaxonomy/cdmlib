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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;

/**
 * The NamePortalController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * Methods mapped at type level, inherited from super classes ({@link BaseController}):
 * <blockquote>
 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;name&#x002F;{name-uuid}</b>
 * 
 * Get the {@link TaxonNameBase} instance identified by the <code>{name-uuid}</code>.
 * The returned TaxonNameBase is initialized by
 * the following strategy: -- NONE --
 * </blockquote>
 * 
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/portal/name/*","/portal/name/*/typedesignations"})
public class NamePortalController extends BaseController<TaxonNameBase, INameService>
{
	
	private static final List<String> TYPE_INIT_STRATEGY = Arrays.asList(new String []{
			"citation.authorTeam",
			"typeName.$",
			"typeName.taggedName",
			"typeStatus.representations",
			"typeSpecimen.media.representations.parts"
	});

	public NamePortalController(){
		super();
		setUuidParameterPattern("^/portal/name/([^/?#&\\.]+).*");
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

	/**
     * Get the list of {@link TypeDesignationBase}s of the 
	 * {@link TaxonNameBase} instance identified by the <code>{name-uuid}</code>.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;name&#x002F;{name-uuid}&#x002F;typeDesignations</b>
	 * 
	 * @param request
	 * @param response
	 * @return a List of {@link TypeDesignationBase} entities which are initialized
	 *         using the following initialization strategy:
	 *         {@link #TYPE_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(
			value = {"/portal/name/*/typedesignations"},
			method = RequestMethod.GET)
	public ModelAndView doGetTypeDesignations(HttpServletRequest request, HttpServletResponse response)throws IOException {
		ModelAndView mv = new ModelAndView();
		TaxonNameBase name = getCdmBase(request, response, null, TaxonNameBase.class);
		Pager p = service.getTypeDesignations(name,  null, null, null, TYPE_INIT_STRATEGY);
		mv.addObject(p.getRecords());
		return mv;
	}


}
