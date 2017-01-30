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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.controller.util.ControllerUtils;
import eu.etaxonomy.cdm.remote.editor.CdmTypePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.MatchModePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.NamedAreaPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import io.swagger.annotations.Api;

/**
 * The TaxonPortalController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * Methods mapped at type level, inherited from super classes ({@link BaseController}):
 * <blockquote>
 * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}</b>
 *
 * Get the {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
 * The returned Taxon is initialized by
 * the following strategy {@link #TAXON_INIT_STRATEGY}
 * </blockquote>
 *
 * @author a.kohlbecker
 * @date 20.07.2009
 *
 */
@Controller
@Api("portal_taxon")
@RequestMapping(value = {"/portal/taxon/{uuid}"})
public class TaxonPortalController extends TaxonController
{

    public static final Logger logger = Logger.getLogger(TaxonPortalController.class);

    @Autowired
    private INameService nameService;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private IClassificationService classificationService;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private ITermService termService;

    private static final List<String> TAXON_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "sources",
            // taxon relations
//            "relationsToThisName.fromTaxon.name",
            // the name
            "name.$",
            "name.nomenclaturalReference.authorship",
            "name.nomenclaturalReference.inReference",
            "name.rank.representations",
            "name.status.type.representations",

//            "descriptions" // TODO remove

            });

    private static final List<String> TAXON_WITH_NODES_INIT_STRATEGY = Arrays.asList(new String []{
            "taxonNodes.$",
            "taxonNodes.classification.$",
            "taxonNodes.childNodes.$"
            });

    private static final List<String> SIMPLE_TAXON_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            // the name
            "name.$",
            "name.rank.representations",
            "name.status.type.representations",
            "name.nomenclaturalReference.authorship",
            "name.nomenclaturalReference.inReference",
            "taxonNodes.classification",
            });

    private static final List<String> SYNONYMY_INIT_STRATEGY = Arrays.asList(new String []{
            // initialize homotypical and heterotypical groups; needs synonyms
            "synonyms.$",
            "synonyms.name.status.type.representations",
            "synonyms.name.nomenclaturalReference.authorship",
            "synonyms.name.nomenclaturalReference.inReference",
//            "synonyms.name.homotypicalGroup.typifiedNames.$",
//            "synonyms.name.homotypicalGroup.typifiedNames.taxonBases.$",
            "synonyms.name.combinationAuthorship.$",

            "name.typeDesignations",

            "name.homotypicalGroup.$",
            "name.homotypicalGroup.typifiedNames.$",
            "name.homotypicalGroup.typifiedNames.nomenclaturalReference.authorship",
            "name.homotypicalGroup.typifiedNames.nomenclaturalReference.inReference",
//            "name.homotypicalGroup.typifiedNames.taxonBases.$"
    });


    private static final List<String> TAXONRELATIONSHIP_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "type.inverseRepresentations",
            "fromTaxon.sec",
            "fromTaxon.name",
            "toTaxon.sec",
            "toTaxon.name"
    });

    private static final List<String> NAMERELATIONSHIP_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "type.inverseRepresentations",
            "toName.$",
            "toName.nomenclaturalReference.authorship",
            "toName.nomenclaturalReference.inReference",
            "fromName.$",
            "fromName.nomenclaturalReference.authorship",
            "fromName.nomenclaturalReference.inReference",

    });

    protected static final List<String> TAXONDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String [] {
            "$",
            "elements.$",
            "elements.stateData.$",
            "elements.sources.citation.authorship",
            "elements.sources.nameUsedInSource",
            "elements.multilanguageText",
            "elements.media",
            "elements.modifyingText",
            "elements.modifiers",
            "elements.kindOfUnit",
            "name.$",
            "name.rank.representations",
            "name.status.type.representations",
            "sources.$",
    });

    protected static final List<String> DESCRIPTION_ELEMENT_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "sources.citation.authorship",
            "sources.nameUsedInSource",
            "multilanguageText",
            "media",
    });


