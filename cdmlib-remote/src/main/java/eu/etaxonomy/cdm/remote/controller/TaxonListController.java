/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.TaxaAndNamesSearchMode;
import eu.etaxonomy.cdm.api.service.config.FindTaxaAndNamesConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator;
import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.dto.MarkedEntityDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.search.LuceneMultiSearchException;
import eu.etaxonomy.cdm.api.service.search.LuceneParseException;
import eu.etaxonomy.cdm.api.service.search.SearchResult;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.NameSearchOrder;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.TaxonTitleType;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.MatchModePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.RankPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.TermBasePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 20.03.2009
 */
@Controller
@Api("taxon")
@RequestMapping(value = {"/taxon"})
public class TaxonListController extends AbstractIdentifiableListController<TaxonBase, ITaxonService> {


    private static final List<String> SIMPLE_TAXON_INIT_STRATEGY = DEFAULT_INIT_STRATEGY;
    protected List<String> getSimpleTaxonInitStrategy() {
        return SIMPLE_TAXON_INIT_STRATEGY;
    }

    /**
     *
     */
    public TaxonListController(){
        super();
        setInitializationStrategy(Arrays.asList(new String[]{"$","name.nomenclaturalReference"}));
    }

    @Override
    @Autowired
    public void setService(ITaxonService service) {
        this.service = service;
    }

    @Autowired
    private IClassificationService classificationService;

    @Autowired
    private ITaxonNodeService taxonNodeService;


