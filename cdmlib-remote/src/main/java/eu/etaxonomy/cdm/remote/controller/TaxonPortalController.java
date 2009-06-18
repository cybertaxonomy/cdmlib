// $Id: TaxonController.java 5473 2009-03-25 13:42:07Z a.kohlbecker $
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.impl.TaxonServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

/**
 * @author a.kohlbecker
 *
 */

@Controller
@RequestMapping(value = {"/*/portal/taxon/*", "/*/portal/taxon/*/*", "/*/portal/name/*/*"})
public class TaxonPortalController extends BaseController<TaxonBase, ITaxonService>
{
	public static final Logger logger = Logger.getLogger(TaxonPortalController.class);
	
	@Autowired
	private INameService nameService;
	@Autowired
	private IDescriptionService descriptionService;
	@Autowired
	private IReferenceService referenceService;
	
	
	private static final List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String []{
			"*",
			// taxon relations 
			"relationsToThisName.fromTaxon.name.taggedName",
			// the name
			"name.$",
			"name.taggedName",
			"name.rank.representations",
			"name.status.type.representations",
			
			// taxon descriptions
			"descriptions.elements.$",
			"descriptions.elements.area",
			"descriptions.elements.area.$",
			"descriptions.elements.multilanguageText",
			"descriptions.elements.media.representations.parts",
			
//			// typeDesignations
//			"name.typeDesignations.$",
//			"name.typeDesignations.citation.authorTeam",
//			"name.typeDesignations.typeName.$",
//			"name.typeDesignations.typeStatus.representations",
//			"name.typeDesignations.typeSpecimen.media.representations.parts"
			
			});
	
	private static final List<String> SIMPLE_TAXON_INIT_STRATEGY = Arrays.asList(new String []{
			"*",
			// taxon relations 
			"relationsToThisName.fromTaxon.name.taggedName",
			// the name
			"name.$",
			"name.taggedName",
			"name.rank.representations",
			"name.status.type.representations"
			});
	
	private static final List<String> SYNONYMY_INIT_STRATEGY = Arrays.asList(new String []{
			// initialize homotypical and heterotypical groups; needs synonyms
			"synonymRelations.$",
			"synonymRelations.synonym.$",
			"synonymRelations.synonym.name.taggedName",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.$",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.name.taggedName",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.taxonBases.$",
			"synonymRelations.synonym.name.homotypicalGroup.typifiedNames.taxonBases.name.taggedName",
			
			"name.homotypicalGroup.$",
			"name.homotypicalGroup.typifiedNames.$",
			"name.homotypicalGroup.typifiedNames.name.taggedName",
			"name.homotypicalGroup.typifiedNames.taxonBases.$",
			"name.homotypicalGroup.typifiedNames.taxonBases.name.taggedName"
	});
	
	private static final List<String> TAXONRELATIONSHIP_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"type.inverseRepresentations",
			"fromTaxon.sec.authorTeam",
			"fromTaxon.name.taggedName"
	});
	
	private static final List<String> NAMERELATIONSHIP_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"type.inverseRepresentations",
			"fromName.taggedName",
	});
	
	
	protected static final List<String> TAXONDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"elements.$",
			"elements.citation.authorTeam",
			"elements.multilanguageText",
			"elements.media.representations.parts",
	});
	
	private static final List<String> NAMEDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
			"uuid",
			"feature",
			
			"elements.$",
			"elements.multilanguageText",
			"elements.media.representations.parts",
	});
	
	private static final List<String> TYPEDESIGNATION_INIT_STRATEGY = Arrays.asList(new String []{
			//"$",
			"typeSpecimen.$",
			"typeStatus.representations",
			"citation.authorTeam",
			"typeName.taggedName"
	});
	
	
	
	private static final String featureTreeUuidPattern = "^/(?:[^/]+)/taxon(?:(?:/)([^/?#&\\.]+))+.*";
	
	public TaxonPortalController(){
		super();
		setUuidParameterPattern("^/(?:[^/]+)/portal/(?:[^/]+)/([^/?#&\\.]+).*");
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(ITaxonService service) {
		this.service = service;
	}
	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
	}
	
	
	@Override
	@RequestMapping(method = RequestMethod.GET)
	public TaxonBase doGet(HttpServletRequest request, HttpServletResponse response)throws IOException {
		TaxonBase tb = getCdmBase(request, response, TAXON_INIT_STRATEGY, TaxonBase.class);
		return tb;
	}
	
	@RequestMapping(method = RequestMethod.GET,
			value = {"/*/portal/taxon/find"}) //TODO map to path /*/portal/taxon/ 
	public Pager<IdentifiableEntity> doFind(
			@RequestParam(value = "q", required = false) String query,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "doTaxa", required = false) Boolean doTaxa,
			@RequestParam(value = "doSynonyms", required = false) Boolean doSynonyms,
			@RequestParam(value = "doTaxaByCommonNames", required = false) Boolean doTaxaByCommonNames,
			@RequestParam(value = "secUuid", required = false) UUID secUuid) throws IOException {
		
		if(page == null){ page = BaseListController.DEFAULT_PAGE;}
		if(pageSize == null){ pageSize = BaseListController.DEFAULT_PAGESIZE;}
			
		ITaxonServiceConfigurator config = new TaxonServiceConfiguratorImpl();
		config.setPageNumber(page);
		config.setPageSize(pageSize);
		config.setSearchString(query);
		config.setDoTaxa(doTaxa!= null ? doTaxa : Boolean.FALSE );
		config.setDoSynonyms(doSynonyms != null ? doSynonyms : Boolean.FALSE );
		config.setDoTaxaByCommonNames(doTaxaByCommonNames != null ? doTaxaByCommonNames : Boolean.FALSE );
		config.setMatchMode(MatchMode.BEGINNING);
		config.setTaxonPropertyPath(SIMPLE_TAXON_INIT_STRATEGY);
		if(secUuid != null){
			ReferenceBase sec = referenceService.findByUuid(secUuid);
			config.setSec(sec);
		}
			
		return (Pager<IdentifiableEntity>) service.findTaxaAndNames(config);
	}

	
	@RequestMapping(
			value = {"/*/portal/taxon/*/synonymy"},
			method = RequestMethod.GET)
	public ModelAndView doGetSynonymy(HttpServletRequest request, HttpServletResponse response)throws IOException {
		ModelAndView mv = new ModelAndView();
		TaxonBase tb = getCdmBase(request, response, null, Taxon.class);
		Taxon taxon = (Taxon)tb;
		Map<String, List<?>> synonymy = new Hashtable<String, List<?>>();
		synonymy.put("homotypicSynonymsByHomotypicGroup", service.getHomotypicSynonymsByHomotypicGroup(taxon, SYNONYMY_INIT_STRATEGY));
		synonymy.put("heterotypicSynonymyGroups", service.getHeterotypicSynonymyGroups(taxon, SYNONYMY_INIT_STRATEGY));
		mv.addObject(synonymy);
		return mv;
	}
	
	@RequestMapping(
			value = {"/*/portal/taxon/*/taxonRelationships"},
			method = RequestMethod.GET)
	public List<TaxonRelationship> doGetTaxonRelations(HttpServletRequest request, HttpServletResponse response)throws IOException {

		TaxonBase tb = getCdmBase(request, response, null, Taxon.class);
		Taxon taxon = (Taxon)tb;
		List<TaxonRelationship> relations = new ArrayList<TaxonRelationship>();
		List<TaxonRelationship> results = service.listToTaxonRelationships(taxon, TaxonRelationshipType.MISAPPLIED_NAME_FOR(), null, null, null, TAXONRELATIONSHIP_INIT_STRATEGY);
		relations.addAll(results);
		results = service.listToTaxonRelationships(taxon, TaxonRelationshipType.INVALID_DESIGNATION_FOR(), null, null, null, TAXONRELATIONSHIP_INIT_STRATEGY);
		relations.addAll(results);

		return relations;
	}
	
	@RequestMapping(
			value = {"/*/portal/taxon/*/nameRelationships"},
			method = RequestMethod.GET)
	public List<NameRelationship> doGetNameRelations(HttpServletRequest request, HttpServletResponse response)throws IOException {
		TaxonBase tb = getCdmBase(request, response, SIMPLE_TAXON_INIT_STRATEGY, Taxon.class);
		List<NameRelationship> list = nameService.listToNameRelationships(tb.getName(), null, null, null, null, NAMERELATIONSHIP_INIT_STRATEGY);
		return list;
	}
	
	@RequestMapping(
			value = {"/*/portal/name/*/descriptions"},
			method = RequestMethod.GET)
	public List<TaxonNameDescription> doGetNameDescriptions(HttpServletRequest request, HttpServletResponse response)throws IOException {
		UUID nameUuuid = readValueUuid(request, null);
		TaxonNameBase tnb = nameService.load(nameUuuid, null);
		Pager<TaxonNameDescription> p = descriptionService.getTaxonNameDescriptions(tnb, null, null, NAMEDESCRIPTION_INIT_STRATEGY);
		return p.getRecords();
	}
	
	@RequestMapping(
			value = {"/*/portal/taxon/*/nameTypeDesignations"},
			method = RequestMethod.GET)
	public List<TypeDesignationBase> doGetNameTypeDesignations(HttpServletRequest request, HttpServletResponse response)throws IOException {
		TaxonBase tb = getCdmBase(request, response, SIMPLE_TAXON_INIT_STRATEGY, Taxon.class);
		Pager<TypeDesignationBase> p = nameService.getTypeDesignations(tb.getName(), null, null, null, TYPEDESIGNATION_INIT_STRATEGY);
		return p.getRecords();
	}
	
	@RequestMapping(
			value = {"/*/portal/taxon/*/descriptions"},
			method = RequestMethod.GET)
	public List<TaxonDescription> doGetDescriptions(HttpServletRequest request, HttpServletResponse response)throws IOException {
		TaxonBase tb = getCdmBase(request, response, null, Taxon.class);
		if(tb instanceof Taxon){
		Pager<TaxonDescription> p = descriptionService.getTaxonDescriptions((Taxon)tb, null, null, null, null, TAXONDESCRIPTION_INIT_STRATEGY);
			return p.getRecords();
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "invalid type; Taxon expected but " + tb.getClass().getSimpleName() + " found.");
			return null;
		}
	}
	