//	private static final List<String> NAMEDESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
//			"uuid",
//			"feature",
//			"elements.$",
//			"elements.multilanguageText",
//			"elements.media",
//	});

    protected static final List<String> TAXONDESCRIPTION_MEDIA_INIT_STRATEGY = Arrays.asList(new String []{
            "elements.media"

    });

    private static final List<String> TYPEDESIGNATION_INIT_STRATEGY = Arrays.asList(new String []{
            "typeSpecimen.$",
            "citation.authorship.$",
            "typeName",
            "typeStatus"
    });

    protected static final List<String> TAXONNODE_WITHTAXON_INIT_STRATEGY = Arrays.asList(new String []{
            "childNodes.taxon",
    });

    protected static final List<String> TAXONNODE_INIT_STRATEGY = Arrays.asList(new String []{
            "taxonNodes.classification"
    });



    private static final String featureTreeUuidPattern = "^/taxon(?:(?:/)([^/?#&\\.]+))+.*";

    public TaxonPortalController(){
        super();
        setInitializationStrategy(TAXON_INIT_STRATEGY);
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
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(NamedArea.class, new NamedAreaPropertyEditor());
        binder.registerCustomEditor(MatchMode.class, new MatchModePropertyEditor());
        binder.registerCustomEditor(Class.class, new CdmTypePropertyEditor());
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
        binder.registerCustomEditor(DefinedTermBaseList.class, new TermBaseListPropertyEditor<NamedArea>(termService));

    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.BaseController#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)

    @Override
    @RequestMapping(method = RequestMethod.GET)
    public TaxonBase doGet(HttpServletRequest request, HttpServletResponse response)throws IOException {
        logger.info("doGet()");
        TaxonBase tb = getCdmBase(request, response, TAXON_INIT_STRATEGY, TaxonBase.class);
        return tb;
    }
     */

    /**
     * Get the synonymy for a taxon identified by the <code>{taxon-uuid}</code>.
     * The synonymy consists
     * of two parts: The group of homotypic synonyms of the taxon and the
     * heterotypic synonymy groups of the taxon. The synonymy is ordered
     * historically by the type designations and by the publication date of the
     * nomenclatural reference
     * <p>
     * URI:
     * <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;synonymy</b>
     *
     *
     * @param request
     * @param response
     * @return a Map with to entries which are mapped by the following keys:
     *         "homotypicSynonymsByHomotypicGroup", "heterotypicSynonymyGroups",
     *         containing lists of {@link Synonym}s which are initialized using the
     *         following initialization strategy: {@link #SYNONYMY_INIT_STRATEGY}
     *
     * @throws IOException
     */
    @RequestMapping(
            value = {"synonymy"},
            method = RequestMethod.GET)
    public ModelAndView doGetSynonymy(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {

        if(request != null){
            logger.info("doGetSynonymy() " + requestPathAndQuery(request));
        }
        ModelAndView mv = new ModelAndView();
        Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);
        Map<String, List<?>> synonymy = new Hashtable<>();

        //new
        List<List<Synonym>> synonymyGroups = service.getSynonymsByHomotypicGroup(taxon, SYNONYMY_INIT_STRATEGY);
        synonymy.put("homotypicSynonymsByHomotypicGroup", synonymyGroups.get(0));
        synonymyGroups.remove(0);
        synonymy.put("heterotypicSynonymyGroups", synonymyGroups);



        //old
//        synonymy.put("homotypicSynonymsByHomotypicGroup", service.getHomotypicSynonymsByHomotypicGroup(taxon, SYNONYMY_INIT_STRATEGY));
//        synonymy.put("heterotypicSynonymyGroups", service.getHeterotypicSynonymyGroups(taxon, SYNONYMY_INIT_STRATEGY));


        mv.addObject(synonymy);
        return mv;
    }


    /**
     * Get the list of {@link TaxonRelationship}s for the given
     * {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;taxonRelationships</b>
     *
     * @param request
     * @param response
     * @return a List of {@link TaxonRelationship} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #TAXONRELATIONSHIP_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
            value = {"taxonRelationships"},
            method = RequestMethod.GET)
    public List<TaxonRelationship> doGetTaxonRelations(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {

        logger.info("doGetTaxonRelations()" + requestPathAndQuery(request));
        Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);
        List<TaxonRelationship> toRelationships = service.listToTaxonRelationships(taxon, null, null, null, null, TAXONRELATIONSHIP_INIT_STRATEGY);
        List<TaxonRelationship> fromRelationships = service.listFromTaxonRelationships(taxon, null, null, null, null, TAXONRELATIONSHIP_INIT_STRATEGY);

        List<TaxonRelationship> allRelationships = new ArrayList<TaxonRelationship>(toRelationships.size() + fromRelationships.size());
        allRelationships.addAll(toRelationships);
        allRelationships.addAll(fromRelationships);

        return allRelationships;
    }

    /**
     * Get the list of {@link NameRelationship}s of the Name associated with the
     * {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;nameRelationships</b>
     *
     * @param request
     * @param response
     * @return a List of {@link NameRelationship} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #NAMERELATIONSHIP_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
            value = {"toNameRelationships"},
            method = RequestMethod.GET)
    public List<NameRelationship> doGetToNameRelations(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {
        logger.info("doGetNameRelations()" + request.getRequestURI());
        TaxonBase taxonBase = getCdmBaseInstance(TaxonBase.class, uuid, response, (List<String>)null);
        List<NameRelationship> list = nameService.listNameRelationships(taxonBase.getName(), Direction.relatedTo, null, null, 0, null, NAMERELATIONSHIP_INIT_STRATEGY);
        //List<NameRelationship> list = nameService.listToNameRelationships(taxonBase.getName(), null, null, null, null, NAMERELATIONSHIP_INIT_STRATEGY);
        return list;
    }

    /**
     * Get the list of {@link NameRelationship}s of the Name associated with the
     * {@link TaxonBase} instance identified by the <code>{taxon-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;nameRelationships</b>
     *
     * @param request
     * @param response
     * @return a List of {@link NameRelationship} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #NAMERELATIONSHIP_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
            value = {"fromNameRelationships"},
            method = RequestMethod.GET)
    public List<NameRelationship> doGetFromNameRelations(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request, HttpServletResponse response)throws IOException {
        logger.info("doGetNameFromNameRelations()" + requestPathAndQuery(request));

        TaxonBase taxonbase = getCdmBaseInstance(TaxonBase.class, uuid, response, SIMPLE_TAXON_INIT_STRATEGY);
        List<NameRelationship> list = nameService.listNameRelationships(taxonbase.getName(), Direction.relatedFrom, null, null, 0, null, NAMERELATIONSHIP_INIT_STRATEGY);
        //List<NameRelationship> list = nameService.listFromNameRelationships(taxonbase.getName(), null, null, null, null, NAMERELATIONSHIP_INIT_STRATEGY);
        return list;
    }

    @Override
    @RequestMapping(value = "taxonNodes", method = RequestMethod.GET)
    public Set<TaxonNode>  doGetTaxonNodes(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetTaxonNodes" + requestPathAndQuery(request));
        TaxonBase taxon = service.load(uuid, TAXONNODE_INIT_STRATEGY);
        if(taxon instanceof Taxon){
            return ((Taxon)taxon).getTaxonNodes();
        } else {
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
            return null;
        }
    }

    /**
     * Get the list of {@link TaxonDescription}s of the
     * {@link Taxon} instance identified by the <code>{taxon-uuid}</code>.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;descriptions</b>
     *
     * @param request
     * @param response
     * @return a List of {@link TaxonDescription} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #TAXONDESCRIPTION_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
            value = {"descriptions"},
            method = RequestMethod.GET)
    public List<TaxonDescription> doGetDescriptions(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "markerTypes", required = false) UuidList markerTypeUUIDs,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException {
        if(request != null){
            logger.info("doGetDescriptions()" + requestPathAndQuery(request));
        }
        List<DefinedTermBase> markerTypeTerms = null;
        Set<UUID> sMarkerTypeUUIDs = null;

        if(markerTypeUUIDs != null && !markerTypeUUIDs.isEmpty()){
            sMarkerTypeUUIDs = new HashSet<UUID>(markerTypeUUIDs);
            markerTypeTerms = termService.find(sMarkerTypeUUIDs);
        } else if(markerTypeUUIDs != null && markerTypeUUIDs.isEmpty()){
            markerTypeTerms = new ArrayList<DefinedTermBase>();
        }
        Set<MarkerType> markerTypes = new HashSet<MarkerType>();
        List<TaxonDescription> descriptions = new ArrayList<TaxonDescription>();
        if (markerTypeTerms != null) {
            for (DefinedTermBase markerTypeTerm : markerTypeTerms) {
                markerTypes.add((MarkerType)markerTypeTerm);
            }
        }
        Taxon t = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);
        if (markerTypeTerms == null) {

            Pager<TaxonDescription> p = descriptionService.pageTaxonDescriptions(t, null, null, null, null, TAXONDESCRIPTION_INIT_STRATEGY);
            descriptions = p.getRecords();
        }

        else if (markerTypeTerms != null && markerTypeTerms.isEmpty()) {
            descriptions = descriptionService.listTaxonDescriptions(t, null, null, markerTypes, null, null, TAXONDESCRIPTION_INIT_STRATEGY);

        }
        else {
            descriptions = descriptionService.listTaxonDescriptions(t, null, null, markerTypes, null, null, TAXONDESCRIPTION_INIT_STRATEGY);
            /*for (TaxonDescription description: descriptions) {
                for (IdentifiableSource source :description.getSources()) {
                    if (source.getOriginalNameString() != null) {
                        description.
                    }

                }


            }*/
        }
        return descriptions;
    }

    @RequestMapping(value = "descriptions/elementsByType/{classSimpleName}", method = RequestMethod.GET)
    public ModelAndView doGetDescriptionElementsByType(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("classSimpleName") String classSimpleName,
            @RequestParam(value = "markerTypes", required = false) UuidList markerTypeUUIDs,
            @RequestParam(value = "count", required = false, defaultValue = "false") Boolean doCount,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("doGetDescriptionElementsByType() - " + requestPathAndQuery(request));

        ModelAndView mv = new ModelAndView();

        List<DescriptionElementBase> allElements = new ArrayList<DescriptionElementBase>();
        List<DescriptionElementBase> elements;
        int count = 0;

        List<String> initStrategy = doCount ? null : DESCRIPTION_ELEMENT_INIT_STRATEGY;

        List<TaxonDescription> taxonDescriptions = doGetDescriptions(uuid, markerTypeUUIDs, request, response);
        try {
            Class type;
            type = Class.forName("eu.etaxonomy.cdm.model.description."
                    + classSimpleName);
            if (taxonDescriptions != null) {
                for (TaxonDescription description : taxonDescriptions) {
                    elements = descriptionService.listDescriptionElements(description, null, type, null, 0, initStrategy);
                    allElements.addAll(elements);
                    count += elements.size();
                }

            }
        } catch (ClassNotFoundException e) {
            HttpStatusMessage.create(e.getLocalizedMessage(), 400).send(response);
        }
        if(doCount){
            mv.addObject(count);
        } else {
            mv.addObject(allElements);
        }
        return mv;
    }

