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
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.wordnik.swagger.annotations.Api;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.TaxonNodeAgentRelation;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.RankPropertyEditor;

/**
 * The AgentController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * Methods mapped at type level, inherited from super classes ({@link BaseController}):
 * <blockquote>
 * URI: <b>&#x002F;{datasource-name}&#x002F;agent&#x002F;name&#x002F;{agent-uuid}</b>
 *
 * Get the {@link AgentBase} instance identified by the <code>{agent-uuid}</code>.
 * The returned AgentBase is initialized by
 * the default initialization strategy: {@link #DEFAULT_INIT_STRATEGY}
 * </blockquote>
 * <blockquote>
 * URI: <b>&#x002F;{datasource-name}&#x002F;agent&#x002F;name&#x002F;{agent-uuid}&#x002F;annotation</b>
 *
 * Returns a {@link Pager} on the {@link Annotation}s for the {@link AgentBase} instance identified by the
 * <code>{agent-uuid}</code>.
 * The returned AgentBase instances are initialized by
 * the following strategy: {@link #ANNOTATION_INIT_STRATEGY}
 * </blockquote>
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@Api(value = "agent")
@RequestMapping(value = {"/agent/{uuid}"})
public class AgentController extends BaseController<AgentBase, IAgentService>
{

    private static final List<String> TAXONNODEAGENTRELATIONS_INIT_STRATEGY = Arrays.asList(new String[]{
            // NOTE: all other cases are covered in the TaxonNodeDaoHibernateImpl method
            // which is using join fetches
            "taxonNode.taxon.name.nomenclaturalReference"
            });

    @Autowired
    private ITaxonNodeService nodeService;

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Autowired
    @Override
    public void setService(IAgentService service) {
        this.service = service;
    }

    @Override
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(Rank.class, new RankPropertyEditor());
    }

    /**
     * This method is only needed for
     * {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase} or sub classes
     * which are also handled by this controller.
     *
     * The method
     * {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase#getTitleCache() } is
     * annotated with @Transient and thus it is not automatically made available
     * by the BaseController.
     *
     * @param uuid
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = {"titleCache"}, method = RequestMethod.GET)
    public ModelAndView doGetTitleCache(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();
        AgentBase<?> agentbase = service.load(uuid);
        mv.addObject(agentbase.getTitleCache());
        return mv;

    }
    /**
     *
     * See also {@link TaxonController#doGetTaxonNodeAgentRelations(UUID, UUID, Integer, Integer, HttpServletRequest, HttpServletResponse)}
     *
     * @param uuid
     * @param classificationUuid
     * @param pageNumber
     * @param pageSize
     * @param request
     * @param response
     * @return
     * @throws IOException
     *
     */
    @RequestMapping(value = "taxonNodeAgentRelations", method = RequestMethod.GET)
    public Pager<TaxonNodeAgentRelation>  doGetTaxonNodeAgentRelations(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "classification_uuid" , required = false) UUID classificationUuid,
            @RequestParam(value = "rank" , required = false) Rank rank,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        UUID rankUuid = null;
        if(rank != null) {
            rankUuid = rank.getUuid();
        }
        Pager<TaxonNodeAgentRelation> pager = nodeService.pageTaxonNodeAgentRelations(null, classificationUuid, uuid,
                rankUuid, pagerParams.getPageSize(), pagerParams.getPageIndex(), TAXONNODEAGENTRELATIONS_INIT_STRATEGY);
        return pager;
    }

}
