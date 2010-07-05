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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.AnnotatableServiceBase;
import eu.etaxonomy.cdm.api.service.DescriptionServiceImpl;
import eu.etaxonomy.cdm.api.service.DistributionTree;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.NamedAreaTree;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.remote.editor.NamedAreaLevelPropertyEditor;

import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;

/**
 * TODO write controller documentation
 * 
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/portal/description/*","/portal/description/{uuid}", "/portal/description/{uuid_list}", "/portal/descriptionelement/*", "/portal/featuretree/*"})
public class DescriptionPortalController extends AnnotatableController<DescriptionBase, IDescriptionService>
{
	@Autowired
	private IFeatureTreeService featureTreeService;
	
	public DescriptionPortalController(){
		super();
		setUuidParameterPattern("^/(?:[^/]+)/([^/?#&\\.]+).*");
	}
	
	private static final List<String> FEATURETREE_INIT_STRATEGY = Arrays.asList(
			new String[]{
				"representations",
				"root.feature.representations",
				"root.children.feature.representations",
			});
	private static final List<String> DESCRIPTIONS_DISTRIBUTION_INIT_STRATEGY = Arrays.asList(new String []{
			// taxon descriptions
			//"$",
			//"elements.$",
			"elements.sources.citation",
			"elements.sources.citation.$",
			"elements.area",
			"elements.area.$",
			//"elements.area.level",
			});
	protected static final List<String> TAXONDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"elements.$",
			"elements.sources.citation.",
			"elements.sources.citation.authorTeam.$",		
//			"elements.sources.citation.authorTeam.titleCache",
//			"elements.sources.citation.authorTeam.nomenclaturalTitleCache",
//			"elements.sources.nameUsedInSource.titleCache",
//			"elements.sources.nameUsedInSource.originalNameString",
//			"elements.area",
			"elements.area.level",
//			"elements.modifyingText",
	});
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
		binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
		binder.registerCustomEditor(NamedAreaLevel.class, new NamedAreaLevelPropertyEditor());
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(IDescriptionService service) {
		this.service = service;
	}
	
	/**
	 * TODO write controller method documentation
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	
	@RequestMapping(value = {"/portal/featuretree/{featuretree_uuid}"}, method = RequestMethod.GET)
	public FeatureTree doGetFeatureTree(
			@PathVariable("featuretree_uuid") UUID featureUuid,
			HttpServletRequest request, 
			HttpServletResponse response)throws IOException {
		//UUID featureTreeUuid = readValueUuid(request, null);
		FeatureTree featureTree = featureTreeService.load(featureUuid, FEATURETREE_INIT_STRATEGY);
		if(featureTree == null){
			HttpStatusMessage.UUID_NOT_FOUND.send(response);
		}
		return featureTree;
	}
	
	@RequestMapping(value = "/{descriptionelement_uuid}/annotation", method = RequestMethod.GET)
	public Pager<Annotation> getAnnotations(
			@PathVariable("descriptionelement_uuid") UUID uuid,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		logger.info("getAnnotations() - " + request.getServletPath());
		DescriptionElementBase annotatableEntity = service.getDescriptionElementByUuid(uuid);
		Pager<Annotation> annotations = service.getDescriptionElementAnnotations(annotatableEntity, null, null, 0, null, ANNOTATION_INIT_STRATEGY);
		return annotations;
	}
	
	@RequestMapping(value = "{uuid_list}/namedAreaTree", method = RequestMethod.GET)
	public NamedAreaTree doGetOrderedDistributions(
			@PathVariable("uuid_list") UuidList descriptionUuidList,
			@RequestParam(value = "omitLevels", required = false) Set<NamedAreaLevel> levels,
			//@ModelAttribute("omitLevels") HashSet<NamedAreaLevel> levels,
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("getOrderedDistributions(" + ObjectUtils.toString(levels) + ") - " + request.getServletPath());
		Set<TaxonDescription> taxonDescriptions = new HashSet<TaxonDescription>();
		TaxonDescription description;
		for (UUID descriptionUuid : descriptionUuidList) {
			description = (TaxonDescription) service.load(descriptionUuid);
			taxonDescriptions.add(description);
		}
		NamedAreaTree areaTree = service.getOrderedDistributions(taxonDescriptions, levels);
		return areaTree;
	}
	
	@RequestMapping(value = "{uuid_list}/DistributionTree", method = RequestMethod.GET)
	public DistributionTree doGetOrderedDistributionsB(
			@PathVariable("uuid_list") UuidList descriptionUuidList,
			@RequestParam(value = "omitLevels", required = false) Set<NamedAreaLevel> levels,
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("getOrderedDistributionsB(" + ObjectUtils.toString(levels) + ") - " + request.getServletPath());
		Set<TaxonDescription> taxonDescriptions = new HashSet<TaxonDescription>();
		TaxonDescription description;
		for (UUID descriptionUuid : descriptionUuidList) {
			description = (TaxonDescription) service.load(descriptionUuid, null);
			taxonDescriptions.add(description);
		}
		DistributionTree distTree = service.getOrderedDistributionsB(taxonDescriptions, levels, TAXONDESCRIPTION_INIT_STRATEGY);
		return distTree;
	}
	

}