//	@RequestMapping(value = "specimens", method = RequestMethod.GET)
//	public ModelAndView doGetSpecimens(
//			@PathVariable("uuid") UUID uuid,
//			HttpServletRequest request,
//			HttpServletResponse response) throws IOException, ClassNotFoundException {
//		logger.info("doGetSpecimens() - " + request.getRequestURI());
//
//		ModelAndView mv = new ModelAndView();
//
//		List<DerivedUnitFacade> derivedUnitFacadeList = new ArrayList<DerivedUnitFacade>();
//
//		// find speciemens in the TaxonDescriptions
//		List<TaxonDescription> taxonDescriptions = doGetDescriptions(uuid, request, response);
//		if (taxonDescriptions != null) {
//
//			for (TaxonDescription description : taxonDescriptions) {
//				derivedUnitFacadeList.addAll( occurrenceService.listDerivedUnitFacades(description, null) );
//			}
//		}
//		// TODO find speciemens in the NameDescriptions ??
//
//		// TODO also find type specimens
//
//		mv.addObject(derivedUnitFacadeList);
//
//		return mv;
//	}

    /**
     * Get the {@link Media} attached to the {@link Taxon} instance
     * identified by the <code>{taxon-uuid}</code>.
     *
     * Usage &#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;{taxon-
     * uuid}&#x002F;media&#x002F;{mime type
     * list}&#x002F;{size}[,[widthOrDuration}][,{height}]&#x002F;
     *
     * Whereas
     * <ul>
     * <li><b>{mime type list}</b>: a comma separated list of mime types, in the
     * order of preference. The forward slashes contained in the mime types must
     * be replaced by a colon. Regular expressions can be used. Each media
     * associated with this given taxon is being searched whereas the first
     * matching mime type matching a representation always rules.</li>
     * <li><b>{size},{widthOrDuration},{height}</b>: <i>not jet implemented</i>
     * valid values are an integer or the asterisk '*' as a wildcard</li>
     * </ul>
     *
     * @param request
     * @param response
     * @return a List of {@link Media} entities which are initialized
     *         using the following initialization strategy:
     *         {@link #TAXONDESCRIPTION_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
        value = {"media"},
        method = RequestMethod.GET)
    public List<Media> doGetMedia(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "type", required = false) Class<? extends MediaRepresentationPart> type,
            @RequestParam(value = "mimeTypes", required = false) String[] mimeTypes,
            @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
            @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
            @RequestParam(value = "includeTaxonDescriptions", required = true) Boolean  includeTaxonDescriptions,
            @RequestParam(value = "includeOccurrences", required = true) Boolean  includeOccurrences,
            @RequestParam(value = "includeTaxonNameDescriptions", required = true) Boolean  includeTaxonNameDescriptions,
            @RequestParam(value = "widthOrDuration", required = false) Integer  widthOrDuration,
            @RequestParam(value = "height", required = false) Integer height,
            @RequestParam(value = "size", required = false) Integer size,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        logger.info("doGetMedia() " + requestPathAndQuery(request));

        Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, (List<String>)null);

        Set<TaxonRelationshipEdge> includeRelationships = ControllerUtils.loadIncludeRelationships(relationshipUuids, relationshipInversUuids, termService);

        List<Media> returnMedia = getMediaForTaxon(taxon, includeRelationships,
                includeTaxonDescriptions, includeOccurrences, includeTaxonNameDescriptions,
                type, mimeTypes, widthOrDuration, height, size);
        return returnMedia;
    }

    @RequestMapping(
            value = {"subtree/media"},
            method = RequestMethod.GET)
        public List<Media> doGetSubtreeMedia(
                @PathVariable("uuid") UUID uuid,
                @RequestParam(value = "type", required = false) Class<? extends MediaRepresentationPart> type,
                @RequestParam(value = "mimeTypes", required = false) String[] mimeTypes,
                @RequestParam(value = "relationships", required = false) UuidList relationshipUuids,
                @RequestParam(value = "relationshipsInvers", required = false) UuidList relationshipInversUuids,
                @RequestParam(value = "includeTaxonDescriptions", required = true) Boolean  includeTaxonDescriptions,
                @RequestParam(value = "includeOccurrences", required = true) Boolean  includeOccurrences,
                @RequestParam(value = "includeTaxonNameDescriptions", required = true) Boolean  includeTaxonNameDescriptions,
                @RequestParam(value = "widthOrDuration", required = false) Integer  widthOrDuration,
                @RequestParam(value = "height", required = false) Integer height,
                @RequestParam(value = "size", required = false) Integer size,
                HttpServletRequest request, HttpServletResponse response)throws IOException {

        logger.info("doGetSubtreeMedia() " + requestPathAndQuery(request));

        Taxon taxon = getCdmBaseInstance(Taxon.class, uuid, response, TAXON_WITH_NODES_INIT_STRATEGY);

        Set<TaxonRelationshipEdge> includeRelationships = ControllerUtils.loadIncludeRelationships(relationshipUuids, relationshipInversUuids, termService);

        List<Media> returnMedia = getMediaForTaxon(taxon, includeRelationships,
                includeTaxonDescriptions, includeOccurrences, includeTaxonNameDescriptions,
                type, mimeTypes, widthOrDuration, height, size);
        TaxonNode node;
        //looking for all medias of genus
        if (taxon.getTaxonNodes().size()>0){
            Set<TaxonNode> nodes = taxon.getTaxonNodes();
            Iterator<TaxonNode> iterator = nodes.iterator();
            //TaxonNode holen
            node = iterator.next();
            //Check if TaxonNode belongs to the current tree

            node = classificationService.loadTaxonNode(node, TAXONNODE_WITHTAXON_INIT_STRATEGY);
            List<TaxonNode> children = node.getChildNodes();
            Taxon childTaxon;
            for (TaxonNode child : children){
                childTaxon = child.getTaxon();
                if(childTaxon != null) {
                childTaxon = (Taxon)taxonService.load(childTaxon.getUuid(), null);
                    returnMedia.addAll(getMediaForTaxon(childTaxon, includeRelationships,
                            includeTaxonDescriptions, includeOccurrences, includeTaxonNameDescriptions,
                            type, mimeTypes, widthOrDuration, height, size));
                }
            }
        }
        return returnMedia;
    }

    /**
     *
     * @param taxon
     * @param includeRelationships
     * @param type
     * @param mimeTypes
     * @param widthOrDuration
     * @param height
     * @param size
     * @return
     */
    private List<Media> getMediaForTaxon(Taxon taxon, Set<TaxonRelationshipEdge> includeRelationships,
            Boolean includeTaxonDescriptions, Boolean includeOccurrences, Boolean includeTaxonNameDescriptions,
            Class<? extends MediaRepresentationPart> type, String[] mimeTypes, Integer widthOrDuration,
            Integer height, Integer size) {

        // list the media
        logger.trace("getMediaForTaxon() - list the media");
        List<Media> taxonGalleryMedia = service.listMedia(taxon, includeRelationships,
                false, includeTaxonDescriptions, includeOccurrences, includeTaxonNameDescriptions, null);

        // filter by preferred size and type

        logger.trace("getMediaForTaxon() - filter the media");
        Map<Media, MediaRepresentation> mediaRepresentationMap = MediaUtils.findPreferredMedia(
                taxonGalleryMedia, type, mimeTypes, null, widthOrDuration, height, size);

        List<Media> filteredMedia = new ArrayList<Media>(mediaRepresentationMap.size());
        for (Media media : mediaRepresentationMap.keySet()) {
            media.getRepresentations().clear();
            media.addRepresentation(mediaRepresentationMap.get(media));
            filteredMedia.add(media);
        }

        logger.trace("getMediaForTaxon() - END ");

        return filteredMedia;
    }

// ---------------------- code snippet preserved for possible later use --------------------
//	@RequestMapping(
//			value = {"//*/portal/taxon/*/descriptions"}, // mapped as absolute path, see CdmAntPathMatcher
//			method = RequestMethod.GET)
//	public List<TaxonDescription> doGetDescriptionsbyFeatureTree(HttpServletRequest request, HttpServletResponse response)throws IOException {
//		TaxonBase tb = getCdmBase(request, response, null, Taxon.class);
//		if(tb instanceof Taxon){
//			//T O D O this is a quick and dirty implementation -> generalize
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
