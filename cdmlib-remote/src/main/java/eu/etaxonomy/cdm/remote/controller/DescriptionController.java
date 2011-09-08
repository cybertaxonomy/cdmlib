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
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.remote.editor.NamedAreaLevelPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/description/{uuid}", "/description/{uuid_list}"})
public class DescriptionController extends AnnotatableController<DescriptionBase, IDescriptionService>
{
	@Autowired
	private IFeatureTreeService featureTreeService;


	public DescriptionController(){
		super();
	}

	private static final List<String> FEATURETREE_INIT_STRATEGY = Arrays.asList(
			new String[]{
				"representations",
				"root.feature.representations",
				"root.children.feature.representations",
				"root.children.children.feature.representations",
			});

	@InitBinder
	@Override
	public void initBinder(WebDataBinder binder) {
		super.initBinder(binder);
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
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = {"/featureTree/{uuid}"}, method = RequestMethod.GET)
	public FeatureTree doGetFeatureTree(
			@PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
			HttpServletResponse response)throws IOException {
		FeatureTree featureTree = getCdmBaseInstance(FeatureTree.class, featureTreeService, uuid, response, FEATURETREE_INIT_STRATEGY);
		return featureTree;
	}


    @RequestMapping(value = "/descriptionElement/{descriptionelement_uuid}/annotations", method = RequestMethod.GET)
	public Pager<Annotation> getAnnotations(
			@PathVariable("descriptionelement_uuid") UUID uuid,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		logger.info("getAnnotations() - " + request.getServletPath());
		DescriptionElementBase annotatableEntity = service.getDescriptionElementByUuid(uuid);
		if(annotatableEntity == null){
			HttpStatusMessage.UUID_INVALID.send(response);
			// method will exit here
			return null;
		}

		Pager<Annotation> annotations = service.getDescriptionElementAnnotations(annotatableEntity, null, null, 0, null, ANNOTATION_INIT_STRATEGY);
		return annotations;
	}

	/*
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
	*/

	@RequestMapping(value = "/description/{uuid}/naturalLanguageDescription/{featuretree_uuid}", method = RequestMethod.GET)
	public ModelAndView doGenerateNaturalLanguageDescription(
			@PathVariable("uuid") UUID uuid,
			@PathVariable("featuretree_uuid") UUID featureTreeUuid,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		logger.info("doGenerateNaturalLanguageDescription() - " + request.getServletPath());

		DescriptionBase description = service.load(uuid);

		ModelAndView mv = new ModelAndView();

		List<Language> languages = LocaleContext.getLanguages();

		if(!(description instanceof TaxonDescription)){
			HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
			// will terminate thread
		}

		FeatureTree featureTree = featureTreeService.load(featureTreeUuid, null);
		if(featureTree == null){
			HttpStatusMessage.UUID_NOT_FOUND.send(response);
			// will terminate thread
		}

		String naturalLanguageDescription = service.generateNaturalLanguageDescription(
                featureTree,
                (TaxonDescription)description,
				languages,
				", ");

		TextData textData = TextData.NewInstance(Feature.DESCRIPTION());
		textData.putText(Language.DEFAULT(), naturalLanguageDescription);

		mv.addObject(textData);

		return mv;
	}


	@RequestMapping(value = "/description/{uuid}/hasStructuredData", method = RequestMethod.GET)
	public ModelAndView doHasStructuredData(
			@PathVariable("uuid") UUID uuid,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		logger.info("doHasStructuredData() - " + request.getServletPath());

		ModelAndView mv = new ModelAndView();

		DescriptionBase description = service.load(uuid);

		if(!(description instanceof TaxonDescription)){
			HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
			// will terminate thread
		}

		boolean hasStructuredData = service.hasStructuredData(description);

		mv.addObject(hasStructuredData);

		return mv;
	}

}