//	@RequestMapping(
//			value = {"/*/portal/taxon/*/descriptions"},
//			method = RequestMethod.GET)
//	public List<TaxonDescription> doGetDescriptionsbyFeatureTree(HttpServletRequest request, HttpServletResponse response)throws IOException {
//		TaxonBase tb = getCdmBase(request, response, null, Taxon.class);
//		if(tb instanceof Taxon){
//			//TODO this is a quick and dirty implementation -> generalize
//			UUID featureTreeUuid = readValueUuid(request, featureTreeUuidPattern);
//			
//			FeatureTree featureTree = descriptionService.getFeatureTreeByUuid(featureTreeUuid);
//			Pager<TaxonDescription> p = descriptionService.getTaxonDescriptions((Taxon)tb, null, null, null, null, TAXONDESCRIPTION_INIT_STRATEGY);
//			List<TaxonDescription> descriptions = p.getRecords();
//			
//			if(!featureTree.isDescriptionSeparated()){
//				
//				TaxonDescription superDescription = TaxonDescription.NewInstance();
//				//put all descriptionElements in superDescription and make it invisible
//				for(TaxonDescription description: descriptions){
//					for(DescriptionElementBase element: description.getElements()){
//						superDescription.addElement(element);
//					}
//				}
//				List<TaxonDescription> separatedDescriptions = new ArrayList<TaxonDescription>(descriptions.size());
//				separatedDescriptions.add(superDescription);
//				return separatedDescriptions;
//			}else{
//				return descriptions;
//			}
//		} else {
//			response.sendError(HttpServletResponse.SC_NOT_FOUND, "invalid type; Taxon expected but " + tb.getClass().getSimpleName() + " found.");
//			return null;
//		}
//	}

}