    @Autowired
    private ITermService termService;

    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(DefinedTermBaseList.class, new TermBaseListPropertyEditor<NamedArea>(termService));
        binder.registerCustomEditor(MatchMode.class, new MatchModePropertyEditor());
        binder.registerCustomEditor(Rank.class, new RankPropertyEditor());
        binder.registerCustomEditor(PresenceAbsenceTerm.class, new TermBasePropertyEditor<PresenceAbsenceTerm>(termService));

    }

    /**
     * Find Taxa, Synonyms, Common Names by name, either globally or in a specific geographic area.
     * <p>
     * URI: <b>taxon&#x002F;search</b>
     *
     * @param query
     *            the string to query for. Since the wildcard character '*'
     *            internally always is appended to the query string, a search
     *            always compares the query string with the beginning of a name.
     *            - <i>required parameter</i>
     * @param classificationUuid
     *            the {@link UUID} of the {@link Classification} to which the
     *            search is to be restricted. - <i>optional parameter</i>
     * @param areas
     *            restrict the search to a set of geographic {@link NamedArea}s.
     *            The parameter currently takes a list of TDWG area labels.
     *            - <i>optional parameter</i>
     * @param pageNumber
     *            the number of the page to be returned, the first page has the
     *            pageNumber = 1 - <i>optional parameter</i>
     * @param pageSize
     *            the maximum number of entities returned per page (can be -1
     *            to return all entities in a single page) - <i>optional parameter</i>
     * @param doTaxa
     *            weather to search for instances of {@link Taxon} - <i>optional parameter</i>
     * @param doSynonyms
     *            weather to search for instances of {@link Synonym} - <i>optional parameter</i>
     * @param doTaxaByCommonNames
     *            for instances of {@link Taxon} by a common name used - <i>optional parameter</i>
     * @return a Pager on a list of {@link IdentifiableEntity}s initialized by
     *         the following strategy {@link #SIMPLE_TAXON_INIT_STRATEGY}
     * @throws IOException
     * @throws LuceneMultiSearchException
     * @throws ParseException
     */
    @RequestMapping(method = RequestMethod.GET, value={"search"})
    public Pager<SearchResult<TaxonBase>> doSearch(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "classificationUuid", required = false) UUID classificationUuid,
            @RequestParam(value = "area", required = false) DefinedTermBaseList<NamedArea> areaList,
            @RequestParam(value = "status", required = false) PresenceAbsenceTerm[] status,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "doTaxa", required = false) Boolean doTaxa,
            @RequestParam(value = "doSynonyms", required = false) Boolean doSynonyms,
            @RequestParam(value = "doMisappliedNames", required = false) Boolean doMisappliedNames,
            @RequestParam(value = "doTaxaByCommonNames", required = false) Boolean doTaxaByCommonNames,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException, LuceneParseException, LuceneMultiSearchException {


        logger.info("search : " + requestPathAndQuery(request) );

        Set<NamedArea> areaSet = null;
        if(areaList != null){
            areaSet = new HashSet<NamedArea>(areaList.size());
            areaSet.addAll(areaList);
            TaxonListController.includeAllSubAreas(areaSet, termService);
        }

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        // TODO change type of do* parameters  to TaxaAndNamesSearchMode
        EnumSet<TaxaAndNamesSearchMode> searchModes = EnumSet.noneOf(TaxaAndNamesSearchMode.class);
        if(BooleanUtils.toBoolean(doTaxa)) {
            searchModes.add(TaxaAndNamesSearchMode.doTaxa);
        }
        if(BooleanUtils.toBoolean(doSynonyms)) {
            searchModes.add(TaxaAndNamesSearchMode.doSynonyms);
        }
        if(BooleanUtils.toBoolean(doMisappliedNames)) {
            searchModes.add(TaxaAndNamesSearchMode.doMisappliedNames);
        }
        if(BooleanUtils.toBoolean(doTaxaByCommonNames)) {
            searchModes.add(TaxaAndNamesSearchMode.doTaxaByCommonNames);
        }

        Classification classification = classificationService.load(classificationUuid);

        Set<PresenceAbsenceTerm> statusSet = null;
        if(status != null) {
                statusSet = new HashSet<PresenceAbsenceTerm>(Arrays.asList(status));
        }

        return service.findTaxaAndNamesByFullText(searchModes, query,
                classification, areaSet, statusSet, null,
                false, pagerParams.getPageSize(), pagerParams.getPageIndex(),
                OrderHint.NOMENCLATURAL_SORT_ORDER.asList(), getSimpleTaxonInitStrategy());
    }

    /**
     * Find Taxa, Synonyms, Common Names by name, either globally or in a specific geographic area.
     * <p>
     * URI: <b>&#x002F;{datasource-name}&#x002F;portal&#x002F;taxon&#x002F;find</b>
     *
     * @param query
     *            the string to query for. Since the wildcard character '*'
     *            internally always is appended to the query string, a search
     *            always compares the query string with the beginning of a name.
     *            - <i>required parameter</i>
     * @param treeUuid
     *            the {@link UUID} of a {@link Classification} to which the
     *            search is to be restricted. - <i>optional parameter</i>
     * @param areas
     *            restrict the search to a set of geographic {@link NamedArea}s.
     *            The parameter currently takes a list of TDWG area labels.
     *            - <i>optional parameter</i>
     * @param pageNumber
     *            the number of the page to be returned, the first page has the
     *            pageNumber = 1 - <i>optional parameter</i>
     * @param pageSize
     *            the maximum number of entities returned per page (can be -1
     *            to return all entities in a single page) - <i>optional parameter</i>
     * @param doTaxa
     *            weather to search for instances of {@link Taxon} - <i>optional parameter</i>
     * @param doSynonyms
     *            weather to search for instances of {@link Synonym} - <i>optional parameter</i>
     * @param doTaxaByCommonNames
     *            for instances of {@link Taxon} by a common name used - <i>optional parameter</i>
     * @param matchMode
     *           valid values are "EXACT", "BEGINNING", "ANYWHERE", "END" (case sensitive !!!)
     * @return a Pager on a list of {@link IdentifiableEntity}s initialized by
     *         the following strategy {@link #SIMPLE_TAXON_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value={"find"})
    public Pager<IdentifiableEntity> doFind(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "tree", required = false) UUID treeUuid,
            @RequestParam(value = "area", required = false) Set<NamedArea> areas,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "doTaxa", required = false) Boolean doTaxa,
            @RequestParam(value = "doSynonyms", required = false) Boolean doSynonyms,
            @RequestParam(value = "doMisappliedNames", required = false) Boolean doMisappliedNames,
            @RequestParam(value = "doTaxaByCommonNames", required = false) Boolean doTaxaByCommonNames,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
            @RequestParam(value = "order", required = false, defaultValue="ALPHA") NameSearchOrder order,
            @RequestParam(value = "includeAuthors", required = false) Boolean includeAuthors,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException {

        boolean includeUnpublished = NO_UNPUBLISHED;

        logger.info("find : " + request.getRequestURI() + "?" + request.getQueryString() );

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        IFindTaxaAndNamesConfigurator<?> config = new FindTaxaAndNamesConfiguratorImpl<>();
        config.setPageNumber(pagerParams.getPageIndex());
        config.setPageSize(pagerParams.getPageSize());
        config.setTitleSearchString(query);
        config.setDoTaxa(doTaxa!= null ? doTaxa : Boolean.FALSE );
        config.setDoSynonyms(doSynonyms != null ? doSynonyms : Boolean.FALSE );
        config.setDoMisappliedNames(doMisappliedNames != null ? doMisappliedNames : Boolean.FALSE);
        config.setDoTaxaByCommonNames(doTaxaByCommonNames != null ? doTaxaByCommonNames : Boolean.FALSE );
        config.setMatchMode(matchMode != null ? matchMode : MatchMode.BEGINNING);
        config.setTaxonPropertyPath(getSimpleTaxonInitStrategy());
        config.setNamedAreas(areas);
        config.setDoIncludeAuthors(includeAuthors != null ? includeAuthors : Boolean.FALSE);
        config.setOrder(order);
        config.setIncludeUnpublished(includeUnpublished);
        if(treeUuid != null){
            Classification classification = classificationService.find(treeUuid);
            config.setClassification(classification);
        }

        return service.findTaxaAndNames(config);

    }


    /**
     * @param clazz
     * @param queryString
     * @param treeUuid TODO unimplemented in TaxonServiceImpl !!!!
     * @param languages
     * @param pageNumber
     * @param pageSize
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @RequestMapping(method = RequestMethod.GET, value={"findByDescriptionElementFullText"})
    public Pager<SearchResult<TaxonBase>> dofindByDescriptionElementFullText(
            @RequestParam(value = "clazz", required = false) Class<? extends DescriptionElementBase> clazz,
            @RequestParam(value = "query", required = true) String queryString,
            @RequestParam(value = "tree", required = false) UUID treeUuid,
            @RequestParam(value = "features", required = false) UuidList featureUuids,
            @RequestParam(value = "languages", required = false) List<Language> languages,
            @RequestParam(value = "hl", required = false) Boolean highlighting,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException, LuceneParseException {

         logger.info("findByDescriptionElementFullText : " + requestPathAndQuery(request) );

         PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
         pagerParams.normalizeAndValidate(response);

         if(highlighting == null){
             highlighting = false;
         }

         Classification classification = null;
        if(treeUuid != null){
            classification = classificationService.find(treeUuid);
        }

        List<Feature> features = null;
        if(featureUuids != null){
            features = new ArrayList<Feature>(featureUuids.size());
            for(UUID uuid : featureUuids){
                features.add((Feature) termService.find(uuid));
            }
        }

        Pager<SearchResult<TaxonBase>> pager = service.findByDescriptionElementFullText(
                clazz, queryString, classification, features, languages, highlighting,
                pagerParams.getPageSize(), pagerParams.getPageIndex(),
                ((List<OrderHint>)null), getSimpleTaxonInitStrategy());
        return pager;
    }

    @RequestMapping(method = RequestMethod.GET, value={"findByFullText"})
    public Pager<SearchResult<TaxonBase>> dofindByFullText(
            @RequestParam(value = "clazz", required = false) Class<? extends TaxonBase> clazz,
            @RequestParam(value = "query", required = true) String queryString,
            @RequestParam(value = "tree", required = false) UUID treeUuid,
            @RequestParam(value = "languages", required = false) List<Language> languages,
            @RequestParam(value = "hl", required = false) Boolean highlighting,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException, LuceneParseException {

         logger.info("findByFullText : " + requestPathAndQuery(request)  );

         PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
         pagerParams.normalizeAndValidate(response);

         if(highlighting == null){
             highlighting = false;
         }

         Classification classification = null;
        if(treeUuid != null){
            classification = classificationService.find(treeUuid);
        }

        Pager<SearchResult<TaxonBase>> pager = service.findByFullText(clazz, queryString, classification, languages,
                highlighting, pagerParams.getPageSize(), pagerParams.getPageIndex(), ((List<OrderHint>)  null),
                initializationStrategy);
        return pager;
    }

    @RequestMapping(method = RequestMethod.GET, value={"findByEverythingFullText"})
    public Pager<SearchResult<TaxonBase>> dofindByEverythingFullText(
            @RequestParam(value = "clazz", required = false) Class<? extends TaxonBase> clazz,
            @RequestParam(value = "query", required = true) String queryString,
            @RequestParam(value = "tree", required = false) UUID treeUuid,
            @RequestParam(value = "languages", required = false) List<Language> languages,
            @RequestParam(value = "hl", required = false) Boolean highlighting,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException, LuceneParseException, LuceneMultiSearchException {

         logger.info("findByEverythingFullText : " + requestPathAndQuery(request) );

         PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
         pagerParams.normalizeAndValidate(response);

         if(highlighting == null){
             highlighting = false;
         }

         Classification classification = null;
        if(treeUuid != null){
            classification = classificationService.find(treeUuid);
        }

        Pager<SearchResult<TaxonBase>> pager = service.findByEverythingFullText(
                queryString, classification, languages, highlighting,
                pagerParams.getPageSize(), pagerParams.getPageIndex(),
                ((List<OrderHint>)null), initializationStrategy);
        return pager;
    }

    /**
     * @param areaSet
     */
    static public void includeAllSubAreas(Set<NamedArea> areaSet, ITermService termService) {
        Collection<NamedArea> tmpAreas = new HashSet<NamedArea>(areaSet);
        // expand all areas to include also the sub areas
        Pager<NamedArea> pager = null;
        while(true){
            pager = termService.getIncludes(tmpAreas, 1000, null, null);
            if(pager.getCount() == 0){
                break;
            }
            tmpAreas = pager.getRecords();
            tmpAreas.removeAll(areaSet);
            areaSet.addAll(tmpAreas);
        }
    }

    @RequestMapping(value = "findBestMatchingTaxon", method = RequestMethod.GET)
    public TaxonBase doFindBestMatchingTaxon(
            @RequestParam(value = "query", required = true) String taxonName,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException {

        logger.info("doFindBestMatchingTaxon : " + requestPathAndQuery(request) );

        Taxon bestMatchingTaxon =  service.findBestMatchingTaxon(taxonName);

        return bestMatchingTaxon;
    }

    /**
     * list IdentifiableEntity objects by identifiers
     *
     * @param type
     * @param identifierType
     * @param identifier
     * @param pageNumber
     * @param pageSize
     * @param matchMode
     * @param request
     * @param response
     * @return
     * @see AbstractIdentifiableListController#doFindByIdentifier(Class, String, String, Integer, Integer, MatchMode, Boolean, HttpServletRequest, HttpServletResponse)
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value={"findByIdentifier"}, params={"subtree"})
    public <T extends TaxonBase>  Pager<IdentifiedEntityDTO<T>> doFindByIdentifier(
            @RequestParam(value = "class", required = false) Class<T> type,
            @RequestParam(value = "identifierType", required = false) UUID identifierType,
            @RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
            @RequestParam(value = "includeEntity", required = false, defaultValue="false") Boolean includeEntity,
            @RequestParam(value = "subtree", required = true) UUID subtreeUuid,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException {

        DefinedTerm definedTerm = null;
        if(identifierType != null){
            definedTerm = CdmBase.deproxy(termService.find(identifierType), DefinedTerm.class);
        }

        TaxonNode subTree;
        Classification cl = classificationService.load(subtreeUuid);
        if (cl != null){
            subTree = cl.getRootNode();
        }else{
            subTree = taxonNodeService.find(subtreeUuid);
        }

        logger.info("doFindByIdentifier [subtreeUuid]  : " + request.getRequestURI() + "?" + request.getQueryString() );


        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber).normalizeAndValidate(response);

        matchMode = matchMode != null ? matchMode : MatchMode.EXACT;
        return service.findByIdentifier(type, identifier, definedTerm , subTree, matchMode, includeEntity, pagerParams.getPageSize(), pagerParams.getPageIndex(), initializationStrategy);
    }

    /**
     * List taxa by markers using a subtree filter
     *
     * @param type
     * @param markerType
     * @param value
     * @param pageNumber
     * @param pageSize
     * @param request
     * @param response
     * @return
     * @see AbstractIdentifiableListController#doFindByMarker(Class, UUID, Boolean, Integer, Integer, Boolean, HttpServletRequest, HttpServletResponse)
     * @see TaxonListController#doFindByIdentifier(Class, UUID, String, Integer, Integer, MatchMode, Boolean, UUID, HttpServletRequest, HttpServletResponse)
     * @see AbstractIdentifiableListController#doFindByIdentifier(Class, String, String, Integer, Integer, MatchMode, Boolean, HttpServletRequest, HttpServletResponse)
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value={"findByMarker"}, params={"subtree"})
    public <T extends TaxonBase>  Pager<MarkedEntityDTO<T>> doFindByMarker(
            @RequestParam(value = "class", required = false) Class<T> type,
            @RequestParam(value = "markerType", required = true) UUID markerTypeUuid,
            @RequestParam(value = "value", required = false) Boolean value,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "includeEntity", required = false, defaultValue="false") Boolean includeEntity,
            @RequestParam(value = "subtree", required = true) UUID subtreeUuid,
            @RequestParam(value = "titleType", required = false) TaxonTitleType titleType,
            HttpServletRequest request,
            HttpServletResponse response
            )
            throws IOException {

        MarkerType markerType = null;
        if(markerTypeUuid != null){
            DefinedTermBase<?> term = CdmBase.deproxy(termService.find(markerTypeUuid), MarkerType.class);
            if (term != null && term.isInstanceOf(MarkerType.class)){
                markerType = CdmBase.deproxy(term, MarkerType.class);
            }
        }

        TaxonNode subTree;
        Classification cl = classificationService.load(subtreeUuid);
        if (cl != null){
            subTree = cl.getRootNode();
        }else{
            subTree = taxonNodeService.find(subtreeUuid);
        }

        if (logger.isDebugEnabled()){logger.info("doFindByMarker [subtreeUuid]  : " + request.getRequestURI() + "?" + request.getQueryString() );}

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber).normalizeAndValidate(response);
        return service.findByMarker(type, markerType, value, subTree, includeEntity, titleType, pagerParams.getPageSize(), pagerParams.getPageIndex(), initializationStrategy);
    }

    @RequestMapping(value = "doFindByNameParts", method = RequestMethod.GET)
    public Pager<TaxonBase> doFindByNameParts(
            @RequestParam(value = "genusOrUninomial", required = false) String genusOrUninomial,
            @RequestParam(value = "infragenericEpithet", required = false) String infragenericEpithet,
            @RequestParam(value = "specificEpithet", required = false) String specificEpithet,
            @RequestParam(value = "infraspecificEpithet", required = false) String infraspecificEpithet,
            @RequestParam(value = "authorship", required = false) String authorship,
            @RequestParam(value = "rankUuid", required = false) UUID rankUuid,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException {

        logger.info("doFindByNameParts : " + requestPathAndQuery(request) );

        if (genusOrUninomial == null && infragenericEpithet == null && specificEpithet == null && infraspecificEpithet == null){
            response.sendError(404 , "At least 1 name part must be defined " );
            return null;
        }

        Rank rank = null;
        if (rankUuid != null){
             rank = findRank(rankUuid);
        }

        Pager<TaxonBase> result = service.findTaxaByName(null, genusOrUninomial, infragenericEpithet, specificEpithet, infraspecificEpithet, authorship, rank, pageSize, pageNumber);

        return result;
    }


    private Rank findRank(UUID rankUuid) {
        Rank rank = null;
        if(rankUuid != null){
            DefinedTermBase<?> definedTermBase =  termService.find(rankUuid);
            if(definedTermBase instanceof Rank){
                rank = (Rank) definedTermBase;
            } else {
               throw new IllegalArgumentException("DefinedTermBase is not a Rank");
            }
        }
        return rank;
    }

}
